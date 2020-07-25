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
package com.github.mygreen.sqltemplate.parser;

import java.util.Stack;

import org.springframework.expression.ExpressionParser;

import com.github.mygreen.sqltemplate.SqlUtils;
import com.github.mygreen.sqltemplate.TwoWaySqlException;
import com.github.mygreen.sqltemplate.node.BeginNode;
import com.github.mygreen.sqltemplate.node.BindVariableNode;
import com.github.mygreen.sqltemplate.node.ContainerNode;
import com.github.mygreen.sqltemplate.node.ElseNode;
import com.github.mygreen.sqltemplate.node.EmbeddedValueNode;
import com.github.mygreen.sqltemplate.node.IfNode;
import com.github.mygreen.sqltemplate.node.Node;
import com.github.mygreen.sqltemplate.node.ParenBindVariableNode;
import com.github.mygreen.sqltemplate.node.PrefixSqlNode;
import com.github.mygreen.sqltemplate.node.SqlNode;
import com.github.mygreen.sqltemplate.parser.SqlTokenizer.TokenType;

/**
 * SQLを解析して<code>Node</code>のツリーにするクラスです。
 *
 * @author higa
 */
public class SqlParser {

    /**
     * SQLテンプレートの字句解析処理。
     */
    private final SqlTokenizer tokenizer;

    /**
     * EL式のパーサ
     */
    private final ExpressionParser expressionParser;

    private final Stack<Node> nodeStack = new Stack<>();

    /**
     * {@link SqlParser}を作成します。
     *
     * @param sql SQL
     * @param expressionParser {@literal IF}コメント名の式を処理するためのEL式のパーサです。
     */
    public SqlParser(final String sql,final ExpressionParser expressionParser) {
        this.tokenizer = new SqlTokenizer(normalizeSql(sql));
        this.expressionParser = expressionParser;
    }

