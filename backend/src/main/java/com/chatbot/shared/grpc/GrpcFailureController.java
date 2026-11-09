package com.chatbot.shared.grpc;

import com.chatbot.shared.infrastructure.model.GrpcCallFailure;
import com.chatbot.shared.infrastructure.repository.GrpcCallFailureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Inspect recorded failed gRPC calls.
 */
@RestController
@RequestMapping("/api/admin/dlq/grpc")
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class GrpcFailureController {

    private final GrpcCallFailureRepository repository;

    @GetMapping("/failures")
    public ResponseEntity<List<GrpcCallFailure>> listFailures(
            @RequestParam(required = false) String method) {
        if (method != null && !method.isBlank()) {
            return ResponseEntity.ok(repository.findTop100ByMethodOrderByOccurredAtDesc(method));
        }
        return ResponseEntity.ok(repository.findTop100ByOrderByOccurredAtDesc());
    }

    @GetMapping("/failures/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        long total = repository.count();
        return ResponseEntity.ok(Map.of("totalFailures", total));
    }
}
