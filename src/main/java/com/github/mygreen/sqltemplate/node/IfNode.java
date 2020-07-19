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
package com.github.mygreen.sqltemplate.node;

import org.springframework.core.style.ToStringCreator;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;

import lombok.Getter;
import lombok.Setter;


/**
 * {@code IF} コメント用の{@link Node}です。
 *
 * @author higa
 * @author T.TSUCHIE
 */
public class IfNode extends ContainerNode {

    /**
     * {@code IF}コメント内の式
     */
    @Getter
    private final String expression;

    private final Expression parsedExpression;

    /**
     * {@code ELSE}のノード
     */
    @Getter
    @Setter
    private ElseNode elseNode;

    /**
     * Creates n <code>IfNode</code> from a string expression.
     *
     * @param expression 式
     * @param expressionParser 式のパーサ
     */
    public IfNode(final String expression, final ExpressionParser expressionParser) {
        this.expression = expression;
        this.parsedExpression = expressionParser.parseExpression(expression);
    }

    @Override
    public void accept(final ProcessContext ctx) {

        EvaluationContext evaluationContext = ctx.getEvaluationContext();
        boolean result = parsedExpression.getValue(evaluationContext, boolean.class);

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
                .append("expression", expression)
                .append("parsedExpression", parsedExpression)
                .append("elseNode", elseNode)
                .append("children", children)
                .toString();
    }
}