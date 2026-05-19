package dev.onyxium.hynnotate.processor.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PascalCaseFormatterTest {

    @Test
    void pascalCases_a_simple_camel_case_string() {
        assertThat(StringHelper.toPascalCase("helloThere")).isEqualTo("HelloThere");
    }

    @Test
    void pascalCases_a_long_camel_case_string() {
        assertThat(StringHelper.toPascalCase("helloThereWhatIsThisString"))
                .isEqualTo("HelloThereWhatIsThisString");
    }
}
