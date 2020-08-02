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
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;

import com.github.mygreen.splate.SqlUtils;
import com.github.mygreen.splate.type.SqlTemplateValueType;
import com.github.mygreen.splate.type.SqlTypeConversionException;
import com.github.mygreen.splate.type.TextConversionException;

/**
 * {@link Node}の抽象クラスです。
 *
 * @author higa
 * @author T.TSUCHIE
 *
 */
public abstract class AbstractNode implements Node {

    /**
     * 子ノードのリスト
     */
    protected List<Node> children = new ArrayList<>();

    /**
     * テンプレート内での位置情報
     */
    protected final int position;

    /**
     * コンストラクタ
     *
     * @param position テンプレート内での位置
     */
    public AbstractNode(final int position) {
        this.position = position;
    }

    @Override
    public int getChildSize() {
        return children.size();
    }

    @Override
    public Node getChild(int index) {
        return children.get(index);
    }

    @Override
    public void addChild(Node node) {
        children.add(node);
    }

    @Override
    public int getPosition() {
        return position;
    }

    /**
     * EL式を評価します。
     * <p>例外処理を含めて共通化のために切り出したメソッドです。</p>
     *
     * @since 0.2
     * @param <T> 戻り値のタイプ
     * @param expression EL式
     * @param evaluationContext EL式のコンテキスト
     * @param requriedType EL式の戻り値
     * @param position テンプレートの位置情報
     * @param parsedSql パース済みのSQLテンプレート
     * @return EL式の評価結果
     * @throws NodeProcessException EL式の評価に失敗した場合にスローされます。
     */
    protected <T> T evaluateExpression(final Expression expression, final EvaluationContext evaluationContext,
            final Class<T> requriedType, final int position, final String parsedSql) {

        try {
            return expression.getValue(evaluationContext, requriedType);
        } catch(EvaluationException e) {
            throw new NodeProcessException(SqlUtils.resolveSqlPosition(parsedSql, position),
                    String.format("Fail evaluating expression '%s'.", expression.getExpressionString()),
                    e);
        }
    }

    /**
     * 変換規則を元にバインド変数を変換します。
     * <p>例外処理を含めて共通化のために切り出したメソッドです。</p>
     * <p>変換規則がnullの場合は、変換対象の値をそのまま返します。</p>
     *
     * @since 0.2
     * @param value 変換対象の値。
     * @param valueType 変換規則
     * @param position テンプレートの位置情報
     * @param parsedSql パース済みのSQLテンプレート
     * @param expression 変換対象の値の元となったEL式
     * @return 変換した値。
     * @throws NodeProcessException 変換時の処理に失敗した場合にスローされます。
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Object getBindVariableValue(final Object value, SqlTemplateValueType valueType,
            final int position, final String parsedSql, final String expression) {

        if(valueType == null) {
            return value;
        }

        try {
            return valueType.getBindVariableValue(value);
        } catch(SqlTypeConversionException e) {
            throw new NodeProcessException(SqlUtils.resolveSqlPosition(parsedSql, position),
                    String.format("Fail converting value of expression '%s'.", expression),
                    e);
        }

    }

    /**
     * 変換規則を元に埋め込み変数を変換します。
     * <p>例外処理を含めて共通化のために切り出したメソッドです。</p>
     * <p>変換規則がnullの場合は、変換対象の値をそのまま返します。</p>
     *
     * @since 0.2
     * @param value 変換対象の値。
     * @param valueType 変換規則
     * @param position テンプレートの位置情報
     * @param parsedSql パース済みのSQLテンプレート
     * @param expression 変換対象の値の元となったEL式
     * @return 変換した値。
     * @throws NodeProcessException 変換時の処理に失敗した場合にスローされます。
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected String getEmbeddedValue(final Object value, SqlTemplateValueType valueType,
            final int position, final String parsedSql, final String expression) {

        if(valueType == null) {
            return value.toString();
        }

        try {
            return valueType.getEmbeddedValue(value);
        } catch(TextConversionException e) {
            throw new NodeProcessException(SqlUtils.resolveSqlPosition(parsedSql, position),
                    String.format("Fail converting value of expression '%s'.", expression),
                    e);
        }

    }

}
