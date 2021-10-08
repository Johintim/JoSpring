package com.jo.service;

import com.spring.Component;

/**
 * @author tyd
 * @date 2021/10/8 14:32
 */
@Component
public class AopTestService implements AopInterface {
    @Override
    public void test() {
        System.out.println("test1111");
    }
}
