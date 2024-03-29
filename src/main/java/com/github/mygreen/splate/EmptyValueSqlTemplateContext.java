package com.github.mygreen.splate;

import java.util.List;

import org.springframework.expression.spel.support.StandardEvaluationContext;

import lombok.Getter;
import lombok.Setter;

/**
 * SQLテンプレートに渡すパラメータがないときのSQLテンプレートのコンテキスト。
 *
 *
 * @version 0.3
 * @author T.TSUCHIE
 *
 */
public class EmptyValueSqlTemplateContext extends SqlTemplateContext<StandardEvaluationContext> {

    /**
     * SQLテンプレート中に存在しないプロパティが定義されているとき、{@literal null} として無視するかどうか。
     *
     * @since 0.3
     * @param ignoreNotFoundProperty SQLテンプレート中に存在しないプロパティが定義されているとき、{@literal null} として無視するかどうか設定します。
     * @return SQLテンプレート中に存在しないプロパティが定義されているとき、{@literal null} として無視するかどうか返します。
     */
    @Setter
    @Getter
    private boolean ignoreNotFoundProperty;

    /**
     * {@inheritDoc}
     * @return {@link StandardEvaluationContext} のインスタンスを返します。
     */
    @Override
    public StandardEvaluationContext createEvaluationContext() {
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
        evaluationContext.setPropertyAccessors(List.of(new CustomReflectivePropertyAccessor(ignoreNotFoundProperty)));
        return evaluationContext;
    }
}
