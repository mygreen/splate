package com.github.mygreen.splate;

import com.github.mygreen.splate.node.Node;
import com.github.mygreen.splate.node.NodeProcessContext;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * パースしたSQLテンプレート情報を保持します。
 *
 *
 * @version 0.2
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
    public ProcessResult process(final SqlTemplateContext templateContext) {

        final NodeProcessContext processContext = new NodeProcessContext(templateContext);
        processContext.setParsedSql(sql);

        // SQLテンプレートを評価します。
        node.accept(processContext);

        return new ProcessResult(processContext.getProcessedSql(), processContext.getBindParams());
    }
}
