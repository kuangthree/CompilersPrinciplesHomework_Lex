import java.io.*;
import java.util.*;

public class LexicalAnalyser {

    public static boolean completed = true;

    public static String lexConfigSrcName = "lex.yy.c";
    public static String lexConfigExecName = "lexyyc";

    public static List<String> srcFilePathList;

    static {
        srcFilePathList = new ArrayList<>();
    }

    public static boolean compileLexConfig() {
        File lexSrcFile = new File(lexConfigSrcName);
        if (!lexSrcFile.exists()) {
            System.err.println("Cannot Find Lex Config Source File.");
            System.err.println("Lex config file should be name as " + lexConfigSrcName + ".");
            return false;
        }
        String command = "gcc -o " + lexConfigExecName + " " + lexConfigSrcName;
        System.out.println("Compiling Lex config...");
        // System.out.println(command);
        try {
            Process ps = Runtime.getRuntime().exec(command);
            ps.waitFor();
            if (ps.exitValue() == 0) {
                System.out.println("Compile Lex Config Succeed.");
                return true;
            } else {
                System.err.println("Lexical Configeration Compile ERROR.");
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean checkInputSrcFiles(String[] filePaths) {
        if (filePaths.length == 0) {
            System.err.println("No Source File Input.");
            return false;
        }
        for (String filePath : filePaths) {
            File src = new File(filePath);
            if (!src.exists()) {
                System.err.println(filePath + " Does Not Exists.");
                return false;
            } else {
                srcFilePathList.add(filePath);
            }
        }
        return true;
    }

    public static void lexAnalyzer() {
        for(String src : srcFilePathList){
            String line = null;
            File srcFile = new File(src);
            String fileName = srcFile.getName();
            fileName = fileName.substring(0, fileName.lastIndexOf(".") == -1 ? fileName.length(): fileName.lastIndexOf("."));
            String formatStr = "%-20s %-15s %-15s %-15s";
            long startTime = System.currentTimeMillis();
            try{
                List<String> command = new ArrayList<>();
                command.add("sh");
                command.add("-c");
                command.add("./" + lexConfigExecName + " < " + src);
                ProcessBuilder processBuilder= new ProcessBuilder(command);
                Process ps = processBuilder.start();
                // System.out.println(command);
                BufferedInputStream inputStream = new BufferedInputStream(ps.getInputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                
                File tokenedFile = new File(fileName + ".tok");
                FileWriter fw = new FileWriter(tokenedFile);
                BufferedWriter writer = new BufferedWriter(fw);
                while((line = br.readLine()) != null){
                    String[] tokenInfo = line.split(" +");
                    System.out.println(line);
                    //System.out.println(String.format(formatStr, tokenInfo[0], tokenInfo[1], tokenInfo[2], tokenInfo[3]));
                    // writer.write(String.format(formatStr, tokenInfo[0], tokenInfo[1], tokenInfo[2], tokenInfo[3]));
                }
                writer.flush();
                writer.close();
                fw.close();

                BufferedInputStream errorStream = new BufferedInputStream(ps.getErrorStream());
                BufferedReader ebr = new BufferedReader(new InputStreamReader(errorStream));
                while((line = ebr.readLine()) != null){
                    System.out.println(line);
                }

                
                ps.waitFor();
                // br.close();
                // inputStream.close();
            } catch (Exception e){
                System.err.println();
            }
            

            long endTime = System.currentTimeMillis();
            System.out.printf("Lexical Analyzed Source File: " + fileName + " in %d ms.\n", endTime - startTime);
        }
    }

    public static void analyze(String[] filePaths) {
        if (!checkInputSrcFiles(filePaths)) {
            System.err.println("Lexical Analyer Quits. Due to invalid source files.");
            completed = false;
            return;
        }
        if (!compileLexConfig()) {
            System.err.println("Lexical Analyser Quits. Due to lex config compilation error.");
            completed = false;
            return;
        }
        long startTime = System.currentTimeMillis();
        lexAnalyzer();
        long endTime = System.currentTimeMillis();
        System.out.printf("Total Analyzing Time: %d ms.\n", endTime - startTime);
    }

    public static void main(String[] args) {
        analyze(args);
        if (!completed)
            System.out.println("Lexcial Analyzing Aborts.");
        else {
            System.out.println("Lexical Analyzing Finishes.");
        }
    }
}
