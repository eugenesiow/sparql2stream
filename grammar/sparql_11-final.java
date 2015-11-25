/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

options
{
    JAVA_UNICODE_ESCAPE = true ;
    UNICODE_INPUT = false ;
  STATIC = false ;
}
PARSER_BEGIN(SPARQLParser11)
package uk.ac.soton.ldanalytics.sparql2stream.parser.lang;
import org.apache.jena.graph.* ;
import org.apache.jena.query.* ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.syntax.* ;
import org.apache.jena.sparql.expr.* ;
import org.apache.jena.sparql.path.* ;
import org.apache.jena.sparql.expr.aggregate.* ;
import org.apache.jena.update.* ;
import org.apache.jena.sparql.modify.request.* ;
@SuppressWarnings("all")
public class SPARQLParser11 extends SPARQLParser11Base
{
    boolean allowAggregatesInExpressions = false ;
}
PARSER_END(SPARQLParser11)
void QueryUnit(): { }
{
  ByteOrderMark()
  { startQuery() ; }
  Query() <EOF>
  { finishQuery() ; }
}
void Query() : { }
{
  Prologue()
  ( SelectQuery() | ConstructQuery() | DescribeQuery() | AskQuery()
  )
  ValuesClause()
}
void UpdateUnit() : {}
{
  ByteOrderMark()
  { startUpdateRequest() ; }
  Update()
  <EOF>
  { finishUpdateRequest() ; }
}
void ByteOrderMark() : {}
{
   (<BOM>)?
}
void Prologue() : {}
{
  ( BaseDecl() | PrefixDecl() )*
}
void BaseDecl() : { String iri ; }
{
  <BASE> iri = IRIREF()
  { getPrologue().setBaseURI(iri) ; }
}
void PrefixDecl() : { Token t ; String iri ; }
{
    <PREFIX> t = <PNAME_NS> iri = IRIREF()
      { String s = fixupPrefix(t.image, t.beginLine, t.beginColumn) ;
        getPrologue().setPrefix(s, iri) ; }
}
void SelectQuery() : { }
{
  SelectClause()
  ( DatasetClause() )*
  WhereClause()
  SolutionModifier()
}
void SubSelect() :{ }
{
  SelectClause()
  WhereClause()
  SolutionModifier()
  ValuesClause()
}
void SelectClause() : { Var v ; Expr expr ; Node n ; }
{
  <SELECT>
    { getQuery().setQuerySelectType() ; }
  ( <DISTINCT> { getQuery().setDistinct(true);}
  | <REDUCED> { getQuery().setReduced(true); }
  )?
  { allowAggregatesInExpressions = true ; }
  (
    (
      v = Var() { getQuery().addResultVar(v) ; }
    |
      (
        { v = null ; }
        <LPAREN>
        expr = Expression()
        <AS> v = Var()
        <RPAREN>
        { getQuery().addResultVar(v, expr) ; }
      )
      { getQuery().setQueryResultStar(false) ; }
    )+
  |
    <STAR> { getQuery().setQueryResultStar(true) ; }
  )
  { allowAggregatesInExpressions = false ; }
}
void ConstructQuery() : { Template t ;
                          TripleCollectorBGP acc = new TripleCollectorBGP() ; }
{
 <CONSTRUCT>
   { getQuery().setQueryConstructType() ; }
 (
    t = ConstructTemplate()
      { getQuery().setConstructTemplate(t) ; }
    ( DatasetClause() )*
    WhereClause()
    SolutionModifier()
 |
    ( DatasetClause() )*
    <WHERE>
    <LBRACE>
    (TriplesTemplate(acc))?
    <RBRACE>
    SolutionModifier()
    {
      t = new Template(acc.getBGP()) ;
      getQuery().setConstructTemplate(t) ;
      ElementPathBlock epb = new ElementPathBlock(acc.getBGP()) ;
      ElementGroup elg = new ElementGroup() ;
      elg.addElement(epb) ;
      getQuery().setQueryPattern(elg) ;
    }
 )
}
void DescribeQuery() : { Node n ; }
{
  <DESCRIBE>
    { getQuery().setQueryDescribeType() ; }
  (
    ( n = VarOrIri() { getQuery().addDescribeNode(n) ; } )+
    { getQuery().setQueryResultStar(false) ; }
  |
    <STAR>
    { getQuery().setQueryResultStar(true) ; }
  )
  ( DatasetClause() )*
  ( WhereClause() )?
  SolutionModifier()
}
void AskQuery() : {}
{
  <ASK> { getQuery().setQueryAskType() ; }
  ( DatasetClause() )*
  WhereClause()
  SolutionModifier()
}
void DatasetClause() : {}
{
  <FROM>
  ( DefaultGraphClause() | NamedClause() )
}
void DefaultGraphClause() : { String iri ; }
{
  iri = SourceSelector()
  {
    getQuery().addGraphURI(iri) ;
  }
}
void NamedClause() : {}
{
  <NAMED>
  ( NamedGraphClause() | StreamClause() )
}
void NamedGraphClause() : { String iri ; }
{
  iri = SourceSelector()
  {
    getQuery().addNamedGraphURI(iri) ;
  }
}
void StreamClause() : { String iri ; }
{
  <STREAM>
  iri = SourceSelector()
  StreamParamsClause(iri)
}
void StreamParamsClause(String iri) : {} 
{
	<LBRACKET>
	(<LAST> {
		getQuery().addNamedGraphURI(iri+";LAST") ;
	} | <RANGE> { RangeClause(iri); })
	<RBRACKET>
}
void RangeClause(String iri) : { Token i; Token t; } 
{
	i = <INTEGER>
	t = <TIMEUNIT>
	{
		iri = iri+";"+i.image+";"+t.image;
	}
	(<TUMBLING> {
		getQuery().addNamedGraphURI(iri+";TUMBLING") ;
	}|<STEP> {
		getQuery().addNamedGraphURI(iri+";STEP") ;
	})
}
String SourceSelector() : { String iri ; }
{
  iri = iri() { return iri ; }
}
void WhereClause() : { Element el ; }
{
   (<WHERE>)?
   { startWherePattern() ; }
   el = GroupGraphPattern() { getQuery().setQueryPattern(el) ; }
   { finishWherePattern() ; }
}
void SolutionModifier() : { }
{
  ( GroupClause() )?
  ( HavingClause() )?
  ( OrderClause() )?
  ( LimitOffsetClauses() )?
}
void GroupClause() : { }
{
  <GROUP> <BY> ( GroupCondition() )+
}
void GroupCondition() : { Var v = null ; Expr expr = null ; }
{
  ( expr = BuiltInCall() { getQuery().addGroupBy((Var)null, expr) ; }
  | expr = FunctionCall() { getQuery().addGroupBy((Var)null, expr) ; }
  |
    <LPAREN>
      expr = Expression()
    ( <AS> v = Var() )?
    <RPAREN>
    { getQuery().addGroupBy(v ,expr) ; }
  | v = Var()
    { getQuery().addGroupBy(v) ; }
  )
}
void HavingClause() : { }
{
    { allowAggregatesInExpressions = true ; }
    <HAVING> (HavingCondition())+
    { allowAggregatesInExpressions = false ; }
}
void HavingCondition() : { Expr c ; }
{
  c = Constraint()
  { getQuery().addHavingCondition(c) ; }
}
void OrderClause() : { }
{
  { allowAggregatesInExpressions = true ; }
  <ORDER> <BY> ( OrderCondition() )+
  { allowAggregatesInExpressions = false ; }
}
void OrderCondition() :
{ int direction = 0 ; Expr expr = null ; Node v = null ; }
{
  { direction = Query.ORDER_DEFAULT ; }
  (
    (
      ( <ASC> { direction = Query.ORDER_ASCENDING ; }
      | <DESC> { direction = Query.ORDER_DESCENDING ; } )
      expr = BrackettedExpression()
    )
  |
    ( expr = Constraint()
    | v = Var()
    )
  )
  { if ( v == null )
          getQuery().addOrderBy(expr, direction) ;
      else
          getQuery().addOrderBy(v, direction) ; }
}
void LimitOffsetClauses() : { }
{
  (
    LimitClause() (OffsetClause())?
  |
    OffsetClause() (LimitClause())?
  )
}
void LimitClause() : { Token t ; }
{
  <LIMIT> t = <INTEGER>
    { getQuery().setLimit(integerValue(t.image)) ; }
}
void OffsetClause() : { Token t ; }
{
  <OFFSET> t = <INTEGER>
    { getQuery().setOffset(integerValue(t.image)) ; }
}
void ValuesClause() : { Token t ; }
{
  (
    t = <VALUES>
    { startValuesClause(t.beginLine, t.beginColumn) ; }
    DataBlock()
    { finishValuesClause(t.beginLine, t.beginColumn) ; }
  )?
}
void Update() : { }
{
   Prologue()
   (Update1() ( <SEMICOLON> Update() )? )?
}
void Update1() : { Update up = null ; }
{
  { startUpdateOperation() ; }
  ( up = Load()
  | up = Clear()
  | up = Drop()
  | up = Add()
  | up = Move()
  | up = Copy()
  | up = Create()
  | up = DeleteWhere()
  | up = Modify()
  | InsertData()
  | DeleteData()
  )
  {
    if (null != up) emitUpdate(up) ;
    finishUpdateOperation() ;
  }
}
Update Load() : { String url ; Node dest = null ; boolean silent = false ; }
{
    <LOAD> (<SILENT> { silent = true ; })? url = iri()
    (<INTO> dest = GraphRef() )?
    { return new UpdateLoad(url, dest, silent) ; }
}
Update Clear() : { boolean silent = false ; Target target ; }
{
   <CLEAR> (<SILENT> { silent = true ; })? target = GraphRefAll()
   { return new UpdateClear(target, silent) ; }
}
Update Drop() : { boolean silent = false ; Target target ; }
{
   <DROP> (<SILENT> { silent = true ; })? target = GraphRefAll()
   { return new UpdateDrop(target, silent) ; }
}
Update Create() : { Node iri ; boolean silent = false ; }
{
   <CREATE> (<SILENT> { silent=true ; } )? iri = GraphRef()
   { return new UpdateCreate(iri, silent) ; }
}
Update Add() : { Target src ; Target dest ; boolean silent = false ; }
{
  <ADD> (<SILENT> { silent=true ; } )? src = GraphOrDefault() <TO> dest = GraphOrDefault()
  { return new UpdateAdd(src, dest, silent) ; }
}
Update Move() : { Target src ; Target dest ; boolean silent = false ; }
{
  <MOVE> (<SILENT> { silent=true ; } )? src = GraphOrDefault() <TO> dest = GraphOrDefault()
  { return new UpdateMove(src, dest, silent) ; }
}
Update Copy() : { Target src ; Target dest ; boolean silent = false ; }
{
  <COPY> (<SILENT> { silent=true ; } )? src = GraphOrDefault() <TO> dest = GraphOrDefault()
  { return new UpdateCopy(src, dest, silent) ; }
}
void InsertData() : { QuadDataAccSink qd = createInsertDataSink() ; Token t ; }
{
  t = <INSERT_DATA>
  { int beginLine = t.beginLine; int beginColumn = t.beginColumn; t = null; }
  { startDataInsert(qd, beginLine, beginColumn) ; }
   QuadData(qd)
  {
    finishDataInsert(qd, beginLine, beginColumn) ;
    qd.close() ;
  }
}
void DeleteData() : { QuadDataAccSink qd = createDeleteDataSink() ; Token t ; }
{
  t = <DELETE_DATA>
  { int beginLine = t.beginLine; int beginColumn = t.beginColumn; t = null; }
  { startDataDelete(qd, beginLine, beginColumn) ; }
  QuadData(qd)
  {
    finishDataDelete(qd, beginLine, beginColumn) ;
    qd.close() ;
  }
}
Update DeleteWhere() : { QuadAcc qp = new QuadAcc() ; Token t ; }
{
  t = <DELETE_WHERE>
  { int beginLine = t.beginLine; int beginColumn = t.beginColumn; t = null; }
  { startDeleteTemplate(qp, beginLine, beginColumn) ; }
  QuadPattern(qp)
  { finishDeleteTemplate(qp, beginLine, beginColumn) ; }
  { return new UpdateDeleteWhere(qp) ; }
}
Update Modify() : { Element el ; String iri = null ;
                    UpdateModify up = new UpdateModify() ; }
{
  { startModifyUpdate() ; }
  ( <WITH> iri = iri() { Node n = createNode(iri) ; up.setWithIRI(n) ; } )?
  ( DeleteClause(up) ( InsertClause(up) )?
  | InsertClause(up)
  )
  (UsingClause(up))*
  <WHERE>
  { startWherePattern() ; }
  el = GroupGraphPattern() { up.setElement(el) ; }
  { finishWherePattern() ; }
  { finishModifyUpdate() ; }
  { return up ; }
}
void DeleteClause(UpdateModify up) : { QuadAcc qp = up.getDeleteAcc() ; Token t ;}
{
   t = <DELETE>
   { int beginLine = t.beginLine; int beginColumn = t.beginColumn; t = null; }
   { startDeleteTemplate(qp, beginLine, beginColumn) ; }
   QuadPattern(qp)
   { finishDeleteTemplate(qp, beginLine, beginColumn) ; }
   { up.setHasDeleteClause(true) ; }
}
void InsertClause(UpdateModify up) : { QuadAcc qp = up.getInsertAcc() ; Token t ; }
{
   t = <INSERT>
   { int beginLine = t.beginLine; int beginColumn = t.beginColumn; t = null; }
   { startInsertTemplate(qp, beginLine, beginColumn) ; }
   QuadPattern(qp)
   { finishInsertTemplate(qp, beginLine, beginColumn) ; }
   { up.setHasInsertClause(true) ; }
}
void UsingClause(UpdateWithUsing update) : { String iri ; Node n ; }
{
  <USING>
  ( iri = iri()
    { n = createNode(iri) ; update.addUsing(n) ; }
  | <NAMED> iri = iri()
    { n = createNode(iri) ; update.addUsingNamed(n) ; }
  )
}
Target GraphOrDefault() : { String iri ; }
{
  ( <DFT> { return Target.DEFAULT ; }
  | (<GRAPH>)?
     iri = iri()
     { return Target.create(createNode(iri)) ; }
  )
}
Node GraphRef() : { String iri ; }
{
    <GRAPH> iri = iri()
    { return createNode(iri) ; }
}
Target GraphRefAll() : { Node iri ; }
{
   ( iri = GraphRef()
     { return Target.create(iri) ; }
   | <DFT> { return Target.DEFAULT ; }
   | <NAMED> { return Target.NAMED ; }
   | <ALL> { return Target.ALL ; }
   )
}
void QuadPattern(QuadAcc acc) : { }
{
    <LBRACE>
    Quads(acc)
    <RBRACE>
}
void QuadData(QuadDataAccSink acc) : { }
{
    <LBRACE>
    Quads(acc)
    <RBRACE>
}
void Quads(QuadAccSink acc) : { }
{
   (TriplesTemplate(acc))?
   (
     QuadsNotTriples(acc)
     (<DOT>)?
     (TriplesTemplate(acc))?
   )*
}
void QuadsNotTriples(QuadAccSink acc) : {Node gn ; Node prev = acc.getGraph() ; }
{
    <GRAPH>
       gn = VarOrIri()
    { setAccGraph(acc, gn) ; }
    <LBRACE>
    (TriplesTemplate(acc))?
    <RBRACE>
    { setAccGraph(acc, prev) ; }
}
void TriplesTemplate(TripleCollector acc) : { }
{
    TriplesSameSubject(acc)
    (<DOT> (TriplesTemplate(acc))?)?
}
Element GroupGraphPattern() : { Element el = null ; Token t ; }
{
  t = <LBRACE>
  { int beginLine = t.beginLine; int beginColumn = t.beginColumn; t = null; }
  (
    { startSubSelect(beginLine, beginColumn) ; }
    SubSelect()
    {
      Query q = endSubSelect(beginLine, beginColumn) ;
      el = new ElementSubQuery(q) ;
    }
  | el = GroupGraphPatternSub()
  )
  <RBRACE>
    { return el ; }
}
Element GroupGraphPatternSub() : { Element el = null ; }
{
      { ElementGroup elg = new ElementGroup() ; }
      { startGroup(elg) ; }
  (
    { startTriplesBlock() ; }
    el = TriplesBlock(null)
    { endTriplesBlock() ;
      elg.addElement(el) ; }
  )?
  (
    el = GraphPatternNotTriples()
    { elg.addElement(el) ; }
    (<DOT>)?
    (
      { startTriplesBlock() ; }
      el = TriplesBlock(null)
      { endTriplesBlock() ;
        elg.addElement(el) ; }
    )?
  )*
      { endGroup(elg) ; }
      { return elg ; }
}
Element TriplesBlock(ElementPathBlock acc) : { }
{
  { if ( acc == null )
        acc = new ElementPathBlock() ;
  }
  TriplesSameSubjectPath(acc)
  ( <DOT> (TriplesBlock(acc))? )?
    { return acc ; }
}
Element GraphPatternNotTriples() : { Element el = null ; }
{
 (
   el = GroupOrUnionGraphPattern()
 |
   el = OptionalGraphPattern()
 |
   el = MinusGraphPattern()
 |
   el = GraphGraphPattern()
 |
   el = ServiceGraphPattern()
 |
   el = Filter()
 |
   el = Bind()
 |
   el = InlineData()
 )
 { return el ; }
}
Element OptionalGraphPattern() : { Element el ; }
{ <OPTIONAL> el = GroupGraphPattern()
    { return new ElementOptional(el) ; }
}
Element GraphGraphPattern() : { Element el ; Node n ;}
{
  <GRAPH> n = VarOrIri() el = GroupGraphPattern()
    { return new ElementNamedGraph(n, el) ; }
}
Element ServiceGraphPattern() : { Element el ; Node n ; boolean silent = false ; }
{
  <SERVICE>
  (<SILENT>
   { silent=true; }
  )?
  n = VarOrIri()
  el = GroupGraphPattern()
    { return new ElementService(n, el, silent) ; }
}
Element Bind() : { Var v ; Expr expr ; }
{
  <BIND>
  <LPAREN>
  expr = Expression()
  <AS>
  v = Var()
  <RPAREN>
  { return new ElementBind(v, expr) ; }
}
Element InlineData() : { ElementData el ; Token t ; }
{
  t = <VALUES>
  { int beginLine = t.beginLine; int beginColumn = t.beginColumn; t = null; }
  { el = new ElementData() ;
    startInlineData(el.getVars(), el.getRows(), beginLine, beginColumn) ; }
  DataBlock()
  { finishInlineData(beginLine, beginColumn) ;
    return el ; }
}
void DataBlock() : { }
{
  ( InlineDataOneVar() | InlineDataFull() )
}
void InlineDataOneVar() : { Var v ; Node n ; Token t ; }
{
  v = Var()
  { emitDataBlockVariable(v) ; }
  t = <LBRACE>
  (
    n = DataBlockValue()
    { startDataBlockValueRow(-1, -1) ;
      emitDataBlockValue(n, -1, -1) ;
      finishDataBlockValueRow(-1, -1) ;
    }
  )*
  t = <RBRACE>
}
void InlineDataFull() : { Var v ; Node n ; Token t ; int beginLine; int beginColumn; }
{
  (
     <NIL>
  |
    <LPAREN>
    (v = Var() { emitDataBlockVariable(v) ; })*
    <RPAREN>
  )
  <LBRACE>
  (
    t = <LPAREN>
    { beginLine = t.beginLine; beginColumn = t.beginColumn; t = null; }
    { startDataBlockValueRow(beginLine, beginColumn) ; }
    (n = DataBlockValue()
        { emitDataBlockValue(n, beginLine, beginColumn) ; }
    ) *
    t = <RPAREN>
    { beginLine = t.beginLine; beginColumn = t.beginColumn; t = null; }
      { finishDataBlockValueRow(beginLine, beginColumn) ; }
  |
    t = <NIL>
    { beginLine = t.beginLine; beginColumn = t.beginColumn; t = null; }
      { startDataBlockValueRow(beginLine, beginColumn) ; }
      { finishDataBlockValueRow(beginLine, beginColumn) ; }
   )*
  <RBRACE>
}
Node DataBlockValue() : { Node n ; String iri ; }
{
  iri = iri() { return createNode(iri) ; }
| n = RDFLiteral() { return n ; }
| n = NumericLiteral() { return n ; }
| n = BooleanLiteral() { return n ; }
| <UNDEF> { return null ; }
}
Element MinusGraphPattern() : { Element el ; }
{
    <MINUS_P>
    el = GroupGraphPattern()
    { return new ElementMinus(el) ; }
}
Element GroupOrUnionGraphPattern() :
    { Element el = null ; ElementUnion el2 = null ; }
{
  el = GroupGraphPattern()
  ( <UNION>
    { if ( el2 == null )
      {
        el2 = new ElementUnion() ;
        el2.addElement(el) ;
      }
    }
  el = GroupGraphPattern()
    { el2.addElement(el) ; }
  )*
    { return (el2==null)? el : el2 ; }
}
Element Filter() : { Expr c ; }
{
  <FILTER> c = Constraint()
  { return new ElementFilter(c) ; }
}
Expr Constraint() : { Expr c ; }
{
  ( c = BrackettedExpression()
  | c = BuiltInCall()
  | c = FunctionCall()
  )
  { return c ; }
}
Expr FunctionCall() : { String fname ; ExprList a ; }
{
  fname = iri()
  a = ArgList()
  {
     if ( AggregateRegistry.isRegistered(fname) ) {
         if ( ! allowAggregatesInExpressions )
            throwParseException("Aggregate expression not legal at this point : "+fname, -1, -1) ;
         Aggregator agg = AggregatorFactory.createCustom(fname, a) ;
         Expr exprAgg = getQuery().allocAggregate(agg) ;
         return exprAgg ;
     }
     return new E_Function(fname, a) ;
  }
}
ExprList ArgList() : { Expr expr ; boolean distinct = false ;
                      ExprList args = new ExprList() ; Token t ; }
{
  (
    <NIL>
  |
    <LPAREN>
      (t = <DISTINCT> { distinct = true ; }
      { int beginLine = t.beginLine; int beginColumn = t.beginColumn; t = null; }
        {
          if ( ! allowAggregatesInExpressions )
              throwParseException("Aggregate expression not legal at this point",
                                 beginLine, beginColumn) ;
        }
      )?
      expr = Expression() { args.add(expr) ; }
      (<COMMA> expr = Expression() { args.add(expr) ; } )*
    <RPAREN>
   )
    { return args ; }
}
ExprList ExpressionList() : { Expr expr = null ; ExprList args = new ExprList() ;}
{
  (
    <NIL>
  |
    <LPAREN>
    expr = Expression() { args.add(expr) ; }
      (<COMMA> expr = Expression() { args.add(expr) ; } )*
    <RPAREN>
  )
  { return args ; }
}
Template ConstructTemplate() : { TripleCollectorBGP acc = new TripleCollectorBGP();
                                 Template t = new Template(acc.getBGP()) ; }
{
    { setInConstructTemplate(true) ; }
  <LBRACE>
    (ConstructTriples(acc))?
  <RBRACE>
    { setInConstructTemplate(false) ;
      return t ; }
}
void ConstructTriples(TripleCollector acc) : { }
{
    TriplesSameSubject(acc)
    (<DOT> (ConstructTriples(acc))? )?
}
void TriplesSameSubject(TripleCollector acc) : { Node s ; }
{
  s = VarOrTerm()
  PropertyListNotEmpty(s, acc)
|
  { ElementPathBlock tempAcc = new ElementPathBlock() ; }
  s = TriplesNode(tempAcc)
  PropertyList(s, tempAcc)
  { insert(acc, tempAcc) ; }
}
void PropertyList(Node s, TripleCollector acc) : { }
{
  ( PropertyListNotEmpty(s, acc) ) ?
}
void PropertyListNotEmpty(Node s, TripleCollector acc) :
    { Node p = null ; }
{
    p = Verb()
    ObjectList(s, p, null, acc)
  ( <SEMICOLON>
    (
       p = Verb()
      ObjectList(s, p, null, acc)
    )?
  )*
}
Node Verb() : { Node p ;}
{
  ( p = VarOrIri() | <KW_A> { p = nRDFtype ; } )
  { return p ; }
}
void ObjectList(Node s, Node p, Path path, TripleCollector acc): { Node o ; }
{
  Object(s, p, path, acc)
  ( <COMMA> Object(s, p, path, acc) )*
}
void Object(Node s, Node p, Path path, TripleCollector acc): { Node o ; }
{
  { ElementPathBlock tempAcc = new ElementPathBlock() ; int mark = tempAcc.mark() ; }
  o = GraphNode(tempAcc)
  { insert(tempAcc, mark, s, p, path, o) ; insert(acc, tempAcc) ; }
}
void TriplesSameSubjectPath(TripleCollector acc) : { Node s ; }
{
  s = VarOrTerm()
  PropertyListPathNotEmpty(s, acc)
|
  { ElementPathBlock tempAcc = new ElementPathBlock() ; }
  s = TriplesNodePath(tempAcc)
  PropertyListPath(s, tempAcc)
  { insert(acc, tempAcc) ; }
}
void PropertyListPath(Node s, TripleCollector acc) : { }
{
  ( PropertyListPathNotEmpty(s, acc) ) ?
}
void PropertyListPathNotEmpty(Node s, TripleCollector acc) :
    { Path path = null ; Node p = null ; }
{
  ( path = VerbPath()
  | p = VerbSimple()
  )
  ObjectListPath(s, p, path, acc)
  ( <SEMICOLON>
    { path = null ; p = null ; }
    (
      ( path = VerbPath()
      | p = VerbSimple()
      )
      ObjectListPath(s, p, path, acc)
    )?
  )*
}
Path VerbPath() : {Node p ; Path path ; }
{
  path = Path() { return path ; }
}
Node VerbSimple() : { Node p ; }
{
  p = Var()
  { return p ; }
}
void ObjectListPath(Node s, Node p, Path path, TripleCollector acc): { Node o ; }
{
  ObjectPath(s, p, path, acc)
  ( <COMMA> ObjectPath(s, p, path, acc) )*
}
void ObjectPath(Node s, Node p, Path path, TripleCollector acc): { Node o ; }
{
  { ElementPathBlock tempAcc = new ElementPathBlock() ; int mark = tempAcc.mark() ; }
  o = GraphNodePath(tempAcc)
  { insert(tempAcc, mark, s, p, path, o) ; insert(acc, tempAcc) ; }
}
Path Path() : { Path p ; }
{
  p = PathAlternative() { return p ; }
}
Path PathAlternative() : { Path p1 , p2 ; }
{
   p1 = PathSequence()
   (
      <VBAR> p2 = PathSequence()
      { p1 = PathFactory.pathAlt(p1, p2) ; }
   )*
   { return p1 ; }
}
Path PathSequence() : { Path p1 , p2 ; }
{
    p1 = PathEltOrInverse()
    ( <SLASH> p2 = PathEltOrInverse()
      { p1 = PathFactory.pathSeq(p1, p2) ; }
    )*
   { return p1; }
}
Path PathElt() : { String str ; Node n ; Path p ; }
{
   p = PathPrimary()
   ( p = PathMod(p) )?
   { return p ; }
}
Path PathEltOrInverse() : { String str ; Node n ; Path p ; }
{
   ( p = PathElt()
   | <CARAT>
     p = PathElt()
     { p = PathFactory.pathInverse(p) ; }
   )
   { return p ; }
}
Path PathMod(Path p) : { long i1 ; long i2 ; }
{
   ( <QMARK> { return PathFactory.pathZeroOrOne(p) ; }
   | <STAR> { return PathFactory.pathZeroOrMore1(p) ; }
   | <PLUS> { return PathFactory.pathOneOrMore1(p) ; }
   )
}
Path PathPrimary() : { String str ; Path p ; Node n ; }
{
  (
    str = iri()
     { n = createNode(str) ; p = PathFactory.pathLink(n) ; }
  | <KW_A>
     { p = PathFactory.pathLink(nRDFtype) ; }
  | <BANG> p = PathNegatedPropertySet()
  | <LPAREN> p = Path() <RPAREN>
  )
 { return p ; }
}
Path PathNegatedPropertySet() : { P_Path0 p ; P_NegPropSet pNegSet ; }
{
  { pNegSet = new P_NegPropSet() ; }
  ( p = PathOneInPropertySet()
    { pNegSet.add(p) ; }
  | <LPAREN>
    ( p = PathOneInPropertySet() { pNegSet.add(p) ; }
      (<VBAR> p = PathOneInPropertySet() { pNegSet.add(p) ; }) *
    )?
     <RPAREN>
  )
  { return pNegSet ; }
}
P_Path0 PathOneInPropertySet() : { String str ; Node n ; }
{
  ( str = iri() { n = createNode(str) ; return new P_Link(n) ; }
  | <KW_A> { return new P_Link(nRDFtype) ; }
  | <CARAT>
    ( str = iri() { n = createNode(str) ; return new P_ReverseLink(n) ; }
    | <KW_A> { return new P_ReverseLink(nRDFtype) ; }
    )
  )
}
long Integer() : {Token t ;}
{
    t = <INTEGER>
    { return integerValue(t.image) ; }
}
Node TriplesNode(TripleCollectorMark acc) : { Node n ; }
{
  n = Collection(acc) { return n ; }
 |
  n = BlankNodePropertyList(acc) { return n ; }
}
Node BlankNodePropertyList(TripleCollector acc) : { Token t ; }
{
  t = <LBRACKET>
    { Node n = createBNode( t.beginLine, t.beginColumn) ; }
  PropertyListNotEmpty(n, acc)
  <RBRACKET>
    { return n ; }
}
Node TriplesNodePath(TripleCollectorMark acc) : { Node n ; }
{
  n = CollectionPath(acc) { return n ; }
 |
  n = BlankNodePropertyListPath(acc) { return n ; }
}
Node BlankNodePropertyListPath(TripleCollector acc) : { Token t ; }
{
  t = <LBRACKET>
    { Node n = createBNode( t.beginLine, t.beginColumn) ; }
  PropertyListPathNotEmpty(n, acc)
  <RBRACKET>
    { return n ; }
}
Node Collection(TripleCollectorMark acc) :
    { Node listHead = nRDFnil ; Node lastCell = null ; int mark ; Node n ; Token t ; }
{
  t = <LPAREN>
  { int beginLine = t.beginLine; int beginColumn = t.beginColumn; t = null; }
  (
    { Node cell = createListNode( beginLine, beginColumn) ;
      if ( listHead == nRDFnil )
         listHead = cell ;
      if ( lastCell != null )
        insert(acc, lastCell, nRDFrest, cell) ;
      mark = acc.mark() ;
    }
    n = GraphNode(acc)
    {
      insert(acc, mark, cell, nRDFfirst, n) ;
      lastCell = cell ;
    }
  ) +
  <RPAREN>
   { if ( lastCell != null )
       insert(acc, lastCell, nRDFrest, nRDFnil) ;
     return listHead ; }
}
Node CollectionPath(TripleCollectorMark acc) :
    { Node listHead = nRDFnil ; Node lastCell = null ; int mark ; Node n ; Token t ; }
{
  t = <LPAREN>
  { int beginLine = t.beginLine; int beginColumn = t.beginColumn; t = null; }
  (
    { Node cell = createListNode( beginLine, beginColumn) ;
      if ( listHead == nRDFnil )
         listHead = cell ;
      if ( lastCell != null )
        insert(acc, lastCell, nRDFrest, cell) ;
      mark = acc.mark() ;
    }
    n = GraphNodePath(acc)
    {
      insert(acc, mark, cell, nRDFfirst, n) ;
      lastCell = cell ;
    }
  ) +
  <RPAREN>
   { if ( lastCell != null )
       insert(acc, lastCell, nRDFrest, nRDFnil) ;
     return listHead ; }
}
Node GraphNode(TripleCollectorMark acc) : { Node n ; }
{
  n = VarOrTerm() { return n ; }
 |
  n = TriplesNode(acc) { return n ; }
}
Node GraphNodePath(TripleCollectorMark acc) : { Node n ; }
{
  n = VarOrTerm() { return n ; }
 |
  n = TriplesNodePath(acc) { return n ; }
}
Node VarOrTerm() : {Node n = null ; }
{
  ( n = Var() | n = GraphTerm() )
  { return n ; }
}
Node VarOrIri() : {Node n = null ; String iri ; }
{
  ( n = Var() | iri = iri() { n = createNode(iri) ; } )
  { return n ; }
}
Node VarOrBlankNodeOrIri() : {Node n = null ; String iri ; }
{
  ( n = Var() | n = BlankNode() | iri = iri() { n = createNode(iri) ; } )
  { return n ; }
}
Var Var() : { Token t ;}
{
    ( t = <VAR1> | t = <VAR2> )
    { return createVariable(t.image, t.beginLine, t.beginColumn) ; }
}
Node GraphTerm() : { Node n ; String iri ; }
{
  iri = iri() { return createNode(iri) ; }
| n = RDFLiteral() { return n ; }
| n = NumericLiteral() { return n ; }
| n = BooleanLiteral() { return n ; }
| n = BlankNode() { return n ; }
| <NIL> { return nRDFnil ; }
}
Expr Expression() : { Expr expr ; }
{
  expr = ConditionalOrExpression()
  { return expr ; }
}
Expr ConditionalOrExpression() : { Expr expr1, expr2 ; }
{
  expr1 = ConditionalAndExpression()
  ( <SC_OR> expr2 = ConditionalAndExpression()
    { expr1 = new E_LogicalOr(expr1, expr2) ; }
  )*
    { return expr1 ; }
}
Expr ConditionalAndExpression() : { Expr expr1, expr2 ;}
{
  expr1 = ValueLogical()
  ( <SC_AND> expr2 = ValueLogical()
    { expr1 = new E_LogicalAnd(expr1, expr2) ; }
  )*
    { return expr1 ; }
}
Expr ValueLogical() : { Expr expr ; }
{
  expr = RelationalExpression()
    { return expr ; }
}
Expr RelationalExpression() : { Expr expr1, expr2 ; ExprList a ; }
{
  expr1 = NumericExpression()
  (
    <EQ> expr2 = NumericExpression()
      { expr1 = new E_Equals(expr1, expr2) ; }
  | <NE> expr2 = NumericExpression()
      { expr1 = new E_NotEquals(expr1, expr2) ; }
  | <LT> expr2 = NumericExpression()
      { expr1 = new E_LessThan(expr1, expr2) ; }
  | <GT> expr2 = NumericExpression()
      { expr1 = new E_GreaterThan(expr1, expr2) ; }
  | <LE> expr2 = NumericExpression()
      { expr1 = new E_LessThanOrEqual(expr1, expr2) ; }
  | <GE> expr2 = NumericExpression()
      { expr1 = new E_GreaterThanOrEqual(expr1, expr2) ; }
  | <IN> a = ExpressionList()
      { expr1 = new E_OneOf(expr1, a) ; }
  | <NOT> <IN> a = ExpressionList()
      { expr1 = new E_NotOneOf(expr1, a) ; }
  )?
    { return expr1 ; }
}
Expr NumericExpression () : { Expr expr ; }
{
  expr = AdditiveExpression()
    { return expr ; }
}
Expr AdditiveExpression() : { Expr expr1, expr2, expr3 ; boolean addition ; Node n ; }
{
  expr1 = MultiplicativeExpression()
  ( <PLUS> expr2 = MultiplicativeExpression()
    { expr1 = new E_Add(expr1, expr2) ; }
  | <MINUS> expr2 = MultiplicativeExpression()
    { expr1 = new E_Subtract(expr1, expr2) ; }
  |
    (
      n = NumericLiteralPositive()
      {
         n = stripSign(n) ;
         expr2 = asExpr(n) ;
         addition = true ;
      }
    |
      n = NumericLiteralNegative()
     {
         n = stripSign(n) ;
         expr2 = asExpr(n) ;
         addition = false ;
     }
     )
    (
      ( <STAR> expr3 = UnaryExpression() { expr2 = new E_Multiply(expr2, expr3) ; } )
    |
      ( <SLASH> expr3 = UnaryExpression() { expr2 = new E_Divide(expr2, expr3) ; } )
    )*
    { if ( addition )
         expr1 = new E_Add(expr1, expr2) ;
      else
         expr1 = new E_Subtract(expr1, expr2) ;
    }
  )*
  { return expr1 ; }
}
Expr MultiplicativeExpression() : { Expr expr1, expr2 ; }
{
  expr1 = UnaryExpression()
  ( <STAR> expr2 = UnaryExpression()
    { expr1 = new E_Multiply(expr1, expr2) ; }
  | <SLASH> expr2 = UnaryExpression()
    { expr1 = new E_Divide(expr1, expr2) ; }
  )*
    { return expr1 ; }
}
Expr UnaryExpression() : { Expr expr ; }
{
  <BANG> expr = PrimaryExpression()
    { return new E_LogicalNot(expr) ; }
  | <PLUS> expr = PrimaryExpression() { return new E_UnaryPlus(expr) ; }
  | <MINUS> expr = PrimaryExpression() { return new E_UnaryMinus(expr) ; }
  | expr = PrimaryExpression() { return expr ; }
}
Expr PrimaryExpression() : { Expr expr ; Node gn ; }
{
  ( expr = BrackettedExpression() { return expr ; }
  | expr = BuiltInCall() { return expr ; }
  | expr = iriOrFunction() { return expr ; }
  | gn = RDFLiteral() { return asExpr(gn) ; }
  | gn = NumericLiteral() { return asExpr(gn) ; }
  | gn = BooleanLiteral() { return asExpr(gn) ; }
  | gn = Var() { return asExpr(gn) ; }
  )
}
Expr BrackettedExpression() : { Expr expr ; }
{
    <LPAREN> expr = Expression() <RPAREN> { return expr ; }
}
Expr BuiltInCall() : { Expr expr ; Expr expr1 = null ; Expr expr2 = null ;
                       Node gn ; ExprList a ; }
{
    expr = Aggregate() { return expr ; }
  |
    <STR> <LPAREN> expr = Expression() <RPAREN>
    { return new E_Str(expr) ; }
  | <LANG> <LPAREN> expr = Expression() <RPAREN>
    { return new E_Lang(expr) ; }
  | <LANGMATCHES>
       <LPAREN> expr1 = Expression() <COMMA> expr2 = Expression() <RPAREN>
    { return new E_LangMatches(expr1, expr2) ; }
  | <DTYPE> <LPAREN> expr = Expression() <RPAREN>
    { return new E_Datatype(expr) ; }
  | <BOUND> <LPAREN> gn = Var() <RPAREN>
    { return new E_Bound(new ExprVar(gn)) ; }
  | <IRI> <LPAREN> expr = Expression() <RPAREN>
    { return new E_IRI(expr) ; }
  | <URI> <LPAREN> expr = Expression() <RPAREN>
    { return new E_URI(expr) ; }
  | <BNODE>
    ( <LPAREN> expr1 = Expression() <RPAREN>
      { return new E_BNode(expr1) ; }
    |
      <NIL> { return new E_BNode() ; }
    )
  | <RAND> <NIL> { return new E_Random() ; }
  | <ABS> <LPAREN> expr1 = Expression() <RPAREN> { return new E_NumAbs(expr1) ; }
  | <CEIL> <LPAREN> expr1 = Expression() <RPAREN> { return new E_NumCeiling(expr1) ; }
  | <FLOOR> <LPAREN> expr1 = Expression() <RPAREN> { return new E_NumFloor(expr1) ; }
  | <ROUND> <LPAREN> expr1 = Expression() <RPAREN> { return new E_NumRound(expr1) ; }
  | <CONCAT> a = ExpressionList() { return new E_StrConcat(a) ; }
  | expr = SubstringExpression() { return expr ; }
  | <STRLEN> <LPAREN> expr1 = Expression() <RPAREN> { return new E_StrLength(expr1) ; }
  | expr = StrReplaceExpression() { return expr ; }
  | <UCASE> <LPAREN> expr1 = Expression() <RPAREN> { return new E_StrUpperCase(expr1) ; }
  | <LCASE> <LPAREN> expr1 = Expression() <RPAREN> { return new E_StrLowerCase(expr1) ; }
  | <ENCODE_FOR_URI> <LPAREN> expr1 = Expression() <RPAREN> { return new E_StrEncodeForURI(expr1) ; }
  | <CONTAINS> <LPAREN> expr1 = Expression() <COMMA> expr2 = Expression() <RPAREN>
    { return new E_StrContains(expr1, expr2) ; }
  | <STRSTARTS> <LPAREN> expr1 = Expression() <COMMA> expr2 = Expression() <RPAREN>
    { return new E_StrStartsWith(expr1, expr2) ; }
  | <STRENDS> <LPAREN> expr1 = Expression() <COMMA> expr2 = Expression() <RPAREN>
    { return new E_StrEndsWith(expr1, expr2) ; }
  | <STRBEFORE> <LPAREN> expr1 = Expression() <COMMA> expr2 = Expression() <RPAREN>
    { return new E_StrBefore(expr1, expr2) ; }
  | <STRAFTER> <LPAREN> expr1 = Expression() <COMMA> expr2 = Expression() <RPAREN>
    { return new E_StrAfter(expr1, expr2) ; }
  | <YEAR> <LPAREN> expr1 = Expression() <RPAREN> { return new E_DateTimeYear(expr1) ; }
  | <MONTH> <LPAREN> expr1 = Expression() <RPAREN> { return new E_DateTimeMonth(expr1) ; }
  | <DAY> <LPAREN> expr1 = Expression() <RPAREN> { return new E_DateTimeDay(expr1) ; }
  | <HOURS> <LPAREN> expr1 = Expression() <RPAREN> { return new E_DateTimeHours(expr1) ; }
  | <MINUTES> <LPAREN> expr1 = Expression() <RPAREN> { return new E_DateTimeMinutes(expr1) ; }
  | <SECONDS> <LPAREN> expr1 = Expression() <RPAREN> { return new E_DateTimeSeconds(expr1) ; }
  | <TIMEZONE> <LPAREN> expr1 = Expression() <RPAREN> { return new E_DateTimeTimezone(expr1) ; }
  | <TZ> <LPAREN> expr1 = Expression() <RPAREN> { return new E_DateTimeTZ(expr1) ; }
  | <NOW> <NIL> { return new E_Now() ; }
  | <UUID> <NIL> { return new E_UUID() ; }
  | <STRUUID> <NIL> { return new E_StrUUID() ; }
  | <MD5> <LPAREN> expr1 = Expression() <RPAREN> { return new E_MD5(expr1) ; }
  | <SHA1> <LPAREN> expr1 = Expression() <RPAREN> { return new E_SHA1(expr1) ; }
  | <SHA256> <LPAREN> expr1 = Expression() <RPAREN> { return new E_SHA256(expr1) ; }
  | <SHA384> <LPAREN> expr1 = Expression() <RPAREN> { return new E_SHA384(expr1) ; }
  | <SHA512> <LPAREN> expr1 = Expression() <RPAREN> { return new E_SHA512(expr1) ; }
  | <COALESCE> a = ExpressionList()
    { return new E_Coalesce(a) ; }
  | <IF> <LPAREN> expr = Expression() <COMMA>
                  expr1 = Expression() <COMMA>
                  expr2 = Expression() <RPAREN>
    { return new E_Conditional(expr, expr1, expr2) ; }
  | <STRLANG> <LPAREN> expr1 = Expression() <COMMA> expr2 = Expression() <RPAREN>
    { return new E_StrLang(expr1, expr2) ; }
  | <STRDT> <LPAREN> expr1 = Expression() <COMMA> expr2 = Expression() <RPAREN>
    { return new E_StrDatatype(expr1, expr2) ; }
  | <SAME_TERM> <LPAREN> expr1 = Expression() <COMMA> expr2 = Expression() <RPAREN>
    { return new E_SameTerm(expr1, expr2) ; }
  | <IS_IRI> <LPAREN> expr = Expression() <RPAREN>
    { return new E_IsIRI(expr) ; }
  | <IS_URI> <LPAREN> expr = Expression() <RPAREN>
    { return new E_IsURI(expr) ; }
  | <IS_BLANK> <LPAREN> expr = Expression() <RPAREN>
    { return new E_IsBlank(expr) ; }
  | <IS_LITERAL> <LPAREN> expr = Expression() <RPAREN>
    { return new E_IsLiteral(expr) ; }
  | <IS_NUMERIC> <LPAREN> expr = Expression() <RPAREN>
    { return new E_IsNumeric(expr) ; }
  |
    expr = RegexExpression() { return expr ; }
  | expr = ExistsFunc() { return expr ; }
  | expr = NotExistsFunc() { return expr ; }
}
Expr RegexExpression() :
{ Expr expr ; Expr patExpr = null ; Expr flagsExpr = null ; }
{
    <REGEX>
    <LPAREN>
      expr = Expression()
      <COMMA>
      patExpr = Expression()
      ( <COMMA> flagsExpr = Expression() ) ?
    <RPAREN>
      { return new E_Regex(expr, patExpr, flagsExpr) ; }
}
Expr SubstringExpression() :
{ Expr expr1 ; Expr expr2 = null ; Expr expr3 = null ; }
{
    <SUBSTR>
    <LPAREN>
      expr1 = Expression()
      <COMMA>
      expr2 = Expression()
      ( <COMMA> expr3 = Expression() ) ?
    <RPAREN>
      { return new E_StrSubstring(expr1, expr2, expr3) ; }
}
Expr StrReplaceExpression() :
{ Expr expr1 ; Expr expr2 = null ; Expr expr3 = null ; Expr expr4 = null ;}
{
  <REPLACE>
  <LPAREN>
  expr1 = Expression()
  <COMMA> expr2 = Expression()
  <COMMA> expr3 = Expression()
  ( <COMMA> expr4 = Expression() ) ?
  <RPAREN>
  { return new E_StrReplace(expr1,expr2,expr3,expr4) ; }
}
Expr ExistsFunc() : { Element el ; }
{
   <EXISTS>
   el = GroupGraphPattern()
   { return createExprExists(el) ; }
}
Expr NotExistsFunc() : { Element el ; }
{
   <NOT> <EXISTS>
   el = GroupGraphPattern()
   { return createExprNotExists(el) ; }
}
Expr Aggregate() : { Aggregator agg = null ; String sep = null ;
                     boolean distinct = false ;
                     Expr expr = null ; Expr expr2 = null ;
                     ExprList a = new ExprList() ;
                     ExprList ordered = new ExprList() ;
                     Token t ; }
{
  ( t = <COUNT> <LPAREN>
    ( <DISTINCT> { distinct = true ; } )?
    ( <STAR> | expr = Expression() )
    <RPAREN>
    { if ( expr == null ) { agg = AggregatorFactory.createCount(distinct) ; }
      if ( expr != null ) { agg = AggregatorFactory.createCountExpr(distinct, expr) ; }
    }
  | t = <SUM> <LPAREN> ( <DISTINCT> { distinct = true ; } )? expr = Expression() <RPAREN>
    { agg = AggregatorFactory.createSum(distinct, expr) ; }
  | t = <MIN> <LPAREN> ( <DISTINCT> { distinct = true ; } )? expr = Expression() <RPAREN>
    { agg = AggregatorFactory.createMin(distinct, expr) ; }
  | t = <MAX> <LPAREN> ( <DISTINCT> { distinct = true ; } )? expr = Expression() <RPAREN>
    { agg = AggregatorFactory.createMax(distinct, expr) ; }
  | t = <AVG> <LPAREN> ( <DISTINCT> { distinct = true ; } )? expr = Expression() <RPAREN>
    { agg = AggregatorFactory.createAvg(distinct, expr) ; }
  | t = <SAMPLE> <LPAREN> ( <DISTINCT> { distinct = true ; } )? expr = Expression() <RPAREN>
    { agg = AggregatorFactory.createSample(distinct, expr) ; }
  | t = <GROUP_CONCAT>
    <LPAREN>
    (t = <DISTINCT> { distinct = true ; })?
    expr = Expression() { a.add(expr) ; }
    (<SEMICOLON> <SEPARATOR> <EQ> sep=String())?
    <RPAREN>
    { agg = AggregatorFactory.createGroupConcat(distinct, expr, sep, ordered) ; }
   )
   {
     if ( ! allowAggregatesInExpressions )
            throwParseException("Aggregate expression not legal at this point",
                                 t.beginLine, t.beginColumn) ;
   }
   { Expr exprAgg = getQuery().allocAggregate(agg) ;
     return exprAgg ; }
}
Expr iriOrFunction() : { String iri ; ExprList a = null ;
                         ExprList params = null ;
                         boolean distinct = false ; }
{
  iri = iri()
  (a = ArgList())?
  { if ( a == null )
       return asExpr(createNode(iri)) ;
    if ( AggregateRegistry.isRegistered(iri) ) {
         if ( ! allowAggregatesInExpressions )
            throwParseException("Aggregate expression not legal at this point : "+iri, -1, -1) ;
         Aggregator agg = AggregatorFactory.createCustom(iri, a) ;
         Expr exprAgg = getQuery().allocAggregate(agg) ;
         return exprAgg ;
      }
    return new E_Function(iri, a) ;
  }
}
Node RDFLiteral() : { Token t ; String lex = null ; }
{
  lex = String()
  { String lang = null ; String uri = null ; }
  (
    ( t = <LANGTAG> { lang = stripChars(t.image, 1) ; } )
  |
    ( <DATATYPE> uri = iri() )
  )?
    { return createLiteral(lex, lang, uri) ; }
}
Node NumericLiteral() : { Node n ; }
{
  (
    n = NumericLiteralUnsigned()
  | n = NumericLiteralPositive()
  | n = NumericLiteralNegative()
  )
  { return n ; }
}
Node NumericLiteralUnsigned() : { Token t ; }
{
  t = <INTEGER> { return createLiteralInteger(t.image) ; }
| t = <DECIMAL> { return createLiteralDecimal(t.image) ; }
| t = <DOUBLE> { return createLiteralDouble(t.image) ; }
}
Node NumericLiteralPositive() : { Token t ; }
{
  t = <INTEGER_POSITIVE> { return createLiteralInteger(t.image) ; }
| t = <DECIMAL_POSITIVE> { return createLiteralDecimal(t.image) ; }
| t = <DOUBLE_POSITIVE> { return createLiteralDouble(t.image) ; }
}
Node NumericLiteralNegative() : { Token t ; }
{
  t = <INTEGER_NEGATIVE> { return createLiteralInteger(t.image) ; }
| t = <DECIMAL_NEGATIVE> { return createLiteralDecimal(t.image) ; }
| t = <DOUBLE_NEGATIVE> { return createLiteralDouble(t.image) ; }
}
Node BooleanLiteral() : {}
{
  <TRUE> { return XSD_TRUE ; }
 |
  <FALSE> { return XSD_FALSE ; }
}
String String() : { Token t ; String lex ; }
{
  ( t = <STRING_LITERAL1> { lex = stripQuotes(t.image) ; }
  | t = <STRING_LITERAL2> { lex = stripQuotes(t.image) ; }
  | t = <STRING_LITERAL_LONG1> { lex = stripQuotes3(t.image) ; }
  | t = <STRING_LITERAL_LONG2> { lex = stripQuotes3(t.image) ; }
  )
    {
      lex = unescapeStr(lex, t.beginLine, t.beginColumn) ;
      return lex ;
    }
}
String iri() : { String iri ; }
{
  iri = IRIREF() { return iri ; }
|
  iri = PrefixedName() { return iri ; }
}
String PrefixedName() : { Token t ; }
{
  ( t = <PNAME_LN>
    { return resolvePName(t.image, t.beginLine, t.beginColumn) ; }
  |
    t = <PNAME_NS>
    { return resolvePName(t.image, t.beginLine, t.beginColumn) ; }
  )
}
Node BlankNode() : { Token t = null ; }
{
  t = <BLANK_NODE_LABEL>
    { return createBNode(t.image, t.beginLine, t.beginColumn) ; }
|
  t = <ANON> { return createBNode(t.beginLine, t.beginColumn) ; }
}
String IRIREF() : { Token t ; }
{
  t = <IRIref>
  { return resolveQuotedIRI(t.image, t.beginLine, t.beginColumn) ; }
}
SKIP : { " " | "\t" | "\n" | "\r" | "\f" }
SPECIAL_TOKEN :
{ <SINGLE_LINE_COMMENT: "#" (~["\n","\r"])* ("\n"|"\r"|"\r\n")? > }
TOKEN: {
  <#WS: " " | "\t" | "\n" | "\r" | "\f">
|
  <#WSC: <WS> | <SINGLE_LINE_COMMENT> >
|
  <BOM: "\uFEFF">
}
TOKEN:
{
   <IRIref: "<" (~[ ">","<", "\"", "{", "}", "^", "\\", "|", "`",
                      "\u0000"-"\u0020"])* ">" >
| <PNAME_NS: (<PN_PREFIX>)? ":" >
| <PNAME_LN: <PNAME_NS> <PN_LOCAL> >
| <BLANK_NODE_LABEL: "_:" (<PN_CHARS_U> | ["0"-"9"]) ((<PN_CHARS>|".")* <PN_CHARS>)? >
| <VAR1: "?" <VARNAME> >
| <VAR2: "$" <VARNAME> >
| <LANGTAG: <AT> (<A2Z>)+("-" (<A2ZN>)+)* >
| <#A2Z: ["a"-"z","A"-"Z"]>
| <#A2ZN: ["a"-"z","A"-"Z","0"-"9"]>
}
TOKEN : { <KW_A: "a" > }
TOKEN [IGNORE_CASE] :
{
   < BASE: "base" >
| < PREFIX: "prefix" >
| < SELECT: "select" >
| < DISTINCT: "distinct" >
| < REDUCED: "reduced" >
| < DESCRIBE: "describe" >
| < CONSTRUCT: "construct" >
| < ASK: "ask" >
| < LIMIT: "limit" >
| < OFFSET: "offset" >
| < ORDER: "order" >
| < BY: "by" >
| < VALUES: "values" >
| < UNDEF: "undef" >
| < ASC: "asc" >
| < DESC: "desc" >
| < NAMED: "named" >
| < FROM: "from" >
| < STREAM: "stream" >
| < RANGE: "range" >
| < TUMBLING: "tumbling" >
| < STEP: "step" >
| < LAST: "LAST" >
| < WHERE: "where" >
| < AND: "and" >
| < GRAPH: "graph" >
| < OPTIONAL: "optional" >
| < UNION: "union" >
| < MINUS_P: "minus" >
| < BIND: "bind" >
| < SERVICE: "service" >
| < EXISTS: "exists" >
| < NOT: "not" >
| < AS: "as" >
| < GROUP: "group" >
| < HAVING: "having" >
| < SEPARATOR: "separator" >
| < AGG: "agg" >
| < COUNT: "count" >
| < MIN: "min" >
| < MAX: "max" >
| < SUM: "sum" >
| < AVG: "avg" >
| < STDDEV: "stdev" >
| < SAMPLE: "sample" >
| < GROUP_CONCAT: "group_concat" >
| < FILTER: "filter" >
| < BOUND: "bound" >
| < COALESCE: "coalesce" >
| < IN: "in" >
| < IF: "if" >
| < BNODE: "bnode" >
| < IRI: "iri" >
| < URI: "uri" >
| < STR: "str" >
| < STRLANG: "strlang" >
| < STRDT: "strdt" >
| < DTYPE: "datatype" >
| < LANG: "lang" >
| < LANGMATCHES: "langmatches" >
| < IS_URI: "isURI" >
| < IS_IRI: "isIRI" >
| < IS_BLANK: "isBlank" >
| < IS_LITERAL: "isLiteral" >
| < IS_NUMERIC: "isNumeric" >
| < REGEX: "regex" >
| < SAME_TERM: "sameTerm" >
| < RAND: "RAND" >
| < ABS: "ABS" >
| < CEIL: "CEIL" >
| < FLOOR: "FLOOR" >
| < ROUND: "ROUND" >
| < CONCAT: "CONCAT" >
| < SUBSTR: "SUBSTR" >
| < STRLEN: "STRLEN" >
| < REPLACE: "REPLACE" >
| < UCASE: "UCASE" >
| < LCASE: "LCASE" >
| < ENCODE_FOR_URI: "ENCODE_FOR_URI" >
| < CONTAINS: "CONTAINS" >
| < STRSTARTS: "STRSTARTS" >
| < STRENDS: "STRENDS" >
| < STRBEFORE: "STRBEFORE" >
| < STRAFTER : "STRAFTER" >
| < YEAR: "YEAR" >
| < MONTH: "MONTH" >
| < DAY: "DAY" >
| < HOURS: "HOURS" >
| < MINUTES: "MINUTES" >
| < SECONDS: "SECONDS" >
| < TIMEZONE: "TIMEZONE" >
| < TZ: "TZ" >
| < NOW: "NOW" >
| < UUID: "UUID" >
| < STRUUID: "STRUUID" >
| < MD5: "MD5" >
| < SHA1: "SHA1" >
| < SHA224: "SHA224" >
| < SHA256: "SHA256" >
| < SHA384: "SHA384" >
| < SHA512: "SHA512" >
| < TRUE: "true" >
| < FALSE: "false" >
}
TOKEN [IGNORE_CASE] :
{
  < DATA: "data" >
| < INSERT: "insert">
| < DELETE: "delete" >
| < INSERT_DATA: <INSERT> (<WSC>)* <DATA> >
| < DELETE_DATA: <DELETE> (<WSC>)* <DATA> >
| < DELETE_WHERE: <DELETE> (<WSC>)* <WHERE> >
| < LOAD: "load" >
| < CLEAR: "clear" >
| < CREATE: "create" >
| < ADD: "add" >
| < MOVE: "move" >
| < COPY: "copy" >
| < META: "meta" >
| < SILENT: "silent" >
| < DROP: "drop" >
| < INTO: "into" >
| < TO: "to" >
| < DFT: "default" >
| < ALL: "all" >
| < WITH: "with" >
| < USING: "using" >
}
TOKEN :
{
  < #DIGITS: (["0"-"9"])+>
| < INTEGER: <DIGITS> >
| < DECIMAL: (<DIGITS>)? "." <DIGITS> >
| < DOUBLE:
      (
        (["0"-"9"])+ "." (["0"-"9"])* <EXPONENT>
        | "." (["0"-"9"])+ (<EXPONENT>)
        | (["0"-"9"])+ <EXPONENT>
      )
      >
| < TIMEUNIT: ("ms"|"s"|"m"|"h"|"d") >
| < INTEGER_POSITIVE: <PLUS> <INTEGER> >
| < DECIMAL_POSITIVE: <PLUS> <DECIMAL> >
| < DOUBLE_POSITIVE: <PLUS> <DOUBLE> >
| < INTEGER_NEGATIVE: <MINUS> <INTEGER> >
| < DECIMAL_NEGATIVE: <MINUS> <DECIMAL> >
| < DOUBLE_NEGATIVE: <MINUS> <DOUBLE> >
| < #EXPONENT: ["e","E"] (["+","-"])? (["0"-"9"])+ >
| < #QUOTE_3D: "\"\"\"">
| < #QUOTE_3S: "'''">
| <ECHAR: "\\" ( "t"|"b"|"n"|"r"|"f"|"\\"|"\""|"'") >
| < STRING_LITERAL1:
      "'" ( (~["'","\\","\n","\r"]) | <ECHAR> )* "'" >
| < STRING_LITERAL2:
      "\"" ( (~["\"","\\","\n","\r"]) | <ECHAR> )* "\"" >
| < STRING_LITERAL_LONG1:
     <QUOTE_3S>
      ( ("'" | "''")? (~["'","\\"] | <ECHAR> ))*
     <QUOTE_3S> >
| < STRING_LITERAL_LONG2:
     <QUOTE_3D>
      ( ("\"" | "\"\"")? (~["\"","\\"] | <ECHAR> ))*
     <QUOTE_3D> >
}
TOKEN :
{
  < LPAREN: "(" >
| < RPAREN: ")" >
| <NIL: <LPAREN> (<WSC>)* <RPAREN> >
| < LBRACE: "{" >
| < RBRACE: "}" >
| < LBRACKET: "[" >
| < RBRACKET: "]" >
| < ANON: <LBRACKET> (<WSC>)* <RBRACKET> >
| < SEMICOLON: ";" >
| < COMMA: "," >
| < DOT: "." >
| < EQ: "=" >
| < NE: "!=" >
| < GT: ">" >
| < LT: "<" >
| < LE: "<=" >
| < GE: ">=" >
| < BANG: "!" >
| < TILDE: "~" >
| < COLON: ":" >
| < SC_OR: "||" >
| < SC_AND: "&&" >
| < PLUS: "+" >
| < MINUS: "-" >
| < STAR: "*" >
| < SLASH: "/" >
| < DATATYPE: "^^">
| < AT: "@">
| < VBAR: "|" >
| < CARAT: "^" >
| < FPATH: "->" >
| < RPATH: "<-" >
| < QMARK: "?" >
}
TOKEN:
{
  <#PN_CHARS_BASE:
          ["A"-"Z"] | ["a"-"z"] |
          ["\u00C0"-"\u00D6"] | ["\u00D8"-"\u00F6"] | ["\u00F8"-"\u02FF"] |
          ["\u0370"-"\u037D"] | ["\u037F"-"\u1FFF"] |
          ["\u200C"-"\u200D"] | ["\u2070"-"\u218F"] | ["\u2C00"-"\u2FEF"] |
          ["\u3001"-"\uD7FF"] | ["\uF900"-"\uFFFD"]
          >
|
  <#PN_CHARS_U: <PN_CHARS_BASE> | "_" >
|
  <#PN_CHARS: (<PN_CHARS_U> | "-" | ["0"-"9"] | "\u00B7" |
              ["\u0300"-"\u036F"] | ["\u203F"-"\u2040"] ) >
