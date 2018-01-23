package tecsun.cjw.systemupdate.utils;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import tecsun.cjw.systemupdate.been.SystemInfo;

public class PullUpdateXmlParser {
  /**
   * <system name="TecSun TA V1.6.11 Build20170816">
   * <address>http://cpzx.e-tecsun.com:8037/update/TA/TecSun TA V1.6.11 Build20170816/</address>
   * <description>.增加开关U口功能</description>
   * <password>tecsun</password>
   * <hwsupport>invalid</hwsupport>
   * <formemory>2G</formemory>
   * <buildfor>TecSun TA V1.6.9 Build20170411</buildfor>
   * <buildfor>TecSun TA V1.6.9 Build20170428</buildfor>
   * </system>
   * 用pull解析器解析xml文件，存入List<SystemInfo> 集合中
   *
   * @param in 读取的Person.xml文件对应的输入流
   * @return 将读取到的List<Person>集合返回
   * @throws Exception
   */
  public static List<SystemInfo> xmlParser(InputStream in) throws Exception {
    List<SystemInfo> systems = null;
    SystemInfo system = null;

    XmlPullParser pullParser = Xml.newPullParser();// 利用ANDROID提供的API快速获得pull解析器
    pullParser.setInput(in, "UTF-8");// 设置需要解析的XML数据
    int event = pullParser.getEventType(); // 取得事件

    // 若为解析到末尾
    while (event != XmlPullParser.END_DOCUMENT) // 文档结束
    {
      String nodeName = pullParser.getName();
      switch (event) {
        case XmlPullParser.START_DOCUMENT: // 文档开始
          systems = new ArrayList<>();
          break;
        case XmlPullParser.START_TAG: // 标签开始
          system = new SystemInfo();
          if ("system".equals(nodeName)) {
            String name = pullParser.getAttributeValue(0);
            system.setName(name);
          }
          if ("address".equals(nodeName)) {
            system.setAddress(pullParser.nextText());
          }
          if ("description".equals(nodeName)) {
            system.setDescription(pullParser.nextText());
          }
          if ("password".equals(nodeName)) {
            system.setPassword(pullParser.nextText());
          }
          if ("hwsupport".equals(nodeName)) {
            system.setHwsupport(pullParser.nextText());
          }
          if ("formemory".equals(nodeName)) {
            system.setFormemory(pullParser.nextText());
          }
          if ("buildfor".equals(nodeName)) {
            system.setBuildfor(pullParser.nextText());
          }
          break;
        case XmlPullParser.END_TAG: // 标签结束
          if ("system".equals(nodeName)) {
            if (systems != null) systems.add(system);
            system = null;
          }
          break;
      }
      event = pullParser.next(); // 下一个标签
    }
    return systems;
  }

  public static SystemInfo systemUpdateXmlParser(InputStream in, String currentVersion) throws Exception {
    SystemInfo currentSystem = null;
    SystemInfo updateSystem = null;

    XmlPullParser pullParser = Xml.newPullParser();// 利用ANDROID提供的API快速获得pull解析器
    pullParser.setInput(in, "UTF-8");// 设置需要解析的XML数据
    int event = pullParser.getEventType(); // 取得事件

    // 若为解析到末尾
    while (event != XmlPullParser.END_DOCUMENT) // 文档结束
    {
      String nodeName = pullParser.getName();
      switch (event) {
        case XmlPullParser.START_DOCUMENT: // 文档开始
          break;
        case XmlPullParser.START_TAG: // 标签开始
          if ("system".equals(nodeName)) {
            String name = pullParser.getAttributeValue(0);
            if (name.equals(currentVersion)) {
              currentSystem = new SystemInfo();
              currentSystem.setName(name);
            } else break;
          }
          if ("address".equals(nodeName)) {
            if (currentSystem != null) currentSystem.setAddress(pullParser.nextText());
          }
          if ("description".equals(nodeName)) {
            if (currentSystem != null) currentSystem.setDescription(pullParser.nextText());
          }
          if ("password".equals(nodeName)) {
            if (currentSystem != null) currentSystem.setPassword(pullParser.nextText());
          }
          if ("hwsupport".equals(nodeName)) {
            if (currentSystem != null) currentSystem.setHwsupport(pullParser.nextText());
          }
          if ("formemory".equals(nodeName)) {
            if (currentSystem != null) currentSystem.setFormemory(pullParser.nextText());
          }
          if ("buildfor".equals(nodeName)) {
            if (currentSystem != null) currentSystem.setBuildfor(pullParser.nextText());
          }
          break;
        case XmlPullParser.END_TAG: // 标签结束
          if ("system".equals(nodeName)) {

          }
          break;
      }
      event = pullParser.next(); // 下一个标签
    }
    return updateSystem;
  }
}
