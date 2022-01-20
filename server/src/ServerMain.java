package cn.xiaym.xchat;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.nio.file.*;

import xutils.xconfig.xconfig;
import org.json.*;
import org.jline.terminal.*;
import org.jline.reader.*;

import cn.xiaym.xchat.plugin.*;
import cn.xiaym.xchat.plugin.dataTypes.*;
import cn.xiaym.xchat.utils.*;

public class ServerMain {
  private static xconfig x = new xconfig("XChatServer.properties");
  private static List<String> users = null;
  protected static List<Socket> Sockets = Collections.synchronizedList(new ArrayList<Socket>());

  public static void main(String[] args){

    int ProtocolVersion = 1;

    users=Collections.synchronizedList(new ArrayList<String>());

    Logger.info("XChat Server 已启动, 协议版本 v"+ProtocolVersion);

    new Thread(new ConsoleInput()).start();

    if(!Files.exists(Paths.get("XChatServer.properties"))){
      x.newCommentLine("服务器启动的端口 (默认 12345)");
      x.put("server_port","12345");
      x.newCommentLine("启用 Debug 构建 标识(可关闭)");
      x.put("debug_build","true");
      x.newCommentLine("是否启用密码安全验证 (默认 true)");
      x.put("authorization_required","true");
      x.save();
    }

    Logger.info("正在尝试加载插件...");
    PluginMain.init();
    Logger.info("插件已加载完成!");

    Logger.info("服务器启动端口: "+x.get("server_port","12345"));
    if(x.get("authorization_required","true").equals("true")){
      Logger.success("服务器已启用密码安全验证.");
    }
    
    try{
      ServerSocket ss = new ServerSocket(Integer.parseInt(x.get("server_port","12345")));
      while(true){
        Socket s = ss.accept();
        Sockets.add(s);
        new Thread(new UserMain(s)).start();
      }
    } catch(Exception e) {
      Logger.err("服务器启动失败或客户端操作发生错误!");
      e.printStackTrace();
      System.exit(-1);
    }
  }

  public static void RemoveUser(Socket s){
    Sockets.remove(s);
  }

  public static xconfig getConfig(){
    return x;
  }

  public static List<String> getUserList(){
    return users;
  }

  public static void newUser(String username){
    users.add(username);
  }

  public static void removeUser(String username){
    users.remove(users.indexOf(username));
  }

  public static List<Socket> getList(){
    return Sockets;
  }

  public static void setUserConfig(String username, String key, String value){
    if(key.equals("password")){
      return;
    }

    Path datap = Paths.get("data/users/");
    if(!Files.exists(datap)){
      try{
        Files.createDirectories(datap);
      } catch(Exception e) {
        e.printStackTrace();
      }
    }

    xconfig tempx = new xconfig("data/users/"+username+".properties");
    tempx.set(key, value);
    tempx.save();
  }

  public static String getUserConfig(String username, String key){
    return new xconfig("data/users/"+username+".properties").get(key);
  }

  public static boolean verifyPassword(String nick, String pass){
    Path datap = Paths.get("data/users/");
    if(!Files.exists(datap)){
      try{
        Files.createDirectories(datap);
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
    xconfig tempx = new xconfig("data/users/"+nick+".properties");
    if(!tempx.has("password")){
      tempx.set("password",SHAUtil.SHA256(pass));
      tempx.save();
      return true;
    }else if(tempx.get("password").equals(SHAUtil.SHA256(pass))){
      return true;
    }

    return false;
  }

  public static void sendMessage(String user, String msg){
    for(Socket s:Sockets){
      JSONObject j = new JSONObject();
      j.put("type","message");
      j.put("user",user);
      j.put("msg",msg);
      try{
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(),"UTF-8"));
        bw.write(j.toString());
        bw.newLine();
        bw.flush();
      } catch(Exception e) {
        // :P
      }
    }
  }

  public static void kill(String nickname, Socket s){
    try{
      if(Sockets.contains(s)){
        s.close();
        Sockets.remove(s);

        if(nickname != null && (users.indexOf(nickname) != -1)){
          Logger.info("退出: "+nickname);
          users.remove(users.indexOf(nickname));
        }

      }
    } catch(Exception e) {
      //emmm
    }
  }

  //CONSOLE
  public static boolean isCommand(String cmd){
    for(JavaPlugin p:PluginMain.getPlugins()){
      try{
        if(p.onCommand(cmd, new CommandSender(true))){
          return true;
        }
        if(cmd.startsWith("perms")){
          Logger.info(Perms.parseCommand(cmd, false));
          return true;
        }
      } catch(Exception e) {
        //没必要抛出
      }
    }

    return false;
  }

  //USER
  public static boolean isCommand(String cmd, User user){
    for(JavaPlugin p:PluginMain.getPlugins()){
      try{
        if(p.onCommand(cmd, new CommandSender(user))){
          return true;
        }
        if(cmd.startsWith("perms")){
          user.sendLine(Perms.parseCommand(cmd, true));
          return true;
        }
      } catch(Exception e) {
        //没必要抛出
      }
    }

    return false;
  }

  public static void stopServer(){
    try{
      Logger.info("正在停止 XChat 服务器...");
      //踢出全部用户并等待一段时间
      for(Socket s:Sockets){
        try{
          s.close();
        } catch(Exception e) {
          //关socket不会有什么报错的(确信
        }
      }

      for(JavaPlugin p:PluginMain.getPlugins()){
        try{
          p.onDisable();
        } catch(Exception e) {
          e.printStackTrace();
        }
      }
      Thread.sleep(500);
      Logger.success("XChat 服务器已停止.");
      Logger.flush();
      System.exit(0);
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
}

class ConsoleInput implements Runnable {
  public ConsoleInput(){}

  @Override
  public void run(){
    try{
      Terminal terminal = TerminalBuilder.builder().system(true).build();
      LineReader lineReader = LineReaderBuilder.builder().terminal(terminal).build();

      while (true){
        String line = lineReader.readLine("");
        if(line.length() > 0){
          this.parseCommand(line);
        }else{
          Logger.flush();
        }
      }

    } catch(UserInterruptException|EndOfFileException e){
      ServerMain.stopServer();
      Logger.flush();
    } catch(Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }

  public void parseCommand(String cmd){
    
    if(cmd.startsWith("say")){
      if(cmd.length()>4){
        ServerMain.sendMessage("Server", cmd.substring(4));
        Logger.info("[Server] "+cmd.substring(4));
      } else {
        Logger.info("用法: say <内容>");
      }
      return;
    }

    if(cmd.equals("stop")){
      ServerMain.stopServer();
      return;
    }

    if(ServerMain.isCommand(cmd)){
      return;
    }

    Logger.info("未知命令!");
  }
}
