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

import lombok.Getter;

/**
 * {@code AND} や {@code OR} や {@code ','}のプレフィックスのための{@link Node}です。
 *
 *
 * @author higa
 */
public class PrefixSqlNode extends AbstractNode {

    /**
     * プレフィックス
     */
    @Getter
    private final String prefix;

    /**
     * SQL
     */
    @Getter
    private final String sql;

    /**
     * Creates a <code>PrefixSqlNode</code>
     *
     * @param prefix プレフィックス
     * @param sql SQL
     */
    public PrefixSqlNode(String prefix, String sql) {
        this.prefix = prefix;
        this.sql = sql;
    }


	@Override
    public void accept(final ProcessContext ctx) {
        if (ctx.isEnabled()) {
            ctx.addSql(prefix);
        }
        ctx.addSql(sql);
    }

    @Override
    public String toString() {
        return new ToStringCreator(this)
                .append("prefix", prefix)
                .append("sql", sql)
                .append("children", children)
                .toString();
    }
}