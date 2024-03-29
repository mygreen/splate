package com.github.mygreen.splate;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import com.github.mygreen.splate.node.Node;
import com.github.mygreen.splate.parser.SqlParser;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * SQLテンプレートを管理します。
 *
 * @author T.TSUCHIE
 *
 */
public class SqlTemplateEngine {

    /**
     * SQLテンプレートファイルの文字コード名。
     * デフォルト値は、{@literal UTF-8} です。
     */
    @Getter
    @Setter
    private String encoding = "UTF-8";

    /**
     * SQLテンプレートのファイル名の接尾語。
     * DBの種類によって読み込み対象のファイルを切り替えたい場合に指定します。
     * デフォルト値は、{@literal null} です。
     */
    @Getter
    @Setter
    private String suffixName;

    /**
     * テンプレートファイルなどのリソースをロードする処理。
     * デフォルト値は、{@link DefaultResourceLoader} のインスタンスです。
     */
    @Getter
    @Setter
    private ResourceLoader resourceLoader = new DefaultResourceLoader();

    /**
     * SQLテンプレートファイルの読み込む処理。
     */
    @Getter
    @Setter
    private TemplateLoader templateLoader = new TemplateLoader();

    /**
     * EL式のパーサ
     */
    @Getter
    @Setter
    private SpelExpressionParser expressionParser = new SpelExpressionParser();

    /**
     * パースしたSQLテンプレートのキャッシュ。
     */
    private final Map<Object, SqlTemplate> templateCache = new ConcurrentHashMap<>();

    /**
     * パースしたプレートをキャッシュするかどうか。
     * デフォルトでは {@literal false} でキャッシュしない設定です。
     */
    @Getter
    @Setter
    private boolean cached;

    /**
     * SQLファイルのリソースパスを指定して、SQLテンプレートを取得します。
     * <p>SQLファイルのリソースは、Springの {@link ResourceLoader} 経由で取得するため、
     *  接頭語を付けることで複数のリソースを参照できます。
     * </p>
     * <ul>
     *  <li>何もつけない場合 - クラスパスから取得します。ex){@literal /sql/hoge.sql} </li>
     *  <li>{@literal classpath:} - クラスパスから取得します。ex){@literal classpath:/sql/hoge.sql} </li>
     *  <li>{@literal file:} - システムファイルから取得します。ex){@literal file:c:/sql/hoge.sql} </li>
     *  <li>{@literal http:} - URLからファイルを取得します。ex){@literal http://hoge.com/sql/hoge.sql} </li>
     * </ul>
     *
     * @param location SQLファイルのリソースパス。
     * @return パースしたSQLテンプレート
     * @throws TwoWaySqlException SQLファイルの読み込みやパースに失敗した場合にスローされます。
     */
    public SqlTemplate getTemplate(@NonNull final String location) {

        if(cached) {
            return templateCache.computeIfAbsent(location, k -> parseTemplateByLocation(location));
        } else {
            return parseTemplateByLocation(location);
        }

    }

    private SqlTemplate parseTemplateByLocation(final String location) {
        final String sqlText = templateLoader.loadByLocation(location, resourceLoader, encoding, Optional.ofNullable(suffixName));
        return parseTemplateByText(sqlText);
    }

    /**
     * SQLファイルのリソースを指定して、SQLテンプレートを取得します。
     *
     * @param resource SQLファイルのリソース。
     * @return パースしたSQLテンプレート
     * @throws TwoWaySqlException SQLファイルの読み込みやパースに失敗した場合にスローされます。
     */
    public SqlTemplate getTemplate(@NonNull final Resource resource) {

        if(cached) {
            return templateCache.computeIfAbsent(resource.getDescription(), k -> parseTemplateByResource(resource));
        } else {
            return parseTemplateByResource(resource);
        }

    }

    private SqlTemplate parseTemplateByResource(final Resource resource) {
        final String sqlText = templateLoader.loadByResource(resource, encoding);
        return parseTemplateByText(sqlText);
    }

    /**
     * SQLを文字列として直接指定し、SQLテンプレートを取得します。
     *
     * @param sql SQLの文字列
     * @return パースしたSQLテンプレート
     * @throws TwoWaySqlException SQLファイルの読み込みやパースに失敗した場合にスローされます。
     */
    public SqlTemplate getTemplateByText(@NonNull final String sql) {

        if(cached) {
            final String key = SqlUtils.getMessageDigest(sql);
            return templateCache.computeIfAbsent(key, k ->  parseTemplateByText(sql));
        } else {
            return parseTemplateByText(sql);
        }

    }

    /**
     * 文字列で指定されたSQLテンプレートをパースします。
     * @param sql SQL
     * @return パース結果。
     */
    private SqlTemplate parseTemplateByText(final String sql) {
        SqlParser parser = createSqlParser(sql);
        Node node = parser.parse();
        return new SqlTemplate(parser.getSql(), node);
    }

    /**
     * {@link SqlParser} のインスタンスを作成します。
     * @param sql パース対象のSQLテンプレート
     * @return {@link SqlParser} のインスタンス
     */
    protected SqlParser createSqlParser(final String sql) {
        return new SqlParser(sql, expressionParser);
    }

    /**
     * 現在キャッシュしている情報をクリアします。
     */
    public void clearCache() {
        this.templateCache.clear();
    }

}
