package com.jo;

import com.jo.service.AopInterface;
import com.jo.service.UserService;
import com.spring.JoApplicationContext;


/**
 * @author tyd
 * @date 2021/10/7 15:40
 */
public class JoTest {
    public static void main(String[] args) {
        JoApplicationContext joApplicationContext = new JoApplicationContext(AppConfig.class);
        UserService userService = (UserService)joApplicationContext.getBean("userService");
        userService.test();
        System.out.println("");
        System.out.println("开始测试AOP");

        // TODO: 2022/3/16  由于使用的是JDK的动态代理，代理的其实是接口，因此下面在getBean时，拿的是AopInterface,而不是aopTestService
        AopInterface aopTestService = (AopInterface)joApplicationContext.getBean("aopTestService");
        aopTestService.test();
    }
}
