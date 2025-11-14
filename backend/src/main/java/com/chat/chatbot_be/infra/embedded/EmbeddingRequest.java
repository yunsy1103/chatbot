package com.chat.chatbot_be.infra.embedded;

public class EmbeddingRequest {
    private String model;
    private String input;

    public EmbeddingRequest(String model, String input) {
        this.model = model;
        this.input = input;
    }

    public String getModel() { return model; }
    public String getInput() { return input; }
}
