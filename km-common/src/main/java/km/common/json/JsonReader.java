package km.common.json;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class JsonReader<T> {
	private JsonParser parser;
	private BufferedReader br;
	private Class<T> cls;

	public JsonReader(String filename, Class<T> cls) throws FileNotFoundException {
		this.parser = GsonParser.getInstance();
		br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filename))));
		this.cls = cls;
	}
	
	public static <T> T getObject(String filename, Class<T> cls) throws IOException {
		JsonReader<T> jr = new JsonReader<T>(filename, cls);
		T t = jr.next();
		jr.close();
		return t;
	}
	
	public static <T> List<T> getList(String filename, Class<T> cls) throws IOException {
		JsonReader<T> jr = new JsonReader<T>(filename, cls);
		List<T> tList = new ArrayList<T>();
		T t;
		while ((t=jr.next()) != null) {
			tList.add(t);
		}
		jr.close();
		return tList;
	}

	public T next() throws IOException {
		String line = br.readLine();

		if (line == null) {
			return null;
		}

		T t = parser.fromJson(line, cls);
		return t;
	}

	public void close() throws IOException {
		br.close();
	}
}
