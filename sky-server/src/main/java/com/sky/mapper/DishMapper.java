package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     *
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);


    @AutoFill(OperationType.INSERT)
    void insert(Dish dish);

    @AutoFill(OperationType.UPDATE)
    void update(Dish dish);

    List<DishVO> listByPage(DishPageQueryDTO dishPageQueryDTO);

    List<Dish> listByIds(List<Long> ids);

    @Select("SELECT * FROM dish WHERE category_id = #{categoryId}")
    List<Dish> listByCategoryId(Long categoryId);

    @Delete("DELETE FROM dish WHERE id=#{id}")
    void deleteById(Long id);

    DishVO getById(Long id);

    @Select("SELECT dish.* FROM dish LEFT JOIN setmeal_dish ON dish.id = setmeal_dish.dish_id WHERE setmeal_dish.setmeal_id=#{id}")
    List<Dish> listBySetmealId(Long id);

    List<Dish> list(Dish dish);
    /**
     * 根据条件统计菜品数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
