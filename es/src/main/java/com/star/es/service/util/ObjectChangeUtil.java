package com.star.es.service.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ObjectChangeUtil {
    public ObjectChangeUtil() {
    }

    public static List<Map<String, Object>> transListBean2ListMap(List<?> objList) {
        List<Map<String, Object>> listmap = new ArrayList();
        if (objList != null && objList.size() != 0) {
            try {
                Iterator var3 = objList.iterator();

                while(var3.hasNext()) {
                    Object obj = var3.next();
                    Map<String, Object> map = new HashMap();
                    BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
                    PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
                    PropertyDescriptor[] var10 = propertyDescriptors;
                    int var9 = propertyDescriptors.length;

                    for(int var8 = 0; var8 < var9; ++var8) {
                        PropertyDescriptor property = var10[var8];
                        String key = property.getName();
                        if (!key.equals("class")) {
                            Method getter = property.getReadMethod();
                            Object value = getter.invoke(obj);
                            map.put(key, value);
                        }
                    }

                    listmap.add(map);
                }

                return listmap;
            } catch (Exception var14) {
                throw new InternalError("javaben to map error: " + var14.getMessage());
            }
        } else {
            return listmap;
        }
    }

    public static Map<String, Object> transBean2Map(Object obj) {
        if (obj == null) {
            return null;
        } else {
            HashMap map = new HashMap();

            try {
                BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
                PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
                PropertyDescriptor[] var7 = propertyDescriptors;
                int var6 = propertyDescriptors.length;

                for(int var5 = 0; var5 < var6; ++var5) {
                    PropertyDescriptor property = var7[var5];
                    String key = property.getName();
                    if (!key.equals("class")) {
                        Method getter = property.getReadMethod();
                        Object value = getter.invoke(obj);
                        map.put(key, value);
                    }
                }

                return map;
            } catch (Exception var11) {
                throw new InternalError("Unexpected IllegalAccessException: " + var11.getMessage());
            }
        }
    }
}
