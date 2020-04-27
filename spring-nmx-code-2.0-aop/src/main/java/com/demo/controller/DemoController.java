package com.demo.controller;

import com.nmx.framework.annotation.NAutowired;
import com.nmx.framework.annotation.NController;
import com.nmx.framework.annotation.NRequestMapping;
import com.nmx.framework.annotation.NRequestParam;
import com.demo.DemoService;
import com.nmx.framework.webmvc.servlet.NModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
            System.out.println(keyword);
            resp.setContentType("text/html;charset=utf-8");
            resp.getWriter().write(demoService.queryInfo(keyword));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @NRequestMapping("/first.html")
    public NModelAndView queryName(@NRequestParam("name") String name){
        String result = demoService.queryInfo(name);
        Map<String,Object> model = new HashMap<String,Object>();
        model.put("name", name);
        model.put("data", result);
        model.put("token", "123456");
        return new NModelAndView("first.html",model);
    }
}
