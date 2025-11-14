package com.chat.chatbot_be.infra.qdrant;

import com.chat.chatbot_be.infra.vector.VectorDbClient;
import com.chat.chatbot_be.infra.vector.VectorSearchResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class QdrantVectorDbClient implements VectorDbClient {

    private final WebClient webClient;
    private final String collectionName;

    public QdrantVectorDbClient(
            @Value("${qdrant.url}") String qdrantUrl,
            @Value("${qdrant.collection}") String collectionName
    ) {
        this.webClient = WebClient.builder()
                .baseUrl(qdrantUrl)
                .build();
        this.collectionName = collectionName;
    }


    // 검색
    @Override
    public List<VectorSearchResult> search(float[] embedding, int topK) {

        QdrantSearchRequest request = new QdrantSearchRequest(embedding, topK);

        QdrantSearchResponse response;
        try {
            response = webClient.post()
                    .uri("/collections/{collection}/points/search", collectionName)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(QdrantSearchResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw e;
        }

        if (response == null || response.getResult() == null) {
            throw new RuntimeException("Qdrant search failed");
        }

        return response.getResult().stream()
                .map(r -> {
                    Map<String, Object> payload = r.getPayload();
                    String question = (String) payload.get("question");
                    String answer = (String) payload.get("answer");

                    VectorSearchResult dto = new VectorSearchResult();
                    dto.setId(String.valueOf(r.getId()));
                    dto.setQuestion(question);
                    dto.setAnswer(answer);
                    dto.setScore(r.getScore());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // 업서트(인덱싱)
    @Override
    public void upsert(String id, float[] embedding, String question, String answer) {

        QdrantUpsertRequest.Point point = new QdrantUpsertRequest.Point();
        point.setId(Long.parseLong(id));
        point.setVector(embedding);
        point.setPayload(Map.of(
                "question", question,
                "answer", answer
        ));

        QdrantUpsertRequest request = new QdrantUpsertRequest(List.of(point));

        try {
            QdrantUpsertResponse response = webClient.put()
                    .uri("/collections/{collection}/points", collectionName)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(QdrantUpsertResponse.class)
                    .block();

            if (response == null || response.getStatus() == null || !"ok".equals(response.getStatus())) {
                throw new RuntimeException("Qdrant upsert failed");
            }
        } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
            throw e;
        }
    }


    // =================== Qdrant DTO ===================

    // 검색 요청
    public static class QdrantSearchRequest {
        private float[] vector;
        private int limit;
        private boolean with_payload = true;
        private boolean with_vector = false;


        public QdrantSearchRequest(float[] vector, int limit) {
            this.vector = vector;
            this.limit = limit;
        }

        public float[] getVector() { return vector; }
        public int getLimit() { return limit; }
        public boolean isWith_payload() { return with_payload; }
        public boolean isWith_vector() { return with_vector; }
    }

    // 검색 응답
    public static class QdrantSearchResponse {

        private List<ResultItem> result;

        public List<ResultItem> getResult() {
            return result;
        }

        public void setResult(List<ResultItem> result) {
            this.result = result;
        }

        public static class ResultItem {
            private Object id;
            private double score;
            private Map<String, Object> payload;

            public Object getId() { return id; }
            public void setId(Object id) { this.id = id; }

            public double getScore() { return score; }
            public void setScore(double score) { this.score = score; }

            public Map<String, Object> getPayload() { return payload; }
            public void setPayload(Map<String, Object> payload) { this.payload = payload; }
        }
    }

    // 업서트 요청
    public static class QdrantUpsertRequest {

        private List<Point> points;

        public QdrantUpsertRequest(List<Point> points) {
            this.points = points;
        }

        public List<Point> getPoints() { return points; }
        public void setPoints(List<Point> points) { this.points = points; }

        public static class Point {
            private Long id;
            private float[] vector;
            private Map<String, Object> payload;

            public Long getId() { return id; }
            public void setId(Long id) { this.id = id; }

            public float[] getVector() { return vector; }
            public void setVector(float[] vector) { this.vector = vector; }

            public Map<String, Object> getPayload() { return payload; }
            public void setPayload(Map<String, Object> payload) { this.payload = payload; }
        }
    }

    // 업서트 응답
    public static class QdrantUpsertResponse {
        private String status;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
