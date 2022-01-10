package com.github.mygreen.splate;

import com.github.mygreen.splate.node.ListParamNodeProcessContext;
import com.github.mygreen.splate.node.NamedParamNodeProcessContext;
import com.github.mygreen.splate.node.Node;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * パースしたSQLテンプレート情報を保持します。
 *
 *
 * @version 0.3
 * @author T.TSUCHIE
 *
 */
@RequiredArgsConstructor
public class SqlTemplate {

    /**
     * パースされたSQL。
     * <p>正規化によりトリムや最後のセミコロン({@literal ;})が削除されたものです。</p>
     */
    @Getter
    private final String sql;

    /**
     * SQLノード
     */
    @Getter
    private final Node node;

    /**
     * SQLテンプレートを評価します。
     *
     * @param templateContext SQLテンプレートに渡すコンテキスト。
     * @return SQLテンプレートを評価した結果。
     */
    public ProcessResult process(final SqlTemplateContext<?> templateContext) {

        final ListParamNodeProcessContext processContext = new ListParamNodeProcessContext(templateContext);
        processContext.setParsedSql(sql);

        // SQLテンプレートを評価します。
        node.accept(processContext);

        return new ProcessResult(processContext.getProcessedSql(), processContext.getBindParams());
    }

    /**
     * 名前付きパラメータでSQLテンプレートを評価します。
     *
     * @since 0.3
     * @param templateContext SQLテンプレートに渡すコンテキスト。
     * @return SQLテンプレートを評価した結果。
     */
    public NamedParamProcessResult processForNamedParam(final SqlTemplateContext<?> templateContext) {

        final NamedParamNodeProcessContext processContext = new NamedParamNodeProcessContext(templateContext);
        processContext.setParsedSql(sql);

        // SQLテンプレートを評価します。
        node.accept(processContext);

        return new NamedParamProcessResult(processContext.getProcessedSql(), processContext.getBindParams());

    }
}
