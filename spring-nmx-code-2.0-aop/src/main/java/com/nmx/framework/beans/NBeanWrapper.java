package com.nmx.framework.beans;/**
 * @author ning_mx
 * @date 14:50 2020/4/15
 * @description
 */

/**
 * @author ning_mx
 * @date 2020/4/15
 */
public class NBeanWrapper {
    private Object wrapperInstance ;
    private Class<?> wrapperClass ;

    public NBeanWrapper(Object wrapperInstance) {
        this.wrapperInstance = wrapperInstance;
        this.wrapperClass = wrapperInstance.getClass() ;
    }

    public Object getWrapperInstance() {
        return this.wrapperInstance;
    }

    public Class<?> getWrapperClass() {
        return this.wrapperClass;
    }
}
