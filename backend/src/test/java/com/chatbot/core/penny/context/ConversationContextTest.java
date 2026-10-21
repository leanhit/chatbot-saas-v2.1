package com.chatbot.core.penny.context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ConversationContext helper methods.
 * No Spring context needed — pure POJO logic.
 */
class ConversationContextTest {

    private ConversationContext ctx;

    @BeforeEach
    void setUp() {
        ctx = ConversationContext.builder()
            .contextId("ctx-1")
            .userId("user-1")
            .botId("bot-1")
            .platform("facebook")
            .sessionId("session-1")
            .createdAt(Instant.now())
            .lastUpdated(Instant.now())
            .status("active")
            .build();
    }

    // ── messageCount ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("incrementMessageCount()")
    class MessageCountTests {

        @Test
        @DisplayName("should start from 0 when messageCount is null and increment correctly")
        void startsFromNullAndIncrements() {
            assertNull(ctx.getMessageCount());

            ctx.incrementMessageCount();
            assertEquals(1, ctx.getMessageCount());
            assertEquals(1, ctx.getMessageCountInCurrentSession());

            ctx.incrementMessageCount();
            assertEquals(2, ctx.getMessageCount());
            assertEquals(2, ctx.getMessageCountInCurrentSession());
        }

        @Test
        @DisplayName("should reset session count but keep total count")
        void resetSessionCount() {
            ctx.incrementMessageCount();
            ctx.incrementMessageCount();
            ctx.incrementMessageCount();

            ctx.resetSessionMessageCount();

            assertEquals(3, ctx.getMessageCount()); // total intact
            assertEquals(0, ctx.getMessageCountInCurrentSession()); // session reset
        }

        @Test
        @DisplayName("isNewConversation() returns true when messageCount ≤ 1")
        void isNewConversation_whenFewMessages() {
            assertTrue(ctx.isNewConversation()); // null → new
            ctx.incrementMessageCount();
            assertTrue(ctx.isNewConversation()); // 1 → still new
            ctx.incrementMessageCount();
            assertFalse(ctx.isNewConversation()); // 2 → returning
        }

        @Test
        @DisplayName("isReturningUser() returns true when messageCount > 1")
        void isReturningUser_whenManyMessages() {
            assertFalse(ctx.isReturningUser()); // null
            ctx.incrementMessageCount();
            assertFalse(ctx.isReturningUser()); // 1
            ctx.incrementMessageCount();
            assertTrue(ctx.isReturningUser()); // 2 → returning
        }
    }

    // ── intentHistory ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("addIntentToHistory()")
    class IntentHistoryTests {

        @Test
        @DisplayName("should initialise list and add intents correctly")
        void addsIntentsToList() {
            assertNull(ctx.getIntentHistory());

            ctx.addIntentToHistory("greeting");
            ctx.addIntentToHistory("order_inquiry");

            assertEquals(2, ctx.getIntentHistory().size());
            assertEquals("greeting", ctx.getIntentHistory().get(0));
            assertEquals("order_inquiry", ctx.getIntentHistory().get(1));
        }

        @Test
        @DisplayName("should keep only the last 50 intents")
        void keepsLast50() {
            for (int i = 0; i < 55; i++) {
                ctx.addIntentToHistory("intent_" + i);
            }
            assertEquals(50, ctx.getIntentHistory().size());
            // Oldest 5 dropped → first element is now intent_5
            assertEquals("intent_5", ctx.getIntentHistory().get(0));
        }

        @Test
        @DisplayName("getLastIntents() returns correct count in order")
        void getLastIntents() {
            for (int i = 0; i < 10; i++) {
                ctx.addIntentToHistory("i_" + i);
            }
            var last3 = ctx.getLastIntents(3);
            assertEquals(3, last3.size());
            assertEquals("i_9", last3.get(2));
            assertEquals("i_7", last3.get(0));
        }
    }

    // ── providerHistory ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("addProviderToHistory()")
    class ProviderHistoryTests {

        @Test
        @DisplayName("should keep only the last 20 providers")
        void keepsLast20() {
            for (int i = 0; i < 25; i++) {
                ctx.addProviderToHistory("PROVIDER_" + i);
            }
            assertEquals(20, ctx.getProviderHistory().size());
            assertEquals("PROVIDER_5", ctx.getProviderHistory().get(0));
        }
    }

    // ── expiry ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("isExpired()")
    class ExpiryTests {

        @Test
        @DisplayName("should return false when expiresAt is null")
        void noExpiryDate_notExpired() {
            assertNull(ctx.getExpiresAt());
            assertFalse(ctx.isExpired());
        }

        @Test
        @DisplayName("should return true when expiresAt is in the past")
        void pastExpiry_isExpired() {
            ctx.setExpiresAt(Instant.now().minusSeconds(60));
            assertTrue(ctx.isExpired());
        }

        @Test
        @DisplayName("should return false when expiresAt is in the future")
        void futureExpiry_notExpired() {
            ctx.setExpiresAt(Instant.now().plusSeconds(3600));
            assertFalse(ctx.isExpired());
        }
    }

    // ── isActive ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("isActive()")
    class IsActiveTests {

        @Test
        @DisplayName("should return true when status='active' and not expired")
        void activeNotExpired_isActive() {
            ctx.setStatus("active");
            ctx.setExpiresAt(Instant.now().plusSeconds(3600));
            assertTrue(ctx.isActive());
        }

