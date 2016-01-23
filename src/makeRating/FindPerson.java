package makeRating;

import static makeRating.TranslatePerson.*;
import java.util.ArrayList;
import java.util.HashSet;

public class FindPerson
{
    public HashSet<String> findPerson(String person, HashSet<String> data) 
    {   
        HashSet<String> goodNames = new HashSet<String>();
        
        //Разделяем ФИО на фамилию, имя, отчество
        String[] nameParts = person.split(" ");

	if ((nameParts.length > 3) || (nameParts.length < 1))
        {
            System.out.println ("Должно быть от одного до трех агрументов - фамилия, имя, отчество");
        }
        else
        {
            //Транслитерация фамилии, имени, отчества
            ArrayList<String>  transliteratedFio = new ArrayList<String>();
            for (String word : nameParts)
            {
                transliteratedFio.add(transliterate(word));
            }
            
            //Поиск соответствия по полным фамилии, имени, отчеству
            goodNames.addAll(search(transliteratedFio, data, false));
            
            //Если частей ФИО больше одной, то поиск по инициалам и поиск без отчества
            if (nameParts.length > 1)
            {
                //Поиск с инициалами
		ArrayList<String>  initialsFio = new ArrayList<String>();
                initialsFio.add(transliteratedFio.get(0));  //Полная фамилия
                initialsFio.add(transliterate(nameParts[1].substring(0,1)) + ".");  //Транслитерированная первая буква имени
                if (nameParts.length == 3) 
                    initialsFio.add(transliterate(nameParts[2].substring(0,1)) + "."); //Если есть, то транслитерированная первая буква отчества

                //Поиск соответствия по фамилии с инициалами
                goodNames.addAll(search(initialsFio, data, true)); 

		//Поиск по фамилии и имени
                ArrayList<String> surnameName = new ArrayList<String>();
                surnameName.add(transliteratedFio.get(0));
                surnameName.add(transliteratedFio.get(1));
                
                goodNames.addAll(search(surnameName, data, false)); 
            }
        }
        return goodNames;
    }
}