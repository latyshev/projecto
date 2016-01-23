package makeRating;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Arrays;

public class TranslatePerson
{
    //Транслитерированные буквы русского алфавита, "ё" в кодировке после остальных, поэтому в конце
    static String[] letterTranslations = {
            "a", "b", "v", "g", "d", "e", "zh", "z", "i", "j", "k", "l", "m", "n", 
        "o", "p", "r", "s", "t", "u", "f", "h", "c", "ch", "sh", "shch", "\"", "y", "'", "e", "yu", "ya", "yo"
    };
	
    //Транслитерация
    public static String transliterate (String word)
    {
        StringBuilder builder =  new StringBuilder();
        
        //Разбиение слова на буквы
        char[] fio = (word.toLowerCase()).toCharArray();
        
        //Для каждой буквы
        for (int i = 0; i < fio.length; i++)
        {
            //Если "ё"
            if ((int)fio[i] == 1105)
                builder.append(letterTranslations[32]);
            else
                //Если буква кириллицы, то берем соответствие из списка транслитерации, иначе не транслитерируем
                if (((int)fio[i] >= 1072) && ((int)fio[i] <= 1103))
                    builder.append(letterTranslations[(int)fio[i]-1072]);
                else builder.append(fio[i]);
        }
        return builder.toString();
    }
       
    //Рассчет дистанции редактирования (расстояния Левенштейна) между двумя строками
    private static int editDistance(String word1, String word2)
    {
        int m = word1.length(), n = word2.length();
        int[] D1;
        int[] D2 = new int[n + 1];

        for(int i = 0; i <= n; i ++)
            D2[i] = i;

        for(int i = 1; i <= m; i ++) 
        {
            D1 = D2;
            D2 = new int[n + 1];
            
            for(int j = 0; j <= n; j ++) 
            {
                if(j == 0) D2[j] = i;
                else 
                {
                    int cost = (word1.charAt(i - 1) != word2.charAt(j - 1)) ? 1 : 0;
                    if(D2[j - 1] < D1[j] && D2[j - 1] < D1[j - 1] + cost)
                        D2[j] = D2[j - 1] + 1;
                    else if(D1[j] < D1[j - 1] + cost)
                        D2[j] = D1[j] + 1;
                    else
                        D2[j] = D1[j - 1] + cost;
                }
            }
        }
        return D2[n];
    }
    
    //Поиск соответствия имени. На вход - части ФИО, список авторов dblp, булевая переменная - поиск по инициалам или нет
    public static HashSet<String> search (ArrayList<String> fio, HashSet<String> names, boolean initials)
    {
        HashSet<String> results = new HashSet<String>();
        
        //Список под комбинации расположения фамилии, имени, отчетва
        ArrayList<String> fullFIOs = new ArrayList<String>();
            
        //Если частей ФИО одна, то комбинация только одна, иначе составление возможных комбинаций из частей    
        if (fio.size() == 1)
            fullFIOs.add(fio.get(0));
        else
            for (String fio1 : fio)
            {
                for (String fio2 : fio)
                {
                   if (fio.size() == 2)
                        if ((fio1 != fio2))
                        {
                            fullFIOs.add(fio1 + " " + fio2);
                        }
                    
                    if (fio.size() == 3) 
                    {
                        for (String fio3 : fio)
                        {
                            if ((fio1 != fio2) && (fio1 != fio3) && (fio2 != fio3))
                            {
                                fullFIOs.add(fio1 + " " + fio2 + " " + fio3);
                            }
                        }
                    }
               }
            }
        
        //Для каждой комбинации ФИО поиск подходящих имен в списке авторов
        for (String fullFIO : fullFIOs)
        {
            //Сравнивая ФИО с каждым автором
            for (String person : names)
            { 
                person = person.toLowerCase();
                
                //Если дистанция редактирования между искомым ФИО и ФИО автора на dblp 
                //В зависимости от длины искомого ФИО определяем, какой порог расстояния редактирования, чтобы выявлять подходящие соответствия
                double limit = (fullFIO.length() > 20) ? fullFIO.length() * 0.2 : (fullFIO.length() > 10) ? 4 : 1;
                if(editDistance(fullFIO, person) <= limit)
                {
                    boolean ok = true;
                    
                    //Если поиск по инициалам, то они должны совпадать точно или отличаться количеством букв (Ц = C или Ts)
                    if (initials)
                    {
                        //Разбиение искомых ФИО и ФИО с dblp на части
                        ArrayList<String> fullFIOParts = new ArrayList<>(Arrays.asList(fullFIO.split(" ")));
                        ArrayList<String> personFIOParts = new ArrayList<>(Arrays.asList(person.split(" ")));
                        
                        //Для каждой части
                        for (int i=0; i<fullFIOParts.size(); i++)
                        {
                            //Если инициал
                            if ((fullFIOParts.get(i).length() == 2) || (fullFIOParts.get(i).length()== 3))
                            {
                                //Если одинаковое количество частей ФИО, то если не полное совпадение и одинаковая длина инициалов, убрать из списка подходящих имен
                                if (fullFIOParts.size() == personFIOParts.size())
                                {
                                    if (!((fullFIOParts.get(i).equalsIgnoreCase(personFIOParts.get(i))) || (Math.abs(fullFIOParts.get(i).length() - personFIOParts.get(i).length()) == 1)))
                                        ok = false;
                                }
                                //Если разное количество частей ФИО, то инициал искомого ФИО должен содержаться в ФИО на dblp
                                else if (!(personFIOParts.contains(fullFIOParts.get(i))))
                                    ok = false;
                            }  
                        }
                    }
                    
                    if (ok)
                        results.add(person);
                }
            }
        }
        return results;
    }
}