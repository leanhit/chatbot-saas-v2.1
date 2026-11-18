-- V9: Training domain tables (training materials, categories, agent skills,
-- certifications, onboarding tasks/progress, training progress)
-- Generated from JPA entities in com.chatbot.core.training.model

CREATE TABLE IF NOT EXISTS training_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT
);

CREATE TABLE IF NOT EXISTS training_materials (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    content TEXT,
    type VARCHAR(50) NOT NULL,
    category_id BIGINT,
    status VARCHAR(50) NOT NULL,
    estimated_duration_minutes INTEGER,
    required_skills JSONB,
    difficulty_level VARCHAR(50),
    is_mandatory BOOLEAN,
    sequence_order INTEGER,
    pass_score_threshold INTEGER,
    quiz_questions JSONB,
    external_url VARCHAR(255),
    file_url VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS agent_skills (
    id BIGSERIAL PRIMARY KEY,
    agent_id BIGINT NOT NULL,
    skill_category VARCHAR(50) NOT NULL,
    score INTEGER NOT NULL,
    proficiency_level VARCHAR(50),
    last_assessed_at VARCHAR(255),
    assessment_method VARCHAR(50),
    training_id BIGINT,
    previous_score INTEGER,
    assessment_count INTEGER,
    notes TEXT
);

CREATE TABLE IF NOT EXISTS agent_training_progress (
    id BIGSERIAL PRIMARY KEY,
    agent_id BIGINT NOT NULL,
    training_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    progress INTEGER NOT NULL,
    quiz_score INTEGER,
    started_at VARCHAR(255),
    completed_at VARCHAR(255),
    time_spent_seconds INTEGER,
    notes TEXT
);

CREATE TABLE IF NOT EXISTS certification_levels (
    id BIGSERIAL PRIMARY KEY,
    level_name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    required_training_ids VARCHAR(255),
    required_skill_scores VARCHAR(255),
    required_stats VARCHAR(255),
    badge_icon VARCHAR(255),
    badge_color VARCHAR(255),
    display_order INTEGER,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    validity_days INTEGER
);

CREATE TABLE IF NOT EXISTS agent_certifications (
    id BIGSERIAL PRIMARY KEY,
    agent_id BIGINT NOT NULL,
    certification_level_id BIGINT NOT NULL,
    status VARCHAR(50),
    issued_at VARCHAR(255),
    expires_at VARCHAR(255),
    certificate_number VARCHAR(255),
    issued_by VARCHAR(255),
    revoked_at VARCHAR(255),
    revocation_reason TEXT,
    revoked_by VARCHAR(255),
    notes TEXT
);

CREATE TABLE IF NOT EXISTS agent_onboarding_tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(50),
    training_id BIGINT,
    required BOOLEAN NOT NULL,
    display_order INTEGER,
    status VARCHAR(50),
    estimated_duration_minutes INTEGER,
    instructions TEXT,
    external_url VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS agent_onboarding_progress (
    id BIGSERIAL PRIMARY KEY,
    agent_id BIGINT NOT NULL,
    task_id BIGINT NOT NULL,
    status VARCHAR(50),
    started_at VARCHAR(255),
    completed_at VARCHAR(255),
    time_spent_seconds INTEGER,
    notes TEXT,
    supervisor_approval VARCHAR(50),
    supervisor_id BIGINT,
    supervisor_comments TEXT,
    approved_at VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_training_materials_category ON training_materials(category_id);
CREATE INDEX IF NOT EXISTS idx_agent_skills_agent ON agent_skills(agent_id);
CREATE INDEX IF NOT EXISTS idx_agent_training_progress_agent ON agent_training_progress(agent_id);
CREATE INDEX IF NOT EXISTS idx_agent_certifications_agent ON agent_certifications(agent_id);
CREATE INDEX IF NOT EXISTS idx_agent_onboarding_progress_agent ON agent_onboarding_progress(agent_id);
