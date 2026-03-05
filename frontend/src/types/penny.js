/**
 * Frontend types for Penny Middleware Package
 * Corresponds to backend DTOs in com.chatbot.shared.penny.dto
 */

// Penny Bot Type Enum
export const PennyBotType = {
  CUSTOMER_SERVICE: 'CUSTOMER_SERVICE',
  SALES: 'SALES',
  SUPPORT: 'SUPPORT',
  MARKETING: 'MARKETING',
  HR: 'HR',
  FINANCE: 'FINANCE',
  GENERAL: 'GENERAL'
};

// Penny Bot Type Display Names
export const PennyBotTypeDisplay = {
  [PennyBotType.CUSTOMER_SERVICE]: 'Customer Service',
  [PennyBotType.SALES]: 'Sales',
  [PennyBotType.SUPPORT]: 'Technical Support',
  [PennyBotType.MARKETING]: 'Marketing',
  [PennyBotType.HR]: 'Human Resources',
  [PennyBotType.FINANCE]: 'Finance',
  [PennyBotType.GENERAL]: 'General Purpose'
};

// Penny Bot Type Botpress IDs
export const PennyBotTypeBotpressId = {
  [PennyBotType.CUSTOMER_SERVICE]: 'botpress-customer-service-001',
  [PennyBotType.SALES]: 'botpress-sales-001',
  [PennyBotType.SUPPORT]: 'botpress-support-001',
  [PennyBotType.MARKETING]: 'botpress-marketing-001',
  [PennyBotType.HR]: 'botpress-hr-001',
  [PennyBotType.FINANCE]: 'botpress-finance-001',
  [PennyBotType.GENERAL]: 'botpress-general-001'
};

/**
 * Penny Bot DTO
 * Corresponds to: com.chatbot.shared.penny.dto.PennyBotDto
 */
export class PennyBotDto {
  constructor(data = {}) {
    this.id = data.id || data.botId; // Handle both 'id' and 'botId' from API
    this.botName = data.botName;
    this.botType = data.botType;
    this.tenantKey = data.tenantKey;
    this.ownerId = data.ownerId;
    this.botpressBotId = data.botpressBotId;
    this.description = data.description;
    this.isActive = data.isActive;
    this.isEnabled = data.isEnabled;
    this.createdAt = data.createdAt;
    this.updatedAt = data.updatedAt;
  }

  /**
   * Get display name for bot type
   */
  getBotTypeDisplay() {
    return PennyBotTypeDisplay[this.botType] || 'Unknown';
  }

  /**
   * Get Botpress bot ID for bot type
   */
  getBotpressBotId() {
    return this.botpressBotId || PennyBotTypeBotpressId[this.botType];
  }

  /**
   * Check if bot is active and enabled
   */
  isFullyActive() {
    return this.isActive === true && this.isEnabled === true;
  }

  /**
   * Create from API response
   */
  static fromApiResponse(data) {
    return new PennyBotDto(data);
  }
}

/**
 * Penny Bot Request DTO
 * Corresponds to: com.chatbot.shared.penny.dto.PennyBotRequest
 */
export class PennyBotRequest {
  constructor(data = {}) {
    this.botName = data.botName;
    this.botType = data.botType;
    this.tenantKey = data.tenantKey;
    this.ownerId = data.ownerId;
    this.botpressBotId = data.botpressBotId;
    this.description = data.description;
  }

  /**
   * Validate request data
   */
  validate() {
    const errors = [];

    if (!this.botName || this.botName.trim().length === 0) {
      errors.push('Bot name is required');
    }

    if (!this.botType || !Object.values(PennyBotType).includes(this.botType)) {
      errors.push('Valid bot type is required');
    }

    if (!this.tenantKey) {
      errors.push('Tenant key is required');
    }

    if (!this.ownerId || this.ownerId.trim().length === 0) {
      errors.push('Owner ID is required');
    }

    return {
      isValid: errors.length === 0,
      errors
    };
  }

