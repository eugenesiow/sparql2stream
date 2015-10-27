package uk.ac.soton.ldanalytics.sparql2stream.cep;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;

public class CepEngine {
	public static void main(String[] args) {
		EPServiceProvider epService = EPServiceProviderManager.getProvider("engine_test");
		String streamName = "_HP001";
		ObjectFormat objectFormat = new ObjectFormat();
		Map<String,Object> definition = objectFormat.getDefinitionMap(streamName);
        epService.getEPAdministrator().getConfiguration().addEventType(streamName, definition);
        String stmt = "    SELECT\n" + 
        		"        DISTINCT 'http://knoesis.wright.edu/ssw/System_HP001' AS sensor ,\n" + 
        		"        _HP001.Precipitation AS value ,\n" + 
        		"        'http://knoesis.wright.edu/ssw/ont/weather.owl#centimeters' AS uom \n" + 
        		"    FROM\n" + 
        		"        _HP001.win:time(1 hour)  ";
        EPStatement statement = epService.getEPAdministrator().createEPL(stmt);
        statement.addListener(new QueryListener());
        
        long startTime = System.currentTimeMillis();
        int counter = 0;
        try {
	        BufferedReader br = new BufferedReader(new FileReader("samples/"+streamName+".csv"));
	        br.readLine();//header
	        String line = "";
	        while((line=br.readLine())!=null) {
	        	String[] parts = line.split(",");
	            Map<String, Object> data = new LinkedHashMap<String, Object>();
	            int i=0;
	            for(Entry<String,Object> row:definition.entrySet()) {
	            	data.put(row.getKey(), convertStrToObject(parts[i++],row.getValue()));
	            }
	            counter++;
	            	
	            epService.getEPRuntime().sendEvent(data, streamName);
//				Thread.sleep(1000);
	        }
	        long timeTaken = System.currentTimeMillis() - startTime;
            System.out.println(timeTaken);
            System.out.println(counter);
	        br.close();
        }catch(IOException e) {
        	e.printStackTrace();
//        } catch (InterruptedException e) {
//			e.printStackTrace();
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
