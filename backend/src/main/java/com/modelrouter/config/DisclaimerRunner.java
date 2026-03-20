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
            [警告] 实验性软件，严禁公网访问。使用风险自负。
            ModelRouter-App 为个人实验项目，无任何担保。详见 LEGAL.md
            ================================================================================
            """;

    @Override
    public void run(ApplicationArguments args) {
        log.warn(DISCLAIMER);
    }
}
