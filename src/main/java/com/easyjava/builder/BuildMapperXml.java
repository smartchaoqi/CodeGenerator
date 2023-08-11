package com.easyjava.builder;

import com.easyjava.bean.Constants;
import com.easyjava.bean.FieldInfo;
import com.easyjava.bean.TableInfo;
import com.easyjava.utils.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BuildMapperXml {
    private static final Logger log = LoggerFactory.getLogger(BuildMapperXml.class);
    private static final String BASE_RESULT_MAP="base_result_map";
    private static final String BASE_COLUMN_LIST="base_column_list";
    private static final String BASE_CONDITION_FILED="base_query_condition";
    private static final String QUERY_CONDITION ="query_condition";
    private static final String BASE_QUERY_CONDITION_EXTEND="base_query_condition_extend";

    public static void execute(TableInfo tableInfo) {
        File folder = new File(Constants.PATH_MAPPER_XMLS);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String className = tableInfo.getBeanName() + Constants.SUFFIX_MAPPER;

        File poFile = new File(folder, className + ".xml");
        OutputStream out = null;
        OutputStreamWriter outw = null;
        BufferedWriter bw = null;

        try {
            out = Files.newOutputStream(poFile.toPath());
            outw = new OutputStreamWriter(out, StandardCharsets.UTF_8);
            bw = new BufferedWriter(outw);

            bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                    "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\"\n" +
                    "        \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">");
            bw.newLine();
            bw.write(String.format("<mapper namespace=\"%s\">",Constants.PACKAGE_MAPPER+"."+className));
            bw.newLine();

            bw.write("    <!--实体映射-->");
            bw.newLine();
            String poClass=Constants.PACKAGE_PO+"."+tableInfo.getBeanName();

            bw.write(String.format("    <resultMap id=\"%s\" type=\"%s\">\n",BASE_RESULT_MAP,poClass));

            FieldInfo idField=null;
            for (Map.Entry<String, List<FieldInfo>> entry : tableInfo.getKeyIndexMap().entrySet()) {
                if ("PRIMARY".equals(entry.getKey())){
                    if (entry.getValue().size()==1){
                        idField=entry.getValue().get(0);
                        break;
                    }
                }
            }

            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                bw.write(String.format("        <!--%s-->\n",fieldInfo.getComment()));
                if (idField!=null&&fieldInfo.getPropertyName().equals(idField.getPropertyName())){
                    bw.write(String.format("        <id column=\"%s\" property=\"%s\"/>\n",fieldInfo.getFieldName(),fieldInfo.getPropertyName()));
                }else{
                    bw.write(String.format("        <result column=\"%s\" property=\"%s\"/>\n",fieldInfo.getFieldName(),fieldInfo.getPropertyName()));
                }
            }
            bw.write("    </resultMap>\n\n");

            bw.write("    <!--通用查询结果列-->\n");
            bw.write(String.format("    <sql id=\"%s\">\n",BASE_COLUMN_LIST));
            StringBuilder baseColumnField=new StringBuilder();
            tableInfo.getFieldList().forEach(field->baseColumnField.append(String.format("`%s`",field.getFieldName())).append(","));
            baseColumnField.setLength(baseColumnField.length()-1);
            bw.write(String.format("        %s\n",baseColumnField));
            bw.write("    </sql>\n\n");

//            基础查询条件
            bw.write("    <!--通用基础查询条件-->\n");
            bw.write(String.format("    <sql id=\"%s\">\n",BASE_CONDITION_FILED));
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                if (ArrayUtils.contains(Constants.SQL_STRING_TYPE,fieldInfo.getSqlType())){
                    bw.write(String.format("\t\t<if test=\"query.%s!=null and query.%s!=''\">\n",fieldInfo.getPropertyName(),fieldInfo.getPropertyName()));
                }else{
                    bw.write(String.format("\t\t<if test=\"query.%s!=null\">\n",fieldInfo.getPropertyName()));
                }
                bw.write(String.format("\t\t\tand %s =#{query.%s}\n",fieldInfo.getFieldName(),fieldInfo.getPropertyName()));
                bw.write("\t\t</if>\n");
            }
            bw.write("    </sql>\n\n");

            //            扩展查询条件
            bw.write("    <!--通用扩展查询条件-->\n");
            bw.write(String.format("    <sql id=\"%s\">\n",BASE_QUERY_CONDITION_EXTEND));
            for (FieldInfo fieldInfo : tableInfo.getFieldExtendList()) {
                bw.write(String.format("\t\t<if test=\"query.%s!=null and query.%s!=''\">\n",fieldInfo.getPropertyName(),fieldInfo.getPropertyName()));
                if (ArrayUtils.contains(Constants.SQL_STRING_TYPE,fieldInfo.getSqlType())){
                    bw.write("\t\t\tand "+fieldInfo.getFieldName()+" like concat('%',#{query."+fieldInfo.getPropertyName()+"},'%')\n");
                }else if (ArrayUtils.contains(Constants.SQL_DATE_TYPES,fieldInfo.getSqlType())||ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES,fieldInfo.getSqlType())){
                    if (fieldInfo.getPropertyName().endsWith(Constants.SUFFIX_BEAN_QUERY_TIME_START)) {
                        bw.write("\t\t\t<![CDATA[ and " + fieldInfo.getFieldName() + ">=str_to_date(#{query." + fieldInfo.getPropertyName() + "},'%Y-%m-%d') ]]>\n");
                    }else if(fieldInfo.getPropertyName().endsWith(Constants.SUFFIX_BEAN_QUERY_TIME_END)){
                        bw.write("\t\t\t<![CDATA[ and "+fieldInfo.getFieldName()+"<date_sub(str_date(#{query."+fieldInfo.getPropertyName()+"},'%Y-%m-%d'),interval -1 day) ]]>\n");
                    }
                }
                bw.write("\t\t</if>\n");
            }
            bw.write("    </sql>\n\n");

            //通用查询条件
            bw.write("    <!--通用查询条件-->\n");
            bw.write(String.format("    <sql id=\"%s\">\n", QUERY_CONDITION));
            bw.write("\t\t<where>\n");
            bw.write(String.format("\t\t\t<include refid=\"%s\"/>\n",BASE_CONDITION_FILED));
            bw.write(String.format("\t\t\t<include refid=\"%s\"/>\n",BASE_QUERY_CONDITION_EXTEND));
            bw.write("\t\t</where>\n");
            bw.write("    </sql>\n\n");

            //查询列表
            bw.write("    <!--查询列表-->\n");
            bw.write(String.format("\t<select id=\"selectList\" resultMap=\"%s\">\n",BASE_RESULT_MAP));
            bw.write("\t\tselect\n");
            bw.write(String.format("\t\t<include refid=\"%s\"/>\n",BASE_COLUMN_LIST));
            bw.write(String.format("\t\tfrom %s\n",tableInfo.getTableName()));
            bw.write(String.format("\t\t<include refid=\"%s\"/>\n", QUERY_CONDITION));
            bw.write("\t\t<if test=\"query.orderBy!=null\">\n");
            bw.write("\t\torder by ${query.orderBy}\n");
            bw.write("\t\t</if>\n");
            bw.write("\t\t<if test=\"query.simplePage!=null\">\n");
            bw.write("\t\tlimit #{query.simplePage.start},#{query.simplePage.end}\n");
            bw.write("\t\t</if>\n");
            bw.write("\t</select>\n\n");

