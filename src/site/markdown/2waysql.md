# 2Way-SQLとは

2Way-SQLは普通のSQL文をファイルに保存したものです。

特別なSQLコメントを使用してパラメータと条件を指定できます。

また、そのままテンプレートはSQLクライアントツールで実行できますし、本ライブラリで読み込んで ``PreparedStatement`` や ``JdbcTemplate`` などで実行することもでいます。
（２つの実行方法があることから2WaySQLと呼ばれます）

この機能は、O/Rマッピングフレームワークの [S2JDBC](http://s2container.seasar.org/2.4/ja/s2jdbc.html) に基づいています。


## 1. バインドパラメータ

SQLにバインドするパラメータを ``/*パラメータ名*/`` のSQLコメントの形式で指定することができます。

- このSQLコメントには空白を含めることができません。

以下にもっとの簡単な例を示します。

```sql
SELECT * FROM EMPLOYEE
WHERE FULL_NAME = /*fullName*/'Taro Yamada'
ORDER BY EMP_ID
```

上の例では、``/*fullName*/`` がバインドパラメータで、SQLテンプレートを評価する際には、この部分がプレースホルダー（``?``）に置き換わります。
さらに、後ろの ``'Taro Yamada'`` が削除されます。

```sql
SELECT * FROM EMPLOYEE
WHERE FULL_NAME = ?
ORDER BY EMP_ID
```

### 1.2. IN句の利用方法

Collection型や配列の値をIN句のパラメータとして指定することもできます。

- IN句にバインドパラメータを指定する場合、バインドパラメータの後に ``(...)``を記述する必要があります。

```sql
SELECT * FROM EMPLOYEE
WHERE JOB_TYPE IN /*jobTypeList*/('ADMIN')
ORDER BY EMP_ID
```

上の例に ``jobTypeList`` として ``{"ADMIN", "STAFF"}`` 指定すると、以下のようにCollectionや配列のサイズによってプレースホルダ―の個数が動的に変わります。

```sql
SELECT * FROM EMPLOYEE
WHERE JOB_TYPE IN (?, ?)
ORDER BY EMP_ID
```

### 1.3. LIKE句の利用方法

LIKE句に対してバインドパラメータを使用する場合は、 '(シングルクオート)で囲まれている部分を置き換えます。

```sql
SELECT * FROM EMPLOYEE
WHERE FULL_NAME LIKE /*fullName*/'%Yamada'
ORDER BY EMP_ID
```

ワイルドカードを使いたい場合は、パラメータの値に埋め込んでください。

```java
MapSqlContext context = new MapSqlContext();
context.setVariable("fullName", "%Yamada");
```

SQLテンプレートを評価した場合は、次のようになります。

```sql
SELECT * FROM EMPLOYEE
WHERE FULL_NAME LIKE ?
ORDER BY EMP_ID
```

## 2. 文字列置換

LIMIT句やORDER BY句など、``PreparedStatement`` にてプレースホルダ―が利用できない部分は、``/*$パラメータ名*/`` のSQLコメントの形式で指定し、単純な文字列置換を行います。

- 置換する値として、SQLインジェクションの脆弱性の原因となる ``;``(セミコロン) を含めることはできません。セミコロンが含まれている場合は、例外 ``TwoWaySqlException`` がスローされます。

```sql
SELECT * FROM EMPLOYEE
ORDER BY /*$orderByColumn*/
LIMIT /*$limit*/ OFFSET /*$offset*/
```

SQLテンプレートを評価した結果として、次のように文字列置換されます。

```sql
SELECT * FROM EMPLOYEE
ORDER BY BIRTHDAY,FULLNAME
LIMIT 10 OFFSET 5
```

> 注意
> 
> 文字列置換は、SQLインジェクションなど脆弱性の原因となる可能性があります。
> 十分に注意を払ったうえで利用してください


## 3. IF, ELSE, END

``/*IF*/`` / ``--ELSE`` / ``/*END*/`` を使用し、動的にSQLを変更することができます。

**IF** コメントでは、次のように条件式を EL式の１種である [SpEL](https://docs.spring.io/spring/docs/5.1.x/spring-framework-reference/core.html#expressions) で記述します。

```sql
SELECT * FROM EMPLOYEE
/*IF fullName != null*/
WHERE FULL_NAME = /*fullName*/'Taro Yamada'
/*END*/
ORDER BY EMP_ID
```

**ELSE** コメントは、IFコメントとENDコメントの間に行コメントとして埋め込みます。

理由として、SQLクライアントから直接実行した場合、ELSE文は無効となり正しいSQLとして解釈できるためです。

```sql
SELECT * FROM EMPLOYEE
/*IF fullName != null*/
WHERE FULL_NAME = /*fullName*/'Taro Yamada'
--ELSE FULL_NAME IS NULL
/*END*/
ORDER BY EMP_ID
```

## 4. BEGIN, END

まず初めに、以下のSQLについて見ていきます。

```sql
SELECT * FROM EMPLOYEE
WHERE
/*IF minSalary != null*/
    SALARY >= /*minSalary*/1000
/*END*/
/*IF maxSalary != null*/
    AND SALARY <= /*maxSalary*/2000
/*END*/
```

バインドパラメータ ``minSalary`` が null、 ``maxSlary`` がnullでないとき、次のように不正なSQLとなります。

```sql
SELECT * FROM EMPLOYEE
WHERE
AND SALARY <= ?
```

また、``minSalary`` と ``maxSalary`` の両方の値が null のときも同様に不正なSQLとなります。

```sql
SELECT * FROM EMPLOYEE
WHERE
```

このようなときは、``/*BEGIN*/``, ``/*END*/`` を使用して、次のように囲みます。

```sql
SELECT * FROM EMPLOYEE
/*BEGIN*/
    WHERE
    /*IF minSalary != null*/
        SALARY >= /*minSalary*/1000
    /*END*/
    /*IF maxSalary != null*/
        AND SALARY <= /*maxSalary*/2000
    /*END*/
/*END*/
```

このようにすると、salaryMinがnullでsalaryMaxがnullではないときは、salaryMaxの条件は、BEGINコメントとENDコメントで囲まれた最初の条件なので、 AND の部分が自動的に削除されて次のようになります。

```sql
SELECT * FROM EMPLOYEE
WHERE
SALARY <= ?
```

また、salaryMinとsalaryMaxがnullの場合は、 BEGINコメントとENDコメントで囲まれた部分に有効なIFコメントが一つもないため、 BEGINコメントとENDコメントで囲まれた全体がカットされて次のようになります。

```sql
SELECT * FROM EMPLOYEE
```


