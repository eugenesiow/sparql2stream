package uk.ac.soton.ldanalytics.sparql2stream.cep;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;

public class ObjectFormat {
	public Map<String,Object> getDefinitionMap(String streamName,String formatFileName) {
		Map<String, Object> definition = new LinkedHashMap<String, Object>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(formatFileName));
			
			String line = "";
			while((line=br.readLine())!=null) {
				String[] parts = line.split(",");
				if(parts.length>1) {
					definition.put(parts[0], classMap(parts[1]));
				}
			}
					
			br.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return definition;
	}
	
	private Object classMap(String className) {
		Object object = null;
		switch(className.toLowerCase()) {
			case "string":
				object = String.class;
				break;
			case "float":
				object = Float.class;
				break;
			case "double":
				object = Double.class;
				break;
			case "time":
				object = Timestamp.class;
				break;
		}
		return object;
	}
}
