package cn.fhj.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * 一些文件输入输出操作工具类
 */
public class IoUtil {
  public static final String UTF8 = "utf-8";
  private static final Log LOG = LogFactory.getLog(IoUtil.class);

  public static void zipFiles(List files, String jarFilename) {
    Map filesMap = new HashMap(1);
    filesMap.put(null, files);
    zipFiles(filesMap, jarFilename);
  }


  /**
   * 添加目录下的文件到out中,没有递归
   */
  public static void addDirToZip(ZipOutputStream out, String dir, String parent) throws
      IOException {
    final File[] files = new File(dir).listFiles();
    for (int i = 0; i < files.length; i++) {
      if (files[i].isFile()) {
        IoUtil.addToZip(out, files[i], parent);
      }
    }
  }

  public static void addToZip(ZipOutputStream out, File file, String dir) throws IOException {
    out.putNextEntry(new ZipEntry(dir + file.getName()));
    FileInputStream in = new FileInputStream(file);
    int len;
    byte[] buf = new byte[1024];
    while ((len = in.read(buf)) > 0) {
      out.write(buf, 0, len);
    }
    out.closeEntry();
    in.close();
  }

  public static void zipFiles(Map filesMap, String jarFilename) {
    ZipOutputStream out = null;
    try {
      out = new ZipOutputStream(new FileOutputStream(jarFilename));
      for (Iterator it = filesMap.entrySet().iterator(); it.hasNext(); ) {
        final Map.Entry me = (Map.Entry) it.next();
        List list = (List) (me);
        for (Iterator i = list.iterator(); i.hasNext(); ) {
          File file = (File) i.next();
          final String zipFilename;
          if (StringUtil.isEmpty(me.getKey())) {
            zipFilename = file.getName();
          } else {
            zipFilename = me.getKey() + "/" + file.getName();
          }
          out.putNextEntry(new ZipEntry(zipFilename));
          // out.write(read(new FileInputStream(file)));
          FileInputStream in = new FileInputStream(file);
          int len;
          byte[] buf = new byte[1024];
          while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
          }
          out.closeEntry();
          in.close();
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("打包文件出错，jarFileName:" + jarFilename, e);
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (Exception e) {
          LOG.info(e, e);// 忽略

        }
      }
    }
  }

  public static void delete(String filename) {
    try {
      new File(filename).delete();
    } catch (Exception e) {
      LOG.info(e, e);// 忽略
    }
  }

  public static void unZipTo(String jarFile, String dir) {
    unZipTo(new File(jarFile), dir);
  }

  public static void unZipTo(File jarFile, String dir) {
    try {
      ZipInputStream in = new ZipInputStream(new FileInputStream(jarFile));
      byte[] bytes = new byte[1024];
      for (ZipEntry ze = in.getNextEntry(); ze != null; ze = in.getNextEntry()) {
        FileOutputStream fis = new FileOutputStream(createNewFile(dir + ze.getName()));
        try {
          int len;
          while ((len = in.read(bytes)) > 0) {
            fis.write(bytes, 0, len);
          }
          fis.flush();
          in.closeEntry();
        } finally {
          try {
            fis.close();
          } catch (Exception e) {
            LOG.info(e, e);
          }
        }
      }
      in.close();
    } catch (Exception e) {
      throw new RuntimeException("解压出错", e);
    }
  }

  /**
   * 创建文件，如果目录不纯在自动创建相应的目录，并返回被创建的文件
   */
  public static File createNewFile(String filename) {
    filename = filename.replace('\\', '/').trim();
    new File(filename.substring(0, filename.lastIndexOf('/'))).mkdirs();
    return new File(filename);
  }

  public static void write(byte[] bytes, String filename) {
    FileOutputStream outStream = null;
    try {
      outStream = new FileOutputStream(filename);
      outStream.write(bytes);
    } catch (Throwable e) {
      throw new RuntimeException(e);
    } finally {
      if (outStream != null) {
        try {
          outStream.close();
        } catch (@SuppressWarnings("unused") Exception e) {
          // 忽略
        }
      }
    }
  }

