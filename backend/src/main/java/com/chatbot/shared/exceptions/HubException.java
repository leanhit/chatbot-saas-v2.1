package com.chatbot.shared.exceptions;

public class HubException extends RuntimeException {

    private String hubName;
    private String errorCode;
    private Object context;

    public HubException() {
        super();
    }

    public HubException(String message) {
        super(message);
    }

    public HubException(String message, Throwable cause) {
        super(message, cause);
    }

    public HubException(String message, String hubName) {
        super(message);
        this.hubName = hubName;
    }

    public HubException(String message, String hubName, String errorCode) {
        super(message);
        this.hubName = hubName;
        this.errorCode = errorCode;
    }

    public HubException(String message, String hubName, String errorCode, Object context) {
        super(message);
        this.hubName = hubName;
        this.errorCode = errorCode;
        this.context = context;
    }

    public HubException(String message, Throwable cause, String hubName) {
        super(message, cause);
        this.hubName = hubName;
    }

    public HubException(String message, Throwable cause, String hubName, String errorCode) {
        super(message, cause);
        this.hubName = hubName;
        this.errorCode = errorCode;
    }

    public HubException(String message, Throwable cause, String hubName, String errorCode, Object context) {
        super(message, cause);
        this.hubName = hubName;
        this.errorCode = errorCode;
        this.context = context;
    }

    public String getHubName() {
        return hubName;
    }

    public void setHubName(String hubName) {
        this.hubName = hubName;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public Object getContext() {
        return context;
    }

    public void setContext(Object context) {
        this.context = context;
    }

    public boolean hasHubName() {
        return hubName != null && !hubName.trim().isEmpty();
    }

    public boolean hasErrorCode() {
        return errorCode != null && !errorCode.trim().isEmpty();
    }

    public boolean hasContext() {
        return context != null;
    }

    @Override
    public String toString() {
        return "HubException{" +
                "hubName='" + hubName + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", message='" + getMessage() + '\'' +
                ", context=" + context +
                '}';
    }

    public static HubException identityHub(String message) {
        return new HubException(message, "identity");
    }

    public static HubException identityHub(String message, String errorCode) {
        return new HubException(message, "identity", errorCode);
    }

    public static HubException userHub(String message) {
        return new HubException(message, "user");
    }

    public static HubException userHub(String message, String errorCode) {
        return new HubException(message, "user", errorCode);
    }

    public static HubException tenantHub(String message) {
        return new HubException(message, "tenant");
    }

    public static HubException tenantHub(String message, String errorCode) {
        return new HubException(message, "tenant", errorCode);
    }

    public static HubException appHub(String message) {
        return new HubException(message, "app");
    }

    public static HubException appHub(String message, String errorCode) {
        return new HubException(message, "app", errorCode);
    }

    public static HubException billingHub(String message) {
        return new HubException(message, "billing");
    }

    public static HubException billingHub(String message, String errorCode) {
        return new HubException(message, "billing", errorCode);
    }

    public static HubException walletHub(String message) {
        return new HubException(message, "wallet");
    }

    public static HubException walletHub(String message, String errorCode) {
        return new HubException(message, "wallet", errorCode);
    }

    public static HubException configHub(String message) {
        return new HubException(message, "config");
    }

    public static HubException configHub(String message, String errorCode) {
        return new HubException(message, "config", errorCode);
    }

    public static HubException messageHub(String message) {
        return new HubException(message, "message");
    }

    public static HubException messageHub(String message, String errorCode) {
        return new HubException(message, "message", errorCode);
    }

    public static HubException facebookSpoke(String message) {
        return new HubException(message, "facebook");
    }

    public static HubException facebookSpoke(String message, String errorCode) {
        return new HubException(message, "facebook", errorCode);
    }

    public static HubException botpressSpoke(String message) {
        return new HubException(message, "botpress");
    }

    public static HubException botpressSpoke(String message, String errorCode) {
        return new HubException(message, "botpress", errorCode);
    }

    public static HubException odooSpoke(String message) {
        return new HubException(message, "odoo");
    }

    public static HubException odooSpoke(String message, String errorCode) {
        return new HubException(message, "odoo", errorCode);
    }

    public static HubException minioSpoke(String message) {
        return new HubException(message, "minio");
    }

    public static HubException minioSpoke(String message, String errorCode) {
        return new HubException(message, "minio", errorCode);
    }

    public static HubException withContext(String message, String hubName, Object context) {
        return new HubException(message, hubName, null, context);
    }

    public static HubException withErrorCode(String message, String hubName, String errorCode) {
        return new HubException(message, hubName, errorCode);
    }

    public static HubException full(String message, String hubName, String errorCode, Object context) {
        return new HubException(message, hubName, errorCode, context);
    }
}
