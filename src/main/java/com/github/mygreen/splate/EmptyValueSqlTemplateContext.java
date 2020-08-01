package com.github.mygreen.splate;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * SQLテンプレートに渡すパラメータがないときのSQLテンプレートのコンテキスト。
 *
 *
 * @version 0.2
 * @author T.TSUCHIE
 *
 */
public class EmptyValueSqlTemplateContext extends SqlTemplateContext {

    @Override
    public EvaluationContext createEvaluationContext() {
        return new StandardEvaluationContext();
    }
}
