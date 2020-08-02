package com.github.mygreen.splate.parse;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.expression.ExpressionException;
import org.springframework.expression.ParseException;

import com.github.mygreen.splate.EmptyValueSqlTemplateContext;
import com.github.mygreen.splate.MapSqlTemplateContext;
import com.github.mygreen.splate.Position;
import com.github.mygreen.splate.ProcessResult;
import com.github.mygreen.splate.SqlTemplate;
import com.github.mygreen.splate.SqlTemplateContext;
import com.github.mygreen.splate.SqlTemplateEngine;
import com.github.mygreen.splate.node.NodeProcessException;
import com.github.mygreen.splate.parser.SqlParseException;

/**
 * {@link SqlParserTest}のテスタ。
 *
 *
 * @author T.TSUCHIE
 *
 */
class SqlParserTest {

    private SqlTemplateEngine templateEngine;

    @BeforeEach
    void setUp() throws Exception {
        this.templateEngine = new SqlTemplateEngine();
    }

    @Test
    void testParse_noParams() {

        String sql = "SELECT * FROM emp";

        SqlTemplate template = templateEngine.getTemplateByText(sql);
        SqlTemplateContext context = new EmptyValueSqlTemplateContext();
        ProcessResult result = template.process(context);

        assertThat(result.getSql()).isEqualTo("SELECT * FROM emp");
        assertThat(result.getParameters()).isEmpty();

    }

    @Test
    void testParse_HintComment() {

        String sql = "SELECT /*+ aabbb */* FROM emp";

        SqlTemplate template = templateEngine.getTemplateByText(sql);
        SqlTemplateContext context = new EmptyValueSqlTemplateContext();
        ProcessResult result = template.process(context);

        assertThat(result.getSql()).isEqualTo("SELECT /*+ aabbb */* FROM emp");
        assertThat(result.getParameters()).isEmpty();

    }

    @DisplayName("コメントの閉じ忘れ")
    @Test
    void testParse_commentEndNotFound() {

        String sql = "SELECT * FROM emp/*hoge";

        assertThatThrownBy(() -> templateEngine.getTemplateByText(sql))
            .isInstanceOf(SqlParseException.class)
            .hasMessageContaining("Not closed comment '*/' for hoge.")
            .hasFieldOrPropertyWithValue("position", new Position(1, 19, sql));

    }

    @DisplayName("IFコメントで条件式がない場合")
    @Test
    void testParse_IfConditionNull() {

        String sql = "SELECT * FROM emp/*IF */ WHERE age = /*age*/20/*END*/";

        assertThatThrownBy(() -> templateEngine.getTemplateByText(sql))
            .isInstanceOf(SqlParseException.class)
            .hasMessageContaining("Not found IF condition.")
            .hasFieldOrPropertyWithValue("position", new Position(1, 22, sql));

    }

    @DisplayName("ENDコメントがない場合")
    @Test
    void testParse_EndCommentNotFound() {

        String sql = "SELECT * FROM emp/*BEGIN*/ WHERE /*IF job != null*/job = /*job*/'CLERK'/*END*//*IF deptno != null*/ AND deptno = /*deptno*/20/*END*/";

        assertThatThrownBy(() -> templateEngine.getTemplateByText(sql))
            .isInstanceOf(SqlParseException.class)
            .hasMessageContaining("Not found END comment.")
            .hasFieldOrPropertyWithValue("position", new Position(1, 132, sql));


    }

    @DisplayName("EL式が不正な場合")
    @Test
    void testParse_IfCondition_wrongExp() {

        String sql = "SELECT * FROM emp/*IF abc/*b*/ WHERE age = /*age*/20/*END*/";

        assertThatThrownBy(() -> templateEngine.getTemplateByText(sql))
            .isInstanceOf(SqlParseException.class)
            .hasCauseInstanceOf(ParseException.class)
            .hasMessageContaining("Fail parsing expression 'abc/*b'.")
            .hasFieldOrPropertyWithValue("position", new Position(1, 22, sql));

    }

    @Test
    void testBindVariable() {

        String sql = "SELECT * FROM emp WHERE job = /*job*/'CLERK' AND deptno = /*deptno*/20";

        SqlTemplate template = templateEngine.getTemplateByText(sql);
        SqlTemplateContext context = new MapSqlTemplateContext(Map.of("job", "Normal", "deptno", 10));
        ProcessResult result = template.process(context);

        assertThat(result.getSql()).isEqualTo("SELECT * FROM emp WHERE job = ? AND deptno = ?");
        assertThat(result.getParameters()).containsExactly("Normal", 10);

    }

