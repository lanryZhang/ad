package com.ifeng.hippo.utils;

import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhanglr on 2016/8/28.
 */
public class SchemaUtil<T> {
    private static ConcurrentHashMap<Class<?>,Schema<?>> schemaCache = new ConcurrentHashMap<>();

    public static <T> Schema<T> getSchema(Class<T> clazz){
        Schema<T> schema;
        if (schemaCache.containsKey(clazz)){
            schema = (Schema<T>) schemaCache.get(clazz);
        }else{
            schema = RuntimeSchema.getSchema(clazz);
            schemaCache.put(clazz,schema);
        }
        return schema;
    }
}
