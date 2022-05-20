package org.mockito.internal.configuration.excelprocessor;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.map.MultiValueMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.mockito.internal.configuration.FieldAnnotationProcessor;
import org.mockito.internal.configuration.excelprocessor.datasource.ExcelFile;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;

public class ExcelMapperAnnotationProcessor implements FieldAnnotationProcessor<ExcelMapper> {

    private String sheetIndex;
    private String subSheet;
    private String FILE_PATH = ExcelFile.getFilePath();
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
        sheetIndex = annotation.sheetIndex();
        String subSheetIndex;
        Class rootCls = (Class) field.getAnnotatedType().getType();
        Multimap<Class<?>, Method> subClsSetterMap = isCollectionType(rootCls);

        /**
         * Check that annotated field is including collection inside
         */
        if (!subClsSetterMap.isEmpty()) {
            Object rootObj = mapper.map(rootCls, sheetIndex).get(0);
            for (Class<?> subCls : subClsSetterMap.keySet()) {
                subSheetIndex = subCls.getSimpleName();
                Object subObj = subObjectExcelMapping(subCls, subSheetIndex);
                Object finalResult = rootObj;
                subClsSetterMap.get(subCls).forEach(method -> {
                    try {
                        method.invoke(finalResult, subObj);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            return rootObj;
        }

        if (Collection.class.isAssignableFrom(field.getType())) {
            ParameterizedType pType = (ParameterizedType) field.getGenericType();
            Class<?> cls = (Class<?>) pType.getActualTypeArguments()[0];
            return mapper.map(cls, sheetIndex);
        }
        Class<?> cls = (Class<?>) field.getAnnotatedType().getType();
        return mapper.map(cls, sheetIndex).get(0);
    }

    /**
     * Check
     *
     * @param cls
     * @return
     * @throws IntrospectionException
     */
    public Multimap<Class<?>, Method> isCollectionType(Class cls) throws IntrospectionException {
        Multimap<Class<?>, Method> subClsSetterMap = ArrayListMultimap.create();
        Field[] fields = cls.getDeclaredFields(); // get all Fields of a Class
        List<PropertyDescriptor> descriptor = new ArrayList<>(List.of(Introspector.getBeanInfo(cls).getPropertyDescriptors()));

        for (Field f : fields) {
            if (Collection.class.isAssignableFrom(f.getType())) {
                // get all PropertyDescriptor (field & getter & setter) of a Class
                for (PropertyDescriptor p : descriptor) {
                    if (p.getName().equals(f.getName())) {
                        ParameterizedType pType = (ParameterizedType) f.getGenericType();
                        Class<?> aClass = (Class<?>) pType.getActualTypeArguments()[0];
                        subClsSetterMap.put(aClass, p.getWriteMethod());
                    }
                }
            }
        }

        return subClsSetterMap;
    }

    public Object subObjectExcelMapping(Class<?> cls, String sheetIndex) throws Exception {
        return mapper.map(cls, sheetIndex);
    }
}
