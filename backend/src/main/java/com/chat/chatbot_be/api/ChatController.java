package com.chat.chatbot_be.api;

import com.chat.chatbot_be.domain.service.QaService;
import com.chat.chatbot_be.api.dto.AnswerResponse;
import com.chat.chatbot_be.api.dto.QuestionRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final QaService qaService;

    public ChatController(QaService qaService) {
        this.qaService = qaService;
    }

    @PostMapping("/ask")
    public AnswerResponse ask(@RequestBody QuestionRequest request) {
        return qaService.answerQuestion(request.getQuestion());
    }
}
