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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.LinkedList;

import org.springframework.core.style.ToStringCreator;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;

import com.github.mygreen.sqltemplate.type.SqlTemplateValueType;

import lombok.Getter;

/**
 * {@literal IN}のバインド変数用の{@link Node}です。
 *
 * @author higa
 * @author shuji.w6e
 * @author T.TSUCHIE
 */
public class ParenBindVariableNode extends AbstractNode {

    /**
     * 式
     */
    @Getter
    private final String expression;

    /**
     * パース済みの式
     */
    private final Expression parsedExpression;

    /**
     * {@link ParenBindVariableNode} を作成します。
     *
     * @param expression 式
     * @param expressionParser 式のパーサ
     */
    public ParenBindVariableNode(final String expression, final ExpressionParser expressionParser) {
        this.expression = expression;
        this.parsedExpression = expressionParser.parseExpression(expression);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void accept(final ProcessContext ctx) {

        final EvaluationContext evaluationContext = ctx.getEvaluationContext();
        final Object var = parsedExpression.getValue(evaluationContext);
        if(var == null) {
            return;
        }

        if (var instanceof Collection) {
            bindArray(ctx, Collection.class.cast(var).toArray());
        } else if (var instanceof Iterable) {
            bindArray(ctx, toArray(Iterable.class.cast(var)));
        } else if (var.getClass().isArray()) {
            bindArray(ctx, var);
        } else {
            // ただのオブジェクトの場合
            Class<?> clazz = parsedExpression.getValueType(evaluationContext);
            SqlTemplateValueType valueType = ctx.getValueTypeRegistry().findValueType(clazz, expression);
            ctx.addSql("?", var, valueType);
        }

    }

    /**
     * 配列に変換します。
     * @param iterable
     * @return
     */
    private Object[] toArray(Iterable<?> iterable) {
        LinkedList<Object> list = new LinkedList<>();
        for (Object o : iterable) {
            list.add(o);
        }
        return list.toArray();
    }

    /**
     * 配列に変換してバインドする。
     * @param ctx the SqlContext
     * @param array the variable array
     */
    @SuppressWarnings("rawtypes")
    private void bindArray(final ProcessContext ctx, final Object array) {
        int length = Array.getLength(array);
        if (length == 0) {
            return;
        }
        Class<?> clazz = null;
        for (int i = 0; i < length; ++i) {
            Object o = Array.get(array, i);
            if (o != null) {
                clazz = o.getClass();
            }
        }

        ctx.addSql("(");
        Object value = Array.get(array, 0);
        SqlTemplateValueType valueType = ctx.getValueTypeRegistry().findValueType(clazz, expression);

        ctx.addSql("?", value, valueType);
        for (int i = 1; i < length; ++i) {
            ctx.addSql(", ");
            value = Array.get(array, i);
            ctx.addSql("?", value, valueType);
        }
        ctx.addSql(")");
    }

    @Override
    public String toString() {
        return new ToStringCreator(this)
                .append("expression", expression)
                .append("parsedExpression", parsedExpression)
                .append("children", children)
                .toString();
    }
}