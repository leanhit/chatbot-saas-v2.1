-- Thêm cột chatbot_provider vào bảng facebook_connection
ALTER TABLE facebook_connection 
ADD COLUMN chatbot_provider VARCHAR(20) DEFAULT 'BOTPRESS' NOT NULL;

-- Tạo comment cho cột mới
COMMENT ON COLUMN facebook_connection.chatbot_provider IS 'Loại chatbot provider được sử dụng (BOTPRESS, RASA, DIALOGFLOW, CUSTOM)';
