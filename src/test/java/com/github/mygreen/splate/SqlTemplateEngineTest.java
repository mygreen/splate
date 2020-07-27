package com.github.mygreen.splate;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;


/**
 * {SqlTemplateEngine}のテスタ。
 *
 *
 * @author T.TSUCHIE
 *
 */
public class SqlTemplateEngineTest {

    private SqlTemplateEngine sqlTemplateEngine;

    @BeforeEach
    void setUp() throws Exception {
        this.sqlTemplateEngine = new SqlTemplateEngine();
    }

    @Test
    void testGetTemplateByLocation_cached() {

        sqlTemplateEngine.setCached(true);

        String path = "classpath:template/employee_select.sql";

        SqlTemplate template = sqlTemplateEngine.getTemplate(path);
        SqlTemplate template2 = sqlTemplateEngine.getTemplate(path);

        // 結果をキャッシュしているので値は同じになる。
        assertThat(template).isEqualTo(template2);

        // キャッシュをクリアした場合
        sqlTemplateEngine.clearCache();
        SqlTemplate template3 = sqlTemplateEngine.getTemplate(path);
        assertThat(template).isNotEqualTo(template3);

    }

    @Test
    void testGetTemplateByResource_cached() {

        sqlTemplateEngine.setCached(true);

        Resource resource = new ClassPathResource("template/employee_select.sql");

        SqlTemplate template = sqlTemplateEngine.getTemplate(resource);
        SqlTemplate template2 = sqlTemplateEngine.getTemplate(resource);

        // 結果をキャッシュしているので値は同じになる。
        assertThat(template).isEqualTo(template2);

        // キャッシュをクリアした場合
        sqlTemplateEngine.clearCache();
        SqlTemplate template3 = sqlTemplateEngine.getTemplate(resource);
        assertThat(template).isNotEqualTo(template3);
    }

    @Test
    void testGetTemplateByText_cached() {

        sqlTemplateEngine.setCached(true);

        String sql = "select * from emp";

        SqlTemplate template = sqlTemplateEngine.getTemplateByText(sql);
        SqlTemplate template2 = sqlTemplateEngine.getTemplateByText(sql);

        // 結果をキャッシュしているので値は同じになる。
        assertThat(template).isEqualTo(template2);

        // キャッシュをクリアした場合
        sqlTemplateEngine.clearCache();
        SqlTemplate template3 = sqlTemplateEngine.getTemplateByText(sql);
        assertThat(template).isNotEqualTo(template3);
    }
}
