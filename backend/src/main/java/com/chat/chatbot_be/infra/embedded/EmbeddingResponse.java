package com.chat.chatbot_be.infra.embedded;

import java.util.List;

public class EmbeddingResponse {

    private List<Data> data;

    public List<Data> getData() {
        return data;
    }

    public static class Data {
        private List<Double> embedding;

        public List<Double> getEmbedding() {
            return embedding;
        }
    }
}
