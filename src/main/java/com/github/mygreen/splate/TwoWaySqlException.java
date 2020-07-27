package com.github.mygreen.splate;

import org.springframework.core.NestedRuntimeException;


/**
 * 2Way-SQL テンプレート処理の例外。
 *
 * @author T.TSUCHIE
 *
 */
public class TwoWaySqlException extends NestedRuntimeException {

    /**
     * メッセージと原因となったエラーを指定しインスタンスを作成します。
     *
     * @param message メッセージ。
     * @param cause 原因となったエラー。
     */
    public TwoWaySqlException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * メッセージを指定しインスタンスを作成します。
     * @param message メッセージ。
     */
    public TwoWaySqlException(String message) {
        super(message);
    }
}
