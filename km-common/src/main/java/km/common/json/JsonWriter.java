package km.common.json;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

public class JsonWriter {
	private JsonParser parser;
	private BufferedWriter bw;
	
	public JsonWriter(String filename) throws FileNotFoundException {
		this.parser = GsonParser.getInstance();
		bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(filename))));
	}

	public JsonWriter(String filename, boolean append) throws FileNotFoundException {
		this.parser = GsonParser.getInstance();
		bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(filename), append)));
	}
	
	public static void saveObject(Object obj, String filename) throws IOException {
		JsonWriter jw = new JsonWriter(filename);
		jw.write(obj);
		jw.close();
	}
	
	public static void saveList(List<?> objects, String filename) throws IOException {
		JsonWriter jw = new JsonWriter(filename);
		jw.write(objects);
		jw.close();
	}

	public static void saveList(List<?> objects, String filename, boolean append) throws IOException {
		JsonWriter jw = new JsonWriter(filename, append);
		jw.write(objects);
		jw.close();
	}

	public void write(Object obj) throws IOException {
		bw.write(parser.toJson(obj));
		bw.write("\n");
	}
	
	public void write(List<?> objects) throws IOException {
		for (Object obj: objects) {
			write(obj);
		}
	}
	
	public void close() throws IOException {
		bw.close();
	}
}
