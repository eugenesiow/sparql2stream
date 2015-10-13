package uk.ac.soton.ldanalytics.sparql2stream.parser;

import org.apache.jena.riot.system.IRIResolver;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import uk.ac.soton.ldanalytics.sparql2stream.parser.lang.ParserSPARQL11;

public class StreamQueryFactory extends QueryFactory {
   
	/** Create a SPARQL query from the given string.
    *
    * @param queryString      The query string
    * @throws QueryException  Thrown when a parse error occurs
    */
   
   static public Query create(String queryString)
   {
       return create(queryString, Syntax.defaultQuerySyntax) ;
   }

   /** Create a query from the given string with the 
    *
    * @param queryString      The query string
    * @param syntax           {@link Syntax}
    * @throws QueryException  Thrown when a parse error occurs
    */
   
   static public Query create(String queryString, Syntax syntax)
   {
       return create(queryString, null, syntax) ;
   }
   
   /** Create a query from the given string by calling the parser.
   *
   * @param queryString      The query string
   * @param baseURI          Base URI
   * @param syntax           {@link Syntax}
   * @throws QueryException  Thrown when a parse error occurs
   */
  
  static public Query create(String queryString, String baseURI, Syntax syntax)
  {
      Query query = new Query() ;
      return parse(query, queryString, baseURI, syntax) ;
  }
  
   static public Query parse(Query query, String queryString, String baseURI, Syntax syntaxURI)
   {
       if ( syntaxURI == null )
           syntaxURI = query.getSyntax() ;
       else
           query.setSyntax(syntaxURI) ;

//       SPARQLParser parser = SPARQLParser.createParser(syntaxURI) ;
       
       ParserSPARQL11 parser = new ParserSPARQL11();
       
//       if ( parser == null )
//           throw new UnsupportedOperationException("Unrecognized syntax for parsing: "+syntaxURI) ;
       
       if ( query.getResolver() == null )
       {
           IRIResolver resolver = null ;
           try { 
               if ( baseURI != null ) { 
                   // Sort out the baseURI - if that fails, dump in a dummy one and continue.
                   resolver = IRIResolver.create(baseURI) ; 
               }
               else { 
                   resolver = IRIResolver.create() ;
               }
           }
           catch (Exception ex) {}
           if ( resolver == null )   
               resolver = IRIResolver.create("http://localhost/query/defaultBase#") ;
           query.setResolver(resolver) ;
           
       }
       return parser.parse(query, queryString) ;
   }
}
