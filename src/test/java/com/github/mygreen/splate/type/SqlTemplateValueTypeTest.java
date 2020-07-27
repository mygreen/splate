package com.github.mygreen.splate.type;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.mygreen.splate.MapSqlContext;
import com.github.mygreen.splate.ProcessResult;
import com.github.mygreen.splate.SqlContext;
import com.github.mygreen.splate.SqlTemplate;
import com.github.mygreen.splate.SqlTemplateEngine;
import com.github.mygreen.splate.type.SqlTemplateValueType;
import com.github.mygreen.splate.type.SqlTypeConversionException;


/**
 * 各ノードの{@link SqlTemplateValueType} による変換処理の実装のテスタ。
 *
 *
 * @author T.TSUCHIE
 *
 */
public class SqlTemplateValueTypeTest {

    private SqlTemplateEngine templateEngine;

    enum JobType {
        /**店員*/
        CLERK,
        /**調理師*/
        COOKS,
        /**オーナー*/
        OWNER
    }

    private static class JobValueType implements SqlTemplateValueType<JobType>{

        @Override
        public Object getBindVariableValue(JobType value) throws SqlTypeConversionException {
            return value.ordinal();
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
        SqlContext context = new MapSqlContext(Map.of("job", JobType.COOKS));
        context.registerValueType(JobType.class, new JobValueType());

        ProcessResult result = template.process(context);

        assertThat(result.getSql()).isEqualTo("SELECT * FROM emp WHERE job = ?");
        assertThat(result.getParameters()).containsExactly(1);
    }

    @Test
    void testParenBindVariableNode() {
        String sql = "SELECT * FROM emp WHERE job in /*job*/('CLERK', 'COOKS')";

        SqlTemplate template = templateEngine.getTemplateByText(sql);
        SqlContext context = new MapSqlContext(Map.of("job", List.of(JobType.COOKS, JobType.OWNER)));
        context.registerValueType(JobType.class, new JobValueType());

        ProcessResult result = template.process(context);

        assertThat(result.getSql()).isEqualTo("SELECT * FROM emp WHERE job in (?, ?)");
        assertThat(result.getParameters()).containsExactly(1, 2);
    }

    @Test
    void testEmbeddedValueNode() {
        String sql = "SELECT * FROM emp WHERE job = /*$job*/'CLERK'";

        SqlTemplate template = templateEngine.getTemplateByText(sql);
        SqlContext context = new MapSqlContext(Map.of("job", JobType.COOKS));
        context.registerValueType(JobType.class, new JobValueType());

        ProcessResult result = template.process(context);

        assertThat(result.getSql()).isEqualTo("SELECT * FROM emp WHERE job = COOKS");
        assertThat(result.getParameters()).isEmpty();
    }
}
