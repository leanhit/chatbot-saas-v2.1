package com.chatbot.spokes.facebook.autoconnect.service;

import com.chatbot.spokes.facebook.connection.model.FacebookConnection;
import com.chatbot.spokes.facebook.connection.repository.FacebookConnectionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class FbConnectionPersistenceService {

    private final FacebookConnectionRepository repository;

    public FbConnectionPersistenceService(FacebookConnectionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void saveConnectionsTransactional(List<FacebookConnection> connections, String ownerId) {
        repository.saveAll(connections);
        log.info("💾 Đã lưu {} connection thay đổi cho user {}", connections.size(), ownerId);
    }
}
