package com.easyjava.bean;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TableInfo {
    private String tableName;

    private String beanName;

    private String beanParamName;

    private String comment;

    private List<FieldInfo> fieldList;

    private List<FieldInfo> fieldExtendList;

    private Map<String,List<FieldInfo>> keyIndexMap=new LinkedHashMap<>();

    private Boolean hasDate;

    private Boolean hasDateTime;

    private Boolean hasBigDecimal;

    public TableInfo(){
        this.hasDate=false;
        this.hasBigDecimal=false;
        this.hasDateTime=false;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getBeanParamName() {
        return beanParamName;
    }

    public void setBeanParamName(String beanParamName) {
        this.beanParamName = beanParamName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<FieldInfo> getFieldList() {
        return fieldList;
    }

    public void setFieldList(List<FieldInfo> fieldList) {
        this.fieldList = fieldList;
    }

    public Map<String, List<FieldInfo>> getKeyIndexMap() {
        return keyIndexMap;
    }

    public void setKeyIndexMap(Map<String, List<FieldInfo>> keyIndexMap) {
        this.keyIndexMap = keyIndexMap;
    }

    public Boolean getHasDate() {
        return hasDate;
    }

    public void setHasDate(Boolean hasDate) {
        this.hasDate = hasDate;
    }

    public Boolean getHasDateTime() {
        return hasDateTime;
    }

    public void setHasDateTime(Boolean hasDateTime) {
        this.hasDateTime = hasDateTime;
    }

    public Boolean getHasBigDecimal() {
        return hasBigDecimal;
    }

    public void setHasBigDecimal(Boolean hasBigDecimal) {
        this.hasBigDecimal = hasBigDecimal;
    }

    public List<FieldInfo> getFieldExtendList() {
        return fieldExtendList;
    }

    public void setFieldExtendList(List<FieldInfo> fieldExtendList) {
        this.fieldExtendList = fieldExtendList;
    }
}
