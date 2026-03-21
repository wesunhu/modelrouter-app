/**
 * DTO for chat completion parameters parsed from OpenAI-style requests.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

package com.modelrouter.client;

import java.util.Map;

/**
 * OpenAI 兼容的聊天参数
 */
public record ChatParams(
        Double temperature,
        Integer maxTokens,
        Double topP,
        Object stop,  // string or array of strings
        Double presencePenalty,
        Double frequencyPenalty,
        Integer seed,
        Object responseFormat  // {"type": "json_object"} for JSON mode
) {
    public static ChatParams from(Map<String, Object> body) {
        if (body == null) return new ChatParams(null, null, null, null, null, null, null, null);
        Double temp = body.get("temperature") != null ? ((Number) body.get("temperature")).doubleValue() : null;
        Integer max = body.get("max_tokens") != null ? ((Number) body.get("max_tokens")).intValue() : null;
        Double topP = body.get("top_p") != null ? ((Number) body.get("top_p")).doubleValue() : null;
        Object stop = body.get("stop");
        Double pp = body.get("presence_penalty") != null ? ((Number) body.get("presence_penalty")).doubleValue() : null;
        Double fp = body.get("frequency_penalty") != null ? ((Number) body.get("frequency_penalty")).doubleValue() : null;
        Integer seed = body.get("seed") != null ? ((Number) body.get("seed")).intValue() : null;
        Object rf = body.get("response_format");
        return new ChatParams(temp, max, topP, stop, pp, fp, seed, rf);
    }
}
