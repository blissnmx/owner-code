package com.nmx.framework.webmvc.servlet;

import com.nmx.framework.annotation.*;
import com.nmx.framework.context.NApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ning_mx
 * @date 2020/4/14
 */
public class NDisptcherServlet extends HttpServlet{
    private NApplicationContext applicationContext;
    //private Map<String,Object> iocMapping = new HashMap<String, Object>();
    //private List<String> clazzs = new ArrayList<String>();
    private Map<NHanderMapping, NHanderAdaptor> handerAdaptors = new HashMap<NHanderMapping, NHanderAdaptor>();
    private List<NHanderMapping> handlerMapping = new ArrayList<NHanderMapping>();
    private List<NViewResolver> viewResolvers = new ArrayList<NViewResolver>();

    //private Properties configContext = new Properties();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req,resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Exception"+ Arrays.toString(e.getStackTrace()));
        }
    }
    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        //完成了对HandlerMapping的封装
        //完成了对方法返回值的封装ModelAndView

        //1、通过URL获得一个HandlerMapping
        NHanderMapping handler = getHandler(req);
        if(handler == null){
            processDispatchResult(req,resp,new NModelAndView("404.html"));
            return;
        }

        //2、根据一个HandlerMaping获得一个HandlerAdapter
        NHanderAdaptor handlerAdapter = getHandlerAdapter(handler);

        //3、解析某一个方法的形参和返回值之后，统一封装为ModelAndView对象
        NModelAndView mv = handlerAdapter.handler(req,resp,handler);

        // 就把ModelAndView变成一个ViewResolver
        processDispatchResult(req,resp,mv);


    }

    private NHanderAdaptor  getHandlerAdapter(NHanderMapping handler) {
        if(!handerAdaptors.isEmpty()){
            return handerAdaptors.get(handler);
        }

        return null ;
    }

    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, NModelAndView mv) throws Exception {
        if(null == mv){return;}
        if(this.viewResolvers.isEmpty()){return;}

        for (NViewResolver viewResolver : this.viewResolvers) {
            NView view = viewResolver.resolveViewName(mv.getViewName());
            //直接往浏览器输出
            view.render(mv.getModel(),req,resp);
            return;
        }
    }

    private NHanderMapping getHandler(HttpServletRequest req) {
        if(handlerMapping.isEmpty()) return null;
        String uri = req.getRequestURI();
        //1、匹配handlerMapping
        String contextPath = req.getContextPath();
        System.out.println(contextPath);
        uri = uri.replace(contextPath, "").replaceAll("/+", "/");
        System.out.println(uri);
        for(NHanderMapping handler:handlerMapping){
            Matcher matcher = handler.pattern.matcher(uri);
            if(!matcher.matches()){continue;}
            return handler;
        }
        return null;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {

        try {
            applicationContext = new NApplicationContext(config.getInitParameter("contextConfigLocation"));

            //初始化mvc九大组件
            initStratefies(applicationContext);

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("NMX MVC Framework is init finish");
    }

    private void initStratefies(NApplicationContext context) throws Exception {
        //        //多文件上传的组件
//        initMultipartResolver(context);
//        //初始化本地语言环境
//        initLocaleResolver(context);
//        //初始化模板处理器
//        initThemeResolver(context);
        //handlerMapping
        initHandlerMappings(context);
        //初始化参数适配器
        initHandlerAdapters(context);
//        //初始化异常拦截器
//        initHandlerExceptionResolvers(context);
//        //初始化视图预处理器
//        initRequestToViewNameTranslator(context);
        //初始化视图转换器
        initViewResolvers(context);
//        //FlashMap管理器
//        initFlashMapManager(context);
    }

    private void initHandlerAdapters(NApplicationContext context) {
        for(NHanderMapping handerMapping:handlerMapping){
            handerAdaptors.put(handerMapping, new NHanderAdaptor());
        }
    }

    private void initViewResolvers(NApplicationContext context) {
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

        File templateRootDir = new File(templateRootPath);
        for (File file : templateRootDir.listFiles()) {
            if(!file.isDirectory()){
                this.viewResolvers.add(new NViewResolver(templateRootPath));
                continue;
            }else{
                //其他模板
            }
        }
    }

    private void initHandlerMappings(NApplicationContext applicationContext) throws Exception {
        if(applicationContext.getBeanDefinitionCount() == 0){ return;}

        for (String beanName : applicationContext.getBeanDefinitionNames()) {
            Object instance = applicationContext.getBean(beanName);

            Class<?> clazz= instance.getClass();
            if(!clazz.isAnnotationPresent(NController.class)) continue;
            String baseUrl = "";
            if(clazz.isAnnotationPresent(NRequestMapping.class)){
                NRequestMapping nRequestMapping = clazz.getAnnotation(NRequestMapping.class);
                baseUrl = nRequestMapping.value();
            }
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                //排除没有注解的方法
                if(!method.isAnnotationPresent(NRequestMapping.class)) continue;
                NRequestMapping annotationMethod = method.getAnnotation(NRequestMapping.class);
                String url =(baseUrl +"/"+ annotationMethod.value()).replaceAll("/+","/");
                Pattern pattern = Pattern.compile(url);
                handlerMapping.add(new NHanderMapping(instance,method,pattern));
                System.out.println("Mapped :"+url+" method:"+method);
            }
        }
    }


}
