package com.github.mygreen.splate;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
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

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void testValueTypeRegistry() {

        // 元となる変換規則の定義
        SqlTemplateValueTypeRegistry registry = new SqlTemplateValueTypeRegistry();
        registry.register(Enum.class, new EnumOrdinalType());


        String sql = "SELECT * FROM Employee emp WHERE job = /*job*/'CLERK'";

        SqlTemplate template = templateEngine.getTemplateByText(sql);

        MapSqlTemplateContext context = new MapSqlTemplateContext(registry, Map.of("job", JobType.COOKS));

        // 列挙型の変換規則を上書きする
        context.registerValueType(Enum.class, new EnumNameType());

        ProcessResult result = template.process(context);

        assertThat(result.getSql()).isEqualTo("SELECT * FROM Employee emp WHERE job = ?");
        assertThat(result.getParameters()).containsExactly("COOKS");

        // 元の変換規則のチェック - 変わっていないことをチェック
        assertThat(registry.findValueType(JobType.class, null)).isInstanceOf(EnumOrdinalType.class);

    }

    @Test
    void testEvaluationContextEditor() {

        String sql = "select * from Employee emp where /*IF #notEmpty(name)*/name like /*name*/'S%'/*END*/";

        SqlTemplate template = templateEngine.getTemplateByText(sql);
        MapSqlTemplateContext templateContext = new MapSqlTemplateContext(Map.of("name", "%abc%"));

        // EL式中のカスタム関数の登録
        templateContext.setEvaluationContextEditor(c -> {
            try {
                c.registerFunction("notEmpty", SqlFunctions.class.getMethod("notEmpty", Object.class));
            } catch (NoSuchMethodException | SecurityException e) {
                throw new RuntimeException(e);
            }
        });

        ProcessResult result = template.process(templateContext);

        assertThat(result.getSql()).isEqualTo("select * from Employee emp where name like ?");
        assertThat(result.getParameters()).containsExactly("%abc%");

    }

    /**
     * SQLテンプレート中で利用可能なカスタム関数
     *
     */
    static class SqlFunctions {

        public static boolean notEmpty(Object value) {
            return value != null && !value.toString().isEmpty();
        }

    }

    @DisplayName("名前付きパラメータのテンプレートの評価")
    @Test
    void testProcessForNamedParam() {

        String sql = "SELECT * FROM Employee emp /*BEGIN*/WHERE"
                + " /*IF job != null*/job in /*job*/('CLERK')/*END*/"
                + " /*IF minAge != null*/AND age >= /*minAge*/20/*END*/"
                + "/*END*/";

        SqlTemplate template = templateEngine.getTemplateByText(sql);

        MapSqlTemplateContext context = new MapSqlTemplateContext(Map.of("job", List.of("DEVELOPER", "ADMIN"), "minAge", 30));

        NamedParamProcessResult result = template.processForNamedParam(context);

        assertThat(result.getSql()).isEqualTo("SELECT * FROM Employee emp WHERE job in (:job, :job_1) AND age >= :minAge");
        assertThat(result.getParameters())
                .containsEntry("job", "DEVELOPER")
                .containsEntry("job_1", "ADMIN")
                .containsEntry("minAge", 30);

    }

}
