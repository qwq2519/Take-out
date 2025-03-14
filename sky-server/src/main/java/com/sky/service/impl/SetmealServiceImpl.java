package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.annotation.AutoFill;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    SetmealDishMapper setmealDishMapper;

    @Autowired
    SetmealMapper setmealMapper;

    @Autowired
    DishMapper dishMapper;


    @Transactional
    @Override
    public void save(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        //插入套餐，获取数据库返回的id
        setmeal.setStatus(StatusConstant.DISABLE);
        setmealMapper.insert(setmeal);

        Long id = setmeal.getId();

        //插入套餐菜品关系
        List<SetmealDish> setmealDishList = setmealDTO.getSetmealDishes();
        setmealDishList.forEach(setmealDish -> {
            setmealDish.setSetmealId(id);
        });

        setmealDishMapper.insertBatch(setmealDishList);

    }

    @Override
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());

        List<SetmealVO> list = setmealMapper.listByPage(setmealPageQueryDTO);
        Page<SetmealVO> p = (Page<SetmealVO>) list;

        return new PageResult(p.getTotal(), p.getResult());
    }

    @Override
    public void deleteBatch(List<Long> ids) {
        //查出所有套餐
        List<Setmeal> list = setmealMapper.listByIds(ids);

        //起售的套餐不能删除
        for (Setmeal setmeal : list) {
            if (StatusConstant.ENABLE.equals(setmeal.getStatus())) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }

        //删除套餐菜品表中的信息
        setmealDishMapper.deleteBySetmealIds(ids);

        //删除套餐
        setmealMapper.deleteByIds(ids);
    }

    @Override
    public SetmealVO getById(Long id) {
        return setmealMapper.getById(id);
    }

    @Transactional
    @Override
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        //删除原来的套餐菜品信息
        setmealDishMapper.deleteBySetmealIds(List.of(setmeal.getId()));


        //插入新的套餐-菜品
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmeal.getId());
        });
        setmealDishMapper.insertBatch(setmealDishes);

        //更新套餐
        setmealMapper.update(setmeal);
    }

    @Override
    public void updateStatus(Integer status, Long id) {
        //查询套餐完整信息
        SetmealVO setMeal = setmealMapper.getById(id);

        //状态一样，那就没必要更新了
        if(status.equals(setMeal.getStatus())) {
            return;
        }


        Setmeal setmeal = new Setmeal();
        setmeal.setStatus(status);
        setmeal.setId(id);


        //停售套餐，直接停了
        if(status.equals(StatusConstant.DISABLE)) {
            setmealMapper.update(setmeal);
            return;
        }

        //如果要起售，那么菜品中不能有停售的

        List<Dish> dishes = dishMapper.listBySetmealId(id);
        if(CollectionUtils.isEmpty(dishes)){
            setmealMapper.update(setmeal);
            return ;
        }
        dishes.forEach(dish -> {
                if (StatusConstant.DISABLE.equals(dish.getStatus())) {
                    throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                }
            });
        setmealMapper.update(setmeal);
//      方案二
// 得到套餐-菜品关系表
//        {
//            List<SetmealDish> setmealDishes = setMeal.getSetmealDishes();
//
//            //将每个SetmealDish的菜品id提取为一个list,得到菜品集合
//            List<Long> dishIds = setmealDishes.stream().map(SetmealDish::getDishId).collect(Collectors.toList());
//            List<Dish> dishes = dishMapper.listByIds(dishIds);
//
//            //检查是否有停售的菜品
//            dishes.forEach(dish -> {
//                if (StatusConstant.DISABLE.equals(dish.getStatus())) {
//                    throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
//                }
//            });
//        }

    }
}
