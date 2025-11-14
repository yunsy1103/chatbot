package com.chat.chatbot_be.domain.service;

import com.chat.chatbot_be.domain.model.AnswerCandidate;
import com.chat.chatbot_be.infra.vector.VectorDbClient;
import com.chat.chatbot_be.infra.vector.VectorSearchResult;
import com.chat.chatbot_be.api.dto.AnswerResponse;
import com.chat.chatbot_be.infra.embedded.EmbeddingClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QaService {

    private final EmbeddingClient embeddingClient;
    private final VectorDbClient vectorDbClient;

    // Top-k 개수
    private static final int TOP_K = 5;
    // 임계값
    private static final double SCORE_THRESHOLD = 0.65;

    public QaService(EmbeddingClient embeddingClient,
                     VectorDbClient vectorDbClient) {
        this.embeddingClient = embeddingClient;
        this.vectorDbClient = vectorDbClient;
    }

    public AnswerResponse answerQuestion(String question) {
        // 질문 임베딩
        float[] queryEmbedding = embeddingClient.embed(question);

        // Vector DB 검색
        List<VectorSearchResult> searchResults =
                vectorDbClient.search(queryEmbedding, TOP_K);

        // Top-1 선택
        VectorSearchResult best = searchResults.isEmpty() ? null : searchResults.get(0);

        AnswerResponse response = new AnswerResponse();
        if (best == null || best.getScore() < SCORE_THRESHOLD) {
            response.setAnswer("제공된 지식 범위 내에서 적절한 답변을 찾지 못했습니다.");
            response.setMatchedQuestion(null);
            response.setScore(0.0);
        } else {
            response.setAnswer(best.getAnswer());
            response.setMatchedQuestion(best.getQuestion());
            response.setScore(best.getScore());
        }

        // 후보들 정보도 같이 내려주기
        List<AnswerCandidate> candidates = searchResults.stream()
                .map(r -> {
                    AnswerCandidate dto = new AnswerCandidate();
                    dto.setQuestion(r.getQuestion());
                    dto.setAnswer(r.getAnswer());
                    dto.setScore(r.getScore());
                    return dto;
                })
                .toList();

        response.setCandidates(candidates);
        return response;
    }
}
