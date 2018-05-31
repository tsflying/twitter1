package cn.fhj.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
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
import org.apache.http.impl.cookie.BestMatchSpec;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;

import cn.fhj.twitter.Twitter;

public class HttpUtil {

	private static int bufferSize = 1024;

	private final static Log log = LogFactory.getLog(Twitter.class);

	private ConnectionConfig connConfig;

	private SocketConfig socketConfig;

	private ConnectionSocketFactory plainSF;

	private Registry<ConnectionSocketFactory> registry;

	private PoolingHttpClientConnectionManager connManager;

	private final HttpClient client;

	private volatile BasicCookieStore cookieStore;

	public String defaultEncoding = "utf-8";

	private BasicHttpContext localContext = new BasicHttpContext();

	private List<NameValuePair> paramsConverter(Map<String, String> params) {
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

	public HttpUtil() {
		// 设置连接参数
		connConfig = ConnectionConfig.custom().setCharset(Charset.forName(defaultEncoding)).build();
		socketConfig = SocketConfig.custom().setSoTimeout(100000).build();
		RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.<ConnectionSocketFactory> create();
		plainSF = new PlainConnectionSocketFactory();
		registryBuilder.register("http", plainSF);
		registry = registryBuilder.build();
		// 设置连接管理器
		connManager = new PoolingHttpClientConnectionManager(registry);
		connManager.setDefaultConnectionConfig(connConfig);
		connManager.setDefaultSocketConfig(socketConfig);
		// 指定cookie存储对象
		// 构建客户端
		HttpClientBuilder builder = HttpClientBuilder.create().setConnectionManager(connManager);
    builder.setProxy(new HttpHost("127.0.0.1", 1080));
		client = builder.build();
	}

	
	
	public InputStream doGet(String url) {
		try {
			HttpResponse response = this.doGet(url, null);
			return response != null ? response.getEntity().getContent() : null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Header[] getResponseHeader(String url) {
		try {
			HttpResponse response = this.doGet(url, null);
			String s = readStream(response.getEntity().getContent(), null);
			System.out.println(s);
			
			return response.getAllHeaders();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String doGetForString(String url) {
		return readStream(this.doGet(url), null);
	}

	public InputStream doGetForStream(String url, Map<String, String> queryParams) throws URISyntaxException,
			ClientProtocolException, IOException {
		HttpResponse response = this.doGet(url, queryParams);
		return response != null ? response.getEntity().getContent() : null;
	}

	public InputStream doGetForStream(String url) {
		try {
			HttpResponse response = this.doGet(url, Collections.EMPTY_MAP);
			return response != null ? response.getEntity().getContent() : null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String doGetForString(String url, Map<String, String> queryParams) throws URISyntaxException,
			ClientProtocolException, IOException {
		return readStream(this.doGetForStream(url, queryParams), null);
	}

	/**
	 * 基本的Get请求
	 * 
	 * @param url
	 *            请求url
	 * @param queryParams
	 *            请求头的查询参数
	 * @return
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public HttpResponse doGet(String url, Map<String, String> queryParams) throws URISyntaxException,
			ClientProtocolException, IOException {
		HttpGet gm = new HttpGet();
		URIBuilder builder = new URIBuilder(url);
		// 填入查询参数
		if (queryParams != null && !queryParams.isEmpty()) {
			builder.setParameters(paramsConverter(queryParams));
		}
		gm.setURI(builder.build());
		return execute(gm);
	}

	// private Header[] headers = null;

	public HttpResponse execute(HttpUriRequest gm) throws IOException, ClientProtocolException {
		HttpResponse response = client.execute(gm, localContext);
		return response;
	}

	public InputStream doPostForStream(String url, Map<String, String> queryParams) {
		try {
			HttpResponse response = this.doPost(url, null, queryParams);
			return response != null ? response.getEntity().getContent() : null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String doPostForString(String url, Map<String, String> queryParams) {
		return readStream(this.doPostForStream(url, queryParams), null);
	}

	public InputStream doPostForStream(String url, Map<String, String> queryParams, Map<String, String> formParams)
			throws URISyntaxException, ClientProtocolException, IOException {
		HttpResponse response = this.doPost(url, queryParams, formParams);
		return response != null ? response.getEntity().getContent() : null;
	}

	public String doPostRetString(String url, Map<String, String> queryParams, Map<String, String> formParams)
			throws URISyntaxException, IOException {
		return readStream(this.doPostForStream(url, queryParams, formParams), null);
	}

	public HttpResponse doPost(String url, Map<String, String> queryParams, Map<String, String> formParams)
			throws URISyntaxException, IOException {
		HttpPost pm = new HttpPost();
		URIBuilder builder = new URIBuilder(url);
		// 填入查询参数
		if (queryParams != null && !queryParams.isEmpty()) {
			builder.setParameters(paramsConverter(queryParams));
		}
		pm.setURI(builder.build());
		// 填入表单参数
		if (formParams != null && !formParams.isEmpty()) {
			pm.setEntity(new UrlEncodedFormEntity(paramsConverter(formParams), defaultEncoding));
		}
		return execute(pm);
	}

	/**
	 * 多块Post请求
	 * 
	 * @param url
	 *            请求url
	 * @param queryParams
	 *            请求头的查询参数
	 * @param formParts
	 *            post表单的参数,支持字符串-文件(FilePart)和字符串-字符串(StringPart)形式的参数
	 * @param maxCount
	 *            最多尝试请求的次数
	 * @return
	 * @throws URISyntaxException
	 * @throws ClientProtocolException
	 * @throws HttpException
	 * @throws IOException
	 */
	public HttpResponse multipartPost(String url, Map<String, String> queryParams, List<FormBodyPart> formParts)
			throws URISyntaxException, ClientProtocolException, IOException {
		HttpPost pm = new HttpPost();
		URIBuilder builder = new URIBuilder(url);
		// 填入查询参数
		if (queryParams != null && !queryParams.isEmpty()) {
			builder.setParameters(paramsConverter(queryParams));
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
	 * @param domain
	 *            作用域
	 * @param port
	 *            端口 传null 默认80
	 * @param path
	 *            Cookie路径 传null 默认"/"
	 * @param useSecure
	 *            Cookie是否采用安全机制 传null 默认false
	 * @return
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



	public String download(String imgUrl, int i) {
		String filename = Twitter.getDataFold() + i + ".jpg";
		IoUtil.write(doGetForStream(imgUrl), filename);
		return filename;
	}

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
			pm.addHeader("Origin", "http://m.weibo.cn");
			pm.addHeader("Referer", "http://m.weibo.cn/mblog");

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
	
	
	
}
