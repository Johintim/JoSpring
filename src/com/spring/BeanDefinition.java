package com.spring;

/**
 * @author tyd
 * @date 2021/10/8 10:33
 * 描述一个bean,属于哪个类镜像，以及作用范围
 */
public class BeanDefinition {
    private Class beanClass;
    private String scope;

    public Class getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class beanClass) {
        this.beanClass = beanClass;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
