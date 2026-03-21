/**
 * Prints legal disclaimer to console at startup.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

package com.modelrouter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 启动时输出免责声明提示
 */
@Component
@Order(Integer.MIN_VALUE)
public class DisclaimerRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DisclaimerRunner.class);

    private static final String DISCLAIMER = """
            ================================================================================
            [WARN] Experimental software. Do NOT expose to public internet. Use at your own risk.
            ModelRouter-App is a personal experiment with no warranty. See LEGAL.md
            ================================================================================
            """;

    @Override
    public void run(ApplicationArguments args) {
        log.warn(DISCLAIMER);
    }
}
