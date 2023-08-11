package com.easyjava.builder;

import com.easyjava.bean.Constants;
import com.easyjava.bean.FieldInfo;
import com.easyjava.bean.TableInfo;
import com.easyjava.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

public class BuildService {
    private static final Logger log= LoggerFactory.getLogger(BuildService.class);
    public static void execute(TableInfo tableInfo){
        File folder = new File(Constants.PATH_SERVICE);
        if (!folder.exists()){
            folder.mkdirs();
        }
        String className=tableInfo.getBeanName()+"Service";
        File poFile = new File(folder, className+".java");
        OutputStream out=null;
        OutputStreamWriter outw=null;
        BufferedWriter bw=null;

        try{
            out= Files.newOutputStream(poFile.toPath());
            outw=new OutputStreamWriter(out, StandardCharsets.UTF_8);
            bw=new BufferedWriter(outw);

            bw.write("package "+Constants.PACKAGE_SERVICE+";");
            bw.newLine();
            bw.newLine();
            bw.write("import java.util.List;\n");
            bw.write(String.format("import %s.PaginationResultVO;\n",Constants.PACKAGE_VO));
            bw.write(String.format("import %s.%s;\n",Constants.PACKAGE_PO,tableInfo.getBeanName()));
            bw.write(String.format("import %s.%s;\n",Constants.PACKAGE_QUERY,tableInfo.getBeanParamName()));
            bw.newLine();

//            if (tableInfo.getHasBigDecimal()){
//                bw.write("import java.math.BigDecimal;");
//                bw.newLine();
//            }
//
//            if (tableInfo.getHasDate()||tableInfo.getHasDateTime()){
//                bw.write("import java.util.Date;");
//                bw.newLine();
//            }

            bw.newLine();
            //构建类注释
            BuildComment.createClassComment(bw,tableInfo.getComment()+"Service");
            bw.write("public interface "+className+" {\n");

            BuildComment.createFieldComment(bw,"根据条件查询列表");
            bw.write(String.format("\tList<%s> findListByParam(%s query);\n\n",tableInfo.getBeanName(),tableInfo.getBeanParamName()));

            BuildComment.createFieldComment(bw,"根据条件查询数量");
            bw.write(String.format("\tInteger findCountByParam(%s query);\n\n",tableInfo.getBeanParamName()));

            BuildComment.createFieldComment(bw,"分页查询");
            bw.write(String.format("\tPaginationResultVO<%s> findListByPage(%s query);\n\n",tableInfo.getBeanName(),tableInfo.getBeanParamName()));

            BuildComment.createFieldComment(bw,"新增");
            bw.write(String.format("\tInteger add(%s bean);\n\n",tableInfo.getBeanName()));

            BuildComment.createFieldComment(bw,"批量新增");
            bw.write(String.format("\tInteger addBatch(List<%s> listBean);\n\n",tableInfo.getBeanName()));

            BuildComment.createFieldComment(bw,"批量新增/修改");
            bw.write(String.format("\tInteger addOrUpdateBatch(List<%s> listBean);\n\n",tableInfo.getBeanName()));

            Map<String, List<FieldInfo>> keyIndexMap = tableInfo.getKeyIndexMap();
            for (Map.Entry<String, List<FieldInfo>> entry : keyIndexMap.entrySet()) {
                StringBuilder methodName = new StringBuilder();
                StringBuilder methodParams = new StringBuilder();

                List<FieldInfo> value = entry.getValue();
                for (int i = 0; i < value.size(); i++) {
                    FieldInfo fieldInfo = value.get(i);
                    methodName.append(StringUtils.upperCaseFirstLetter(fieldInfo.getPropertyName()));
                    methodParams.append(String.format("%s %s", fieldInfo.getJavaType(),fieldInfo.getPropertyName()));
                    if (i != value.size() - 1) {
                        methodName.append("And");
                        methodParams.append(",");
                    }
                }
                BuildComment.createFieldComment(bw, "根据" + methodName + "查询");
                bw.write("\t"+tableInfo.getBeanName()+" get"+tableInfo.getBeanName()+"By" + methodName + "("+methodParams+");");
                bw.newLine();
                bw.newLine();

                BuildComment.createFieldComment(bw, "根据" + methodName + "修改");
                bw.write("\tInteger update"+ tableInfo.getBeanName()+"By" + methodName + "("+ tableInfo.getBeanName()+" t, "+methodParams+");");
                bw.newLine();
                bw.newLine();

                BuildComment.createFieldComment(bw, "根据" + methodName + "删除");
                bw.write("\tInteger delete"+ tableInfo.getBeanName()+"By" + methodName + "("+methodParams+");");
                bw.newLine();
                bw.newLine();
            }

            bw.write("}");
            bw.flush();
        }catch (Exception e){
            log.error("创建service失败",e);
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
