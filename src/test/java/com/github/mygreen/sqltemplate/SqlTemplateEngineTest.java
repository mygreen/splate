package com.github.mygreen.sqltemplate;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

//@ExtendWith(SpringExtension.class)
//@ContextConfiguration(classes=TestConfig.class)
//@ContextConfiguration(locations = "classpath:/SqlTemplateContext.xml")
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

    }

    @Test
    void testGetTemplateByResource_cached() {

        sqlTemplateEngine.setCached(true);

        Resource resource = new ClassPathResource("template/employee_select.sql");

        SqlTemplate template = sqlTemplateEngine.getTemplate(resource);
        SqlTemplate template2 = sqlTemplateEngine.getTemplate(resource);

        // 結果をキャッシュしているので値は同じになる。
        assertThat(template).isEqualTo(template2);

    }

    @Test
    void testGetTemplateByText_cached() {

        sqlTemplateEngine.setCached(true);

        String sql = "select * from emp";

        SqlTemplate template = sqlTemplateEngine.getTemplateByText(sql);
        SqlTemplate template2 = sqlTemplateEngine.getTemplateByText(sql);

        // 結果をキャッシュしているので値は同じになる。
        assertThat(template).isEqualTo(template2);

    }
}
