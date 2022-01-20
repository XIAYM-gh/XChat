package cn.xiaym.xchat.plugin.dataTypes;

public class CommandSender {
  private boolean is_console;
  private User u=null;

  public CommandSender(boolean isConsole){
    this.is_console=isConsole;
  }

  public CommandSender(User u){
    this.u=u;
  }

  public boolean isConsole(){
    return this.is_console;
  }

  public User getUser(){
    return this.u;
  }
}
