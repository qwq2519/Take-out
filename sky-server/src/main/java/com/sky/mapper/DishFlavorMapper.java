package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
    //不需要更新公共字段,没有这个属性
    void insertBatch(List<DishFlavor> flavorList);


    @Delete("DELETE FROM dish_flavor  WHERE dish_id = #{id}")
    void deleteByDishId(Long id);
}
