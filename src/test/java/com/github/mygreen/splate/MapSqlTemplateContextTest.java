package com.github.mygreen.splate;

import static com.github.mygreen.splate.SqlUtils.*;
import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import com.github.mygreen.splate.node.NodeProcessException;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * {@link MapSqlTemplateContext}のテスタ
 *
 * @author T.TSUCHIE
 *
 */
class MapSqlTemplateContextTest {

    private SqlTemplateEngine templateEngine;

    private ResourceLoader resourceLoader;

    @BeforeEach
    void setUp() {
         this.templateEngine = new SqlTemplateEngine();
         this.resourceLoader = new DefaultResourceLoader();
    }

    @Test
    void testEvaluateTemplate() throws Exception {

        String path = "classpath:template/employee_select.sql";

        SqlTemplate template = templateEngine.getTemplate(path);

        Map<String, Object> param = Map.of("salaryMin", new BigDecimal(1200), "salaryMax", new BigDecimal(1800));

        ProcessResult result = template.process(new MapSqlTemplateContext(param));

        String expectedSql = readStream(resourceLoader.getResource("classpath:result/employee_select.sql").getInputStream(), "UTF-8");
        assertThat(result.getSql()).isEqualTo(expectedSql);

        assertThat(result.getParameters()).containsExactly(param.get("salaryMin"), param.get("salaryMax"));

    }

    @Test
    void testNotFoundProperty_ignoreNotFoundPropertyIsTrue() throws Exception {

        String path = "classpath:template/employee_select.sql";

        SqlTemplate template = templateEngine.getTemplate(path);

        Map<String, Object> param = Map.of("salaryMin", new BigDecimal(1200)/*, "salaryMax", new BigDecimal(1800)*/);

        MapSqlTemplateContext context = new MapSqlTemplateContext(param);
        context.setIgnoreNotFoundProperty(true);
        ProcessResult result = template.process(context);

        String expectedSql = readStream(resourceLoader.getResource("classpath:result/employee_select_salaryMaxNull.sql").getInputStream(), "UTF-8");
        assertThat(result.getSql()).isEqualTo(expectedSql);

        assertThat(result.getParameters()).containsExactly(param.get("salaryMin"));

    }

    @Test
    void testNotFoundProperty_ignoreNotFoundPropertyIsFalse() throws Exception {

        String path = "classpath:template/employee_select.sql";

        SqlTemplate template = templateEngine.getTemplate(path);

        Map<String, Object> param = Map.of("salaryMin", new BigDecimal(1200)/*, "salaryMax", new BigDecimal(1800)*/);

        MapSqlTemplateContext context = new MapSqlTemplateContext(param);
        context.setIgnoreNotFoundProperty(false);

        assertThatThrownBy(() -> template.process(context))
                .isInstanceOf(NodeProcessException.class);


    }

    @Test
    void testEvaluateTemplate_nested() throws Exception {

        String path = "classpath:template/address_select.sql";

        SqlTemplate template = templateEngine.getTemplate(path);

        Map<String, Object> param = Map.of("pk", new NestedPK("000001", 2L), "telNumber", "001-0123-456");

        ProcessResult result = template.process(new MapSqlTemplateContext(param));

        String expectedSql = readStream(resourceLoader.getResource("classpath:result/address_select.sql").getInputStream(), "UTF-8");
        assertThat(result.getSql()).isEqualTo(expectedSql);

        assertThat(result.getParameters()).containsExactly(
                "000001",
                2l,
                "001-0123-456");

    }

    @Test
    void testNotFoundProperty_nested_ignoreNotFoundPropetyIsTrue() throws Exception {

        String path = "classpath:template/address_select.sql";

        SqlTemplate template = templateEngine.getTemplate(path);

        Map<String, Object> param = Map.of("pk", new LackedNestedPK("000001"), "telNumber", "001-0123-456");

        MapSqlTemplateContext context = new MapSqlTemplateContext(param);
        context.setIgnoreNotFoundProperty(true);
        ProcessResult result = template.process(context);

        String expectedSql = readStream(resourceLoader.getResource("classpath:result/address_select_addressIdNull.sql").getInputStream(), "UTF-8");
        assertThat(result.getSql()).isEqualTo(expectedSql);

        assertThat(result.getParameters()).containsExactly(
                "000001",
                "001-0123-456");

    }

    @Test
    void testNotFoundProperty_nested_ignoreNotFoundPropetyIsFalse() throws Exception {

        String path = "classpath:template/address_select.sql";

        SqlTemplate template = templateEngine.getTemplate(path);

        Map<String, Object> param = Map.of("pk", new LackedNestedPK("000001"), "telNumber", "001-0123-456");

        MapSqlTemplateContext context = new MapSqlTemplateContext(param);
        context.setIgnoreNotFoundProperty(false);

        assertThatThrownBy(() -> template.process(context))
                .isInstanceOf(NodeProcessException.class);

    }

    @AllArgsConstructor
    @Data
    public static class NestedPK {

        private String employeeId;

        private Long addressId;

    }

    @AllArgsConstructor
    @Data
    public static class LackedNestedPK {

        private String employeeId;

    }
}
