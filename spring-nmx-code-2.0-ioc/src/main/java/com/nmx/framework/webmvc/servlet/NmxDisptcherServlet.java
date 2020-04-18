package com.nmx.framework.webmvc.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author ning_mx
 * @date 2020/4/14
 */
public class NmxDisptcherServlet extends HttpServlet{

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {

        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Exception"+ Arrays.toString(e.getStackTrace()));
        }
    }


}