  /**
   * Create for API request
   */
  toApiRequest() {
    return {
      botName: this.botName,
      botType: this.botType,
      tenantKey: this.tenantKey,
      ownerId: this.ownerId,
      botpressBotId: this.botpressBotId || PennyBotTypeBotpressId[this.botType],
      description: this.description
    };
  }
}

/**
 * Penny Bot Response DTO
 * Corresponds to: com.chatbot.shared.penny.dto.PennyBotResponse
 */
export class PennyBotResponse {
  constructor(data = {}) {
    this.id = data.id || data.botId; // Handle both 'id' and 'botId' from API
    this.botName = data.botName;
    this.botType = data.botType;
    this.tenantKey = data.tenantKey;
    this.ownerId = data.ownerId;
    this.botpressBotId = data.botpressBotId;
    this.description = data.description;
    this.isActive = data.isActive;
    this.isEnabled = data.isEnabled;
    this.createdAt = data.createdAt;
    this.updatedAt = data.updatedAt;
  }

  /**
   * Create from API response
   */
  static fromApiResponse(data) {
    return new PennyBotResponse(data);
  }
}

/**
 * Middleware Request DTO
 * Corresponds to: com.chatbot.shared.penny.dto.request.MiddlewareRequest
 */
export class MiddlewareRequest {
  constructor(data = {}) {
    this.requestId = data.requestId;
    this.userId = data.userId;
    this.platform = data.platform;
    this.message = data.message;
    this.connectionId = data.connectionId;
    this.botId = data.botId;
    this.tenantKey = data.tenantKey;
    this.ownerId = data.ownerId;
    this.metadata = data.metadata || {};
    this.timestamp = data.timestamp;
    this.messageType = data.messageType;
    this.language = data.language;
    this.sessionId = data.sessionId;
    this.pageId = data.pageId;
    this.conversationId = data.conversationId;
    this.priority = data.priority;
    this.isRetry = data.isRetry;
    this.retryCount = data.retryCount;
    this.userAgent = data.userAgent;
    this.ipAddress = data.ipAddress;
    this.location = data.location;
    this.deviceType = data.deviceType;
    this.context = data.context || {};
  }

  /**
   * Get metadata value by key
   */
  getMetadata(key, type = String) {
    if (!this.metadata || !this.metadata.hasOwnProperty(key)) {
      return null;
    }
    const value = this.metadata[key];
    return typeof value === type || value === null ? value : null;
  }

  /**
   * Add metadata
   */
  addMetadata(key, value) {
    if (!this.metadata) {
      this.metadata = {};
    }
    this.metadata[key] = value;
  }

  /**
   * Check if request has metadata key
   */
  hasMetadata(key) {
    return this.metadata && this.metadata.hasOwnProperty(key);
  }

  /**
   * Get context value by key
   */
  getContext(key, type = String) {
    if (!this.context || !this.context.hasOwnProperty(key)) {
      return null;
    }
    const value = this.context[key];
    return typeof value === type || value === null ? value : null;
  }

  /**
   * Add context information
   */
  addContext(key, value) {
    if (!this.context) {
      this.context = {};
    }
    this.context[key] = value;
  }

  /**
   * Check if this is a high priority request
   */
  isHighPriority() {
    return this.priority === 'high' || this.priority === 'urgent';
  }

  /**
   * Check if this is a text message
   */
  isTextMessage() {
    return this.messageType === 'text' || this.messageType == null;
  }

  /**
   * Check if this is a Vietnamese message
   */
  isVietnamese() {
    return this.language === 'vi' || (this.language == null && this.isVietnameseText(this.message));
  }

  /**
   * Simple Vietnamese text detection
   */
  isVietnameseText(text) {
    if (!text || text.trim().length === 0) {
      return false;
    }
    // Check for common Vietnamese characters
    const vietnameseRegex = /[àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđĐÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴÈÉẸẺẼÊỀẾỆỂỄÌÍỊỈĨÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠÙÚỤỦŨƯỪỨỰỬỮỲÝỴỶỸ]/;
    return vietnameseRegex.test(text);
  }

