package org.mockito.internal.configuration.excelprocessor;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.mockito.internal.configuration.FieldAnnotationProcessor;
import org.mockito.internal.configuration.excelprocessor.datasource.ExcelFile;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ExcelMapperAnnotationProcessor implements FieldAnnotationProcessor<ExcelMapper> {

    private final String FILE_PATH = ExcelFile.getFilePath();
    private ExcelToObjectMapper mapper;

    @Override
    public Object process(ExcelMapper annotation, Field field) {
        Object result = null;
        try {
            result = processAnnotationForExcelMapper(annotation, field);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public Object processAnnotationForExcelMapper(ExcelMapper annotation, Field field) throws Exception {
        mapper = new ExcelToObjectMapper(FILE_PATH);
        String sheetIndex = annotation.sheetIndex();
        Class<?> rootCls = (Class<?>) field.getAnnotatedType().getType();
        Multimap<Class<?>, Method> subClsSetterMap = isCollectionFieldInside(rootCls);

        /**
         * Excel mapping for annotated field is including collection inside
         */
        if (!subClsSetterMap.isEmpty()) {
            Object rootObj = mapper.map(rootCls, sheetIndex).get(0);
            for (Class<?> subCls : subClsSetterMap.keySet()) {
                String subSheetIndex = subCls.getSimpleName();
                List<?> subObj = subObjectExcelMapping(subCls, subSheetIndex);
                subClsSetterMap.get(subCls).forEach(method -> {
                    try {
                        if (method.getName().contains("List"))
                            method.invoke(rootObj, subObj);
                        else method.invoke(rootObj, subObj.get(0));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            return rootObj;
        }

        return mapper.map(rootCls, sheetIndex).get(0);
    }

    /**
     * Checking a Class is including a Field that is Collection
     *
     * @param cls Class to check
     * @return Map with Key is Class type, Value is Setter method
     * @throws IntrospectionException
     */
    public Multimap<Class<?>, Method> isCollectionFieldInside(Class<?> cls) throws IntrospectionException {
        Multimap<Class<?>, Method> subClsSetterMap = ArrayListMultimap.create();
        Field[] fields = cls.getDeclaredFields(); // get all Fields of a Class
        List<PropertyDescriptor> descriptor = new ArrayList<>();
        Collections.addAll(descriptor, Introspector.getBeanInfo(cls).getPropertyDescriptors()); // get all PropertyDescriptor (field & getter & setter) of a Class// get all PropertyDescriptor (field & getter & setter) of a Class
        for (Field f : fields) {
            if (Collection.class.isAssignableFrom(f.getType())) {
                for (PropertyDescriptor p : descriptor) {
                    if (p.getName().equals(f.getName())) {
                        ParameterizedType pType = (ParameterizedType) f.getGenericType();
                        Class<?> aClass = (Class<?>) pType.getActualTypeArguments()[0];
                        subClsSetterMap.put(aClass, p.getWriteMethod());
                    }
                }
            } else if (isInfoproClass(f.getType())) {
                for (PropertyDescriptor p : descriptor) {
                    if (p.getName().equals(f.getName())) {
                        subClsSetterMap.put(f.getType(), p.getWriteMethod());
                    }
                }
            }
        }
        return subClsSetterMap;
    }

    public List<?> subObjectExcelMapping(Class<?> cls, String sheetIndex) throws Exception {
        return mapper.map(cls, sheetIndex);
    }

    private boolean isInfoproClass(Class<?> cls) {
        return cls.getName().startsWith("com.infopro");
    }
}
