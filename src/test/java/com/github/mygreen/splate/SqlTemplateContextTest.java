package com.github.mygreen.splate;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.expression.spel.support.StandardEvaluationContext;

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

    @Test
    void testEvaluationContextEditor() {

        String sql = "select * from where /*IF #notEmpty(name)*/name like /*name*/'S%'/*END*/";

        SqlTemplate template = templateEngine.getTemplateByText(sql);
        SqlTemplateContext templateContext = new MapSqlTemplateContext(Map.of("name", "%abc%"));

        // EL式中のカスタム関数の登録
        templateContext.setEvaluationContextEditor(c -> {
            try {
                ((StandardEvaluationContext)c).registerFunction("notEmpty", SqlFunctions.class.getMethod("notEmpty", Object.class));
            } catch (NoSuchMethodException | SecurityException e) {
                throw new RuntimeException(e);
            }
        });

        ProcessResult result = template.process(templateContext);

        assertThat(result.getSql()).isEqualTo("select * from where name like ?");
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
}
