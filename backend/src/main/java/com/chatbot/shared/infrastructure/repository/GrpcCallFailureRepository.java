package com.chatbot.shared.infrastructure.repository;

import com.chatbot.shared.infrastructure.model.GrpcCallFailure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GrpcCallFailureRepository extends JpaRepository<GrpcCallFailure, Long> {

    List<GrpcCallFailure> findTop100ByOrderByOccurredAtDesc();

    List<GrpcCallFailure> findTop100ByMethodOrderByOccurredAtDesc(String method);
}
