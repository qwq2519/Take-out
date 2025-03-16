package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.BaseException;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import net.bytebuddy.asm.Advice;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void redisCleanCache(String pattern) {
        //清除所有分类菜品的缓存
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }

    @Override
    public void save(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        if (dish.getStatus() == null) {
            dish.setStatus(0);
        }

        //清除新增的菜品分类的redis缓存
        redisCleanCache("categoryId_" + dishDTO.getCategoryId());

        dishMapper.insert(dish);

        //获取insert语句生成的主键
        Long id = dish.getId();

        List<DishFlavor> flavorList = dishDTO.getFlavors();
        if (!CollectionUtils.isEmpty(flavorList)) {
            flavorList.forEach(flavor -> flavor.setDishId(id));

            dishFlavorMapper.insertBatch(flavorList);
        }

    }

    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        List<DishVO> list = dishMapper.listByPage(dishPageQueryDTO);

        Page<DishVO> p = (Page<DishVO>) list;
        return new PageResult(p.getTotal(), p.getResult());
    }

    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }

        //一次将所有dish都查询下来
        List<Dish> dishes = dishMapper.listByIds(ids);

        //处于启用状态的不能删除
        for (Dish dish : dishes) {
            if (StatusConstant.ENABLE.equals(dish.getStatus())) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        //套餐表中有的不能删除
        List<SetmealDish> setmealIds = setmealDishMapper.listByDishIds(ids);

        if (!CollectionUtils.isEmpty(setmealIds)) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        //清除redis所有缓存
        redisCleanCache("categoryId_*");

        //删除dish时，关联的dishflavor也要删除
        ids.forEach(
                id -> {
                    dishFlavorMapper.deleteByDishId(id);
                    dishMapper.deleteById(id);
                }
        );
    }


    @Override
    public DishVO getById(Long id) {
        DishVO byId = dishMapper.getById(id);

        List<DishFlavor> byDishId = dishFlavorMapper.getByDishId(byId.getId());
        byId.setFlavors(byDishId);

        return byId;
    }

    @Transactional
    @Override
    public void update(DishDTO dishDTO) {

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        //清除对应分类的redis所有缓存
        redisCleanCache("categoryId_*");

        dishMapper.update(dish);

        dishFlavorMapper.deleteByDishId(dish.getId());

        List<DishFlavor> flavorList = dishDTO.getFlavors();
        if (!CollectionUtils.isEmpty(flavorList)) {
            flavorList.forEach(flavor -> {
                if (flavor.getId() == null) {
                    flavor.setDishId(dish.getId());
                }
            });
            dishFlavorMapper.insertBatch(flavorList);
        }
    }

    @Override
    public List<Dish> listByCategoryId(Long categoryId) {
        return dishMapper.listByCategoryId(categoryId);
    }

    @Override
    public void updateStatus(Integer status, Long id) {
        DishVO dishVO = dishMapper.getById(id);

        if (status.equals(dishVO.getStatus())) {
            return;
        }

        Dish dish = new Dish();
        dish.setId(id);
        dish.setStatus(status);


        //清除redis所有缓存
        redisCleanCache("categoryId_*");


        //要起售，直接起售
        if (StatusConstant.ENABLE.equals(status)) {
            dishMapper.update(dish);
            return;
        }


        //如果要停售,要看看有没有处于起售的套餐


        //先查询套餐-菜品表
        List<SetmealDish> setmealDishList = setmealDishMapper.listByDishIds(List.of(id));


        //该菜品存在关联的套餐
        if(!CollectionUtils.isEmpty(setmealDishList)) {
            //将其中的套餐id列出来
            List<Long> setmealIds = setmealDishList.stream().map(SetmealDish::getSetmealId).collect(Collectors.toList());

            List<Setmeal> setmealList = setmealMapper.listByIds(setmealIds);

                for (Setmeal setmeal : setmealList) {
                    //有处于起售的套餐，不能停售
                    if (StatusConstant.ENABLE.equals(setmeal.getStatus())) {
                        throw new BaseException(MessageConstant.DISH_IN_ACTIVE_SETMEAL_CANNOT_DISABLE);
                    }
                }

        }
        //可以停售
        dishMapper.update(dish);

    }

    /**
     * 条件查询菜品和口味
     *
     * @param dish
     * @return
     */
    @Override
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d, dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }


        //加入redis缓存
        String key = "categoryId_" + dish.getCategoryId();
        redisTemplate.opsForValue().set(key, dishVOList);

        return dishVOList;
    }
}
