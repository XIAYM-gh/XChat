package cn.xiaym.xchat.utils;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

import static org.fusesource.jansi.Ansi.*;
import static org.fusesource.jansi.Ansi.Color.*;

public class Logger {
  public static void out(String str, String type, String typecolor, String textcolor){
    String[] str_split = str.split("\n");
    for(String str_:str_split){

      String time=new SimpleDateFormat("HH:mm:ss").format(new Date(System.currentTimeMillis()));
      System.console().printf("\r["+time+" "+ansi().fgBright(ConvertColor(typecolor)).bold().a(type).reset()+"] "+ansi().fgBright(ConvertColor(textcolor)).a(str_).reset()+"%n");
    }

    Logger.flush();
  }

  public static void info(Object obj){
    Logger.out(String.valueOf(obj), "INFO", "default", "default");
  }

  public static void warn(Object obj){
    Logger.out(String.valueOf(obj), "WARN", "yellow", "yellow");
  }

  public static void err(Object obj){
    Logger.out(String.valueOf(obj), "ERROR", "red", "red");
  }

  public static void success(Object obj){
    Logger.out(String.valueOf(obj), "INFO", "default", "green");
  }

  public static void flush(){
    System.out.flush();
    System.err.flush();
    System.console().printf("\r> ");
  }

  public static Color ConvertColor(String colorName){
    switch(colorName.toLowerCase()){
      case "red":
        return RED;
      case "blue":
        return BLUE;
      case "purple":
      case "magenta":
        return MAGENTA;
      case "black":
        return BLACK;
      case "white":
        return WHITE;
      case "yellow":
        return YELLOW;
      case "cyan":
      case "grey":
        return CYAN;
      case "green":
        return GREEN;
      case "default":
        return DEFAULT;
      default:
        return DEFAULT;
    }
  }

  public static void Test(){
    Logger.Test("default");
    Logger.Test("red");
    Logger.Test("blue");
    Logger.Test("purple");
    Logger.Test("black");
    Logger.Test("white");
    Logger.Test("yellow");
    Logger.Test("cyan");
    Logger.Test("green");
  }

  public static void Test(String color){
    Logger.out(color + " test", "TESTING", "blue", color);
  }

  static {
    // 屏蔽 System.out/err.print(ln)()
    // 用System.out/err是不标准的 xD
    try{
      ByteArrayOutputStream os_stdout = new ByteArrayOutputStream(); 
      ByteArrayOutputStream os_stderr = new ByteArrayOutputStream();

      PrintStream ps_stdout = new PrintStream(os_stdout);
      PrintStream ps_stderr = new PrintStream(os_stderr);

      System.setOut(ps_stdout);
      System.setErr(ps_stderr);

      // 标准输出处理
      new Thread( () -> {
        String line = "";
        while((line = os_stdout.toString()).length() > 0){
          Logger.info("[STDOUT] "+line);
          os_stdout.reset();
        }
      }).start();

      // 标准错误处理
      new Thread( () -> {
        String line = "";
        while((line = os_stderr.toString()).length() > 0){
          Logger.err("[STDERR] "+line);
          os_stderr.reset();
        }
      }).start();

    } catch(Exception e) {
      Logger.err("挂钩System.err/out失败，程序正在终止!");
      System.exit(-1);
    }
  }
}


