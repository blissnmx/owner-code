package com.nmx.framework.context;/**
 * @author ning_mx
 * @date 14:53 2020/4/15
 * @description
 */

import com.nmx.framework.annotation.NAutowired;
import com.nmx.framework.annotation.NController;
import com.nmx.framework.annotation.NService;
import com.nmx.framework.beans.NBeanWrapper;
import com.nmx.framework.beans.config.NBeanDefinition;
import com.nmx.framework.beans.support.NBeanDefinitionReader;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ning_mx
 * @date 2020/4/15
 */
public class NApplicationContext {

    private final Map<String, NBeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String, NBeanDefinition>();

    private NBeanDefinitionReader reader ; //加载配置，扫描类初始化为NBeanDefinition

    private Map<String, Object> factoryBeanObjectCache = new ConcurrentHashMap<String, Object>();
    private Map<String,NBeanWrapper> factoryBeanInstanceCache = new HashMap<String, NBeanWrapper>();

    public NApplicationContext(String ... configLocations) throws IOException {
        //1、加载配置文件
        reader = new NBeanDefinitionReader(configLocations);
        try{
            //2、解析配置文件，封装成BeanDefinition
            final List<NBeanDefinition> nBeanDefinitions = reader.loadBeanDefinitions();
            List<NBeanDefinition> beanDefinitions = nBeanDefinitions;

            //3、缓存BeanDefinition
            doRegistBeanDefinition(beanDefinitions);

            //4、依赖注入
            doAutowired();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void doRegistBeanDefinition(List<NBeanDefinition> beanDefinitions) throws Exception {
        for(NBeanDefinition beanDefinition : beanDefinitions){
            if(beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())){
                throw new Exception("The " + beanDefinition.getFactoryBeanName() + "is exists");
            }
            beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
            beanDefinitionMap.put(beanDefinition.getBeanClassName(), beanDefinition);
        }
    }

    private void doAutowired() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        for(Map.Entry<String,NBeanDefinition> entry : beanDefinitionMap.entrySet()){
            getBean(entry.getKey());
        }
    }

    public Object getBean(String beanName) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        /**
         * 1、得到bean定义信息
         * 2、实例化所有bean
         * 3、得到beanWrapper
         * 4、保存IoC
         * 5、执行依赖注入（非全部）
         * 6、返回bean实例
         */
        NBeanDefinition beanDefinition = beanDefinitionMap.get(beanName);

        Object instance = instantiateBean(beanName,beanDefinition);
        NBeanWrapper beanWrapper = new NBeanWrapper(instance);
        factoryBeanInstanceCache.put(beanName,beanWrapper);
        populateBean(beanWrapper);

        return instance;

    }
    //创建真正的实例对象
    private Object instantiateBean(String beanName, NBeanDefinition beanDefinition) {
        String className = beanDefinition.getBeanClassName();
        Object instance = null;
        try {
            if(factoryBeanObjectCache.get(beanName) != null ) {
                instance = factoryBeanObjectCache.get(beanName);
            }
            Class<?> clazz = Class.forName(className);
            //2、默认的类名首字母小写
            instance = clazz.newInstance();
            factoryBeanObjectCache.put(beanName, instance);

        }catch (Exception e){
            e.printStackTrace();
        }
        return instance;
    }

    public Object getBean(Class clazz) throws Exception {
        return getBean( clazz.getName() );
    }
    /**
     * @author  ning_mx
     * @date    19:48 2020/4/15
     * @description
     * 此时并没有全部注入，在Dispatcher过程调用getBean时完全注入
     */
    private void populateBean(NBeanWrapper beanWrapper) {
        Object instance = beanWrapper.getWrapperInstance();

        Class<?> clazz = beanWrapper.getWrapperClass();

        //在Spring中@Component
        if(!(clazz.isAnnotationPresent(NController.class) || clazz.isAnnotationPresent(NService.class))){
            return;
        }
        //把所有的包括private/protected/default/public 修饰字段都取出来
        for (Field field : clazz.getDeclaredFields()) {
            if(!field.isAnnotationPresent(NAutowired.class)){ continue; }

            NAutowired autowired = field.getAnnotation(NAutowired.class);

            //如果用户没有自定义的beanName，就默认根据类型注入
            String autowiredBeanName = autowired.value().trim();
            if("".equals(autowiredBeanName)){
                //field.getType().getName() 获取字段的类型
                autowiredBeanName = field.getType().getName() ;
            }
            //暴力访问
            field.setAccessible(true);
            try {
                if(this.factoryBeanInstanceCache.get(autowiredBeanName) == null){
                    continue;
                }
                field.set(instance,this.factoryBeanInstanceCache.get(autowiredBeanName).getWrapperInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                continue;
            }
        }


    }

    public Properties getConfig() {
        return this.reader.getConfig();
    }

    public int getBeanDefinitionCount() {
        return this.beanDefinitionMap.size();
    }

    public String[] getBeanDefinitionNames() {
        return this.beanDefinitionMap.keySet().toArray(new String[this.beanDefinitionMap.size()]);
    }
}
