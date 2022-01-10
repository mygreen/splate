package com.github.mygreen.splate.node;

import java.util.HashMap;
import java.util.Map;

import com.github.mygreen.splate.NamedParamBindNameResolver;
import com.github.mygreen.splate.SqlTemplateContext;

import lombok.Getter;
import lombok.Setter;

/**
 * 名前付きパラメータの{@code SQLテンプレート}を評価するときのコンテキストです。 コンテキストで{@code SQL}を実行するのに必要な情報を組み立てた後、
 * {@code getSql()}, {@code getBindVariables()},
 * {@code getBindVariableTypes()}で、 情報を取り出して{@code SQL}を実行します。
 * {@code SQL}で{@code BEGIN}コメントと{@code END}コメントで囲まれている部分が、
 * 子供のコンテキストになります。 通常は、 {@code WHERE}句を{@code BEGIN}コメントと{@code END}コメントで囲み、
 * {@code WHERE}句の中の{@code IF}コメントが1つでも成立した場合、{@code enabled}になります。
 *
 * @version 0.3
 * @author T.TSUCHIE
 *
 */
public class NamedParamNodeProcessContext extends NodeProcessContext {

    /**
     * バインド変数の名称を解決する処理
     */
    @Setter
    @Getter
    private NamedParamBindNameResolver bindNameResolver = new NamedParamBindNameResolver();

    /**
     * SQLテンプレート中の変数をバインドしたパラメータ
     */
    @Getter
    private Map<String, Object> bindParams = new HashMap<>();

    /**
     * テンプレートパラメータなどのSQLコンテキストを指定するコンストラクタ。
     * @param templateContext SQLテンプレートのコンテキスト
     */
    public NamedParamNodeProcessContext(final SqlTemplateContext<?> templateContext) {
        super(templateContext);
    }

    /**
     * 親コンテキストを指定するコンストラクタ。
     *
     * @param parent 親のコンテキスト.
     */
    public NamedParamNodeProcessContext(final NamedParamNodeProcessContext parent) {
        super(parent);
        this.bindNameResolver = parent.bindNameResolver;

    }

    /**
     * バインド変数を割り当てます。
     * 既に存在するパラメータ
     * @param bindName 割り当て候補のバインド変数。
     * @return 新たに割り当てられたバインド変数名。
     */
    public String allocateBindName(String bindName) {
        return bindNameResolver.newBindName(bindParams.keySet(), bindName);
    }

    /**
     * {@code SQL} とバインド変数を追加します。
     * @param sql SQL
     * @param bindName バインドする変数の名称
     * @param bindValue バインドする変数の値
     */
    public void addSql(String sql, String bindName, Object bindValue) {
        addSql(sql);
        this.bindParams.put(bindName, bindValue);
    }

    /**
     * {@code SQL} とバインド変数を追加します。
     * @param sql SQL
     * @param bindParams バインドする変数情報
     */
    public void addSql(final String sql, final Map<String, Object> bindParams) {
        addSql(sql);
        this.bindParams.putAll(bindParams);

    }

}
