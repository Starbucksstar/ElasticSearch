package com.star.es.service.util;

public class StringUtil {
    public StringUtil() {
    }

    public static String substringBeforeLast(String str, String formater) {
        return str.substring(0, str.indexOf(formater));
    }

    public static String substringAfterLast(String str, String formater) {
        return str.substring(str.indexOf(formater) + 1, str.length());
    }

    public static void main(String[] arg) {
        System.out.println(substringBeforeLast("127.0.0.1:8080", ":"));
        System.out.println(substringAfterLast("127.0.0.1:8080", ":"));
    }
}