  public static void write(String text, File file) {
    FileOutputStream outStream = null;
    try {
      outStream = new FileOutputStream(file);
      outStream.write(text.getBytes(UTF8));
    } catch (Throwable e) {
      throw new RuntimeException(e);
    } finally {
      if (outStream != null) {
        try {
          outStream.close();
        } catch (@SuppressWarnings("unused") Exception e) {
          // 忽略
        }
      }
    }
  }

  public static void deleteFiles(String dir) {
    final File[] files = new File(dir).listFiles();
    for (int i = 0; i < files.length; i++) {
      if (files[i].isFile()) {
        files[i].delete();
      }
    }
  }

  private static final int BUFFER_SIZE = 16 * 1024;

  /**
   * 文件对拷
   *
   * @param src 源文件
   * @param dst 目标文件
   */
  public static void copy(File src, File dst) {
    InputStream in = null;
    OutputStream out = null;
    try {
      in = new BufferedInputStream(new FileInputStream(src), BUFFER_SIZE);
      out = new BufferedOutputStream(new FileOutputStream(dst), BUFFER_SIZE);
      byte[] buffer = new byte[BUFFER_SIZE];
      int len = 0;
      while ((len = in.read(buffer)) > 0) {
        out.write(buffer, 0, len);
      }
    } catch (IOException e) {
      throw new RuntimeException("copy文件出错:" + e, e);
    } finally {
      if (null != in) {
        try {
          in.close();
        } catch (IOException e) {
          LOG.info(e, e);
        }
      }
      if (null != out) {
        try {
          out.close();
        } catch (IOException e) {
          LOG.info(e, e);
        }
      }
    }
  }

  /**
   * 获取文件名的扩展名
   */
  public static String getExt(String filename) {
    return filename.substring(filename.lastIndexOf('.') + 1);
  }

  /**
   * 去掉路径获取文件名
   */
  public static String shortName(String filename) {
    int a = filename.indexOf('/');
    int b = filename.indexOf('\\');
    int pos;
    if (a < 0) {
      pos = b;
    } else if (b < 0) {
      pos = a;
    } else {
      pos = a > b ? a : b;
    }
    return filename.substring(pos + 1);
  }

  public static byte[] read(File file) {
    long len = file.length();
    byte[] bytes = new byte[(int) len];
    try {
      BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
      int r = bufferedInputStream.read(bytes);
      bufferedInputStream.close();
      if (r != len) {
        throw new IOException("读取文件不正确");
      }
    } catch (Exception e) {
      throw new RuntimeException("读取文件出错:" + e, e);
    }
    return bytes;
  }

  public static String readText(File file) {
    try {
      return new String(read(file), UTF8);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("读取文件出错:" + e, e);
    }
  }

  public static void save(Set<String> list, String filename) {
    if (list.isEmpty()) {
      return;
    }
    StringBuilder sb = new StringBuilder();
    for (String s : list) {
      sb.append(s).append('\n');
    }
    sb.deleteCharAt(sb.length() - 1);
    try {
      IoUtil.write(sb.toString().getBytes(UTF8), filename);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("getBytes:" + e, e);
    }
  }

  public static String[] readList(String filename) {
    File file = new File(filename);
    if (!file.exists()) {
      return new String[]{};
    }
    return IoUtil.readText(file).split("\n");
  }

  public static void write(InputStream in, String filename) {
    try {
      OutputStream out = new FileOutputStream(filename);

      int byteCount = 0;

      byte[] bytes = new byte[1024];

      while ((byteCount = in.read(bytes)) != -1) {
        out.write(bytes, 0, byteCount);
      }
      out.close();
      in.close();
    } catch (Exception e) {
      throw new RuntimeException("读取文件出错:" + e, e);
    }
  }

  public static String getProjectPath() {
    java.net.URL url = IoUtil.class.getProtectionDomain().getCodeSource()
        .getLocation();
    String filePath = null;
    try {
      filePath = java.net.URLDecoder.decode(url.getPath(), "utf-8");
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (filePath.endsWith(".jar")) {
      filePath = filePath.substring(0, filePath.lastIndexOf("/") + 1);
    }
    java.io.File file = new java.io.File(filePath);
    filePath = file.getAbsolutePath();
    return filePath;
  }


  public static void close(Closeable c) {
    if (c == null) {
      return;
    }
    try {
      c.close();
    } catch (IOException e) {
      //Ignore
    }
  }
}