        @Test
        @DisplayName("should return false when status != 'active'")
        void wrongStatus_notActive() {
            ctx.setStatus("closed");
            assertFalse(ctx.isActive());
        }

        @Test
        @DisplayName("should return false when expired even if status='active'")
        void expired_notActive() {
            ctx.setStatus("active");
            ctx.setExpiresAt(Instant.now().minusSeconds(1));
            assertFalse(ctx.isActive());
        }
    }

    // ── escalation ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("needsEscalation()")
    class EscalationTests {

        @Test
        @DisplayName("should return true when needsHumanIntervention = true")
        void humanIntervention_needsEscalation() {
            ctx.setNeedsHumanIntervention(true);
            assertTrue(ctx.needsEscalation());
        }

        @Test
        @DisplayName("should return true when escalationLevel > 0")
        void escalationLevel_needsEscalation() {
            ctx.setEscalationLevel(1);
            assertTrue(ctx.needsEscalation());
        }

        @Test
        @DisplayName("should return false when both conditions are absent")
        void noEscalation_returnsFalse() {
            ctx.setNeedsHumanIntervention(false);
            ctx.setEscalationLevel(0);
            assertFalse(ctx.needsEscalation());
        }
    }

    // ── metadata ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Metadata operations")
    class MetadataTests {

        @Test
        @DisplayName("should add and retrieve typed metadata values")
        void addAndGetMetadata() {
            ctx.addMetadata("order_id", "#ABC123");
            ctx.addMetadata("retry_count", 3);

            assertEquals("#ABC123", ctx.getMetadata("order_id", String.class));
            assertEquals(3, ctx.getMetadata("retry_count", Integer.class));
        }

        @Test
        @DisplayName("should return null for missing metadata key")
        void missingKey_returnsNull() {
            assertNull(ctx.getMetadata("nonexistent", String.class));
        }

        @Test
        @DisplayName("should return null for type mismatch in metadata")
        void typeMismatch_returnsNull() {
            ctx.addMetadata("count", "not-a-number");
            assertNull(ctx.getMetadata("count", Integer.class));
        }
    }

    // ── sessionData & variables ───────────────────────────────────────────────

    @Nested
    @DisplayName("SessionData and Variables")
    class SessionAndVariableTests {

        @Test
        @DisplayName("should store and retrieve session data")
        void sessionDataRoundTrip() {
            ctx.addSessionData("cart_size", 5);
            assertEquals(5, ctx.getSessionData("cart_size", Integer.class));
        }

        @Test
        @DisplayName("should store and retrieve custom variables")
        void variableRoundTrip() {
            ctx.setVariable("promo_code", "SALE20");
            assertEquals("SALE20", ctx.getVariable("promo_code", String.class));
        }
    }

    // ── tags ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Tag operations")
    class TagTests {

        @Test
        @DisplayName("should add, check, and remove tags correctly")
        void tagLifecycle() {
            ctx.addTag("vip");
            ctx.addTag("returning");

            assertTrue(ctx.hasTag("vip"));
            assertTrue(ctx.hasTag("returning"));
            assertFalse(ctx.hasTag("new"));

            ctx.removeTag("vip");
            assertFalse(ctx.hasTag("vip"));
            assertTrue(ctx.hasTag("returning")); // unaffected
        }
    }

    // ── message history ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("addMessageToHistory()")
    class MessageHistoryTests {

        @Test
        @DisplayName("should store user and bot messages with timestamp")
        void storesMessagesWithTimestamp() {
            ctx.addMessageToHistory("Xin chào", "Chào bạn!");
            ctx.addMessageToHistory("Giá sản phẩm?", "300.000đ");

            assertEquals(2, ctx.getMessageHistory().size());

            Map<String, String> first = ctx.getMessageHistory().get(0);
            assertEquals("Xin chào", first.get("user"));
            assertEquals("Chào bạn!", first.get("bot"));
            assertNotNull(first.get("timestamp"));
        }
    }

    // ── processing time ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("Processing time")
    class ProcessingTimeTests {

        @Test
        @DisplayName("should accumulate processing time correctly")
        void accumulatesProcessingTime() {
            ctx.incrementMessageCount();
            ctx.incrementMessageCount();
            ctx.addProcessingTime(100L);
            ctx.addProcessingTime(300L);

            assertEquals(400L, ctx.getTotalProcessingTime());
            assertEquals(200.0, ctx.getAverageProcessingTime(), 0.01);
        }

        @Test
        @DisplayName("getAverageProcessingTime() returns 0 when no messages")
        void zeroMessages_zeroAverage() {
            assertEquals(0.0, ctx.getAverageProcessingTime(), 0.0);
        }
    }

    // ── error count ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("incrementErrorCount()")
    class ErrorCountTests {

        @Test
        @DisplayName("should start from null and increment correctly")
        void incrementsFromNull() {
            assertNull(ctx.getErrorCount());
            ctx.incrementErrorCount();
            assertEquals(1, ctx.getErrorCount());
            ctx.incrementErrorCount();
            assertEquals(2, ctx.getErrorCount());
        }
    }

    // ── toString ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("toString() should include contextId and userId without NPE")
    void toStringDoesNotThrow() {
        String str = assertDoesNotThrow(ctx::toString);
        assertTrue(str.contains("ctx-1"));
        assertTrue(str.contains("user-1"));
    }
}
