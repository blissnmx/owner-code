package com.nmx.framework.v2;

import com.nmx.framework.annotation.*;

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

/**
 * @author ning_mx
 * @date 2020/4/14
 */
public class NmxDisptcherServlet extends HttpServlet{
    private Map<String,Object> iocMapping = new HashMap<String, Object>();
    private List<String> clazzs = new ArrayList<String>();

    private Map<String, Object> handlerMapping = new HashMap<String,Object>();
    private Properties configContext = new Properties();
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
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] paramValues = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            if(parameterType == HttpServletRequest.class){
                paramValues[i] = req;
            }else if(parameterType == HttpServletResponse.class){
                paramValues[i] = resp;
            }else{
                Annotation[][] parameterAnnotations = method.getParameterAnnotations();
                for (int j = 0; j < parameterAnnotations.length; j++) {
                    for (Annotation a:  parameterAnnotations[j]){
                        if ( a instanceof NRequestParam){
                            String paramName = ((NRequestParam) a).value();
                            if(!"".equals(paramName.trim())){
                                String[] values = parameterMap.get(paramName);
                                System.out.println(values.toString());
                                String strValue = Arrays.toString(values).replaceAll("\\[|\\]","").replaceAll("\\s",",");
                                paramValues[i] = strValue;
                            }
                        }
                    }
                }
            }
        }


        Class<?> declaringClass = method.getDeclaringClass();
        System.out.println(declaringClass);
        Object o = iocMapping.get(toLowerFirstCase(declaringClass.getSimpleName()));
        method.invoke(o,paramValues);

    }

    @Override
    public void init(ServletConfig config) throws ServletException {

        try {
            //1、加载配置文件
            doLoadPropreties(config);

            //2、扫描相关类
            doScanner( configContext.getProperty("scanPackage"));

            //3、创建实例化并保存至容器
            doInstance();

            //4、依赖注入
            doAutowired();

            //5、初始化handlerMapping
            initHandlerMapping();

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("NMX MVC Framework is init finish");
    }

    private void initHandlerMapping() {
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
                handlerMapping.put(url, method);
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
