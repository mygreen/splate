package com.github.mygreen.splate;

import org.springframework.expression.EvaluationContext;

import com.github.mygreen.splate.type.SqlTemplateValueType;
import com.github.mygreen.splate.type.SqlTemplateValueTypeRegistry;

import lombok.Getter;
import lombok.NonNull;

/**
 * SQLテンプレートを実行し評価する際に渡すパラメータなどを保持するコンテキスト。
 *
 * @version 0.2
 * @author T.TSUCHIE
 *
 */
public abstract class SqlTemplateContext {

    /**
     * SQLテンプレートのパラメータの変換処理を管理する処理。
     */
    @Getter
    private final SqlTemplateValueTypeRegistry valueTypeRestRegistry;

    public SqlTemplateContext() {
        this.valueTypeRestRegistry = new SqlTemplateValueTypeRegistry();
    }

    /**
     * {@link SqlTemplateValueTypeRegistry} を指定してインスタンスを作成します。
     *
     * @param valueTypeRestRegistry SQLテンプレートのパラメータの変換処理を管理する処理。
     */
    public SqlTemplateContext(@NonNull SqlTemplateValueTypeRegistry valueTypeRestRegistry) {
        this.valueTypeRestRegistry = new SqlTemplateValueTypeRegistry(valueTypeRestRegistry);
    }

    /**
     * {@link SqlTemplateValueType} を登録します。
     * @param <T> 関連付ける型
     * @param type 関連付けるクラスタイプ
     * @param valueType {@link SqlTemplateValueType}の実装
     */
    public <T> void registerValueType(@NonNull Class<T> type, @NonNull SqlTemplateValueType<T> valueType) {
        valueTypeRestRegistry.register(type, valueType);
    }

    /**
     * プロパティのパスを指定して{@link SqlTemplateValueType} を登録します。
     * <p>SQLテンプレート中の変数（プロパティパス／式）を元に関連付ける再に使用します。
     *
     * @param <T> 関連付ける型
     * @param propertyPath プロパティパス／式
     * @param type 関連付けるクラスタイプ
     * @param valueType {@link SqlTemplateValueType}の実装
     */
    public <T> void registerValueType(@NonNull String propertyPath, @NonNull Class<T> type, @NonNull SqlTemplateValueType<T> valueType) {
        valueTypeRestRegistry.register(propertyPath, type, valueType);
    }

    /**
     * EL式を評価するときのコンテキストを作成します。
     * @return EL式を評価するときのコンテキスト。
     */
    public abstract EvaluationContext createEvaluationContext();



}
