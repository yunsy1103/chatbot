package com.chat.chatbot_be.infra.vector;

import com.chat.chatbot_be.infra.loader.GridDataLoader;

import java.util.List;

public interface VectorDbClient {

    // 검색
    List<VectorSearchResult> search(float[] embedding, int topK);

    // 인덱싱 (Q&A 저장)
    void upsert(String id, float[] embedding, String question, String answer);

    boolean isInitialized();                      // Qdrant에 데이터 있는지 확인

    void upsertAll(List<GridDataLoader.QaRow> rows);            // 엑셀에서 읽은 모든 QA 적재
}
