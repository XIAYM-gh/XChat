package cn.xiaym.xchat;

import java.io.*;
import java.net.*;

import org.jline.reader.*;
import org.jline.terminal.*;
import org.json.*;

class SendThread implements Runnable {
  private BufferedWriter bw;
  private Socket s;
  public SendThread(Socket s){
    this.s=s;
    try{
      this.bw=new BufferedWriter(new OutputStreamWriter(s.getOutputStream(),"UTF-8"));
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  @Override
  public void run(){
    try{
      Terminal terminal = TerminalBuilder.builder().system(true).build();
      LineReader lineReader = LineReaderBuilder.builder().terminal(terminal).build();

      while(true){
        String line = lineReader.readLine("");
        if(line.length() > 0){
          if(line.equals("stop")){
            System.exit(0);
          }
          JSONObject jo = new JSONObject();
          if(!line.startsWith("/")){
            jo.put("type","message");
            jo.put("msg",line);
          } else {
            jo.put("type","command");
            jo.put("msg",line.substring(1));
          }
          bw.write(jo.toString());
          bw.newLine();
          bw.flush();
        }
      }
    }catch(UserInterruptException|EndOfFileException e){
      try{
        s.close();
      } catch(Exception e_){
        //Do nothing
      }
    }catch(Exception e){
      e.printStackTrace();
    }
  }
}

class ReceiveThread implements Runnable {
  private BufferedReader br;
  private Socket s;
  private String line;

  public ReceiveThread(Socket s) {
    this.s=s;
    try{
      this.br=new BufferedReader(new InputStreamReader(s.getInputStream(),"UTF-8"));
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void run(){
    try{
      while ((line = br.readLine()) != null) {
        JSONObject jo = new JSONObject(line);
        switch(jo.getString("type")){
          case "line":
            PrintUtil.print(jo.getString("msg"));
            break;
          case "message":
            PrintUtil.print(jo.getString("user")+">> "+jo.getString("msg"));
            break;
          case "kick":
            PrintUtil.print("被服务器踢出:\n"+jo.getString("msg"));
            System.out.flush();
            System.exit(0);
            break;
          default:
            PrintUtil.print("[客户端-格式化失败] "+jo.getString("msg"));
            break;
        }
      }
    }catch(JSONException e){
      PrintUtil.print("一条数据JSON转化失败!");
    }catch(SocketException e){
      System.exit(0);
    }catch(Exception e){
      e.printStackTrace();
      System.exit(-1);
    }
  }
}

class HeartBeatThread implements Runnable {
  private Socket s;

  public HeartBeatThread(Socket s) {
    this.s=s;
  }

  @Override
  public void run(){
    try{
      while(true){
        s.sendUrgentData(0xFF);
        Thread.sleep(200);
      }
    }catch(Exception e){
      PrintUtil.print("连接已丢失..");
      System.exit(0);
    }
  }
}
