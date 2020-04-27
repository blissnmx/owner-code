package com.nmx.framework.webmvc.servlet;/**
 * @author ning_mx
 * @date 15:42 2020/4/18
 * @description
 */

import java.io.File;

/**
 * @author ning_mx
 * @date 2020/4/18
 */
public class NViewResolver {
    private final String DEFAULT_TEMPLATE_SUFFIX = ".html";
    private File tempateRootDir;
    public NViewResolver(String templateRoot) {
        //String templateRootPath = this.getClass().getResource(templateRoot).getFile();
        tempateRootDir = new File(templateRoot);
    }

    public NView resolveViewName(String viewName) {
        if(null == viewName || "".equals(viewName.trim())){return null;}
        viewName = viewName.endsWith(DEFAULT_TEMPLATE_SUFFIX)? viewName : (viewName + DEFAULT_TEMPLATE_SUFFIX);
        File templateFile = new File((tempateRootDir.getPath() + "/" + viewName).replaceAll("/+","/"));
        return new NView(templateFile);
    }
}
