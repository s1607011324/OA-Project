package com.oa.core.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListConvertUtil {
    public static <T> List<Map<String,Object>> listConvert(List<T> list){
        List<Map<String,Object>> list_map=new ArrayList<Map<String,Object>>();
        try {
            for (T t : list) {
                Field[] fields=t.getClass().getDeclaredFields();
                Map<String, Object> m = new HashMap<String, Object>();
                for(Field field:fields){
                    String keyName=field.getName();
                    PropertyDescriptor pd = new PropertyDescriptor(keyName,t.getClass());
                    Method getMethod = pd.getReadMethod();// 获得getter方法
                    Object o = getMethod.invoke(t);// 执行get方法返回一个Object
                    m.put(keyName, o);
                }
                list_map.add(m);
            }
            return list_map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
