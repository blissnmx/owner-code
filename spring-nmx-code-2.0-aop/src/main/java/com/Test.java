package com;

import com.demo.DemoService;
import com.demo.service.DemoServiceImpl;
import com.nmx.framework.context.NApplicationContext;

/**
 * @author ning_mx
 * @date 2020/4/14
 */
public class Test {
    public static void main(String[] args) {
        try {
            NApplicationContext applicationContext = new NApplicationContext("application.properties");
            DemoService bean = (DemoService)applicationContext.getBean(DemoServiceImpl.class);
            String werwer = bean.queryInfo("werwer");
            System.out.println(werwer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
