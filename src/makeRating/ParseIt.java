package makeRating;

import java.io.File;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.util.HashSet;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class ParseIt {
   public static int n = 0;
  
   public static HashSet<String> parsing(){
      try { 

         File inputFile = new File("dblp.xml");
         SAXParserFactory factory = SAXParserFactory.newInstance();
         SAXParser saxParser = factory.newSAXParser();
	
         UserHandler userhandler = new UserHandler();
         saxParser.parse(inputFile, userhandler);    
         
         return userhandler.list;
      
	  }
	  catch (Exception e) {
         e.printStackTrace();
		}
      return new HashSet<String>();
   }   
}

class UserHandler extends DefaultHandler {

   HashSet<String> list = new HashSet<String>();

   boolean bAuthor = false;

   @Override
   public void startElement(String uri, 
      String localName, String qName, Attributes attributes)
         throws SAXException {

      if (qName.equalsIgnoreCase("article")) {

      } else if (qName.equalsIgnoreCase("author")) {
            bAuthor = true;
      }
   }

   @Override
   public void endElement(String uri, 
      String localName, String qName) throws SAXException {
      if (qName.equalsIgnoreCase("article")) {

      }
   }

   @Override
   public void characters(char ch[], 
      int start, int length) throws SAXException {

      String value = new String(ch, start, length).trim();

      if (bAuthor) {
        list.add(value);
        bAuthor = false;
      }

   }
}