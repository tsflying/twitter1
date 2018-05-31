package cn.fhj.twitter;

import cn.fhj.TwitterFrm;
import cn.fhj.util.*;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SinaWeibo {

  private Logger log = LogUtil.getLog(this.getClass());
  private static final String URL_PATTERN = "(http://|https://)[^\u4e00-\u9fa5\\s]*?\\.(com|net|cn|me|tw|fr)[^\u4e00-\u9fa5\\s]*";
  private final List<Grid> recentGrids = new ArrayList();
  protected HttpsUtil util;
  private boolean pause;
  private boolean ready = false;

  private String st = null;

  private boolean hidden = false;

  private long time = 0;

  public SinaWeibo(String cks, boolean pause) {
    this.setPause(pause);
    if (isPause()) {
      return;
    }
    util = HttpsUtil.getSinaInstance(cks);
    String msg;
    try {
      getSt();
      msg = "Succes logined";
    } catch (Exception e) {
      LogUtil.getLog(this.getClass()).error(e, e);
      msg = "Failed to login";
    }

    showMessage(msg + " " + this.getClass().getSimpleName());
  }

  protected void getSt() {
    this.setReady(false);
    String html = util.doGetForString("https://weibo.cn/");
    Elements form = HtmUtil.getBody(html).getElementsByTag("form");
    if (form.size() < 1) {
      throw new MsgException("No found st,maybe not logined");
    }
    String action = form.get(0).attr("action");
    int indexOf = action.indexOf("st=");
    if (indexOf < 0) {
      throw new MsgException("No found st,maybe not logined");
    }
    st = action.substring(indexOf + 3);
    this.setReady(true);
  }


  public void setHidden(boolean hidden) {
    this.hidden = hidden;
  }

  public String send(Grid grid) {
    if (isPause()) {
      return "-2";
    }
    long curTime = System.currentTimeMillis();

    if (curTime - time > 1000 * 3600 * 6) {
      try {
        getSt();
      } catch (Exception e) {
        LogUtil.getLog(this.getClass()).error(e, e);
        return null;
      }
    }
    time = curTime;
    Map<String, String> formData = new HashMap();
    formData.put("content", trim(grid.getText()));
    formData.put("st", st);
    formData.put("visible", hidden ? "1" : "0");
    String picId = uploadPics(grid.getPicFiles());
    if (picId != null) {
      formData.put("picId", picId);
    }
    for (int count = 0; count < 3; count++) {
      String result = util.post("https://m.weibo.cn/mblogDeal/addAMblog", formData, null, null);
      if (result != null) {
        String sinaId = getSinaId(result);

        if (sinaId != null) {
          if (!"-1".equals(sinaId)) {
            grid.setSinaId(sinaId);
            this.getRecentGrids().add(grid);
          }
          return sinaId;
        }
      }
      try {
        getSt();
      } catch (Exception e) {
        LogUtil.getLog(this.getClass()).error(e, e);
        return null;
      }
    }
    return null;
  }

  private String getSinaId(String result) {
    try {
      JSONObject object = new JSONObject(result);
      return object.getString("id");
    } catch (Exception e) {
      log.error("parse sinaId error:" + result, e);
      if (result.contains("20021")) {
        return "-1";
      }
      return null;
    }
  }

  private String uploadPics(List<String> pics) {
    StringBuilder sb = new StringBuilder();
    for (String pic : pics) {
      String id = uploadPic(pic);
      if (id != null) {
        sb.append(id).append(',');
      }
    }
    if (sb.length() > 0) {
      return sb.substring(0, sb.length() - 1);
    }
    return null;
  }

  protected String uploadPic(String pic) {
    Map<String, String> formData = new HashMap();
    formData.put("type", "json");
    String s = util.post("https://m.weibo.cn/mblogDeal/addPic", formData, "pic", pic);
    if (s == null) {
      log.info(s);
      return null;
    }
    try {
      JSONObject object = new JSONObject(s);
      return object.getString("pic_id");
    } catch (Exception e) {
      log.warn(s);
      return null;
    }
  }

  public void delete(String sinaId) {
    if (isPause()) {
      return;
    }
    Map<String, String> formData = new HashMap();
    formData.put("id", sinaId);
    util.post("https://m.weibo.cn/mblogDeal/delMyMblog", formData, null, null);
  }

  public List<Grid> getRecentGrids() {
    return recentGrids;
  }

  protected int getWeiboLength() {
    return 3000;
  }

  public boolean isLarge(String text) {
    int count = 0;
    for (char c : text.toCharArray()) {
      if (c < 256) {
        count++;
      }
    }
    int length = text.length() - count / 2;
    if (length <= getWeiboLength()) {
      return false;
    }

    Pattern pattern = Pattern.compile(URL_PATTERN);
    Matcher matcher = pattern.matcher(text);
    while (matcher.find()) {
      String s = matcher.group(0);
      length -= s.length() / 2 - 10;
    }
    return length > getWeiboLength();
  }

  public String trim(String text) {
    int count = 0;
    for (char c : text.toCharArray()) {
      if (c < 256) {
        count++;
      }
    }
    int dif = text.length() - getWeiboLength() - count / 2;
    if (dif <= 0) {
      return text;
    }

    Pattern pattern = Pattern.compile(URL_PATTERN);
    // 空格结束
    Matcher matcher = pattern.matcher(text);
    while (matcher.find()) {
      String s = matcher.group(0);
      dif -= s.length() / 2 - 10;
      if (dif <= 0) {
        return text;
      }
    }
    return trim(text.substring(0, text.length() - dif));
  }

  public void setPause(boolean pause) {
    this.pause = pause;
  }

  public boolean isPause() {
    return pause;
  }

  public boolean isReady() {
    return pause || ready;
  }

  public void setReady(boolean ready) {
    this.ready = ready;
  }

  protected void showMessage(String msg) {
    TwitterFrm.setMessage(msg);
  }

  public boolean shouldSave() {
    return true;
  }

  public long getSleep() {
    return 37000;
  }

  public void sleepAfterSend() {
    ThreadUtil.sleep((long) (1000 * 60 * (2 + Math.random() * 5)));
  }
}
