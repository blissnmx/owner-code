package com.nmx.framework.beans.config;

/**
 * @author ning_mx
 * @date 2020/4/15
 */

public class NBeanDefinition {
    private String beanClassName ; //类全限定名
    private String factoryBeanName ;//简单类名

    public NBeanDefinition(String factoryBeanName, String beanClassName) {
        this.beanClassName = beanClassName;
        this.factoryBeanName = factoryBeanName;
    }

    public String getBeanClassName() {
        return beanClassName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }

    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }
}
