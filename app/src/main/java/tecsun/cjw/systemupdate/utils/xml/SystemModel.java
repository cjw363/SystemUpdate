package tecsun.cjw.systemupdate.utils.xml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SystemModel implements Serializable{
	private String name;
	private String addr;
	private String description;
	private String password;
	private String formemory;
	private List<String> hwSupportList;
	private List<Target> tagetList;
	
	public SystemModel(){
		if(tagetList == null){
			tagetList = new ArrayList<Target>();
		}
		if(hwSupportList == null){
			hwSupportList  = new ArrayList<String>();
		}
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
	
	public void setAddr(String addr){
		this.addr = addr;
	}
	
	public String getAddr(){
		return addr;
	}
	
	public void setDescription(String description){
		this.description = description;
	}
	
	public String getDescription(){
		return description;
	}
	
	public void setPassword(String password){
		this.password = password;
	}
	
	public String getPassword(){
		return password;
	}
	
	public void setForMemory(String forMemory){
		this.formemory = forMemory;
	}
	
	public String getForMemory(){
		return formemory;
	}
	
	public void addHWsupport(String hwsupport){
		this.hwSupportList.add(hwsupport);
	}
	
	public List<String> getHWsupport(){
		return hwSupportList;
	}
	
	public void addTaget(Target target){
		tagetList.add(target);
	}
	
	public List<Target> getTagetList(){
		return tagetList;
	}
	
	
	public class Target implements Serializable{
		private String name;
		private String addr;
		private String description;
		private String password;
		private String formemory;
		private List<String> hwSupportList;
		private ArrayList<LinkedHashMap<String,String>> steps;
		public Target(){
			steps = new ArrayList<LinkedHashMap<String,String>>();
			if(hwSupportList == null){
				hwSupportList  = new ArrayList<String>();
			}
		}
		public void setAddr(String addr){
			this.addr = addr;
		}
		public String getAddr(){
			return addr;
		}
		public void setName(String name){
			this.name = name;
		}
		public String getName(){
			return name;
		}
		
		public void setDescription(String description){
			this.description = description;
		}
		
		public String getDescription(){
			return description;
		}
		
		public void setPassword(String password){
			this.password = password;
		}
		
		public String getPassword(){
			return password;
		}
		
		public void setForMemory(String forMemory){
			this.formemory = forMemory;
		}
		
		public String getForMemory(){
			return formemory;
		}
		
		public void addHWsupport(String hwsupport){
			this.hwSupportList.add(hwsupport);
		}
		
		public List<String> getHWsupport(){
			return hwSupportList;
		}
		
		public void addstep(String name ,String url,String description,String password){
			LinkedHashMap<String,String> step = new LinkedHashMap<String,String>();
			step.put("name", name);
			step.put("url", url);
			step.put("description", description);
			step.put("password", password);
			steps.add(step);
		}
		public ArrayList<LinkedHashMap<String,String>> getsteps(){
			return steps;
		}		
	}
	
}
