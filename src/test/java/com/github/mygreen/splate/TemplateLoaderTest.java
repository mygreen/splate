package com.github.mygreen.splate;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;


/**
 * {@link TemplateLoader}のテスタ。
 *
 *
 * @author T.TSUCHIE
 *
 */
class TemplateLoaderTest {

    private TemplateLoader templateLoader;

    private ResourceLoader resourceLoader;

    @BeforeEach
    void setUp() {
         this.templateLoader = new TemplateLoader();
         this.resourceLoader = new DefaultResourceLoader();
    }

    @DisplayName("接尾語指定しない")
    @Test
    void testLoadByLocation_noSuffix() {

        String location = "template/suffix.sql";
        Optional<String> suffixName = Optional.empty();

        String actual = templateLoader.loadByLocation(location, resourceLoader, "UTF-8", suffixName);
        assertThat(actual).isEqualTo("select * from test limit /*$limit*/;");
    }

    @DisplayName("接尾語指定 - 接尾語付きのSQLファイルが存在する場合")
    @Test
    void testLoadByLocation_suffix() {

        String location = "template/suffix.sql";
        Optional<String> suffixName = Optional.of("oracle");

        String actual = templateLoader.loadByLocation(location, resourceLoader, "UTF-8", suffixName);
        assertThat(actual).isEqualTo("select * from (select * from test) where rownum < /*$limit*/;");
    }

    @DisplayName("接尾語指定 - 接尾語付きのSQLファイルが存在しない場合")
    @Test
    void testLoadByLocation_noexist_suffix() {

        String location = "template/suffix2.sql";
        Optional<String> suffixName = Optional.of("oracle");

        String actual = templateLoader.loadByLocation(location, resourceLoader, "UTF-8", suffixName);
        assertThat(actual).isEqualTo("select * from test2 limit /*$limit*/;");
    }

    @DisplayName("接尾語指定 - 拡張子がない場合")
    @Test
    void testLoadByLocation_nosuffix_noExtention() {

        String location = "template/suffix";
        Optional<String> suffixName = Optional.empty();

        String actual = templateLoader.loadByLocation(location, resourceLoader, "UTF-8", suffixName);
        assertThat(actual).isEqualTo("/*no_extension*/select * from test limit /*$limit*/;");
    }

    @DisplayName("接尾語指定 - 拡張子がない場合")
    @Test
    void testLoadByLocation_suffix_noExtention() {

        String location = "template/suffix";
        Optional<String> suffixName = Optional.of("oracle");

        String actual = templateLoader.loadByLocation(location, resourceLoader, "UTF-8", suffixName);
        assertThat(actual).isEqualTo("/*no_extension*/select * from (select * from test) where rownum < /*$limit*/;");
    }

    @Test
    void testLoadByResource() throws Exception {

        Resource resource = new ByteArrayResource("select * from test limit /*$limit*/;".getBytes("UTF-8"));

        String actual = templateLoader.loadByResource(resource, "UTF-8");

        assertThat(actual).isEqualTo("select * from test limit /*$limit*/;");

    }


}
