package cn.xiaym.xchat;

import java.io.*;
import java.util.*;
import java.text.*;

class PrintUtil {
  public static void print(String str){
    String[] str_split = str.split("\n");
    for(String str_:str_split){

      StringBuilder StrAppended = new StringBuilder();
      StrAppended.append("\r");
      StrAppended.append(new SimpleDateFormat("[HH:mm:ss] ").format(new Date(System.currentTimeMillis())));
      StrAppended.append(str_);

      System.out.println(StrAppended.toString());
    }

    PrintUtil.flush();
  }

  public static void print(Object obj){
    PrintUtil.print(String.valueOf(obj));
  }

  public static void flush(){
    System.out.flush();
    System.err.flush();
    System.out.print("\r> ");
  }
}


