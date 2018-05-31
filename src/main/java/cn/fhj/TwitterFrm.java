package cn.fhj;

import cn.fhj.twitter.SinaWeibo;
import cn.fhj.twitter.Twitter;
import cn.fhj.util.DateUtil;
import cn.fhj.util.IoUtil;
import cn.fhj.util.StringUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

public class TwitterFrm extends JFrame {
  private final static Log log = LogFactory.getLog(TwitterFrm.class);

  private static final long serialVersionUID = 1983923261476635370L;

  private JTextField name = new JTextField("fangshimin");

  private JButton begin = new JButton("转weibo.com");

  private JButton beginQq = new JButton("转t.qq.com");

  private JTextField cksTxt = new JTextField();

  private JFileChooser jfc = new JFileChooser();

  private JTextArea statusText = new JTextArea();

  private JScrollPane scroll = new JScrollPane(statusText);

  private JTextField saveDir = new JTextField(getCurrentDir());// ("c:/twitter");

  private JButton dirBt = new JButton(":");

  private static String getCurrentDir() {
    return IoUtil.getProjectPath();

  }

  public TwitterFrm() {
    setResizable(false);
    setLayout(null);
    this.setTitle("转发Twitter18-5-27");

    int frmWidth = 680, frmHeight = 380;
    int xSpace = 20, ySpace = 10, margin = 30;
    int x = margin, y = 10, lblWidth = 100, height = 30, btHeight = height;
    int btWidth = 60;
    int proxyCheckBoxWidth = 80;

    JLabel dirLbl = new JLabel("本地目录");
    add(dirLbl);
    dirLbl.setBounds(x, y, lblWidth, height);
    x += lblWidth + xSpace;
    add(saveDir);
    saveDir.setBounds(x, y, frmWidth - x - 3 * xSpace - margin - btWidth - proxyCheckBoxWidth,
        height);
    saveDir.setEditable(false);
    add(dirBt);
    dirBt.setBounds(x + saveDir.getWidth(), y, btHeight, btHeight);

    // add(beginQq);
    // beginQq.setBounds(frmWidth - margin - 2 * btWidth, y, 2 * btWidth,
    // btHeight);

    x = margin;
    y += height + ySpace;
    JLabel bokLbl = new JLabel("转发推特账户");
    add(bokLbl);
    bokLbl.setBounds(x, y, lblWidth, height);

    x += bokLbl.getWidth() + xSpace;
    this.add(name);
    name.setBounds(x, y, frmWidth - x - 3 * xSpace - margin - btWidth - proxyCheckBoxWidth, height);

    add(begin);
    begin.setBounds(frmWidth - margin - 2 * btWidth, y, 2 * btWidth, btHeight);

    x = margin;
    y += height + ySpace;
    JLabel sinaLbl = new JLabel("Sina Cookies");
    add(sinaLbl);
    sinaLbl.setBounds(x, y, lblWidth, height);
    x += sinaLbl.getWidth() + xSpace;
    cksTxt.setBounds(x, y, frmWidth - x - margin, height);
    this.add(cksTxt);


    // progress.setStringPainted(true); // 设置进度条呈现进度字符串,默认为false
    statusText.setEditable(false);
    statusText.setBackground(new Color(255, 255, 200));

    y += height + ySpace;
    scroll.setBounds(xSpace, y, frmWidth - 2 * xSpace, frmHeight - y - 4 * ySpace);
    statusText.setLineWrap(true); // 激活自动换行功能
    statusText.setWrapStyleWord(true);
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    add(scroll);
    setSize(frmWidth, frmHeight);
    setVisible(true);
    double lx = Toolkit.getDefaultToolkit().getScreenSize().getWidth();

    double ly = Toolkit.getDefaultToolkit().getScreenSize().getHeight();

    setLocation(new Point((int) (lx / 2) - this.getWidth() / 2, (int) (ly / 2) - this.getHeight()
        / 2));// 设定窗口出现位置
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(new java.awt.event.WindowAdapter() {
      @Override
      public void windowClosing(java.awt.event.WindowEvent evt) {
        if (Twitter.canExist()) {
          System.exit(0);
        } else {
          setMessage("Wait for sending...");
        }
      }
    });
    dirBt.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent arg0) {
        String dir = getSaveDir();
        if (dir == null) {
          name.grabFocus();
          return;
        }
        saveDir.setText(dir);

      }

