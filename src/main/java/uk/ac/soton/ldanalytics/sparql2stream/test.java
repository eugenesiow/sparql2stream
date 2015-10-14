package uk.ac.soton.ldanalytics.sparql2stream;

import uk.ac.soton.ldanalytics.sparql2sql.model.RdfTableMapping;
import uk.ac.soton.ldanalytics.sparql2sql.model.SparqlOpVisitor;
import uk.ac.soton.ldanalytics.sparql2stream.parser.StreamQueryFactory;
import uk.ac.soton.ldanalytics.sparql2sql.util.SQLFormatter;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpWalker;

public class test {
	public static void main(String[] args) {
		String queryStr = "PREFIX om-owl: <http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#>\n" + 
				"PREFIX weather: <http://knoesis.wright.edu/ssw/ont/weather.owl#>\n" + 
				"\n" + 
				"SELECT DISTINCT ?sensor ?value ?uom\n" + 
				"FROM NAMED STREAM <http://www.cwi.nl/SRBench/observations> [RANGE 1h TUMBLING]\n" +
//				"FROM NAMED STREAM <http://www.cwi.nl/SRBench/observations>\n" +
				"WHERE {\n" + 
				"  ?observation om-owl:procedure ?sensor ;\n" + 
				"               a weather:RainfallObservation ;\n" + 
				"               om-owl:result ?result .\n" + 
				"  ?result om-owl:floatValue ?value ;\n" + 
				"          om-owl:uom ?uom .\n" + 
				"}";
		
		RdfTableMapping mapping = new RdfTableMapping();
//		mapping.loadMapping("mapping/4UT01.nt");
		mapping.loadMapping("/Users/eugene/Downloads/knoesis_observations_map_meta/4UT01.nt");
//		mapping.loadMapping("mapping/smarthome_environment.nt");
//		mapping.loadMapping("mapping/smarthome_sensors.nt");
//		mapping.loadMapping("mapping/smarthome_meter.nt");
//		mapping.loadMapping("mapping/smarthome_motion.nt");
		
		
//		System.out.println(queryStr);
		
		long startTime = System.currentTimeMillis();
		Query query = StreamQueryFactory.create(queryStr);
		System.out.println(query.getNamedGraphURIs());
		Op op = Algebra.compile(query);
		System.out.println(op);
		
		SparqlOpVisitor v = new SparqlOpVisitor();
		v.useMapping(mapping);
		OpWalker.walk(op,v);
		SQLFormatter formatter = new SQLFormatter();
		
		System.out.println(System.currentTimeMillis() - startTime);
		
		System.out.println(formatter.format(v.getSQL()));
		
	}
	
}
