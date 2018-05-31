package cn.fhj.twitter;

import cn.fhj.TwitterFrm;
import cn.fhj.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import twitter4j.MediaEntity;
import twitter4j.Status;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Twitter {
  private static String LOC_HOME = "c:\\twitter\\";

  public static SinaWeibo weibo;

  public static String getDataFold() {
    return Twitter.LOC_HOME + name + "/data/";
  }

  private static final String MAX_ID = "MAX_ID";

  private static String getConfigFile() {
    return LOC_HOME + name + "/config.inc";
  }

  public static final String HOME = "https://twitter.com/";

  protected static String name = "fangshimin";

  protected static long maxId = 0;

  private static TwitterClient twitterClient;

  public static void initMaxId() {
    String idStr = readConfig(MAX_ID);
    if (idStr != null) {
      maxId = Long.parseLong(idStr);
      return;
    }
    List<Status> statuses = twitterClient.getRecent(8);
    maxId = statuses.get(statuses.size() - 1).getId();
    saveConfig(MAX_ID, maxId);
  }

  public static String readConfig(String key) {
    Map<String, String> map = readConfig();
    return map.get(key);
  }

  public static Map<String, String> readConfig() {
    Map<String, String> map = new HashMap();
    File file = new File(getConfigFile());
    if (file.exists())
      for (String ss : IoUtil.readText(file).trim().split("\\s*\n\\s*")) {
        if (StringUtil.isEmpty(ss)) {
          continue;
        }
        int index = ss.indexOf('=');
        map.put(ss.substring(0, index).trim(), ss.substring(index + 1));
      }
    return map;
  }

  public static void saveConfig(String key, Object value) {
    Map<String, String> map = readConfig();
    map.put(key, String.valueOf(value));
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, String> me : map.entrySet()) {
      if (sb.length() > 0) {
        sb.append('\n');
      }
      sb.append(me.getKey()).append('=').append(me.getValue());
    }
    File file = new File(getConfigFile());
    if (!file.exists()) {
      try {

        file.createNewFile();
      } catch (IOException e) {
        // Ignore
        e.printStackTrace();
      }
    }
    IoUtil.write(sb.toString(), file);
  }

  private final static Log log = LogFactory.getLog(Twitter.class);

  public static void start(String name, String dir, SinaWeibo weibo) {
    Twitter.name = name;
    Twitter.LOC_HOME = dir;
    Twitter.weibo = weibo;
    Twitter.twitterClient = new TwitterClient(name);
    new File(getDataFold()).mkdirs();
    initMaxId();
    new Thread() {
      @Override
      public void run() {
        for (; ; ) {
          try {
            checkDelete();
            if (!refresh()) {
              return;
            }
          } catch (Exception e) {
            log.error(e.getMessage(), e);
          }
          ThreadUtil.sleep(Twitter.weibo.getSleep());
        }
      }

    }.start();
  }

  private static boolean sending = false;

  public static boolean canExist() {
    return !sending;
  }

  protected static boolean refresh() {
    TwitterFrm.setMessage(DateUtil.format("HH:mm:ss") + " 抓取新微博...");
    List<Status> statuses = twitterClient.since(maxId);
    TwitterFrm.setMessage("抓取新微博条数：" + statuses.size());
    Collections.reverse(statuses);
    for (Status status : statuses) {
      Grid grid = toGrid(status);
      String pic = grid.creatPic();
      try {
        sending = true;
        log.info(grid);
        TwitterFrm.setMessage("微博发送中：" + grid.getText());
        String weiboId = weibo.send(grid);
        if (weiboId != null) {
          maxId = grid.getId();

          if (weibo.shouldSave()) {
            saveGrid(grid, pic);
            if ("-1".equals(weiboId)) {
              saveFailure(grid);
            }
          }
          saveConfig(MAX_ID, maxId);
          sending = false;
          TwitterFrm.setMessage("微博发成功：" + weiboId);
          weibo.sleepAfterSend();
        } else {
          TwitterFrm.setMessage("微博发送失败！");
          TwitterFrm.getInstance().setButtons(true);
          return false;
        }
      } finally {
        sending = false;
      }
    }
    return true;
  }

  private static void checkDelete() {
    long time = System.currentTimeMillis();
    long timeout = 1000 * 60 * 10;
    List<Grid> tobeRemoved = new ArrayList();
    for (Grid grid : weibo.getRecentGrids()) {
      if (checkDelete(grid)) {
        tobeRemoved.add(grid);
      } else if (time - grid.getDate().getTime() > timeout) {
        tobeRemoved.add(grid);
      }
    }
    weibo.getRecentGrids().removeAll(tobeRemoved);
  }

  public static boolean checkDelete(Grid grid) {
    if (!twitterClient.exists(grid.getId())) {
      weibo.delete(grid.getSinaId());
      return true;
    }
    return false;
  }

  public static void saveGrid(Grid grid, String pic) {
    if (pic != null) {
      String newPic = getDataFold() + grid.getId() + pic.substring(pic.lastIndexOf('.'));
      new File(pic).renameTo(new File(newPic));
    }
    IoUtil.write(grid.getText(), new File(getDataFold() + grid.getId() + ".txt"));
  }

  public static void saveFailure(Grid grid) {
    String s = grid.getText().length() > 10 ?
        grid.getText().substring(0, 10) + "..." : grid.getText();
    IoUtil.write("新浪微博不让机器人转发：" + s,
        new File(getDataFold() + (grid.getId() + 1) + ".txt"));
  }

  protected static Grid toGrid(Status status) {

    Grid grid = new Grid();
    grid.setId(status.getId());

    grapConversations(grid, twitterClient.digReply(status));

    grid.setOwner(getUserName(status));
    grid.setText(twitterClient.grapText(status));
    grapPic(grid, status);

    grid.simpleConverSations();
    return grid;
  }

  private static String getUserName(Status status) {
    if (status.getUser().getName().matches("\\w+")) {
      return status.getUser().getScreenName();
    }
    return status.getUser().getName();
  }

  private static void grapConversations(Grid grid, List<Status> reply) {
    for (Status st : reply) {
      grapPic(grid, st);
      String text = twitterClient.grapText(st);
      grid.addConversation(getUserName(st), text);
    }
  }


  public static void grapPic(Grid grid, Status st) {
    for (MediaEntity media : st.getMediaEntities()) {
      grid.addPic(media.getMediaURL());
    }
  }

}
