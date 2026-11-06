package com.chatbot.core.ai.service;

import com.chatbot.core.ai.model.SentimentLabel;
import com.chatbot.core.ai.model.SentimentSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for lexicon-based sentiment scoring (deterministic, no Spring context).
 */
class SentimentServiceTest {

    private final SentimentService service = new SentimentService(
        null, null, null, null, null);

    @Test
    void positiveTextScoresPositive() {
        SentimentService.ScoredText result = service.scoreWithLexicon("Thank you, great service!");
        assertTrue(result.score() > 0);
        assertEquals(SentimentLabel.POSITIVE, result.label());
        assertEquals(SentimentSource.LEXICON, result.source());
    }

    @Test
    void negativeTextScoresNegative() {
        SentimentService.ScoredText result = service.scoreWithLexicon("This is terrible, I am very angry and frustrated");
        assertTrue(result.score() < 0);
        assertEquals(SentimentLabel.NEGATIVE, result.label());
    }

    @Test
    void neutralTextScoresNeutral() {
        SentimentService.ScoredText result = service.scoreWithLexicon("I have a question about my order status");
        assertEquals(0.0, result.score());
        assertEquals(SentimentLabel.NEUTRAL, result.label());
    }

    @Test
    void blankTextScoresNeutral() {
        SentimentService.ScoredText result = service.scoreWithLexicon("   ");
        assertEquals(0.0, result.score());
        assertEquals(SentimentLabel.NEUTRAL, result.label());
    }

    @Test
    void nullTextScoresNeutral() {
        SentimentService.ScoredText result = service.scoreWithLexicon(null);
        assertEquals(0.0, result.score());
        assertEquals(SentimentLabel.NEUTRAL, result.label());
    }

    @Test
    void vietnamesePositiveDetected() {
        SentimentService.ScoredText result = service.scoreWithLexicon("Cảm ơn bạn, dịch vụ tốt quá");
        assertTrue(result.score() > 0);
    }

    @Test
    void scoreWithinBounds() {
        SentimentService.ScoredText result = service.scoreWithLexicon("good great excellent amazing wonderful happy thanks love perfect awesome nice helpful solved resolved appreciate");
        assertTrue(result.score() >= -1.0 && result.score() <= 1.0);
    }
}
