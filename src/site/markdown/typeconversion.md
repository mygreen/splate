# 型変換処理

JDBCでは使用できるクラスタイプは限られているため、``java.time.LocalDate`` などはバインドパラメータとして使用する場合は ``java.sql.Date`` に変換しておく必要があります。

しかし、エンティティクラスなどをパラメータとして直接使用したいときは、わざわざ変換するのが面倒なときがあります。

そのようなときは、変換規則を登録しておくことで、そのままパラメータとして渡すことができます。

例として、``java.time.LocalDate`` の変換処理を示します。

## 1. SqlTemplateValueType の実装

型変換処理は、 ``SqlTemplateValueType`` を実装します。

- ``getBindVariableValue(...)`` にて、バインドパラメータの値を変換します。

    - 下記の例では、``java.util.Date`` に変換する例ですが、SpringのJdbcTemplateを使用する場合は、[SqlParameterValue](https://spring.pleiades.io/spring/docs/5.1.x/javadoc-api/org/springframework/jdbc/core/SqlParameter.html) に変換してもかまいません。

    - その場合、``SqlParameterValue(Types.BLOB, new SqlLobValue(value, lobHandler));`` のようにラージオブジェクトも処理ができます。

- ``getEmbeddedValue(...)`` にて、文字列置換のパラメータを変換します。

    - デフォルト実装では、``#toString()`` メソッドが呼ばれるので必要であれば実装します。


```java
public class LocalDateType implements SqlTemplateValueType<LocalDate> {

    // バインドパラメータの変換処理
    @Override
    public Object getBindVariableValue(LocalDate value) throws SqlTypeConversionException {
        Date sqlValue = (value != null ? Date.valueOf(value) : null);
        return sqlValue;
    }

    // 文字列置換のパラメータの変換処理
    // ※ default実装されており、必要なときにオーバーライドして実装します。
    @Override
    public String getEmbeddedValue(LocalDate value) {
        if(value == null) {
            return null;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        return value.format(formatter);
    }

}
```

## 2. SqlTemplateValueType の登録

``SqlTemplateValueType`` は、``SqlContext#registerValueType(...)`` にて登録します。

クラスタイプを指定することで、すべての ``LocalDate`` に対して変換が適用されます。

```java
MapSqlContext sqlContext = new MapSqlContext();

// パラメータ変数の設定
sqlContext.setVariable("birthday", LocalDate.of(2020, 10, 1));

// 変換処理の登録
sqlContext.registerValueType(LocalDate.class, new LocalDateType());
```

パラメータ名によって変換処理を変更したい場合は、パラメータ名を指定して登録します。

- クラスタイプの指定とパラメータ名の指定の両方が登録されている場合、パラメータ名の方が優先されます。

```java
MapSqlContext sqlContext = new MapSqlContext();

// パラメータ変数の設定
sqlContext.setVariable("birthday", LocalDate.of(2020, 10, 1));

// 変換処理の登録(birthdayのみに適用されます)
sqlContext.registerValueType("birthday", LocalDate.class, new LocalDateType());
```

ネストしたパラメータの場合は、パスを指定します。

  - ネストした要素がリスト/配列/マップの場合は、パラメータ名は ``data[1].value`` のようにインデックスやキー名を指定していても、変換処理の登録は ``data.value`` のインデックスやキー名を省略した形式で登録してください。


```java
MapSqlContext sqlContext = new MapSqlContext();

// ネストしたパラメータ変数の設定
Person pserson = new Person();
pserson.setBirthday(LocalDate.of(2020, 10, 1));

sqlContext.setVariable("person", pserson);

// 変換処理の登録(person.birthdayのみに適用されます)
sqlContext.registerValueType("person.birthday", LocalDate.class, new LocalDateType());
```

## 3. 列挙型の変換処理

列挙型の実態は ``java.lang.Enum`` の具象クラスであるため、全ての列挙型に対する変換処理は次のように実装します。

```java
// 汎用的な列挙型をordinalに変換する処理
public class EnumOrdinalType<T extends Enum<T>> implements SqlTemplateValueType<T> {

    @Override
    public Object getBindVariableValue(T value) throws SqlTypeConversionException {
        Integer sqlValue = (value != null ? value.ordinal() : null);
        return sqlValue;
    }

}
```

変換処理の登録は、 ``Enum.class`` に対して登録します。

```java
// 列挙型の定義
enum Color {RED, BLUE, YELLOW};

// パラメータ変数の設定
MapSqlContext sqlContext = new MapSqlContext();
sqlContext.setVariable("color", Color.RED);

// 変換処理の登録(全ての列挙型に適用されます)
sqlContext.registerValueType(Enum.class, new EnumOrdinalType());


```
