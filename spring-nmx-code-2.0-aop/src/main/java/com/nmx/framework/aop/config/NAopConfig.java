package com.nmx.framework.aop.config;/**
 * @author ning_mx
 * @date 19:49 2020/4/26
 * @description
 */

import lombok.Data;

/**
 * @author ning_mx
 * @date 2020/4/26
 */
@Data
public class NAopConfig {
    private String pointCut;
    private String aspectClass;
    private String aspectBefore;
    private String aspectAfter;
    private String aspectAfterThrow;
    private String aspectAfterThrowingName;
}
