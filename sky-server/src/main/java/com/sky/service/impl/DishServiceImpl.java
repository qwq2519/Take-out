package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public void save(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        if (dish.getStatus() == null) {
            dish.setStatus(0);
        }

        dishMapper.insert(dish);

        //获取insert语句生成的主键
        Long id = dish.getId();

        List<DishFlavor> flavorList = dishDTO.getFlavors();
        if (!CollectionUtils.isEmpty(flavorList)) {
            flavorList.forEach(flavor -> {
                flavor.setDishId(id);
            });
            dishFlavorMapper.insertBatch(flavorList);
        }

    }

    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());

        List<DishVO> list = dishMapper.list(dishPageQueryDTO);

        Page<DishVO> p=(Page<DishVO>)list;
        return new PageResult(p.getTotal(),p.getResult());
    }

    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {
        if(CollectionUtils.isEmpty(ids)){
            return ;
        }

        //一次将所有dish都查询下来
        List<Dish> dishes = dishMapper.listByIds(ids);

        //处于启用状态的不能删除
        for (Dish dish : dishes) {
            if(StatusConstant.ENABLE.equals(dish.getStatus())){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        //套餐表中有的不能删除
        List<Long> setmealIds= setmealMapper.listByDishIds(ids);

        if(!CollectionUtils.isEmpty(setmealIds)){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        //删除dish时，关联的dishflavor也要删除
        ids.forEach(
                id->{
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
}
