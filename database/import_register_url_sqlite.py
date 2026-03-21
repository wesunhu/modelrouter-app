#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Applies register_url migration to the SQLite ModelRouter database.

Input: SQL file ``migrate_add_register_url_sqlite.sql``; discovers ``data/modelrouter.db``.
Output: executes DDL/DML; prints status to stdout.

version: 1.0.1
since: 2026-03-21
author: wesun hu

Usage: ``python database/import_register_url_sqlite.py`` (from project root), or run from ``database/``.
"""
import os
import sqlite3
import sys

# 查找 data/modelrouter.db
def find_db():
    script_dir = os.path.dirname(os.path.abspath(__file__))
    candidates = [
        os.path.join(script_dir, "..", "data", "modelrouter.db"),
        os.path.join(os.getcwd(), "data", "modelrouter.db"),
        "data/modelrouter.db",
    ]
    for p in candidates:
        path = os.path.abspath(p)
        if os.path.isfile(path):
            return path
    return None

def main():
    db_path = find_db()
    if not db_path:
        print("ERROR: data/modelrouter.db not found. Run ModelRouter once to create it.")
        sys.exit(1)

    sql_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "migrate_add_register_url_sqlite.sql")
    if not os.path.isfile(sql_path):
        print(f"ERROR: {sql_path} not found")
        sys.exit(1)

    print(f"Using database: {db_path}")
    conn = sqlite3.connect(db_path)
    conn.execute("PRAGMA foreign_keys=ON")

    try:
        # 1. 添加列（忽略已存在）
        try:
            conn.execute("ALTER TABLE providers ADD COLUMN register_url VARCHAR(512)")
            conn.commit()
            print("Added register_url column.")
        except sqlite3.OperationalError as e:
            if "duplicate column" in str(e).lower():
                print("Column register_url already exists, skip.")
            else:
                raise

        # 2. 插入/更新平台数据
        providers = [
            ("OpenAI", "https://api.openai.com/v1", "openai", "https://platform.openai.com/"),
            ("阿里云百炼", "https://dashscope.aliyuncs.com/compatible-mode/v1", "openai", "https://bailian.console.aliyun.com"),
            ("七牛云AI", "https://ai.qiniuapi.com/v1", "openai", "https://ai.qiniu.com/free"),
            ("硅基流动 (SiliconFlow)", "https://api.siliconflow.cn/v1", "openai", "https://cloud.siliconflow.cn/i/R4OZl1HU"),
            ("智谱AI (GLM)", "https://open.bigmodel.cn/api/paas/v4", "glm", "https://open.bigmodel.cn/"),
            ("百度智能云千帆", "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/", "baidu", "https://console.bce.baidu.com/qianfan/"),
            ("腾讯云混元", "https://hunyuan.tencentcloudapi.com", "tencent", "https://cloud.tencent.com/product/hunyuan"),
            ("讯飞星火", "https://spark-api.xf-yun.com/v1.1/chat", "spark", "https://xinghuo.xfyun.cn/"),
            ("DeepSeek 官方", "https://api.deepseek.com/v1", "openai", "https://platform.deepseek.com/"),
            ("火山引擎方舟", "https://ark.cn-beijing.volces.com/api/v3", "volcengine", "https://ark.cn-beijing.volces.com/"),
            ("无问芯穹", "https://api.inspire.ai/v1", "openai", "https://platform.inspire.ai/"),
            ("Google AI Studio (Gemini)", "https://generativelanguage.googleapis.com/v1beta", "google", "https://aistudio.google.com/"),
            ("OpenRouter", "https://openrouter.ai/api/v1", "openai", "https://openrouter.ai/"),
        ]
        for name, base_url, api_type, register_url in providers:
            conn.execute(
                """INSERT INTO providers (name, base_url, api_type, register_url)
                   VALUES (?, ?, ?, ?)
                   ON CONFLICT(name) DO UPDATE SET
                     base_url = excluded.base_url,
                     register_url = excluded.register_url,
                     api_type = excluded.api_type""",
                (name, base_url, api_type, register_url),
            )
        conn.commit()
        print("Platform data updated successfully.")
    except Exception as e:
        conn.rollback()
        print(f"ERROR: {e}")
        sys.exit(1)
    finally:
        conn.close()

if __name__ == "__main__":
    main()
