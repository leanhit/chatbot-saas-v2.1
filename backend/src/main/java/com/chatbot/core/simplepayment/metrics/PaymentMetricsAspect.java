package com.chatbot.core.simplepayment.metrics;

import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentMetricsAspect {

    private final PaymentMetricsService paymentMetricsService;

    @Around("execution(* com.chatbot.core.simplepayment.service.SimplePaymentService.createDeposit(..))")
    public Object trackCreateDeposit(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = paymentMetricsService.startPaymentProcessingTimer();
        
        try {
            Object result = joinPoint.proceed();
            paymentMetricsService.incrementPaymentCreated();
            paymentMetricsService.stopPaymentProcessingTimer(sample);
            return result;
        } catch (Exception e) {
            paymentMetricsService.incrementPaymentFailed();
            paymentMetricsService.stopPaymentProcessingTimer(sample);
            throw e;
        }
    }

    @Around("execution(* com.chatbot.core.simplepayment.service.SimplePaymentService.completePayment(..))")
    public Object trackCompletePayment(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = paymentMetricsService.startPaymentProcessingTimer();
        
        try {
            Object result = joinPoint.proceed();
            paymentMetricsService.incrementPaymentCompleted();
            paymentMetricsService.stopPaymentProcessingTimer(sample);
            return result;
        } catch (Exception e) {
            paymentMetricsService.incrementPaymentFailed();
            paymentMetricsService.stopPaymentProcessingTimer(sample);
            throw e;
        }
    }

    @Around("execution(* com.chatbot.core.simplepayment.service.BankApiService.*(..))")
    public Object trackBankApiCalls(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = paymentMetricsService.startBankApiCallTimer();
        
        try {
            Object result = joinPoint.proceed();
            paymentMetricsService.stopBankApiCallTimer(sample);
            return result;
        } catch (Exception e) {
            paymentMetricsService.stopBankApiCallTimer(sample);
            throw e;
        }
    }

    @Around("execution(* com.chatbot.core.simplepayment.service.QRCodeService.generateQRCode(..))")
    public Object trackQrCodeGeneration(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = paymentMetricsService.startQrCodeGenerationTimer();
        
        try {
            Object result = joinPoint.proceed();
            paymentMetricsService.stopQrCodeGenerationTimer(sample);
            return result;
        } catch (Exception e) {
            paymentMetricsService.stopQrCodeGenerationTimer(sample);
            throw e;
        }
    }
}
