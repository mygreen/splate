package com.github.mygreen.splate;

import java.util.List;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.github.mygreen.splate.type.SqlTemplateValueTypeRegistry;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * SQLテンプレートのパラメータをJavaBean として渡すときのSQLテンプレートのコンテキスト。
 * SQLテンプレート中では、JavaBeanのプロパティ名で参照できます。
 *
 * @version 0.3
 * @author T.TSUCHIE
 *
 */
public class BeanPropertySqlTemplateContext extends SqlTemplateContext {

    /**
     * JavaBeanのインスタンス。
     */
    @Getter
    private final Object value;

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
     * JavaBeanを指定するコンストラクタ。
     * @param object SQLテンプレート中のパラメータとして渡すJavaBeanのインスタンス
     */
    public BeanPropertySqlTemplateContext(final @NonNull Object object) {
        super();
        this.value = object;
    }

    /**
     *  {@link SqlTemplateValueTypeRegistry}とJavaBeanを指定してインスタンスを作成します。
     *
     * @param valueTypeRegistry SQLテンプレートのパラメータの変換処理を管理する処理。
     * @param object JavaBeanのインスタンス
     */
    public BeanPropertySqlTemplateContext(SqlTemplateValueTypeRegistry valueTypeRegistry, final @NonNull Object object) {
        super(valueTypeRegistry);
        this.value = object;
    }

    /**
     * {@inheritDoc}
     * @return {@link StandardEvaluationContext} のインスタンスを返します。
     */
    @Override
    public EvaluationContext createEvaluationContext() {
        final StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
        evaluationContext.setPropertyAccessors(List.of(new CustomReflectivePropertyAccessor(ignoreNotFoundProperty)));
        evaluationContext.setRootObject(value);
        return evaluationContext;
    }
}
