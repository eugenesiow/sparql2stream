package uk.ac.soton.ldanalytics.sparql2stream.cep;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;

public class CepEngineSmarthomeTimestamps {
	public static void main(String[] args) {		
		EPServiceProvider epService = EPServiceProviderManager.getProvider("engine_test");
		ObjectFormat objectFormat = new ObjectFormat();
		Map<String,Object> definitionEnv = objectFormat.getDefinitionMap("environment","format/environment.format");
		Map<String,Object> definitionMeter = objectFormat.getDefinitionMap("meter","format/meter.format");
		Map<String,Object> definitionMotion = objectFormat.getDefinitionMap("motion","format/motion.format");
        epService.getEPAdministrator().getConfiguration().addEventType("environment", definitionEnv);
        epService.getEPAdministrator().getConfiguration().addEventType("meter", definitionMeter);
        epService.getEPAdministrator().getConfiguration().addEventType("motion", definitionMotion);
        
        String filename = "queries/smarthome/q4.epl";
        if(args.length>0) {
			filename = args[0];
		}
        int MAX_EVENTS = 101; //total number of times to run
        if(args.length>1) {
        	MAX_EVENTS = Integer.parseInt(args[1]);
        }
        int delay = 1000;
        if(args.length>2) { //delay after each send
        	delay = Integer.parseInt(args[2]);
        }
        
		try {
			if(!filename.equals("blank")) {
				String stmt = FileUtils.readFileToString(new File(filename));
				EPStatement statement = epService.getEPAdministrator().createEPL(stmt);
		        statement.addListener(new QueryTimestampListener());
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
        
        try {
        	BufferedWriter bw = new BufferedWriter(new FileWriter(filename + ".out"));
	        BufferedReader br1 = new BufferedReader(new FileReader("samples/environment.csv"));
	        BufferedReader br2 = new BufferedReader(new FileReader("samples/meter.csv"));
	        BufferedReader br3 = new BufferedReader(new FileReader("samples/motion.csv"));
	        String line1="",line2="",line3 = "";	        
	        
            
            int counter = 0;
            while(counter < MAX_EVENTS) {
            	line1=br1.readLine();
            	line2=br2.readLine();
            	line3=br3.readLine();
            	Map<String, Object> data1 = new LinkedHashMap<String, Object>();
                Map<String, Object> data2 = new LinkedHashMap<String, Object>();
                Map<String, Object> data3 = new LinkedHashMap<String, Object>();
                
                String[] parts = line1.split(",");
                int i=0;
                for(Entry<String,Object> row:definitionEnv.entrySet()) {
                	data1.put(row.getKey(), convertStrToObject(parts[i++],row.getValue()));
                }
                i=0;
                parts = line2.split(",");
                for(Entry<String,Object> row:definitionMeter.entrySet()) {
                	data2.put(row.getKey(), convertStrToObject(parts[i++],row.getValue()));
                }
                i=0;
                parts = line3.split(",");
                for(Entry<String,Object> row:definitionMotion.entrySet()) {
                	data3.put(row.getKey(), convertStrToObject(parts[i++],row.getValue()));
                }
                
	            long ms = System.currentTimeMillis();
	            epService.getEPRuntime().sendEvent(data1, "environment");
	            epService.getEPRuntime().sendEvent(data2, "meter");
	            epService.getEPRuntime().sendEvent(data3, "motion");
	            bw.append(ms + "\n");
	            bw.flush();
	            counter++;
	            Thread.sleep(delay);
            }
            br1.close();
            br2.close();
            br3.close();
            bw.close();
	        
        }catch(IOException | InterruptedException e) {
        	e.printStackTrace();
        }
	}

	private static Object convertStrToObject(String val, Object className) {
		Object object = null;
		if(className.equals(String.class)) {
			object = val;
		} else if(className.equals(Float.class)) {
			object = Float.parseFloat(val);
		} else if(className.equals(Integer.class)) {
			object = Integer.parseInt(val);
		} else if(className.equals(Long.class)) {
			object = System.currentTimeMillis();
		}
		return object;
	}
}
