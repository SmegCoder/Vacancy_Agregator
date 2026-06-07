package matve.vacancies.parser;

import org.junit.jupiter.api.Test;
import matve.vacancies.util.SalaryParser;

import static org.junit.jupiter.api.Assertions.*;

class SalaryParserTest {
    @Test
    void parsesSalaryRange() {
        SalaryParser.Salary salary = SalaryParser.parse("100 000 - 180 000 ₽");
        assertEquals(100000, salary.min().intValue());
        assertEquals(180000, salary.max().intValue());
        assertEquals("RUB", salary.currency());
    }

    @Test
    void parsesSalaryUntil() {
        SalaryParser.Salary salary = SalaryParser.parse("до 250 000 ₽");
        assertNull(salary.min());
        assertEquals(250000, salary.max().intValue());
    }
}
