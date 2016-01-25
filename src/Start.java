

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import makeRating.MakeRating;
import static makeRating.ParseIt.*;
import static makeRating.GetBySparql.*;

/**
 * Servlet implementation class Start
 */
@WebServlet("/Start")
public class Start extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	static HashMap results = new HashMap();
    public Start() {
        
    	super();
        
    }
    public static <K, V extends Comparable<? super V>> HashMap<K, V> sortByValue(HashMap<K, V> map)
    {
        LinkedList<HashMap.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort( list, new Comparator<HashMap.Entry<K, V>>()
            {
                @Override
                public int compare(HashMap.Entry<K, V> e1, HashMap.Entry<K, V> e2)
                {
                    return (e2.getValue()).compareTo(e1.getValue());
                }
            }
        );

        HashMap<K, V> result = new LinkedHashMap<>();
        for (HashMap.Entry<K, V> entry : list)
        {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html;charset=windows-1251");
		PrintWriter out1 = response.getWriter();
		//request.getParameter("Data");
		
		try 
        {
            //Получение списка сотрудников лаборатории (класс GetBySparql)
            HashSet<String> persons = getPersons(); 
            
            //Получение списка авторов на dblp (класс ParseIt)
            HashSet<String> data = parsing();
                       
            //Выполнение необходимых рассчетов (класс MakeRating)
            MakeRating ratingCalculation = new MakeRating();
            results.putAll(ratingCalculation.calculationsInThreads(persons, data));
            
            //Сортировка по значению SJR
            results = sortByValue(results);
            
            //Запись результатов в файл
            File outputFile = new File("results.csv");
            
            //Если файла нет - создать
            if(!outputFile.exists())
            {
                outputFile.createNewFile();
            }
            
            PrintWriter out = new PrintWriter(outputFile.getAbsoluteFile(), "Cp1251");
            
            Iterator iterator = results.keySet().iterator();
            while (iterator.hasNext())
            {
                String key = iterator.next().toString();
                String value = results.get(key).toString();
                out.println(key + " , " + value + " , ");
            }
            out.close();
        } catch (Exception ex) {
            Logger.getLogger(Start.class.getName()).log(Level.SEVERE, null, ex);
        }
	}
}
