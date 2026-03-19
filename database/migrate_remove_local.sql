-- 升级迁移：移除本地模型相关数据与结构
-- 适用于已部署的数据库，新安装请直接使用 init.sql
-- 执行: psql -U postgres -d modelrouter -f migrate_remove_local.sql

DO $$
DECLARE
  local_provider_id BIGINT;
BEGIN
  SELECT id INTO local_provider_id FROM providers WHERE name = 'Local' LIMIT 1;
  IF local_provider_id IS NOT NULL THEN
    UPDATE routes SET primary_model_id = NULL WHERE primary_model_id IN (SELECT id FROM models WHERE provider_id = local_provider_id);
    DELETE FROM route_backup_models WHERE model_id IN (SELECT id FROM models WHERE provider_id = local_provider_id);
    DELETE FROM models WHERE provider_id = local_provider_id;
    DELETE FROM providers WHERE id = local_provider_id;
  END IF;
END $$;

ALTER TABLE models DROP COLUMN IF EXISTS is_local;
