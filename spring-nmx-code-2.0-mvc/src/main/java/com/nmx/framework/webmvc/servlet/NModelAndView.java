package com.nmx.framework.webmvc.servlet;/**
 * @author ning_mx
 * @date 15:18 2020/4/18
 * @description
 */

import java.util.Map;

/**
 * @author ning_mx
 * @date 2020/4/18
 */
public class NModelAndView {
    private String viewName;
    private Map<String,?> model;

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public Map<String, ?> getModel() {
        return model;
    }

    public void setModel(Map<String, ?> model) {
        this.model = model;
    }

    public NModelAndView(String s) {
    }
}
