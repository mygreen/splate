package com.github.mygreen.splate;

import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;


/**
 * {@link SqlUtils}のテスタ。
 *
 * @since 0.2
 * @author T.TSUCHIE
 *
 */
class SqlUtilsTest {

    @Test
    void testIndexOfAny() {

        assertThat(SqlUtils.indexOfAny(null, 0, null, "a")).isEqualTo(-1);
        assertThat(SqlUtils.indexOfAny("abc", 0, new AtomicReference<CharSequence>(), new String[0])).isEqualTo(-1);

        {
            AtomicReference<CharSequence> foundStr = new AtomicReference<>();
            int index = SqlUtils.indexOfAny("zzabyycdxx", 0, foundStr, "ab", "cd");
            assertThat(index).isEqualTo(2);
            assertThat(foundStr.get()).isEqualTo("ab");
        }

        {
            AtomicReference<CharSequence> foundStr = new AtomicReference<>();
            int index = SqlUtils.indexOfAny("zzabyycdxx", 0, foundStr, "cd", "ab");
            assertThat(index).isEqualTo(2);
            assertThat(foundStr.get()).isEqualTo("ab");
        }

        {
            AtomicReference<CharSequence> foundStr = new AtomicReference<>();
            int index = SqlUtils.indexOfAny("zzabyycdxx", 0, foundStr, "zab", "aby");
            assertThat(index).isEqualTo(1);
            assertThat(foundStr.get()).isEqualTo("zab");
        }

        {
            AtomicReference<CharSequence> foundStr = new AtomicReference<>();
            int index = SqlUtils.indexOfAny("zzabyycdxx", 0, foundStr, "", "abc");
            assertThat(index).isEqualTo(0);
            assertThat(foundStr.get()).isEqualTo("");
        }

    }

    @Test
    void testResolveSqlPosition() {

        String sql = "SELECT * "
                + "\r\n" + " FROM"
                + "\r\n" + " EMPLOYEE"
                + "\r\n" + " WHERE"
                + "\r\n" + "  /*IF id != null*/"
                + "\r\n" + "  id = /*id*/10"
                + "\r\n" + "  /*END*/"
                ;

        {
            // 先頭行
            Position result = SqlUtils.resolveSqlPosition(sql, 7);
            assertThat(result.getRow()).isEqualTo(1);
            assertThat(result.getCol()).isEqualTo(7);
            assertThat(result.getLine()).isEqualTo("SELECT * ");
        }

        {
            // 行の途中
            Position result = SqlUtils.resolveSqlPosition(sql, 46);
            assertThat(result.getRow()).isEqualTo(5);
            assertThat(result.getCol()).isEqualTo(8);
            assertThat(result.getLine()).isEqualTo("  /*IF id != null*/");
        }

    }
}
