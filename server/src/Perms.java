package cn.xiaym.xchat;

import java.nio.file.*;
import java.nio.*;
import java.util.*;

import xutils.xconfig.xconfig;
import org.json.*;
import cn.xiaym.xchat.utils.*;

public class Perms {
  private static xconfig config;
  private static JSONArray groups;
  private static HashMap<String, JSONArray> group_perms = new HashMap<String, JSONArray>();
  private static HashMap<String, JSONArray> user_perms = new HashMap<String, JSONArray>();

  public static void init(){
    if(!Files.exists(Paths.get("data/"))){
      try{
        Files.createDirectories(Paths.get("data/"));
      } catch(Exception e) {
        ErrorUtil.trace(e);
      }
    }
    config = new xconfig("data/perms.properties");
    config.putIfAbsent("groups","[\"default\"]");
    config.putIfAbsent("group::default::desc","Default Group");
    config.putIfAbsent("group::default::perms","[\"xchat.basic.chat\"]");
    config.save();
    try{
      groups = new JSONArray(config.get("groups"));
      for(Object s_obj:groups){
        String s = s_obj.toString();
        JSONArray ja = new JSONArray(config.get("group::GROUPNAME::perms".replaceFirst("GROUPNAME",s)));
        group_perms.put(s, ja);
      }
    } catch(Exception e) {
      Logger.err("权限系统发生错误!");
      ErrorUtil.trace(e);
    }
  }

  public static String parseCommand(String line, boolean isUser){
    if(!line.startsWith("perms")){
      Logger.err("Perms.ParseCommand 调用错误");
      return "调用错误.";
    }
    if(isUser){
      return "只有控制台可以运行此指令.";
    }

    /*return "- 权限管理系统 - 帮助\n"+
           " perms groups - 显示当前的权限组列表\n"+
           " perms groupaddu <用户名> <权限组> - 将用户添加到权限组\n"+
           " perms groupaddp <权限> <权限组> - 将权限添加给权限组\n"+
           " perms groupdelu <用户名> <权限组> - 将用户从权限组中移出(归为 default 权限组)\n"+
           " perms groupdelp <权限> <权限组> - 将权限从权限组移除\n"+
           " perms useraddp <权限> <用户名> - 将权限添加给用户\n"+
           " perms userdelp <权限> <用户名> - 移除用户的权限";*/

    return "未知命令.";
  }
}