    @Test
    void testBindVariable_placeholder() {
        String sql = "BETWEEN sal ? AND ?";

        SqlTemplate template = templateEngine.getTemplateByText(sql);
        SqlTemplateContext context = new MapSqlTemplateContext(Map.of("$1", 1, "$2", 10));
        ProcessResult result = template.process(context);

        assertThat(result.getSql()).isEqualTo("BETWEEN sal ? AND ?");
        assertThat(result.getParameters()).containsExactly(1, 10);
    }

    @Test
    void testBindVariable_evalELError() {

        String sql = "SELECT * FROM emp WHERE job = /*job*/'CLERK' AND deptno = /*deptno*/20";

        SqlTemplate template = templateEngine.getTemplateByText(sql);
        SqlTemplateContext context = new MapSqlTemplateContext(Map.of("job", "Normal"/*, "deptno", 10*/));

        assertThatThrownBy(() -> template.process(context))
            .isInstanceOf(NodeProcessException.class)
            .hasCauseInstanceOf(ExpressionException.class)
            .hasMessageContaining("Fail evaluating expression 'deptno'.")
            .hasFieldOrPropertyWithValue("position", new Position(1, 60, sql));

    }

    @Test
    void testParenBindVariable_collection() {
        String sql = "SELECT * FROM emp WHERE id in /*id*/(10, 20)";

        SqlTemplate template = templateEngine.getTemplateByText(sql);
        SqlTemplateContext context = new MapSqlTemplateContext(Map.of("id", List.of(1, 2)));
        ProcessResult result = template.process(context);

        assertThat(result.getSql()).isEqualTo("SELECT * FROM emp WHERE id in (?, ?)");
        assertThat(result.getParameters()).containsExactly(1, 2);

    }

    @Test
    void testParenBindVariable_array() {
        String sql = "SELECT * FROM emp WHERE id in /*id*/(10, 20)";

        SqlTemplate template = templateEngine.getTemplateByText(sql);
        SqlTemplateContext context = new MapSqlTemplateContext(Map.of("id", new int[] {1, 2}));
        ProcessResult result = template.process(context);

        assertThat(result.getSql()).isEqualTo("SELECT * FROM emp WHERE id in (?, ?)");
        assertThat(result.getParameters()).containsExactly(1, 2);

    }

    @Test
    void testParenBindVariable_object() {
        String sql = "SELECT * FROM emp WHERE id in /*id*/(10, 20)";

        SqlTemplate template = templateEngine.getTemplateByText(sql);
        SqlTemplateContext context = new MapSqlTemplateContext(Map.of("id", 1));
        ProcessResult result = template.process(context);

        assertThat(result.getSql()).isEqualTo("SELECT * FROM emp WHERE id in ?");
        assertThat(result.getParameters()).containsExactly(1);

    }

    @Test
    void testParenBindVariable_evalELError() {
        String sql = "SELECT * FROM emp WHERE id in /*id*/(10, 20)";

        SqlTemplate template = templateEngine.getTemplateByText(sql);
        SqlTemplateContext context = new MapSqlTemplateContext(/*Map.of("id", 1)*/);

        assertThatThrownBy(() -> template.process(context))
            .isInstanceOf(NodeProcessException.class)
            .hasCauseInstanceOf(ExpressionException.class)
            .hasMessageContaining("Fail evaluating expression 'id'.")
            .hasFieldOrPropertyWithValue("position", new Position(1, 32, sql));

    }

    @Test
    void testEmbeddedValue() {

        String sql = "SELECT * FROM emp limit /*$limit*/10 offset /*$offset*/5";

        SqlTemplate template = templateEngine.getTemplateByText(sql);
        SqlTemplateContext context = new MapSqlTemplateContext(Map.of("limit", 100, "offset", 10));
        ProcessResult result = template.process(context);

        assertThat(result.getSql()).isEqualTo("SELECT * FROM emp limit 100 offset 10");
        assertThat(result.getParameters()).isEmpty();

    }

    @Test
    void testEmbeddedValue_semicolon() {

        String sql = "SELECT * FROM emp limit /*$limit*/10 offset /*$offset*/5";

        SqlTemplate template = templateEngine.getTemplateByText(sql);
        SqlTemplateContext context = new MapSqlTemplateContext(Map.of("limit", 100, "offset", ";update"));

        assertThatThrownBy(() -> template.process(context))
            .isInstanceOf(NodeProcessException.class)
            .hasMessageContaining("Not allowed semicolon at embedded value 'offset' to ';update'.")
            .hasFieldOrPropertyWithValue("position", new Position(1, 47, sql));

    }

