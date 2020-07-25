package com.github.mygreen.sqltemplate;

import static com.github.mygreen.sqltemplate.SqlUtils.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

/**
 * {@link SqlContext} のテスタ。
 *
 *
 * @author T.TSUCHIE
 *
 */
public class SqlContextTest {

    private SqlTemplateEngine sqlTemplateEndine;

    private ResourceLoader resourceLoader;

    @BeforeEach
    public void setUp() {
         this.sqlTemplateEndine = new SqlTemplateEngine();
         this.resourceLoader = new DefaultResourceLoader();
    }

    @DisplayName("BeanPropertySqlContextによるテスト")
    @Test
    public void testBeanProperty() throws Exception {

        String path = "classpath:template/employee_select.sql";

        SqlTemplate template = sqlTemplateEndine.getTemplate(path);

        SelectParam param = SelectParam.builder()
                .salaryMin(new BigDecimal(1200))
                .salaryMax(new BigDecimal(1800))
                .build();

        ProcessResult result = template.process(new BeanPropertySqlContext(param));

        String expectedSql = readStream(resourceLoader.getResource("classpath:result/employee_select.sql").getInputStream(), "UTF-8");
        assertThat(result.getSql()).isEqualTo(expectedSql);

        assertThat(result.getParameters()).containsExactly(param.getSalaryMin(), param.getSalaryMax());

    }

    @DisplayName("MapSqlContextによるテスト")
    @Test
    public void testMap() throws Exception {

        String path = "classpath:template/employee_select.sql";

        SqlTemplate template = sqlTemplateEndine.getTemplate(path);

        Map<String, Object> param = Map.of("salaryMin", new BigDecimal(1200), "salaryMax", new BigDecimal(1800));

        ProcessResult result = template.process(new MapSqlContext(param));

        String expectedSql = readStream(resourceLoader.getResource("classpath:result/employee_select.sql").getInputStream(), "UTF-8");
        assertThat(result.getSql()).isEqualTo(expectedSql);

        assertThat(result.getParameters()).containsExactly(param.get("salaryMin"), param.get("salaryMax"));

    }

    @Test
    public void testCallback() {
        fail("まだ実装されていません");
    }
}
