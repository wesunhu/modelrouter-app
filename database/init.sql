CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS providers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    base_url VARCHAR(512) NOT NULL,
    api_type VARCHAR(100) DEFAULT 'openai',
    auth_header BOOLEAN DEFAULT true,
    api_key VARCHAR(512),
    register_url VARCHAR(512),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS models (
    id BIGSERIAL PRIMARY KEY,
    provider_id BIGINT NOT NULL REFERENCES providers(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    model_id VARCHAR(255) NOT NULL,
    model_type VARCHAR(50) DEFAULT 'text',
    context_window INTEGER DEFAULT 4096,
    max_tokens INTEGER DEFAULT 2048,
    cost_input DOUBLE PRECISION DEFAULT 0,
    cost_output DOUBLE PRECISION DEFAULT 0,
    token_cost DOUBLE PRECISION DEFAULT NULL,
    status VARCHAR(50) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS api_keys (
    id BIGSERIAL PRIMARY KEY,
    key VARCHAR(255) NOT NULL,
    platform VARCHAR(255) NOT NULL,
    auth_type VARCHAR(50) DEFAULT 'api_key',
    api_endpoint VARCHAR(512),
    secret VARCHAR(512),
    quota INTEGER,
    status VARCHAR(50) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS api_key_models (
    api_key_id BIGINT REFERENCES api_keys(id) ON DELETE CASCADE,
    model_id BIGINT REFERENCES models(id) ON DELETE CASCADE,
    PRIMARY KEY (api_key_id, model_id)
);

CREATE TABLE IF NOT EXISTS routes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    api_key VARCHAR(255),
    primary_model_id BIGINT REFERENCES models(id),
    model_type VARCHAR(50) DEFAULT 'text',
    timeout INTEGER DEFAULT 60000,
    strategy VARCHAR(50) DEFAULT 'primary-first',
    token_selling_price DOUBLE PRECISION DEFAULT NULL,
    status VARCHAR(50) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS route_backup_models (
    route_id BIGINT REFERENCES routes(id) ON DELETE CASCADE,
    model_id BIGINT REFERENCES models(id) ON DELETE CASCADE,
    priority INTEGER DEFAULT 1,
    PRIMARY KEY (route_id, model_id)
);

CREATE TABLE IF NOT EXISTS usage_logs (
    id BIGSERIAL PRIMARY KEY,
    route_id BIGINT,
    route_name VARCHAR(255),
    model_id BIGINT,
    model_name VARCHAR(255),
    platform VARCHAR(255),
    api_key_id BIGINT,
    prompt_tokens INTEGER DEFAULT 0,
    completion_tokens INTEGER DEFAULT 0,
    total_tokens INTEGER DEFAULT 0,
    cost DOUBLE PRECISION DEFAULT 0,
    cost_per_token DOUBLE PRECISION DEFAULT NULL,
    selling_price_per_token DOUBLE PRECISION DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_usage_logs_route ON usage_logs(route_id);
CREATE INDEX IF NOT EXISTS idx_usage_logs_model ON usage_logs(model_id);
CREATE INDEX IF NOT EXISTS idx_usage_logs_created ON usage_logs(created_at);

-- 初始数据：平台列表（含注册地址）
INSERT INTO providers (name, base_url, api_type, register_url) VALUES
  ('硅基流动 (SiliconFlow)', 'https://api.siliconflow.cn/v1', 'openai', 'https://cloud.siliconflow.cn/i/R4OZl1HU'),
  ('OpenAI', 'https://api.openai.com/v1', 'openai', 'https://www.openai.com/'),
  ('阿里云百炼', 'https://dashscope.aliyuncs.com/compatible-mode/v1', 'openai', 'https://bailian.console.aliyun.com'),
  ('七牛云AI', 'https://ai.qiniuapi.com/v1', 'openai', 'https://www.qiniu.com'),
  ('智谱AI (GLM)', 'https://open.bigmodel.cn/api/paas/v4', 'glm', 'https://open.bigmodel.cn/'),
  ('百度智能云千帆', 'https://aip.baidubce.com/rpc/2.0/ai_custom/v1', 'baidu', 'https://console.bce.baidu.com/qianfan/'),
  ('腾讯云混元', 'https://hunyuan.tencentcloudapi.com', 'tencent', 'https://cloud.tencent.com/product/hunyuan'),
  ('讯飞星火', 'https://spark-api.xf-yun.com/v1.1/chat', 'spark', 'https://xinghuo.xfyun.cn/'),
  ('DeepSeek 官方', 'https://api.deepseek.com/v1', 'openai', 'https://platform.deepseek.com/'),
  ('火山引擎方舟', 'https://ark.cn-beijing.volces.com/api/v3', 'volcengine', 'https://ark.cn-beijing.volces.com/'),
  ('无问芯穹', 'https://api.inspire.ai/v1', 'openai', 'https://platform.inspire.ai/'),
  ('Google AI Studio (Gemini)', 'https://generativelanguage.googleapis.com/v1beta', 'google', 'https://aistudio.google.com/'),
  ('OpenRouter', 'https://openrouter.ai/api/v1', 'openai', 'https://openrouter.ai/')
ON CONFLICT (name) DO UPDATE SET base_url = EXCLUDED.base_url, register_url = EXCLUDED.register_url, api_type = EXCLUDED.api_type;
