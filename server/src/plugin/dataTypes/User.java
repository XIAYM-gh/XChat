package cn.xiaym.xchat.plugin.dataTypes;

import java.net.*;
import java.io.*;

import org.json.*;

public class User {
  private Socket userSocket;
  private String currentUsername="";

  public User(Socket s, String u){
    this.userSocket = s;
    this.currentUsername = u;
  }

  public Socket getSocket(){
    return this.userSocket;
  }

  public String getName(){
    return this.currentUsername;
  }

  public void sendLine(String line){
    JSONObject j = new JSONObject();
    j.put("type","line");
    j.put("msg",line);
    try{
      BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(this.userSocket.getOutputStream(),"UTF-8"));
      bw.write(j.toString());
      bw.newLine();
      bw.flush();
    } catch(Exception e) {
      // :P
    }

  }
}
