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
    private Map<String,Object> iocMapping = new HashMap<String, Object>();
    private List<String> clazzs = new ArrayList<String>();
    private Map<NHanderMapping, NHanderAdaptor> handerAdaptors = new HashMap<NHanderMapping, NHanderAdaptor>();
    private List<NHanderMapping> handlerMapping = new ArrayList<NHanderMapping>();
    private List<NViewResolver> viewResolvers = new ArrayList<NViewResolver>();

    private Properties configContext = new Properties();
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
            processDispatchResult(req,resp,new NModelAndView("404"));
            return;
        }

        //2、根据一个HandlerMaping获得一个HandlerAdapter
        NHanderAdaptor handlerAdapter = getHandlerAdapter(handler);

        //3、解析某一个方法的形参和返回值之后，统一封装为ModelAndView对象
        NModelAndView mv = handlerAdapter.handler(req,resp,handler);

        // 就把ModelAndView变成一个ViewResolver
        processDispatchResult(req,resp,mv);

       /* //2、反射调用invoker方法
        Method method = handler.method;
        Map<String, String[]> parameterMap = req.getParameterMap();
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] paramValues = new Object[parameterTypes.length];
        for(Map.Entry<String,String[]> entry : parameterMap.entrySet()){
            if(handler.paramIndexMapping.containsKey(entry.getKey())){
                Integer index = handler.paramIndexMapping.get(entry.getKey());
                String strValue = Arrays.toString(entry.getValue()).replaceAll("\\[|\\]","").replaceAll("\\s",",");

                paramValues[index] = convertType(parameterTypes[index],strValue);
            }
        }
        paramValues[handler.paramIndexMapping.get(HttpServletRequest.class.getName())] = req;
        paramValues[handler.paramIndexMapping.get(HttpServletResponse.class.getName())] = resp;
        Object o = iocMapping.get(toLowerFirstCase(method.getDeclaringClass().getSimpleName()));
        method.invoke(o,paramValues);*/

    }

    private NHanderAdaptor  getHandlerAdapter(NHanderMapping handler) {
        if(!handerAdaptors.isEmpty()){
            return handerAdaptors.get(handler);
        }

        return null ;
    }

    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, NModelAndView nModelAndView) {
    }

    private Object convertType(Class<?> parameterType, String  value) {
        Object result = null;
        if(Integer.class == parameterType){
            result = Integer.valueOf(value);
        }else if(String.class == parameterType){
            result = String.valueOf(value);
        }else{
            System.out.println("");
        }
        return result;
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
            NApplicationContext applicationContext = new NApplicationContext(config.getInitParameter("contextConfigLocation"));

            //初始化mvc九大组件
            initStratefies(applicationContext);

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("NMX MVC Framework is init finish");
    }

    private void initStratefies(NApplicationContext context) {
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
    }

    private void initHandlerMappings(NApplicationContext context) {
        if(iocMapping.isEmpty())return;
        for (Map.Entry<String,Object> entry:iocMapping.entrySet()) {
            Class<?> clazz= entry.getValue().getClass();
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
                handlerMapping.add(new Handler(entry.getKey(),method,pattern));
                System.out.println("Mapped :"+url+" method:"+method);
            }
        }
    }

    private void doAutowired() throws IllegalAccessException {
        for(Object object : iocMapping.values()){
            if(object == null ) continue;
            Class<?> clazz = object.getClass();
            System.out.println(clazz.getName());
            if(clazz.isAnnotationPresent(NController.class) || clazz.isAnnotationPresent(NService.class)){
                Field[] declaredFields = clazz.getDeclaredFields();
                for (Field field :declaredFields) {
                    if (!field.isAnnotationPresent(NAutowired.class)) continue;
                    NAutowired annotation = field.getAnnotation(NAutowired.class);
                    Class<?> fieldClass = field.getType();
                    String beanName = fieldClass.getName();
                    if(!"".equals(annotation.value())){
                        beanName = annotation.value();
                    }
                    field.setAccessible(true);
                    field.set(iocMapping.get(toLowerFirstCase(clazz.getSimpleName())), iocMapping.get(beanName));
                }
            }else{
                System.out.println("unknow bean ");
            }

        }

    }

    private void doInstance() throws Exception {
        for (String clazzName : clazzs) {
            Class<?> clazz = Class.forName(clazzName);
            if(clazz.isAnnotationPresent(NController.class)){
                iocMapping.put(toLowerFirstCase(clazz.getSimpleName()), clazz.newInstance());
            }else if(clazz.isAnnotationPresent(NService.class)){
                NService service = clazz.getAnnotation(NService.class);
                String beanName = clazz.getSimpleName();

                if(!"".equals(service.value())){
                    beanName = service.value();
                }
                Object instance = clazz.newInstance();
                iocMapping.put(beanName, instance);
                for(Class<?> i : clazz.getInterfaces()){
                    if(iocMapping.containsKey(i.getName())){
                        throw new Exception("The beanName is exists !");
                    }
                    //key为接口的类型，全限定名
                    iocMapping.put(i.getName(), instance);
                }
            }else{
                System.out.println("unkonw class");
                continue;
            }
        }

    }

    private void doLoadPropreties(ServletConfig config) throws IOException {
        //1、加载配置文件
        String contextConfigLocation = config.getInitParameter("contextConfigLocation").replaceAll("classpath:","");
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        configContext.load(is);
        if(is !=null){
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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


    private String toLowerFirstCase(String simpleName){
        char[] chars = simpleName.toCharArray();
        chars[0] += 32 ;
        return String.valueOf(chars);
    }



}
