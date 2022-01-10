# 基本的な使い方

## 1. SQLテンプレートの定義

SQLテンプレートをファイルに定義します。

- SQLにバインドするパラメータを ``/*パラメータ名*/`` の形式で指定します。
- バインドパラメータは、最終的にはプレースホルダー ``?`` に置換された出力されます。

```sql
select * from employee
/*BEGIN*/
where
/*IF salaryMin != null*/
salary >= /*salaryMin*/1000
/*END*/
/*IF salaryMax != null*/
and salary <= /*salaryMax*/2000
/*END*/
/*END*/
```

詳細は、「[2Way-SQLとは](2waysql.html) 」を参照してください。


## 2. SQLテンプレートのパース

``SqlTemplateEngine`` のインスタンスを作成します。

```java
SqlTemplateEngine templateEngine = new SqlTemplateEngine();

```

``SqlTemplateEngine#getTemplate(...)`` を使い、SQLテンプレートをパースします。

SpringFrameworkの [ResourceLoader](https://docs.spring.io/spring/docs/5.1.x/spring-framework-reference/core.html#resources-resourceloader) を使用しているため接頭語を付けることで様々なリソースにアクセスできます。

- ``classpath:`` - クラスパス上から読み込みます。例） ``classpath:/sql/hoge.sql``
- ``file:`` - システムファイルから読み込みます。例）``file:c:/sql/hoge.sql``
- ``http:`` - ネットワーク上のURLから読み込みます。例）``http://hoge.com/sql/hoge.sql``
- なし - クラスパス上から読み込みます。例） ``/sql/hoge.sql``

```java
SqlTemplate template = templateEngine.getTemplate("classpath:/sql/employee_select.sql");
```

## 3. パラメータの指定

パラメータは、JavaBeanの形式とMapの形式で指定することができます。

### 3.1. JavaBeanによるパラメータの指定

JavaBeanで指定するときには、プロパティ名をSQLテンプレート中で定義したバインドパラメータ名と一致させる必要があります。

また、フィールドが ``public`` であれば、getter/setter のアクセッサメソッドは省略できます。

EL式の実装の１つである [SpEL](https://docs.spring.io/spring/docs/5.1.x/spring-framework-reference/core.html#expressions) を使用して参照するため、ネストした形式でも定義可能です。

```java
// パラメータ用JavaBeanの定義
public class SelectParam {
    public BigDecimal salaryMin;
    public BigDecimal salaryMax;
}

// JavaBeanのインスタンスの作成
SelectParam bean = new SelectParam();
bean.salaryMin = new BigDecimal(1200);
bean.salaryMax = new BigDecimal(1800);
```

SQLテンプレートを評価する際の引数 ``SqlTemplateContext`` の実装の１つである ``BeanPropertySqlTemplateContext`` を使用します。
```java
SqlTemplateContext templateContext = new BeanPropertySqlTemplateContext(bean);
```

### 3.2. Mapによるパラメータの指定

Mapで指定するときには、キー名をSQLテンプレート中で定義したバインドパラメータ名と一致させる必要があります。

```java
Map<String, Object> map = Map.of("salaryMin", 1200, "salaryMax", 1800);
```

SQLテンプレートを評価する際の引数 ``SqlTemplateContext`` の実装の１つである ``MapSqlTemplateContext`` を使用します。
```java
SqlTemplateContext templateContext = new MapSqlTemplateContext(map);
```

また、``MapSqlTemplateContext`` はインスタンス作成後でもバインドパラメータを設定可能です。

```java
MapSqlTemplateContext templateContext = new MapSqlTemplateContext();
context.setVariable("salaryMin", 1200);
context.setVariable("salaryMax", 1800);
```

### 3.3. パラメータがない場合

SQLテンプレートに渡すパラメータがない場合は、 ``EmptyValueSqlTemplateContext`` を使用します。

```java
SqlTemplateContext templateContext = new EmptyValueSqlTemplateContext();
```

## 4. SQLテンプレートの評価

``SqlTemplate#process(...)`` にて、SQLテンプレートを評価します。

結果は、``PreparedStatement`` や ``JdbcTemplate`` に渡せる形式になっています。

```java
ProcessResult result = template.process(context);

// 評価したSQLテンプレートの取得
// 結果 : select * from employee salary >= ? and salary <= ?
String sql = result.getSql();

// SQL中のバインドパラメータの取得
// テンプレート中のプレースホルダーの定義準になっています。
List<Object> bindParams = result.getParameters();
```
