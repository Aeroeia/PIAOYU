package com.damai.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;

import static org.springframework.ai.chat.messages.MessageType.ASSISTANT;
import static org.springframework.ai.chat.messages.MessageType.USER;

@NoArgsConstructor
@Data
public class ChatHistoryMessageVO {
    private String role;
    private String content;

    public ChatHistoryMessageVO(Message message) {
        MessageType messageType = message.getMessageType();
        if (messageType == USER) {
            this.role = "user";
        }else if (messageType == ASSISTANT) {
            this.role = "assistant";
        }else {
            this.role = "";
        }
        this.content = message.getText();
    }
}
