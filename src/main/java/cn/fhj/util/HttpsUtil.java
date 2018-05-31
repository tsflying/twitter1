package cn.fhj.util;

import cn.fhj.twitter.Twitter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie2;
import org.apache.http.impl.cookie.BestMatchSpec;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.Map.Entry;

class AnyTrustStrategy implements TrustStrategy {

  public boolean isTrusted(X509Certificate[] chain, String authType) {
    return true;
  }

}

public class HttpsUtil {

  private static int bufferSize = 1024;

  private static volatile HttpsUtil sinaInstance;

  private static volatile HttpsUtil qqInstance;

  private ConnectionConfig connConfig;

  private SocketConfig socketConfig;

  private ConnectionSocketFactory plainSF;

  private KeyStore trustStore;

  private SSLContext sslContext;

  private LayeredConnectionSocketFactory sslSF;

  private Registry<ConnectionSocketFactory> registry;

  private PoolingHttpClientConnectionManager connManager;

  public final HttpClient client;

  private volatile BasicCookieStore cookieStore;

  public String defaultEncoding = "utf-8";

  private static List<NameValuePair> paramsConverter(Map<String, String> params) {
    List<NameValuePair> nvps = new LinkedList();
    Set<Entry<String, String>> paramsSet = params.entrySet();
    for (Entry<String, String> paramEntry : paramsSet) {
      nvps.add(new BasicNameValuePair(paramEntry.getKey(), paramEntry.getValue()));
    }
    return nvps;
  }

