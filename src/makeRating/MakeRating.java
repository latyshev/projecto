package makeRating;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MakeRating
{
    static HashMap resultTotal = new HashMap();
    
    public HashMap calculationsInThreads(HashSet<String> persons, HashSet<String> data)
    {
        ArrayList<String> listPersons = new ArrayList<String>(persons);
        
        //Получаем ExecutorService утилитного класса Executors с размером пула потоков равному 10
        ExecutorService executor = Executors.newFixedThreadPool(10);
        //Создаем список с Future, которые ассоциированы с Callable
        ArrayList<Future<HashMap>> listFuture = new ArrayList<Future<HashMap>>();
        
        //Для каждого сотрудника создаем отдельный поток и добавляем в список Future
        for(int i=0; i < listPersons.size(); i++)
        {
            Future<HashMap> future = executor.submit(new PersonThread(listPersons.get(i), data));
            listFuture.add(future);
        }
        
        //Для каждой задачи списка Future получаем результат, который добавляем в итоговый список результатов
        for(Future<HashMap> fut : listFuture)
        {
            try 
            {
                resultTotal.putAll(fut.get());
            } 
            catch (InterruptedException | ExecutionException e) 
            {
                e.printStackTrace();
            }
        }
        return resultTotal;
    }
}

//Класс для поточного выполнения расчетов SJR
class PersonThread implements Callable
{
    //ФИО сотрудника, для которого выполняется расчет
    private String personName;
    private String personURI = "http://lod.ifmo.ru/resource/Person";
        
    //Список авторов, среди которых ищется соответствие
    private HashSet<String> data = new HashSet<String>();
        
    public PersonThread(String person, HashSet<String> data1)
    {
        String [] idAndName = person.split(";");
        data = data1;
        personURI += idAndName[0];
        personName = idAndName[1];
    }
        
    @Override
    public HashMap call() 
    {
        return calculations(personURI, personName, data);
    }
        
    //Все вычисления. На вход - ФИО сотрудниа и список авторов
    public HashMap calculations(String personId, String personName, HashSet<String> data)
    {
        HashMap result = new HashMap();

        try
        {
            //Переменные дл последующего рассчета среднего ариметическго SJR
            int journalsWithSJR = 0;
            double sumSJR = 0;
            double currentSJR = 0;
            double SJR = 0;
            
            //Получение спика имен с dblp, которые соответствуют ФИО сотрудника
            FindPerson finder = new FindPerson();
            HashSet<String> names = finder.findPerson(personName, data);

            //Для каждого из списка подходящих имен с dblp получаем SJR публикаций
            for (String name : names)
            {
                try 
                {
                    //Получение URL на информацию об авторе
                    GetData getter = new GetData();
                    String url = getter.getURL(name);
                        
                    //Если URL есть, то выполняем дальше (URL нет, если ФИО хранится как "aka" другого автора, поэтому необходима проверка)
                    if (url.length() > 0)
                    {
                        //Получение списка публикаций (статей и сборников конференций)
                        ArrayList<String> journals = getter.getPublications(url);
                            
                        //Для каждой публикации ищем SJR
                        for(String journal: journals)
                        {
                            //При получении публикаций год публикации дописывается в возвращаемое название, здесь извлекается
                            int year = 0;
                            try 
                            {
                                year = Integer.parseInt(journal.substring(0,4));
                            }
                            catch (Exception ex)
                            {}
                       
                            //Получение SJR
                            currentSJR = getter.getSJR(journal.substring(4), year);
                                
                            //Если SJR найден, то изменяем переменные для рассчета среднего арифметического
                            if (currentSJR != -1)
                            {
                                sumSJR += currentSJR;
                                journalsWithSJR++;
                            }
                        }
                    }
                } 
                catch (Exception ex) 
                {
                    System.out.println("Error for: " + personName);
                    ex.printStackTrace();
                }
            }
                
            //Если у сотрудника есть публикации, для которых был найден SJR, то рассчитываем среднее арифметическое
            if (journalsWithSJR > 0) 
            {
                SJR = sumSJR/journalsWithSJR;
            }
                
            //Добавление результата в HashMap: ключ - ФИО сотрудника, значение - итоговый рейтинг SJR
            result.put(personId+" , "+personName, SJR);
        } 
        catch (Exception ex) 
        {
            Logger.getLogger(MakeRating.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
}