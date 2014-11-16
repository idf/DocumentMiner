package km.common.json;

import com.google.gson.Gson;

public class JsonParserTest {
	public class Item {
		private int id;
		
		public int getId() {
			return id;
		}
	}
	
	public static void main(String[]  args) {
		Gson gson = new Gson();
		String json = "{id:3}";
		Item item = gson.fromJson(json, Item.class);
		System.out.println(item.getId());
	}
}
