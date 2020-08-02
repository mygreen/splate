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
package com.github.mygreen.splate.parser;

import java.util.Stack;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;

import com.github.mygreen.splate.SqlUtils;
import com.github.mygreen.splate.node.BeginNode;
import com.github.mygreen.splate.node.BindVariableNode;
import com.github.mygreen.splate.node.ContainerNode;
import com.github.mygreen.splate.node.ElseNode;
import com.github.mygreen.splate.node.EmbeddedValueNode;
import com.github.mygreen.splate.node.IfNode;
import com.github.mygreen.splate.node.Node;
import com.github.mygreen.splate.node.ParenBindVariableNode;
import com.github.mygreen.splate.node.PrefixSqlNode;
import com.github.mygreen.splate.node.SqlNode;
import com.github.mygreen.splate.parser.SqlTokenizer.TokenType;

/**
 * SQLを解析して<code>Node</code>のツリーにするクラスです。
 *
 * @version 0.2
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
     * 解析対象のSQLを取得します。
     * @return 正規化（トリム、最後のセミコロン(;)削除）を返します。
     */
    public String getSql() {
        return tokenizer.getSql();
    }

    /**
     *
     * @return SQLを解析して<code>Node</code>のツリーを返します。
     */
    public Node parse() {
        push(new ContainerNode(0));
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
                    node.addChild(new PrefixSqlNode(tokenizer.getPosition(), ", ", sql.substring(2)));
                } else {
                    node.addChild(new PrefixSqlNode(tokenizer.getPosition(), ",", sql.substring(1)));
                }
            } else if ("AND".equalsIgnoreCase(token)
                    || "OR".equalsIgnoreCase(token)) {
                node.addChild(new PrefixSqlNode(tokenizer.getPosition(), st.getBefore(), st.getAfter()));
            } else {
                node.addChild(new SqlNode(tokenizer.getPosition(), sql));
            }
        } else {
            node.addChild(new SqlNode(tokenizer.getPosition(), sql));
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
            peek().addChild(new SqlNode(tokenizer.getPosition(), "/*" + comment + "*/"));
        }
    }

    /**
     * {@code IF} 句を解析します。
     */
    protected void parseIf() {
        final String condition = tokenizer.getToken().substring(2).trim();
        final int position = Math.max(tokenizer.getPosition() - 2, 0);
        if (SqlUtils.isEmpty(condition)) {
            throw new SqlParseException(SqlUtils.resolveSqlPosition(tokenizer.getSql(), position),
                    "Not found IF condition.");
        }
        IfNode ifNode = new IfNode(position, condition, parseExpression(condition, position));
        peek().addChild(ifNode);
        push(ifNode);
        parseEnd();
    }

    /**
     * {@code BEGIN} 句を解析します。
     */
    protected void parseBegin() {
        BeginNode beginNode = new BeginNode(tokenizer.getPosition());
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
        throw new SqlParseException(SqlUtils.resolveSqlPosition(tokenizer.getSql(), tokenizer.getPosition()),
                "Not found END comment.");
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
        ElseNode elseNode = new ElseNode(tokenizer.getPosition());
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
        int position = tokenizer.getPosition();
        if (s.startsWith("(") && s.endsWith(")")) {
            peek().addChild(new ParenBindVariableNode(position, expr, parseExpression(expr, position)));
        } else if (expr.startsWith("$")) {
            expr = expr.substring(1);
            peek().addChild(new EmbeddedValueNode(position, expr, parseExpression(expr, position)));
        } else if (expr.equalsIgnoreCase("orderBy")) {
            peek().addChild(new EmbeddedValueNode(position, expr, parseExpression(expr, position)));
        } else {
            peek().addChild(new BindVariableNode(position, expr, parseExpression(expr, position)));
        }
    }

    /**
     * バインド変数を解析します。
     */
    protected void parseBindVariable() {
        String expr = tokenizer.getToken();
        int position = tokenizer.getPosition();
        peek().addChild(new BindVariableNode(position, expr, parseExpression(expr, position)));
    }

    /**
     * EL式をパースします。
     * <p>例外処理を含めて共通化のために切り出したメソッドです。</p>
     *
     * @since 0.2
     * @param expression 式
     * @param position テンプレート位置
     * @return パースした式
     * @throws SqlParseException EL式のパースに失敗した場合にスローされます。
     */
    protected Expression parseExpression(final String expression, final int position) {
        try {
            return expressionParser.parseExpression(expression);
        } catch(ParseException e) {
            throw new SqlParseException(SqlUtils.resolveSqlPosition(tokenizer.getSql(), position),
                    String.format("Fail parsing expression '%s'.", expression),
                    e);
        }
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
