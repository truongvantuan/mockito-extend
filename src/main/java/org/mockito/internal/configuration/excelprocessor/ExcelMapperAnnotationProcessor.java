package org.mockito.internal.configuration.excelprocessor;

import org.mockito.internal.configuration.FieldAnnotationProcessor;
import org.mockito.internal.configuration.excelprocessor.datasource.ExcelFile;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
        subSheet = annotation.subSheet();
        Object result;
        Class rootCls = (Class) field.getAnnotatedType().getType();

        Map<Class<?>, Method> subClsSetterMap = isCombineObjectType(rootCls);

        if (subClsSetterMap != null) {
            String subSheetIndex = annotation.subSheet();
            Class subCls = subClsSetterMap.keySet().stream().findFirst().get();
            Object subObj = subObjectExcelMapping(subCls, subSheetIndex);
            result = mapper.map(rootCls, sheetIndex);
            subClsSetterMap.get(subCls).invoke(result, subObj);
            return result;
        }

        if (Collection.class.isAssignableFrom(field.getType())) {
            ParameterizedType pType = (ParameterizedType) field.getGenericType();
            Class<?> cls = (Class<?>) pType.getActualTypeArguments()[0];
            return mapper.map(cls, sheetIndex);
        }
        Class<?> cls = (Class<?>) field.getAnnotatedType().getType();


        return mapper.map(cls, sheetIndex).get(0);
    }

    public Map<Class<?>, Method> isCombineObjectType(Class cls) throws IntrospectionException {
        Field[] fields = cls.getFields();
        Map<Class<?>, Method> subClsSetter = new HashMap<>();
        for (Field f : fields) {
            if (Collection.class.isAssignableFrom(f.getType())) {
                PropertyDescriptor[] descriptor = Introspector.getBeanInfo(cls).getPropertyDescriptors();
                for (PropertyDescriptor p : descriptor) {
                    if (p.getPropertyType().getName().equals(f.getClass().getName())) {
                        subClsSetter.put(f.getClass(), p.getWriteMethod());
                    }
                }
            }
        }
        return null;
    }

    public Object subObjectExcelMapping(Class<?> cls, String sheetIndex) throws Exception {
        return mapper.map(cls, sheetIndex);
    }
}
