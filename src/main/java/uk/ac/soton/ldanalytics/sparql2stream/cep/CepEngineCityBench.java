package uk.ac.soton.ldanalytics.sparql2stream.cep;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import uk.ac.soton.ldanalytics.sparql2stream.CityBench.TrafficStream;
import uk.ac.soton.ldanalytics.sparql2stream.CityBench.UserLocationStream;
import uk.ac.soton.ldanalytics.sparql2stream.CityBench.WeatherStream;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;

public class CepEngineCityBench {
	public static void main(String[] args) {
		testQ3();
	}
	
	public static void testQ1() {
		EPServiceProvider epService = EPServiceProviderManager.getProvider("engine_test");
		TrafficStream AarhusTrafficData182955 = new TrafficStream(epService,"AarhusTrafficData182955","182955","dataset/trafficMetaData.csv");
		AarhusTrafficData182955.setupSourceFile("streams/AarhusTrafficData182955.stream");
		TrafficStream AarhusTrafficData158505 = new TrafficStream(epService,"AarhusTrafficData158505","158505","dataset/trafficMetaData.csv");
		AarhusTrafficData158505.setupSourceFile("streams/AarhusTrafficData158505.stream");
		String stmt = "";
		try {
			stmt = FileUtils.readFileToString(new File("Queries/CityBench/q1.epl"));
		} catch(IOException e) {
			e.printStackTrace();
		}
        EPStatement statement = epService.getEPAdministrator().createEPL(stmt);
        statement.addListener(new SimpleQueryListener());
        
        for(int i=0;i<100;i++) {
        	AarhusTrafficData182955.sendEvent();
        	AarhusTrafficData158505.sendEvent();
        }
        
        //shutdown
        AarhusTrafficData182955.shutdown();
        AarhusTrafficData158505.shutdown();
        epService.destroy();
	}
	
	public static void testQ2() {
		EPServiceProvider epService = EPServiceProviderManager.getProvider("engine_test");
		WeatherStream AarhusWeatherData0 = new WeatherStream(epService,"AarhusWeatherData0");
		AarhusWeatherData0.setupSourceFile("streams/AarhusWeatherData0.stream");
		TrafficStream AarhusTrafficData158505 = new TrafficStream(epService,"AarhusTrafficData158505","158505","dataset/trafficMetaData.csv");
		AarhusTrafficData158505.setupSourceFile("streams/AarhusTrafficData158505.stream");
		String stmt = "";
		try {
			stmt = FileUtils.readFileToString(new File("Queries/CityBench/q2.epl"));
		} catch(IOException e) {
			e.printStackTrace();
		}
        EPStatement statement = epService.getEPAdministrator().createEPL(stmt);
        statement.addListener(new SimpleQueryListener());
        
        for(int i=0;i<100;i++) {
        	AarhusWeatherData0.sendEvent();
        	AarhusTrafficData158505.sendEvent();
        }
        
        //shutdown
        AarhusWeatherData0.shutdown();
        AarhusTrafficData158505.shutdown();
        epService.destroy();
	}
	
	public static void testQ3() {
		EPServiceProvider epService = EPServiceProviderManager.getProvider("engine_test");
		TrafficStream AarhusTrafficData182955 = new TrafficStream(epService,"AarhusTrafficData182955","182955","dataset/trafficMetaData.csv");
		AarhusTrafficData182955.setupSourceFile("streams/AarhusTrafficData182955.stream");
		TrafficStream AarhusTrafficData158505 = new TrafficStream(epService,"AarhusTrafficData158505","158505","dataset/trafficMetaData.csv");
		AarhusTrafficData158505.setupSourceFile("streams/AarhusTrafficData158505.stream");
		String stmt = "";
		try {
			stmt = FileUtils.readFileToString(new File("Queries/CityBench/q3.epl"));
		} catch(IOException e) {
			e.printStackTrace();
		}
        EPStatement statement = epService.getEPAdministrator().createEPL(stmt);
        statement.addListener(new SimpleQueryListener());
        
        for(int i=0;i<100;i++) {
        	AarhusTrafficData182955.sendEvent();
        	AarhusTrafficData158505.sendEvent();
        }
        
        //shutdown
        AarhusTrafficData182955.shutdown();
        AarhusTrafficData158505.shutdown();
        epService.destroy();
	}
	
	public static void testQ4() {
		EPServiceProvider epService = EPServiceProviderManager.getProvider("engine_test");
		UserLocationStream UserLocationService = new UserLocationStream(epService,"UserLocationService");
		UserLocationService.setupSourceFile("streams/UserLocationService.stream");
		String stmt = "";
		try {
			stmt = FileUtils.readFileToString(new File("Queries/CityBench/q4.epl"));
		} catch(IOException e) {
			e.printStackTrace();
		}
        EPStatement statement = epService.getEPAdministrator().createEPL(stmt);
        statement.addListener(new SimpleQueryListener());
        
        for(int i=0;i<100;i++) {
        	UserLocationService.sendEvent();
        }
        
        //shutdown
        UserLocationService.shutdown();
        epService.destroy();
	}
}
