package com.github.mygreen.splate;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.github.mygreen.splate.type.SqlTemplateValueTypeRegistry;

import lombok.NonNull;

/**
 * SQLテンプレートのパラメータを{@link Map} として渡すときのSQLテンプレートのコンテキスト。
 * SQLテンプレート中では、マップのキー名で参照できます。
 *
 *
 * @author T.TSUCHIE
 *
 */
public class MapSqlTemplateContext extends SqlTemplateContext {

    private Map<String, Object> values = new HashMap<String, Object>();

    /**
     * コンストラクタ。
     */
    public MapSqlTemplateContext() {
        super();
    }

    /**
     * マップを指定するコンストラクタ。
     * @param variables SQLテンプレート中のパラメータとして渡すマップ。
     */
    public MapSqlTemplateContext(@NonNull Map<String, Object> variables) {
        super();
        this.values.putAll(variables);
    }

    /**
     *
     * @param valueTypeRestRegistry SQLテンプレートのパラメータの変換処理を管理する処理。
     */
    public MapSqlTemplateContext(SqlTemplateValueTypeRegistry valueTypeRestRegistry) {
        super(valueTypeRestRegistry);
    }

    /**
     *
     * @param valueTypeRestRegistry SQLテンプレートのパラメータの変換処理を管理する処理。
     * @param variables SQLテンプレート中で使用可能な変数
     */
    public MapSqlTemplateContext(SqlTemplateValueTypeRegistry valueTypeRestRegistry, @NonNull Map<String, Object> variables) {
        super(valueTypeRestRegistry);
        this.values.putAll(variables);
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

    /**
     * SQLテンプレート中で使用可能な変数を追加します。
     *
     * @param variables 変数のマップ
     */
    public void setVariables(@NonNull Map<String, Object> variables) {
        this.values.putAll(variables);
    }


}
