package uk.ac.soton.ldanalytics.sparql2stream.cep;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;

public class CepEngineRandom {
	public static void main(String[] args) {
		final long MAX_EVENTS = 100;
		final int delay = 1;
		
		EPServiceProvider epService = EPServiceProviderManager.getProvider("engine_test");
		String streamName = "_HP001";
		ObjectFormat objectFormat = new ObjectFormat();
		Map<String,Object> definition = objectFormat.getDefinitionMap(streamName);
        epService.getEPAdministrator().getConfiguration().addEventType(streamName, definition);
        
        long runTime = 10;
        String filename = "queries/q1.epl";
        if(args.length>0) {
			filename = args[0];
		}
        if(args.length>1) {
        	runTime = Long.parseLong(args[1]);
        }
        runTime *= 1000; //convert to ms
        
		try {
			if(!filename.equals("blank")) {
				String stmt = FileUtils.readFileToString(new File(filename));
				EPStatement statement = epService.getEPAdministrator().createEPL(stmt);
		        statement.addListener(new QueryListener());
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
        
        try {
        	BufferedWriter bw = new BufferedWriter(new FileWriter(filename + ".out"));
	        BufferedReader br = new BufferedReader(new FileReader("samples/"+streamName+".csv"));
	        br.readLine();//header
	        String line = "";
	        line=br.readLine();
	        br.close();
	        
        	String[] parts = line.split(",");
            Map<String, Object> data = new LinkedHashMap<String, Object>();
            int i=0;
            for(Entry<String,Object> row:definition.entrySet()) {
            	data.put(row.getKey(), convertStrToObject(parts[i++],row.getValue()));
            }
            
            long startTime = System.currentTimeMillis();
            long totalTimeTaken = 0;
            int counter = 1;
            while(totalTimeTaken < runTime) {
	            
	            long ms = System.currentTimeMillis();
	            for(int j=0;j<MAX_EVENTS;j++) {
	            	epService.getEPRuntime().sendEvent(data, streamName);
	            }
		        long timeTaken = System.currentTimeMillis() - ms;
		        totalTimeTaken  = System.currentTimeMillis() - startTime;
	            bw.append((counter++)+";"+totalTimeTaken/1000.0 + ";" + String.format("%.8f", (timeTaken*1.0)/(MAX_EVENTS*1.0)) + "\n");
	            bw.flush();
	            Thread.sleep(delay);
            }
            bw.append(Double.toString(totalTimeTaken/(counter*MAX_EVENTS*1.0)));
            bw.close();
	        
        }catch(IOException | InterruptedException e) {
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
