package com.nmx.framework.webmvc.servlet;/**
 * @author ning_mx
 * @date 15:22 2020/4/18
 * @description
 */

import com.nmx.framework.annotation.NRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author ning_mx
 * @date 2020/4/18
 */
public class NHanderMapping {
    protected Object controller ;
    protected Method method ;
    protected Pattern pattern; //${} url占位符解析
    protected Map<String,Integer> paramIndexMapping ; //参数顺序

    public NHanderMapping(Object controller, Method method, Pattern pattern) {
        this.controller = controller;
        this.method = method;
        this.pattern = pattern;

        paramIndexMapping = new HashMap<String, Integer>();
        putParamIndexMapping(method);
    }

    private void putParamIndexMapping(Method method) {

        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            if (parameterType == HttpServletRequest.class || parameterType == HttpServletResponse.class) {
                paramIndexMapping.put(parameterType.getName(), i);
            }
        }
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int j = 0; j < parameterAnnotations.length; j++) {
            for (Annotation a:  parameterAnnotations[j]){
                if ( a instanceof NRequestParam){
                    String paramName = ((NRequestParam) a).value();
                    if(!"".equals(paramName.trim())){
                        paramIndexMapping.put(paramName, j);
                    }
                }
            }
        }

    }
}
