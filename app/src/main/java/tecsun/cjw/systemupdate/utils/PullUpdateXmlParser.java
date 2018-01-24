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
		List<String> buildfors = null;

		XmlPullParser pullParser = Xml.newPullParser();// 利用ANDROID提供的API快速获得pull解析器
		pullParser.setInput(in, "GB2312");// 设置需要解析的XML数据
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
					if ("system".equals(nodeName)) {
						system = new SystemInfo();
						buildfors = new ArrayList<>();
						String name = pullParser.getAttributeValue(null, "name");
						system.setName(name);
					} else if ("address".equals(nodeName)) {
						if (system != null) system.setAddress(pullParser.nextText());
					} else if ("description".equals(nodeName)) {
						if (system != null) system.setDescription(pullParser.nextText());
					} else if ("password".equals(nodeName)) {
						if (system != null) system.setPassword(pullParser.nextText());
					} else if ("hwsupport".equals(nodeName)) {
						if (system != null) system.setHwsupport(pullParser.nextText());
					} else if ("formemory".equals(nodeName)) {
						if (system != null) system.setFormemory(pullParser.nextText());
					} else if ("buildfor".equals(nodeName)) {
						if (buildfors != null) buildfors.add(pullParser.nextText());
					}
					break;
				case XmlPullParser.END_TAG: // 标签结束
					if ("system".equals(nodeName)) {
						if (systems != null && system != null) {
							system.setBuildfors(buildfors);
							systems.add(system);
						}
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
		List<String> buildfors = null;
		boolean flag = false;//是否已找到当前版本，开始查找新版本

		XmlPullParser pullParser = Xml.newPullParser();// 利用ANDROID提供的API快速获得pull解析器
		pullParser.setInput(in, "GB2312");// 设置需要解析的XML数据
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
						String name = pullParser.getAttributeValue(null, "name");
						if (name.equals(currentVersion)) {
							currentSystem = new SystemInfo();
							currentSystem.setName(name);
						}
						if (flag) {
							updateSystem = new SystemInfo();
							buildfors = new ArrayList<>();
							updateSystem.setName(name);
						}
					} else if ("address".equals(nodeName)) {
						String address = pullParser.nextText();
						if (!flag && currentSystem != null) currentSystem.setAddress(address);
						if (flag && updateSystem != null) updateSystem.setAddress(address);
					} else if ("description".equals(nodeName)) {
						String description = pullParser.nextText();
						if (!flag && currentSystem != null) currentSystem.setDescription(description);
						if (flag && updateSystem != null) updateSystem.setDescription(description);
					} else if ("password".equals(nodeName)) {
						String password = pullParser.nextText();
						if (!flag && currentSystem != null) currentSystem.setPassword(password);
						if (flag && updateSystem != null) updateSystem.setPassword(password);
					} else if ("hwsupport".equals(nodeName)) {
						String hwsupport = pullParser.nextText();
						if (!flag && currentSystem != null) currentSystem.setHwsupport(hwsupport);
						if (flag && updateSystem != null) updateSystem.setHwsupport(hwsupport);
					} else if ("formemory".equals(nodeName)) {
						String formemory = pullParser.nextText();
						if (!flag && currentSystem != null) currentSystem.setFormemory(formemory);
						if (flag && updateSystem != null) updateSystem.setFormemory(formemory);
					} else if ("buildfor".equals(nodeName)) {
						String buildfor = pullParser.nextText();
						if (flag && updateSystem != null) buildfors.add(buildfor);
					}
					break;
				case XmlPullParser.END_TAG: // 标签结束
					if ("system".equals(nodeName)) {
						if (!flag && currentSystem != null) flag = true;
						if (flag && updateSystem != null) {
							updateSystem.setBuildfors(buildfors);
							if (("invalid").equals(updateSystem.getHwsupport()) || (!currentSystem.getFormemory().equals(updateSystem.getFormemory())) || (!updateSystem.getBuildfors().contains(currentVersion))) {
								updateSystem = null;
								buildfors = null;
							} else {//成功
								return updateSystem;
							}
						}
					}
					break;
			}
			event = pullParser.next(); // 下一个标签
		}
		return null;
	}
}
