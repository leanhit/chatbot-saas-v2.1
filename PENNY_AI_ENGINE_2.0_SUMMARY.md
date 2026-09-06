# Penny AI Engine 2.0 - Implementation Summary

## Overview
Penny AI Engine 2.0 upgrade implements a comprehensive RAG (Retrieval-Augmented Generation) pipeline with multi-LLM provider support, document ingestion capabilities, and enhanced bot persona customization.

## Implementation Date
September 6, 2026

## Phase 1: Database & Dependencies

### Dependencies Added (build.gradle)
- Apache Tika 2.9.1 - Universal document parsing
- Apache PDFBox 2.0.29 - PDF text extraction
- Apache POI 5.2.3 - DOCX/XLSX parsing

### Database Migrations
- **V14__penny_bot_ai_config_v2.sql**: Extended `penny_bots` table with AI configuration columns
  - `provider_type` (VARCHAR) - LLM provider (OPENAI, CLAUDE, GEMINI, OLLAMA)
  - `model_name` (VARCHAR) - Model identifier
  - `temperature` (FLOAT) - Response creativity (0.0-1.0)
  - `persona_style` (VARCHAR) - Bot personality (PROFESSIONAL, FRIENDLY, ENTHUSIASTIC, HUMOROUS, FORMAL)
  - `custom_instructions` (TEXT) - Custom system prompt instructions
  - `greeting_message` (VARCHAR) - Default greeting
  - `fallback_message` (VARCHAR) - Fallback when no answer found

- **V15__penny_knowledge_documents.sql**: Created `penny_knowledge_documents` table
  - Stores uploaded documents (PDF, DOCX, XLSX)
  - Tracks processing status, chunk count, file metadata
  - Links to bot and tenant

- **V16__penny_knowledge_chunks.sql**: Created `penny_knowledge_chunks` table
  - Stores text chunks with pgvector embeddings
  - HNSW index for fast similarity search
  - 1536-dimensional vectors (OpenAI text-embedding-3-small)

### Entity Models
- **PennyKnowledgeDocument.java**: Document entity with metadata
- **PennyKnowledgeChunk.java**: Chunk entity with vector embedding
- **LlmProviderType.java**: Enum for LLM providers
- **BotPersonaStyle.java**: Enum for bot personalities

### Repositories
- **PennyKnowledgeDocumentRepository.java**: Document CRUD operations
- **PennyKnowledgeChunkRepository.java**: Chunk CRUD with vector similarity search
  - `findSimilarByVector()` - Cosine similarity search
  - `findSimilarByVectorWithThreshold()` - Threshold-filtered search

## Phase 2: RAG Pipeline Implementation

### Document Parsing Service
**File**: `DocumentParsingService.java`
- Parses PDF using Apache PDFBox
- Parses DOCX using Apache POI XWPF
- Parses XLSX using Apache POI XSSF
- Fallback to Apache Tika for universal parsing
- Extracts text per page/sheet
- Handles file type detection

### Text Chunking Service
**File**: `TextChunkerService.java`
- Chunks text into 500-800 token segments
- 100 token overlap for context preservation
- Preserves page numbers and sheet names
- Estimates token count per chunk
- Supports document-level chunking

### Knowledge Chunk Search Service
**File**: `KnowledgeChunkSearchService.java`
- Generates embeddings using OpenAI text-embedding-3-small
- Performs pgvector HNSW similarity search
- Filters by cosine similarity threshold (default 0.72)
- Formats retrieved chunks for LLM context
- Caches embeddings for performance

### Document Processing Service
**File**: `DocumentProcessingService.java`
- Orchestrates end-to-end document pipeline:
  1. Parse document
  2. Chunk text
  3. Generate embeddings
  4. Store chunks with vectors
- Handles errors gracefully
- Updates document status
- Tracks processing metrics

### API Controller
**File**: `KnowledgeDocumentController.java`
- `POST /api/v1/penny/knowledge-base/upload` - Upload document
- `GET /api/v1/penny/knowledge-base/documents` - List documents
- `GET /api/v1/penny/knowledge-base/documents/{id}` - Get document
- `DELETE /api/v1/penny/knowledge-base/documents/{id}` - Delete document
- `DELETE /api/v1/penny/knowledge-base/documents/bot/{botId}` - Delete all bot documents
- `POST /api/v1/penny/knowledge-base/documents/{id}/reprocess` - Re-process document

## Phase 3: Multi-LLM Architecture

### LLM Provider Interface
**File**: `LlmProvider.java`
- Standard interface for all LLM providers
- Methods: `generateResponse()`, `isAvailable()`, `getProviderType()`

### DTOs
- **LlmRequest.java**: Standardized request parameters
  - System prompt, user message, conversation history
  - Temperature, max tokens
- **LlmResponse.java**: Standardized response data
  - Text, model, tokens, finish reason, processing time

### Provider Adapters
**Files**:
- **OpenAiLlmProvider.java**: OpenAI GPT-4o, GPT-4o-mini
- **ClaudeLlmProvider.java**: Anthropic Claude 3.5 Sonnet, Haiku, Opus
- **GeminiLlmProvider.java**: Google Gemini 1.5 Pro, Flash
- **OllamaLlmProvider.java**: Local models (Llama 3, Mistral, CodeLlama)

### LLM Routing Service
**File**: `LlmRoutingService.java`
- Routes requests to configured provider
- Automatic failover on provider failure
- Circuit breaker pattern (Resilience4j)
- Tracks provider health metrics
- Configurable retry logic

### System Prompt Builder Service
**File**: `SystemPromptBuilderService.java`
- Builds dynamic system prompts integrating:
  - Bot persona style (Vietnamese descriptions)
  - Business name and description
  - Custom instructions
  - RAG context from knowledge base
  - Response rules and fallback message
