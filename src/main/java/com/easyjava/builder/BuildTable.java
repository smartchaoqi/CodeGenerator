package com.easyjava.builder;

import com.easyjava.bean.Constants;
import com.easyjava.bean.FieldInfo;
import com.easyjava.bean.TableInfo;
import com.easyjava.utils.JsonUtils;
import com.easyjava.utils.PropertiesUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildTable {
    private static Connection conn=null;

    public static final String SQL_SHOW_TABLE_STATUS="show table status";

    public static final String SQL_SHOW_TABLE_FIELDS="show full FIELDS from %s";

    public static final String SQL_SHOW_TABLE_INDEX="show index from %s";

    public static final Logger log= LoggerFactory.getLogger(BuildTable.class);

    static {
        String driverName= PropertiesUtils.getString("db.driver.name");
        String url= PropertiesUtils.getString("db.url");
        String username= PropertiesUtils.getString("db.username");
        String password= PropertiesUtils.getString("db.password");

        try {
            Class.forName(driverName);
            conn= DriverManager.getConnection(url,username,password);
        } catch (Exception e) {
            log.error("数据库连接失败",e);
        }
    }

    public static List<TableInfo> getTables(){
        PreparedStatement ps=null;
        ResultSet set=null;

        List<TableInfo> tableInfoList=new ArrayList<>();

        try {
            ps = conn.prepareStatement(SQL_SHOW_TABLE_STATUS);
            set = ps.executeQuery();
            while (set.next()){
                String tableName = set.getString("name");
                String comment = set.getString("comment");
//                log.info("tableName:{},comment:{}",tableName,comment);

                TableInfo tableInfo = new TableInfo();
                String beanName=tableName;
                if (Constants.IGNORE_TABLE_PREFIX){
                    beanName=beanName.substring(beanName.indexOf("_")+1);
                }
                beanName = processFiled(beanName, true);

                tableInfo.setTableName(tableName);
                tableInfo.setBeanName(beanName);
                tableInfo.setBeanParamName(beanName +Constants.SUFFIX_BEAN_QUERY);
                tableInfo.setComment(comment);

                readFieldInfo(tableInfo);

                getKeyIndexInfo(tableInfo);

                log.info(JsonUtils.convertObj2Json(tableInfo));

                tableInfoList.add(tableInfo);
            }

            return tableInfoList;
        } catch (SQLException e) {
            log.error("读取索引失败",e);
        }finally {
            if (ps!=null) {
                try {
                    ps.close();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
            if (set!=null){
                try {
                    set.close();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }

            if (conn!=null){
                try {
                    conn.close();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        return tableInfoList;
    }

    public static void readFieldInfo(TableInfo tableInfo){
        PreparedStatement ps=null;
        ResultSet set=null;

        List<FieldInfo> fieldInfos=new ArrayList<>();
        List<FieldInfo> fieldExtendList=new ArrayList<>();

        try {
            ps = conn.prepareStatement(String.format(SQL_SHOW_TABLE_FIELDS,tableInfo.getTableName()));
            set = ps.executeQuery();
            while (set.next()){
                FieldInfo fieldInfo = new FieldInfo();
                String field = set.getString("Field");
                String comment = set.getString("Comment");
                String type = set.getString("type");
                String extra = set.getString("extra");
                if (type.indexOf("(")>0){
                    type=type.substring(0,type.indexOf("("));
                }
                String propertyName = processFiled(field,false);

                fieldInfo.setFieldName(field);
                fieldInfo.setComment(comment);
                fieldInfo.setSqlType(type);
                fieldInfo.setAutoIncrement("auto_increment".equalsIgnoreCase(extra));
                fieldInfo.setPropertyName(propertyName);
                fieldInfo.setJavaType(processJavaType(type));
                log.info("field:{},comment:{},type:{},extra:{},propertyName:{}",field,comment,type,extra,propertyName);

                if (ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES,type)){
                    tableInfo.setHasDateTime(true);
                }

                if (ArrayUtils.contains(Constants.SQL_DATE_TYPES,type)){
                    tableInfo.setHasDate(true);
                }

                if (ArrayUtils.contains(Constants.SQL_DECIMAL_TYPE,type)){
                    tableInfo.setHasBigDecimal(true);
                }

                if (ArrayUtils.contains(Constants.SQL_STRING_TYPE,fieldInfo.getSqlType())){
                    FieldInfo fieldInfoFuzzy = new FieldInfo();
                    fieldInfoFuzzy.setJavaType(fieldInfo.getJavaType());
                    fieldInfoFuzzy.setPropertyName(fieldInfo.getPropertyName()+Constants.SUFFIX_BEAN_QUERY_FUZZY);
                    fieldInfoFuzzy.setFieldName(fieldInfo.getFieldName());
                    fieldInfoFuzzy.setSqlType(type);
                    fieldExtendList.add(fieldInfoFuzzy);
                }

                if (ArrayUtils.contains(Constants.SQL_DATE_TYPES,fieldInfo.getSqlType())||ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES,fieldInfo.getSqlType())){
                    FieldInfo fieldInfoTimeStart = new FieldInfo();
                    fieldInfoTimeStart.setJavaType("String");
                    fieldInfoTimeStart.setFieldName(fieldInfo.getFieldName());
                    fieldInfoTimeStart.setPropertyName(fieldInfo.getPropertyName()+Constants.SUFFIX_BEAN_QUERY_TIME_START);
                    fieldInfoTimeStart.setSqlType(type);

                    FieldInfo fieldInfoTimeEnd = new FieldInfo();
                    fieldInfoTimeEnd.setJavaType("String");
                    fieldInfoTimeEnd.setFieldName(fieldInfo.getFieldName());
                    fieldInfoTimeEnd.setPropertyName(fieldInfo.getPropertyName()+Constants.SUFFIX_BEAN_QUERY_TIME_END);
                    fieldInfoTimeEnd.setSqlType(type);

                    fieldExtendList.add(fieldInfoTimeStart);
                    fieldExtendList.add(fieldInfoTimeEnd);
                }

                fieldInfos.add(fieldInfo);
            }

            tableInfo.setFieldList(fieldInfos);
            tableInfo.setFieldExtendList(fieldExtendList);
        } catch (SQLException e) {
            log.error("读取表失败",e);
        }finally {
            if (ps!=null) {
                try {
                    ps.close();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
            if (set!=null){
                try {
                    set.close();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    public static void getKeyIndexInfo(TableInfo tableInfo){
        PreparedStatement ps=null;
        ResultSet set=null;

        try {
            ps = conn.prepareStatement(String.format(SQL_SHOW_TABLE_INDEX,tableInfo.getTableName()));
            set = ps.executeQuery();

            Map<String,FieldInfo> tempMap=new HashMap<>();
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                tempMap.put(fieldInfo.getFieldName(),fieldInfo);
            }
            Map<String, List<FieldInfo>> keyIndexMap = tableInfo.getKeyIndexMap();
            while (set.next()){
                String keyName = set.getString("Key_name");
                String columnName = set.getString("Column_name");
                int nonUnique = set.getInt("Non_unique");

                if (nonUnique==1){
                    continue;
                }

                if (!keyIndexMap.containsKey(keyName)){
                    keyIndexMap.put(keyName,new ArrayList<>());
                }

                List<FieldInfo> fieldInfos = keyIndexMap.get(keyName);
                FieldInfo fieldInfoByName = tempMap.get(columnName);
                fieldInfos.add(fieldInfoByName);

            }
        } catch (SQLException e) {
            log.error("读取表失败",e);
        }finally {
            if (ps!=null) {
                try {
                    ps.close();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
            if (set!=null){
                try {
                    set.close();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }

        }
    }

    private static String processFiled(String field,boolean updateFirstLetter){
        StringBuilder sb = new StringBuilder();
        String[] strings = field.split("_");
        char[] first = strings[0].toCharArray();
        if (updateFirstLetter){
            first[0]=(char)(first[0]-'a'+'A');
        }
        sb.append(new String(first));

        for (int i = 1; i < strings.length; i++) {
            char[] array = strings[i].toCharArray();
            array[0]=(char)(array[0]-'a'+'A');
            sb.append(new String(array));
        }
        return sb.toString();
    }

    private static String processJavaType(String type){
        if (ArrayUtils.contains(Constants.SQL_INTEGER_TYPE,type)){
            return "Integer";
        }else if (ArrayUtils.contains(Constants.SQL_DECIMAL_TYPE,type)){
            return "BigDecimal";
        }else if(ArrayUtils.contains(Constants.SQL_LONG_TYPE,type)){
            return "Long";
        }else if (ArrayUtils.contains(Constants.SQL_DATE_TYPES,type)||ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES,type)){
            return "Date";
        }else if (ArrayUtils.contains(Constants.SQL_STRING_TYPE,type)){
            return "String";
        }else{
            throw new RuntimeException("无法识别类型"+type);
        }
    }
}
