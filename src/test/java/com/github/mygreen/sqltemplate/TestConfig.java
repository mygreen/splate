package com.github.mygreen.sqltemplate;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

@Configuration
public class TestConfig {

    @Bean
    public SqlTemplateEngine sqlTemplateEngine(ResourceLoader resourceLoader) {
        SqlTemplateEngine loader = new SqlTemplateEngine();
        loader.setResourceLoader(resourceLoader);
        loader.setSuffixName("h2");
        return loader;
    }

}
