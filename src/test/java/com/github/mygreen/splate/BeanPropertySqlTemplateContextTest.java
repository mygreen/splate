package com.github.mygreen.splate;

import static com.github.mygreen.splate.SqlUtils.*;
import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import com.github.mygreen.splate.node.NodeProcessException;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

/**
 * {@link BeanPropertySqlTemplateContext}のテスタ
 *
 * @author T.TSUCHIE
 *
 */
class BeanPropertySqlTemplateContextTest {

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

        NormalParam param = NormalParam.builder()
                .salaryMin(new BigDecimal(1200))
                .salaryMax(new BigDecimal(1800))
                .build();

        ProcessResult result = template.process(new BeanPropertySqlTemplateContext(param));

        String expectedSql = readStream(resourceLoader.getResource("classpath:result/employee_select.sql").getInputStream(), "UTF-8");
        assertThat(result.getSql()).isEqualTo(expectedSql);

        assertThat(result.getParameters()).containsExactly(param.getSalaryMin(), param.getSalaryMax());

    }

    @Test
    void testNotFoundProperty_ignoreNotFoundPropertyIsTrue() throws Exception {

        String path = "classpath:template/employee_select.sql";

        SqlTemplate template = templateEngine.getTemplate(path);

        LackedParam param = LackedParam.builder()
                .salaryMin(new BigDecimal(1200))
                .build();

        BeanPropertySqlTemplateContext context = new BeanPropertySqlTemplateContext(param);
        context.setIgnoreNotFoundProperty(true);
        ProcessResult result = template.process(context);

        String expectedSql = readStream(resourceLoader.getResource("classpath:result/employee_select_salaryMaxNull.sql").getInputStream(), "UTF-8");
        assertThat(result.getSql()).isEqualTo(expectedSql);

        assertThat(result.getParameters()).containsExactly(param.getSalaryMin());
    }

    @Test
    void testNotFoundProperty_ignoreNotFoundPropertyIsFalse() throws Exception {

        String path = "classpath:template/employee_select.sql";

        SqlTemplate template = templateEngine.getTemplate(path);

        LackedParam param = LackedParam.builder()
                .salaryMin(new BigDecimal(1200))
                .build();

        BeanPropertySqlTemplateContext context = new BeanPropertySqlTemplateContext(param);

        assertThatThrownBy(() -> template.process(context))
                .isInstanceOf(NodeProcessException.class);

    }

    @Test
    void testEvaluateTemplate_nested() throws Exception {

        String path = "classpath:template/address_select.sql";

        SqlTemplate template = templateEngine.getTemplate(path);

        NestedParam param = NestedParam.builder()
                .pk(new NestedParam.PK("000001", 2L))
                .telNumber("001-0123-456")
                .build();

        ProcessResult result = template.process(new BeanPropertySqlTemplateContext(param));

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

        LackedNestedParam param = LackedNestedParam.builder()
                .pk(new LackedNestedParam.PK("000001"))
                .telNumber("001-0123-456")
                .build();

        BeanPropertySqlTemplateContext context = new BeanPropertySqlTemplateContext(param);
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

        LackedNestedParam param = LackedNestedParam.builder()
                .pk(new LackedNestedParam.PK("000001"))
                .telNumber("001-0123-456")
                .build();

        BeanPropertySqlTemplateContext context = new BeanPropertySqlTemplateContext(param);

        assertThatThrownBy(() -> template.process(context))
                .isInstanceOf(NodeProcessException.class);

    }


    @Value
    @Builder
    public static class NormalParam {

        private BigDecimal salaryMin;

        private BigDecimal salaryMax;
    }

    /**
     * パラメータが足りない場合のパラメータ
     *
     */
    @Value
    @Builder
    public static class LackedParam {

        private BigDecimal salaryMin;

    }

    /**
     * ネストしたBeanのパラメータ
     *
     */
    @Value
    @Builder
    public static class NestedParam {

        private PK pk;

        private String telNumber;

        @AllArgsConstructor
        @Data
        public static class PK {

            private String employeeId;

            private Long addressId;

        }

    }

    /**
     * パラメータが足りない場合のネストしたBeanのパラメータ
     *
     */
    @Value
    @Builder
    public static class LackedNestedParam {

        private PK pk;

        private String telNumber;

        @AllArgsConstructor
        @Data
        public static class PK {

            private String employeeId;

        }

    }
}
