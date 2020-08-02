package com.github.mygreen.splate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * テンプレートの位置情報を表す
 *
 * @since 0.2
 * @author T.TSUCHIE
 *
 */
@NoArgsConstructor
@AllArgsConstructor
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