|
  <#PN_PREFIX: <PN_CHARS_BASE> ((<PN_CHARS>|".")* <PN_CHARS>)? >
|
  <#PN_LOCAL: (<PN_CHARS_U> | ":" | ["0"-"9"] | <PLX> )
              ( (<PN_CHARS> | "." |":" | <PLX> )*
                (<PN_CHARS> | ":" | <PLX>) )? >
|
  <#VARNAME: ( <PN_CHARS_U> | ["0"-"9"] )
             ( <PN_CHARS_U> | ["0"-"9"] | "\u00B7" |
               ["\u0300"-"\u036F"] | ["\u203F"-"\u2040"] )* >
|
  < #PN_LOCAL_ESC: "\\"
          ( "_" |
            "~" | "." | "-" | "!" | "$" | "&" | "'" |
           "(" | ")" | "*" | "+" | "," | ";" | "=" |
           "/" | "?" | "#" | "@" | "%" ) >
|
  <#PLX: <PERCENT> | <PN_LOCAL_ESC> >
|
  < #HEX: ["0"-"9"] | ["A"-"F"] | ["a"-"f"] >
|
  < #PERCENT: "%" <HEX> <HEX> >
}
TOKEN:
{
  <#UNKNOWN: (~[" ","\t","\n","\r","\f" ])+ >
}