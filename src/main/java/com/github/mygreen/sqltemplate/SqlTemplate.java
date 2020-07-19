package com.github.mygreen.sqltemplate;

import com.github.mygreen.sqltemplate.node.Node;
import com.github.mygreen.sqltemplate.node.ProcessContext;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * SQLテンプレート。
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
     * SQLテンプレートを実行します。
     *
     * @param sqlContext SQLテンプレートに渡すコンテキスト。
     * @return SQLテンプレートの評価した結果。
     */
    public ProcessResult process(final SqlContext sqlContext) {

        final ProcessContext processContext = new ProcessContext(sqlContext);

        // SQLテンプレートを評価します。
        node.accept(processContext);

        return new ProcessResult(processContext.getSql(), processContext.getBindParams());
    }
}
