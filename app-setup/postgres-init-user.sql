-- Enable pgvector extension for vector search
CREATE EXTENSION IF NOT EXISTS vector;

-- Grant all privileges on schema public
GRANT ALL PRIVILEGES ON SCHEMA public TO chatbot_user;

-- Grant all privileges on all tables
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO chatbot_user;

-- Grant all privileges on all sequences
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO chatbot_user;

-- Grant all privileges on all functions
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public TO chatbot_user;
