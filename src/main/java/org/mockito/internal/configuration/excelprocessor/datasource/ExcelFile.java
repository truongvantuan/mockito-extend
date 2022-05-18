package org.mockito.internal.configuration.excelprocessor.datasource;

import java.io.File;

public class ExcelFile {
    private static String filename = "src/test/java/com/infopro/cbint/web/datasource/testingdata.xlsx";

    public static String getFilePath() {

        if (System.getProperty("os.name").contains("Windows"))  {
            filename = filename.replace("/", "\\");
        }
        return new File(filename).getAbsolutePath();
    }
}
