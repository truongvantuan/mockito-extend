package org.exceltest.datasource;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.internal.configuration.excelprocessor.ExcelMapper;

public class MockTest {

    @Mock
    protected PayeeRequestDto payeeRequestDto1;

    @ExcelMapper(sheetIndex = "PayeeRequestDto")
    protected PayeeRequestDto payeeRequestDto;

    @Test
    public void test() {
    }

    @Before
    public void setUp() {
    }
}
