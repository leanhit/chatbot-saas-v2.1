package com.chatbot.core.simplepayment.repository;

import com.chatbot.core.simplepayment.model.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {
    
    Optional<Discount> findByCode(String code);
    
    List<Discount> findByIsActiveTrueOrderByCreatedAtDesc();
    
    @Query("SELECT d FROM Discount d WHERE d.isActive = true AND d.validFrom <= :now AND d.validUntil >= :now")
    List<Discount> findActiveDiscounts(@Param("now") LocalDateTime now);
    
    @Query("SELECT d FROM Discount d WHERE d.code = :code AND d.isActive = true AND d.validFrom <= :now AND d.validUntil >= :now")
    Optional<Discount> findActiveDiscountByCode(@Param("code") String code, @Param("now") LocalDateTime now);
    
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Discount d WHERE d.code = :code")
    boolean existsByCode(@Param("code") String code);
    
    @Query("SELECT d FROM Discount d WHERE d.validUntil < :now")
    List<Discount> findExpiredDiscounts(@Param("now") LocalDateTime now);
}
