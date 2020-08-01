package com.github.mygreen.splate;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.github.mygreen.splate.type.SqlTemplateValueTypeRegistry;

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
public class BeanPropertySqlTemplateContext extends SqlTemplateContext {

    /**
     * JavaBeanのインスタンス。
     */
    @Getter
    private final Object value;

    /**
     * JavaBeanを指定するコンストラクタ。
     * @param object SQLテンプレート中のパラメータとして渡すJavaBeanのインスタンス
     */
    public BeanPropertySqlTemplateContext(final @NonNull Object object) {
        super();
        this.value = object;
    }

    /**
     * @param valueTypeRestRegistry SQLテンプレートのパラメータの変換処理を管理する処理。
     * @param object JavaBeanのインスタンス
     */
    public BeanPropertySqlTemplateContext(SqlTemplateValueTypeRegistry valueTypeRestRegistry, final @NonNull Object object) {
        super(valueTypeRestRegistry);
        this.value = object;
    }

    @Override
    public EvaluationContext createEvaluationContext() {
        final StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
        evaluationContext.setRootObject(value);
        return evaluationContext;
    }
}
