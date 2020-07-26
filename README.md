# SqlTemplate

本ライブラリは、 **2Way-SQL** 機能のみを [S2JDBC](http://s2container.seasar.org/2.4/ja/s2jdbc.html) から分離し、使いやすくしたたライブラリです。

このライブラリは、SpringFrameworkに依存しています。理由は以下の通りです。

- SpringFrameworkに依存している理由として、普段、Javaで開発するときには、多くがSpringBootを利用しいる状況にあるためです。
- そのため、無理にSpringFrameworkに依存しないように作ってもコード量が増えるだけで、あまりメリットがないと考えられます。
- SpringFrameworkに依存しないで2Way-SQLを利用するならば、他の多くのライブラリ（ [Doma2](https://doma.readthedocs.io/) / [DBFlute](http://dbflute.seasar.org/) / [Mirage-SQL](https://github.com/mirage-sql/mirage)）が既に存在するためそちらを利用すすることを検討すべきです。

## Licensee
Apache2 License

## Dependency

- Java 11+
- SpringFramework 5.1+
- Slf4j 1.7+


## Setup

App dependency. ex) pom.xml

```xml
<dependency>
	<groupId>com.github.mygreen</groupId>
	<artifactId>sqltemplate</artifactId>
	<vesion>0.1</version>
</dependency>
```

## How to use

1. Define SQL file.
  ```sql
  select * from employee
  /*BEGIN*
  where
  /*IF salaryMin != null*/
  salary >= /*salaryMin*/1000
  /*END*/
  /*IF salaryMax != null*/
  and salary <= /*salaryMax*/2000
  /*END*/
  /*END*/
  ```
2. Create instance the ``SqlTemplateEngine`` and parse SQL template file.
  ```java
  SqlTemplateEngine templateEngine = new SqlTemplateEngine();
  SqlTemplate template = templateEngine.getTemplate("classpath:/sql/employee_select.sql");
  ```
3. Create template parameter with ``SqlContext``.
  ```java
  public class SelectParam {
      public BigDecimal salaryMin;
      public BigDecimal salaryMax;
  }
  ```

  ```java
  // create parameter with JavaBean.
  SelectParam param = new SelectParam();
  param.salaryMin = new BigDecimal(1200);
  param.salaryMax = new BigDecimal(1800);
  
  // create instance of SqlContext.
  SqlContext context = new BeanPropertySqlContext(param);
  ```
4. Evaluating SQL template.
  ```java
  ProcessResult result = template.process(context);

  // sql : select * from employee salary >= ? and salary <= ?
  String sql = result.getSql();

  // bind parameters
  List<Object> params = result.getParameters();
  ```

## Documentation

- Project information and manual.
  - https://mygreen.github.io/sqltemplate/index.html
- JavaDoc
  - https://mygreen.github.io/sqltemplate/apidocs/index.html

