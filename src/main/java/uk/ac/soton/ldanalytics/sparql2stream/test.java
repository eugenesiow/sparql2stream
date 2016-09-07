package uk.ac.soton.ldanalytics.sparql2stream;

import java.util.Map;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpWalker;

import uk.ac.soton.ldanalytics.sparql2sql.model.RdfTableMapping;
import uk.ac.soton.ldanalytics.sparql2sql.model.RdfTableMappingJena;
import uk.ac.soton.ldanalytics.sparql2sql.model.SparqlOpVisitor;
import uk.ac.soton.ldanalytics.sparql2sql.util.SQLFormatter;
import uk.ac.soton.ldanalytics.sparql2stream.parser.StreamQueryFactory;
import uk.ac.soton.ldanalytics.sparql2stream.util.StreamFormatUtil;

public class test {
	public static void main(String[] args) {
//		String queryStr = "PREFIX om-owl: <http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#>\n" + 
//				"PREFIX weather: <http://knoesis.wright.edu/ssw/ont/weather.owl#>\n" + 
//				"\n" + 
//				"SELECT DISTINCT ?sensor ?value ?uom\n" + 
//				"FROM NAMED STREAM <http://www.cwi.nl/SRBench/observations> [RANGE 1h STEP 10m]\n" +
////				"FROM NAMED STREAM <http://www.cwi.nl/SRBench/observations>\n" +
//				"WHERE {\n" + 
//				"  ?observation om-owl:procedure ?sensor ;\n" + 
//				"               a weather:RainfallObservation ;\n" + 
//				"               om-owl:result ?result .\n" + 
//				"  ?result om-owl:floatValue ?value ;\n" + 
//				"          om-owl:uom ?uom .\n" + 
//				"}";
		
//		String queryStr = "PREFIX om-owl: <http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#>\n" + 
//				"PREFIX weather: <http://knoesis.wright.edu/ssw/ont/weather.owl#>\n" + 
//				"PREFIX wgs84_pos: <http://www.w3.org/2003/01/geo/wgs84_pos#>\n" + 
//				"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" + 
//				"PREFIX owl-time: <http://www.w3.org/2006/time#>\n" + 
//				"\n" + 
//				"SELECT ( MIN(?temperature) AS ?minTemperature ) ( MAX(?temperature) AS ?maxTemperature )\n" + 
//				"    FROM NAMED STREAM <http://www.cwi.nl/SRBench/observations> [RANGE 1d TUMBLING]\n" + 
//				"WHERE {\n" + 
//				"  	?sensor om-owl:processLocation ?sensorLocation ;\n" + 
//				"          om-owl:generatedObservation ?observation .\n" + 
//				"        ?sensorLocation wgs84_pos:alt \"6300\"^^xsd:float ;\n" + 
//				"                      wgs84_pos:lat \"32.892\"^^xsd:float ;\n" + 
//				"                      wgs84_pos:long \"-116.4199\"^^xsd:float .\n" + 
//				"  	?observation om-owl:observedProperty weather:_AirTemperature ;\n" + 
//				"               om-owl:result [ om-owl:floatValue ?temperature ] .\n" + 
//				"}\n" + 
//				"GROUP BY ?sensor";
		
//		String queryStr = "PREFIX  ssn:  <http://purl.oclc.org/NET/ssnx/ssn#>\n" + 
//				"PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
//				"PREFIX  iot:  <http://purl.oclc.org/NET/iot#>\n" + 
//				"\n" + 
//				"SELECT (?sensor as ?roomName) (sum(?motionOrNoMotion) as ?totalMotion)\n" + 
//				"FROM NAMED STREAM <http://iot.soton.ac.uk/smarthome/motion> [RANGE 10s TUMBLING]\n" + 
//				"WHERE {\n" + 
//				"    ?obsval a iot:MotionValue;\n" + 
//				"      iot:hasQuantityValue ?motionOrNoMotion.\n" + 
//				"    ?snout ssn:hasValue ?obsval.\n" + 
//				"    ?obs ssn:observationResult ?snout.\n" + 
//				"    ?obs ssn:observedBy ?sensor.\n" + 
//				"} GROUP BY ?sensor\n" + 
//				"HAVING ( sum(?motionOrNoMotion) > 0 )";
		
//		String queryStr = "PREFIX  ssn:  <http://purl.oclc.org/NET/ssnx/ssn#>\n" + 
//				"PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
//				"PREFIX  iot:  <http://purl.oclc.org/NET/iot#>\n" + 
//				"\n" + 
//				"SELECT (?meter as ?meterName) (avg(?meterval) as ?averagePower)\n" + 
//				"FROM NAMED STREAM <http://iot.soton.ac.uk/smarthome/meter> [RANGE 30s STEP]\n" + 
//				"WHERE {\n" + 
//				"	?meterobs ssn:observationResult ?metersnout.\n" + 
//				"    ?metersnout ssn:hasValue ?meterobsval.\n" + 
//				"    ?meterobsval a iot:EnergyValue; \n" + 
//				"    	iot:hasQuantityValue ?meterval.\n" + 
//				"    ?meterobs ssn:observedBy ?meter.\n" + 
//				"} GROUP BY ?meter\n" + 
//				"HAVING ( avg(?meterval) > 0 )";
		
//		String queryStr = "PREFIX  ssn:  <http://purl.oclc.org/NET/ssnx/ssn#>\n" + 
//				"PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
//				"PREFIX  iot:  <http://purl.oclc.org/NET/iot#>\n" + 
//				"PREFIX iotsn: <http://iot.soton.ac.uk/smarthome/sensor#>\n" + 
//				"\n" + 
//				"SELECT ?currentTemp ?currentHumidity ?currentWindSpeed ?currentWindGust ?currentWindDirection\n" + 
//				"FROM NAMED STREAM <http://iot.soton.ac.uk/smarthome/environment> [LAST]\n" + 
//				"WHERE {\n" + 
//				"	?obs ssn:observedBy iotsn:environmental1;\n" + 
//				"		ssn:observationResult ?sntemp;\n" + 
//				"		ssn:observationResult ?snhumidity;\n" + 
//				"		ssn:observationResult ?snwindspeed;\n" + 
//				"		ssn:observationResult ?snwindgust;\n" + 
//				"		ssn:observationResult ?snwinddir.\n" + 
//				"	\n" + 
//				"	?sntemp ssn:hasValue ?valtemp.\n" + 
//				"	?snhumidity ssn:hasValue ?valhumidity.\n" + 
//				"	?snwindspeed ssn:hasValue ?valwindspeed.\n" + 
//				"	?snwindgust ssn:hasValue ?valwindgust.\n" + 
//				"	?snwinddir ssn:hasValue ?valwinddir.\n" + 
//				"	\n" + 
//				"	?valtemp a iot:InternalTemperatureValue;\n" + 
//				"		iot:hasQuantityValue ?currentTemp.\n" + 
//				"	?valhumidity a iot:InternalHumidityValue;\n" + 
//				"		iot:hasQuantityValue ?currentHumidity.\n" + 
//				"	?valwindspeed a iot:WindSpeedValue;\n" + 
//				"		iot:hasQuantityValue ?currentWindSpeed.\n" + 
//				"	?valwindgust a iot:WindGustValue;\n" + 
//				"		iot:hasQuantityValue ?currentWindGust.\n" + 
//				"	?valwinddir a iot:WindGustDirectionValue;\n" + 
//				"		iot:hasQuantityValue ?currentWindDirection.\n" + 
//				"}";
		
//		String queryStr = "PREFIX om-owl: <http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#>\n" + 
//        		"PREFIX weather: <http://knoesis.wright.edu/ssw/ont/weather.owl#>\n" + 
//        		"PREFIX wgs84_pos: <http://www.w3.org/2003/01/geo/wgs84_pos#>\n" + 
//        		"PREFIX owl-time: <http://www.w3.org/2006/time#>\n" + 
//        		"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" + 
//        		"\n" + 
//        		"SELECT DISTINCT ?sensor ?value ?uom ?lat ?lon\n" + 
//        		"FROM NAMED STREAM <http://www.cwi.nl/SRBench/observations> [RANGE 1h STEP]\n" +
//        		"FROM NAMED <http://www.cwi.nl/SRBench/sdsd>\n" +
//        		"WHERE {\n" + 
//        		"  	?observation om-owl:procedure ?sensor ;\n" + 
//        		"               a weather:RainfallObservation ;\n" + 
//        		"               om-owl:result ?result.\n"+
//        		"	?sensor om-owl:processLocation ?sensorLocation.	\n" +
//        		"	?sensorLocation wgs84_pos:lat ?lat;	\n" +
//        		"		wgs84_pos:long ?lon.	\n" +
//        		"  	?result om-owl:floatValue ?value ;\n" + 
//        		"          om-owl:uom ?uom .\n" + 
//        		"}";
		
//		String queryStr = "PREFIX om-owl: <http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#>\n" + 
//				"PREFIX weather: <http://knoesis.wright.edu/ssw/ont/weather.owl#>\n" + 
//				"PREFIX wgs84_pos: <http://www.w3.org/2003/01/geo/wgs84_pos#>\n" + 
//				"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" + 
//				"PREFIX owl-time: <http://www.w3.org/2006/time#>\n" + 
//				"\n" + 
//				"SELECT DISTINCT ?lat ?long ?alt\n" + 
//				"FROM NAMED STREAM <http://www.cwi.nl/SRBench/observations> [RANGE 1d STEP]\n" + 
//				"WHERE {\n" + 
//				"  ?sensor om-owl:generatedObservation [a weather:SnowfallObservation ] .\n" + 
//				"  ?sensor om-owl:processLocation ?sensorLocation .\n" + 
//				"  ?sensorLocation wgs84_pos:alt ?alt ;\n" + 
//				"                  wgs84_pos:lat ?lat ;\n" + 
//				"                  wgs84_pos:long ?long .\n" + 
//				"}";
//		String queryStr = "PREFIX om-owl: <http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#>\n" + 
//				"PREFIX weather: <http://knoesis.wright.edu/ssw/ont/weather.owl#>\n" + 
//				"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" + 
//				"PREFIX owl-time: <http://www.w3.org/2006/time#>\n" + 
//				"\n" + 
//				"SELECT ?sensor\n" + 
//				"FROM NAMED STREAM <http://www.cwi.nl/SRBench/observations> [RANGE 1h STEP]\n" + 
//				"WHERE {\n" + 
//				"  ?sensor om-owl:generatedObservation [a weather:SnowfallObservation ] ;\n" + 
//				"          om-owl:generatedObservation ?o1 ;\n" + 
//				"          om-owl:generatedObservation ?o2 .\n" + 
//				"  ?o1 a weather:TemperatureObservation ;\n" + 
//				"      om-owl:observedProperty weather:_AirTemperature ;\n" + 
//				"      om-owl:result [om-owl:floatValue ?temperature] .\n" + 
//				"  ?o2 a weather:WindSpeedObservation ;\n" + 
//				"      om-owl:observedProperty weather:_WindSpeed ;\n" + 
//				"      om-owl:result [om-owl:floatValue ?windSpeed] .\n" + 
//				"} GROUP BY ?sensor \n" + 
//				"HAVING ( AVG(?temperature) < \"32\"^^xsd:float  &&  MIN(?windSpeed) > \"40.0\"^^xsd:float ) ";
		//PT10M
//		String queryStr = "PREFIX om-owl: <http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#>\n" + 
//				"PREFIX weather: <http://knoesis.wright.edu/ssw/ont/weather.owl#>\n" + 
//				"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" + 
//				"PREFIX owl-time: <http://www.w3.org/2006/time#>\n" + 
//				"\n" + 
//				"SELECT ?sensor\n" + 
//				"FROM NAMED WINDOW :win ON <http://www.cwi.nl/SRBench/observations> [RANGE 1h]\n" + 
//				"WHERE {\n" + 
//				"  WINDOW :win { ?sensor om-owl:generatedObservation [a weather:SnowfallObservation ] ;\n" + 
//				"          om-owl:generatedObservation ?o1 ;\n" + 
//				"          om-owl:generatedObservation ?o2 .\n" + 
//				"  ?o1 a weather:TemperatureObservation ;\n" + 
//				"      om-owl:observedProperty weather:_AirTemperature ;\n" + 
//				"      om-owl:result [om-owl:floatValue ?temperature] .\n" + 
//				"  ?o2 a weather:WindSpeedObservation ;\n" + 
//				"      om-owl:observedProperty weather:_WindSpeed ;\n" + 
//				"      om-owl:result [om-owl:floatValue ?windSpeed] . }\n" + 
//				"} GROUP BY ?sensor \n" + 
//				"HAVING ( AVG(?temperature) < \"32\"^^xsd:float  &&  MIN(?windSpeed) > \"40.0\"^^xsd:float ) ";
//		String queryStr = "PREFIX sao: <http://purl.oclc.org/NET/sao/>\n" + 
//				"PREFIX ssn: <http://purl.oclc.org/NET/ssnx/ssn#>\n" + 
//				"PREFIX ct: 	<http://www.insight-centre.org/citytraffic#>\n" + 
//				"PREFIX ns: 	<http://www.insight-centre.org/dataset/SampleEventService#>\n" + 
//				"\n" + 
//				"SELECT ?v1 ?v2\n" + 
//				"FROM NAMED WINDOW :traffic1 ON <http://www.insight-centre.org/dataset/SampleEventService#AarhusTrafficData182955> [RANGE 3s]\n" + 
//				"FROM NAMED WINDOW :traffic2 ON <http://www.insight-centre.org/dataset/SampleEventService#AarhusTrafficData158505> [RANGE 3s]\n" + 
//				"WHERE {\n" + 
//				"	WINDOW :traffic1 {\n" + 
//				"		?obId1 a ssn:Observation;\n" + 
//				"			ssn:observedProperty ?p1;\n" + 
//				"			sao:hasValue ?v1;\n" + 
//				"			ssn:observedBy ns:AarhusTrafficData182955.\n" + 
//				"		?p1 a ct:CongestionLevel.\n" + 
//				"	}\n" + 
//				"	WINDOW :traffic2 {\n" + 
//				"		?obId2 a ssn:Observation;\n" + 
//				"			ssn:observedProperty ?p2;\n" + 
//				"			sao:hasValue ?v2;\n" + 
//				"			ssn:observedBy ns:AarhusTrafficData158505.\n" + 
//				"		?p2 a ct:CongestionLevel.\n" + 
//				"	}\n" + 
//				"}";
		String queryStr = "PREFIX sao: <http://purl.oclc.org/NET/sao/>\n" + 
				"PREFIX ssn: <http://purl.oclc.org/NET/ssnx/ssn#>\n" + 
				"PREFIX ct: 	<http://www.insight-centre.org/citytraffic#>\n" + 
				"PREFIX ns: 	<http://www.insight-centre.org/dataset/SampleEventService#>\n" + 
				"\n" + 
				"SELECT ?title ?lat1 ?lon1 ?lat2 ?lon2\n" + 
				"FROM NAMED WINDOW :traffic ON <http://www.insight-centre.org/dataset/SampleEventService#AarhusTrafficData158505> [RANGE 3s]\n" + 
				"FROM NAMED <http://www.insight-centre.org/dataset/SampleEventService#AarhusCulturalEvents>\n" + 
				"WHERE {\n" + 
				"	?p2 a ct:CongestionLevel;\n" + 
				"		ssn:isPropertyOf ?foi2.\n" + 
				"	?foi2 ct:hasStartLatitude ?lat2;\n" + 
				"		ct:hasStartLongitude ?lon2.\n" + 
				"	GRAPH ns:AarhusCulturalEvents {\n" + 
				"		?evtId a ssn:Observation;\n" + 
				"			sao:value ?title;\n" + 
				"			ssn:featureOfInterest ?foi.\n" + 
				"		?foi ct:hasFirstNode ?node.\n" + 
				"		?node ct:hasLatitude ?lat1;\n" + 
				"			ct:hasLongitude ?lon1.\n" + 
				"	}\n" + 
				"	WINDOW :traffic {\n" + 
				"		?obId2 a ssn:Observation;\n" + 
				"			ssn:observedProperty ?p2;\n" + 
				"			sao:hasValue ?v2;\n" + 
				"			ssn:observedBy ns:AarhusTrafficData158505.\n" + 
				"	}\n" + 
				"	FILTER (((?lat2-?lat1)*(?lat2-?lat1)+(?lon2-?lon1)*(?lon2-?lon1))<0.1)\n" + 
				"}";
//		String queryStr = "PREFIX ssn: <http://purl.oclc.org/NET/ssnx/ssn#>\n" + 
//				"PREFIX iotsn: <http://iot.soton.ac.uk/smarthome/sensor#>\n" + 
//				"PREFIX iot: <http://purl.oclc.org/NET/iot#>\n" + 
//				"\n" + 
//				"SELECT ?platform (sum(?power) as ?totalpower)\n" + 
//				"FROM NAMED STREAM <http://iot.soton.ac.uk/smarthome/meter> [RANGE 1h STEP]\n" + 
//				"WHERE\n" + 
//				"{\n" + 
//				"    {\n" + 
//				"      SELECT  ?platform (avg(?meterval) as ?power)\n" + 
//				"      WHERE\n" + 
//				"      {\n" + 
//				"        ?meter ssn:onPlatform ?platform.\n" + 
//				"        ?meterobs ssn:observedBy ?meter;\n" + 
//				"          ssn:observationResult ?metersnout.\n" + 
//				"        ?metersnout ssn:hasValue ?meterobsval.\n" + 
//				"        ?meterobsval a iot:EnergyValue.\n" + 
//				"        ?meterobsval iot:hasQuantityValue ?meterval.\n" + 
//				"        FILTER(?meterval > 0)\n" + 
//				"      } GROUP BY ?platform ?meter\n" + 
//				"    }\n" + 
//				"} GROUP BY ?platform";
		
//		System.out.println(queryStr);
		
		RdfTableMapping mapping = new RdfTableMappingJena();
//		mapping.loadMapping("mapping/4UT01.nt");
//		mapping.loadMapping("/Users/eugene/Downloads/knoesis_observations_map_meta/4UT01.nt");
//		mapping.loadMapping("/Users/eugene/Downloads/knoesis_observations_ike_map_meta/HP001.nt");
//		mapping.loadMapping("/Users/eugene/Downloads/knoesis_observations_ike_map_snow/ALPHA.nt");
		mapping.loadMapping("mapping/SensorRepository.nt");
//		mapping.loadMapping("mapping/smarthome_sensors.nt");
//		mapping.loadMapping("mapping/smarthome_meter.nt");
//		mapping.loadMapping("mapping/smarthome_motion.nt");
		
//		System.out.println(queryStr);
		
		Map<String,String> streamCatalog = StreamFormatUtil.loadStreamCatalog("streams/catalogue.txt");
		Map<String,String> mappingCatalog = StreamFormatUtil.loadMappingCatalog("streams/mapping_catalogue.txt");
		
		long startTime = System.currentTimeMillis();
		Query query = StreamQueryFactory.create(queryStr);
		Op op = Algebra.compile(query);
		System.out.println(op);
		
		SparqlOpVisitor v = new SparqlOpVisitor();
		v.setMappingCatalog(mappingCatalog,"jena");
		v.useMapping(mapping);
		v.setNamedGraphs(query.getNamedGraphURIs());
		v.setStreamCatalog(streamCatalog);
		OpWalker.walk(op,v);
		SQLFormatter formatter = new SQLFormatter();
		
		System.out.println(System.currentTimeMillis() - startTime);
		
		System.out.println(formatter.format(v.getSQL()));
		
	}
	
}
