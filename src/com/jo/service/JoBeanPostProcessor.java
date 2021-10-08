package com.jo.service;

import com.jo.JoAnnotation;
import com.spring.BeanPostProcessor;
import com.spring.Component;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author tyd
 * @date 2021/10/8 13:54
 */
@Component
public class JoBeanPostProcessor implements BeanPostProcessor {
    // 假设自己自定义了一个注解，想把这个注解上的值，注入到这个属性上。就可以这么做
    @Override
    public Object postProcessBeforeInitialization(String beanName, Object bean) {
        for (Field field : bean.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(JoAnnotation.class)) {
                field.setAccessible(true);
                String value = field.getAnnotation(JoAnnotation.class).value();
                try {
                    field.set(bean,value);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(String beanName, Object bean) {
        if ("aopTestService".equals(beanName)) {
            Object proxyInstance = Proxy.newProxyInstance(JoBeanPostProcessor.class.getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    // 切面逻辑
                    System.out.println("切面方法");

                    return method.invoke(bean, args);
                }
            });
            return proxyInstance;
        }
        return bean;
    }
}