  /**
   * Get request age in milliseconds
   */
  getAgeInMillis() {
    if (!this.timestamp) {
      return 0;
    }
    const timestamp = new Date(this.timestamp).getTime();
    return Date.now() - timestamp;
  }

  /**
   * Check if request is expired
   */
  isExpired(maxAgeMillis) {
    return this.getAgeInMillis() > maxAgeMillis;
  }

  /**
   * Create for API request
   */
  toApiRequest() {
    return {
      requestId: this.requestId,
      userId: this.userId,
      platform: this.platform,
      message: this.message,
      connectionId: this.connectionId,
      botId: this.botId,
      tenantId: this.tenantId,
      ownerId: this.ownerId,
      metadata: this.metadata,
      timestamp: this.timestamp,
      messageType: this.messageType,
      language: this.language,
      sessionId: this.sessionId,
      pageId: this.pageId,
      conversationId: this.conversationId,
      priority: this.priority,
      isRetry: this.isRetry,
      retryCount: this.retryCount,
      userAgent: this.userAgent,
      ipAddress: this.ipAddress,
      location: this.location,
      deviceType: this.deviceType,
      context: this.context
    };
  }
}

/**
 * Middleware Response DTO
 * Corresponds to: com.chatbot.shared.penny.dto.response.MiddlewareResponse
 */
export class MiddlewareResponse {
  constructor(data = {}) {
    this.requestId = data.requestId;
    this.response = data.response;
    this.providerUsed = data.providerUsed;
    this.intentAnalysis = data.intentAnalysis;
    this.processingMetrics = data.processingMetrics;
    this.timestamp = data.timestamp;
    this.status = data.status;
    this.errorMessage = data.errorMessage;
    this.errorCode = data.errorCode;
    this.shouldSendResponse = data.shouldSendResponse;
    this.responseType = data.responseType;
    this.metadata = data.metadata || {};
    this.actions = data.actions || [];
    this.quickReplies = data.quickReplies || [];
    this.attachments = data.attachments || [];
    this.suggestions = data.suggestions || [];
    this.confidence = data.confidence;
    this.needsHumanIntervention = data.needsHumanIntervention;
    this.escalationReason = data.escalationReason;
    this.sessionUpdates = data.sessionUpdates || {};
    this.analyticsEvents = data.analyticsEvents || [];
  }

  /**
   * Check if response is successful
   */
  isSuccess() {
    return this.status === 'success' && !this.errorMessage;
  }

  /**
   * Check if response has error
   */
  hasError() {
    return !!(this.errorMessage || this.errorCode);
  }

  /**
   * Check if response needs escalation
   */
  needsEscalation() {
    return this.needsHumanIntervention === true;
  }

  /**
   * Get metadata value by key
   */
  getMetadata(key, type = String) {
    if (!this.metadata || !this.metadata.hasOwnProperty(key)) {
      return null;
    }
    const value = this.metadata[key];
    return typeof value === type || value === null ? value : null;
  }

  /**
   * Add metadata
   */
  addMetadata(key, value) {
    if (!this.metadata) {
      this.metadata = {};
    }
    this.metadata[key] = value;
  }

  /**
   * Add quick reply
   */
  addQuickReply(title, payload, imageUrl = null, metadata = {}) {
    if (!this.quickReplies) {
      this.quickReplies = [];
    }
    this.quickReplies.push({
      title,
      payload,
      imageUrl,
      metadata
    });
  }

  /**
   * Add attachment
   */
  addAttachment(type, url, filename = null, size = null, mimeType = null) {
    if (!this.attachments) {
      this.attachments = [];
    }
    this.attachments.push({
      type,
      url,
      filename,
      size,
      mimeType
    });
  }

  /**
   * Add action
   */
  addAction(type, data, description = null) {
    if (!this.actions) {
      this.actions = [];
    }
    this.actions.push({
      type,
      data,
      description
    });
  }

  /**
   * Add analytics event
   */
  addAnalyticsEvent(eventType, data) {
    if (!this.analyticsEvents) {
      this.analyticsEvents = [];
    }
    this.analyticsEvents.push({
      eventType,
      data,
      timestamp: new Date().toISOString()
    });
  }

