package congge.utils;

import java.util.HashMap;
import java.util.Map;

public class ServerUtils {
	
	public static Map<String, String> service_info ;
	
	static{
		service_info = new HashMap<String, String>();
		service_info.put("18085", "127.0.0.1");
		service_info.put("18086", "127.0.0.1");
	}
	
}
