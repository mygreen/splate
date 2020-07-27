package com.github.mygreen.sqltemplate.type;

import com.github.mygreen.sqltemplate.TwoWaySqlException;

import lombok.Getter;

/**
 * 値をSQL型に変換する際に失敗したときにスローされる例外です。
 *
 *
 * @author T.TSUCHIE
 *
 */
public class SqlTypeConversionException extends TwoWaySqlException {

    /**
     * 変換対象の値
     */
    @Getter
    private final Object targetValue;

    /**
     * メッセージを指定してインスタンスと作成します。
     * @param targetValue 変換対象の値
     * @param message メッセージ
     */
    public SqlTypeConversionException(Object targetValue, String message) {
        super(message);
        this.targetValue = targetValue;
    }

    /**
     * メッセージと例外を指定してインスタンスを作成します。
     * @param targetValue 変換対象の値
     * @param message メッセージ
     * @param cause 原因となる例外
     */
    public SqlTypeConversionException(Object targetValue, String message, Throwable cause) {
        super(message, cause);
        this.targetValue = targetValue;
    }
}
