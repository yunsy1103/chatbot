package com.chat.chatbot_be.infra.loader;

import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelLoader implements GridDataLoader {

    @Override
    public List<List<String>> load(File file) throws IOException {
        List<List<String>> rows = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0); // 첫 번째 시트 기준

            for (Row row : sheet) {
                List<String> cellValues = new ArrayList<>();
                for (Cell cell : row) {
                    cellValues.add(getCellValueAsString(cell));
                }
                rows.add(cellValues);
            }
        } catch (IOException e) {
            throw new IOException("엑셀 파일 포맷이 올바르지 않습니다: " + file.getName(), e);
        }

        return rows;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                } else {
                    yield Double.toString(cell.getNumericCellValue());
                }
            }
            case BOOLEAN -> Boolean.toString(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            case BLANK -> "";
            default -> "";
        };
    }
}
