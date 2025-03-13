package com.sky.aspect;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Slf4j
@Component
public class AutoFillAspect {
    @Pointcut("execution(* com.sky.mapper.*.*(..)) " +
            "&& @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointcut() {}

    @Before("autoFillPointcut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始进行公共字段填充");

        //获取当前方法签名->注解——>操作类型
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        AutoFill annotation = methodSignature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType = annotation.value();

        //获取当前方法参数
        Object[] args = joinPoint.getArgs();
        if(args == null || args.length == 0) {
            return;
        }
        Object element = args[0];

        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        setFieldHelper(element, AutoFillConstant.SET_UPDATE_TIME, now, LocalDateTime.class);
        setFieldHelper(element, AutoFillConstant.SET_UPDATE_USER, currentId, Long.class);

        if(operationType == OperationType.INSERT) {
            setFieldHelper(element, AutoFillConstant.SET_CREATE_TIME, now, LocalDateTime.class);
            setFieldHelper(element, AutoFillConstant.SET_CREATE_USER, currentId, Long.class);
        }
    }

    private void setFieldHelper(Object target,String methodName,Object value,Class<?> paramType){
        try {
            Method method = target.getClass().getDeclaredMethod(methodName, paramType);
            method.invoke(target,value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(String.format("字段填充失败 @%s :%s",methodName,e.getMessage()));
        }
    }

}
