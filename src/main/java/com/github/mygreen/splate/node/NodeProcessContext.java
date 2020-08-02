/*
 * Copyright 2004-2010 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.github.mygreen.splate.node;

import java.util.ArrayList;
import java.util.List;

import org.springframework.expression.EvaluationContext;

import com.github.mygreen.splate.SqlTemplateContext;
import com.github.mygreen.splate.type.SqlTemplateValueTypeRegistry;

import lombok.Getter;
import lombok.Setter;

/**
 * {@code SQLテンプレート}を評価するときのコンテキストです。 コンテキストで{@code SQL}を実行するのに必要な情報を組み立てた後、
 * {@code getSql()}, {@code getBindVariables()},
 * {@code getBindVariableTypes()}で、 情報を取り出して{@code SQL}を実行します。
 * {@code SQL}で{@code BEGIN}コメントと{@code END}コメントで囲まれている部分が、
 * 子供のコンテキストになります。 通常は、 {@code WHERE}句を{@code BEGIN}コメントと{@code END}コメントで囲み、
 * {@code WHERE}句の中の{@code IF}コメントが1つでも成立した場合、{@code enabled}になります。
 *
 * @version 0.2
 * @author higa
 *
 */
public class NodeProcessContext {

    /**
     * SQLテンプレートのコンテキスト
     */
    private final SqlTemplateContext templateContext;

    /**
     * 組み立てたSQL
     */
    private StringBuffer sqlBuf = new StringBuffer(255);

    /**
     * SQLテンプレート中の変数をバインドしたパラメータ
     */
    @Getter
    private List<Object> bindParams = new ArrayList<>();

    /**
     * {@code BEGIN} コメントと{@code END} コメントで、囲まれた子供のコンテキストが有効かどうか。
     */
    @Setter
    @Getter
    private boolean enabled = true;

    /**
     * 親のノードの情報。
     */
    @Getter
    private NodeProcessContext parent;

    /**
     * パースされた状態のSQLテンプレート。
     * エラー時のメッセージを出力するために使用します。
     */
    @Getter
    @Setter
    private String parsedSql;

    /**
     * テンプレートパラメータなどのSQLコンテキストを指定するコンストラクタ。
     * @param templateContext SQLテンプレートのコンテキスト
     */
    public NodeProcessContext(final SqlTemplateContext templateContext) {
        this.templateContext = templateContext;
    }

    /**
     * 親コンテキストを指定するコンストラクタ。
     *
     * @param parent 親のコンテキスト.
     */
    public NodeProcessContext(final NodeProcessContext parent) {
        this.parent = parent;
        this.enabled = false;

        // 各種情報の引継ぎ
        this.templateContext = parent.templateContext;
        this.parsedSql = parent.parsedSql;

    }

    /**
     * 処理済みの{@code SQL} を取得します。
     *
     * @return SQL
     */
    public String getProcessedSql() {
        return sqlBuf.toString();
    }

    /**
     * {@code SQL} を追加します。
     *
     * @param sql SQL
     */
    public void addSql(String sql) {
        sqlBuf.append(sql);
    }

    /**
     * {@code SQL} とバインド変数を追加します。
     * @param sql SQL
     * @param bindValue バインドする変数の値
     */
    public void addSql(String sql, Object bindValue) {
        sqlBuf.append(sql);
        bindParams.add(bindValue);
    }

    /**
     * {@code SQL} とバインド変数を追加します。
     * @param sql SQL
     * @param bindParams バインドする変数情報
     */
    public void addSql(final String sql, final List<Object> bindParams) {
        this.sqlBuf.append(sql);
        this.bindParams.addAll(bindParams);

    }

    /**
     * EL式で指定された時の式を評価するためのコンテキストを取得します。
     * @return EL式で指定された時の式を評価するためのコンテキスト
     */
    public EvaluationContext getEvaluationContext() {
        return templateContext.createEvaluationContext();
    }

    /**
     * SQLテンプレート中の変数を変換するための管理クラスを取得します。
     * @return SQLテンプレート中の変数を変換するための管理クラス
     */
    public SqlTemplateValueTypeRegistry getValueTypeRegistry() {
        return templateContext.getValueTypeRestRegistry();
    }
}