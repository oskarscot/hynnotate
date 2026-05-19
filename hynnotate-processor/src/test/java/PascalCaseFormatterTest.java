import dev.onyxium.hynnotate.processor.util.StringHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PascalCaseFormatterTest {

    @Test
    public void test_pascal_case_formatter() {
        String helloThere = StringHelper.toPascalCase("helloThere");
        Assertions.assertEquals("HelloThere", helloThere);
    }

    @Test
    public void test_pascal_case_formatter_weird_string() {
        String helloThere = StringHelper.toPascalCase("helloThereWhatIsThisString");
        Assertions.assertEquals("HelloThereWhatIsThisString", helloThere);
    }
}
