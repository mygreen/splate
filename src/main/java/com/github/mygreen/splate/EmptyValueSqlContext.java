package com.github.mygreen.splate;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * SQLテンプレートに渡すパラメータがないときのコンテキスト。
 *
 *
 * @author T.TSUCHIE
 *
 */
public class EmptyValueSqlContext extends SqlContext {

    @Override
    public EvaluationContext createEvaluationContext() {
        return new StandardEvaluationContext();
    }
}
