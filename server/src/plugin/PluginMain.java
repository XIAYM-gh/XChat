package cn.xiaym.xchat.plugin;

import java.io.*;
import java.util.*;
import java.net.*;

import cn.xiaym.xchat.*;
import cn.xiaym.xchat.utils.*;

public class PluginMain {
  private static ArrayList<JavaPlugin> Plugins = new ArrayList<>();
  private static ArrayList<String> hiddenCommands = new ArrayList<>();

  public static void init(){
    File plugindir = new File("plugins/");
    if(!plugindir.exists()){
      plugindir.mkdir();
    }

    File[] pluginsFile = plugindir.listFiles();

    List<File> pluginJars = Collections.synchronizedList(new ArrayList<File>());

    for (File f : pluginsFile) {
      if(f.getName().endsWith(".jar")){
        pluginJars.add(f);
      }
    }

    int fl_size = pluginJars.size();

    for (int i=1;i<=fl_size;i++) {
      Logger.info("("+i+"/"+fl_size+") " + pluginJars.get(i-1).getName());
      try{
        URLClassLoader u = new URLClassLoader(new URL[]{ pluginJars.get(i-1).toURI().toURL() });
        InputStream is = u.getResourceAsStream("config.properties");
        if(is != null){
          JavaPlugin plugin = initPlugin(is,u,pluginJars.get(i-1).getName());
          if(plugin != null){
            Plugins.add(plugin);
            try{
              plugin.onEnable();
            } catch(Exception e_PluginOnEnable) {
              Logger.err("无法执行插件的onEnable方法.");
              e_PluginOnEnable.printStackTrace();
            }
          }
        } else {
          Logger.err("无法在 "+pluginJars.get(i-1).getName()+" 中找到 /config.properties 文件!");
        }
      } catch(Exception e) {
        Logger.err("插件加载时出现错误!");
        e.printStackTrace();
      }
    }
  }

  public static JavaPlugin initPlugin(InputStream is, URLClassLoader u, String fileName) {
    try{
      Properties pc = new Properties();
      pc.load(is);
      Class<?> clazz = u.loadClass(pc.getProperty("main-class"));
      JavaPlugin p = (JavaPlugin) clazz.getDeclaredConstructor().newInstance();
      p.setName(pc.getProperty("plugin-name", fileName));
      p.setVersion(pc.getProperty("plugin-version", "1.0.0"));
      p.setAuthor(pc.getProperty("plugin-author", "Unknown"));
      return p;
    } catch(ClassNotFoundException|NoSuchMethodException e) {
      Logger.err("无法找到插件主类，请检查插件的配置文件!");
      return null;
    } catch(Exception e) {
      Logger.err("构造插件失败: "+e.toString());
      return null;
    }
  }

  public static ArrayList<JavaPlugin> getPlugins(){
    return Plugins;
  }

  //隐藏命令提示
  public static void addHiddenCommand(String cmd){
    hiddenCommands.add(cmd);
  }

  public static void removeHiddenCommand(String cmd){
    hiddenCommands.remove(cmd);
  }

  public static ArrayList<String> getHiddenCommands(){
    return hiddenCommands;
  }

  public static boolean isHiddenCommand(String cmd){
    for(String h:hiddenCommands){
      if(h.startsWith(cmd)){
        return true;
      }
    }

    return false;
  }
}