  /**
   * Convert to JSON string
   */
  toJson() {
    try {
      return JSON.stringify(this);
    } catch (e) {
      return '{"error":"Failed to serialize response"}';
    }
  }

  /**
   * Create error response
   */
  static error(requestId, errorMessage, errorCode) {
    return new MiddlewareResponse({
      requestId,
      status: 'error',
      errorMessage,
      errorCode,
      timestamp: new Date().toISOString(),
      shouldSendResponse: true
    });
  }

  /**
   * Create success response
   */
  static success(requestId, response, providerUsed) {
    return new MiddlewareResponse({
      requestId,
      response,
      providerUsed,
      status: 'success',
      timestamp: new Date().toISOString(),
      shouldSendResponse: true
    });
  }

  /**
   * Create from API response
   */
  static fromApiResponse(data) {
    return new MiddlewareResponse(data);
  }
}

/**
 * Rule Type Enum
 */
export const RuleType = {
  RESPONSE: 'RESPONSE',
  REDIRECT: 'REDIRECT', 
  WEBHOOK: 'WEBHOOK',
  SCRIPT: 'SCRIPT',
  AI: 'AI',
  KEYWORD: 'KEYWORD',
  INTENT: 'INTENT'
};

/**
 * Rule Trigger Type Enum
 */
export const RuleTriggerType = {
  INTENT: 'INTENT',
  KEYWORD: 'KEYWORD',
  REGEX: 'REGEX',
  CONDITION: 'CONDITION',
  ALWAYS: 'ALWAYS'
};

/**
 * Rule Request DTO
 */
export class RuleRequest {
  constructor(data = {}) {
    this.ruleName = data.ruleName;
    this.ruleType = data.ruleType;
    this.triggerType = data.triggerType;
    this.triggerValue = data.triggerValue;
    this.condition = data.condition;
    this.action = data.action;
    this.botId = data.botId;
    this.description = data.description;
    this.priority = data.priority || 50;
    this.config = data.config || {};
  }

  validate() {
    const errors = [];
    if (!this.ruleName || this.ruleName.trim().length === 0) {
      errors.push('Rule name is required');
    }
    if (!this.ruleType || !Object.values(RuleType).includes(this.ruleType)) {
      errors.push('Valid rule type is required');
    }
    if (!this.triggerType || !Object.values(RuleTriggerType).includes(this.triggerType)) {
      errors.push('Valid trigger type is required');
    }
    if (!this.botId) {
      errors.push('Bot ID is required');
    }
    if (!this.action || this.action.trim().length === 0) {
      errors.push('Rule action is required');
    }
    if (this.triggerType !== RuleTriggerType.ALWAYS && !this.triggerValue) {
      errors.push('Trigger value is required for this trigger type');
    }
    return {
      isValid: errors.length === 0,
      errors
    };
  }

  toApiRequest() {
    return {
      ruleName: this.ruleName,
      ruleType: this.ruleType,
      triggerType: this.triggerType,
      triggerValue: this.triggerValue,
      condition: this.condition,
      action: this.action,
      botId: this.botId,
      description: this.description,
      priority: this.priority,
      config: this.config
    };
  }
}

/**
 * Rule DTO
 */
export class RuleDto {
  constructor(data = {}) {
    this.id = data.id;
    this.ruleName = data.ruleName;
    this.ruleType = data.ruleType;
    this.triggerType = data.triggerType;
    this.triggerValue = data.triggerValue;
    this.condition = data.condition;
    this.action = data.action;
    this.botId = data.botId;
    this.description = data.description;
    this.priority = data.priority;
    this.isActive = data.isActive;
    this.createdAt = data.createdAt;
    this.updatedAt = data.updatedAt;
  }

  static fromApiResponse(data) {
    return new RuleDto(data);
  }
}

/**
 * Connection Request DTO
 */
export class ConnectionRequest {
  constructor(data = {}) {
    this.connectionName = data.connectionName;
    this.connectionType = data.connectionType;
    this.botId = data.botId;
    this.config = data.config || {};
  }

