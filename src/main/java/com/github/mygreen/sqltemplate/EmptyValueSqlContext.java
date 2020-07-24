package com.github.mygreen.sqltemplate;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.PropertyAccessor;
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
    public PropertyAccessor createPropertyAccessor() {
        return new BeanWrapperImpl();
    }

    @Override
    public StandardEvaluationContext createEvaluationContext() {
        return new StandardEvaluationContext();
    }
}
