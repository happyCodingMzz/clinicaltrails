package app.service;


import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.tmt.v20180321.TmtClient;
import com.tencentcloudapi.tmt.v20180321.models.TextTranslateRequest;
import com.tencentcloudapi.tmt.v20180321.models.TextTranslateResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class TranslatorService {

    @Autowired
    private TmtClient tmtClient;

    public String translate(String text) {
        int maxRetry = 10;
        String result;
        do {
            maxRetry--;
            result = translate(text, "auto", "zh");
        } while (Objects.equals(result, "") && maxRetry >= 0);
        return result;
    }

    @SneakyThrows
    public String translate(String text, String from, String to) {
        List<String> fragments = splitBySentences(text);
        List<String> translatedFragments = new ArrayList<>();
        try {
            for (String fragment : fragments) {
                TextTranslateRequest textTranslateRequest = new TextTranslateRequest();
                textTranslateRequest.setSource(from);
                textTranslateRequest.setTarget(to);
                textTranslateRequest.setProjectId(0L);
                textTranslateRequest.setSourceText(fragment);
                TextTranslateResponse response = tmtClient.TextTranslate(textTranslateRequest);
                translatedFragments.add(response.getTargetText());
            }
        } catch (TencentCloudSDKException e) {
            log.error("Translation failed");
            return "";
        }
        Thread.sleep(1000);
        return String.join(" ", translatedFragments);

    }

    private List<String> splitBySentences(String text) {
        List<String> fragments = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return fragments;
        }

        // 使用正则表达式按句子边界分割
        String[] sentences = text.split("(?<=[。！？.!?\\n])");
        StringBuilder currentFragment = new StringBuilder();

        for (String sentence : sentences) {
            // 如果当前句子本身超过最大长度，需要按字符数硬切分
            if (sentence.length() > 1900) {
                // 先保存当前已积累的片段
                if (currentFragment.length() > 0) {
                    fragments.add(currentFragment.toString());
                    currentFragment = new StringBuilder();
                }
                // 对超长句子进行硬切分
                List<String> chunks = splitByLength(sentence);
                fragments.addAll(chunks);
            }
            // 如果当前片段加上新句子不超过限制
            else if (currentFragment.length() + sentence.length() <= 1900) {
                currentFragment.append(sentence);
            }
            // 如果超过限制，保存当前片段并开始新的
            else {
                if (currentFragment.length() > 0) {
                    fragments.add(currentFragment.toString());
                }
                currentFragment = new StringBuilder(sentence);
            }
        }

        // 添加最后一个片段
        if (currentFragment.length() > 0) {
            fragments.add(currentFragment.toString());
        }

        return fragments;
    }

    private List<String> splitByLength(String text) {
        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < text.length(); i += 1900) {
            int end = Math.min(text.length(), i + 1900);
            chunks.add(text.substring(i, end));
        }
        return chunks;
    }

}
