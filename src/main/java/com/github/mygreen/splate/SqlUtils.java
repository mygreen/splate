package com.github.mygreen.splate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicReference;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * 2Way-SQL機能の中で提供されるユーティリティクラス。
 * <p>MirageSQL/Seaser2からの持ち込みなので、既存のユーティリティクラスとは分けて定義する。</p>
 *
 * @version 0.2
 * @author T.TSUCHIE
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
        StringBuilder buf = new StringBuilder(100);
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
     * リソースをテキストとして読み込む。
     * <p>引数で指定したストリームは自動的にクローズします。</p>
     *
     * @param in リソース
     * @param encoding エンコーディング
     * @return 読み込んだテキスト
     * @throws IOException リソースの読み込みに失敗した場合にスローされます。
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

    /**
     * 文字列のメッセージダイジェストを作成します。
     * @param text 計算対象の文字列
     * @return メッセージダイジェスト
     */
    public static String getMessageDigest(final String text) {

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(text.getBytes(StandardCharsets.UTF_8));

            byte[] hash = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("not such algorithm name.", e);
        }

    }

    /**
     * SQL中の位置として行、列の位置を解決します。
     * @param sql SQL
     * @param position 位置
     * @return テンプレートの位置情報
     */
    public static Position resolveSqlPosition(final @NonNull String sql, final int position) {

        // 現在の位置より前の情報を切り出し、改行を検索する。
        final String before = sql.substring(0, position);

        int row = 1;

        final String[] searchWords = {"\r\n", "\r", "\n"};

        AtomicReference<CharSequence> foundStr = new AtomicReference<>();
        int lastIndex = 0;
        int index = 0;
        while((index = indexOfAny(before, index, foundStr, searchWords)) >= 0) {
            // 改行が見つかったら行数をカウントアップする。
            row++;

            index += foundStr.get().length();

            // 現在見つかっている改行の位置を保持しておく
            lastIndex = index;
        }

        // 最後に改行が見つかった位置から、SQLの位置を引けば列がわかる
        int col = position - lastIndex;
        if(lastIndex > 0) {
            col--;
        }

        // 行の切り出し
        final String after = sql.substring(lastIndex);
        String line = after;
        if(after != null && (index = indexOfAny(after, 0, null, searchWords)) >= 0) {
            line = after.substring(0, index);
        }

        final Position result = new Position();
        result.setCol(col);
        result.setRow(row);
        result.setLine(line);

        return result;

    }

    /**
     * <p>指定した複数の文字列から、最初に出現する位置のインデックスを返します。</p>
     * <p>CommonsLangの{@code StringUtils#indexOfAny}の持ち込み。</p>
     *
     * <ul>
     *   <li>検索対象の文字が {@code null}の場合は、{@code -1} を返します。</li>
     *   <li>検索文字が {@code null} または空の配列の場合は、{@code -1} を返します。</li>
     *   <li>検索文字に 空文字({@code ""})が含まれる場合、{@code 0}を返します。</li>
     *   <li></li>
     * </ul>
     *
     * @param str 検索対象の文字列
     * @param startPos 検索開始する位置。0から始まります。
     * @param foundStr 見つかった文字列。見つかった場合には値がセットされます。{@code null}の場合は値はセットされません。
     * @param searchStrs  検索する文字列
     * @return 初めに見つかった文字列のインデクスを返します。見つからない場合は、{@code -1}を返します。
     * @since 0.2
     */
    public static int indexOfAny(final CharSequence str, final int startPos,
            final AtomicReference<CharSequence> foundStr, final CharSequence... searchStrs) {

        if (str == null || searchStrs == null || searchStrs.length == 0) {
            return INDEX_NOT_FOUND;
        }

        // String's can't have a MAX_VALUEth index.
        int ret = Integer.MAX_VALUE;

        int tmp = 0;
        for (final CharSequence search : searchStrs) {
            if (search == null) {
                continue;
            }
            tmp = indexOf(str, search, startPos);
            if (tmp == INDEX_NOT_FOUND) {
                continue;
            }

            if (tmp < ret) {
                ret = tmp;

                // 見つかった文字列を保存する
                if(foundStr != null) {
                    foundStr.set(search);
                }
            }
        }

        return ret == Integer.MAX_VALUE ? INDEX_NOT_FOUND : ret;
    }

    /**
     * 文字列の位置検索で失敗したときの値.
     * @since 0.2
     */
    private static final int INDEX_NOT_FOUND = -1;

    /**
     * {@link CharSequence}に対する {@code indexOf(CharSequence)}の実装。
     * <p>CommonsLangの{@code CharSequenceUtils#indexOf}の持ち込み。</p>
     *
     * @param cs the {@code CharSequence} to be processed
     * @param searchChar the {@code CharSequence} to be searched for
     * @param start the start index
     * @return the index where the search sequence was found
     * @since 0.2
     */
    private static int indexOf(final CharSequence cs, final CharSequence searchChar, final int start) {
        if (cs instanceof String) {
            return ((String) cs).indexOf(searchChar.toString(), start);
        } else if (cs instanceof StringBuilder) {
            return ((StringBuilder) cs).indexOf(searchChar.toString(), start);
        } else if (cs instanceof StringBuffer) {
            return ((StringBuffer) cs).indexOf(searchChar.toString(), start);
        }
        return cs.toString().indexOf(searchChar.toString(), start);
    }
}
