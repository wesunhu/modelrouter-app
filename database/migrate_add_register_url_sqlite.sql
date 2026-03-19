-- SQLite: 添加 register_url 列并填充平台数据
-- 用法: 见 import-migrate-sqlite.bat

PRAGMA foreign_keys = OFF;

-- 添加列（SQLite 3.35+ 支持 IF NOT EXISTS）
ALTER TABLE providers ADD COLUMN register_url VARCHAR(512);

PRAGMA foreign_keys = ON;
