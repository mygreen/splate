package com.github.mygreen.sqltemplate;

import org.springframework.beans.DirectFieldAccessor;
import org.springframework.beans.PropertyAccessor;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.github.mygreen.sqltemplate.type.SqlTemplateValueTypeRegistry;

import lombok.Getter;
import lombok.NonNull;

/**
 * SQLテンプレートのパラメータをJavaBean として渡すときのSQLコンテキスト。
 * SQLテンプレート中では、JavaBeanのプロパティ名で参照できます。
 *
 *
 * @author T.TSUCHIE
 *
 */
public class BeanPropertySqlContext extends SqlContext {

    @Getter
    private final Object value;

    /**
     *
     * @param object JavaBeanのインスタンス
     */
    public BeanPropertySqlContext(final @NonNull Object object) {
        super();
        this.value = object;
    }

    /**
     * @param valueTypeRestRegistry SQLテンプレートのパラメータの変換処理を管理する処理。
     * @param object JavaBeanのインスタンス
     */
    public BeanPropertySqlContext(SqlTemplateValueTypeRegistry valueTypeRestRegistry, final @NonNull Object object) {
        super(valueTypeRestRegistry);
        this.value = object;
    }

    @Override
    public PropertyAccessor createPropertyAccessor() {
        return new DirectFieldAccessor(value);
    }

    @Override
    public EvaluationContext createEvaluationContext() {
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
        evaluationContext.setRootObject(value);
        return evaluationContext;
    }
}
