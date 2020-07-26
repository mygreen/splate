# 設定

SQLテンプレートを処理する環境設定の変更について説明します。

## SQLテンプレートファイルの文字コード設定

リソースからSQLテンプレートを読み込む場合の文字コードを変更可能です。

デフォルト値は、``UTF-8`` です。

```java
SqlTemplateEngine templateEngine = new SqlTemplateEngine();
templateEngine.setEncoding("MS932");
```

## 環境によってSQLファイルを切り替える

接続するDBの種類によって、読み込むリソースファイルを切り替えることができます。

一致する接尾語のファイルがない場合は、接尾語なしのファイルが読み込まれます。

```
<classpath_root>
   └─ sql
       ├─ employee_select.sql
       ├─ employee_select-h2.sql      ・・・接尾語付きのSQL(H2用)
       └─ employee_select-oracle.sql  ・・・接尾語付きのSQL(Oracle用)
```

```java
SqlTemplateEngine templateEngine = new SqlTemplateEngine();

// リソース名の接尾語の指定
templateEngine.setSuffixName("oracle");

// テンプレートの取得
// 「sql/employee_select-oracle.sql」というファイルが読み込まれる。
SqlTemplate template = templateEngine.getTemplate("/sql/employee_select.sql");
```

## SQLテンプレートのキャッシュ設定

SQLテンプレートのパースは処理が重いため、何度も呼び出す場合はコストがかかります。

その場合、キャッシュ設定を有効にすることでパース下SQLテンプレートのインスタンスをキャッシュすることができます。

```java
SqlTemplateEngine templateEngine = new SqlTemplateEngine();

// キャッシュの有効化
templateEngine.setCached(true);
```

> 注意
> 
> 注意事項として、キャッシュを有効にしている状態で、途中で ``SqlTemplateEngine#setSuffixName(...)`` にて接尾語を変更した場合、切り替える前のSQLテンプレートが返されるため注意してください。


## リソースローダの変更

SQLテンプレートを読み込む際のリソースローダーを切り替えることができます。

デフォルト値は、``DefaultResourceLoader`` です。

```java
SqlTemplateEngine templateEngine = new SqlTemplateEngine();

// リロースローダの設定
templateEngine.setResourceLoader(...);
```

