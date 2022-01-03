package com.github.mygreen.splate;

import static com.github.mygreen.splate.SqlUtils.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import com.github.mygreen.splate.node.NodeProcessException;

/**
 * {@link EmptyValueSqlTemplateContext}のテスタ
 *
 * @author T.TSUCHIE
 *
 */
class EmptyValueSqlTemplateContextTest {

    private SqlTemplateEngine templateEngine;

    private ResourceLoader resourceLoader;

    @BeforeEach
    void setUp() {
         this.templateEngine = new SqlTemplateEngine();
         this.resourceLoader = new DefaultResourceLoader();
    }

    @Test
    void testNotFoundProperty_ignoreNotFoundPropertyIsTrue() throws Exception {

        String path = "classpath:template/employee_select.sql";

        SqlTemplate template = templateEngine.getTemplate(path);

        EmptyValueSqlTemplateContext context = new EmptyValueSqlTemplateContext();
        context.setIgnoreNotFoundProperty(true);
        ProcessResult result = template.process(context);

        String expectedSql = readStream(resourceLoader.getResource("classpath:result/employee_select_allNull.sql").getInputStream(), "UTF-8");
        assertThat(result.getSql()).isEqualTo(expectedSql);

        assertThat(result.getParameters()).isEmpty();

    }

    @Test
    void testNotFoundProperty_ignoreNotFoundPropertyIsFalse() throws Exception {

        String path = "classpath:template/employee_select.sql";

        SqlTemplate template = templateEngine.getTemplate(path);

        EmptyValueSqlTemplateContext context = new EmptyValueSqlTemplateContext();
        context.setIgnoreNotFoundProperty(false);

        assertThatThrownBy(() -> template.process(context))
                .isInstanceOf(NodeProcessException.class);

    }

    @Test
    void testNotFoundProperty_nested_ignoreNotFoundPropertyIsTrue() throws Exception {

        String path = "classpath:template/address_select.sql";

        SqlTemplate template = templateEngine.getTemplate(path);

        EmptyValueSqlTemplateContext context = new EmptyValueSqlTemplateContext();
        context.setIgnoreNotFoundProperty(true);
        ProcessResult result = template.process(context);

        String expectedSql = readStream(resourceLoader.getResource("classpath:result/address_select_allNull.sql").getInputStream(), "UTF-8");
        assertThat(result.getSql()).isEqualTo(expectedSql);

        assertThat(result.getParameters()).isEmpty();

    }

    @Test
    void testNotFoundProperty_nested_ignoreNotFoundPropertyIsFalse() throws Exception {

        String path = "classpath:template/address_select.sql";

        SqlTemplate template = templateEngine.getTemplate(path);

        EmptyValueSqlTemplateContext context = new EmptyValueSqlTemplateContext();
        context.setIgnoreNotFoundProperty(false);

        assertThatThrownBy(() -> template.process(context))
                .isInstanceOf(NodeProcessException.class);

    }

}
