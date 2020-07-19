package com.github.mygreen.sqltemplate;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.PropertyAccessor;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.github.mygreen.sqltemplate.type.SqlTemplateValueTypeRegistry;

import lombok.NonNull;

/**
 * SQLテンプレートのパラメータを{@link Map} として渡すときのSQLコンテキスト。
 * SQLテンプレート中では、マップのキー名で参照できます。
 *
 *
 * @author T.TSUCHIE
 *
 */
public class MapSqlContext extends SqlContext {

    private Map<String, Object> values = new HashMap<String, Object>();

    public MapSqlContext() {
        super();
    }

    /**
     *
     * @param variables SQLテンプレート中で使用可能な変数
     */
    public MapSqlContext(@NonNull Map<String, Object> variables) {
        super();
        this.values.putAll(variables);
    }

    /**
     *
     * @param valueTypeRestRegistry SQLテンプレートのパラメータの変換処理を管理する処理。
     */
    public MapSqlContext(SqlTemplateValueTypeRegistry valueTypeRestRegistry) {
        super(valueTypeRestRegistry);
    }

    /**
     *
     * @param valueTypeRestRegistry SQLテンプレートのパラメータの変換処理を管理する処理。
     * @param variables SQLテンプレート中で使用可能な変数
     */
    public MapSqlContext(SqlTemplateValueTypeRegistry valueTypeRestRegistry, @NonNull Map<String, Object> variables) {
        super(valueTypeRestRegistry);
        this.values.putAll(variables);
    }

    @Override
    public PropertyAccessor createPropertyAccessor() {
        return new MapPropertyAccessor(values);
    }

    @Override
    public EvaluationContext createEvaluationContext() {
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
        evaluationContext.addPropertyAccessor(new MapAccessor());
        evaluationContext.setRootObject(values);
        return evaluationContext;
    }

    /**
     * SQLテンプレート中で使用可能な変数を追加します。
     *
     * @param name 変数名
     * @param value 値
     */
    public void setVariable(@NonNull String name, Object value) {
        this.values.put(name, value);
    }


}
