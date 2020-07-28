# このプロジェクトについて

splate（エス・プレート）は、 **2Way-SQL** 機能のみを [S2JDBC](http://s2container.seasar.org/2.4/ja/s2jdbc.html) から分離し、使いやすくしたライブラリです。

このライブラリは、SpringFrameworkに依存しています。理由は以下の通りです。

- SpringFrameworkに依存している理由として、普段、Javaで開発するときには、多くがSpringBootを利用しいる状況にあるためです。
- そのため、無理にSpringFrameworkに依存しないように作ってもコード量が増えるだけで、あまりメリットがないと考えられます。
- SpringFrameworkに依存しないで2Way-SQLを利用するならば、他の多くのライブラリ（ [Doma2](https://doma.readthedocs.io/) / [DBFlute](http://dbflute.seasar.org/) / [Mirage-SQL](https://github.com/mirage-sql/mirage) / [uroboroSQL](https://future-architect.github.io/uroborosql-doc/) ）が既に存在するので、そちらを利用することを検討した方がよいかもしれません。
