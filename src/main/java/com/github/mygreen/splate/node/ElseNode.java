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

/**
 * {@code ELSE} コメント用の{@link Node}です。
 *
 * @author higa
 * @author T.TSUCHIE
 */
public class ElseNode extends ContainerNode {

    public ElseNode(final int position) {
        super(position);
    }

    @Override
    public void accept(final NodeProcessContext ctx) {
        super.accept(ctx);
        ctx.setEnabled(true);
    }

    @Override
    public String toString() {
        return new ToStringCreator(this)
                .append("position", getPosition())
                .append("children", children)
                .toString();
    }
}