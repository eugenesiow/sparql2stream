/**
 * 
 */
package uk.ac.soton.ldanalytics.sparql2stream;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpWalker;
import org.junit.Test;

import uk.ac.soton.ldanalytics.sparql2sql.model.RdfTableMapping;
import uk.ac.soton.ldanalytics.sparql2sql.model.RdfTableMappingJena;
import uk.ac.soton.ldanalytics.sparql2sql.model.SparqlOpVisitor;
import uk.ac.soton.ldanalytics.sparql2sql.util.SQLFormatter;
import uk.ac.soton.ldanalytics.sparql2stream.parser.StreamQueryFactory;
import uk.ac.soton.ldanalytics.sparql2stream.util.StreamFormatUtil;

/**
 * @author eugene
 *
 */
public class CityBenchTest {

	@Test
	public void testQueries() {
		//load queries
		String queryPath = "Queries/CityBench/";
		String resultsPath = "Queries/CityBench/";
		
		RdfTableMapping mapping = new RdfTableMappingJena();
		mapping.loadMapping("mapping/smarthome_environment.nt");
		Map<String,String> streamCatalog = StreamFormatUtil.loadStreamCatalog("streams/catalogue.txt");
		Map<String,String> mappingCatalog = StreamFormatUtil.loadMappingCatalog("streams/mapping_catalogue.txt");
		
		for(int i=0;i<4;i++) {
			try {
				String queryStr = FileUtils.readFileToString(new File(queryPath + "q" + (i+1) + ".sparql"));
				String resultStr = FileUtils.readFileToString(new File(resultsPath + "q" + (i+1) + ".epl"));
				Query query = StreamQueryFactory.create(queryStr);
				Op op = Algebra.compile(query);
				
				SparqlOpVisitor v = new SparqlOpVisitor();
				v.setMappingCatalog(mappingCatalog,"jena");
				v.useMapping(mapping);
				v.setNamedGraphs(query.getNamedGraphURIs());
				v.setStreamCatalog(streamCatalog);
				OpWalker.walk(op,v);
				SQLFormatter formatter = new SQLFormatter();		
				assertEquals(resultStr.trim(),formatter.format(v.getSQL()).trim());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
