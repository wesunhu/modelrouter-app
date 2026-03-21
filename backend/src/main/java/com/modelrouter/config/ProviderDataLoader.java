/**
 * Loads or seeds provider data on startup.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

package com.modelrouter.config;

import com.modelrouter.entity.Provider;
import com.modelrouter.repository.ProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * SQLite 模式下初始化平台列表数据（仅在表为空时插入）
 */
@Component
@Profile("sqlite")
@Order(1)
public class ProviderDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ProviderDataLoader.class);

    private static final List<ProviderSeed> SEEDS = List.of(
            new ProviderSeed("OpenAI", "https://api.openai.com/v1", "openai", "https://platform.openai.com/"),
            new ProviderSeed("Alibaba Bailian", "https://dashscope.aliyuncs.com/compatible-mode/v1", "openai", "https://bailian.console.aliyun.com"),
            new ProviderSeed("Qiniu AI", "https://ai.qiniuapi.com/v1", "openai", "https://ai.qiniu.com/free"),
            new ProviderSeed("SiliconFlow", "https://api.siliconflow.cn/v1", "openai", "https://cloud.siliconflow.cn/i/6N2Q2X2L"),
            new ProviderSeed("Zhipu AI (GLM)", "https://open.bigmodel.cn/api/paas/v4", "glm", "https://open.bigmodel.cn/"),
            new ProviderSeed("Baidu Qianfan", "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/", "baidu", "https://console.bce.baidu.com/qianfan/"),
            new ProviderSeed("Tencent Hunyuan", "https://hunyuan.tencentcloudapi.com", "tencent", "https://cloud.tencent.com/product/hunyuan"),
            new ProviderSeed("iFlytek Spark", "https://spark-api.xf-yun.com/v1.1/chat", "spark", "https://xinghuo.xfyun.cn/"),
            new ProviderSeed("DeepSeek", "https://api.deepseek.com/v1", "openai", "https://platform.deepseek.com/"),
            new ProviderSeed("Volcengine Ark", "https://ark.cn-beijing.volces.com/api/v3", "volcengine", "https://ark.cn-beijing.volces.com/"),
            new ProviderSeed("Inspire AI", "https://api.inspire.ai/v1", "openai", "https://platform.inspire.ai/"),
            new ProviderSeed("Google AI Studio (Gemini)", "https://generativelanguage.googleapis.com/v1beta", "google", "https://aistudio.google.com/"),
            new ProviderSeed("OpenRouter", "https://openrouter.ai/api/v1", "openai", "https://openrouter.ai/")
    );

    private final ProviderRepository providerRepository;

    public ProviderDataLoader(ProviderRepository providerRepository) {
        this.providerRepository = providerRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (providerRepository.count() > 0) {
            log.debug("providers table has data, skipping init");
            return;
        }
        log.info("Initializing provider list...");
        for (ProviderSeed s : SEEDS) {
            if (providerRepository.findByName(s.name).isEmpty()) {
                Provider p = new Provider();
                p.setName(s.name);
                p.setBaseUrl(s.baseUrl);
                p.setApiType(s.apiType);
                p.setRegisterUrl(s.registerUrl);
                p.setAuthHeader(true);
                providerRepository.save(p);
            }
        }
        log.info("Provider list initialized");
    }

    private record ProviderSeed(String name, String baseUrl, String apiType, String registerUrl) {}
}
