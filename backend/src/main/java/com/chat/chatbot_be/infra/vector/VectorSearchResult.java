package com.chat.chatbot_be.infra.vector;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VectorSearchResult {
    private String id;
    private String question; // 엑셀의 질문
    private String answer;   // 엑셀의 답변
    private double score;    // 유사도

}
