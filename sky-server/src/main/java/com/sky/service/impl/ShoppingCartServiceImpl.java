package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;


    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);

        //用户只能操作自己的购物车
        shoppingCart.setUserId(BaseContext.getCurrentId());

        //查询用户的购物车是否已经存在
        List<ShoppingCart> shoppingCartList=shoppingCartMapper.list(shoppingCart);


        //为空，直接添加
        if(CollectionUtils.isEmpty(shoppingCartList)){
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCart.setNumber(1);

            if(shoppingCart.getDishId()!=null){
                DishVO item = dishMapper.getById(shoppingCart.getDishId());
                shoppingCart.setImage(item.getImage());
                shoppingCart.setName(item.getName());
                shoppingCart.setAmount(item.getPrice());
            }else{
                SetmealVO item = setmealMapper.getById(shoppingCart.getSetmealId());
                shoppingCart.setImage(item.getImage());
                shoppingCart.setName(item.getName());
                shoppingCart.setAmount(item.getPrice());
            }

            shoppingCartMapper.insert(shoppingCart);

            return;
        }

        //不为空，那就是增加数量
        shoppingCart.setCreateTime(LocalDateTime.now());
        ShoppingCart shoppingCartExist = shoppingCartList.get(0);

        shoppingCartExist.setNumber(shoppingCartExist.getNumber()+1);

        shoppingCartMapper.updateNumberById(shoppingCartExist);

    }

    @Override
    public List<ShoppingCart> list() {

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        return shoppingCartList;
    }
}
