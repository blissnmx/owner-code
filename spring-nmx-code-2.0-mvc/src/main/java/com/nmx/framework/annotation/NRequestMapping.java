package com.nmx.framework.annotation;

import java.lang.annotation.*;

/**
 * @author ning_mx
 * @date 16:04 2020/4/14
 * @description
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NRequestMapping {
    String value() default "";
}
