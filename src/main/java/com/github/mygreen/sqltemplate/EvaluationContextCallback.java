package com.github.mygreen.sqltemplate;

import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * EL式を評価する際の {@link StandardEvaluationContext} を設定するためのコールバック。
 * <p>独自の変数や関数を登録したいときに使用します。</p>
 *
 * @author T.TSUCHIE
 *
 */
@FunctionalInterface
public interface EvaluationContextCallback {

    /**
     * コールバック処理としての呼び出し。
     * @param evaluationContext EL式を評価する際のコンテキスト。
     */
    void call(StandardEvaluationContext evaluationContext);

}
