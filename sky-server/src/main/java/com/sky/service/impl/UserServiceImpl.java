package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    public static final String WX_LOGIN="https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;

    @Override
    public User wxlogin(UserLoginDTO userLoginDTO) {

        String code = userLoginDTO.getCode();
        String openId = getOpenId(code);

        if(openId==null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        //判断是否为新用户
        User user = userMapper.getByOpenId(openId);

        //新用户，那么就插入数据库
        if(user==null){
            user=User.builder()
                    .createTime(LocalDateTime.now())
                    .openid(openId)
                    .build();
            userMapper.insert(user);
        }

        return user;
    }

    /**
     * 调用微信服务，获取用户的openid
     * @param code
     * @return
     */
    private String getOpenId(String code){
        Map<String,String> map=new HashMap<>();

        //配置Get请求的参数
        map.put("appid",weChatProperties.getAppid());
        map.put("secret",weChatProperties.getSecret());
        map.put("js_code",code);
        map.put("grant_type","authorization_code");

        String json = HttpClientUtil.doGet(WX_LOGIN, map);

        //响应结果是json，要解析
        JSONObject jsonObject = JSONObject.parseObject(json);
        return jsonObject.getString("openid");
    }
}
