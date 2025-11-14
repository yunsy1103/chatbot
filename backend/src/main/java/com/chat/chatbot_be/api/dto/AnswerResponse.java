package com.chat.chatbot_be.api.dto;

import com.chat.chatbot_be.domain.model.AnswerCandidate;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AnswerResponse {
    private String answer;                 // 최종 답변
    private String matchedQuestion;        // 어떤 질문과 매칭됐는지
    private double score;                  // 그 질문의 유사도 점수
    private List<AnswerCandidate> candidates; // Top-k 후보 리스트

}
