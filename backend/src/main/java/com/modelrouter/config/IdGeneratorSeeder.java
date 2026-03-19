package com.modelrouter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * SQLite 模式下，启动时同步 id_generator 与各表最大 ID，避免主键冲突。
 * 解决从 IDENTITY 迁移或 id_generator 未初始化时的 PRIMARY KEY constraint failed。
 */
@Component
@Profile("sqlite")
@Order(0)
public class IdGeneratorSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(IdGeneratorSeeder.class);

    private static final String[][] ENTITY_TABLES = {
            {"providers", "providers"},
            {"models", "models"},
            {"api_keys", "api_keys"},
            {"routes", "routes"},
            {"usage_logs", "usage_logs"}
    };

    private final JdbcTemplate jdbcTemplate;

    public IdGeneratorSeeder(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            for (String[] pair : ENTITY_TABLES) {
                String genKey = pair[0];
                String tableName = pair[1];
                syncGenerator(genKey, tableName);
            }
        } catch (Exception e) {
            log.warn("同步 id_generator 失败（表可能尚未创建）: {}", e.getMessage());
        }
    }

    private void syncGenerator(String genKey, String tableName) {
        try {
            Long nextVal = jdbcTemplate.queryForObject(
                    "SELECT COALESCE(MAX(id), 0) + 1 FROM " + tableName,
                    Long.class);
            if (nextVal == null) nextVal = 1L;

            jdbcTemplate.update(
                    "INSERT OR REPLACE INTO id_generator (gen_key, gen_value) VALUES (?, ?)",
                    genKey, nextVal);
        } catch (Exception e) {
            log.debug("跳过 {} 表（可能不存在）: {}", tableName, e.getMessage());
        }
    }
}
