package com.nmx.framework.annotation;

import java.lang.annotation.*;

/**
 * @author ning_mx
 * @date 16:03 2020/4/14
 * @description
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NService {
    String value() default "";
}
