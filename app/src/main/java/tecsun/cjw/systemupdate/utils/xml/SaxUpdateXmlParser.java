package tecsun.cjw.systemupdate.utils.xml;

import android.content.Context;
import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import javax.xml.parsers.SAXParserFactory;

public class SaxUpdateXmlParser implements IUpdatexmlParser {

	public static final String TAG = "cd";
	private String hwVersion;
	private String memory;
	
	@Override
	public List<SystemModel> parse(Context context,InputStream is) throws Exception {
		//SAXParserFactory factory = SAXParserFactory.newInstance(); // 取得SAXParserFactory实例
		//SAXParser parser = factory.newSAXParser(); // 从factory获取SAXParser实例
//		MyHandler handler = new MyHandler(); // 实例化自定义Handler
//		parser.parse(is, handler); // 根据自定义Handler规则解析输入流
		
		memory = MemoryUtils.getTotalMemory(context);
//		memory = "2G";
		
		hwVersion = "未设置";
		HWVersionDao.initializeInstance(context);
		HWVersionDao hWVersionDao = HWVersionDao.getInstance();
		hWVersionDao.openDatabase();
		HwVersionModel hwVersionModel;
		hwVersionModel = hWVersionDao.query();
		hWVersionDao.closeDatabase();
		if (hwVersionModel != null) {
			hwVersion = hwVersionModel.getHwVersion();
		}
		SAXParserFactory factory = SAXParserFactory.newInstance();
		XMLReader reader = null;
		reader = factory.newSAXParser().getXMLReader();
		MyHandler handler = new MyHandler(context);
		reader.setContentHandler(handler);// 解析类
		InputStreamReader isr = new InputStreamReader(is, "GB2312");
		reader.parse(new InputSource(isr));
		is.close();
		isr.close();
		
		
		return handler.getBooks();
	}

	@Override
	public String serialize(List<SystemModel> SystemModelList) throws Exception {
		return null;
	}

	// 需要重写DefaultHandler的方法
	private class MyHandler extends DefaultHandler {

		private List<SystemModel> systems;
		private SystemModel system;
		private StringBuilder builder;
		private Context context;
		
		public MyHandler(Context context){
			this.context = context;
		}
		
		// 返回解析后得到的Book对象集合
		public List<SystemModel> getBooks() {
			return systems;
		}

		@Override
		public void startDocument() throws SAXException {
			super.startDocument();
			systems = new ArrayList<SystemModel>();
			builder = new StringBuilder();
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			super.startElement(uri, localName, qName, attributes);
			if (localName.equals("system")) {
				system = new SystemModel();
				String name = attributes.getValue(0);
				system.setName(name);
			}
			builder.setLength(0); // 将字符长度设置为0 以便重新开始读取元素内的字符节点
		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			super.characters(ch, start, length);
			builder.append(ch, start, length); // 将读取的字符数组追加到builder中
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			super.endElement(uri, localName, qName);
			if (localName.equals("address")) {
				system.setAddr(builder.toString());
			}else if (localName.equals("description")) {
				system.setDescription(builder.toString());
			}else if (localName.equals("password")) {
				system.setPassword(builder.toString());
			}else if (localName.equals("hwsupport")) {
				system.addHWsupport(builder.toString());
			}else if (localName.equals("formemory")) {
				Log.d("cd", "formemory"+builder.toString());
				system.setForMemory(builder.toString());
			}else if (localName.equals("buildfor")) {
				//Log.d(TAG, "builder.toString() : " + builder.toString());
				for (int i = 0; i < systems.size(); i++) {					
					SystemModel temp = systems.get(i);
					//Log.d(TAG, "source : " + temp.getName());
										
					if ((system.getForMemory().equals(memory))&&((system.getHWsupport().size()==0 && temp.getName().equals(builder.toString()))
							|| (system.getHWsupport().toString().contains(hwVersion) && temp.getName().equals(builder.toString())))) {
						Log.d(TAG, "target : " + system.getName());
						SystemModel.Target target = temp.new Target();
						target.setName(system.getName());
						target.setAddr(system.getAddr());
						target.setDescription(system.getDescription());
						target.setPassword(system.getPassword());
						temp.addTaget(target);
						break;
					}
					List<SystemModel.Target> tempList = temp.getTagetList();					
					for (int ii = tempList.size() - 1; ii >= 0; ii--) {
						SystemModel.Target tar = tempList.get(ii);
//						if (tar.getName().equals(builder.toString())) {
						if ((system.getForMemory().equals(memory))&&((system.getHWsupport().size()==0 && tar.getName().equals(builder.toString()))
								|| (system.getHWsupport().toString().contains(hwVersion) && tar.getName().equals(builder.toString())))) {
							SystemModel.Target target = temp.new Target();
							target.setAddr(system.getAddr());
							target.setName(system.getName());
							target.setDescription(system.getDescription());
							ArrayList<LinkedHashMap<String,String>> steps = tar.getsteps();
							Iterator<LinkedHashMap<String,String>> it = steps.iterator();
							while(it.hasNext()){
								LinkedHashMap<String,String> step = it.next();
								target.addstep(step.get("name"), step.get("url"), step.get("description"),step.get("password"));
							}
							//Log.d(TAG, "step : " + tar.getName());
							//Log.d(TAG, "target : " + system.getName());
							target.addstep(tar.getName(),tar.getAddr(),tar.getDescription(),tar.getPassword());
							temp.addTaget(target);
							break;
						}
					}

				}
			} else if (localName.equals("system")) {
				systems.add(system);
			}
		}
	}

}
