package cn.xiaym.xchat.events;

import cn.xiaym.xchat.plugin.dataTypes.*;

public class UserChatEvent extends CancelableEvent {
  private String msg = "";
  private User u;

  public UserChatEvent(String msg, User u) {
    this.msg = msg;
    this.u = u;
  }

  public User getUser() {
    return u;
  }

  public String getMessage() {
    return msg;
  }
}
