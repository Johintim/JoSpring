package com.spring;

/**
 * @author tyd
 * @date 2021/10/8 13:52
 * 这个可能有多个，可以在一个bean的初始化前后执行一些操作
 */
public interface BeanPostProcessor {
    // 初始化前，也可以做一些设置，可以自定义相关的操作
    default Object postProcessBeforeInitialization(String beanName, Object bean){
        return bean;
    }
    // 初始化后，这个可以用来AOP
    default Object postProcessAfterInitialization(String beanName, Object bean){
        return bean;
    }
}
