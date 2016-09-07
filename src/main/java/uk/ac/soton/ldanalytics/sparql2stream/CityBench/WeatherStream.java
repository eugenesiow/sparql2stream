package uk.ac.soton.ldanalytics.sparql2stream.CityBench;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.espertech.esper.client.EPServiceProvider;

public class WeatherStream {
	BufferedReader br = null;  
	EPServiceProvider epService = null;
	String streamName;
	String SEPARATOR = ",";
	List<String> header = new ArrayList<String>();

	public WeatherStream(EPServiceProvider epService, String streamName) {
		this.epService = epService;
		this.streamName = streamName;
		Map<String,Object> streamDefinition = new LinkedHashMap<String, Object>();
		streamDefinition.put("hum", Double.class);
		streamDefinition.put("tempm", Double.class);
		streamDefinition.put("wspdm", Double.class);
		streamDefinition.put("TIMESTAMP", String.class);
		epService.getEPAdministrator().getConfiguration().addEventType(streamName, streamDefinition);
	}

	public void setupSourceFile(String srcFile) {
		try {
			br = new BufferedReader(new FileReader(srcFile));
			for(String headerEl:br.readLine().split(SEPARATOR)) {
				header.add(headerEl);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendEvent() {
		try {
			String line = br.readLine();  
			if(line!=null) {
				epService.getEPRuntime().sendEvent(fillData(line), streamName);
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private Map<String, Object> fillData(String line) {
		Map<String, Object> data = new LinkedHashMap<String, Object>();
		String[] parts = line.split(SEPARATOR);
		for(String headerEl:header) {
			data.put(headerEl, parts[header.indexOf(headerEl)]);
		}
		return data;
	}

	public void shutdown() {
		try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
