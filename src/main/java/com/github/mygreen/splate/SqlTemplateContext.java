package com.github.mygreen.splate;

import org.springframework.expression.EvaluationContext;

import com.github.mygreen.splate.type.SqlTemplateValueType;
import com.github.mygreen.splate.type.SqlTemplateValueTypeRegistry;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

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
    private final SqlTemplateValueTypeRegistry valueTypeRegistry;

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

    public SqlTemplateContext() {
        this.valueTypeRegistry = new SqlTemplateValueTypeRegistry();
    }

    /**
     * {@link SqlTemplateValueTypeRegistry} を指定してインスタンスを作成します。
     *
     * @param valueTypeRegistry SQLテンプレートのパラメータの変換処理を管理する処理。
     */
    public SqlTemplateContext(@NonNull SqlTemplateValueTypeRegistry valueTypeRegistry) {
        this.valueTypeRegistry = new SqlTemplateValueTypeRegistry(valueTypeRegistry);
    }

    /**
     * {@link SqlTemplateValueType} を登録します。
     * @param <T> 関連付ける型
     * @param type 関連付けるクラスタイプ
     * @param valueType {@link SqlTemplateValueType}の実装
     */
    public <T> void registerValueType(@NonNull Class<T> type, @NonNull SqlTemplateValueType<T> valueType) {
        valueTypeRegistry.register(type, valueType);
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
        valueTypeRegistry.register(propertyPath, type, valueType);
    }

    /**
     * EL式を評価するときのコンテキストを作成します。
     * @return EL式を評価するときのコンテキスト。
     */
    public abstract EvaluationContext createEvaluationContext();



}
