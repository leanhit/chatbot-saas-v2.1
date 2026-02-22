package com.chatbot.shared.messaging;

import java.time.LocalDateTime;
import java.util.Map;

public class Event {

    private String eventId;
    private String eventType;
    private Object eventData;
    private LocalDateTime timestamp;
    private String correlationId;
    private String userId;
    private String tenantId;
    private String source;
    private String version;
    private Map<String, Object> metadata;

    public Event() {
        this.timestamp = LocalDateTime.now();
        this.version = "1.0";
    }

    public Event(String eventType, Object eventData) {
        this();
        this.eventType = eventType;
        this.eventData = eventData;
        this.eventId = java.util.UUID.randomUUID().toString();
    }

    public Event(String eventType, Object eventData, String correlationId) {
        this(eventType, eventData);
        this.correlationId = correlationId;
    }

    public Event(String eventType, Object eventData, String correlationId, String userId, String tenantId) {
        this(eventType, eventData, correlationId);
        this.userId = userId;
        this.tenantId = tenantId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Object getEventData() {
        return eventData;
    }

    public void setEventData(Object eventData) {
        this.eventData = eventData;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public boolean hasCorrelationId() {
        return correlationId != null && !correlationId.trim().isEmpty();
    }

    public boolean hasUserId() {
        return userId != null && !userId.trim().isEmpty();
    }

    public boolean hasTenantId() {
        return tenantId != null && !tenantId.trim().isEmpty();
    }

    public boolean hasSource() {
        return source != null && !source.trim().isEmpty();
    }

    public boolean hasMetadata() {
        return metadata != null && !metadata.isEmpty();
    }

    public void addMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = new java.util.HashMap<>();
        }
        metadata.put(key, value);
    }

    public Object getMetadata(String key) {
        return metadata != null ? metadata.get(key) : null;
    }

    public boolean hasMetadata(String key) {
        return metadata != null && metadata.containsKey(key);
    }

    public void removeMetadata(String key) {
        if (metadata != null) {
            metadata.remove(key);
        }
    }

    public void clearMetadata() {
        if (metadata != null) {
            metadata.clear();
        }
    }

    public long getAgeInSeconds() {
        return java.time.Duration.between(timestamp, LocalDateTime.now()).getSeconds();
    }

    public long getAgeInMinutes() {
        return java.time.Duration.between(timestamp, LocalDateTime.now()).toMinutes();
    }

    public boolean isExpired(long maxAgeSeconds) {
        return getAgeInSeconds() > maxAgeSeconds;
    }

    public Event copy() {
        Event copy = new Event();
        copy.eventId = this.eventId;
        copy.eventType = this.eventType;
        copy.eventData = this.eventData;
        copy.timestamp = this.timestamp;
        copy.correlationId = this.correlationId;
        copy.userId = this.userId;
        copy.tenantId = this.tenantId;
        copy.source = this.source;
        copy.version = this.version;
        copy.metadata = this.metadata != null ? new java.util.HashMap<>(this.metadata) : null;
        return copy;
    }

    @Override
    public String toString() {
        return "Event{" +
                "eventId='" + eventId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", timestamp=" + timestamp +
                ", correlationId='" + correlationId + '\'' +
                ", userId='" + userId + '\'' +
                ", tenantId='" + tenantId + '\'' +
                ", source='" + source + '\'' +
                ", version='" + version + '\'' +
                ", metadataSize=" + (metadata != null ? metadata.size() : 0) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return eventId != null ? eventId.equals(event.eventId) : event.eventId == null;
    }

    @Override
    public int hashCode() {
        return eventId != null ? eventId.hashCode() : 0;
    }

    public static EventBuilder builder() {
        return new EventBuilder();
    }

    public static class EventBuilder {
        private final Event event;

        private EventBuilder() {
            this.event = new Event();
        }

        public EventBuilder eventId(String eventId) {
            event.setEventId(eventId);
            return this;
        }

        public EventBuilder eventType(String eventType) {
            event.setEventType(eventType);
            return this;
        }

        public EventBuilder eventData(Object eventData) {
            event.setEventData(eventData);
            return this;
        }

        public EventBuilder timestamp(LocalDateTime timestamp) {
            event.setTimestamp(timestamp);
            return this;
        }

        public EventBuilder correlationId(String correlationId) {
            event.setCorrelationId(correlationId);
            return this;
        }

        public EventBuilder userId(String userId) {
            event.setUserId(userId);
            return this;
        }

        public EventBuilder tenantId(String tenantId) {
            event.setTenantId(tenantId);
            return this;
        }

        public EventBuilder source(String source) {
            event.setSource(source);
            return this;
        }

        public EventBuilder version(String version) {
            event.setVersion(version);
            return this;
        }

        public EventBuilder metadata(Map<String, Object> metadata) {
            event.setMetadata(metadata);
            return this;
        }

        public EventBuilder addMetadata(String key, Object value) {
            event.addMetadata(key, value);
            return this;
        }

        public Event build() {
            if (event.getEventId() == null) {
                event.setEventId(java.util.UUID.randomUUID().toString());
            }
            if (event.getTimestamp() == null) {
                event.setTimestamp(LocalDateTime.now());
            }
            if (event.getVersion() == null) {
                event.setVersion("1.0");
            }
            return event;
        }
    }
}
