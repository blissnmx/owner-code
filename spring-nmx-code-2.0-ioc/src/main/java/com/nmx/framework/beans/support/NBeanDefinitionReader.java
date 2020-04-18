package com.nmx.framework.beans.support;/**
 * @author ning_mx
 * @date 15:15 2020/4/15
 * @description
 */

import com.nmx.framework.annotation.NController;
import com.nmx.framework.annotation.NService;
import com.nmx.framework.beans.config.NBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author ning_mx
 * @date 2020/4/15
 */
public class NBeanDefinitionReader {
    private Properties configContext = new Properties();
    private List<String> clazzs = new ArrayList<String>();

    public NBeanDefinitionReader(String ... configLocations) throws IOException {
        //可能有多个配置文件，此处默认一个
        doLoadConfig(configLocations[0]);

        //扫描类
        doScanner(configContext.getProperty("scanPackage"));
    }

    private void doLoadConfig(String configLocation) throws IOException {
        //1、加载配置文件
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(configLocation.replaceAll("classpath:",""));
        configContext.load(is);
        if(is !=null){
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void doScanner(String scanPackage) {
        URL filePath = NBeanDefinitionReader.class.getResource("/" + scanPackage.replaceAll("\\.", "/"));
        //URL filePath = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
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

    public List<NBeanDefinition> loadBeanDefinitions() throws ClassNotFoundException {
        List<NBeanDefinition> result = new ArrayList<NBeanDefinition>();

        for(String className :clazzs){
            Class<?> beanClass = Class.forName(className);
            //接口类型，不加入BeanDefinition缓存
            if(beanClass.isInterface()) continue;
            result.add(new NBeanDefinition(toLowerFirstCase(beanClass.getSimpleName()), beanClass.getName()));
            //类的接口注入
            for(Class<?> inf : beanClass.getInterfaces() ){
                result.add(new NBeanDefinition(inf.getName(), beanClass.getName()));

            }
        }
        return result;
    }


    public String toLowerFirstCase(String simpleName){
        char[] chars = simpleName.toCharArray();
        chars[0] += 32 ;
        return String.valueOf(chars);
    }
}
