package com.github.mygreen.sqltemplate;

import static com.github.mygreen.sqltemplate.SqlUtils.*;
import static org.assertj.core.api.Assertions.*;

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

    private SqlTemplateEngine templateEndine;

    private ResourceLoader resourceLoader;

    @BeforeEach
    public void setUp() {
         this.templateEndine = new SqlTemplateEngine();
         this.resourceLoader = new DefaultResourceLoader();
    }

    @DisplayName("BeanPropertySqlContextによるテスト")
    @Test
    public void testBeanProperty() throws Exception {

        String path = "classpath:template/employee_select.sql";

        SqlTemplate template = templateEndine.getTemplate(path);

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

        SqlTemplate template = templateEndine.getTemplate(path);

        Map<String, Object> param = Map.of("salaryMin", new BigDecimal(1200), "salaryMax", new BigDecimal(1800));

        ProcessResult result = template.process(new MapSqlContext(param));

        String expectedSql = readStream(resourceLoader.getResource("classpath:result/employee_select.sql").getInputStream(), "UTF-8");
        assertThat(result.getSql()).isEqualTo(expectedSql);

        assertThat(result.getParameters()).containsExactly(param.get("salaryMin"), param.get("salaryMax"));

    }

//    @Disabled
//    @Test
//    public void testCallback() {
//
//        String sql = "select * from where name like /*#contains(name)*/'S%'";
//
//        SqlTemplate template = templateEndine.getTemplateByText(sql);
//        SqlContext sqlContext = new MapSqlContext(Map.of("name", "abc"));
//
//        // EL式中のカスタム関数の登録
//        sqlContext.setEvaluationContextCallback(c -> {
//            try {
//                c.registerFunction("contains", SqlFunctions.class.getMethod("contains", String.class));
//            } catch (NoSuchMethodException | SecurityException e) {
//                throw new RuntimeException(e);
//            }
//        });
//
//        ProcessResult result = template.process(sqlContext);
//
//        assertThat(result.getSql()).isEqualTo("select * from where name like ?");
//        assertThat(result.getParameters()).containsExactly("%abc%");
//
//    }

    /**
     * SQLテンプレート中で利用可能なカスタム関数
     *
     */
    static class SqlFunctions {

        public static String contains(String value) {
            return "%" + value + "%";
        }

    }
}
