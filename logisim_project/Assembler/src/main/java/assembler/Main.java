package assembler;

import java.io.File;

/**
 * @author Lydia BARAUKOVA
 */
public class Main {
    public static void main(String[] args) {
        File fin = new File("data/AssemblerIn.txt");
        File fout = new File("data/AssemblerOut.txt");
        new Converter().convertCode(fin,fout);
    }
}