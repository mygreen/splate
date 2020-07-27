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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.core.style.ToStringCreator;

/**
 * {@link Node} representing the <code>WHERE</code> clause of an SQL.
 *
 * @author higa
 * @author T.TSUCHIE
 */
public class AddWhereIfNode extends ContainerNode {

    private static Pattern pat = Pattern.compile("\\s*(order\\sby)|$)");

    public AddWhereIfNode() {
    }

    @Override
    public void accept(final ProcessContext ctx) {

        ProcessContext childCtx = new ProcessContext(ctx);
        super.accept(childCtx);
        if (childCtx.isEnabled()) {
            String sql = childCtx.getSql();
            Matcher m = pat.matcher(sql);
            if (!m.lookingAt()) {
                sql = " WHERE " + sql;
            }
            ctx.addSql(sql, childCtx.getBindParams());
        }
    }

    @Override
    public String toString() {
        return new ToStringCreator(this)
                .append("children", children)
                .toString();
    }
}