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

import com.github.mygreen.splate.TwoWaySqlException;

/**
 * SQLをトークンに分解するクラスです。.
 *
 * @author higa
 */
public class SqlTokenizer {

    /**
     * トークンの種類
     *
     */
    public enum TokenType {
        SQL,
        COMMENT,
        ELSE,
        BIND_VARIABLE,
        EOF
    }

    private String sql;

    private int position = 0;

    private String token;

    private TokenType tokenType = TokenType.SQL;

    private TokenType nextTokenType = TokenType.SQL;

    private int bindVariableNum = 0;

    public SqlTokenizer(String sql) {
        this.sql = sql;
    }

    /**
     * @return SQLを返します。
     */
    public String getSql() {
        return sql;
    }

    /**
     * @return 現在解析しているポジションを返します。
     */
    public int getPosition() {
        return position;
    }

	/**
     * @return トークンを返します。
     */
    public String getToken() {
        return token;
    }

    /**
     * @return 現在解析しているポジションより前のSQLを返します。
     */
    public String getBefore() {
        return sql.substring(0, position);
    }

    /**
     * @return 現在解析しているポジションより後ろのSQLを返します。
     */
    public String getAfter() {
        return sql.substring(position);
    }

    /**
     * @return 現在のトークン種別を返します。
     */
    public TokenType getTokenType() {
        return tokenType;
    }

    /**
     * @return 次のトークン種別を返します。
     */
    public TokenType getNextTokenType() {
        return nextTokenType;
    }

    /**
     * @return 次のトークンに進みます。
     */
    public TokenType next() {
        if (position >= sql.length()) {
            token = null;
            tokenType = TokenType.EOF;
            nextTokenType = TokenType.EOF;
            return tokenType;
        }
        switch (nextTokenType) {
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
            parseEof();
            break;
        }
        return tokenType;
    }

    /**
     * Parse the SQL.
     */
    protected void parseSql() {
        int commentStartPos = sql.indexOf("/*", position);
        int commentStartPos2 = sql.indexOf("#*", position);
        if (0 < commentStartPos2 && commentStartPos2 < commentStartPos) {
            commentStartPos = commentStartPos2;
        }
        int lineCommentStartPos = sql.indexOf("--", position);
        int bindVariableStartPos = sql.indexOf("?", position);
        int elseCommentStartPos = -1;
        int elseCommentLength = -1;
        if (lineCommentStartPos >= 0) {
            int skipPos = skipWhitespace(lineCommentStartPos + 2);
            if (skipPos + 4 < sql.length()
                    && "ELSE".equals(sql.substring(skipPos, skipPos + 4))) {
                elseCommentStartPos = lineCommentStartPos;
                elseCommentLength = skipPos + 4 - lineCommentStartPos;
            }
        }
        int nextStartPos = getNextStartPos(commentStartPos,
                elseCommentStartPos, bindVariableStartPos);
        if (nextStartPos < 0) {
            token = sql.substring(position);
            nextTokenType = TokenType.EOF;
            position = sql.length();
            tokenType = TokenType.SQL;
        } else {
            token = sql.substring(position, nextStartPos);
            tokenType = TokenType.SQL;
            boolean needNext = nextStartPos == position;
            if (nextStartPos == commentStartPos) {
                nextTokenType = TokenType.COMMENT;
                position = commentStartPos + 2;
            } else if (nextStartPos == elseCommentStartPos) {
                nextTokenType = TokenType.ELSE;
                position = elseCommentStartPos + elseCommentLength;
            } else if (nextStartPos == bindVariableStartPos) {
                nextTokenType = TokenType.BIND_VARIABLE;
                position = bindVariableStartPos;
            }
            if (needNext) {
                next();
            }
        }
    }

