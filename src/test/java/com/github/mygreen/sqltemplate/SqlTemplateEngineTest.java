package com.github.mygreen.sqltemplate;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes=TestConfig.class)
public class SqlTemplateEngineTest {

    @Autowired
    SqlTemplateEngine sqlTemplateEndine;

    @BeforeEach
    void setUp() throws Exception {
    }

    @Test
    void test() {
        fail("まだ実装されていません");
    }
}
