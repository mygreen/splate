package com.github.mygreen.splate.parser;

import com.github.mygreen.splate.Position;
import com.github.mygreen.splate.TwoWaySqlException;

import lombok.Getter;

/**
 * SQLテンプレートのパース時のエラー。
 *
 * @since 0.2
 * @author T.TSUCHIE
 *
 */
public class SqlParseException extends TwoWaySqlException {

    /**
     * パースエラーが発生したテンプレート内での位置情報
     */
    @Getter
    private final Position position;

    /**
     * メッセージと原因となったエラーを使用して新しいインスタンスを構築します。
     *
     * @param position パースエラーが発生したテンプレート内での位置情報
     * @param message エラーメッセージ
     * @param cause 原因となったエラー
     */
    public SqlParseException(final Position position, String message, Throwable cause) {
        super(message, cause);
        this.position = position;
    }

    /**
     * メッセージ使用して新しいインスタンスを構築します。
     * @param position パースエラーが発生したテンプレート内での位置情報
     * @param message エラーメッセージ
     */
    public SqlParseException(final Position position, String message) {
        super(message);
        this.position = position;
    }

    @Override
    public String getMessage() {
        return super.getMessage()
                + System.getProperty("line.separator")
                + String.format("[row=%d, col=%d] %s", position.getRow(), position.getCol(), position.getLine());

    }

}
