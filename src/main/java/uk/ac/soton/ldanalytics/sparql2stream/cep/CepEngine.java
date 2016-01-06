package uk.ac.soton.ldanalytics.sparql2stream.cep;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;

public class CepEngine {
	private EPServiceProvider epService = null;
	private Map<String,Map<String,Object>> streams = new HashMap<String,Map<String,Object>>();
	
	public CepEngine(String providerName) {
		epService = EPServiceProviderManager.getProvider(providerName);
	}
	
	public void AddStream(String streamName,String formatFileName) {
		ObjectFormat objectFormat = new ObjectFormat();
		Map<String,Object> definition = objectFormat.getDefinitionMap(streamName,formatFileName);
		streams.put(streamName, definition);
        epService.getEPAdministrator().getConfiguration().addEventType(streamName, definition);
	}
	
	public void AddQuery(String queryStr) {
		EPStatement statement = epService.getEPAdministrator().createEPL(queryStr);
        statement.addListener(new QueryListener());
	}
	
	public void ReadFromCSV(String streamName, String fileName, Boolean headerLine) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
	        if(headerLine)
	        	br.readLine();//header
	        String line = "";
	        Map<String,Object> definition = streams.get(streamName);
	        while((line=br.readLine())!=null) {
	        	String[] parts = line.split(",");
	            Map<String, Object> data = new LinkedHashMap<String, Object>();
	            int i=0;
	            for(Entry<String,Object> row:definition.entrySet()) {
	            	data.put(row.getKey(), convertStrToObject(parts[i++],row.getValue()));
	            }
	            	
	            epService.getEPRuntime().sendEvent(data, streamName);
	        }
	        br.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
        
    private static Object convertStrToObject(String val, Object className) {
		Object object = null;
		if(className.equals(String.class)) {
			object = val;
		} else if(className.equals(Float.class)) {
			object = Float.parseFloat(val);
		} else if(className.equals(Timestamp.class)) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				object = new Timestamp(sdf.parse(val).getTime());
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return object;
	}
}
