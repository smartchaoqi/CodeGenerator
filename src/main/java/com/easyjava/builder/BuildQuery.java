package com.easyjava.builder;

import com.easyjava.bean.Constants;
import com.easyjava.bean.FieldInfo;
import com.easyjava.bean.TableInfo;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class BuildQuery {

    private static final Logger log = LoggerFactory.getLogger(BuildQuery.class);

    public static void execute(TableInfo tableInfo) {
        File folder = new File(Constants.PATH_QUERY);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String className = tableInfo.getBeanName() + Constants.SUFFIX_BEAN_QUERY;

        File poFile = new File(folder, className + ".java");
        OutputStream out = null;
        OutputStreamWriter outw = null;
        BufferedWriter bw = null;

        try {
            out = Files.newOutputStream(poFile.toPath());
            outw = new OutputStreamWriter(out, StandardCharsets.UTF_8);
            bw = new BufferedWriter(outw);

            bw.write("package " + Constants.PACKAGE_QUERY + ";");
            bw.newLine();
            bw.newLine();

            if (Constants.USE_LOMBOK) {
                bw.write("import lombok.Data;\n");
            }

            if (tableInfo.getHasBigDecimal()) {
                bw.write("import java.math.BigDecimal;");
                bw.newLine();
            }

            if (tableInfo.getHasDate() || tableInfo.getHasDateTime()) {
                bw.write("import java.util.Date;");
                bw.newLine();
            }

            bw.newLine();
            //构建类注释
            BuildComment.createClassComment(bw, tableInfo.getComment() + "查询对象");

            if (Constants.USE_LOMBOK) {
                bw.write("@Data\n");
            }
            bw.write("public class " + className + " extends BaseQuery{");
            bw.newLine();

            List<FieldInfo> tempList = new ArrayList<>();

            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                if (StringUtils.isNotBlank(fieldInfo.getComment())) {
                    BuildComment.createFieldComment(bw, fieldInfo.getComment());
                }
                bw.write("\tprivate " + fieldInfo.getJavaType() + " " + fieldInfo.getPropertyName() + ";");
                bw.newLine();
                bw.newLine();

                if (ArrayUtils.contains(Constants.SQL_STRING_TYPE, fieldInfo.getSqlType())) {
                    bw.write("\tprivate " + fieldInfo.getJavaType() + " " + fieldInfo.getPropertyName() + Constants.SUFFIX_BEAN_QUERY_FUZZY + ";");
                    bw.newLine();
                    bw.newLine();
                    FieldInfo fieldInfoFuzzy = new FieldInfo();
                    fieldInfoFuzzy.setJavaType(fieldInfo.getJavaType());
                    fieldInfoFuzzy.setPropertyName(fieldInfo.getPropertyName() + Constants.SUFFIX_BEAN_QUERY_FUZZY);
                    tempList.add(fieldInfoFuzzy);
                }

                if (ArrayUtils.contains(Constants.SQL_DATE_TYPES, fieldInfo.getSqlType()) || ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES, fieldInfo.getSqlType())) {
                    bw.write("\tprivate String " + fieldInfo.getPropertyName() + Constants.SUFFIX_BEAN_QUERY_TIME_START + ";");
                    bw.newLine();
                    bw.newLine();
                    bw.write("\tprivate String " + fieldInfo.getPropertyName() + Constants.SUFFIX_BEAN_QUERY_TIME_END + ";");
                    bw.newLine();
                    bw.newLine();

                    FieldInfo fieldInfoTimeStart = new FieldInfo();
                    fieldInfoTimeStart.setJavaType("String");
                    fieldInfoTimeStart.setPropertyName(fieldInfo.getPropertyName() + Constants.SUFFIX_BEAN_QUERY_TIME_START);

                    FieldInfo fieldInfoTimeEnd = new FieldInfo();
                    fieldInfoTimeEnd.setJavaType("String");
                    fieldInfoTimeEnd.setPropertyName(fieldInfo.getPropertyName() + Constants.SUFFIX_BEAN_QUERY_TIME_END);

                    tempList.add(fieldInfoTimeStart);
                    tempList.add(fieldInfoTimeEnd);
                }
            }

//            tableInfo.getFieldList().addAll(tempList);
//            tableInfo.setFieldExtendList(new ArrayList<>(tempList));
            tempList.addAll(tableInfo.getFieldList());

            if (!Constants.USE_LOMBOK) {
                for (FieldInfo fieldInfo : tempList) {
                    String temField = com.easyjava.utils.StringUtils.upperCaseFirstLetter(fieldInfo.getPropertyName());
                    bw.write(String.format("\tpublic void set%s(%s %s) {", temField, fieldInfo.getJavaType(), fieldInfo.getPropertyName()));
                    bw.newLine();
                    bw.write(String.format("\t\tthis.%s = %s;", fieldInfo.getPropertyName(), fieldInfo.getPropertyName()));
                    bw.newLine();
                    bw.write("\t}");
                    bw.newLine();
                    bw.newLine();

                    bw.write(String.format("\tpublic %s get%s() {", fieldInfo.getJavaType(), temField));
                    bw.newLine();
                    bw.write(String.format("\t\treturn %s;", fieldInfo.getPropertyName()));
                    bw.newLine();
                    bw.write("\t}");
                    bw.newLine();
                    bw.newLine();
                }
            }

            bw.write("}");
            bw.flush();
        } catch (Exception e) {
            log.error("创建po失败", e);
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
