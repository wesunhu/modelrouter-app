-- 为已有数据库添加 register_url 列并填充平台数据
ALTER TABLE providers ADD COLUMN IF NOT EXISTS register_url VARCHAR(512);

-- 更新/插入平台数据（含注册地址）
INSERT INTO providers (name, base_url, api_type, register_url) VALUES
  ('OpenAI', 'https://api.openai.com/v1', 'openai', 'https://platform.openai.com/'),
  ('阿里云百炼', 'https://dashscope.aliyuncs.com/compatible-mode/v1', 'openai', 'https://bailian.console.aliyun.com'),
  ('七牛云AI', 'https://ai.qiniuapi.com/v1', 'openai', 'https://ai.qiniu.com/free'),
  ('硅基流动 (SiliconFlow)', 'https://api.siliconflow.cn/v1', 'openai', 'https://cloud.siliconflow.cn/i/R4OZl1HU'),
  ('智谱AI (GLM)', 'https://open.bigmodel.cn/api/paas/v4', 'glm', 'https://open.bigmodel.cn/'),
  ('百度智能云千帆', 'https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/', 'baidu', 'https://console.bce.baidu.com/qianfan/'),
  ('腾讯云混元', 'https://hunyuan.tencentcloudapi.com', 'tencent', 'https://cloud.tencent.com/product/hunyuan'),
  ('讯飞星火', 'https://spark-api.xf-yun.com/v1.1/chat', 'spark', 'https://xinghuo.xfyun.cn/'),
  ('DeepSeek 官方', 'https://api.deepseek.com/v1', 'openai', 'https://platform.deepseek.com/'),
  ('火山引擎方舟', 'https://ark.cn-beijing.volces.com/api/v3', 'volcengine', 'https://ark.cn-beijing.volces.com/'),
  ('无问芯穹', 'https://api.inspire.ai/v1', 'openai', 'https://platform.inspire.ai/'),
  ('Google AI Studio (Gemini)', 'https://generativelanguage.googleapis.com/v1beta', 'google', 'https://aistudio.google.com/'),
  ('OpenRouter', 'https://openrouter.ai/api/v1', 'openai', 'https://openrouter.ai/')
ON CONFLICT (name) DO UPDATE SET 
  base_url = EXCLUDED.base_url, 
  register_url = EXCLUDED.register_url, 
  api_type = EXCLUDED.api_type;
