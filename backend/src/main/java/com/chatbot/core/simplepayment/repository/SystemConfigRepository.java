package com.chatbot.core.simplepayment.repository;

import com.chatbot.core.simplepayment.model.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {

    Optional<SystemConfig> findByConfigKey(String configKey);

    List<SystemConfig> findByConfigCategory(String configCategory);

    boolean existsByConfigKey(String configKey);

    @Query("SELECT sc FROM SystemConfig sc WHERE sc.configKey LIKE :pattern")
    List<SystemConfig> searchByConfigKey(@Param("pattern") String pattern);

    @Query("SELECT sc FROM SystemConfig sc WHERE sc.configCategory = :category AND sc.isSensitive = false")
    List<SystemConfig> findNonSensitiveByCategory(@Param("category") String category);
}