  validate() {
    const errors = [];
    if (!this.connectionName || this.connectionName.trim().length === 0) {
      errors.push('Connection name is required');
    }
    if (!this.connectionType || !Object.values(ConnectionType).includes(this.connectionType)) {
      errors.push('Valid connection type is required');
    }
    if (!this.botId) {
      errors.push('Bot ID is required');
    }
    return {
      isValid: errors.length === 0,
      errors
    };
  }

  toApiRequest() {
    return {
      connectionName: this.connectionName,
      connectionType: this.connectionType,
      botId: this.botId,
      config: this.config
    };
  }
}

/**
 * Connection DTO
 */
export class ConnectionDto {
  constructor(data = {}) {
    this.id = data.id;
    this.connectionName = data.connectionName;
    this.connectionType = data.connectionType;
    this.botId = data.botId;
    this.config = data.config;
    this.isActive = data.isActive;
    this.health = data.health;
    this.lastUsed = data.lastUsed;
    this.createdAt = data.createdAt;
    this.updatedAt = data.updatedAt;
  }

  static fromApiResponse(data) {
    return new ConnectionDto(data);
  }
}

/**
 * Processing Metrics DTO
 * Inner class of MiddlewareResponse
 */
export class ProcessingMetrics {
  constructor(data = {}) {
    this.providerType = data.providerType;
    this.selectionReason = data.selectionReason;
    this.confidence = data.confidence;
    this.processingTime = data.processingTime;
    this.retryCount = data.retryCount;
    this.additionalMetrics = data.additionalMetrics || {};
  }
}

/**
 * Response Action DTO
 * Inner class of MiddlewareResponse
 */
export class ResponseAction {
  constructor(data = {}) {
    this.type = data.type;
    this.data = data.data || {};
    this.description = data.description;
  }
}

/**
 * Quick Reply DTO
 * Inner class of MiddlewareResponse
 */
export class QuickReply {
  constructor(data = {}) {
    this.title = data.title;
    this.payload = data.payload;
    this.imageUrl = data.imageUrl;
    this.metadata = data.metadata || {};
  }
}

/**
 * Attachment DTO
 * Inner class of MiddlewareResponse
 */
export class Attachment {
  constructor(data = {}) {
    this.type = data.type;
    this.url = data.url;
    this.filename = data.filename;
    this.size = data.size;
    this.mimeType = data.mimeType;
  }
}

/**
 * Analytics Event DTO
 * Inner class of MiddlewareResponse
 */
export class AnalyticsEvent {
  constructor(data = {}) {
    this.eventType = data.eventType;
    this.data = data.data || {};
    this.timestamp = data.timestamp;
  }
}

/**
 * Intent Analysis Result DTO
 * Corresponds to: com.chatbot.shared.penny.routing.dto.IntentAnalysisResult
 */
export class IntentAnalysisResult {
  constructor(data = {}) {
    this.intent = data.intent;
    this.confidence = data.confidence;
    this.entities = data.entities || [];
    this.sentiment = data.sentiment;
    this.language = data.language;
    this.processingTime = data.processingTime;
    this.provider = data.provider;
    this.metadata = data.metadata || {};
  }

  /**
   * Check if confidence is above threshold
   */
  isConfidentAbove(threshold = 0.7) {
    return this.confidence >= threshold;
  }

  /**
   * Get entity by name
   */
  getEntity(name) {
    return this.entities.find(entity => entity.name === name);
  }

  /**
   * Get all entities by type
   */
  getEntitiesByType(type) {
    return this.entities.filter(entity => entity.type === type);
  }
}

/**
 * Provider Selection DTO
 * Corresponds to: com.chatbot.shared.penny.routing.dto.ProviderSelection
 */
export class ProviderSelection {
  constructor(data = {}) {
    this.providerType = data.providerType;
    this.provider = data.provider;
    this.selectionReason = data.selectionReason;
    this.confidence = data.confidence;
    this.alternatives = data.alternatives || [];
    this.metadata = data.metadata || {};
  }

