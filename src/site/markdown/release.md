# リリースノート

## ver 0.3 - 2022-01-10

- [#16](https://github.com/mygreen/splate/pull/16) テスト用の依存ライブラリのアップデート

    - logbackを最新版 `1.2.10` に変更。

    - lombokを最新版 `1.18.22` に変更。

- [#17](https://github.com/mygreen/splate/pull/17) SQLテンプレート中に存在しないプロパティやキーを指定したときに無視するオプションを追加

    - `SqlTemplateContext` の実装クラスである `BeanPropertySqlTemplateContext` / `MapSqlTemplateContext` / `EmptyValueSqlTemplateContext` に、メソッド`setIgnoreNotFoundProperty(boolean)` を追加。

    - 値を `true` に設定すると、SQLテンプレート中に存在しないプロパティや変数を指定したときに `null` として扱うようになります。

- [#17](https://github.com/mygreen/splate/pull/17) `MapSqlTemplateContext` のバインド変数を追加するメソッド名を変更。

    - `setVariable` ⇒ `addVariable` 

    - `setVariables` ⇒ `addVariables` 

- [#18](https://github.com/mygreen/splate/pull/18) EL式の `EvaluationContext` を編集するメソッドを追加。

    - メソッド `SqlTemplateContext#setEvaluationContextEditor(EvaluationContext)` を追加し、SQLテンプレート評価前に実行されるコールバック処理を設定できます。

## ver 0.2.1 - 2021-01-30

- [#13](https://github.com/mygreen/splate/pull/13) フィールド名や引数名の間違い

    - ``valueTypeRestRegistry`` -> ``valueTypeRegistry``


## ver 0.2 - 2020-08-02

- [#4](https://github.com/mygreen/splate/pull/4) ライブラリの依存関係のバージョンの見直し

    - SpringFramework依存関係を v5.1 -> v5.0 に変更。

- [#5](https://github.com/mygreen/splate/pull/5) 役割をより明確にするために一部のクラス名を変更。

    - SqlContext -> SqlTemplateContext

    - BeanPropertySqlContext -> BeanPropertySqlTemplateContext

    - MapSqlContext -> MapSqlTemplateContext

    - EmptyValueSqlContext -> EmptyValueSqlTemplateContext

    - ProcessContext -> NodeProcessContext

- [#7](https://github.com/mygreen/splate/pull/7) エラー発生時のテンプレートの位置などを詳細に出す用変更

- [#8](https://github.com/mygreen/splate/pull/8) 使用していないクラス、メソッドを削除

- [#9](https://github.com/mygreen/splate/pull/9) / [#11](https://github.com/mygreen/splate/pull/11) 静的解析の指摘に対する修正


