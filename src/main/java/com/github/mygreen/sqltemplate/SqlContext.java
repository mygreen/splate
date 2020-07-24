package com.github.mygreen.sqltemplate;

import java.util.Optional;

import org.springframework.beans.PropertyAccessor;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.github.mygreen.sqltemplate.type.SqlTemplateValueType;
import com.github.mygreen.sqltemplate.type.SqlTemplateValueTypeRegistry;

import lombok.Getter;
import lombok.NonNull;

/**
 * SQLテンプレートを実行し評価する際に渡すパラメータなどを保持するコンテキスト。
 *
 * @author T.TSUCHIE
 *
 */
public abstract class SqlContext {

    /**
     * SQLテンプレートのパラメータの変換処理を管理する処理。
     */
    @Getter
    private final SqlTemplateValueTypeRegistry valueTypeRestRegistry;

    /**
     * EL式を評価する際の{@link StandardEvaluationContext} を作成する際のコールバック処理。
     */
    @Getter
    private Optional<EvaluationContextCallback> evaluationContextCallback = Optional.empty();

    public SqlContext() {
        this.valueTypeRestRegistry = new SqlTemplateValueTypeRegistry();
    }

    /**
     * {@link SqlTemplateValueTypeRegistry} を指定してインスタンスを作成します。
     *
     * @param valueTypeRestRegistry SQLテンプレートのパラメータの変換処理を管理する処理。
     */
    public SqlContext(@NonNull SqlTemplateValueTypeRegistry valueTypeRestRegistry) {
        this.valueTypeRestRegistry = valueTypeRestRegistry.clone();
    }

    /**
     * {@link SqlTemplateValueType} を登録します。
     * @param <T> 関連付ける型
     * @param type 関連付けるクラスタイプ
     * @param valueType {@link ValueType}の実装
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
     * @param valueType {@link TypeValue}の実装
     */
    public <T> void registerValueType(@NonNull String propertyPath, @NonNull Class<T> type, @NonNull SqlTemplateValueType<T> valueType) {
        valueTypeRestRegistry.register(propertyPath, type, valueType);
    }

    /**
     * EL式を評価する際の{@link StandardEvaluationContext} を作成する際のコールバック処理。
     * @param callback コールバック処理
     */
    public void setEvaluationContextCallback(final EvaluationContextCallback callback) {
        this.evaluationContextCallback = Optional.ofNullable(callback);
    }

    /**
     * SQLテンプレートに渡す変数のアクセッサを作成します。
     * @return SQLテンプレートに渡す変数のアクセッサ。
     */
    public abstract PropertyAccessor createPropertyAccessor();

    /**
     * EL式を評価するときのコンテキストを作成します。
     * @return EL式を評価するときのコンテキスト。
     */
    public abstract StandardEvaluationContext createEvaluationContext();



}
