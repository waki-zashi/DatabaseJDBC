package src.util;

import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class AppLogger {

    private static JTextArea logArea;

    public static void setLogArea(JTextArea area){
        logArea = area;
    }

    public static void log(String message){

        String line = "["+ LocalDateTime.now()+"] "+message;

        if(logArea!=null){
            logArea.append(line+"\n");
        }

        try(FileWriter fw = new FileWriter("library.log",true)){

            fw.write(line+"\n");

        }catch(IOException ignored){}
    }
}