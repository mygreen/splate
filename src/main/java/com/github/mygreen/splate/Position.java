package com.github.mygreen.splate;

import lombok.Data;

/**
 * テンプレートの位置情報を表す
 *
 * @since 0.2
 * @author T.TSUCHIE
 *
 */
@Data
public class Position {

    /**
     * 行（1から始まる）
     */
    private int row;

    /**
     * 列（1から始まる）
     */
    private int col;

    /**
     * 行の文字列
     */
    private String line;

}