//            查询数量
            bw.write("\t<select id=\"selectCount\" resultType=\"Integer\">\n");
            bw.write(String.format("\t\tselect count(1) from %s\n",tableInfo.getTableName()));
            bw.write(String.format("\t\t<include refid=\"%s\"/>\n",QUERY_CONDITION));
            bw.write("\t</select>\n\n");

//            单条插入
            bw.write("\t<!--插入(匹配有值的字段)-->\n");
            bw.write(String.format("\t<insert id=\"insert\" parameterType=\"%s.%s\">\n",Constants.PACKAGE_PO,tableInfo.getBeanName()));

            FieldInfo autoIncrement=null;
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                if (fieldInfo.getAutoIncrement()!=null&&fieldInfo.getAutoIncrement()){
                    autoIncrement=fieldInfo;
                    break;
                }
            }
            if (autoIncrement!=null){
                bw.write(String.format("\t\t<selectKey keyProperty=\"bean.%s\" resultType=\"%s\" order=\"AFTER\">\n",autoIncrement.getPropertyName(),autoIncrement.getJavaType()));
                bw.write("\t\t\tselect LAST_INSERT_ID()\n");
                bw.write("\t\t</selectKey>\n");
            }

            bw.write(String.format("\t\tinsert into %s\n",tableInfo.getTableName()));
            bw.write("\t\t<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">\n");
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                bw.write(String.format("\t\t\t<if test=\"bean.%s!=null\">\n",fieldInfo.getPropertyName()));
                bw.write(String.format("\t\t\t\t%s,\n",fieldInfo.getFieldName()));
                bw.write("\t\t\t</if>\n");
            }
            bw.write("\t\t</trim>\n");
            bw.write("\t\t<trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\">\n");
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                bw.write(String.format("\t\t\t<if test=\"bean.%s!=null\">\n",fieldInfo.getPropertyName()));
                bw.write(String.format("\t\t\t\t#{bean.%s},\n",fieldInfo.getPropertyName()));
                bw.write("\t\t\t</if>\n");
            }
            bw.write("\t\t</trim>\n\n");
            bw.write("\t</insert>\n\n");

