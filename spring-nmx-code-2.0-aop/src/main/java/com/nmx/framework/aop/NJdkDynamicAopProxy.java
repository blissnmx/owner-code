package com.nmx.framework.aop;/**
 * @author ning_mx
 * @date 19:47 2020/4/26
 * @description
 */

import com.nmx.framework.aop.aspect.NAdvice;
import com.nmx.framework.aop.support.NAdvisedSupport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * @author ning_mx
 * @date 2020/4/26
 */
public class NJdkDynamicAopProxy implements InvocationHandler{
    private NAdvisedSupport config;

    public NJdkDynamicAopProxy(NAdvisedSupport config) {
        this.config = config;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Map<String,NAdvice> advices = config.getAdvices(method,null);

        Object returnValue;
        try {
            invokeAdivce(advices.get("before"));

            returnValue = method.invoke(this.config.getTarget(),args);

            invokeAdivce(advices.get("after"));
        }catch (Exception e){
            invokeAdivce(advices.get("afterThrow"));
            throw e;
        }

        return returnValue;
    }

    private void invokeAdivce(NAdvice advice) {
        try {
            advice.getAdviceMethod().invoke(advice.getAspect());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public Object getProxy() {
        return Proxy.newProxyInstance(this.getClass().getClassLoader(),this.config.getTargetClass().getInterfaces(),this);
    }
}
