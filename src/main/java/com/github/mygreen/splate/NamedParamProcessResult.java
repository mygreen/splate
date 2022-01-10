package com.github.mygreen.splate;

import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 名前付きパラメータでSQLテンプレートを評価した結果
 *
 * @since 0.3
 * @author T.TSUCHIE
 *
 */
@RequiredArgsConstructor
public class NamedParamProcessResult {

    /**
     * 評価されたSQL
     */
    @Getter
    private final String sql;

    /**
     * SQL中のバインドパラメータ
     */
    @Getter
    private final Map<String, Object> parameters;
}