//                <!--插入或者更新(匹配有效的字段)-->
            bw.write("\t<!--插入或者更新(匹配有效的字段)-->\n");
            bw.write(String.format("\t<insert id=\"insertOrUpdate\" parameterType=\"%s.%s\">\n",Constants.PACKAGE_PO,tableInfo.getBeanName()));
            bw.write(String.format("\t\tinsert into %s\n",tableInfo.getTableName()));
            bw.write("\t\t<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">\n");
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                bw.write(String.format("\t\t\t<if test=\"bean.%s!=null\">\n",fieldInfo.getPropertyName()));
                bw.write(String.format("\t\t\t\t%s,\n",fieldInfo.getFieldName()));
                bw.write("\t\t\t</if>\n");
            }
            bw.write("\t\t</trim>\n");
            bw.write("\t\t<trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\">\n");
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                bw.write(String.format("\t\t\t<if test=\"bean.%s!=null\">\n",fieldInfo.getPropertyName()));
                bw.write(String.format("\t\t\t\t#{bean.%s},\n",fieldInfo.getPropertyName()));
                bw.write("\t\t\t</if>\n");
            }
            bw.write("\t\t</trim>\n");
            bw.write("        on DUPLICATE key update\n");
            bw.write("        <trim prefix=\"\" suffix=\"\" suffixOverrides=\",\">\n");

            Set<String> tempSet=new HashSet<>();
            for (Map.Entry<String, List<FieldInfo>> stringListEntry : tableInfo.getKeyIndexMap().entrySet()) {
                for (FieldInfo fieldInfo : stringListEntry.getValue()) {
                    tempSet.add(fieldInfo.getFieldName());
                }
            }

            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                if (tempSet.contains(fieldInfo.getFieldName())) continue;
                bw.write(String.format("            <if test=\"bean.%s!=null\">\n",fieldInfo.getPropertyName()));
                bw.write(String.format("                %s=values(%s),\n",fieldInfo.getFieldName(),fieldInfo.getFieldName()));
                bw.write("            </if>\n");
            }
            bw.write("        </trim>\n");
            bw.write("\t</insert>\n\n");

//                <!--添加(批量插入)-->
            bw.write("    <!--添加(批量插入)-->\n");
            bw.write(String.format("    <insert id=\"insertBatch\" parameterType=\"%s.%s\">\n",Constants.PACKAGE_PO,tableInfo.getBeanName()));
            bw.write(String.format("        insert into %s(",tableInfo.getTableName()));
            StringBuilder insertField = new StringBuilder();
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                if (fieldInfo.getAutoIncrement()) continue;
                insertField.append(fieldInfo.getFieldName()).append(",");
            }
            insertField.setLength(insertField.length()-1);
            bw.write(insertField.toString());
            bw.write(") VALUES\n");
            bw.write("        <foreach collection=\"list\" item=\"item\" separator=\",\">\n");

            StringBuilder insertPropertyBuilder = new StringBuilder("");
            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                if (fieldInfo.getAutoIncrement()) continue;
                insertPropertyBuilder.append(String.format("#{item.%s},",fieldInfo.getPropertyName()));
            }
            insertPropertyBuilder.setLength(insertPropertyBuilder.length()-1);
            bw.write(String.format("             (%s)\n", insertPropertyBuilder));

            bw.write("        </foreach>\n");
            bw.write("    </insert>\n\n");

