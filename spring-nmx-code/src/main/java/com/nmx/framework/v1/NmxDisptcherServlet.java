package com.nmx.framework.v1;

import com.nmx.framework.annotation.NAutowired;
import com.nmx.framework.annotation.NController;
import com.nmx.framework.annotation.NRequestMapping;
import com.nmx.framework.annotation.NService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @author ning_mx
 * @date 2020/4/14
 */
public class NmxDisptcherServlet extends HttpServlet{
    private Map<String,Object> iocMapping = new HashMap<String, Object>();
    private List<String> clazzs = new ArrayList<String>();

    private Map<String, Object> handlerMapping = new HashMap<String,Object>();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
       this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Exception"+ Arrays.toString(e.getStackTrace()));
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String uri = req.getRequestURI();
        //1、匹配handlerMapping
        String contextPath = req.getContextPath();
        System.out.println(contextPath);
        uri = uri.replace(contextPath, "").replaceAll("/+", "/");
        System.out.println(uri);
        if(!handlerMapping.containsKey(uri)){
            resp.getWriter().write("404 Not Found !");
            return;
        }
        //2、反射调用invoker方法
        Method method = (Method) handlerMapping.get(uri);
        Map<String, String[]> parameterMap = req.getParameterMap();
        Class<?> declaringClass = method.getDeclaringClass();
        System.out.println(declaringClass);
        Object o = iocMapping.get(declaringClass.getName());
        method.invoke(o,new Object[]{req,resp,parameterMap.get("keyword")[0]});
        //3、返回结果输出到浏览器

    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        InputStream is = null ;
        try {
            //1、加载配置文件
            Properties properties = new Properties();
            String contextConfigLocation = config.getInitParameter("contextConfigLocation").replaceAll("classpath:","");
           is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
            properties.load(is);
            //2、扫描相关类
            String scanPackage = properties.getProperty("scanPackage");
            doScanner(scanPackage);
            //3、创建实例化并保存至容器
            for (String clazzName : clazzs) {
                Class<?> clazz = Class.forName(clazzName);
                if(clazz.isAnnotationPresent(NController.class)){
                    iocMapping.put(clazzName, clazz.newInstance());
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
                        handlerMapping.put(url, method);
                        System.out.println("Mapped :"+url+" method:"+method);
                    }
                }else if(clazz.isAnnotationPresent(NService.class)){
                    NService service = clazz.getAnnotation(NService.class);
                    String beanName = service.value();
                    if("".equals(beanName)){
                        beanName = clazz.getName();
                    }
                    Object instance = clazz.newInstance();
                    iocMapping.put(beanName, instance);
                    for(Class<?> i : clazz.getInterfaces()){
                        iocMapping.put(i.getName(), instance);
                    }
                }else{
                    System.out.println("unkonw class");
                    continue;
                }
            }
            //4、DI操作,依赖注入
            for(Object object : iocMapping.values()){
                if(object == null ) continue;
                Class<?> clazz = object.getClass();
                System.out.println(clazz.getName());
                if(clazz.isAnnotationPresent(NController.class) || clazz.isAnnotationPresent(NService.class)){
                    Field[] declaredFields = clazz.getDeclaredFields();
                    for (Field field :declaredFields) {
                        if (!field.isAnnotationPresent(NAutowired.class)) continue;
                        NAutowired annotation = field.getAnnotation(NAutowired.class);
                        String beanName = annotation.value();
                        if("".equalsIgnoreCase(beanName)){
                            beanName = field.getType().getName();
                        }
                        field.setAccessible(true);
                        field.set(iocMapping.get(clazz.getName()), iocMapping.get(beanName));
                    }
                }else{
                    System.out.println("unknow bean ");
                }

            }


        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(is !=null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("NMX MVC Framework is init finish");
    }

    private void doScanner(String scanPackage) throws ClassNotFoundException {
        URL filePath = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File classDir = new File(filePath.getFile());
        for (File file : classDir.listFiles()) {
            if(file.isDirectory()){
                doScanner(scanPackage+"."+file.getName());
            }else{
                if(!file.getName().endsWith(".class")) continue;
                //class类全限定名
                String clazzName = (scanPackage + "." + file.getName().replace(".class", ""));
                clazzs.add(clazzName);
            }
        }
    }


}
