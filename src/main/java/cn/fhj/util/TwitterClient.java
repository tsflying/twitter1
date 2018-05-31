package cn.fhj.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.util.ArrayList;
import java.util.List;

public class TwitterClient {
  private final static Log log = LogFactory.getLog(TwitterClient.class);
  public static final String CONSUMER_KEY = "yyTA33jyRcwaF0taLYdqYCHKx";
  public static final String CONSUMER_SECRET = "ROmwgEVe9bUzHReOGCYRigQGqVF9gRXrzcEGCEK4MYS8FHYzuY";
  public static final String TOKEN = "2869790670-Zdj17nxZAj1z2Q8DEH98yhIcMnWbYGOXqJkpIVO";
  public static final String TOKEN_SECRET = "QmsuC9ILTwt35SeenGfx4iX1EaMU5XK78mcfdXZZdxSBY";
  private final Twitter twt;
  private final String name;

  public TwitterClient(String name) {
    this(name, null, 0);
  }

  public TwitterClient(String name, String proxyHost, int port) {
    this.name = name;
    ConfigurationBuilder cb = new ConfigurationBuilder();
    cb.setDebugEnabled(true)
        .setOAuthConsumerKey(TwitterClient.CONSUMER_KEY)
        .setOAuthConsumerSecret(TwitterClient.CONSUMER_SECRET)
        .setOAuthAccessToken(TwitterClient.TOKEN)
        .setTweetModeExtended(true)
        .setOAuthAccessTokenSecret(TwitterClient.TOKEN_SECRET);
    cb.setJSONStoreEnabled(true);
    cb.setIncludeEntitiesEnabled(true);
    if (proxyHost != null) {
      cb.setHttpProxyHost(proxyHost).setHttpProxyPort(port);
    }
    twt = new TwitterFactory(cb.build()).getInstance();
  }

  public Query getQuery() {
    Query query = new Query("from:" + name);
    return query;
  }

  private List<Status> getStatuses(Query query) {
    QueryResult result = null;
    try {
      result = twt.search(query);
    } catch (TwitterException e) {
      throw new MsgException(e);
    }
    return result.getTweets();
  }

  public List<Status> getRecent(int limit) {
    Query query = getQuery();
    query.setCount(limit);
    return getStatuses(query);
  }

  public boolean exists(long id) {
    return getStatus(id) != null;
  }


  Status getStatus(long id) {
    try {
      return twt.showStatus(id);
    } catch (TwitterException e) {
      log.warn(e, e);
      return null;
    }
  }

  ResponseList<Status> getRetweets(long id) {
    try {
      return twt.getRetweets(id);
    } catch (TwitterException e) {
      throw new MsgException(e);
    }
  }

  public List<Status> since(long id) {
    Query query = getQuery();
    query.setSinceId(id);
    return getStatuses(query);
  }

  public List<Status> digReply(Status status) {
    Status quote = status.getQuotedStatus();
    List<Status> statuses = new ArrayList<>();
    if (quote != null) {
      statuses.add(quote);
      digParent(quote, statuses);
    } else if (status.getInReplyToStatusId() > 0) {
      statuses.addAll(digStatus(status.getInReplyToStatusId()));
    }
    return statuses;

  }

  private void digParent(Status quote, List<Status> statuses) {
    if (quote.getQuotedStatusId() > 0) {
      statuses.addAll(digStatus(quote.getQuotedStatusId()));
    } else if (quote.getInReplyToStatusId() > 0) {
      statuses.addAll(digStatus(quote.getInReplyToStatusId()));
    }
  }

  private List<Status> digStatus(long id) {
    Status status = getStatus(id);
    List<Status> statuses = new ArrayList<>();
    if (status != null) {
      statuses.add(status);
      Status quote = status.getQuotedStatus();
      if (quote != null) {
        statuses.add(quote);
        digParent(quote, statuses);
      } else if (status.getInReplyToStatusId() > 0) {
        statuses.addAll(digStatus(status.getInReplyToStatusId()));
      }
    }
    return statuses;
  }

  public String grapText(Status st) {
    String text = st.getText();
    for (URLEntity url : st.getURLEntities()) {
      String expandUrl = url.getExpandedURL();
      String s = expandUrl.startsWith("https://twitter.com/") ? "" : expandUrl;
      text = text.replace(url.getURL(), s);
    }
    int index = text.lastIndexOf("https://t.co/");
    if (index >= 0 && index + 30 > text.length()) {
      text = text.substring(0, index);
    }
    return trimMentions(text);
  }

  String trimMentions(String text) {
    return text.trim().replaceAll("^@\\w+\\s+(@\\w+\\s+)*", "").replaceAll("(@\\w+\\s+)*@\\w+\\s*$", "").trim();
  }
}
