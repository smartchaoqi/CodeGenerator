package com.easyjava.bean;

import com.easyjava.utils.PropertiesUtils;

import java.util.Arrays;
import java.util.List;

public class Constants {
    public static String AUTHOR_COMMENT;
    public static Boolean IGNORE_TABLE_PREFIX;
    public static String SUFFIX_BEAN_QUERY;

    //需要忽略的属性
    public static List<String> IGNORE_BEAN_TOJSON_FILED;
    public static String IGNORE_BEAN_TOJSON_EXPRESSION;
    public static String IGNORE_BEAN_TOJSON_CLASS;
    //日期格式化
    public static String BEAN_DATE_FORMAT_EXPRESSION;
    public static String BEAN_DATE_FORMAT_CLASS;

    public static String BEAN_DATE_UNFORMAT_EXPRESSION;
    public static String BEAN_DATE_UNFORMAT_CLASS;
    public static String PATH_BASE;
    public static String PACKAGE_BASE;
    public static String PATH_PO;
    public static String PATH_VO;
    public static String PATH_MAPPER;
    public static String PATH_SERVICE;
    public static String PATH_SERVICE_IMPL;
    public static String PATH_QUERY;
    public static String PACKAGE_PO;
    public static String PACKAGE_VO;
    public static String PACKAGE_QUERY;
    public static String PACKAGE_MAPPER;
    public static String PACKAGE_SERVICE;
    public static String PACKAGE_EXCEPTION;
    public static String PACKAGE_SERVICE_IMPL;
    public static String PACKAGE_UTILS;

    public static String PATH_UTILS;
    public static String PATH_EXCEPTION;
    public static String PACKAGE_ENUMS;
    public static String PACKAGE_CONTROLLER;
    public static String PATH_ENUMS;
    public static String PATH_CONTROLLER;
    private static String PATH_JAVA="java";

    private static String PATH_RESOURCES="resources";

    public static String SUFFIX_BEAN_QUERY_FUZZY;
    public static String SUFFIX_BEAN_QUERY_TIME_START;
    public static String SUFFIX_BEAN_QUERY_TIME_END;
    public static String SUFFIX_MAPPER;
    public static String PATH_MAPPER_XMLS;

