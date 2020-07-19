package com.github.mygreen.sqltemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 2Way-SQL機能の中で提供されるユーティリティクラス。
 * <p>MirageSQL/Seaser2からの持ち込みなので、既存のユーティリティクラスとは分けて定義する。</p>
 *
 *
 * @author T.TSUCHIE
 *
 */
public class SqlUtils {

    /**
     * 空の文字列の配列です。
     */
    public static final String[] EMPTY_STRINGS = new String[0];

    /**
     * 文字列を置き換えます。
     * 置換対象の文字列がnullの場合は、結果として {@literal null} を返します。
     *
     * @param text テキスト
     * @param fromText 置き換え対象のテキスト
     * @param toText 置き換えるテキスト
     * @return 結果
     */
    public static final String replace(final String text, final String fromText, final String toText) {

        if (text == null || fromText == null || toText == null) {
            return null;
        }
        StringBuffer buf = new StringBuffer(100);
        int pos2 = 0;
        while (true) {
            int pos = text.indexOf(fromText, pos2);
            if (pos == 0) {
                buf.append(toText);
                pos2 = fromText.length();
            } else if (pos > 0) {
                buf.append(text, pos2, pos);
                buf.append(toText);
                pos2 = pos + fromText.length();
            } else {
                buf.append(text.substring(pos2));
                break;
            }
        }
        return buf.toString();
    }

    /**
     * 文字列が空かどうか判定します。
     *
     * @param text 文字列
     * @return 文字列が {@literal null} または空文字列なら {@literal true} を返します。
     */
    public static final boolean isEmpty(final String text) {
        return text == null || text.length() == 0;
    }

    /**
     * 文字列が空でないかどうか判定します。
     *
     * @param text 文字列
     * @return 文字列が {@literal null} または空文字列なら {@literal false} を返します。
     */
    public static final boolean isNotEmpty(final String text) {
        return !isEmpty(text);
    }

    /**
     * リソースをテキストとして読み込む。
     * <p>引数で指定したストリームは自動的にクローズします。</p>
     *
     * @param in リソース
     * @param encoding エンコーディング
     * @return 読み込んだテキスト
     */
    public static String readStream(final InputStream in, final String encoding) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try(in; out) {
            byte[] buf = new byte[1024 * 8];
            int length = 0;
            while((length = in.read(buf)) != -1){
                out.write(buf, 0, length);
            }

            return new String(out.toByteArray(), encoding);

        }
    }

//    /**
//     * 文字列を分割します。
//     * 分割対象の文字列が空文字またはnullの場合は、サイズ0の文字列の配列({@code new String[0]})を返します。
//     *
//     * @param str 文字列
//     * @param delim 分割するためのデリミタ
//     * @return 分割された文字列の配列
//     */
//    public static String[] split(final String str, final String delim) {
//        if (isEmpty(str)) {
//            return EMPTY_STRINGS;
//        }
//        List<String> list = new ArrayList<String>();
//        StringTokenizer st = new StringTokenizer(str, delim);
//        while (st.hasMoreElements()) {
//            list.add((String) st.nextElement());
//        }
//        return list.toArray(new String[list.size()]);
//    }
}
