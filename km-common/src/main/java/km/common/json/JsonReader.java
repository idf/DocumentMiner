package km.common.json;

import io.deepreader.java.commons.util.IOHandler;

import java.io.*;
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
		JsonReader<T> jr = new JsonReader<>(filename, cls);
		return jr.getList();
	}

	public List<T> getList() throws IOException {
		List<T> lst = new ArrayList<>();
		T t;
		while((t=this.next())!=null) {
			lst.add(t);
		}
		this.close();
		return lst;
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
		IOHandler.safeClose(br);
	}
}
