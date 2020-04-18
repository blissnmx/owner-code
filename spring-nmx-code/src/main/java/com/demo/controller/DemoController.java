package com.demo.controller;

import com.nmx.framework.annotation.NAutowired;
import com.nmx.framework.annotation.NController;
import com.nmx.framework.annotation.NRequestMapping;
import com.nmx.framework.annotation.NRequestParam;
import com.demo.DemoService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author ning_mx
 * @date 2020/4/14
 */
@NController
@NRequestMapping(value = "/demo")
public class DemoController {

    @NAutowired
    private DemoService demoService ;

    @NRequestMapping("/query")
    public void query(HttpServletRequest req, HttpServletResponse resp, @NRequestParam(value = "keyword",require = false) String keyword){
        try {
            resp.setContentType("text/html;charset=utf-8");
            resp.getWriter().write(demoService.queryInfo(keyword));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
