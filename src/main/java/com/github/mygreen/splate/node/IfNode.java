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

import lombok.Getter;
import lombok.Setter;


/**
 * {@code IF} コメント用の{@link Node}です。
 *
 * @version 0.2
 * @author higa
 * @author T.TSUCHIE
 */
public class IfNode extends ContainerNode {

    /**
     * {@code IF}コメント内の式
     */
    @Getter
    private final String expression;

    /**
     * パース済みの式
     */
    private final Expression parsedExpression;

    /**
     * {@code ELSE}のノード
     */
    @Getter
    @Setter
    private ElseNode elseNode;

    /**
     * 条件式を元に、Creates {@link IfNode}を作成します。
     *
     * @param position 位置情報
     * @param expression 式
     * @param parsedExpression パース済みの式
     */
    public IfNode(final int position, final String expression, final Expression parsedExpression) {
        super(position);
        this.expression = expression;
        this.parsedExpression = parsedExpression;
    }

    @Override
    public void accept(final ListParamNodeProcessContext ctx) {

        final EvaluationContext evaluationContext = ctx.getEvaluationContext();
        boolean result = evaluateExpression(parsedExpression, evaluationContext, boolean.class, getPosition(), ctx.getParsedSql());

        if (result) {
            super.accept(ctx);
            ctx.setEnabled(true);
        } else if (elseNode != null) {
            elseNode.accept(ctx);
            ctx.setEnabled(true);
        }
    }

    @Override
    public void accept(final NamedParamNodeProcessContext ctx) {

        final EvaluationContext evaluationContext = ctx.getEvaluationContext();
        boolean result = evaluateExpression(parsedExpression, evaluationContext, boolean.class, getPosition(), ctx.getParsedSql());

        if (result) {
            super.accept(ctx);
            ctx.setEnabled(true);
        } else if (elseNode != null) {
            elseNode.accept(ctx);
            ctx.setEnabled(true);
        }
    }

    @Override
    public String toString() {
        return new ToStringCreator(this)
                .append("position", getPosition())
                .append("expression", expression)
                .append("parsedExpression", parsedExpression)
                .append("elseNode", elseNode)
                .append("children", children)
                .toString();
    }
}