- Generates persona-specific greetings
- Handles RAG enable/disable logic

## Phase 4: Frontend Implementation

### API Integration
**File**: `frontend/src/api/pennyApi.js`
Added methods:
- `uploadKnowledgeDocument()` - Upload with FormData
- `getKnowledgeDocuments()` - List documents
- `getKnowledgeDocument()` - Get single document
- `deleteKnowledgeDocument()` - Delete document
- `deleteKnowledgeDocumentsByBot()` - Delete all
- `reprocessKnowledgeDocument()` - Re-process

### Document Upload Component
**File**: `frontend/src/views/penny/knowledge-base/DocumentUpload.vue`
Features:
- Drag-and-drop file upload
- File type validation (PDF, DOCX, XLSX)
- File size validation (50MB max)
- Document name customization
- Upload progress tracking
- Document list with status
- Delete functionality
- Error handling and status messages

### AI Bot Configuration Component
**File**: `frontend/src/views/penny/bots/components/AiBotConfig.vue`
Features:
- LLM provider selection (OpenAI, Claude, Gemini, Ollama)
- Model selection per provider
- Temperature slider (0.0-1.0)
- Persona style selection (5 options with icons)
- Custom instructions textarea
- Greeting message input
- Fallback message textarea
- Save/Reset functionality
- Status feedback

### Translations
**File**: `frontend/src/locales/vi.json`
Added Vietnamese translations for:
- Document upload UI (all labels, messages, errors)
- AI configuration UI (all fields, personas, messages)

## Testing

### Unit Tests Created
**Files**:
- `DocumentParsingServiceTest.java` - 5 tests
  - Text parsing, empty files, file type detection
- `TextChunkerServiceTest.java` - 10 tests
  - Chunking, empty handling, page/sheet preservation
- `SystemPromptBuilderServiceTest.java` - 14 tests
  - Prompt building, RAG integration, persona handling

### Test Results
- All 29 tests passing
- Backend compilation successful
- No test failures or errors

## Configuration

### Environment Variables Required
- `OPENAI_API_KEY` - OpenAI API key
- `CLAUDE_API_KEY` - Anthropic API key (optional)
- `GEMINI_API_KEY` - Google API key (optional)
- `OLLAMA_BASE_URL` - Ollama endpoint (optional)

### Database Requirements
- PostgreSQL with pgvector extension
- HNSW index for vector similarity
- Flyway migrations applied

## Key Features

### RAG Pipeline
- Document ingestion: PDF, DOCX, XLSX
- Intelligent chunking with overlap
- Vector embeddings (1536 dimensions)
- Fast similarity search (HNSW)
- Threshold-based filtering

### Multi-LLM Support
- 4 providers: OpenAI, Claude, Gemini, Ollama
- Automatic failover
- Circuit breaker protection
- Health monitoring

### Bot Personalization
- 5 persona styles
- Custom instructions
- Dynamic greetings
- Fallback messages
- Vietnamese language support

### API Endpoints
- RESTful document management
- Multipart file upload
- Status tracking
- Re-processing capability

## Performance Considerations

### Caching
- Embedding caching in EmbeddingService
- Vector similarity search with HNSW index
- Connection pooling for LLM APIs

### Resilience
- Circuit breaker for LLM providers
- Automatic failover
- Retry logic
- Error handling

### Scalability
- pgvector HNSW for fast vector search
- Chunking for large documents
- Async processing pipeline

## Future Enhancements

### Potential Improvements
- Streaming responses for large documents
- Batch document upload
- Document versioning
- Advanced chunking strategies
- More LLM providers
- Real-time processing status
- Document preview
- Chunk-level editing

### Integration Points
- Connect to existing bot configuration UI
- Integrate with chat interface
- Add to bot creation flow
- Dashboard metrics for RAG usage

## Deployment Checklist

- [ ] Apply database migrations (V14, V15, V16)
- [ ] Configure API keys for LLM providers
- [ ] Verify pgvector extension installed
- [ ] Build and deploy backend
- [ ] Build and deploy frontend
- [ ] Test document upload flow
- [ ] Test AI configuration changes
- [ ] Verify RAG retrieval
- [ ] Test multi-LLM failover
- [ ] Monitor circuit breaker metrics

## Files Modified/Created

### Backend (18 files)
- build.gradle
- V14__penny_bot_ai_config_v2.sql
- V15__penny_knowledge_documents.sql
- V16__penny_knowledge_chunks.sql
- PennyBot.java
- PennyKnowledgeDocument.java
- PennyKnowledgeChunk.java
- LlmProviderType.java
- BotPersonaStyle.java
- PennyKnowledgeDocumentRepository.java
- PennyKnowledgeChunkRepository.java
- DocumentParsingService.java
- TextChunkerService.java
- KnowledgeChunkSearchService.java
- DocumentProcessingService.java
- KnowledgeDocumentController.java
- LlmProvider.java
- LlmRequest.java
- LlmResponse.java
- OpenAiLlmProvider.java
- ClaudeLlmProvider.java
- GeminiLlmProvider.java
- OllamaLlmProvider.java
- LlmRoutingService.java
- SystemPromptBuilderService.java
- DocumentParsingServiceTest.java
- TextChunkerServiceTest.java
- SystemPromptBuilderServiceTest.java

### Frontend (4 files)
- pennyApi.js
- DocumentUpload.vue
- AiBotConfig.vue
- vi.json

## Conclusion

Penny AI Engine 2.0 successfully implements a production-ready RAG pipeline with multi-LLM support, comprehensive document ingestion, and enhanced bot personalization. All backend tests pass, and the frontend components are ready for integration. The system is designed for scalability, resilience, and ease of use with Vietnamese language support.