  /**
   * Check if selection is confident
   */
  isConfident(threshold = 0.7) {
    return this.confidence >= threshold;
  }
}

/**
 * Conversation Context DTO
 * Corresponds to: com.chatbot.shared.penny.context.ConversationContext
 */
export class ConversationContext {
  constructor(data = {}) {
    this.conversationId = data.conversationId;
    this.userId = data.userId;
    this.platform = data.platform;
    this.sessionId = data.sessionId;
    this.tenantId = data.tenantId;
    this.messages = data.messages || [];
    this.variables = data.variables || {};
    this.metadata = data.metadata || {};
    this.createdAt = data.createdAt;
    this.updatedAt = data.updatedAt;
    this.expiresAt = data.expiresAt;
  }

  /**
   * Get context variable
   */
  getVariable(key, defaultValue = null) {
    return this.variables[key] || defaultValue;
  }

  /**
   * Set context variable
   */
  setVariable(key, value) {
    this.variables[key] = value;
  }

  /**
   * Get recent messages
   */
  getRecentMessages(count = 10) {
    return this.messages.slice(-count);
  }

  /**
   * Add message to context
   */
  addMessage(message) {
    if (!this.messages) {
      this.messages = [];
    }
    this.messages.push(message);
  }

  /**
   * Check if context is expired
   */
  isExpired() {
    if (!this.expiresAt) {
      return false;
    }
    return new Date() > new Date(this.expiresAt);
  }
}

// Connection Type Enum
export const ConnectionType = {
  FACEBOOK: 'FACEBOOK',
  ZALO: 'ZALO',
  WEBSITE: 'WEBSITE',
  WEBHOOK: 'WEBHOOK',
  API: 'API',
  DATABASE: 'DATABASE'
};

// Connection Status Enum
export const ConnectionStatus = {
  ACTIVE: 'ACTIVE',
  INACTIVE: 'INACTIVE',
  PENDING: 'PENDING',
  ERROR: 'ERROR'
};

// Health Status Enum
export const HealthStatus = {
  HEALTHY: 'HEALTHY',
  WARNING: 'WARNING',
  ERROR: 'ERROR',
  UNKNOWN: 'UNKNOWN'
};

// Connection Type Display Names
export const ConnectionTypeDisplay = {
  [ConnectionType.FACEBOOK]: 'Facebook Messenger',
  [ConnectionType.ZALO]: 'Zalo',
  [ConnectionType.WEBSITE]: 'Website Chat',
  [ConnectionType.WEBHOOK]: 'Webhook',
  [ConnectionType.API]: 'REST API',
  [ConnectionType.DATABASE]: 'Database'
};

// Connection Status Display Names
export const ConnectionStatusDisplay = {
  [ConnectionStatus.ACTIVE]: 'Active',
  [ConnectionStatus.INACTIVE]: 'Inactive',
  [ConnectionStatus.PENDING]: 'Pending',
  [ConnectionStatus.ERROR]: 'Error'
};

// Health Status Display Names
export const HealthStatusDisplay = {
  [HealthStatus.HEALTHY]: 'Healthy',
  [HealthStatus.WARNING]: 'Warning',
  [HealthStatus.ERROR]: 'Error',
  [HealthStatus.UNKNOWN]: 'Unknown'
};

/**
 * Facebook Connection DTO
 * Corresponds to: com.chatbot.spokes.facebook.connection.dto.FacebookConnectionResponse
 */
export class FacebookConnectionDto {
  constructor(data = {}) {
    this.id = data.id;
    this.botId = data.botId;
    this.botName = data.botName;
    this.pageId = data.pageId;
    this.fanpageUrl = data.fanpageUrl;
    this.pageAccessToken = data.pageAccessToken;
    this.isEnabled = data.isEnabled;
    this.isActive = data.isActive;
    this.createdAt = data.createdAt;
    this.updatedAt = data.updatedAt;
  }

  /**
   * Check if connection is active
   */
  isActive() {
    return this.isActive === true && this.isEnabled === true;
  }

