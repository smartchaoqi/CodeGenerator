package com.easyjava.builder;

import com.easyjava.bean.Constants;
import com.easyjava.bean.FieldInfo;
import com.easyjava.bean.TableInfo;
import com.easyjava.utils.DateUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class BuildPo {

    private static final Logger log= LoggerFactory.getLogger(BuildPo.class);
    public static void execute(TableInfo tableInfo){
        File folder = new File(Constants.PATH_PO);
        if (!folder.exists()){
            folder.mkdirs();
        }
        File poFile = new File(folder, tableInfo.getBeanName() + ".java");
        OutputStream out=null;
        OutputStreamWriter outw=null;
        BufferedWriter bw=null;

        try{
            out= Files.newOutputStream(poFile.toPath());
            outw=new OutputStreamWriter(out, StandardCharsets.UTF_8);
            bw=new BufferedWriter(outw);

            bw.write("package "+Constants.PACKAGE_PO+";");
            bw.newLine();
            bw.newLine();
            if (Constants.USE_LOMBOK){
                bw.write("import lombok.Data;\n");
            }
            bw.write("import java.io.Serializable;");
            bw.newLine();

            if (tableInfo.getHasBigDecimal()){
                bw.write("import java.math.BigDecimal;");
                bw.newLine();
            }

            if (tableInfo.getHasDate()||tableInfo.getHasDateTime()){
                bw.write("import java.util.Date;");
                bw.newLine();
                bw.write(Constants.BEAN_DATE_FORMAT_CLASS);
                bw.newLine();
                bw.write(Constants.BEAN_DATE_UNFORMAT_CLASS);
                bw.newLine();
            }

            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                if (Constants.IGNORE_BEAN_TOJSON_FILED.contains(fieldInfo.getPropertyName())){
                    bw.write(Constants.IGNORE_BEAN_TOJSON_CLASS);
                    bw.newLine();
                    break;
                }
            }

            bw.newLine();
            //构建类注释
            BuildComment.createClassComment(bw,tableInfo.getComment());
            if (Constants.USE_LOMBOK){
                bw.write("@Data\n");
            }

            bw.write("public class "+tableInfo.getBeanName()+" implements Serializable {");
            bw.newLine();

            for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
                if (StringUtils.isNotBlank(fieldInfo.getComment())){
                    BuildComment.createFieldComment(bw,fieldInfo.getComment());
                }
                if (ArrayUtils.contains(Constants.SQL_DATE_TIME_TYPES,fieldInfo.getSqlType())){
                    bw.write("\t"+String.format(Constants.BEAN_DATE_FORMAT_EXPRESSION, DateUtils.YYYY_MM_DD_HH_MM_SS));
                    bw.newLine();

                    bw.write("\t"+String.format(Constants.BEAN_DATE_UNFORMAT_EXPRESSION, DateUtils.YYYY_MM_DD_HH_MM_SS));
                    bw.newLine();
                }

                if (ArrayUtils.contains(Constants.SQL_DATE_TYPES,fieldInfo.getSqlType())){
                    bw.write("\t"+String.format(Constants.BEAN_DATE_FORMAT_EXPRESSION, DateUtils.YYYY_MM_DD));
                    bw.newLine();

                    bw.write("\t"+String.format(Constants.BEAN_DATE_UNFORMAT_EXPRESSION, DateUtils.YYYY_MM_DD));
                    bw.newLine();
                }

                if (Constants.IGNORE_BEAN_TOJSON_FILED.contains(fieldInfo.getPropertyName())){
                    bw.write("\t"+Constants.IGNORE_BEAN_TOJSON_EXPRESSION);
                    bw.newLine();
                }
                bw.write("\tprivate "+fieldInfo.getJavaType()+" "+fieldInfo.getPropertyName()+";");
                bw.newLine();
                bw.newLine();
            }
            if (!Constants.USE_LOMBOK) {
                for (FieldInfo fieldInfo : tableInfo.getFieldList()) {
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

//            重写toString()
                bw.write("\t@Override");
                bw.newLine();
                bw.write("\tpublic String toString() {");
                bw.newLine();
                bw.write(String.format("\t\treturn \"%s{\" +", tableInfo.getBeanName()));
                bw.newLine();
                for (int i = 0; i < tableInfo.getFieldList().size(); i++) {
                    FieldInfo fieldInfo = tableInfo.getFieldList().get(i);
                    if (i == 0) {
                        bw.write(String.format("\t\t\t\t\"%s=\" + %s +", fieldInfo.getPropertyName(), fieldInfo.getPropertyName()));
                    } else {
                        bw.write(String.format("\t\t\t\t\", %s=\" + %s +", fieldInfo.getPropertyName(), fieldInfo.getPropertyName()));
                    }
                    bw.newLine();
                }
                bw.write("\t\t\t\t'}';");
                bw.newLine();
                bw.write("\t}");
                bw.newLine();
            }

            bw.write("}");
            bw.flush();
        }catch (Exception e){
            log.error("创建po失败",e);
        }finally {
            if (bw!=null){
                try {
                    bw.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (outw!=null){
                try {
                    outw.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (out!=null){
                try {
                    out.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
