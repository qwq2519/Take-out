package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    void insertBatch(List<SetmealDish> setmealDishList);

    List<SetmealDish> listByDishIds(List<Long> ids);

    void deleteBySetmealIds(List<Long> ids);


}
