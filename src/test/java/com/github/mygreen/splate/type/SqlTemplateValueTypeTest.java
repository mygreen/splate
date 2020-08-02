package com.github.mygreen.splate.type;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.mygreen.splate.MapSqlTemplateContext;
import com.github.mygreen.splate.Position;
import com.github.mygreen.splate.ProcessResult;
import com.github.mygreen.splate.SqlTemplate;
import com.github.mygreen.splate.SqlTemplateContext;
import com.github.mygreen.splate.SqlTemplateEngine;
import com.github.mygreen.splate.node.NodeProcessException;


/**
 * 各ノードの{@link SqlTemplateValueType} による変換処理の実装のテスタ。
 *
 *
 * @version 0.2
 * @author T.TSUCHIE
 *
 */
class SqlTemplateValueTypeTest {

    private SqlTemplateEngine templateEngine;

    private static class JobValueType implements SqlTemplateValueType<JobType>{

        @Override
        public Object getBindVariableValue(JobType value) throws SqlTypeConversionException {
            return value.ordinal();
        }
    }

    /**
     * 必ず例外をスローする。
     * @since 0.2
     *
     */
    private static class ErrorJobValueType implements SqlTemplateValueType<JobType>{

        @Override
        public Object getBindVariableValue(JobType value) throws SqlTypeConversionException {
            throw new SqlTypeConversionException(value, "fail conversion");
        }

        @Override
        public String getEmbeddedValue(JobType value) throws TextConversionException {
            throw new TextConversionException(value, "fail conversion");
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        this.templateEngine = new SqlTemplateEngine();
    }

    @Test
    void testBindVariableNode() {
        String sql = "SELECT * FROM emp WHERE job = /*job*/'CLERK'";

        SqlTemplate template = templateEngine.getTemplateByText(sql);
        SqlTemplateContext context = new MapSqlTemplateContext(Map.of("job", JobType.COOKS));
        context.registerValueType(JobType.class, new JobValueType());

        ProcessResult result = template.process(context);

        assertThat(result.getSql()).isEqualTo("SELECT * FROM emp WHERE job = ?");
        assertThat(result.getParameters()).containsExactly(1);
    }

    @Test
    void testBindVariableNode_path() {
        String sql = "SELECT * FROM emp WHERE job = /*job*/'CLERK'";

        SqlTemplate template = templateEngine.getTemplateByText(sql);
        SqlTemplateContext context = new MapSqlTemplateContext(Map.of("job", JobType.COOKS));
        context.registerValueType("job", JobType.class, new JobValueType());

        ProcessResult result = template.process(context);

        assertThat(result.getSql()).isEqualTo("SELECT * FROM emp WHERE job = ?");
        assertThat(result.getParameters()).containsExactly(1);
    }

    @Test
    void testBindVariableNode_failConversion() {
        String sql = "SELECT * FROM emp WHERE job = /*job*/'CLERK'";

        SqlTemplate template = templateEngine.getTemplateByText(sql);
        SqlTemplateContext context = new MapSqlTemplateContext(Map.of("job", JobType.COOKS));
        context.registerValueType(JobType.class, new ErrorJobValueType());

        assertThatThrownBy(() -> template.process(context))
            .isInstanceOf(NodeProcessException.class)
            .hasCauseInstanceOf(SqlTypeConversionException.class)
            .hasMessageContaining("Fail converting value of expression 'job'.")
            .hasFieldOrPropertyWithValue("position", new Position(1, 32, sql));

    }

    @Test
    void testParenBindVariableNode() {
        String sql = "SELECT * FROM emp WHERE job in /*job*/('CLERK', 'COOKS')";

        SqlTemplate template = templateEngine.getTemplateByText(sql);
        SqlTemplateContext context = new MapSqlTemplateContext(Map.of("job", List.of(JobType.COOKS, JobType.OWNER)));
        context.registerValueType(JobType.class, new JobValueType());

        ProcessResult result = template.process(context);

        assertThat(result.getSql()).isEqualTo("SELECT * FROM emp WHERE job in (?, ?)");
        assertThat(result.getParameters()).containsExactly(1, 2);
    }

    @Test
    void testParenBindVariableNode_failConversion() {
        String sql = "SELECT * FROM emp WHERE job in /*job*/('CLERK', 'COOKS')";

        SqlTemplate template = templateEngine.getTemplateByText(sql);
        SqlTemplateContext context = new MapSqlTemplateContext(Map.of("job", List.of(JobType.COOKS, JobType.OWNER)));
        context.registerValueType(JobType.class, new ErrorJobValueType());

        assertThatThrownBy(() -> template.process(context))
            .isInstanceOf(NodeProcessException.class)
            .hasCauseInstanceOf(SqlTypeConversionException.class)
            .hasMessageContaining("Fail converting value of expression 'job'.")
            .hasFieldOrPropertyWithValue("position", new Position(1, 33, sql));

    }

    @Test
    void testEmbeddedValueNode() {
        String sql = "SELECT * FROM emp WHERE job = /*$job*/'CLERK'";

        SqlTemplate template = templateEngine.getTemplateByText(sql);
        SqlTemplateContext context = new MapSqlTemplateContext(Map.of("job", JobType.COOKS));
        context.registerValueType(JobType.class, new JobValueType());

        ProcessResult result = template.process(context);

        assertThat(result.getSql()).isEqualTo("SELECT * FROM emp WHERE job = COOKS");
        assertThat(result.getParameters()).isEmpty();
    }

    @Test
    void testEmbeddedValueNode_failConversion() {
        String sql = "SELECT * FROM emp WHERE job = /*$job*/'CLERK'";

        SqlTemplate template = templateEngine.getTemplateByText(sql);
        SqlTemplateContext context = new MapSqlTemplateContext(Map.of("job", JobType.COOKS));
        context.registerValueType(JobType.class, new ErrorJobValueType());

        assertThatThrownBy(() -> template.process(context))
            .isInstanceOf(NodeProcessException.class)
            .hasCauseInstanceOf(TextConversionException.class)
            .hasMessageContaining("Fail converting value of expression 'job'.")
            .hasFieldOrPropertyWithValue("position", new Position(1, 33, sql));
    }
}
