package com.chat.chatbot_be.domain.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AnswerCandidate{
    private String question;   // 매칭된 원본 질문
    private String answer;     // 엑셀의 답변
    private double score;      // 유사도 점수

}
