package cn.xiaym.xchat;

import java.net.*;
import java.io.*;

import org.json.*;
import xutils.xconfig.xconfig;

import cn.xiaym.xchat.utils.*;
import cn.xiaym.xchat.plugin.*;
import cn.xiaym.xchat.plugin.dataTypes.*;
import cn.xiaym.xchat.events.*;

public class UserMain implements Runnable {
  private Socket s;
  private String addr;
  private BufferedWriter bw;
  private BufferedReader br;
  private String line;
  private boolean isLoggedIn;
  private String NickName;

  public UserMain(Socket s){
    this.s=s;
    try{
      this.addr=s.getInetAddress().toString() + ":" + s.getPort();
      this.bw=new BufferedWriter(new OutputStreamWriter(s.getOutputStream(),"UTF-8"));
      this.br=new BufferedReader(new InputStreamReader(s.getInputStream(),"UTF-8"));
    } catch(Exception e) {
      //Do nothing
    }
  }

  public String getNickname(){
    return NickName;
  }

  public void writeMsg(String msg, String type){
    JSONObject joe = new JSONObject();
    joe.put("type",type);
    joe.put("msg",msg);
    try{
      bw.write(joe.toString());
      bw.newLine();
      bw.flush();
    }catch(IOException e){
      //Do nothing
    }
  }

  public boolean userConnected(String NickName){
    for(String s:ServerMain.getUserList()){
      if(s.equals(NickName)){
        return true;
      }
    }
    
    return false;
  }

  @Override
  public void run(){
    Thread hbt = new Thread(new HeartBeat(s, Thread.currentThread(), this));
    hbt.start();

    try{
      continueRun(hbt);
    } catch(Exception e) {
      ServerMain.kill(NickName, s);
      hbt.interrupt();
      Thread.currentThread().interrupt();
    }
  }

  public void continueRun(Thread h) throws IOException {
    while ((line = br.readLine()) != null){
      JSONObject jo = new JSONObject();
      try{
        jo = new JSONObject(line);
        switch(jo.getString("type")){
          case "status":
            JSONObject jos = new JSONObject();
            jos.put("server_version","1");
            jos.put("authorization_required",ServerMain.getConfig().get("authorization_required","true"));
            bw.write(jos.toString());
            bw.newLine();
            bw.flush();
            break;
          case "login":
            NickName = jo.getString("nickname");
            String PassWord = jo.getString("password");
            if(userConnected(NickName)){
              NickName=null;
              writeMsg("???????????????!","kick");
              return;
            }
            if(!NickName.matches("[a-zA-Z0-9_]+")){
              ServerMain.removeUser(NickName);
              NickName=null;
              writeMsg("?????????????????? a-z A-Z 0-9 _ ??????!","kick");
              return;
            }
            if(NickName.length() > 16 || NickName.length() < 3){
              ServerMain.removeUser(NickName);
              NickName=null;
              writeMsg("??????????????? 3-16 ???????????????.","kick");
              return;
            }
            String arStatus = ServerMain.getConfig().get("authorization_required","true");
            if(!arStatus.equals("true")) {
              isLoggedIn=true;
              Logger.info("??????: "+NickName+" (%ip_address%)".replaceAll("%ip_address%",addr));
              ServerMain.newUser(NickName);
            } else {
              Boolean t = ServerMain.verifyPassword(NickName, PassWord);
              if(t){
                isLoggedIn=true;
                writeMsg("??????????????????","line");
                Logger.info("??????: "+NickName+" (%ip_address%)".replaceAll("%ip_address%",addr));
                ServerMain.newUser(NickName);
              } else {
                ServerMain.removeUser(NickName);
                NickName=null;
                writeMsg("??????????????????!","kick");
              }
            }
            break;
          case "message":
            UserChatEvent event = new UserChatEvent(jo.getString("msg"), new User(s, NickName));
            for(JavaPlugin p:PluginMain.getPlugins()){
              p.onUserChat(event);
            }
            if(isLoggedIn){
              if(!event.isCancelled()){
                Logger.info(NickName+" > "+jo.getString("msg"));
                ServerMain.sendMessage(NickName, jo.getString("msg"));
              }
            } else {
              ServerMain.kill(NickName, s);
            }
            break;
          case "command":
            if(!ServerMain.isCommand(jo.getString("msg"), new User(s, NickName))){
              if(!PluginMain.isHiddenCommand(jo.getString("msg"))){
                Logger.info(NickName+" ???????????????: "+jo.getString("msg")+" (??????????????????)");
              }
              writeMsg("????????????!", "line");
            } else {
              if(!PluginMain.isHiddenCommand(jo.getString("msg"))){
                Logger.info(NickName+" ???????????????: "+jo.getString("msg")+" (?????????)");
              }
            }
            break;
          default:
            s.close();
            break;
        }
      }catch(Exception e){
        ServerMain.kill(NickName, s);
        h.interrupt();
        Thread.currentThread().interrupt();
      }
    }
  }
}

class HeartBeat implements Runnable {
  private Socket s;
  private Thread pt;
  private UserMain c;
  public HeartBeat(Socket s, Thread parentThread, UserMain c){
    this.s=s;
    this.pt=parentThread;
    this.c=c;
  }

  @Override
  public void run(){
    try{
      while(true){
        s.sendUrgentData(0xFF);
        Thread.sleep(200);
      }
    } catch(Exception e) {
      ServerMain.kill(c.getNickname() ,s);

      //??????UserMain???HeartBeat ????????????
      pt.interrupt();
      Thread.currentThread().interrupt();
    }
  }

}
