package com.easyjava.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class PropertiesUtils {
    private static Properties props=new Properties();

    private static Map<String,String> properMap=new HashMap<>();

    static {
        InputStream input=null;
        try{
            input=PropertiesUtils.class.getClassLoader().getResourceAsStream("./application.properties");
            props.load(input);

            Iterator<Object> iterator = props.keySet().iterator();
            while (iterator.hasNext()){
                String key=(String)iterator.next();
                properMap.put(key,(String)props.get(key));
            }
        }catch (Exception e){

        }finally {
            if (input!=null){
                try {
                    input.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static String getString(String key){
        return properMap.get(key);
    }

    public static void main(String[] args) {
        System.out.println(getString("db.url"));
    }
}
