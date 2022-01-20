package cn.xiaym.xchat.plugin;

import java.io.*;
import java.net.*;

import cn.xiaym.xchat.utils.*;
import cn.xiaym.xchat.plugin.dataTypes.*;
import cn.xiaym.xchat.events.*;

abstract class PluginCore {
  public void onEnable(){}

  public void onDisable(){}

  public boolean onCommand(String cmd, CommandSender s){
    return false;
  }

  public void onUserJoin(UserJoinEvent event){}

  public void onUserLeave(UserLeaveEvent event){}

  public void onUserChat(UserChatEvent event){}
}

public class JavaPlugin extends PluginCore {
  private String name;
  private String version;
  private String author;
  private boolean Enabled;

  public void info(Object obj){
    Logger.info("[%s] %s".replaceFirst("%s",this.name).replaceFirst("%s",String.valueOf(obj)));
  }

  public void warn(Object obj){
    Logger.warn("[%s] %s".replaceFirst("%s",this.name).replaceFirst("%s",String.valueOf(obj)));
  }

  public void err(Object obj){
    Logger.err("[%s] %s".replaceFirst("%s",this.name).replaceFirst("%s",String.valueOf(obj)));
  }

  public void success(Object obj){
    Logger.success("[%s] %s".replaceFirst("%s",this.name).replaceFirst("%s",String.valueOf(obj)));
  }

  public void setName(String name){
    this.name = name;
  }

  public void setVersion(String version){
    this.version = version;
  }

  public void setAuthor(String author){
    this.author = author;
  }

  public void setEnabled(boolean enab){
    this.Enabled = enab;
  }

  public String getName(){
    return this.name;
  }

  public String getVersion(){
    return this.version;
  }

  public String getAuthor(){
    return this.author;
  }

  public boolean isEnabled(){
    return this.Enabled;
  }
}
