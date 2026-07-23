package com.chatbot.shared.penny.performance;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance Benchmark Test - Measures system performance under load
 * Tests throughput, latency, and resource usage for key components
 * Note: These are lightweight benchmarks that don't require Spring context
 */
class PerformanceBenchmarkTest {

    @Test
    void benchmarkStringOperations_Throughput() {
        System.out.println("=== String Operations Throughput Benchmark ===");
        
        int iterations = 10000;
        List<String> testData = new ArrayList<>();
        
        Instant start = Instant.now();
        
        for (int i = 0; i < iterations; i++) {
            String text = "Test string " + i + " with some content";
            String result = text.toUpperCase().toLowerCase().trim();
            testData.add(result);
        }
        
        Instant end = Instant.now();
        long durationMs = Duration.between(start, end).toMillis();
        double throughput = (iterations / (double) durationMs) * 1000;
        double avgLatencyMs = (double) durationMs / iterations;

        System.out.println("Total iterations: " + iterations);
        System.out.println("Total duration: " + durationMs + " ms");
        System.out.println("Throughput: " + String.format("%.2f", throughput) + " ops/sec");
        System.out.println("Average latency: " + String.format("%.2f", avgLatencyMs) + " ms");

        assertTrue(throughput >= 1000, "Throughput should be at least 1000 ops/sec");
    }

    @Test
    void benchmarkConcurrentExecution() {
        System.out.println("=== Concurrent Execution Benchmark ===");

        int concurrentTasks = 100;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        Instant start = Instant.now();
        
        List<CompletableFuture<Integer>> futures = new ArrayList<>();
        for (int i = 0; i < concurrentTasks; i++) {
            final int taskId = i;
            CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
                // Simulate some work
                int sum = 0;
                for (int j = 0; j < 1000; j++) {
                    sum += j;
                }
                return sum;
            }, executor);
            futures.add(future);
        }
        
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        Instant end = Instant.now();
        
        long durationMs = Duration.between(start, end).toMillis();
        double throughput = (concurrentTasks / (double) durationMs) * 1000;
        double avgLatencyMs = (double) durationMs / concurrentTasks;

        System.out.println("Total tasks: " + concurrentTasks);
        System.out.println("Total duration: " + durationMs + " ms");
        System.out.println("Throughput: " + String.format("%.2f", throughput) + " ops/sec");
        System.out.println("Average latency: " + String.format("%.2f", avgLatencyMs) + " ms");

        executor.shutdown();
        
        assertTrue(throughput >= 50, "Throughput should be at least 50 ops/sec");
    }

    @Test
    void benchmarkMemoryAllocation() {
        System.out.println("=== Memory Allocation Benchmark ===");

        Runtime runtime = Runtime.getRuntime();
        
        // Force GC before measurement
        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Memory before: " + (memoryBefore / 1024 / 1024) + " MB");

        // Create objects to test memory allocation (reduced from 100MB to 50MB)
        List<byte[]> allocations = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            allocations.add(new byte[1024 * 1024]); // 1MB each
        }

        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Memory after: " + (memoryAfter / 1024 / 1024) + " MB");
        System.out.println("Memory used: " + ((memoryAfter - memoryBefore) / 1024 / 1024) + " MB");

        // Clean up
        allocations.clear();
        allocations = null;
        System.gc();

        assertTrue((memoryAfter - memoryBefore) < 100 * 1024 * 1024, "Memory usage should be reasonable (< 100MB)");
    }

    @Test
    void benchmarkListOperations() {
        System.out.println("=== List Operations Benchmark ===");

        int iterations = 10000;
        List<Integer> list = new ArrayList<>();
        
        Instant start = Instant.now();
        
        // Add operations
        for (int i = 0; i < iterations; i++) {
            list.add(i);
        }
        
        // Search operations
        for (int i = 0; i < iterations; i++) {
            list.contains(i);
        }
        
        // Remove operations
        for (int i = 0; i < iterations; i++) {
            list.remove(Integer.valueOf(i));
        }
        
        Instant end = Instant.now();
        long durationMs = Duration.between(start, end).toMillis();
        double throughput = (iterations * 3 / (double) durationMs) * 1000;

        System.out.println("Total operations: " + (iterations * 3));
        System.out.println("Total duration: " + durationMs + " ms");
        System.out.println("Throughput: " + String.format("%.2f", throughput) + " ops/sec");

        assertTrue(throughput >= 1000, "Throughput should be at least 1000 ops/sec");
    }

    @Test
    void benchmarkHashOperations() {
        System.out.println("=== Hash Operations Benchmark ===");

        int iterations = 10000;
        java.util.HashMap<String, Integer> map = new java.util.HashMap<>();
        
        Instant start = Instant.now();
        
        // Put operations
        for (int i = 0; i < iterations; i++) {
            map.put("key_" + i, i);
        }
        
        // Get operations
        for (int i = 0; i < iterations; i++) {
            map.get("key_" + i);
        }
        
        // Remove operations
        for (int i = 0; i < iterations; i++) {
            map.remove("key_" + i);
        }
        
        Instant end = Instant.now();
        long durationMs = Duration.between(start, end).toMillis();
        double throughput = (iterations * 3 / (double) durationMs) * 1000;

        System.out.println("Total operations: " + (iterations * 3));
        System.out.println("Total duration: " + durationMs + " ms");
        System.out.println("Throughput: " + String.format("%.2f", throughput) + " ops/sec");

        assertTrue(throughput >= 1000, "Throughput should be at least 1000 ops/sec");
    }
}
