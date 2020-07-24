package com.github.mygreen.sqltemplate;

import java.util.Optional;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import com.github.mygreen.sqltemplate.node.Node;
import com.github.mygreen.sqltemplate.parser.SqlParser;

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
     * SQLテンプレートファイルの文字コード名
     */
    @Getter
    @Setter
    private String encoding = "UTF-8";

    /**
     * SQLテンプレートのファイル名の接尾語。
     * DBの種類によって読み込み対象のファイルを切り替えたい場合に指定します。
     */
    @Getter
    @Setter
    private String suffixName;

    /**
     * テンプレートファイルなどのリソースをロードする処理。
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
     * SQLファイルのリソースパスを指定して、SQLテンプレートを取得します。
     * <p>SQLファイルのリソースは、Springの {@link ResourceLoader} 経由で取得するため、
     *  接頭語を付けることで複数のリソースを参照できます。
     * </p>
     * <ul>
     *  <li>何もつけない場合 - クラスパスから取得します。ex){@literal /sql/hoge.sql} </li>
     *  <li>{@literal classpath:} - クラスパスから取得します。ex){@literal classpath:/sql/hoge.sql} </li>
     *  <li>{@literal file:} - システムファイルから取得します。ex){@literal file:c:/sql/hoge.sql} </li>
     *  <li>{@literal http/https:} - URLからファイルを取得します。ex){@literal http://localhost:8080/sql/hoge.sql} </li>
     * </ul>
     *
     * @param location SQLファイルのリソースパス。
     * @return パースしたSQLテンプレート
     * @throws TwoWaySqlException SQLファイルの読み込みやパースに失敗した場合にスローされます。
     */
    public SqlTemplate getTemplate(@NonNull final String location) {

        final String sqlText = templateLoader.loadByLocation(location, resourceLoader, encoding, Optional.ofNullable(suffixName));
        SqlParser parser = createSqlParser(sqlText);
        Node node = parser.parse();
        return new SqlTemplate(node);

    }

    /**
     * SQLファイルのリソースを指定して、SQLテンプレートを取得します。
     *
     * @param resource SQLファイルのリソース。
     * @return パースしたSQLテンプレート
     * @throws TwoWaySqlException SQLファイルの読み込みやパースに失敗した場合にスローされます。
     */
    public SqlTemplate getTemplate(@NonNull final Resource resource) {

        final String sqlText = templateLoader.loadByResource(resource, encoding);
        SqlParser parser = createSqlParser(sqlText);
        Node node = parser.parse();
        return new SqlTemplate(node);
    }

    /**
     * SQLを文字列として直接指定し、SQLテンプレートを取得します。
     *
     * @param sql SQLの文字列
     * @return パースしたSQLテンプレート
     * @throws TwoWaySqlException SQLファイルの読み込みやパースに失敗した場合にスローされます。
     */
    public SqlTemplate getTemplateByText(@NonNull final String sql) {

        SqlParser parser = createSqlParser(sql);
        Node node = parser.parse();
        return new SqlTemplate(node);

    }

    /**
     * {@link SqlParser} のインスタンスを作成します。
     * @param sql パース対象のSQLテンプレート
     * @return {@link SqlParser} のインスタンス
     */
    protected SqlParser createSqlParser(final String sql) {

        final SqlParser sqlParser = new SqlParser(sql, new SpelExpressionParser());

        return sqlParser;
    }

}
