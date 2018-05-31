package cn.fhj.util;

import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.util.Formatter;
import java.util.Random;

public class StringUtil {
  public static String hex(String sourceStr) {
    StringBuilder sb = new StringBuilder();
    sb.append(sourceStr);

    try {
      return DigestUtils.md5Hex(sb.toString().getBytes("UTF-8"));
    } catch (UnsupportedEncodingException var3) {
      var3.printStackTrace();
      return "";
    }
  }

  /**
   * 产生随机字符串
   *
   * @param length 随机字符串的长度
   * @return
   */
  public static String randomKey(int length) {
    final String buffer = "0123456789abcdefghijklmnopqrstuvwxyz";
    StringBuilder sb = new StringBuilder(length);
    Random r = new Random();
    final int range = buffer.length();
    for (int i = 0; i < length; i++) {
      sb.append(buffer.charAt(r.nextInt(range)));
    }
    return sb.toString();
  }

  /**
   * 判断字符串是否为null或者空白字符或者空字符串
   */
  public static boolean isEmpty(Object str) {
    return str == null || str.toString().matches("\\s*");
  }

  /**
   * 判断两个对象是否相等
   */
  public static boolean isEqual(Object o1, Object o2) {
    return HashUtil.isEqual(o1, o2);
  }

  /**
   * 产生62位的随机字符串
   */
  public static String randomKey() {
    final String buffer = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    StringBuilder sb = new StringBuilder();
    Random r = new Random();
    int range = buffer.length();
    for (int i = 0; i < range; i++) {
      sb.append(buffer.charAt(r.nextInt(range)));
    }
    return sb.toString();
  }

  /**
   * 如果str为空，则返回空字符串，否则trim之后返回
   */
  public static String killNull(Object str) {
    return killNull(str, "");
  }

  /**
   * 如果str为空，则返回字符串defaultStr，否则trim之后返回
   *
   * @param str
   * @param defaultStr
   */
  public static String killNull(Object str, String defaultStr) {
    if (isEmpty(str)) {
      return defaultStr;
    }
    return str.toString().trim();
  }

  /**
   * 文件长度友好显示
   */
  public static String fomatFileSize(long size) {
    if (size < 1024) {
      return size + "B";
    } else if (size < 1024 * 1024) {
      return size / 1024 + "K";
    } else {
      return size / 1024 / 1024 + "M";
    }
  }

  /**
   * 判断字符串str可否转化成实型数据。
   */
  public static boolean isNumber(String str) {
    if (str == null) {
      return false;
    }
    return str.length() > 0 && str.matches("\\d*\\.{0,1}\\d*") && !".".equals(str);
  }

  /**
   * 判断字符串是否是纯数字组成
   *
   * @param str
   */
  public static boolean isInteger(String str) {
    if (str == null) {
      return false;
    }
    return str.matches("\\d+");
  }

  /**
   * 删除str最后一个字符如果是ch则删除这个字符，通常在根据list拼装字符串的时候使用
   *
   * @param str
   * @param ch
   */
  public static StringBuilder delLastChar(StringBuilder str, char ch) {
    if (str.length() < 1) {
      return str;
    }
    if (str.charAt(str.length() - 1) == ch) {
      str.deleteCharAt(str.length() - 1);
    }
    return str;
  }

  public static Class getVoClass(Object vo) {
    final Class clazz = vo.getClass();
    return clazz.getName().indexOf("$") > 0 ? clazz.getSuperclass() : clazz;
  }

  /**
   * 字符串的补位操作 如果需要长度为8位不足的添加某个字符
   *
   * @param length  长度需要补足
   * @param addChar 填充的字符串
   * @param origin  源字符串
   */
  public static String addChar2Full(int length, String addChar, String origin) {
    if (origin.length() < length) {
      String temp = "";
      for (int i = 0; i < length - origin.length(); i++) {
        temp = addChar + temp;
      }
      origin = temp + origin;
    }
    return origin;
  }

  /**
   * 第一个字母小写
   */
  public static String unCapFirst(String str) {
    final char c = str.charAt(0);
    if (c > 'Z' || c < 'A') {
      return str;
    }
    return (char) (c + 32) + str.substring(1);
  }

  /**
   * 第一个单词大写
   */
  public static String capFirst(String str) {
    final char c = str.charAt(0);
    if (c > 'z' || c < 'a') {
      return str;
    }
    return (char) (c - 32) + str.substring(1);
  }

  public static String getSimpleName(Class clazz) {
    String name = clazz.getName();
    return name.substring(name.lastIndexOf('.') + 1);

  }


  private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

  private static String toHexString(byte[] bytes) {
    Formatter formatter = new Formatter();

    for (byte b : bytes) {
      formatter.format("%02x", b);
    }

    return formatter.toString();
  }

  public static String hmac(String data, String key) {
    try {
      SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
      Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
      mac.init(signingKey);
      return toHexString(mac.doFinal(data.getBytes()));
    } catch (Exception e) {
      throw new MsgException(e);
    }
  }

  public static void main(String[] args) throws Exception {
    String hmac = hmac("data", "key");

    System.out.println(hmac);
    assert hmac.equals("104152c5bfdca07bc633eebd46199f0255c9f49d");
  }
}
