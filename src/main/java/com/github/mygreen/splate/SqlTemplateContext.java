package com.github.mygreen.splate;

import java.util.Optional;
import java.util.function.Consumer;

import org.springframework.expression.EvaluationContext;

import com.github.mygreen.splate.type.SqlTemplateValueType;
import com.github.mygreen.splate.type.SqlTemplateValueTypeRegistry;

import lombok.Getter;
import lombok.NonNull;

/**
 * SQLテンプレートを実行し評価する際に渡すパラメータなどを保持するコンテキスト。
 *
 * @version 0.3
 * @author T.TSUCHIE
 *
 * @param <C> EL式を評価するときのコンテキストのタイプ。
 *
 */
public abstract class SqlTemplateContext<C extends EvaluationContext> {

    /**
     * SQLテンプレートのパラメータの変換処理を管理する処理。
     * @return SQLテンプレートのパラメータの変換処理を管理する処理を返します。
     */
    @Getter
    private final SqlTemplateValueTypeRegistry valueTypeRegistry;

    /**
     * {@link EvaluationContext}を編集する処理。
     * @return {@link EvaluationContext}を編集する処理を返します。
     */
    @Getter
    private Optional<Consumer<C>> evaluationContextEditor = Optional.empty();

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
    public abstract C createEvaluationContext();

    /**
     * {@link EvaluationContext} を編集するコールバック処理を設定します。
     * <p>SQLテンプレートを評価する際に、{@link #createEvaluationContext} で作成した{@link EvaluationContext} が引数として渡されます。
     *
     * @since 0.3
     * @param editor 編集処理。
     */
    public void setEvaluationContextEditor(@NonNull Consumer<C> editor) {
        this.evaluationContextEditor = Optional.of(editor);
    }
}
