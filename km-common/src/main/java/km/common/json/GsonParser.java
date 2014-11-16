package km.common.json;

import com.google.gson.Gson;

public class GsonParser implements JsonParser {

	private static GsonParser instance;
	private Gson gson;

	private GsonParser() {
		gson = new Gson();
	}
	
	public static GsonParser getInstance() {
		if (instance == null) {
			instance = new GsonParser();
		}
		return instance;
	}

	@Override
	public String toJson(Object obj) {
		return gson.toJson(obj);
	}

	@Override
	public <T> T fromJson(String src, Class<T> cls) {
		return gson.fromJson(src, cls);
	}
}
