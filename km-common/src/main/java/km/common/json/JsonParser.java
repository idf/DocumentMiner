package km.common.json;

public interface JsonParser {
	String toJson(Object obj);

	<T> T fromJson(String src, Class<T> cls);
}
