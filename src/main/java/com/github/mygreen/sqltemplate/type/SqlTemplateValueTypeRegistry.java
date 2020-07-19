package com.github.mygreen.sqltemplate.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;


/**
 * {@link SqlTemplateValueType}
 *
 *
 * @author T.TSUCHIE
 *
 */
public class SqlTemplateValueTypeRegistry implements Cloneable {

    /**
     * クラスタイプで関連付けられた{@link SqlTemplateValueType}のマップ
     */
    private Map<Class<?>, SqlTemplateValueType<?>> typeMap = new ConcurrentHashMap<>();

    /**
     * パスで関連づけられた{@link SqlTemplateValueType}のマップ
     */
    private Map<String, ValueTypeHolder> pathMap = new ConcurrentHashMap<>();

    /**
     * 自身のインスタンスのクローンを作成します。
     * <p>登録されている{@link SqlTemplateValueType}をシャロ―コピーします。</p>
     */
    @Override
    public SqlTemplateValueTypeRegistry clone() {
        SqlTemplateValueTypeRegistry copy = new SqlTemplateValueTypeRegistry();
        copy.typeMap.putAll(this.typeMap);
        copy.pathMap.putAll(pathMap);

        return copy;
    }

    /**
     * プロパティパスに対応した値の変換処理を取得します。
     * @param requiredType プロパティのクラスタイプ。
     * @param propertyPath プロパティのパス。
     * @return 対応する変換処理の実装を返します。見つからない場合は {@literal null} を返しまsう。
     */
    public SqlTemplateValueType<?> findValueType(@NonNull Class<?> requiredType, String propertyPath) {

        // 完全なパスで比較
        if(pathMap.containsKey(propertyPath)) {
            return pathMap.get(propertyPath).get(requiredType);
        }

        // インデックスを除去した形式で比較
        final List<String> strippedPaths = new ArrayList<>();
        addStrippedPropertyPaths(strippedPaths, "", propertyPath);
        for(String strippedPath : strippedPaths) {
            SqlTemplateValueType<?> valueType = pathMap.get(strippedPath).get(requiredType);
            if(valueType != null) {
                return valueType;
            }
        }

        // 見つからない場合は、クラスタイプで比較
        if(typeMap.containsKey(requiredType)) {
            return typeMap.get(requiredType);
        }

        return null;

    }

    /**
     * {@link SqlTemplateValueType} を登録します。
     * @param <T> 関連付ける型
     * @param type 関連付けるクラスタイプ
     * @param valueType {@link ValueType}の実装
     */
    public <T> void register(@NonNull Class<T> type, @NonNull SqlTemplateValueType<T> valueType) {
        this.typeMap.put(type, valueType);
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
    public <T> void register(@NonNull String propertyPath, @NonNull Class<T> type, @NonNull SqlTemplateValueType<T> valueType) {
        this.pathMap.put(propertyPath, new ValueTypeHolder(type, valueType));
    }

    /**
     * パスからリストのインデックス([1])やマップのキー([key])を除去したものを構成する。
     * <p>SpringFrameworkの「PropertyEditorRegistrySupport#addStrippedPropertyPaths(...)」の処理</p>
     * @param strippedPaths 除去したパス
     * @param nestedPath 現在のネストしたパス
     * @param propertyPath 処理対象のパス
     */
    protected void addStrippedPropertyPaths(List<String> strippedPaths, String nestedPath, String propertyPath) {

        final int startIndex = propertyPath.indexOf('[');
        if (startIndex != -1) {
            final int endIndex = propertyPath.indexOf(']');
            if (endIndex != -1) {
                final String prefix = propertyPath.substring(0, startIndex);
                final String key = propertyPath.substring(startIndex, endIndex + 1);
                final String suffix = propertyPath.substring(endIndex + 1, propertyPath.length());

                // Strip the first key.
                strippedPaths.add(nestedPath + prefix + suffix);

                // Search for further keys to strip, with the first key stripped.
                addStrippedPropertyPaths(strippedPaths, nestedPath + prefix, suffix);

                // Search for further keys to strip, with the first key not stripped.
                addStrippedPropertyPaths(strippedPaths, nestedPath + prefix + key, suffix);
            }
        }
    }

    @RequiredArgsConstructor
    private static class ValueTypeHolder {

        private final Class<?> registeredType;

        private final SqlTemplateValueType<?> valueType;

        SqlTemplateValueType<?> get(final Class<?> requiredType) {
            if(registeredType.isAssignableFrom(requiredType)) {
                return valueType;
            }

            return null;
        }

    }
}
