package com.yoru.qingxintutor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.EncodedResourceResolver;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

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

        @NonNull
        @Override
        protected Resource getResource(@NonNull String resourcePath, @NonNull Resource location) throws IOException {
            Resource resolved = super.getResource(resourcePath, location);
            if (!resolved.exists()) throw new FileNotFoundException("Access denied, wrong path to avatar file");

            Path resolvedPath = resolved.getFile().toPath().normalize();

            if (!resolvedPath.startsWith(allowedBasePath)) {
                throw new FileNotFoundException("Access denied, wrong path to avatar file");
            }

            return resolved;
        }
    }
}
