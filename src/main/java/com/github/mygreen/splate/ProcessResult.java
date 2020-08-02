package com.github.mygreen.splate;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * SQLテンプレートを評価した結果
 *
 * @author T.TSUCHIE
 *
 */
@RequiredArgsConstructor
public class ProcessResult {

    /**
     * 評価されたSQL
     */
    @Getter
    private final String sql;

    /**
     * SQL中のバインドパラメータ
     */
    @Getter
    private final List<Object> parameters;
}
