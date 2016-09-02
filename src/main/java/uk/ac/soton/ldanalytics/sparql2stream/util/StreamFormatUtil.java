package uk.ac.soton.ldanalytics.sparql2stream.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StreamFormatUtil {
	public static Map<String, String> loadStreamCatalog(String path) {
		Map<String,String> catalog = new HashMap<String,String>();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			String line="";
			while((line=br.readLine())!=null) {
				String[] parts = line.split(",");
				if(parts.length>1) {
					catalog.put(parts[0].trim(),parts[1].trim());
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return catalog;
	}
	
	public static Map<String, String> loadMappingCatalog(String path) {
		Map<String,String> catalog = new HashMap<String,String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			String line="";
			while((line=br.readLine())!=null) {
				String[] parts = line.split(",");
				if(parts.length>1) {
					catalog.put(parts[0].trim(),parts[1].trim());
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return catalog;
	}
}
