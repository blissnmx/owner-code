package com.nmx.framework.aop.aspect;/**
 * @author ning_mx
 * @date 19:52 2020/4/26
 * @description
 */

import lombok.Data;

import java.lang.reflect.Method;

/**
 * @author ning_mx
 * @date 2020/4/26
 */
@Data
public class NAdvice {
    private Object aspect;
    private Method adviceMethod;
    private String throwName;

    public NAdvice(Object aspect, Method adviceMethod) {
        this.aspect = aspect;
        this.adviceMethod = adviceMethod;
    }
}