  /**
   * Get connection type
   */
  getConnectionType() {
    return ConnectionType.FACEBOOK;
  }

  /**
   * Get display name for connection type
   */
  getConnectionTypeDisplay() {
    return ConnectionTypeDisplay[ConnectionType.FACEBOOK];
  }

  /**
   * Create from API response
   */
  static fromApiResponse(data) {
    return new FacebookConnectionDto(data);
  }
}

/**
 * Create Facebook Connection Request DTO
 * Corresponds to: com.chatbot.spokes.facebook.connection.dto.CreateFacebookConnectionRequest
 */
export class CreateFacebookConnectionRequest {
  constructor(data = {}) {
    this.botId = data.botId;
    this.botName = data.botName;
    this.pageId = data.pageId;
    this.fanpageUrl = data.fanpageUrl;
    this.pageAccessToken = data.pageAccessToken;
    this.isEnabled = data.isEnabled !== undefined ? data.isEnabled : true;
    this.chatbotProvider = data.chatbotProvider || 'BOTPRESS';
  }

  /**
   * Validate request data
   */
  validate() {
    const errors = [];

    if (!this.botId) {
      errors.push('Bot ID is required');
    }

    if (!this.pageId) {
      errors.push('Facebook Page ID is required');
    }

    if (!this.pageAccessToken) {
      errors.push('Page Access Token is required');
    }

    return {
      isValid: errors.length === 0,
      errors
    };
  }

  /**
   * Create for API request
   */
  toApiRequest() {
    return {
      botId: this.botId,
      botName: this.botName,
      pageId: this.pageId,
      fanpageUrl: this.fanpageUrl,
      pageAccessToken: this.pageAccessToken,
      isEnabled: this.isEnabled,
      chatbotProvider: this.chatbotProvider
    };
  }
}

/**
 * Connection Test Result DTO
 */
export class ConnectionTestResult {
  constructor(data = {}) {
    this.success = data.success;
    this.message = data.message;
    this.error = data.error;
    this.responseTime = data.responseTime;
    this.testData = data.testData || {};
    this.timestamp = data.timestamp;
  }

  /**
   * Check if test was successful
   */
  isSuccessful() {
    return this.success === true;
  }

  /**
   * Create from API response
   */
  static fromApiResponse(data) {
    return new ConnectionTestResult(data);
  }
}

/**
 * Connection Health Check Result DTO
 */
export class ConnectionHealthCheckResult {
  constructor(data = {}) {
    this.connectionId = data.connectionId;
    this.status = data.status;
    this.message = data.message;
    this.responseTime = data.responseTime;
    this.lastCheck = data.lastCheck;
    this.metrics = data.metrics || {};
  }

  /**
   * Check if connection is healthy
   */
  isHealthy() {
    return this.status === HealthStatus.HEALTHY;
  }

  /**
   * Create from API response
   */
  static fromApiResponse(data) {
    return new ConnectionHealthCheckResult(data);
  }
}

// Export all types for easy importing
export default {
  // Enums and constants
  PennyBotType,
  PennyBotTypeDisplay,
  PennyBotTypeBotpressId,
  ConnectionType,
  ConnectionStatus,
  HealthStatus,
  ConnectionTypeDisplay,
  ConnectionStatusDisplay,
  HealthStatusDisplay,

  // Main DTO classes
  PennyBotDto,
  PennyBotRequest,
  PennyBotResponse,
  MiddlewareRequest,
  MiddlewareResponse,

  // Connection DTO classes
  FacebookConnectionDto,
  CreateFacebookConnectionRequest,
  ConnectionTestResult,
  ConnectionHealthCheckResult,

  // Supporting DTO classes
  ProcessingMetrics,
  ResponseAction,
  QuickReply,
  Attachment,
  AnalyticsEvent,
  IntentAnalysisResult,
  ProviderSelection,
  ConversationContext,

  // Rule DTO classes
  RuleType,
  RuleTriggerType,
  RuleRequest,
  RuleDto,

  // Connection DTO classes
  ConnectionRequest,
  ConnectionDto
};
