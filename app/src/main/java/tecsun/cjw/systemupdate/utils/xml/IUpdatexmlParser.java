package tecsun.cjw.systemupdate.utils.xml;

import android.content.Context;

import java.io.InputStream;
import java.util.List;

public interface IUpdatexmlParser {

	public List<SystemModel> parse(Context context, InputStream is) throws Exception;
	
	public String serialize(List<SystemModel> SystemModelList) throws Exception;
}
