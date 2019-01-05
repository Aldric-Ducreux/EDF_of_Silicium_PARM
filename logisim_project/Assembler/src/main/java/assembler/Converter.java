package assembler;

import java.io.*;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Lydia BARAUKOVA
 */
class Converter {

    private enum DataProcessingEnum {
        ands("0000"), eors("0001"), lsls("0010"), lsrs("0011"),
        asrs("0100"), adcs("0101"), sbcs("0110"), rors("0111"),
        tst("1000"), rsbs("1001"), cmp("1010"), cmn("1011"),
        orrs("1100"), muls("1101"), bics("1110"), mvns("1111");
        private final String code; // code de l'opération
        DataProcessingEnum(String code) { this.code = code; }
    }
    private enum ShiftAddSubMovEnum {
        lsls("000"), lsrs("001"), asrs("010"), movs("100"),
        adds("01100"), subs("01101"), addsimm("01110"), subsimm("01111");
        private final String code;
        ShiftAddSubMovEnum(String code) { this.code = code; }
    }
    private enum LoadStoreEnum {
        str("0"), ldr("1");
        private final String code;
        LoadStoreEnum(String code) {
            this.code = code;
        }
    }
    private enum MiscellaneousEnum {
        add("0"), sub("1");
        private final String code;
        MiscellaneousEnum(String code) { this.code = code; }
    }
    private enum ConditionalBranchEnum {
        beq("0000"), bne("0001"), bsc("0010"), bcc("0011"),
        bmi("0100"), bpl("0101"), bvs("0110"), bvc("0111"),
        bhi("1000"), bls("1001"), bge("1010"), blt("1011"),
        bgt("1100"), ble("1101"), bal("1110"), b("1110");
        private final String code;
        ConditionalBranchEnum(String code) { this.code = code; }
    }

    private class DataProcessing {
        String opcode(DataProcessingEnum op, String[] args) {
            int rdn = argToInt(args[0]);
            int rm = argToInt(args[1]);
            return "010000" + op.code + decToBin(rm,3) + decToBin(rdn,3);
        }
    }
    private class ShiftAddSubMov {
        String opcode(ShiftAddSubMovEnum op, String[] args) {
            int rd = argToInt(args[0]);
            switch(args.length) {
                case 2:
                    int imm8 = argToInt(args[1]);
                    return "00" + op.code + decToBin(rd,3) + decToBin(imm8,8);
                case 3:
                    int rn = argToInt(args[1]);
                    int rm = argToInt(args[2]);
                    return "00" + op.code + decToBin(rm,3) + decToBin(rn,3) + decToBin(rd,3);
                default: return null;
            }
        }
    }
    private class LoadStore {
        String opcode(LoadStoreEnum op, String[] args) {
            int rt = argToInt(args[0]);
            int i = args.length - 1;
            int imm8 = argToInt(args[i]);
            return "1001" + op.code + decToBin(rt,3) + decToBin(imm8,8);
        }
    }
    private class Miscellaneous {
        String opcode(MiscellaneousEnum op, String[] args) {
            int i = args.length - 1;
            int imm7 = argToInt(args[i]);
            return "10110000" + op.code + decToBin(imm7,7);
        }
    }
    private class ConditionalBranch {
        String opcode(ConditionalBranchEnum op, String[] args) {
            int imm8 = argToInt(args[0]);
            return "1101" + op.code + decToBin(imm8,8);
        }
    }

    private AbstractMap<String,Integer> data, labels;
    private DataProcessing dp;
    private ShiftAddSubMov sa;
    private LoadStore ls;
    private Miscellaneous ml;
    private ConditionalBranch cb;

    Converter() {
        data = new HashMap<>();
        labels = new HashMap<>();
        dp = new DataProcessing();
        sa = new ShiftAddSubMov();
        ls = new LoadStore();
        ml = new Miscellaneous();
        cb = new ConditionalBranch();
    }

    /**
     * Lit le code assembler dans un fichier, le convertit en code hexa et l'écrit dans un autre fichier.
     * @param fin Fichier d'entrée.
     * @param fout Fichier de sortie.
     */
    void convertCode(File fin, File fout) {
        writeNewCode(convert(readCode(fin)),fout);
    }