    /**
     * パース対象のSQLをトリムなど行い正規化します。
     * @param sql パース対象のSQL
     * @return 正規化したSQL
     */
    protected String normalizeSql(final String sql) {
        String result = sql;
        result = result.strip();
        if (result.endsWith(";")) {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }

    /**
     *
     * @return SQLを解析して<code>Node</code>のツリーを返します。
     */
    public Node parse() {
        push(new ContainerNode());
        while (TokenType.EOF != tokenizer.next()) {
            parseToken();
        }
        return pop();
    }

	/**
     * トークンを解析します。
     */
    protected void parseToken() {
        switch (tokenizer.getTokenType()) {
        case SQL:
            parseSql();
            break;
        case COMMENT:
            parseComment();
            break;
        case ELSE:
            parseElse();
            break;
        case BIND_VARIABLE:
            parseBindVariable();
            break;
        default:
            break;
        }
    }

    /**
     * SQLを解析します。
     */
    protected void parseSql() {
        String sql = tokenizer.getToken();
        if (isElseMode()) {
            sql = SqlUtils.replace(sql, "--", "");
        }
        Node node = peek();
        if ((node instanceof IfNode || node instanceof ElseNode)
                && node.getChildSize() == 0) {

            SqlTokenizer st = new SqlTokenizer(sql);
            st.skipWhitespace();
            String token = st.skipToken();
            st.skipWhitespace();
            if (sql.startsWith(",")) {
                if (sql.startsWith(", ")) {
                    node.addChild(new PrefixSqlNode(", ", sql.substring(2)));
                } else {
                    node.addChild(new PrefixSqlNode(",", sql.substring(1)));
                }
            } else if ("AND".equalsIgnoreCase(token)
                    || "OR".equalsIgnoreCase(token)) {
                node.addChild(new PrefixSqlNode(st.getBefore(), st.getAfter()));
            } else {
                node.addChild(new SqlNode(sql));
            }
        } else {
            node.addChild(new SqlNode(sql));
        }
    }

    /**
     * コメントを解析します。
     */
    protected void parseComment() {
        String comment = tokenizer.getToken();
        if (isTargetComment(comment)) {
            if (isIfComment(comment)) {
                parseIf();
            } else if (isBeginComment(comment)) {
                parseBegin();
            } else if (isEndComment(comment)) {
                return;
            } else {
                parseCommentBindVariable();
            }
        } else if(isHintComment(comment)){
            peek().addChild(new SqlNode("/*" + comment + "*/"));
        }
    }

    /**
     * {@code IF} 句を解析します。
     */
    protected void parseIf() {
        String condition = tokenizer.getToken().substring(2).trim();
        if (SqlUtils.isEmpty(condition)) {
            throw new TwoWaySqlException("If condition not found.");
        }
        IfNode ifNode = new IfNode(condition, expressionParser);
        peek().addChild(ifNode);
        push(ifNode);
        parseEnd();
    }

    /**
     * {@code BEGIN} 句を解析します。
     */
    protected void parseBegin() {
        BeginNode beginNode = new BeginNode();
        peek().addChild(beginNode);
        push(beginNode);
        parseEnd();
    }

    /**
     * {@code END} 句を解析します。
     */
    protected void parseEnd() {
        while (TokenType.EOF != tokenizer.next()) {
            if (tokenizer.getTokenType() == TokenType.COMMENT
                    && isEndComment(tokenizer.getToken())) {

                pop();
                return;
            }
            parseToken();
        }
        throw new TwoWaySqlException(String.format(
                "END comment of %s not found.", tokenizer.getSql()));
    }

    /**
     * {@code ELSE} 句を解析します。
     */
    protected void parseElse() {
        Node parent = peek();
        if (!(parent instanceof IfNode)) {
            return;
        }
        IfNode ifNode = (IfNode) pop();
        ElseNode elseNode = new ElseNode();
        ifNode.setElseNode(elseNode);
        push(elseNode);
        tokenizer.skipWhitespace();
    }

    /**
     * バインド変数コメントを解析します。
     */
    protected void parseCommentBindVariable() {
        String expr = tokenizer.getToken();
        String s = tokenizer.skipToken();
        if (s.startsWith("(") && s.endsWith(")")) {
            peek().addChild(new ParenBindVariableNode(expr, expressionParser));
        } else if (expr.startsWith("$")) {
            peek().addChild(new EmbeddedValueNode(expr.substring(1), expressionParser));
        } else if (expr.equalsIgnoreCase("orderBy")) {
            peek().addChild(new EmbeddedValueNode(expr, expressionParser));
        } else {
            peek().addChild(new BindVariableNode(expr, expressionParser));
        }
    }

    /**
     * バインド変数を解析します。
     */
    protected void parseBindVariable() {
        String expr = tokenizer.getToken();
        peek().addChild(new BindVariableNode(expr, expressionParser));
    }

    /**
     * Pop (remove from the stack) the top node.
     *
     * @return the top node.
     */
    /**
     * 一番上のノードをポップ（スタックからも取り出す）します。
     *
     * @return 一番上のノード
     */
    protected Node pop() {
        return nodeStack.pop();
    }

    /**
     * 一番上のノードを返します。
     *
     * @return 一番上のノード
     */
    protected Node peek() {
        return nodeStack.peek();
    }

    /**
     * ノードを一番上に追加します。
     *
     * @param node ノード
     */
    protected void push(Node node) {
        nodeStack.push(node);
    }

    /**
     * {@code ELSE} モード（ELSE句の中の）かどうかを返します。
     *
     * @return {@code ELSE} モードのとき {@literal true} を返します。
     */
    protected boolean isElseMode() {
        for (Node node : nodeStack) {
            if (node instanceof ElseNode) {
                return true;
            }
        }
        return false;
    }

    /**
     * 対象とするコメントかどうかを返します。
     * バインド変数のコメントかどうかの判定に使用します。
     *
     * @param comment コメント
     * @return 対象とするコメントのとき {@literal true} を返します。
     */
    protected static boolean isTargetComment(String comment) {
        return comment != null && comment.length() > 0
                && Character.isJavaIdentifierStart(comment.charAt(0));
    }

    /**
     * Checks if this comment is a "Mirage-SQL" <code>IF</code> keyword.
     *
     * @param comment the comment to check
     * @return <code>true</code> if this comment is an <code>IF</code> keyword.
     */

    /**
     * {@literal IF} コメントかどうか判定します。
     *
     * @param comment コメント
     * @return {@literal IF} コメントのとき {@literal true} を返します。
     */
    protected static boolean isIfComment(String comment) {
        return comment.startsWith("IF");
    }

    /**
     * {@literal BEGIN} コメントかどうか判定します。
     *
     * @param comment コメント
     * @return {@literal BEGIN} コメントのとき {@literal true} を返します。
     */
    protected static boolean isBeginComment(String comment) {
        return comment != null && "BEGIN".equals(comment);
    }

    /**
     * {@literal END} コメントかどうか判定します。
     *
     * @param comment コメント
     * @return {@literal END} コメントのとき {@literal true} を返します。
     */
    protected static boolean isEndComment(String comment) {
        return comment != null && "END".equals(comment);
    }

    /**
     * Checks if the comment is an Oracle optimizer hint.
     *
     * @param content the comment to check
     * @return <code>true</code> if this comment is an Oracle optimizer hint.
     */
    /**
     * Oracle のヒントコメントかどうか判定します。
     *
     * @param comment コメント
     * @return Oracle のヒントコメントのとき {@literal true} を返します。
     */
    protected static boolean isHintComment(String comment) {
        return comment.startsWith("+");
    }
}
