package com.jo.service;

import com.jo.JoAnnotation;
import com.spring.*;

/**
 * @author tyd
 * @date 2021/10/7 15:49
 */
@Component("userService")
@Scope
public class UserService implements InitializingBean, BeanNameAware {

    @Autowired
    private OrderService orderService;

    @JoAnnotation("xxx")
    private String beforeString;

    private String beanName;

    public void test() {
        System.out.println("执行userService.test()----start----");
        System.out.println(beforeString);
        System.out.println(beanName);
        System.out.println("执行userService.test()----end----");
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("afterPropertiesSet() => 初始化userService");
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }
}
