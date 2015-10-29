package uk.ac.soton.ldanalytics.sparql2stream;

import java.util.Map;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpWalker;

import uk.ac.soton.ldanalytics.sparql2sql.model.RdfTableMapping;
import uk.ac.soton.ldanalytics.sparql2sql.model.SparqlOpVisitor;
import uk.ac.soton.ldanalytics.sparql2sql.util.FormatUtil;
import uk.ac.soton.ldanalytics.sparql2sql.util.SQLFormatter;
import uk.ac.soton.ldanalytics.sparql2stream.parser.StreamQueryFactory;

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
		
		String queryStr = "PREFIX om-owl: <http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#>\n" + 
				"PREFIX weather: <http://knoesis.wright.edu/ssw/ont/weather.owl#>\n" + 
				"PREFIX wgs84_pos: <http://www.w3.org/2003/01/geo/wgs84_pos#>\n" + 
				"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" + 
				"PREFIX owl-time: <http://www.w3.org/2006/time#>\n" + 
				"\n" + 
				"SELECT ( MIN(?temperature) AS ?minTemperature ) ( MAX(?temperature) AS ?maxTemperature )\n" + 
				"    FROM NAMED STREAM <http://www.cwi.nl/SRBench/observations> [RANGE 1d TUMBLING]\n" + 
				"WHERE {\n" + 
				"  	?sensor om-owl:processLocation ?sensorLocation ;\n" + 
				"          om-owl:generatedObservation ?observation .\n" + 
				"        ?sensorLocation wgs84_pos:alt \"6300\"^^xsd:float ;\n" + 
				"                      wgs84_pos:lat \"32.892\"^^xsd:float ;\n" + 
				"                      wgs84_pos:long \"-116.4199\"^^xsd:float .\n" + 
				"  	?observation om-owl:observedProperty weather:_AirTemperature ;\n" + 
				"               om-owl:result [ om-owl:floatValue ?temperature ] .\n" + 
				"}\n" + 
				"GROUP BY ?sensor";
//		System.out.println(queryStr);
		
		RdfTableMapping mapping = new RdfTableMapping();
//		mapping.loadMapping("mapping/4UT01.nt");
//		mapping.loadMapping("/Users/eugene/Downloads/knoesis_observations_map_meta/4UT01.nt");
		mapping.loadMapping("/Users/eugene/Downloads/knoesis_observations_ike_map_meta/HP001.nt");
//		mapping.loadMapping("mapping/smarthome_environment.nt");
//		mapping.loadMapping("mapping/smarthome_sensors.nt");
//		mapping.loadMapping("mapping/smarthome_meter.nt");
//		mapping.loadMapping("mapping/smarthome_motion.nt");
		
//		System.out.println(queryStr);
		
		Map<String,String> streamCatalog = FormatUtil.loadStreamCatalog("streams/catalogue.txt");
		
		long startTime = System.currentTimeMillis();
		Query query = StreamQueryFactory.create(queryStr);
		Op op = Algebra.compile(query);
		System.out.println(op);
		
		SparqlOpVisitor v = new SparqlOpVisitor();
		v.useMapping(mapping);
		v.setNamedGraphs(query.getNamedGraphURIs());
		v.setStreamCatalog(streamCatalog);
		OpWalker.walk(op,v);
		SQLFormatter formatter = new SQLFormatter();
		
		System.out.println(System.currentTimeMillis() - startTime);
		
		System.out.println(formatter.format(v.getSQL()));
		
	}
	
}
