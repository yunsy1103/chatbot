package com.chat.chatbot_be.infra.embedded;

// 질문을 벡터로 변환하는 역할
public interface EmbeddingClient {
    float[] embed(String text);
}
