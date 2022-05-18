package org.mockito.internal.configuration.excelprocessor;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
@Documented
public @interface ExcelMapper {
    String sheetIndex();
    boolean parameterizedType() default false;
    String subSheet() default "";
}
