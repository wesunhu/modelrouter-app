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
            new ProviderSeed("阿里云百炼", "https://dashscope.aliyuncs.com/compatible-mode/v1", "openai", "https://bailian.console.aliyun.com"),
            new ProviderSeed("七牛云AI", "https://ai.qiniuapi.com/v1", "openai", "https://ai.qiniu.com/free"),
            new ProviderSeed("硅基流动 (SiliconFlow)", "https://api.siliconflow.cn/v1", "openai", "https://cloud.siliconflow.cn/i/6N2Q2X2L"),
            new ProviderSeed("智谱AI (GLM)", "https://open.bigmodel.cn/api/paas/v4", "glm", "https://open.bigmodel.cn/"),
            new ProviderSeed("百度智能云千帆", "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/", "baidu", "https://console.bce.baidu.com/qianfan/"),
            new ProviderSeed("腾讯云混元", "https://hunyuan.tencentcloudapi.com", "tencent", "https://cloud.tencent.com/product/hunyuan"),
            new ProviderSeed("讯飞星火", "https://spark-api.xf-yun.com/v1.1/chat", "spark", "https://xinghuo.xfyun.cn/"),
            new ProviderSeed("DeepSeek 官方", "https://api.deepseek.com/v1", "openai", "https://platform.deepseek.com/"),
            new ProviderSeed("火山引擎方舟", "https://ark.cn-beijing.volces.com/api/v3", "volcengine", "https://ark.cn-beijing.volces.com/"),
            new ProviderSeed("无问芯穹", "https://api.inspire.ai/v1", "openai", "https://platform.inspire.ai/"),
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
            log.debug("providers 表已有数据，跳过初始化");
            return;
        }
        log.info("初始化平台列表数据...");
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
        log.info("平台列表初始化完成");
    }

    private record ProviderSeed(String name, String baseUrl, String apiType, String registerUrl) {}
}
