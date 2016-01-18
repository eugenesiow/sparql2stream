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
	protected EPServiceProvider epService = null;
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
		String queryHash = Long.toString(queryStr.hashCode());
        statement.addListener(new QueryListener(queryHash));
	}
	
	public void PlayFromCSV(String streamName, String fileName, Boolean headerLine, int timeColumnPos, String timeFormat, int fixedDelay) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
	        if(headerLine)
	        	br.readLine();//header
	        
	        SimpleDateFormat sdf = new SimpleDateFormat(timeFormat);
	        
	        long previousTime = 0;
	        long delay = 0;
	        if(fixedDelay>0) {
	        	delay = fixedDelay;
	        }
	        
	        String line = "";
	        Map<String,Object> definition = streams.get(streamName);
	        while((line=br.readLine())!=null) {
	        	String[] parts = line.split(",");
	        	
	        	if(fixedDelay<0) {
		        	if(timeColumnPos>=0) { //has timeColumn
		        		long rowTime = sdf.parse(parts[timeColumnPos]).getTime();
		        		if(previousTime > 0) {
		        			delay = rowTime - previousTime;
		        		}
		        		previousTime = rowTime;
		        	}
	        	}
	        	
	            Map<String, Object> data = new LinkedHashMap<String, Object>();
	            int i=0;
	            for(Entry<String,Object> row:definition.entrySet()) {
//	            	System.out.println(row.getKey());
	            	data.put(row.getKey(), convertStrToObject(parts[i++],row.getValue(),timeFormat));
	            }
	            
//	            delay = 1000; //override delay for testing
	            	
	            epService.getEPRuntime().sendEvent(data, streamName);
	            Thread.sleep(delay);
//	            System.out.println(line);
	        }
	        br.close();
		} catch(IOException | ParseException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void ReadFromCSV(String streamName, String fileName, Boolean headerLine) {
		PlayFromCSV(streamName,fileName,headerLine,-1,"",-1);
	}
        
    private static Object convertStrToObject(String val, Object className, String timeFormat) {
		Object object = null;
		if(className.equals(String.class)) {
			object = val;
		} else if(className.equals(Float.class)) {
			object = Float.parseFloat(val);
		} else if(className.equals(Timestamp.class)) {
			SimpleDateFormat sdf = new SimpleDateFormat(timeFormat);
			try {
				object = new Timestamp(sdf.parse(val).getTime());
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return object;
	}
}
