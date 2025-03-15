package com.sky.controller.admin;


import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = "店铺操作")
public class ShopController {

    public static final String KEY="SHOP_STATUS";

    @Autowired
    RedisTemplate redisTemplate;

    @GetMapping("/status")
    @ApiOperation("获取店铺营业状态")
    public Result<Integer> getStatus() {
        log.info("管理端获取店铺营业状态");
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY);
        return Result.success(status);
    }
    @PutMapping("/{status}")
    @ApiOperation("设置店铺营业状态")
    public Result updateStatus(@PathVariable Integer status) {
        redisTemplate.opsForValue().set(KEY,status);
        return Result.success();
    }

}
