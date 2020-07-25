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

import org.springframework.beans.PropertyAccessor;
import org.springframework.core.style.ToStringCreator;

import com.github.mygreen.sqltemplate.TwoWaySqlException;
import com.github.mygreen.sqltemplate.type.SqlTemplateValueType;

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
     * Creates a <code>EmbeddedValueNode</code> from a string expression.
     *
     * @param expression the string expression to create the node from.
     */
    public EmbeddedValueNode(String expression) {
        this.expression = expression;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void accept(final ProcessContext ctx) {

        final PropertyAccessor accessor = ctx.getPropertyAccessor();
        Object value = accessor.getPropertyValue(expression);

        if (value != null) {
            // SQLファイルに埋め込むために、文字列に変換する。
            Class<?> clazz = accessor.getPropertyType(expression);
            SqlTemplateValueType valueType = ctx.getValueTypeRegistry().findValueType(clazz, expression);

            final String sql = valueType != null ? valueType.getEmbeddedValue(value) : value.toString();
            if (sql.indexOf(';') >= 0) {
                throw new TwoWaySqlException("semicolon is not allowed.");
            }
            ctx.addSql(sql);
        }
    }

    @Override
    public String toString() {
        return new ToStringCreator(this)
                .append("expression", expression)
                .append("children", children)
                .toString();
    }
}