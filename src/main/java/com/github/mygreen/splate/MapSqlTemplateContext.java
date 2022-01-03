package com.github.mygreen.splate;

import java.util.HashMap;
import java.util.Map;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.github.mygreen.splate.type.SqlTemplateValueTypeRegistry;

import lombok.NonNull;

/**
 * SQLテンプレートのパラメータを{@link Map} として渡すときのSQLテンプレートのコンテキスト。
 * SQLテンプレート中では、マップのキー名で参照できます。
 *
 *
 * @version 0.3
 * @author T.TSUCHIE
 *
 */
public class MapSqlTemplateContext extends SqlTemplateContext {

    private Map<String, Object> values = new HashMap<>();

    /**
     * コンストラクタ。
     */
    public MapSqlTemplateContext() {
        super();
    }

    /**
     * テンプレート中の変数を指定してインスタンスを作成します。
     * @param variables SQLテンプレート中のパラメータとして渡すマップ。
     */
    public MapSqlTemplateContext(@NonNull Map<String, Object> variables) {
        super();
        this.values.putAll(variables);
    }

    /**
     * {@link SqlTemplateValueTypeRegistry}を指定してインスタンスを作成します。
     * @param valueTypeRegistry SQLテンプレートのパラメータの変換処理を管理する処理。
     */
    public MapSqlTemplateContext(SqlTemplateValueTypeRegistry valueTypeRegistry) {
        super(valueTypeRegistry);
    }

    /**
     * {@link SqlTemplateValueTypeRegistry}とテンプレート中の変数を指定してインスタンスを作成します。
     * @param valueTypeRegistry SQLテンプレートのパラメータの変換処理を管理する処理。
     * @param variables SQLテンプレート中で使用可能な変数
     */
    public MapSqlTemplateContext(SqlTemplateValueTypeRegistry valueTypeRegistry, @NonNull Map<String, Object> variables) {
        super(valueTypeRegistry);
        this.values.putAll(variables);
    }

    @Override
    public EvaluationContext createEvaluationContext() {
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
        evaluationContext.addPropertyAccessor(new CustomMapAccessor(isIgnoreNotFoundProperty()));
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
