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

public class BuildServiceImpl {
    private static final Logger log= LoggerFactory.getLogger(BuildServiceImpl.class);
    public static void execute(TableInfo tableInfo){
        File folder = new File(Constants.PATH_SERVICE_IMPL);
        if (!folder.exists()){
            folder.mkdirs();
        }
        String className=tableInfo.getBeanName()+"ServiceImpl";
        File poFile = new File(folder, className+".java");
        OutputStream out=null;
        OutputStreamWriter outw=null;
        BufferedWriter bw=null;

        try{
            out= Files.newOutputStream(poFile.toPath());
            outw=new OutputStreamWriter(out, StandardCharsets.UTF_8);
            bw=new BufferedWriter(outw);

            bw.write("package "+Constants.PACKAGE_SERVICE_IMPL+";");
            bw.newLine();
            bw.newLine();
            bw.write("import java.util.List;\n");
            bw.write(String.format("import %s.PaginationResultVO;\n",Constants.PACKAGE_VO));
            bw.write(String.format("import %s.%s;\n",Constants.PACKAGE_PO,tableInfo.getBeanName()));
            bw.write(String.format("import %s.%s;\n",Constants.PACKAGE_QUERY,tableInfo.getBeanParamName()));
            bw.write(String.format("import %s.%sMapper;\n",Constants.PACKAGE_MAPPER,tableInfo.getBeanName()));
            bw.write(String.format("import %s.%sService;\n",Constants.PACKAGE_SERVICE,tableInfo.getBeanName()));
            bw.write(String.format("import %s.SimplePage;\n",Constants.PACKAGE_QUERY));
            bw.write(String.format("import %s.PageSize;\n",Constants.PACKAGE_ENUMS));
            bw.write("import org.springframework.stereotype.Service;\n");
            bw.newLine();
            bw.write("import javax.annotation.Resource;\n\n");

            //构建类注释
            BuildComment.createClassComment(bw,tableInfo.getComment()+"ServiceImpl");
            bw.write(String.format("@Service(\"%s\")\n",StringUtils.lowerCaseFirstLetter(tableInfo.getBeanName())+"Service"));

            bw.write("public class "+className+" implements "+tableInfo.getBeanName()+"Service{\n");
            bw.write("\t@Resource\n");
            String mapperName = StringUtils.lowerCaseFirstLetter(tableInfo.getBeanName() + "Mapper");
            bw.write(String.format("\tprivate %sMapper<%s,%s> %s;\n\n", tableInfo.getBeanName(),tableInfo.getBeanName(),tableInfo.getBeanParamName(), mapperName));

            BuildComment.createFieldComment(bw,"根据条件查询列表");
            bw.write(String.format("\tpublic List<%s> findListByParam(%s query){\n",tableInfo.getBeanName(),tableInfo.getBeanParamName()));
            bw.write(String.format("\t\treturn this.%s.selectList(query);\n",mapperName));
            bw.write("\t}\n\n");

            BuildComment.createFieldComment(bw,"根据条件查询数量");
            bw.write(String.format("\tpublic Integer findCountByParam(%s query){\n",tableInfo.getBeanParamName()));
            bw.write(String.format("\t\treturn this.%s.selectCount(query);\n",mapperName));
            bw.write("\t}\n\n");

            BuildComment.createFieldComment(bw,"分页查询");
            bw.write(String.format("\tpublic PaginationResultVO<%s> findListByPage(%s query){\n",tableInfo.getBeanName(),tableInfo.getBeanParamName()));
            bw.write("\t\tInteger count = this.findCountByParam(query);\n");
            bw.write("\t\tint pageSize=query.getPageSize()==null? PageSize.SIZE15.getSize() : query.getPageSize();\n\n");
            bw.write("\t\tSimplePage page=new SimplePage(query.getPageNo(),count,pageSize);\n");
            bw.write("\t\tquery.setSimplePage(page);\n");
            bw.write(String.format("\t\tList<%s> list = this.findListByParam(query);\n",tableInfo.getBeanName()));
            bw.write("\t\treturn new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);\n");
            bw.write("\t}\n\n");

            BuildComment.createFieldComment(bw,"新增");
            bw.write(String.format("\tpublic Integer add(%s bean){\n",tableInfo.getBeanName()));
            bw.write(String.format("\t\treturn this.%s.insert(bean);\n",mapperName));
            bw.write("\t}\n\n");

            BuildComment.createFieldComment(bw,"批量新增");
            bw.write(String.format("\tpublic Integer addBatch(List<%s> listBean){\n",tableInfo.getBeanName()));
            bw.write("\t\tif (listBean==null||listBean.size()==0){\n");
            bw.write("\t\t\treturn 0;\n");
            bw.write("\t\t}\n");
            bw.write(String.format("\t\treturn this.%s.insertBatch(listBean);\n",mapperName));
            bw.write("\t}\n\n");

            BuildComment.createFieldComment(bw,"批量新增/修改");
            bw.write(String.format("\tpublic Integer addOrUpdateBatch(List<%s> listBean){\n",tableInfo.getBeanName()));
            bw.write("\t\tif (listBean==null||listBean.size()==0){\n");
            bw.write("\t\t\treturn 0;\n");
            bw.write("\t\t}\n");
            bw.write(String.format("\t\treturn this.%s.insertOrUpdateBatch(listBean);\n",mapperName));
            bw.write("\t}\n\n");

            Map<String, List<FieldInfo>> keyIndexMap = tableInfo.getKeyIndexMap();
            for (Map.Entry<String, List<FieldInfo>> entry : keyIndexMap.entrySet()) {
                StringBuilder methodName = new StringBuilder();
                StringBuilder methodParams = new StringBuilder();
                StringBuilder param=new StringBuilder();

                List<FieldInfo> value = entry.getValue();
                for (int i = 0; i < value.size(); i++) {
                    FieldInfo fieldInfo = value.get(i);
                    methodName.append(StringUtils.upperCaseFirstLetter(fieldInfo.getPropertyName()));
                    methodParams.append(String.format("%s %s", fieldInfo.getJavaType(),fieldInfo.getPropertyName()));
                    param.append(fieldInfo.getPropertyName());
                    if (i != value.size() - 1) {
                        methodName.append("And");
                        methodParams.append(",");
                        param.append(",");
                    }
                }
                BuildComment.createFieldComment(bw, "根据" + methodName + "查询");
                bw.write("\tpublic "+tableInfo.getBeanName()+" get"+tableInfo.getBeanName()+"By" + methodName + "("+methodParams+"){");
                bw.newLine();
                bw.write(String.format("\t\treturn this.%s.selectBy%s(%s);\n",mapperName,methodName,param));
                bw.write("\t}\n\n");

                BuildComment.createFieldComment(bw, "根据" + methodName + "修改");
                bw.write("\tpublic Integer update"+ tableInfo.getBeanName()+"By" + methodName + "("+ tableInfo.getBeanName()+" t, "+methodParams+"){");
                bw.newLine();
                bw.write(String.format("\t\treturn this.%s.updateBy%s(t,%s);\n",mapperName,methodName,param));
                bw.write("\t}\n\n");

                BuildComment.createFieldComment(bw, "根据" + methodName + "删除");
                bw.write("\tpublic Integer delete"+ tableInfo.getBeanName()+"By" + methodName + "("+methodParams+"){");
                bw.newLine();
                bw.write(String.format("\t\treturn this.%s.deleteBy%s(%s);\n",mapperName,methodName,param));
                bw.write("\t}\n\n");
            }

            bw.write("}");
            bw.flush();
        }catch (Exception e){
            log.error("创建service impl失败",e);
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
