package assembler;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * @author Lydia BARAUKOVA
 */
public class TestWithBranches {

    private String code;

    @Before
    public void setupProgramWithBranches() {
        code = ";=============================================================\n"
                + "; 1 programme de test : MAX = max(A, B)\n"
                + ";=============================================================\n"
                + ".data\n"
                +     "A: .word 0xf\n" // A = 15
                +     "B: .word 0xff\n" // B = 255
                +     "MAX: .word 0x0\n" // MAX = 0
                + ".end\n"
                + ".text\n"
                +     "LDR R0,A\n" // R0 = A
                +     "LDR R1,B\n" // R1 = B
                + "if:\n"
                +     "CMP R0,R1\n" // comparer R0 à R1
                +     "BMI else\n" // si R0 - R1 < 0, passer à else
                +     "STR R0,MAX\n" // sinon, MAX = R0
                +     "B endif\n" // passer à endif
                + "else:\n"
                +     "STR R1,MAX\n" // si on est à else, MAX = R1
                + "endif:\n" // endif
                + ".end";
    }

    @Test
    public void testProgramWithBranches() {
        String expected = "v2.0 raw\n"
                + "9800\n" // ldr 0 0
                + "9901\n" // ldr 1 1
                + "4288\n" // cmp 0 1
                + "d406\n" // bmi 6
                + "9002\n" // str 0 2
                + "de07\n" // b 7
                + "9102\n"; // str 1 2
        String result = "v2.0 raw\n" + new Converter().convert(code);
        assertEquals(expected, result);
    }
}
