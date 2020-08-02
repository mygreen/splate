package com.github.mygreen.splate.type;

import static org.assertj.core.api.Assertions.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * {@link SqlTemplateValueType}のテスタ
 *
 *
 * @author T.TSUCHIE
 *
 */
class SqlTemplateValueTypeRegistryTest {

    private SqlTemplateValueTypeRegistry registry;

    @BeforeEach
    void setUp() throws Exception {
        this.registry = new SqlTemplateValueTypeRegistry();
    }

    private enum Color {
        BLUE, RED, YELLOW;
    }

    @Test
    void testAddStrippedPropertyPaths_noParen() {

        List<String> strippedPaths = new ArrayList<>();
        String propertyPath = "abc";

        registry.addStrippedPropertyPaths(strippedPaths, "", propertyPath);

        assertThat(strippedPaths).isEmpty();

    }

    @Test
    void testAddStrippedPropertyPaths_nest() {

        List<String> strippedPaths = new ArrayList<>();
        String propertyPath = "abc.efg";

        registry.addStrippedPropertyPaths(strippedPaths, "", propertyPath);

        assertThat(strippedPaths).isEmpty();

    }

    @Test
    void testAddStrippedPropertyPaths_index() {

        List<String> strippedPaths = new ArrayList<>();
        String propertyPath = "abc[1]";

        registry.addStrippedPropertyPaths(strippedPaths, "", propertyPath);

        assertThat(strippedPaths).containsExactly("abc");

    }

    @Test
    void testAddStrippedPropertyPaths_nested_index() {

        List<String> strippedPaths = new ArrayList<>();
        String propertyPath = "abc[1].efg";

        registry.addStrippedPropertyPaths(strippedPaths, "", propertyPath);

        assertThat(strippedPaths).containsExactly("abc.efg");

    }

    @Test
    void testAddStrippedPropertyPaths_nested_index2() {

        List<String> strippedPaths = new ArrayList<>();
        String propertyPath = "abc[1].efg[key]";

        registry.addStrippedPropertyPaths(strippedPaths, "", propertyPath);

        assertThat(strippedPaths).containsExactly("abc.efg[key]", "abc.efg", "abc[1].efg");

    }

    @Test
    void testFindValueType_matchPath() {

        registry.register("birthday", LocalDate.class, new LocalDateType());

        SqlTemplateValueType<?> valueType = registry.findValueType(LocalDate.class, "birthday");
        assertThat(valueType).isNotNull();

    }

    @Test
    void testFindValueType_matchPath_notType() {

        registry.register("birthday", LocalDate.class, new LocalDateType());

        SqlTemplateValueType<?> valueType = registry.findValueType(Date.class, "birthday");
        assertThat(valueType).isNull();

    }

    @Test
    void testFindValueType_matchNestedPath() {

        registry.register("person.birthday", LocalDate.class, new LocalDateType());

        SqlTemplateValueType<?> valueType = registry.findValueType(LocalDate.class, "person[key].birthday");
        assertThat(valueType).isNotNull();

    }

    @Test
    void testFindValueType_matchType() {

        registry.register(LocalDate.class, new LocalDateType());

        SqlTemplateValueType<?> valueType = registry.findValueType(LocalDate.class, "person[key].birthday");
        assertThat(valueType).isNotNull();

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void testFindValueType_matchEnum() {

        registry.register(Enum.class, new EnumOrdinalType());

        SqlTemplateValueType<?> valueType = registry.findValueType(Color.class, "color");
        assertThat(valueType).isNotNull();

    }


}
