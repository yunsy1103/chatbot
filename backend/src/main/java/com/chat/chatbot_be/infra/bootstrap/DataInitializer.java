package com.chat.chatbot_be.infra.bootstrap;

import com.chat.chatbot_be.infra.embedded.EmbeddingClient;
import com.chat.chatbot_be.infra.vector.VectorDbClient;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final EmbeddingClient embeddingClient;
    private final VectorDbClient vectorDbClient;

    public DataInitializer(
            EmbeddingClient embeddingClient,
            VectorDbClient vectorDbClient
    ) {
        this.embeddingClient = embeddingClient;
        this.vectorDbClient = vectorDbClient;
    }

    @Override
    public void run(String... args) throws Exception {

        // Excel 파일 로딩
        List<QaPair> rows = loadExcel("faq.xlsx");

        int id = 1;
        for (QaPair row : rows) {
            float[] vector = embeddingClient.embed(row.getQuestion());
            vectorDbClient.upsert(String.valueOf(id), vector, row.getQuestion(), row.getAnswer());

            id++;
        }

    }

    private List<QaPair> loadExcel(String fileName) throws Exception {
        List<QaPair> result = new ArrayList<>();

        ClassLoader classLoader = getClass().getClassLoader();
        InputStream is = classLoader.getResourceAsStream(fileName);

        if (is == null) {
            throw new IllegalStateException("클래스패스에서 엑셀 파일을 찾을 수 없습니다: " + fileName);
        }

        Workbook workbook = WorkbookFactory.create(is);
        Sheet sheet = workbook.getSheetAt(0);
        DataFormatter formatter = new DataFormatter();

        int lastRowNum = sheet.getLastRowNum();

        // Row 2부터 시작
        for (int i = 3; i <= lastRowNum; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }

            // C열(2번 인덱스)에서 질문 찾기
            String cColValue = getCellValueWithMerge(sheet, row.getRowNum(), 2, formatter);

            if (cColValue == null || cColValue.isEmpty()) {
                continue;
            }

            cColValue = cColValue.trim();

            // Q로 시작하는 질문 찾기
            if (cColValue.startsWith("Q.") || cColValue.startsWith("Q ")) {
                String question = cColValue.replaceFirst("^Q\\.?\\s*", "").trim();

                // 다음 행에서 답변 찾기
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
                    result.add(new QaPair(question, answer));
                }
            }
        }

        workbook.close();
        is.close();

        System.out.println("최종 추출된 Q&A 쌍: " + result.size() + "개");
        return result;
    }

    // 병합된 셀의 값을 가져오는 메서드
    private String getCellValueWithMerge(Sheet sheet, int rowNum, int colNum, DataFormatter formatter) {
        Row row = sheet.getRow(rowNum);
        if (row == null) return "";

        Cell cell = row.getCell(colNum);

        // 일반 셀인 경우
        if (cell != null) {
            String value = formatter.formatCellValue(cell);
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }

        // 병합된 셀인 경우 - 병합 영역에서 값 찾기
        for (CellRangeAddress mergedRegion : sheet.getMergedRegions()) {
            if (mergedRegion.isInRange(rowNum, colNum)) {
                // 병합 영역의 첫 번째 셀에서 값 가져오기
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


    //=================== dto ===================

    public static class QaPair {
        private String question;
        private String answer;

        public QaPair(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }

        public String getQuestion() {
            return question;
        }

        public String getAnswer() {
            return answer;
        }
    }
}