    @Test
    void testEmbeddedValue_evalELError() {

        String sql = "SELECT * FROM emp limit /*$limit*/10 offset /*$offset*/5";

        SqlTemplate template = templateEngine.getTemplateByText(sql);
        SqlTemplateContext context = new MapSqlTemplateContext(Map.of("limit", 100/*, "offset", 10*/));

        assertThatThrownBy(() -> template.process(context))
            .isInstanceOf(NodeProcessException.class)
            .hasCauseInstanceOf(ExpressionException.class)
            .hasMessageContaining("Fail evaluating expression 'offset'.")
            .hasFieldOrPropertyWithValue("position", new Position(1, 47, sql));

    }

    @Test
    void testIf_null() {

        String sql = "SELECT * FROM emp/*IF job != null*/ WHERE job = /*job*/'CLERK'/*END*/";

        SqlTemplate template = templateEngine.getTemplateByText(sql);
        SqlTemplateContext context = new MapSqlTemplateContext(Map.of("job", "Normal"));
        ProcessResult result = template.process(context);

        assertThat(result.getSql()).isEqualTo("SELECT * FROM emp WHERE job = ?");
        assertThat(result.getParameters()).containsExactly("Normal");

    }

    @Test
    void testIf_evalELError() {

        String sql = "SELECT * FROM emp/*IF job != null*/ WHERE job = /*job*/'CLERK'/*END*/";

        SqlTemplate template = templateEngine.getTemplateByText(sql);
        SqlTemplateContext context = new MapSqlTemplateContext(/*Map.of("job", "Normal")*/);

        assertThatThrownBy(() -> template.process(context))
            .isInstanceOf(NodeProcessException.class)
            .hasCauseInstanceOf(ExpressionException.class)
            .hasMessageContaining("Fail evaluating expression 'job != null'.")
            .hasFieldOrPropertyWithValue("position", new Position(1, 22, sql));

    }



    @Test
    void testIf_compare() {

        String sql = "SELECT * FROM emp/*IF age >= 1*/ WHERE age = /*age*/20/*END*/";

        SqlTemplate template = templateEngine.getTemplateByText(sql);

        {
            SqlTemplateContext context = new MapSqlTemplateContext(Map.of("age", 1));
            ProcessResult result = template.process(context);

            assertThat(result.getSql()).isEqualTo("SELECT * FROM emp WHERE age = ?");
            assertThat(result.getParameters()).containsExactly(1);

        }

        {
            SqlTemplateContext context = new MapSqlTemplateContext(Map.of("age", -1));
            ProcessResult result = template.process(context);

            assertThat(result.getSql()).isEqualTo("SELECT * FROM emp");
            assertThat(result.getParameters()).isEmpty();

        }

    }

    @Test
    void testElse() {

        String sql = "SELECT * FROM emp WHERE /*IF job != null*/job = /*job*/'CLERK'-- ELSE job is null/*END*/";

        SqlTemplate template = templateEngine.getTemplateByText(sql);

        {
            // ifの評価
            SqlTemplateContext context = new MapSqlTemplateContext(Map.of("job", "Normal"));
            ProcessResult result = template.process(context);

            assertThat(result.getSql()).isEqualTo("SELECT * FROM emp WHERE job = ?");
            assertThat(result.getParameters()).containsExactly("Normal");

        }

        {
            // elseの評価
            MapSqlTemplateContext context = new MapSqlTemplateContext();
            context.setVariable("job", null);
            ProcessResult result = template.process(context);

            assertThat(result.getSql()).isEqualTo("SELECT * FROM emp WHERE job is null");
            assertThat(result.getParameters()).isEmpty();

        }

    }

    @Test
    void testBegin() {

        String sql = "SELECT * FROM emp/*BEGIN*/ WHERE /*IF job != null*/job = /*job*/'CLERK'/*END*//*IF deptno != null*/ AND deptno = /*deptno*/20/*END*//*END*/";

        SqlTemplate template = templateEngine.getTemplateByText(sql);

        {
            // 全てのプロパティがnull
            MapSqlTemplateContext context = new MapSqlTemplateContext();
            context.setVariable("job", null);
            context.setVariable("deptno", null);

            ProcessResult result = template.process(context);

            assertThat(result.getSql()).isEqualTo("SELECT * FROM emp");
            assertThat(result.getParameters()).isEmpty();

        }

        {
            // １つのプロパティがnull
            MapSqlTemplateContext context = new MapSqlTemplateContext();
            context.setVariable("job", "Normal");
            context.setVariable("deptno", null);
            ProcessResult result = template.process(context);

            assertThat(result.getSql()).isEqualTo("SELECT * FROM emp WHERE job = ?");
            assertThat(result.getParameters()).containsExactly("Normal");

        }


    }

}
