package cn.xiaym.xchat.events;

import cn.xiaym.xchat.plugin.dataTypes.*;

public class UserJoinEvent {
  private User u;

  public UserJoinEvent(User u) {
    this.u=u;
  }

  public User getUser() {
    return u;
  }
}
