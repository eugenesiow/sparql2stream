package uk.ac.soton.ldanalytics.sparql2stream.cep;

import java.util.Map;
import java.util.Map.Entry;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class QueryListener implements UpdateListener {
	private String queryName;
	
	public QueryListener(String queryName) {
		this.queryName = queryName;
	}

	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		if(newEvents.length>0) {
			JsonObject message = new JsonObject();
			message.addProperty("queryName", queryName);
			for(int i=0;i<newEvents.length;i++) {
				for(Object map:((Map<?, ?>)newEvents[i].getUnderlying()).entrySet()) {
					Entry<?, ?> entry = ((Entry<?, ?>)map);
//					System.out.println(entry.getKey()+":"+entry.getValue());
					String key = entry.getKey().toString();
					String val = entry.getValue().toString();
					if(message.has(key)) {
						JsonElement jVal = message.get(key);
						JsonArray newVal = new JsonArray();
						if(jVal.isJsonArray()) {
							newVal = jVal.getAsJsonArray();
						} else if(jVal.isJsonPrimitive()) {
							newVal.add(jVal.getAsString());
						}
						newVal.add(val);
						message.add(key, newVal);
					} else {
						message.addProperty(key,val);
					}
				}
			}
			System.out.println(message.toString());
		}
	}

}
