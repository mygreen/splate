package com.github.mygreen.splate;

import static com.github.mygreen.splate.SqlUtils.*;
import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import com.github.mygreen.splate.type.EnumNameType;
import com.github.mygreen.splate.type.EnumOrdinalType;
import com.github.mygreen.splate.type.JobType;
import com.github.mygreen.splate.type.SqlTemplateValueTypeRegistry;


/**
 * {@link SqlTemplateContext} のテスタ。
 *
 * @author T.TSUCHIE
 *
 */
class SqlTemplateContextTest {

    private SqlTemplateEngine templateEngine;

    private ResourceLoader resourceLoader;

    @BeforeEach
    void setUp() {
         this.templateEngine = new SqlTemplateEngine();
         this.resourceLoader = new DefaultResourceLoader();
    }

    @DisplayName("BeanPropertySqlTemplateContextによるテスト")
    @Test
    void testBeanProperty() throws Exception {

        String path = "classpath:template/employee_select.sql";

        SqlTemplate template = templateEngine.getTemplate(path);

        SelectParam param = SelectParam.builder()
                .salaryMin(new BigDecimal(1200))
                .salaryMax(new BigDecimal(1800))
                .build();

        ProcessResult result = template.process(new BeanPropertySqlTemplateContext(param));

        String expectedSql = readStream(resourceLoader.getResource("classpath:result/employee_select.sql").getInputStream(), "UTF-8");
        assertThat(result.getSql()).isEqualTo(expectedSql);

        assertThat(result.getParameters()).containsExactly(param.getSalaryMin(), param.getSalaryMax());

    }

    @DisplayName("BeanPropertySqlTemplateContextによるテスト")
    @Test
    void testBeanProperty_notFoundProperty() throws Exception {

        String path = "classpath:template/employee_select.sql";

        SqlTemplate template = templateEngine.getTemplate(path);

        SelectParam2 param = SelectParam2.builder()
                .salaryMin(new BigDecimal(1200))
                .build();

        BeanPropertySqlTemplateContext context = new BeanPropertySqlTemplateContext(param);
        context.setIgnoreNotFoundProperty(true);
        ProcessResult result = template.process(context);

        String expectedSql = readStream(resourceLoader.getResource("classpath:result/employee_select_salaryMaxNull.sql").getInputStream(), "UTF-8");
        assertThat(result.getSql()).isEqualTo(expectedSql);

        assertThat(result.getParameters()).containsExactly(param.getSalaryMin());

    }

    @DisplayName("MapSqlTemplateContextによるテスト")
    @Test
    void testMap() throws Exception {

        String path = "classpath:template/employee_select.sql";

        SqlTemplate template = templateEngine.getTemplate(path);

        Map<String, Object> param = Map.of("salaryMin", new BigDecimal(1200), "salaryMax", new BigDecimal(1800));

        ProcessResult result = template.process(new MapSqlTemplateContext(param));

        String expectedSql = readStream(resourceLoader.getResource("classpath:result/employee_select.sql").getInputStream(), "UTF-8");
        assertThat(result.getSql()).isEqualTo(expectedSql);

        assertThat(result.getParameters()).containsExactly(param.get("salaryMin"), param.get("salaryMax"));

    }

    @DisplayName("MapSqlTemplateContextによるテスト - 変数が存在しない場合")
    @Test
    void testMap_notFoundVariable() throws Exception {

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

    @SuppressWarnings({"rawtypes", "unchecked"})
    @DisplayName("SqlTemplateValueTypeRegistry を書き換える")
    @Test
    void testValueTypeRegistry() {

        // 元となる変換規則の定義
        SqlTemplateValueTypeRegistry registry = new SqlTemplateValueTypeRegistry();
        registry.register(Enum.class, new EnumOrdinalType());


        String sql = "SELECT * FROM emp WHERE job = /*job*/'CLERK'";

        SqlTemplate template = templateEngine.getTemplateByText(sql);

        MapSqlTemplateContext context = new MapSqlTemplateContext(registry, Map.of("job", JobType.COOKS));

        // 列挙型の変換規則を上書きする
        context.registerValueType(Enum.class, new EnumNameType());

        ProcessResult result = template.process(context);

        assertThat(result.getSql()).isEqualTo("SELECT * FROM emp WHERE job = ?");
        assertThat(result.getParameters()).containsExactly("COOKS");

        // 元の変換規則のチェック - 変わっていないことをチェック
        assertThat(registry.findValueType(JobType.class, null)).isInstanceOf(EnumOrdinalType.class);

    }

//    @Disabled
//    @Test
//    void testCallback() {
//
//        String sql = "select * from where name like /*#contains(name)*/'S%'";
//
//        SqlTemplate template = templateEndine.getTemplateByText(sql);
//        SqlTemplateContext templateContext = new MapSqlTemplateContext(Map.of("name", "abc"));
//
//        // EL式中のカスタム関数の登録
//        templateContext.setEvaluationContextCallback(c -> {
//            try {
//                c.registerFunction("contains", SqlFunctions.class.getMethod("contains", String.class));
//            } catch (NoSuchMethodException | SecurityException e) {
//                throw new RuntimeException(e);
//            }
//        });
//
//        ProcessResult result = template.process(templateContext);
//
//        assertThat(result.getSql()).isEqualTo("select * from where name like ?");
//        assertThat(result.getParameters()).containsExactly("%abc%");
//
//    }
//
//    /**
//     * SQLテンプレート中で利用可能なカスタム関数
//     *
//     */
//    static class SqlFunctions {
//
//        public static String contains(String value) {
//            return "%" + value + "%";
//        }
//
//    }
}
