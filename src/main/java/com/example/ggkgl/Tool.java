package com.example.ggkgl;

import com.csvreader.CsvReader;
import jdk.internal.dynalink.beans.StaticClass;
import net.sf.json.JSONArray;
import org.apache.tomcat.util.bcel.Const;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Tool {
    public static void main(String args[])
    {
        try {
//            Charset charset=Charset.forName("utf-8");
            List<HashMap> maps=new ArrayList<>();
            CsvReader csvReader=new CsvReader("AcademicianEngineering.csv",',',Charset.forName("utf-8"));
            csvReader.readRecord();
            String [] titles=csvReader.getValues();
            System.out.println(titles[0]);
            for(char c:titles[0].toCharArray())
            {
                System.out.println(c);
            }
            if (titles[0].equals("XB"))
                System.out.println("yes");
            else {
                String str="XB";
                System.out.println(str.length());
                System.out.println(titles[0].length());
                System.out.println("?????");
            }
            while(csvReader.readRecord())
            {
                HashMap map=new HashMap();
                for(int i=0;i<csvReader.getValues().length;i++)
                {
                    map.put(titles[i],csvReader.getValues()[i]);
                }
                maps.add(map);
                JSONArray jsonArray=JSONArray.fromObject(maps);
                BufferedWriter bufferedWriter=new BufferedWriter(new FileWriter("json.txt"));
                bufferedWriter.write(jsonArray.toString());
                bufferedWriter.flush();
                bufferedWriter.close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}
