package app.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.tmt.v20180321.TmtClient;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableJpaRepositories(basePackages = "app.dao")
public class AppConfiguration {

    @Value("${api.tencent.secretId}")
    public String tencentSecretId;
    
    @Value("${api.tencent.secretKey}")
    public String tencentSecretKey;

    @Value("${api.tencent.region}")
    public String tencentRegion;


    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
    }


    @Bean
    public TmtClient translatorTencentClient(){
        Credential cred = new Credential(tencentSecretId, tencentSecretKey);
        return new TmtClient(cred, tencentRegion);
    }
}
