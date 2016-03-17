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

public class CepEngineRandomBench {
	public static void main(String[] args) {		
		EPServiceProvider epService = EPServiceProviderManager.getProvider("engine_test");
		String streamName = "_HP001";
		ObjectFormat objectFormat = new ObjectFormat();
		Map<String,Object> definition = objectFormat.getDefinitionMap(streamName,"format/"+streamName+".format");
        epService.getEPAdministrator().getConfiguration().addEventType(streamName, definition);
        
        String filename = "queries/q1.epl";
        if(args.length>0) {
			filename = args[0];
		}
        int MAX_EVENTS = 1001; //total number of times to run
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
		        statement.addListener(new QueryRecorderListener());
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
            
            int counter = 0;
            while(counter < MAX_EVENTS) {
	            long ms = System.currentTimeMillis();
	            epService.getEPRuntime().sendEvent(data, streamName);
	            bw.append(ms + "\n");
	            bw.flush();
	            counter++;
	            Thread.sleep(delay);
            }
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
