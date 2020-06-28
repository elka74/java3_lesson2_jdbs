package client;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class FileClass {
    private static PrintWriter out;
    public static String fileName (String login){
        return "folderWithFiles/history_" + login + ".txt";
    }

    public static void createFile(String login){
        try{
            out = new PrintWriter(new FileOutputStream(fileName(login)), true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public static void stopFile(){
        if(out != null){
            out.close();
        }
    }
     public static void line (String message){
        out.println(message);
     }


     public static String lastLine(String login){
        if(!Files.exists(Paths.get(fileName(login)))){
            return "";

        }
        StringBuilder stb = new StringBuilder();
        try{
            List <String> ls = Files.readAllLines(Paths.get(fileName(login)));
            int stPos = 0;
            if (ls.size() >100){
                stPos = ls.size() - 100;
            }
            for (int i = stPos; i < ls.size(); i++){
                stb.append(ls.get(i)).append(System.lineSeparator());

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stb.toString();
     }

}
