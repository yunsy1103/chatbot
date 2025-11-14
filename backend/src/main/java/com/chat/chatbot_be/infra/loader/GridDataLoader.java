package com.chat.chatbot_be.infra.loader;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


@Component
public class GridDataLoader {

    public List<QaRow> load() throws Exception {
        return loadExcel("faq.xlsx");
    }

    private List<QaRow> loadExcel(String fileName) throws Exception {
        List<QaRow> result = new ArrayList<>();

        ClassLoader classLoader = getClass().getClassLoader();
        InputStream is = classLoader.getResourceAsStream(fileName);

        if (is == null) {
            throw new IllegalStateException("클래스패스에서 엑셀 파일을 찾을 수 없습니다: " + fileName);
        }

        Workbook workbook = WorkbookFactory.create(is);
        Sheet sheet = workbook.getSheetAt(0);
        DataFormatter formatter = new DataFormatter();

        int lastRowNum = sheet.getLastRowNum();

        for (int i = 3; i <= lastRowNum; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }

            String cColValue = getCellValueWithMerge(sheet, row.getRowNum(), 2, formatter);
            if (cColValue == null || cColValue.isEmpty()) {
                continue;
            }
            cColValue = cColValue.trim();

            if (cColValue.startsWith("Q.") || cColValue.startsWith("Q ")) {
                String question = cColValue.replaceFirst("^Q\\.?\\s*", "").trim();

                String answer = "";
                for (int j = i + 1; j <= lastRowNum && j <= i + 3; j++) {
                    String nextValue = getCellValueWithMerge(sheet, j, 2, formatter);
                    if (nextValue != null && !nextValue.isEmpty()) {
                        nextValue = nextValue.trim();
                        if (nextValue.startsWith("A.") || nextValue.startsWith("A ")) {
                            answer = nextValue.replaceFirst("^A\\.?\\s*", "").trim();
                            i = j;
                            break;
                        }
                    }
                }

                if (!question.isEmpty() && !answer.isEmpty()) {
                    result.add(new QaRow(question, answer));
                }
            }
        }

        workbook.close();
        is.close();

        System.out.println("최종 추출된 Q&A 쌍: " + result.size() + "개");
        return result;
    }

    private String getCellValueWithMerge(Sheet sheet, int rowNum, int colNum, DataFormatter formatter) {
        Row row = sheet.getRow(rowNum);
        if (row == null) return "";

        Cell cell = row.getCell(colNum);

        if (cell != null) {
            String value = formatter.formatCellValue(cell);
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }

        for (CellRangeAddress mergedRegion : sheet.getMergedRegions()) {
            if (mergedRegion.isInRange(rowNum, colNum)) {
                Row firstRow = sheet.getRow(mergedRegion.getFirstRow());
                if (firstRow != null) {
                    Cell firstCell = firstRow.getCell(mergedRegion.getFirstColumn());
                    if (firstCell != null) {
                        return formatter.formatCellValue(firstCell);
                    }
                }
            }
        }

        return "";
    }

    // 엑셀 한 줄(Q/A) DTO
    public static class QaRow {
        private String question;
        private String answer;

        public QaRow(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }

        public String getQuestion() { return question; }
        public String getAnswer() { return answer; }
    }
}