    static {
        SUFFIX_BEAN_QUERY_FUZZY=PropertiesUtils.getString("suffix.bean.query.fuzzy");
        SUFFIX_BEAN_QUERY_TIME_START=PropertiesUtils.getString("suffix.bean.query.time.start");
        SUFFIX_BEAN_QUERY_TIME_END=PropertiesUtils.getString("suffix.bean.query.time.end");
        SUFFIX_MAPPER=PropertiesUtils.getString("suffix.mapper");

        IGNORE_TABLE_PREFIX = Boolean.valueOf(PropertiesUtils.getString("ignore.table.prefix"));
        SUFFIX_BEAN_QUERY = PropertiesUtils.getString("suffix.bean.query");

        AUTHOR_COMMENT=PropertiesUtils.getString("author.comment");

        PACKAGE_BASE=PropertiesUtils.getString("package.base");
        PACKAGE_PO=PACKAGE_BASE+"."+PropertiesUtils.getString("package.po");
        PACKAGE_CONTROLLER=PACKAGE_BASE+"."+PropertiesUtils.getString("package.controller");
        PACKAGE_EXCEPTION=PACKAGE_BASE+"."+PropertiesUtils.getString("package.exception");
        PACKAGE_VO=PACKAGE_BASE+"."+PropertiesUtils.getString("package.vo");
        PACKAGE_QUERY=PACKAGE_BASE+"."+PropertiesUtils.getString("package.query");
        PACKAGE_UTILS=PACKAGE_BASE+"."+PropertiesUtils.getString("package.utils");
        PACKAGE_ENUMS=PACKAGE_BASE+"."+PropertiesUtils.getString("package.enums");
        PACKAGE_MAPPER=PACKAGE_BASE+"."+PropertiesUtils.getString("package.mapper");
        PACKAGE_SERVICE=PACKAGE_BASE+"."+PropertiesUtils.getString("package.service");
        PACKAGE_SERVICE_IMPL=PACKAGE_BASE+"."+PropertiesUtils.getString("package.service.impl");

        PATH_BASE=PropertiesUtils.getString("path.base");
        PATH_BASE=(PATH_BASE+PATH_JAVA+"/"+PACKAGE_BASE).replace(".","/");

        PATH_PO=(PATH_BASE+"/"+PropertiesUtils.getString("package.po")).replace(".","/");
        PATH_EXCEPTION=(PATH_BASE+"/"+PropertiesUtils.getString("package.exception")).replace(".","/");
        PATH_CONTROLLER=(PATH_BASE+"/"+PropertiesUtils.getString("package.controller")).replace(".","/");
        PATH_VO=(PATH_BASE+"/"+PropertiesUtils.getString("package.vo")).replace(".","/");
        PATH_QUERY=(PATH_BASE+"/"+PropertiesUtils.getString("package.query")).replace(".","/");
        PATH_UTILS=(PATH_BASE+"/"+PropertiesUtils.getString("package.utils")).replace(".","/");
        PATH_ENUMS=(PATH_BASE+"/"+PropertiesUtils.getString("package.enums")).replace(".","/");
        PATH_MAPPER=(PATH_BASE+"/"+PropertiesUtils.getString("package.mapper")).replace(".","/");
        PATH_SERVICE=(PATH_BASE+"/"+PropertiesUtils.getString("package.service")).replace(".","/");
        PATH_SERVICE_IMPL=(PATH_BASE+"/"+PropertiesUtils.getString("package.service.impl")).replace(".","/");

        //需要忽略的属性
        IGNORE_BEAN_TOJSON_FILED= Arrays.asList(PropertiesUtils.getString("ignore.bean.tojson.filed").split(","));
        IGNORE_BEAN_TOJSON_EXPRESSION=PropertiesUtils.getString("ignore.bean.tojson.expression");
        IGNORE_BEAN_TOJSON_CLASS=PropertiesUtils.getString("ignore.bean.tojson.class");
        //日期格式化
        BEAN_DATE_FORMAT_EXPRESSION=PropertiesUtils.getString("bean.date.format.expression");
        BEAN_DATE_FORMAT_CLASS=PropertiesUtils.getString("bean.date.format.class");

        BEAN_DATE_UNFORMAT_EXPRESSION=PropertiesUtils.getString("bean.date.unformat.expression");
        BEAN_DATE_UNFORMAT_CLASS=PropertiesUtils.getString("bean.date.unformat.class");

        PATH_MAPPER_XMLS=PropertiesUtils.getString("path.base")+PATH_RESOURCES+"/"+PACKAGE_MAPPER.replace(".","/");
    }


    public static final String[] SQL_DATE_TIME_TYPES = {"datetime", "timestamp"};

    public final static String[] SQL_DATE_TYPES = {"date"};

    public static final String[] SQL_DECIMAL_TYPE = {"decimal", "double", "float"};

    public static final String[] SQL_STRING_TYPE = {"char", "varchar", "text", "mediumtext", "longtext"};

    public static final String[] SQL_INTEGER_TYPE = {"int", "tinyint"};

    public static final String[] SQL_LONG_TYPE = {"bigint"};

    public static void main(String[] args) {
//        System.out.println(PACKAGE_BASE);
//        System.out.println(PATH_BASE);
//        System.out.println(PATH_PO);
//        System.out.println(PATH_PARAM);
//        System.out.println(PACKAGE_PO);
//
//        System.out.println(PATH_UTILS);
//        System.out.println(PACKAGE_UTILS);
        System.out.println(PACKAGE_VO);
    }
}
