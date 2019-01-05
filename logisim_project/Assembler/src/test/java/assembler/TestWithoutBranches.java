package assembler;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * @author Lydia BARAUKOVA
 */
public class TestWithoutBranches {

    private String code;

    @Before
    public void setupProgramWithoutBranches() {
        code = "sub sp,#12\n" // Agrandir la pile de 3*4 octets d’où le sp − 12
            + "movs r0,#0\n" // Placer dans un registre la valeur contenue dans la variable a
            + "str r0,[sp,#8]\n" // Stocker cette valeur dans la pile
            + "movs r1,#1\n" // Placer dans un registre la valeur contenue dans la variable b
            + "str r1,[sp,#4]\n" // Stocker cette valeur dans la pile
            + "ldr r1,[sp,#8]\n" // Charger dans le registre 1 la valeur contenue à la dernière
            + "ldr r2,[sp,#4]\n" // Charger dans le registre 2 la valeur contenue à l’avant dernière
            + "adds r1,r1,r2\n" // Additionner les valeurs des registres 1 et 2, stocker le résultat dans le registre 1
            + "str r1,[sp]\n" // Stocker le contenu du registre 1 à l’adresse pointée par le pointeur de pile
            + "#lol\n" // commentaire
            + "add sp,#12"; // Réduire la pile de 3*4 octets
    }

    @Test
    public void testProgramWithoutBranches() {
        String expected = "v2.0 raw\n"
                        + "b08c\n"
                        + "2000\n"
                        + "9008\n"
                        + "2101\n"
                        + "9104\n"
                        + "9908\n"
                        + "9a04\n"
                        + "1889\n"
                        + "9100\n"
                        + "b00c\n";
        String result = "v2.0 raw\n" + new Converter().convert(code);
        assertEquals(expected, result);
    }
}
