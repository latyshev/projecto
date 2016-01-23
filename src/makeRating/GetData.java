package makeRating;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GetData 
{
    //Получение URL на страницу с информацией об авторе на dblp
    public String getURL(String author) throws Exception
    {
        //Страница поиска по ФИО автора
        String address = "http://dblp.uni-trier.de/search/author/api?q="+author.replace(" ", "+")+"&h=1000&format=xml";
        
        String authorURL="";
        try 
        {
            //Подключение к сервису
            URL url = new URL(address);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            
            try
            {
                //Получение DOM-файла
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(conn.getInputStream());

                //Создание объекта XPath
                XPathFactory xPathfactory = XPathFactory.newInstance();
                XPath xpath = xPathfactory.newXPath();
                
                //Получение списка узлов info
                XPathExpression expr = xpath.compile("//*[name()='info']");
		NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
                
                Element elem;

                for (int i = 0; i < nl.getLength(); i++)
		{
                    //Для каждого узла info, если текст в тэге author равен искомому, то получение URL из тэга url 
                    elem = (Element) nl.item(i);
                    if(author.equalsIgnoreCase(elem.getElementsByTagName("author").item(0).getTextContent()))
                            authorURL = elem.getElementsByTagName("url").item(0).getTextContent();
                }
            }
            catch (Exception ex)
            {
                System.out.println("Error in getURL for: " + author);
                ex.printStackTrace();
            }
            finally
            {
		//Закрытие соединения
		try
		{
                    if(conn != null)
                    conn.disconnect();
		}
		catch (Exception e)
		{
                    System.out.println("Возникла ошибка при закрытии соединания");
                    e.printStackTrace();
		}
            }
        }
        catch (MalformedURLException ex) 
        {
            System.out.println("Error in getURL for: " + author);
            ex.printStackTrace();
        }
        return authorURL;
    }

    //Получение публикаций
    public ArrayList<String> getPublications(String address) throws Exception
    {
        ArrayList<String> issues = new ArrayList<String>();
        try 
        {
            //Подключение к странице с информацией об авторе, в формате xml
            URL url = new URL(address+".xml");
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            
            try
            {
                //Получение DOM-файла
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(conn.getInputStream());

                //Создание объекта XPath
                XPathFactory xPathfactory = XPathFactory.newInstance();
                XPath xpath = xPathfactory.newXPath();
                
                //Получение журналов и годов публикаций
                XPathExpression expr = xpath.compile("//*[name()='article']/*[name()='journal']/text()");
                XPathExpression exprYear = xpath.compile("//*[name()='article']/*[name()='year']/text()");
		
                NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
                NodeList nlYear = (NodeList) exprYear.evaluate(doc, XPathConstants.NODESET);

                //Год добавляем перед названием и добавляем журнал в список найденных публикаций
                for (int i = 0; i < nl.getLength(); i++)
		{ 
                    issues.add(nlYear.item(i).getNodeValue()+nl.item(i).getNodeValue());
                }
                
                //Получение книг, свзанных с конференциями и годов публикаций
                expr = xpath.compile("//*[name()='inproceedings']/*[name()='booktitle']/text()");
                exprYear = xpath.compile("//*[name()='inproceedings']/*[name()='year']/text()");
                
		nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
                nlYear = (NodeList) exprYear.evaluate(doc, XPathConstants.NODESET);
                
                for (int i = 0; i < nl.getLength(); i++)
		{ 
                    issues.add(nlYear.item(i).getNodeValue()+nl.item(i).getNodeValue());
                }
            }
            catch (Exception ex)
            {
                System.out.println("Error in getPublications for: " + address);
                ex.printStackTrace();
            }
            finally
            {
		//Закрытие соединения
		try
		{
                    if(conn != null)
                    conn.disconnect();
		}
		catch (Exception e)
		{
                    System.out.println("Возникла ошибка при закрытии соединания");
                    e.printStackTrace();
		}
            }
        }
        catch (MalformedURLException ex) 
        {
            System.out.println("Error in getPublications for: " + address);
            ex.printStackTrace();
        }
        return issues;
    } 	 
    
    //Получение SJR
    public double getSJR(String title, int publishYear) throws Exception
    {
        double SJR = -1;
        String journalURL = "";
        
        //Подключение к странице поиска по названию журнала
        org.jsoup.nodes.Document doc = Jsoup.connect("http://www.scimagojr.com/journalsearch.php?q="+title.replace(" ", "+")+ "&tip=jou&exact=yes").timeout(5*60*1000).get();

        //Получение ссылки на страницу о журнале
        Elements results = (doc.getElementById("derecha_contenido")).select("p a");
        for (org.jsoup.nodes.Element result : results)
        { 
            if (title.equalsIgnoreCase(result.getElementsByTag("strong").text()))
            {
                journalURL = result.attr("href");
                break;
            }
        }
                
        //Если журнал есть на сайте (ссылка найдена), извлекаем SJR
        if (journalURL.length() > 0)
        {
            //Подключение к странице о журнале
            doc = Jsoup.connect("http://www.scimagojr.com/" + journalURL).timeout(5*60*1000).get();
            
            //Получение списка годов из таблицы с рейтингами
            Elements years = (doc.getElementById("grupo_data")).getElementsByClass("tabla_datos").select("thead tr th");
            
            //Поиск года публикации. Если его нет, то берется последний
            int i=1;
            while (i<(years.size()-1)) 
            {
                if (publishYear == Double.parseDouble(years.get(i).text())) break;
                else i++;
            }
            
            //Получаем список значений SJR
            results = (doc.getElementById("grupo_data")).getElementsByClass("tabla_datos").select("tbody tr:eq(0)");
            //Если для года публикации нет рейтинга, то берем последний из имеющихся
            if (results.select("td:eq("+i+")").get(0).text().equals("-"))
            {
                for (int j=(years.size()-1); j>0; j--)
                    if (!(results.select("td:eq("+j+")").get(0).text().equals("-")))
                    {
                        SJR = Double.parseDouble(results.select("td:eq("+j+")").get(0).text().replace(",", "."));
                        break;
                    }
            }
            //иначе берем рейтинг за год публикации
            else
                SJR = Double.parseDouble(results.select("td:eq("+i+")").get(0).text().replace(",", "."));
        }
        return SJR;
    }
}