//                <!--添加(批量插入或更新)-->
            bw.write("    <!--添加(批量插入更新)-->\n");
            bw.write(String.format("    <insert id=\"insertOrUpdateBatch\" parameterType=\"%s.%s\">\n",Constants.PACKAGE_PO,tableInfo.getBeanName()));
            bw.write(String.format("        insert into %s(",tableInfo.getTableName()));
            bw.write(insertField.toString());
            bw.write(") VALUES\n");
            bw.write("        <foreach collection=\"list\" item=\"item\" separator=\",\">\n");
            bw.write(String.format("             (%s)\n", insertPropertyBuilder));
            bw.write("        </foreach>\n");
            bw.write("        on DUPLICATE key update\n");

            for (int i = 0; i < tableInfo.getFieldList().size(); i++) {
                FieldInfo fieldInfo = tableInfo.getFieldList().get(i);
                bw.write(String.format("\t\t%s=values(%s)",fieldInfo.getFieldName(),fieldInfo.getFieldName()));
                if (i!=tableInfo.getFieldList().size()-1){
                    bw.write(",\n");
                }else{
                    bw.write("\n");
                }
            }

            bw.write("    </insert>\n\n");

            //<!--根据id修改-->
            for (Map.Entry<String, List<FieldInfo>> entry : tableInfo.getKeyIndexMap().entrySet()) {
                StringBuilder methodName = new StringBuilder();
                StringBuilder methodParams = new StringBuilder();
                StringBuilder paramsName = new StringBuilder();

                List<FieldInfo> value = entry.getValue();
                for (int i = 0; i < value.size(); i++) {
                    FieldInfo fieldInfo = value.get(i);
                    methodName.append(StringUtils.upperCaseFirstLetter(fieldInfo.getPropertyName()));
                    methodParams.append(String.format("@Param(\"%s\") %s %s", fieldInfo.getFieldName(),fieldInfo.getJavaType(),fieldInfo.getPropertyName()));
                    paramsName.append(String.format("%s=#{%s} ",fieldInfo.getFieldName(),fieldInfo.getFieldName()));
                    if (i != value.size() - 1) {
                        methodName.append("And");
                        methodParams.append(",");
                        paramsName.append("and ");
                    }
                }
//                BuildComment.createFieldComment(bw, "根据" + methodName + "查询");
                bw.write(String.format("    <!--根据%s查询-->\n",methodName));
                bw.write(String.format("    <select id=\"selectBy%s\" resultMap=\"%s\">\n",methodName,BASE_RESULT_MAP));
                bw.write("        select\n");
                bw.write(String.format("        <include refid=\"%s\"/>\n",BASE_COLUMN_LIST));
                bw.write(String.format("        from %s where %s\n",tableInfo.getTableName(),paramsName));
                bw.write("    </select>\n\n");

                bw.write(String.format("    <!--根据%s修改-->\n",methodName));
                bw.write(String.format("    <update id=\"updateBy%s\" parameterType=\"%s.%s\">\n",methodName,Constants.PACKAGE_PO,tableInfo.getBeanName()));
                bw.write(String.format("        update %s\n",tableInfo.getTableName()));
                bw.write("        <set>\n");
                for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                    bw.write(String.format("            <if test=\"bean.%s != null\">\n",fieldInfo.getPropertyName()));
                    bw.write(String.format("                %s=#{bean.%s}\n",fieldInfo.getFieldName(),fieldInfo.getPropertyName()));
                    bw.write("            </if>\n");
                }
                bw.write("        </set>\n");
                bw.write(String.format("        where %s\n",paramsName));
                bw.write("    </update>\n\n");

                bw.write(String.format("    <!--根据%s删除-->\n",methodName));
                bw.write(String.format("    <delete id=\"deleteBy%s\">\n",methodName));
                bw.write(String.format("        delete from %s where\n",tableInfo.getTableName()));
                bw.write(String.format("\t\t%s\n",paramsName));
                bw.write("    </delete>\n\n");

            }

            bw.write("</mapper>");
            bw.newLine();
            bw.flush();
        } catch (Exception e) {
            log.error("创建mapperXml失败", e);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (outw != null) {
                try {
                    outw.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }


    }
}
