package com.github.mygreen.sqltemplate;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * SQLテンプレートを実行した結果
 *
 * @author T.TSUCHIE
 *
 */
@RequiredArgsConstructor
public class ProcessResult {

    /**
     * SQL
     */
    @Getter
    private final String sql;

    /**
     * SQL中のバインドパラメータ
     */
    @Getter
    private final List<Object> parameters;
}
