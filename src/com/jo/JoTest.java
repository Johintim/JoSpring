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

        AopInterface aopTestService = (AopInterface)joApplicationContext.getBean("aopTestService");
        aopTestService.test();
    }
}
