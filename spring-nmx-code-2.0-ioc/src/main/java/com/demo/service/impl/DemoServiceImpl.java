package com.demo.service.impl;/**
 * @author ning_mx
 * @date 16:58 2020/4/14
 * @description
 */

import com.demo.service.DemoService;
import com.nmx.framework.annotation.NService;

/**
 * @author ning_mx
 * @date 2020/4/14
 */
@NService
public class DemoServiceImpl implements DemoService {
    @Override
    public String queryInfo(String keyword) {

        return "请求参数 :"+keyword;
    }
}
