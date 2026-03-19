-- 模型增加 token 费用（每 token 成本）
ALTER TABLE models ADD COLUMN IF NOT EXISTS token_cost DOUBLE PRECISION DEFAULT NULL;

-- 路由增加 tokens 销售单价
ALTER TABLE routes ADD COLUMN IF NOT EXISTS token_selling_price DOUBLE PRECISION DEFAULT NULL;

-- 使用日志增加成本单价、销售单价快照
ALTER TABLE usage_logs ADD COLUMN IF NOT EXISTS cost_per_token DOUBLE PRECISION DEFAULT NULL;
ALTER TABLE usage_logs ADD COLUMN IF NOT EXISTS selling_price_per_token DOUBLE PRECISION DEFAULT NULL;
