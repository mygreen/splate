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

import org.springframework.core.style.ToStringCreator;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;

import com.github.mygreen.splate.SqlUtils;
import com.github.mygreen.splate.type.SqlTemplateValueType;

import lombok.Getter;


/**
 * 値を埋め込む用の{@link Node}です
 *
 * @author higa
 * @author T.TSUCHIE
 */
public class EmbeddedValueNode extends AbstractNode {

    @Getter
    private final String expression;

    /**
     * パース済みの式
     */
    private final Expression parsedExpression;

    /**
     * {@link EmbeddedValueNode}のインスタンスを作成します。.
     *
     * @param position 位置情報
     * @param expression 式
     * @param parsedExpression パース済みの式
     */
    public EmbeddedValueNode(final int position, final String expression, final Expression parsedExpression) {
        super(position);
        this.expression = expression;
        this.parsedExpression = parsedExpression;
    }

    @Override
    public void accept(final NodeProcessContext ctx) {

        EvaluationContext evaluationContext = ctx.getEvaluationContext();
        Object value = evaluateExpression(parsedExpression, evaluationContext, Object.class, getPosition(), ctx.getParsedSql());

        if (value != null) {
            // SQLファイルに埋め込むために、文字列に変換する。
            final Class<?> clazz = parsedExpression.getValueType(evaluationContext);
            SqlTemplateValueType<?> valueType = ctx.getValueTypeRegistry().findValueType(clazz, expression);
            final String sql = getEmbeddedValue(value, valueType, getPosition(), ctx.getParsedSql(), expression);

            if (sql.indexOf(';') >= 0) {
                // SQLインジェクションの原因となるセミコロンが含まれる場合は例外をスローする。
                throw new NodeProcessException(SqlUtils.resolveSqlPosition(ctx.getParsedSql(), getPosition()),
                        String.format("Not allowed semicolon at embedded value '%s'.", sql));
            }
            ctx.addSql(sql);
        }
    }

    @Override
    public String toString() {
        return new ToStringCreator(this)
                .append("position", getPosition())
                .append("expression", expression)
                .append("parsedExpression", parsedExpression)
                .append("children", children)
                .toString();
    }
}