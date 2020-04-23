package com.nmx.framework.webmvc.servlet;/**
 * @author ning_mx
 * @date 15:27 2020/4/18
 * @description
 */

import com.nmx.framework.annotation.NRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ning_mx
 * @date 2020/4/18
 */
public class NHanderAdaptor {
    public NModelAndView handler(HttpServletRequest req, HttpServletResponse resp, NHanderMapping handler) throws InvocationTargetException, IllegalAccessException {
        //保存形参列表
        //将参数名称和参数的位置，这种关系保存起来
        Map<String,Integer> paramIndexMapping = new HashMap<String, Integer>();

        //通过运行时的状态去拿到你
        Annotation[] [] pa = handler.getMethod().getParameterAnnotations();
        for (int i = 0; i < pa.length ; i ++) {
            for(Annotation a : pa[i]){
                if(a instanceof NRequestParam){
                    String paramName = ((NRequestParam) a).value();
                    if(!"".equals(paramName.trim())){
//                        String value = Arrays.toString(params.get(paramName))
//                                .replaceAll("\\[|\\]","")
//                                .replaceAll("\\s+",",");
//                        paramValues[i] = value;
                        paramIndexMapping.put(paramName,i);
                    }
                }
            }
        }

        //request response初始化一下
        Class<?> [] paramTypes = handler.getMethod().getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> paramterType = paramTypes[i];
            if(paramterType == HttpServletRequest.class || paramterType == HttpServletResponse.class){
                paramIndexMapping.put(paramterType.getName(),i);
            }
        }


        //去拼接实参列表
        Map<String,String[]> params = req.getParameterMap();

        Object [] paramValues = new Object[paramTypes.length];

        for (Map.Entry<String,String[]> param : params.entrySet()) {
            String value = Arrays.toString(params.get(param.getKey()))
                    .replaceAll("\\[|\\]","")
                    .replaceAll("\\s+",",");

            if(!paramIndexMapping.containsKey(param.getKey())){continue;}

            int index = paramIndexMapping.get(param.getKey());

            //允许自定义的类型转换器Converter
            paramValues[index] = castStringValue(value,paramTypes[index]);
        }

        if(paramIndexMapping.containsKey(HttpServletRequest.class.getName())){
            int index = paramIndexMapping.get(HttpServletRequest.class.getName());
            paramValues[index] = req;
        }

        if(paramIndexMapping.containsKey(HttpServletResponse.class.getName())){
            int index = paramIndexMapping.get(HttpServletResponse.class.getName());
            paramValues[index] = resp;
        }
        //调用目标方法，得到其返回值
        Object result = handler.getMethod().invoke(handler.getController(),paramValues);
        if(result == null || result instanceof Void){return null;}

        boolean isModelAndView = handler.getMethod().getReturnType() == NModelAndView.class;
        if(isModelAndView){
            return (NModelAndView)result;
        }
        return null;
    }

    private Object castStringValue(String value, Class<?> paramType) {
        if(String.class == paramType){
            return value;
        }else if(Integer.class == paramType){
            return Integer.valueOf(value);
        }else if(Double.class == paramType){
            return Double.valueOf(value);
        }else {
            if(value != null){
                return value;
            }
            return null;
        }

    }
}
