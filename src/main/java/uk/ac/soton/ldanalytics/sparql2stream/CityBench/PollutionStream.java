package uk.ac.soton.ldanalytics.sparql2stream.CityBench;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.espertech.esper.client.EPServiceProvider;

public class PollutionStream {
	BufferedReader br = null;  
	EPServiceProvider epService = null;
	String streamName;
	String SEPARATOR = ",";
	List<String> header = new ArrayList<String>();

	public PollutionStream(EPServiceProvider epService, String streamName) {
		this.epService = epService;
		this.streamName = streamName;
		Map<String,Object> streamDefinition = new LinkedHashMap<String, Object>();
		streamDefinition.put("ozone", Double.class);
		streamDefinition.put("particullate_matter", Double.class);
		streamDefinition.put("carbon_monoxide", Double.class);
		streamDefinition.put("sulfure_dioxide", Double.class);
		streamDefinition.put("nitrogen_dioxide", Double.class);
		streamDefinition.put("longitude", Double.class);
		streamDefinition.put("latitude", Double.class);
		streamDefinition.put("timestamp", String.class);
		streamDefinition.put("api", Double.class);
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
		Double ozone = Double.parseDouble(parts[header.indexOf("ozone")]);
		Double particullate_matter = Double.parseDouble(parts[header.indexOf("particullate_matter")]);
		Double sulfure_dioxide = Double.parseDouble(parts[header.indexOf("sulfure_dioxide")]);
		Double nitrogen_dioxide = Double.parseDouble(parts[header.indexOf("nitrogen_dioxide")]);
		Double carbon_monoxide = Double.parseDouble(parts[header.indexOf("carbon_monoxide")]);
		Double api = ozone;
		if (particullate_matter > api)
			api = particullate_matter;
		if (carbon_monoxide > api)
			api = carbon_monoxide;
		if (sulfure_dioxide > api)
			api = sulfure_dioxide;
		if (nitrogen_dioxide > api)
			api = nitrogen_dioxide;
		data.put("api", api);
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
