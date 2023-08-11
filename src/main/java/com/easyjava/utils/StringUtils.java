package com.easyjava.utils;

public class StringUtils {
    public static String upperCaseFirstLetter(String s){
        char[] array = s.toCharArray();
        array[0]=(char)(array[0]-'a'+'A');
        return new String(array);
    }

    public static String lowerCaseFirstLetter(String s){
        char[] array = s.toCharArray();
        array[0]=(char)(array[0]-'A'+'a');
        return new String(array);
    }
}
