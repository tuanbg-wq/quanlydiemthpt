package com.quanly.webdiem.model.service.shared;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClassCodeSupportTest {

    @Test
    void buildFromClassNameShouldGenerateCodeFromCourseAndSuffix() {
        ClassCodeSupport.ClassCodeParts parts = ClassCodeSupport.buildFromClassName("k06", "10a1", 10);
        assertEquals("K06A1", parts.classCode());
        assertEquals("10A1", parts.className());
        assertEquals("A1", parts.suffix());
    }

    @Test
    void buildFromClassCodeShouldValidatePattern() {
        ClassCodeSupport.ClassCodeParts parts = ClassCodeSupport.buildFromClassCode("K06", "k06a2", 10);
        assertEquals("K06A2", parts.classCode());
        assertEquals("10A2", parts.className());
    }

    @Test
    void buildFromClassCodeShouldRejectWrongCoursePrefix() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ClassCodeSupport.buildFromClassCode("K06", "K07A1", 10)
        );
    }
}
