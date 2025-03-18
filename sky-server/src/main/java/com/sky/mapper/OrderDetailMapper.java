package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderDetailMapper {
    @Select("SELECT * FROM order_detail WHERE order_id = #{id}")
    List<OrderDetail> listByOrderId(Long id);


    void insertBatch(List<OrderDetail> orderDetailList);

    @Select("SELECT * FROM order_detail WHERE order_id = #{id}")
    List<OrderDetail> getByOrderId(Long id);
}
