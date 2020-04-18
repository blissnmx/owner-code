package com;

import com.demo.controller.DemoController;
import com.nmx.framework.context.NApplicationContext;

/**
 * @author ning_mx
 * @date 2020/4/14
 */
public class Test {
    public static void main(String[] args) {
        try {
            NApplicationContext applicationContext = new NApplicationContext("application.properties");
            DemoController bean = (DemoController)applicationContext.getBean(DemoController.class);

            System.out.println(bean);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
