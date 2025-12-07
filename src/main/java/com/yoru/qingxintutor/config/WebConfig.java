package com.yoru.qingxintutor.config;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.EncodedResourceResolver;
import org.springframework.web.servlet.resource.PathResourceResolver;

import javax.net.ssl.SSLContext;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.time.Duration;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.file-dir}")
    private String fileDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/file/**")
                .addResourceLocations("file:" + fileDir + "/")
                .resourceChain(true)
                .addResolver(new EncodedResourceResolver())
                .addResolver(new SafePathResourceResolver(fileDir));
    }

    private static class SafePathResourceResolver extends PathResourceResolver {
        private final Path allowedBasePath;

        public SafePathResourceResolver(String baseDir) {
            this.allowedBasePath = Paths.get(baseDir).toAbsolutePath().normalize();
        }

        @SuppressWarnings("NullableProblems")
        @Nullable
        @Override
        protected Resource getResource(@NonNull String resourcePath, @NonNull Resource location) throws IOException {
            Resource resolved = super.getResource(resourcePath, location);
            //noinspection ConstantValue
            if (resolved == null || !resolved.exists())
                throw new FileNotFoundException("Access denied, wrong path to avatar file");

            Path resolvedPath = resolved.getFile().toPath().normalize();

            if (!resolvedPath.startsWith(allowedBasePath)) {
                throw new FileNotFoundException("Access denied, wrong path to avatar file");
            }

            return resolved;
        }
    }

    /**
     * 创建 HttpClient 5 实例
     */
    @Bean
    public HttpClient httpClient() throws Exception {
        // 1. 定义信任所有证书的策略
        TrustStrategy trustStrategy = (X509Certificate[] chain, String authType) -> true;

        // 2. 构建 SSLContext
        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(null, trustStrategy)
                .build();

        // 3. 创建 SSL 连接工厂（禁用主机名校验）
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslContext,
                (hostname, session) -> true
        );

        // 4. 构建连接管理器
        var connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(sslsf)
                .setMaxConnTotal(50)
                .setMaxConnPerRoute(20)
                .setDefaultSocketConfig(SocketConfig.custom()
                        .setSoTimeout(Timeout.of(Duration.ofSeconds(10)))
                        .build())
                .build();

        // 5. 构建 HttpClient
        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .disableCookieManagement()
                .build();
    }

    // 信任所有ssl证书的 RestTemplate 实例
    @Bean
    public RestTemplate restTemplate(HttpClient httpClient) {
        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        factory.setConnectionRequestTimeout(5000);
        factory.setReadTimeout(10000);

        return new RestTemplate(factory);
    }
}
