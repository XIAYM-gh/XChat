package cn.xiaym.xchat;

import java.net.*;
import java.io.*;
import java.nio.file.*;

import org.json.*;
import xutils.xconfig.xconfig;

public class ClientMain {
  public static int ProtocolVersion=1;
  public static void main(String[] args){
    try{
      PrintUtil.print("XChat 客户端已启动，协议版本: v"+ProtocolVersion);

      xconfig x = new xconfig("XChatClient.properties");
      if(!Files.exists(Paths.get("XChatClient.properties"))){
        x.newCommentLine("XChat Client - Configuration version v1");
        x.newCommentLine("XC By XIAYM");
        x.newLine("");
        x.newCommentLine("默认登录的服务器ip (如果留空则在启动时填写)");
        x.put("server_ip","");
        x.newCommentLine("默认使用的用户名 (如果留空则在启动时填写)");
        x.put("username","");
        x.newCommentLine("默认使用的密码 (如果留空则在启动时填写)");
        x.put("password","");
        x.save();
      }

      Console cons = System.console();

      String ip_all;

      if(x.get("server_ip","").equals("")){
        ip_all = cons.readLine("\r请输入服务器IP: ");
      } else {
        ip_all = x.get("server_ip");
      }

      String s_ip = "127.0.0.1";
      int s_port = 12345;
      String password = "";

      if(ip_all.contains(":")){
        s_ip = ip_all.split(":")[0];
        s_port = Integer.parseInt(ip_all.split(":")[1]);
      } else {
        s_ip = ip_all;
      }

      String nickn;

      if(x.get("username","").equals("")){
        nickn = cons.readLine("\r请输入昵称: ");
      } else {
        nickn = x.get("username");
      }

      PrintUtil.print("正在连接到: "+s_ip+":"+s_port);

      Socket s = null;

      try{
        s = new Socket(s_ip, s_port);
      } catch(Exception e) {
        PrintUtil.print("无法连接到服务器!");
        PrintUtil.print(e.toString());
        System.exit(-1);
      }

      BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(),"UTF-8"));

      PrintUtil.print("正在询问服务器信息..");

      JSONObject jo = new JSONObject();
      jo.put("type","status");

      bw.write(jo.toString());
      bw.newLine();
      bw.flush();

      BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream(),"UTF-8"));
      String line;
      String auth_req = "";
      int remote_protocol_version = 0;
      while ((line = br.readLine()) != null) {
        jo = new JSONObject(line);
        auth_req = jo.getString("authorization_required");
        remote_protocol_version = Integer.parseInt(jo.getString("server_version"));
        break;
      }

      if(ProtocolVersion != remote_protocol_version){
        PrintUtil.print("服务器协议版本不兼容，无法进入");
        System.exit(0);
      }

      if(auth_req.equals("true")){
        if(x.get("password","").equals("")){
          password = String.valueOf(cons.readPassword("\r请输入密码: "));
        } else {
          password = x.get("password");
        }
      }

      //发送登录请求
      PrintUtil.print("正在使用账户 "+nickn+" 进行登录...");
      jo = new JSONObject();
      jo.put("type","login");
      jo.put("nickname",nickn);
      jo.put("password",password);

      bw.write(jo.toString());
      bw.newLine();
      bw.flush();

      new Thread(new SendThread(s)).start();
      new Thread(new ReceiveThread(s)).start();
      new Thread(new HeartBeatThread(s)).start();

    }catch(SocketException e){
      PrintUtil.print("连接终止!");
      PrintUtil.print(e.toString());
      System.exit(0);
    }catch(Exception e){
      e.printStackTrace();
      System.exit(-1);
    }
  }
}
