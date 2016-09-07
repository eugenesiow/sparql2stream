package uk.ac.soton.ldanalytics.sparql2stream.CityBench;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.csvreader.CsvReader;
import com.espertech.esper.client.EPServiceProvider;

public class TrafficStream {
	BufferedReader br = null;  
	EPServiceProvider epService = null;
	String streamName;
	Double distance;
	String SEPARATOR = ",";
	List<String> header = new ArrayList<String>();

	public TrafficStream(EPServiceProvider epService, String streamName, String reportId, String metadataPath) {
		this.epService = epService;
		this.streamName = streamName;
		Map<String,Object> streamDefinition = new LinkedHashMap<String, Object>();
		streamDefinition.put("status", String.class);
		streamDefinition.put("avgMeasuredTime", Double.class);
		streamDefinition.put("averageSpeed", Double.class);
		streamDefinition.put("extID", Double.class);
		streamDefinition.put("medianMeasuredTime", Double.class);
		streamDefinition.put("TIMESTAMP", String.class);
		streamDefinition.put("vehicleCount", Double.class);
		streamDefinition.put("_id", Double.class);
		streamDefinition.put("REPORT_ID", Double.class);
		streamDefinition.put("congestionLevel", Double.class);
		saveMetaData(reportId,metadataPath);
		epService.getEPAdministrator().getConfiguration().addEventType(streamName, streamDefinition);
	}
	
	private void saveMetaData(String reportId,String metadataPath) {
		try {
			CsvReader metaData = new CsvReader(metadataPath);
			metaData.readHeaders();
			while (metaData.readRecord()) {
				if (reportId.equals(metaData.get("REPORT_ID"))) {
					distance = Double.parseDouble(metaData.get("DISTANCE_IN_METERS")); //read the distance of this particular station from metadata file
					metaData.close();
					break;
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
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
		data.put("congestionLevel", (Double.parseDouble(parts[header.indexOf("vehicleCount")]) / distance));
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
