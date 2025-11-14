package com.chat.chatbot_be.infra.embedded;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;

@Service
public class OpenAIEmbeddingClient implements EmbeddingClient {

    private final WebClient webClient;
    private final String model;

    @Autowired
    public OpenAIEmbeddingClient(
            WebClient openAiWebClient,
            @Value("${openai.embedding-model}") String model
    ) {
        this.webClient = openAiWebClient;
        this.model = model;
    }

    @Override
    public float[] embed(String text) {

        EmbeddingRequest request = new EmbeddingRequest(model, text);

        // API 호출
        EmbeddingResponse response = webClient.post()
                .bodyValue(request)
                .retrieve()
                .bodyToMono(EmbeddingResponse.class)
                .block();

        if (response == null ||
                response.getData() == null ||
                response.getData().isEmpty()) {
            throw new RuntimeException("Failed to generate embeddings");
        }

        // Open Ai한테 받은 벡터 길이 float로 변환
        List<Double> embedding = response.getData().get(0).getEmbedding();
        float[] vector = new float[embedding.size()];
        for (int i = 0; i < embedding.size(); i++) {
            vector[i] = embedding.get(i).floatValue();
        }

        return vector;
    }
}
