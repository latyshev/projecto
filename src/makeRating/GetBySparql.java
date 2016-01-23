package makeRating;

import java.util.HashSet;
import org.apache.jena.query.*;

public class GetBySparql 
{
    static String sparqlEndpoint = "http://lod.ifmo.ru/sparql";

    // текст SPARQL-запроса
    static String sparqlQuery = "" +
    "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n" +
    "PREFIX vwc: <http://vivoweb.org/ontology/core#> \n" +
    "PREFIX aksw: <http://vivoplus.aksw.org/ontology#> \n" +
    "select distinct ?name ?lastName ?middleName where {\n" + 
    "?person vwc:affiliatedOrganization ?laboratory;\n" +
    "foaf:firstName ?name; \n" +
    "foaf:lastName ?lastName;\n" +
    "vwc:middleName ?middleName }";

    public static HashSet<String> getPersons() 
    {
        //Выполнение запроса
        Query query = QueryFactory.create(sparqlQuery) ;
        QueryExecution qExe = QueryExecutionFactory.sparqlService(sparqlEndpoint, query);

        //Из каждой строки результата получаем ФИО и добавляем в возвращаемый список 
        ResultSet results = qExe.execSelect();
        HashSet<String> persons = new HashSet<String>();
        while (results.hasNext()) 
        {
            QuerySolution solution = results.next();
            String name = solution.get("name").asLiteral().getLexicalForm();
            String surname = solution.get("lastName").asLiteral().getLexicalForm();
            String middlename = solution.get("middleName").asLiteral().getLexicalForm();
            persons.add(surname + " " + name + " " + middlename);
        }
        return persons;
    }
}