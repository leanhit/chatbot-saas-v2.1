package com.chatbot.shared.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Iterator;

public class JsonUtils {

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
    }

    public static String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }

    public static String toJsonPretty(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize object to pretty JSON", e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        if (StringUtils.isEmpty(json)) return null;
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JSON to object", e);
        }
    }

    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (StringUtils.isEmpty(json)) return null;
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JSON to object", e);
        }
    }

    public static <T> List<T> fromJsonList(String json, Class<T> clazz) {
        if (StringUtils.isEmpty(json)) return null;
        try {
            return objectMapper.readValue(json, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JSON to list", e);
        }
    }

    public static <K, V> Map<K, V> fromJsonMap(String json, Class<K> keyClass, Class<V> valueClass) {
        if (StringUtils.isEmpty(json)) return null;
        try {
            return objectMapper.readValue(json, 
                objectMapper.getTypeFactory().constructMapType(Map.class, keyClass, valueClass));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JSON to map", e);
        }
    }

    public static JsonNode parseJson(String json) {
        if (StringUtils.isEmpty(json)) return null;
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse JSON", e);
        }
    }

    public static boolean isValidJson(String json) {
        if (StringUtils.isEmpty(json)) return false;
        try {
            objectMapper.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    public static String extractValue(String json, String jsonPath) {
        JsonNode node = parseJson(json);
        if (node == null) return null;
        
        String[] pathParts = jsonPath.split("\\.");
        JsonNode current = node;
        
        for (String part : pathParts) {
            if (current == null) return null;
            
            if (part.contains("[") && part.endsWith("]")) {
                String fieldName = part.substring(0, part.indexOf("["));
                String indexStr = part.substring(part.indexOf("[") + 1, part.length() - 1);
                
                if (StringUtils.isNotEmpty(fieldName)) {
                    current = current.get(fieldName);
                }
                
                if (current != null && StringUtils.isNumeric(indexStr)) {
                    int index = Integer.parseInt(indexStr);
                    current = current.get(index);
                }
            } else {
                current = current.get(part);
            }
        }
        
        return current != null ? current.asText() : null;
    }

    public static <T> T extractValue(String json, String jsonPath, Class<T> clazz) {
        JsonNode node = parseJson(json);
        if (node == null) return null;
        
        String[] pathParts = jsonPath.split("\\.");
        JsonNode current = node;
        
        for (String part : pathParts) {
            if (current == null) return null;
            
            if (part.contains("[") && part.endsWith("]")) {
                String fieldName = part.substring(0, part.indexOf("["));
                String indexStr = part.substring(part.indexOf("[") + 1, part.length() - 1);
                
                if (StringUtils.isNotEmpty(fieldName)) {
                    current = current.get(fieldName);
                }
                
                if (current != null && StringUtils.isNumeric(indexStr)) {
                    int index = Integer.parseInt(indexStr);
                    current = current.get(index);
                }
            } else {
                current = current.get(part);
            }
        }
        
        if (current == null) return null;
        
        try {
            return objectMapper.treeToValue(current, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert JSON node to object", e);
        }
    }

    public static boolean hasField(String json, String fieldName) {
        JsonNode node = parseJson(json);
        return node != null && node.has(fieldName);
    }

    public static boolean hasPath(String json, String jsonPath) {
        JsonNode node = parseJson(json);
        if (node == null) return false;
        
        String[] pathParts = jsonPath.split("\\.");
        JsonNode current = node;
        
        for (String part : pathParts) {
            if (current == null) return false;
            
            if (part.contains("[") && part.endsWith("]")) {
                String fieldName = part.substring(0, part.indexOf("["));
                String indexStr = part.substring(part.indexOf("[") + 1, part.length() - 1);
                
                if (StringUtils.isNotEmpty(fieldName)) {
                    current = current.get(fieldName);
                }
                
                if (current != null && StringUtils.isNumeric(indexStr)) {
                    int index = Integer.parseInt(indexStr);
                    current = current.get(index);
                }
            } else {
                current = current.get(part);
            }
        }
        
        return current != null;
    }

    public static String mergeJson(String json1, String json2) {
        try {
            JsonNode node1 = parseJson(json1);
            JsonNode node2 = parseJson(json2);
            
            if (node1 == null) return json2;
            if (node2 == null) return json1;
            
            JsonNode merged = mergeNodes(node1, node2);
            return objectMapper.writeValueAsString(merged);
        } catch (Exception e) {
            throw new RuntimeException("Failed to merge JSON", e);
        }
    }

    private static JsonNode mergeNodes(JsonNode node1, JsonNode node2) {
        if (node2.isObject()) {
            ObjectNode merged = (ObjectNode) node1.deepCopy();
            Iterator<Map.Entry<String, JsonNode>> fields = node2.fields();
            
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                JsonNode value = field.getValue();
                
                if (value.isObject() && merged.has(field.getKey())) {
                    JsonNode existingValue = merged.get(field.getKey());
                    if (existingValue.isObject()) {
                        merged.set(field.getKey(), mergeNodes(existingValue, value));
                    } else {
                        merged.set(field.getKey(), value);
                    }
                } else {
                    merged.set(field.getKey(), value);
                }
            }
            
            return merged;
        } else {
            return node2;
        }
    }

    public static String removeField(String json, String fieldName) {
        JsonNode node = parseJson(json);
        if (node == null || !node.isObject()) return json;
        
        ObjectNode objectNode = (ObjectNode) node;
        objectNode.remove(fieldName);
        
        try {
            return objectMapper.writeValueAsString(objectNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to remove field from JSON", e);
        }
    }

    public static String addField(String json, String fieldName, Object value) {
        JsonNode node = parseJson(json);
        ObjectNode objectNode;
        
        if (node == null) {
            objectNode = objectMapper.createObjectNode();
        } else if (node.isObject()) {
            objectNode = (ObjectNode) node;
        } else {
            throw new RuntimeException("Cannot add field to non-object JSON");
        }
        
        JsonNode valueNode = objectMapper.valueToTree(value);
        objectNode.set(fieldName, valueNode);
        
        try {
            return objectMapper.writeValueAsString(objectNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to add field to JSON", e);
        }
    }

    public static String updateField(String json, String fieldName, Object value) {
        return addField(json, fieldName, value);
    }

    public static String flattenJson(String json) {
        JsonNode node = parseJson(json);
        if (node == null) return null;
        
        Map<String, Object> flattened = flattenNode(node, "");
        try {
            return objectMapper.writeValueAsString(flattened);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to flatten JSON", e);
        }
    }

    private static Map<String, Object> flattenNode(JsonNode node, String prefix) {
        Map<String, Object> result = new java.util.HashMap<>();
        
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String newPrefix = prefix.isEmpty() ? field.getKey() : prefix + "." + field.getKey();
                result.putAll(flattenNode(field.getValue(), newPrefix));
            }
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                String newPrefix = prefix + "[" + i + "]";
                result.putAll(flattenNode(node.get(i), newPrefix));
            }
        } else {
            result.put(prefix, node.asText());
        }
        
        return result;
    }

    public static String minifyJson(String json) {
        if (StringUtils.isEmpty(json)) return json;
        return toJson(parseJson(json));
    }

    public static String formatJson(String json) {
        return toJsonPretty(parseJson(json));
    }

    public static Map<String, Object> jsonToMap(String json) {
        return fromJsonMap(json, String.class, Object.class);
    }

    public static <T> Map<String, T> jsonToTypedMap(String json, Class<T> valueClass) {
        return fromJsonMap(json, String.class, valueClass);
    }

    public static String mapToJson(Map<String, Object> map) {
        return toJson(map);
    }

    public static Optional<String> getOptionalValue(String json, String jsonPath) {
        return Optional.ofNullable(extractValue(json, jsonPath));
    }

    public static <T> Optional<T> getOptionalValue(String json, String jsonPath, Class<T> clazz) {
        return Optional.ofNullable(extractValue(json, jsonPath, clazz));
    }

    public static boolean isEmptyJson(String json) {
        if (StringUtils.isEmpty(json)) return true;
        JsonNode node = parseJson(json);
        return node == null || node.isNull() || node.isEmpty();
    }

    public static int getJsonSize(String json) {
        JsonNode node = parseJson(json);
        if (node == null) return 0;
        
        if (node.isObject()) {
            return node.size();
        } else if (node.isArray()) {
            return node.size();
        } else {
            return 1;
        }
    }

    public static List<String> getFieldNames(String json) {
        JsonNode node = parseJson(json);
        if (node == null || !node.isObject()) return new java.util.ArrayList<>();
        
        List<String> fieldNames = new java.util.ArrayList<>();
        Iterator<String> fieldIterator = node.fieldNames();
        while (fieldIterator.hasNext()) {
            fieldNames.add(fieldIterator.next());
        }
        
        return fieldNames;
    }

    public static String[] getFieldNamesArray(String json) {
        return getFieldNames(json).toArray(new String[0]);
    }
}