    private String readCode(File fin) {
        StringBuilder codeToConvert = new StringBuilder();
        FileInputStream fis;
        BufferedReader br;
        try {
            fis = new FileInputStream(fin);
            br = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = br.readLine()) != null) {
                codeToConvert.append(line);
                codeToConvert.append("\n");
            }
            br.close();
        } catch (IOException e) {
            System.err.println("IOException");
        }
        return codeToConvert.toString();
    }

    String convert(String code) {
        StringBuilder convertedCode = new StringBuilder();
        String[] splitCode = code.split("\n");
        readData(splitCode);
        readLabels(splitCode);
        for (String line: splitCode) {
            if (!line.equals("")) {
                String hex = lineToHex(line);
                if (!hex.equals("")) {
                    convertedCode.append(hex);
                    convertedCode.append("\n");
                }
            }
        }
        return convertedCode.toString();
    }

    private void writeNewCode(String convertedCode, File fout) {
        FileOutputStream fos;
        BufferedWriter bw;
        try {
            fos = new FileOutputStream(fout);
            bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write("v2.0 raw"); // ligne nécessaire pour que Logisim puisse comprendre le code converti
            bw.newLine();
            bw.write(convertedCode);
            bw.close();
        } catch (IOException e) {
            System.err.println("IOException");
        }
    }

    private void readData(String[] code) {
        boolean reading = false;
        int lineCounter = 0;
        for (String line: code) {
            if (line.equals(".data")) reading = true;
            else if (line.equals(".end")) reading = false;
            else if (reading) {
                String var = getLabel(line);
                if (!var.equals("")) data.put(var.toLowerCase(), lineCounter);
                lineCounter++;
            }
        }
    }

    private void readLabels(String[] code) {
        boolean reading = false;
        int programCounter = 0;
        for (String line: code) {
            if (line.equals(".text")) reading = true;
            else if (line.equals(".end")) reading = false;
            else if (reading) {
                if (isLabel(line)) labels.put(getLabel(line.toLowerCase()), programCounter);
                else programCounter++;
            }
        }
    }

    private String getLabel(String line) {
        if (isLabel(line)) return line.replace(":","");
        String[] splitLine = line.split("[ \t]+");
        for (String s: splitLine) {
            if (!s.equals("")) {
                if (isLabel(s)) return s.replace(":","");
            }
        }
        return "";
    }

    private boolean isLabel(String s) {
        String[] splitLabel = s.split("");
        int l = splitLabel.length - 1;
        return splitLabel[l].equals(":");
    }

    private String lineToHex(String line) {
        String binLine = lineToBin(line);
        if ("".equals(binLine)) return "";
        return binToHex(binLine);
    }

    private String lineToBin(String line) {
        // gestion des différences de registre
        line = line.toLowerCase();
        // gestion des espaces et des tabulations en trop
        String[] splitLine = line.split("[ \t]+");
        ArrayList<String> notEmptyElements = new ArrayList<>();
        for (String s: splitLine) {
            if (!s.equals("")) notEmptyElements.add(s);
        }
        if (notEmptyElements.size() == 1 || notEmptyElements.size() > 2) return "";
        String operation = notEmptyElements.get(0);

        String[] splitArgs = notEmptyElements.get(1).split(",");
        int nbArgs = splitArgs.length;

        switch(operation) {
            // opérations de ShiftAddSubMovEnum et de DataProcessingEnum
            case "lsls":
                switch(nbArgs) {
                    case 3: return sa.opcode(ShiftAddSubMovEnum.lsls,splitArgs);
                    case 2: return dp.opcode(DataProcessingEnum.lsls,splitArgs);
                    default: return null;
                }
            case "lsrs":
                switch(nbArgs) {
                    case 3: return sa.opcode(ShiftAddSubMovEnum.lsrs,splitArgs);
                    case 2: return dp.opcode(DataProcessingEnum.lsrs,splitArgs);
                    default: return null;
                }
            case "asrs":
                switch(nbArgs) {
                    case 3: return sa.opcode(ShiftAddSubMovEnum.asrs,splitArgs);
                    case 2: return dp.opcode(DataProcessingEnum.asrs,splitArgs);
                    default: return null;
                }
            // opérations de DataProcessingEnum
            case "ands": return dp.opcode(DataProcessingEnum.ands,splitArgs);
            case "eors": return dp.opcode(DataProcessingEnum.eors,splitArgs);
            case "adcs": return dp.opcode(DataProcessingEnum.adcs,splitArgs);
            case "sbcs": return dp.opcode(DataProcessingEnum.sbcs,splitArgs);
            case "rors": return dp.opcode(DataProcessingEnum.rors,splitArgs);
            case "tst": return dp.opcode(DataProcessingEnum.tst,splitArgs);
            case "rsbs": return dp.opcode(DataProcessingEnum.rsbs,splitArgs);
            case "cmp": return dp.opcode(DataProcessingEnum.cmp,splitArgs);
            case "cmn": return dp.opcode(DataProcessingEnum.cmn,splitArgs);
            case "orrs": return dp.opcode(DataProcessingEnum.orrs,splitArgs);
            case "muls": return dp.opcode(DataProcessingEnum.muls,splitArgs);
            case "bics": return dp.opcode(DataProcessingEnum.bics,splitArgs);
            case "mvns": return dp.opcode(DataProcessingEnum.mvns,splitArgs);
            // opérations de ShiftAddSubMovEnum
            case "adds":
                switch(splitArgs[2].split("")[0]) {
                    // si le dernier argument est un registre
                    case "r": return sa.opcode(ShiftAddSubMovEnum.adds,splitArgs);
                    // si le dernier argument est un immédiat
                    case "#": return sa.opcode(ShiftAddSubMovEnum.addsimm,splitArgs);
                    default: return null;
                }
            case "subs":
                switch(splitArgs[2].split("")[0]) {
                    // si le dernier argument est un registre
                    case "r": return sa.opcode(ShiftAddSubMovEnum.subs,splitArgs);
                    // si le dernier argument est un immédiat
                    case "#": return sa.opcode(ShiftAddSubMovEnum.subsimm,splitArgs);
                    default: return null;
                }
            case "movs": return sa.opcode(ShiftAddSubMovEnum.movs,splitArgs);
            // opérations de LoadStore
            case "str": return ls.opcode(LoadStoreEnum.str,splitArgs);
            case "ldr": return ls.opcode(LoadStoreEnum.ldr,splitArgs);
            // opérations de ConditionalBranchEnum
            case "beq": return cb.opcode(ConditionalBranchEnum.beq,splitArgs);
            case "bne": return cb.opcode(ConditionalBranchEnum.bne,splitArgs);
            case "bsc": return cb.opcode(ConditionalBranchEnum.bsc,splitArgs);
            case "bcc": return cb.opcode(ConditionalBranchEnum.bcc,splitArgs);
            case "bmi": return cb.opcode(ConditionalBranchEnum.bmi,splitArgs);
            case "bpl": return cb.opcode(ConditionalBranchEnum.bpl,splitArgs);
            case "bvs": return cb.opcode(ConditionalBranchEnum.bvs,splitArgs);
            case "bvc": return cb.opcode(ConditionalBranchEnum.bvc,splitArgs);
            case "bhi": return cb.opcode(ConditionalBranchEnum.bhi,splitArgs);
            case "bls": return cb.opcode(ConditionalBranchEnum.bls,splitArgs);
            case "bge": return cb.opcode(ConditionalBranchEnum.bge,splitArgs);
            case "blt": return cb.opcode(ConditionalBranchEnum.blt,splitArgs);
            case "bgt": return cb.opcode(ConditionalBranchEnum.bgt,splitArgs);
            case "ble": return cb.opcode(ConditionalBranchEnum.ble,splitArgs);
            case "bal": return cb.opcode(ConditionalBranchEnum.bal,splitArgs);
            case "b": return cb.opcode(ConditionalBranchEnum.b,splitArgs);
            // autres opérations
            case "add": return ml.opcode(MiscellaneousEnum.add,splitArgs);
            case "sub": return ml.opcode(MiscellaneousEnum.sub,splitArgs);
            default: return "";
        }
    }

    private int argToInt(String arg) {
        String cleanArg = arg.replace("[","").replace("]","");
        String firstChar = cleanArg.split("")[0];
        if (firstChar.equals("r")) return Integer.parseInt(cleanArg.replace("r", ""));
        if (firstChar.equals("#")) return Integer.parseInt(cleanArg.replace("#", ""));
        if (data.containsKey(cleanArg)) return data.get(cleanArg);
        if (labels.containsKey(cleanArg)) return labels.get(cleanArg);
        return 0;
    }

    private String binToHex(String bin) {
        int dec = Integer.parseInt(bin,2);
        return Integer.toString(dec,16);
    }

    private String decToBin(int dec, int bits) {
        return String.format("%" + bits + "s", Integer.toBinaryString(dec)).replace(' ', '0');
    }
}
