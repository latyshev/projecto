

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html;charset=windows-1251");
		PrintWriter out1 = response.getWriter();
		//request.getParameter("Data");
		try {
			//Получение списка сотрудников лаборатории (класс GetBySparql)
			HashSet<String> persons = getPersons(); 
        
			//Получение списка авторов на dblp (класс ParseIt)
			HashSet<String> data = parsing();
                   
			//Выполнение необходимых рассчетов (класс MakeRating)
			MakeRating ratingCalculation = new MakeRating();
			results.putAll(ratingCalculation.calculationsInThreads(persons, data));
        
			out1.println("<P ALIGN='center'><TABLE BORDER=1>");
			Iterator iterator = results.keySet().iterator();
			while (iterator.hasNext())
			{	
				out1.println("<tr>");
            	String key = iterator.next().toString();
            	String value = results.get(key).toString();
            	out1.println("<td>"+ key + "</td>" + "<td>" + value + "</td>");
            	out1.println("</tr>");
			}
		} catch (Exception ex) {
			Logger.getLogger(Start.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
