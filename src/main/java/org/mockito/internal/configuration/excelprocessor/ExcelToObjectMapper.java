package org.mockito.internal.configuration.excelprocessor;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.mockito.internal.configuration.excelprocessor.exception.InvalidExcelFileException;
import org.mockito.internal.configuration.excelprocessor.exception.InvalidObjectFieldNameException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;


/**
 * A simple Excel to Object mapper utility using Apache POI.
 * This class provides utility methods, to read an Excel file and convert each rows of
 * the excel file into appropriate model object as specified and return all rows of excel
 * file as list of given object type.
 */

public class ExcelToObjectMapper {
    private Workbook workbook;

    public ExcelToObjectMapper(String fileUrl) throws IOException, InvalidExcelFileException {
        try {
            workbook = createWorkBook(fileUrl);
        } catch (InvalidFormatException e) {
            throw new InvalidExcelFileException(e.getMessage());
        }
    }

    /**
     * Read data from Excel file and convert each rows into list of given object of Type T.
     *
     * @param cls Class of Type T.
     * @param <T> Generic type T, result will list of type T objects.
     * @return List of object of type T.
     * @throws Exception if failed to generate mapping.
     */
    public <T> ArrayList<T> map(Class<T> cls, String sheetIndex) throws Exception {
        ArrayList<T> list = new ArrayList();

        Sheet sheet = workbook.getSheet(sheetIndex);
        int lastRow = sheet.getLastRowNum();
        for (int i = 1; i <= lastRow; i++) {
            Object obj = cls.newInstance();
            Field[] fields = obj.getClass().getDeclaredFields();
            for (Field field : fields) {
                String fieldName = field.getName();
                int index = getHeaderIndex(fieldName, sheetIndex);
                Cell cell = sheet.getRow(i).getCell(index);
                Field classField = obj.getClass().getDeclaredField(fieldName);
                setObjectFieldValueFromCell(obj, classField, cell);
            }
            list.add((T) obj);
        }
        return list;
    }

    /**
     * Read value from Cell and set it to given field of given object.
     * Note: supported data Type: String, Date, int, long, float, double and boolean.
     *
     * @param obj   Object whom given field belong.
     * @param field Field which value need to be set.
     * @param cell  Apache POI cell from which value needs to be retrived.
     */
    private void setObjectFieldValueFromCell(Object obj, Field field, Cell cell) {
        Class<?> cls = field.getType();
        field.setAccessible(true);
        if (cls == char.class) {
            try {
                field.set(obj, cell.getStringCellValue().charAt(0));
            } catch (Exception e) {
                try {
                    field.set(obj, null);
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                }
            }
        } else if (cls == String.class) {
            try {
                field.set(obj, cell.getStringCellValue());
            } catch (Exception e) {
                try {
                    field.set(obj, null);
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                }
            }
        } else if (cls == Date.class) {
            try {
                Date date = cell.getDateCellValue();
                field.set(obj, date);
            } catch (Exception e) {
                try {
                    field.set(obj, null);
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                }
            }
        } else if (cls == int.class || cls == long.class || cls == float.class || cls == double.class || cls == BigDecimal.class) {
            double value = cell.getNumericCellValue();
            try {
                if (cls == int.class) {
                    field.set(obj, (int) value);
                } else if (cls == long.class) {
                    field.set(obj, (long) value);
                } else if (cls == float.class) {
                    field.set(obj, (float) value);
                } else if (cls == BigDecimal.class) {
                    field.set(obj, BigDecimal.valueOf(value));
                } else {
                    //Double value
                    field.set(obj, value);
                }
            } catch (Exception e) {
                try {
                    field.set(obj, null);
                } catch (IllegalAccessException e1) {
                    System.out.println("chinna - ");
                    e1.printStackTrace();
                }
            }
        } else if (cls == boolean.class) {
            boolean value = cell.getBooleanCellValue();
            try {
                field.set(obj, value);
            } catch (Exception e) {
                try {
                    field.set(obj, null);
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                }
            }
        } else if (Collection.class.isAssignableFrom(cls)) {
            try {
                field.set(obj, null);
            } catch (Exception e) {
                try {
                    field.set(obj, null);
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                }
            }
        } else if (isInfoproClass(cls)) {
            try {
                field.set(obj, null);
            } catch (Exception e) {
                try {
                    field.set(obj, null);
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                }
            }
        } else if (cls == LocalDate.class) {
            try {
                String date = cell.getStringCellValue();
                field.set(obj, LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            } catch (Exception e) {
                try {
                    field.set(obj, null);
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                }
            }
        } else if (cls == LocalDateTime.class) {
            try {
                String date = cell.getStringCellValue();
                field.set(obj, LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            } catch (Exception e) {
                try {
                    field.set(obj, null);
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                }
            }
        }

    }

    private boolean isInfoproClass(Class<?> cls) {
        return cls.getName().startsWith("com.infopro");
    }

    /**
     * Create Apache POI @{@link Workbook} for given excel file.
     *
     * @param file
     * @return
     * @throws IOException
     * @throws InvalidFormatException
     */
    private Workbook createWorkBook(String file) throws IOException, InvalidFormatException {
        InputStream inp = new FileInputStream(file);
        return WorkbookFactory.create(inp);
    }

    /**
     * Read first row/header of Excel file, match given header name and return its index.
     *
     * @param headerName
     * @param sheetIndex
     * @return Index number of header name.
     * @throws InvalidObjectFieldNameException
     */
    private int getHeaderIndex(String headerName, String sheetIndex) throws Exception {
        Sheet sheet = workbook.getSheet(sheetIndex);
        int totalColumns = sheet.getRow(0).getLastCellNum();
        int index = -1;
        for (index = 0; index < totalColumns; index++) {
            Cell cell = sheet.getRow(0).getCell(index);
            if (cell.getStringCellValue().equalsIgnoreCase(headerName)) {
                break;
            }
        }
        if (index == -1) {
            throw new InvalidObjectFieldNameException("Invalid object field name provided.");
        }
        return index;
    }
}