      private String getSaveDir() {
        jfc.setFileSelectionMode(1);// 设定只能选择到文件夹
        int state = jfc.showOpenDialog(TwitterFrm.this);// 此句是打开文件选择器界面的触发语句
        if (state == 1) {
          return null;
        } else {
          File f = jfc.getSelectedFile();// f为选择到的目录
          return f.getAbsolutePath();
        }
      }
    });
    ActionListener beginListener = new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        if (StringUtil.isEmpty(name.getText())) {
          name.grabFocus();
          return;
        }
        if (StringUtil.isEmpty(cksTxt.getText())) {
          cksTxt.grabFocus();
          return;
        }
        setButtons(false);
        new Thread() {
          public void run() {
            try {
              SinaWeibo weibo;
              String cks = cksTxt.getText();
//              if (e.getSource().equals(beginQq)) {
//                weibo = new QqWeibo(cks);
//              } else {
              weibo = new SinaWeibo(cks, false);
//              }
              TwitterFrm.this.setTitle("Sina" + (++times) + ":" + new Date());
              if (weibo.isReady()) {
                Twitter.start(name.getText().trim(), getSaveDir(), weibo);
              } else {
                setButtons(true);
              }
            } catch (Exception e) {
              log.error(e.getMessage(), e);
            }
          }

          private String getSaveDir() {
            String saveText = saveDir.getText().trim();
            if (saveText.endsWith("/") || saveText.endsWith("\\")) {
              return saveText;
            }
            return saveText + "/";
          }
        }.start();
      }

    };
    begin.addActionListener(beginListener);
    beginQq.addActionListener(beginListener);

    message("点击开始按钮即可开始转推");
    message("敬请关注");
    message("脑中有科学，心中有道义");
    message("程序制作人：@后军");
  }

  private static int times = 0;
  protected static TwitterFrm frm;

  public static void main(String[] args) {
    if (!new File(Twitter.getDataFold()).exists()) {
      new File(Twitter.getDataFold()).mkdirs();
    }
    frm = new TwitterFrm();
  }

  private Queue<String> que = new LinkedList();

  public static void setMessage(String msg) {
    if (frm != null) {
      frm.message(msg);
    } else {
      System.out.println(msg);
    }
  }

  public void message(String msg) {
    log.info(msg);
    if (que.size() > 20) {
      que.poll();
    }
    que.add(msg);
    StringBuilder sb = new StringBuilder();
    for (String s : que) {
      sb.append(DateUtil.format("MM-dd HH:mm")).append(s).append("\n");
    }
    statusText.setText(sb.toString());
    JScrollBar bar = scroll.getVerticalScrollBar();
    bar.setValue(bar.getMaximum());
  }

  public void setButtons(boolean enable) {
    begin.setEnabled(enable);
    beginQq.setEnabled(enable);
    name.setEditable(enable);
    autoRestart();
  }

  public static String downdPic(String fileUrl) {
    String savePath = Twitter.getDataFold() + "login.jpg";
    try {
      URL url = new URL(fileUrl);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      DataInputStream in = new DataInputStream(connection.getInputStream());
      DataOutputStream out = new DataOutputStream(new FileOutputStream(savePath));
      byte[] buffer = new byte[4096];
      int count = 0;
      while ((count = in.read(buffer)) > 0) {
        out.write(buffer, 0, count);
      }
      out.close();
      in.close();
      connection.disconnect();

    } catch (Exception e) {
      System.out.println(e + fileUrl + savePath);
    }
    return savePath;
  }

  public static TwitterFrm getInstance() {
    return frm;
  }

  public void autoRestart() {
    if (!this.begin.isEnabled()) {
      return;
    }
    try {
      Thread.sleep(6000 * 5);
    } catch (InterruptedException e) {
      //ingore
    }
    this.begin.doClick();
  }
}
