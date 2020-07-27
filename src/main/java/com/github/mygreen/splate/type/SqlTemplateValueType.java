package com.github.mygreen.sqltemplate.type;

import org.springframework.lang.Nullable;

/**
 * SQLテンプレート中のパラメータ（変数）の型とJDBCの型を橋渡しするためのインタフェースです。
 *
 *
 * @author T.TSUCHIE
 * @param <T> マッピング対象の型
 */
public interface SqlTemplateValueType<T> {

    /**
     * SQLのパラメータ変数として値を取得します。
     * <p>JDBCが対応していないタイプの場合は、対応している値に変換します。</p>
     *
     * @param value 変換する値
     * @return SQLのパラメータ変数。
     * @throws SqlTypeConversionException SQLで扱う型（JDBCの型）の変換にした場合にスローされます。
     */
    Object getBindVariableValue(@Nullable T value) throws SqlTypeConversionException;

    /**
     * SQLに直接埋め込む値として文字列に変換します。
     *
     * @param value 変換する値。非nullが渡されます。
     * @return 文字列に変換した値
     * @throws TextConversionException 値を文字列への変換に失敗したときにストローされます。
     */
    default String getEmbeddedValue(final @Nullable T value) throws TextConversionException {
        if(value == null) {
            return null;
        }
        return value.toString();
    }
}
