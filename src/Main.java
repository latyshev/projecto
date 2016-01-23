

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet("/Main")
public class Main extends HttpServlet {
	private static final long serialVersionUID = 1L;
  
    public Main() {
        super();
    }


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html;charset=windows-1251");
		PrintWriter out1 = response.getWriter();
		request.getParameter("SourceData");
		int rowCount = 0;
		
		BufferedReader in = null;
		try {
		    in = new BufferedReader(new FileReader("/home/latyshev/workspace/projectx/results.txt"));
		    String read = null;

		    out1.println("<P ALIGN='center'><TABLE BORDER=1>");
		    while ((read = in.readLine()) != null) {
		        out1.println("<tr>");
		        String[] splited = read.split(" - ", 2);
		    	for (String part : splited) {
		            out1.println("<td>"+ part + "</td>");
		        }
		        out1.println("</tr>");
		    }
		    out1.println("</table>");
		} catch (IOException e) {
		    out1.println("There was a problem: " + e);
		    e.printStackTrace();
		} finally {
		    try {
		        in.close();
		    } catch (Exception e) {
		    }
		}
	}


}
