# 高度な使い方

## 1. ネストしたパラメータを使用する

SpEL式を使用しているため、パラメータがネストした形式でも定義可能です。

```java
// パラメータ用JavaBeanの定義
public class NestedParam {

    // ネストしたクラス
    public PK pk;

    public String name;

    public static PK {
        public String employeeId;
        public Long addressId;
    }
}
```

```sql
select * from employee_address
/*BEGIN*/
where
    /*IF pk.employeeId != null*/
    employee_id = /*pk.employeeId*/'001'
    /*END*/
    /*IF pk.addressId != null*/
    AND address_id /*pk.addressId*/1
    /*END*/
/*END*/
```


## 2. SQLテンプレート中に存在しないプロパティやキーを無視する

デフォルトでは、SQLテンプレート中に定義しているバインドパラメータを `BeanPropertySqlTemplateContext` や `MapSqlTemplateContext` にて、全て定義する必要があり、存在しない場合は例外がスローされます。

しかし、メソッド `setIgnoreNotFoundProperty(boolean)` を指定することで、値をパラメータとして `null` を指定されたときと同様に扱うことができます。

```java
BeanPropertySqlTemplateContext context = new BeanPropertySqlTemplateContext(param);
// 存在しないプロパティを無視する設定を有効化
context.setIgnoreNotFoundProperty(true);
ProcessResult result = template.process(context);
```

## 3. SpEL式中でカスタム関数を使用する

SQLテンプレート中の `/*IF <式>*/` / `--ELSE <式>` 中でSpELのカスタム関数を使用することができます。

メソッド ``SqlTemplateContext#setEvaluationContextEditor(EvaluationContext)` にて、SQLテンプレート評価直前の SpELの `EvaluationContext` を編集することができます。

以下のメソッド `SqlFunctions#notEmpty(...)` を、SpELのカスタム関数として登録する方法を説明します。

```java
/**
 * SQLテンプレート中で利用可能なカスタム関数
 */
public class SqlFunctions {

    public static boolean notEmpty(Object value) {
        return value != null && !value.toString().isEmpty();
    }

}
```

`SqlTemplateContext` の実装クラスである `BeanPropertySqlTemplateContext` / `MapSqlTemplateContext` / `EmptyValueSqlTemplateContext` のメソッド `setEvaluationContextEditor` で、匿名クラス(ラムダ式)で処理を定義します。

```java
MapSqlTemplateContext templateContext = new MapSqlTemplateContext(Map.of("name", "%abc%"));

// EL式中のカスタム関数の登録
templateContext.setEvaluationContextEditor(c -> {
    try {
        c.registerFunction("notEmpty", SqlFunctions.class.getMethod("notEmpty", Object.class));
    } catch (NoSuchMethodException | SecurityException e) {
        throw new RuntimeException(e);
    }
});
```

SQLテンプレート中で登録したカスタム関数を呼び出すときには、`#関数名(...)` で使用します。

```sql
select * from Employee emp
/*BEGIN*/
where
    /*IF #notEmpty(name)*/
    name like /*name*/'S%'
    /*END*/
/*END*/
```

