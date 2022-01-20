package cn.xiaym.xchat.events;

import cn.xiaym.xchat.plugin.dataTypes.*;

public class UserLeaveEvent {
  private User u;

  public UserLeaveEvent(User u) {
    this.u = u;
  }

  public User getUser() {
    return u;
  }
}
