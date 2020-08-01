package com.github.mygreen.splate;

import com.github.mygreen.splate.node.Node;
import com.github.mygreen.splate.node.NodeProcessContext;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * パースしたSQLテンプレート情報を保持します。
 *
 *
 * @author T.TSUCHIE
 *
 */
@RequiredArgsConstructor
public class SqlTemplate {

    /**
     * SQLノード
     */
    @Getter
    private final Node node;

    /**
     * SQLテンプレートを評価します。
     *
     * @param sqlContext SQLテンプレートに渡すコンテキスト。
     * @return SQLテンプレートを評価した結果。
     */
    public ProcessResult process(final SqlTemplateContext sqlContext) {

        final NodeProcessContext processContext = new NodeProcessContext(sqlContext);

        // SQLテンプレートを評価します。
        node.accept(processContext);

        return new ProcessResult(processContext.getSql(), processContext.getBindParams());
    }
}
