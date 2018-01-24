package tecsun.cjw.systemupdate.utils.xml;

public class HwVersionModel {
	
	String hwVersion;
	
	public HwVersionModel(){}
	
	public HwVersionModel(String hwVersion){
		this.hwVersion = hwVersion;
	}
	
	public void setHwVersion(String hwVersion){
		this.hwVersion = hwVersion;
	}
	public String getHwVersion(){
		return hwVersion;
	}
}
