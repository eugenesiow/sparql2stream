package uk.ac.soton.ldanalytics.sparql2stream.cep;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpWalker;

import uk.ac.soton.ldanalytics.sparql2sql.model.RdfTableMapping;
import uk.ac.soton.ldanalytics.sparql2sql.model.RdfTableMappingJena;
import uk.ac.soton.ldanalytics.sparql2sql.model.SparqlOpVisitor;
import uk.ac.soton.ldanalytics.sparql2sql.util.SQLFormatter;
import uk.ac.soton.ldanalytics.sparql2stream.CityBench.TrafficStream;
import uk.ac.soton.ldanalytics.sparql2stream.CityBench.UserLocationStream;
import uk.ac.soton.ldanalytics.sparql2stream.CityBench.WeatherStream;
import uk.ac.soton.ldanalytics.sparql2stream.parser.StreamQueryFactory;
import uk.ac.soton.ldanalytics.sparql2stream.util.StreamFormatUtil;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.ConfigurationDBRef;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;

public class CepEngineCityBench {
	public static void main(String[] args) {
		testQ5();
	}
	
	public static void testQ1() {
		EPServiceProvider epService = EPServiceProviderManager.getProvider("engine_test");
		TrafficStream AarhusTrafficData182955 = new TrafficStream(epService,"AarhusTrafficData182955","182955","dataset/trafficMetaData.csv");
		AarhusTrafficData182955.setupSourceFile("streams/AarhusTrafficData182955.stream");
		TrafficStream AarhusTrafficData158505 = new TrafficStream(epService,"AarhusTrafficData158505","158505","dataset/trafficMetaData.csv");
		AarhusTrafficData158505.setupSourceFile("streams/AarhusTrafficData158505.stream");
		String stmt = getStatement("q1");
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
		String stmt = getStatement("q2");
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
	
	private static String getStatement(String queryName) {
		String stmt = null;
		try {
			String queryStr = FileUtils.readFileToString(new File("Queries/CityBench/"+queryName+".sparql"));
			RdfTableMapping mapping = new RdfTableMappingJena();
			mapping.loadMapping("mapping/SensorRepository.nt");
			Map<String,String> streamCatalog = StreamFormatUtil.loadStreamCatalog("streams/catalogue.txt");
			Map<String,String> mappingCatalog = StreamFormatUtil.loadMappingCatalog("streams/mapping_catalogue.txt");
			Query query = StreamQueryFactory.create(queryStr);
			Op op = Algebra.compile(query);
			
			SparqlOpVisitor v = new SparqlOpVisitor();
			v.setMappingCatalog(mappingCatalog,"jena");
			v.useMapping(mapping);
			v.setNamedGraphs(query.getNamedGraphURIs());
			v.setStreamCatalog(streamCatalog);
			OpWalker.walk(op,v);
			SQLFormatter formatter = new SQLFormatter();		
			stmt = formatter.format(v.getSQL()).trim();
		} catch(IOException e) {
			e.printStackTrace();
		}
		return stmt;
	}

	public static void testQ3() {
		EPServiceProvider epService = EPServiceProviderManager.getProvider("engine_test");
		TrafficStream AarhusTrafficData182955 = new TrafficStream(epService,"AarhusTrafficData182955","182955","dataset/trafficMetaData.csv");
		AarhusTrafficData182955.setupSourceFile("streams/AarhusTrafficData182955.stream");
		TrafficStream AarhusTrafficData158505 = new TrafficStream(epService,"AarhusTrafficData158505","158505","dataset/trafficMetaData.csv");
		AarhusTrafficData158505.setupSourceFile("streams/AarhusTrafficData158505.stream");
		String stmt = getStatement("q3");
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
		ConfigurationDBRef dbConfig = new ConfigurationDBRef();
		dbConfig.setDriverManagerConnection("org.h2.Driver",
		                                    "jdbc:h2:./dataset/CityBench", 
		                                    "sa", 
		                                    "");

		Configuration engineConfig = new Configuration();
		engineConfig.addDatabaseReference("AarhusCulturalEvents", dbConfig);
		
		EPServiceProvider epService = EPServiceProviderManager.getProvider("engine_test",engineConfig);
		UserLocationStream UserLocationService = new UserLocationStream(epService,"UserLocationService");
		UserLocationService.setupSourceFile("streams/UserLocationService.stream");
		String stmt = getStatement("q4");
//		String stmt = null;
//		try {
//			stmt = FileUtils.readFileToString(new File("Queries/CityBench/q4.epl"));
//		}catch(Exception e) {
//			e.printStackTrace();
//		}
        EPStatement statement = epService.getEPAdministrator().createEPL(stmt);
        statement.addListener(new SimpleQueryListener());
        
        for(int i=0;i<100;i++) {
        	UserLocationService.sendEvent();
        }
        
        //shutdown
        UserLocationService.shutdown();
        epService.destroy();
	}
	
	public static void testQ5() {
		ConfigurationDBRef dbConfig = new ConfigurationDBRef();
		dbConfig.setDriverManagerConnection("org.h2.Driver",
		                                    "jdbc:h2:./dataset/CityBench", 
		                                    "sa", 
		                                    "");

		Configuration engineConfig = new Configuration();
		engineConfig.addDatabaseReference("AarhusCulturalEvents", dbConfig);
		engineConfig.addDatabaseReference("SensorRepository", dbConfig);
		
		EPServiceProvider epService = EPServiceProviderManager.getProvider("engine_test",engineConfig);
		TrafficStream AarhusTrafficData158505 = new TrafficStream(epService,"AarhusTrafficData158505","158505","dataset/trafficMetaData.csv");
		AarhusTrafficData158505.setupSourceFile("streams/AarhusTrafficData158505.stream");
//		String stmt = getStatement("q4");
		String stmt = null;
		try {
			stmt = FileUtils.readFileToString(new File("Queries/CityBench/q5.epl"));
		}catch(Exception e) {
			e.printStackTrace();
		}
        EPStatement statement = epService.getEPAdministrator().createEPL(stmt);
        statement.addListener(new SimpleQueryListener());
        
        for(int i=0;i<100;i++) {
        	AarhusTrafficData158505.sendEvent();
        }
        
        //shutdown
        AarhusTrafficData158505.shutdown();
        epService.destroy();
	}
}
