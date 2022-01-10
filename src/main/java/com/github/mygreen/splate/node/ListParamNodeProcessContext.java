package com.github.mygreen.splate.node;

import java.util.ArrayList;
import java.util.List;

import com.github.mygreen.splate.SqlTemplateContext;

import lombok.Getter;


public class ListParamNodeProcessContext extends NodeProcessContext {

    /**
     * SQLテンプレート中の変数をバインドしたパラメータ
     */
    @Getter
    private List<Object> bindParams = new ArrayList<>();

    /**
     * {@code SQL} とバインド変数を追加します。
     * @param sql SQL
     * @param bindValue バインドする変数の値
     */
    public void addSql(String sql, Object bindValue) {
        addSql(sql);
        this.bindParams.add(bindValue);
    }

    /**
     * {@code SQL} とバインド変数を追加します。
     * @param sql SQL
     * @param bindParams バインドする変数情報
     */
    public void addSql(final String sql, final List<Object> bindParams) {
        addSql(sql);
        this.bindParams.addAll(bindParams);

    }

    /**
     * テンプレートパラメータなどのSQLコンテキストを指定するコンストラクタ。
     * @param templateContext SQLテンプレートのコンテキスト
     */
    public ListParamNodeProcessContext(final SqlTemplateContext<?> templateContext) {
        super(templateContext);
    }

    /**
     * 親コンテキストを指定するコンストラクタ。
     *
     * @param parent 親のコンテキスト.
     */
    public ListParamNodeProcessContext(final ListParamNodeProcessContext parent) {
        super(parent);
    }

}