  public String readStream(InputStream in, String encoding) {
    if (in == null) {
      return null;
    }
    try {
      InputStreamReader inReader = null;
      if (encoding == null) {
        inReader = new InputStreamReader(in, defaultEncoding);
      } else {
        inReader = new InputStreamReader(in, encoding);
      }
      char[] buffer = new char[bufferSize];
      int readLen = 0;
      StringBuffer sb = new StringBuffer();
      while ((readLen = inReader.read(buffer)) != -1) {
        sb.append(buffer, 0, readLen);
      }
      inReader.close();
      return sb.toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public HttpsUtil(String cks, String domain) {
    this(null, cks, domain);
  }

  public HttpsUtil(Proxy proxy, String cks, String domain) {
    this.addCommonHeaders();
    // 设置连接参数
    connConfig = ConnectionConfig.custom().setCharset(Charset.forName(defaultEncoding)).build();
    socketConfig = SocketConfig.custom().setSoTimeout(100000).build();
    RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.<ConnectionSocketFactory>create();
    plainSF = new PlainConnectionSocketFactory();
    registryBuilder.register("http", plainSF);
    // 指定信任密钥存储对象和连接套接字工厂
    try {
      trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
      sslContext = SSLContexts.custom().useTLS().loadTrustMaterial(trustStore, new AnyTrustStrategy()).build();
      sslSF = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
      registryBuilder.register("https", sslSF);
    } catch (KeyStoreException e) {
      throw new RuntimeException(e);
    } catch (KeyManagementException e) {
      throw new RuntimeException(e);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
    registry = registryBuilder.build();
    // 设置连接管理器
    connManager = new PoolingHttpClientConnectionManager(registry);
    connManager.setDefaultConnectionConfig(connConfig);
    connManager.setDefaultSocketConfig(socketConfig);
    // 指定cookie存储对象
    cookieStore = new BasicCookieStore();
    if (cks != null) {
      for (String ck : cks.split(";")) {
        String[] ss = ck.split("=");
        BasicClientCookie2 cookie = new BasicClientCookie2(ss[0].trim(), ss[1].trim());
        cookie.setExpiryDate(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 60));
        cookie.setDomain(domain);
        cookieStore.addCookie(cookie);
      }
    }
    // 构建客户端
    HttpClientBuilder builder = HttpClientBuilder.create().setDefaultCookieStore(cookieStore)
        .setConnectionManager(connManager);
    client = builder.build();
  }

  public static HttpsUtil getSinaInstance(String cks) {
    sinaInstance = new HttpsUtil(cks, ".weibo.cn");
    sinaInstance.weiboHeaders.put("Origin", "https://m.weibo.cn");
    sinaInstance.weiboHeaders.put("Referer", "https://m.weibo.cn/mblog");
    sinaInstance.weiboHeaders.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
    return sinaInstance;
  }

  private void addCommonHeaders() {
    weiboHeaders.put("User-Agent",
        "Mozilla/5.0 AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.125 Safari/537.36");
  }

  public static HttpsUtil getQqInstance(String cks) {
    synchronized (HttpsUtil.class) {
      if (HttpsUtil.qqInstance == null) {
        qqInstance = new HttpsUtil(cks, ".qq.com");
      }
      qqInstance.weiboHeaders.put("Origin", "http://api.t.qq.com");
      qqInstance.weiboHeaders.put("Referer", "http://api.t.qq.com/proxy.html");
      return qqInstance;
    }
  }

  public String download(String imgUrl, int i) {
    String filename = Twitter.getDataFold() + i + ".jpg";
    IoUtil.write(doGetForStream(imgUrl), filename);
    return filename;
  }

  public InputStream doGet(String url) {
    try {
      HttpResponse response = this.doGet(url, null,Collections.emptyMap());
      return response != null ? response.getEntity().getContent() : null;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public String doGetForString(String url) {
    return readStream(this.doGet(url), null);
  }

  public InputStream doGetForStream(String url, Map<String, String> queryParams, Map<String, String> headers) {
    try {
      HttpResponse response = this.doGet(url, queryParams, headers);
      return response != null ? response.getEntity().getContent() : null;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public InputStream doGetForStream(String url) {
    try {
      HttpResponse response = this.doGet(url, Collections.EMPTY_MAP,Collections.emptyMap());
      return response != null ? response.getEntity().getContent() : null;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static String download(String url, String name) {
    String filename = name + getSufix(url);
    IoUtil.write(new HttpsUtil(null, null).doGetForStream(url), filename);
    return filename;
  }

  public static String getSufix(String url) {
    int begin = url.lastIndexOf(".");
    int end = url.lastIndexOf(":");
    return begin > end ? url.substring(begin) : url.substring(begin, end);
  }

  public String doGetForString(String url, Map<String, String> queryParams, Map<String, String> headers) {
    return readStream(this.doGetForStream(url, queryParams, headers), null);
  }

  /**
   * 基本的Get请求
   *
   * @param url         请求url
   * @param queryParams 请求头的查询参数
   */
  public HttpResponse doGet(String url, Map<String, String> queryParams, Map<String, String> headers) throws URISyntaxException,
      ClientProtocolException, IOException {
    HttpGet gm = new HttpGet();
    URIBuilder builder = new URIBuilder(url);
    setUserAgent(gm);
    // 填入查询参数
    if (queryParams != null && !queryParams.isEmpty()) {
      builder.setParameters(HttpsUtil.paramsConverter(queryParams));
    }
    gm.setURI(builder.build());

    for (Map.Entry<String, String> me : twitterHeaders.entrySet()) {
      gm.addHeader(me.getKey(), me.getValue());
    }
    for (Map.Entry<String, String> me : headers.entrySet()) {
      gm.addHeader(me.getKey(), me.getValue());
    }
    return execute(gm);
  }

  private void setUserAgent(HttpGet gm) {
    gm.addHeader("User-Agent",
        "Mozilla/5.0 AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.125 Safari/537.36");
  }

  public HttpResponse execute(HttpUriRequest gm) throws IOException, ClientProtocolException {
    HttpResponse response = client.execute(gm, localContext);
    return response;
  }

  private BasicHttpContext localContext = new BasicHttpContext();

  public InputStream doPostForStream(String url, Map<String, String> params, Map<String, String> headers) {
    try {
      HttpResponse response = this.doPost(url, params, headers);
      return response != null ? response.getEntity().getContent() : null;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public String doPostForString(String url, Map<String, String> params, Map<String, String> headers) {
    return readStream(this.doPostForStream(url, params, headers), null);
  }

  public HttpResponse doPost(String url, Map<String, String> params, Map<String, String> headers) throws IOException, URISyntaxException {
    HttpPost pm = new HttpPost(url);
    URIBuilder builder = new URIBuilder(url);
    // 填入查询参数
    if (params != null && !params.isEmpty()) {
      builder.setParameters(HttpsUtil.paramsConverter(params));
    }
    pm.setURI(builder.build());
    for (Map.Entry<String, String> me : headers.entrySet()) {
      pm.addHeader(me.getKey(), me.getValue());
    }
    return execute(pm);
  }

  /**
   * 多块Post请求
   *
   * @param url         请求url
   * @param queryParams 请求头的查询参数
   * @param formParts   post表单的参数,支持字符串-文件(FilePart)和字符串-字符串(StringPart)形式的参数 最多尝试请求的次数
   */
  public HttpResponse multipartPost(String url, Map<String, String> queryParams, List<FormBodyPart> formParts)
      throws URISyntaxException, ClientProtocolException, IOException {
    HttpPost pm = new HttpPost();
    URIBuilder builder = new URIBuilder(url);
    // 填入查询参数
    if (queryParams != null && !queryParams.isEmpty()) {
      builder.setParameters(HttpsUtil.paramsConverter(queryParams));
    }
    pm.setURI(builder.build());
    // 填入表单参数
    if (formParts != null && !formParts.isEmpty()) {
      MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
      entityBuilder = entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
      for (FormBodyPart formPart : formParts) {
        entityBuilder = entityBuilder.addPart(formPart.getName(), formPart.getBody());
      }
      pm.setEntity(entityBuilder.build());
    }
    return execute(pm);
  }

  /**
   * 获取当前Http客户端状态中的Cookie
   *
   * @param domain    作用域
   * @param port      端口 传null 默认80
   * @param path      Cookie路径 传null 默认"/"
   * @param useSecure Cookie是否采用安全机制 传null 默认false
   */
  public Map<String, Cookie> getCookie(String domain, Integer port, String path, Boolean useSecure) {
    if (domain == null) {
      return null;
    }
    if (port == null) {
      port = 80;
    }
    if (path == null) {
      path = "/";
    }
    if (useSecure == null) {
      useSecure = false;
    }
    List<Cookie> cookies = cookieStore.getCookies();
    if (cookies == null || cookies.isEmpty()) {
      return null;
    }

    CookieOrigin origin = new CookieOrigin(domain, port, path, useSecure);
    BestMatchSpec cookieSpec = new BestMatchSpec();
    Map<String, Cookie> retVal = new HashMap();
    for (Cookie cookie : cookies) {
      if (cookieSpec.match(cookie, origin)) {
        retVal.put(cookie.getName(), cookie);
      }
    }
    return retVal;
  }


  private final static Log log = LogFactory.getLog(HttpsUtil.class);

  public String post(String url, Map<String, String> formData, String picName, String pic) {

    HttpPost pm = new HttpPost(url);
    try {
      // 填入表单参数
      MultipartEntityBuilder builder = MultipartEntityBuilder.create();
      builder = builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
      ContentType contentType = ContentType.create("application/x-www-form-urlencoded", defaultEncoding);
      for (Map.Entry<String, String> me : formData.entrySet()) {
        StringBody stringBody = new StringBody(me.getValue(), contentType);
        builder = builder.addPart(me.getKey(), stringBody);
      }
      if (pic != null) {
        builder.addBinaryBody(picName, new File(pic));
      }
      pm.setEntity(builder.build());
      pm.addHeader("Accept", "application/json, text/javascript, */*; q=0.01");
      pm.addHeader("X-Requested-With", "XMLHttpRequest");
      pm.addHeader("Accept-Encoding", "gzip, deflate");
      for (Map.Entry<String, String> me : weiboHeaders.entrySet()) {
        pm.addHeader(me.getKey(), me.getValue());
      }

      HttpResponse response = execute(pm);

      int statusCode = response.getStatusLine().getStatusCode();
      String result = readStream(response.getEntity().getContent(), null);
      if (statusCode > 399 || statusCode < 200) {
        log.error("Error Response: " + result);
        return null;
      }
      return result;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private final Map<String, String> weiboHeaders = new HashMap();
  private final Map<String, String> twitterHeaders = new HashMap();

}
