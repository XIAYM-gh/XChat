package cn.xiaym.xchat.events;

/**
 * 参考来自于 nukkitx @ Nukkit Project
 * @see cn.nukkit.event.Event
 */
public abstract class CancelableEvent {
  private boolean isCancelled = false;
  private String eventName = null;

  final public String getEventName(){
    return eventName == null ? getClass().getName() : eventName;
  }

  public boolean isCancelled(){
    return isCancelled;
  }

  public void setCancelled(){
    isCancelled = true;
  }

  public void setCancelled(boolean cancelled){
    isCancelled = cancelled;
  }
}