    /**
     * Returns the next starting position.
     *
     * @param commentStartPos starting position of the comment
     * @param elseCommentStartPos starting position of the ELSE comment
     * @param bindVariableStartPos starting position of the bind variable
     * @return the next starting position.
     */
    protected int getNextStartPos(int commentStartPos, int elseCommentStartPos,
            int bindVariableStartPos) {

        int nextStartPos = -1;
        if (commentStartPos >= 0) {
            nextStartPos = commentStartPos;
        }
        if (elseCommentStartPos >= 0
                && (nextStartPos < 0 || elseCommentStartPos < nextStartPos)) {
            nextStartPos = elseCommentStartPos;
        }
        if (bindVariableStartPos >= 0
                && (nextStartPos < 0 || bindVariableStartPos < nextStartPos)) {
            nextStartPos = bindVariableStartPos;
        }
        return nextStartPos;
    }

    /**
     * Parse the comment.
     */
    protected void parseComment() {
        int commentEndPos = sql.indexOf("*/", position);
        int commentEndPos2 = sql.indexOf("*#", position);
        if (0 < commentEndPos2 && commentEndPos2 < commentEndPos) {
            commentEndPos = commentEndPos2;
        }
        if (commentEndPos < 0) {
            throw new TwoWaySqlException(String.format(
                    "%s is not closed with %s.", sql.substring(position), "*/"));
        }
        token = sql.substring(position, commentEndPos);
        nextTokenType = TokenType.SQL;
        position = commentEndPos + 2;
        tokenType = TokenType.COMMENT;
    }

    /**
     * Parse the bind variable.
     */
    protected void parseBindVariable() {
        token = nextBindVariableName();
        nextTokenType = TokenType.SQL;
        position += 1;
        tokenType = TokenType.BIND_VARIABLE;
    }

    /**
     * Parse the ELSE comment.
     */
    protected void parseElse() {
        token = null;
        nextTokenType = TokenType.SQL;
        tokenType = TokenType.ELSE;
    }

    /**
     * Parse the end of the SQL.
     */
    protected void parseEof() {
        token = null;
        tokenType = TokenType.EOF;
        nextTokenType = TokenType.EOF;
    }

    /**
     * @return the bind variable name for the position parameters.
     */
    protected String nextBindVariableName() {
        return "$" + ++bindVariableNum;
    }

    /**
     * トークンをスキップします。
     *
     * @return スキップしたトークン
     */
    public String skipToken() {
        int index = sql.length();
        char quote = position < sql.length() ? sql.charAt(position) : '\0';
        boolean quoting = quote == '\'' || quote == '(';
        if (quote == '(') {
            quote = ')';
        }
        for (int i = quoting ? position + 1 : position; i < sql.length(); ++i) {
            char c = sql.charAt(i);
            if ((Character.isWhitespace(c) || c == ',' || c == ')' || c == '(')
                    && !quoting) {
                index = i;
                break;
            } else if (c == '/' && i + 1 < sql.length()
                    && sql.charAt(i + 1) == '*') {
                index = i;
                break;
            } else if (c == '-' && i + 1 < sql.length()
                    && sql.charAt(i + 1) == '-') {
                index = i;
                break;
            } else if (quoting && quote == '\'' && c == '\''
                    && (i + 1 >= sql.length() || sql.charAt(i + 1) != '\'')) {
                index = i + 1;
                break;
            } else if (quoting && c == quote) {
                index = i + 1;
                break;
            }
        }
        token = sql.substring(position, index);
        tokenType = TokenType.SQL;
        nextTokenType = TokenType.SQL;
        position = index;
        return token;
    }

	/**
     * ホワイトスペースをスキップします。
     *
     * @return スキップしたホワイストスペース
     */
    public String skipWhitespace() {
        int index = skipWhitespace(position);
        token = sql.substring(position, index);
        position = index;
        return token;
    }

    private int skipWhitespace(int position) {
        int index = sql.length();
        for (int i = position; i < sql.length(); ++i) {
            char c = sql.charAt(i);
            if (!Character.isWhitespace(c)) {
                index = i;
                break;
            }
        }
        return index;
    }
}