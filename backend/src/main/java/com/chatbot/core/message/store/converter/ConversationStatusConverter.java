package com.chatbot.core.message.store.converter;

import com.chatbot.core.message.store.model.ConversationStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ConversationStatusConverter implements AttributeConverter<ConversationStatus, String> {

    @Override
    public String convertToDatabaseColumn(ConversationStatus attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public ConversationStatus convertToEntityAttribute(String dbData) {
        return dbData == null || dbData.isBlank() ? null : ConversationStatus.fromValue(dbData);
    }
}
