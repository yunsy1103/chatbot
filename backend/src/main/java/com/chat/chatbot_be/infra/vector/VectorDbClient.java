package com.chat.chatbot_be.infra.vector;

import java.util.List;

public interface VectorDbClient {

    // 검색
    List<VectorSearchResult> search(float[] embedding, int topK);

    // 인덱싱 (Q&A 저장)
    void upsert(String id, float[] embedding, String question, String answer);
}
