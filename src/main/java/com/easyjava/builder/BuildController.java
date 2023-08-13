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

public class BuildController {
    private static final Logger log= LoggerFactory.getLogger(BuildController.class);
    public static void execute(TableInfo tableInfo){
        File folder = new File(Constants.PATH_CONTROLLER);
        if (!folder.exists()){
            folder.mkdirs();
        }
        String className=tableInfo.getBeanName()+"Controller";
        File poFile = new File(folder, className+".java");
        OutputStream out=null;
        OutputStreamWriter outw=null;
        BufferedWriter bw=null;

        try{
            out= Files.newOutputStream(poFile.toPath());
            outw=new OutputStreamWriter(out, StandardCharsets.UTF_8);
            bw=new BufferedWriter(outw);

            bw.write("package "+Constants.PACKAGE_CONTROLLER+";");
            bw.newLine();
            bw.newLine();
            bw.write("import java.util.List;\n\n");
            bw.write(String.format("import %s.%s;\n",Constants.PACKAGE_PO,tableInfo.getBeanName()));
            bw.write(String.format("import %s.%s;\n",Constants.PACKAGE_QUERY,tableInfo.getBeanParamName()));
            bw.write(String.format("import %s.ResponseVO;\n",Constants.PACKAGE_VO));
            bw.write(String.format("import %s.%sService;\n",Constants.PACKAGE_SERVICE,tableInfo.getBeanName()));
            bw.write("import org.springframework.web.bind.annotation.*;\n");
            bw.newLine();
            bw.write("import javax.annotation.Resource;\n\n");

            //构建类注释
            BuildComment.createClassComment(bw,tableInfo.getComment()+"Controller");
            bw.write("@RestController\n");
            bw.write(String.format("@RequestMapping(\"/%s\")\n",StringUtils.lowerCaseFirstLetter(tableInfo.getBeanName())));

            bw.write("public class "+className+" extends ABaseController{\n");
            bw.write("\t@Resource\n");
            String serviceName = StringUtils.lowerCaseFirstLetter(tableInfo.getBeanName() + "Service");
            bw.write(String.format("\tprivate %sService %s;\n\n", tableInfo.getBeanName(),serviceName));

            BuildComment.createFieldComment(bw,"根据条件分页查询");
            bw.write("\t@PostMapping(\"loadDataList\")\n");
            bw.write(String.format("\tpublic ResponseVO loadDataList(@RequestBody %s query){\n",tableInfo.getBeanParamName()));
            bw.write(String.format("\t\treturn getSuccessResponseVO(%s.findListByParam(query));\n",serviceName));
            bw.write("\t}\n\n");

            BuildComment.createFieldComment(bw,"新增");
            bw.write("\t@PostMapping(\"add\")\n");
            bw.write(String.format("\tpublic ResponseVO add(@RequestBody %s bean){\n",tableInfo.getBeanName()));
            bw.write(String.format("\t\t%s.add(bean);\n",serviceName));
            bw.write("\t\treturn getSuccessResponseVO(null);\n");
            bw.write("\t}\n\n");

            BuildComment.createFieldComment(bw,"批量新增");
            bw.write("\t@PostMapping(\"addBatch\")\n");
            bw.write(String.format("\tpublic ResponseVO addBatch(@RequestBody List<%s> listBean){\n",tableInfo.getBeanName()));
            bw.write(String.format("\t\t%s.addBatch(listBean);\n",serviceName));
            bw.write("\t\treturn getSuccessResponseVO(null);\n");
            bw.write("\t}\n\n");

            BuildComment.createFieldComment(bw,"批量新增/修改");
            bw.write("\t@PostMapping(\"addOrUpdateBatch\")\n");
            bw.write(String.format("\tpublic ResponseVO addOrUpdateBatch(@RequestBody List<%s> listBean){\n",tableInfo.getBeanName()));
            bw.write(String.format("\t\t%s.addOrUpdateBatch(listBean);\n",serviceName));
            bw.write("\t\treturn getSuccessResponseVO(null);\n");
            bw.write("\t}\n\n");

            Map<String, List<FieldInfo>> keyIndexMap = tableInfo.getKeyIndexMap();
            for (Map.Entry<String, List<FieldInfo>> entry : keyIndexMap.entrySet()) {
                StringBuilder methodName = new StringBuilder();
                StringBuilder methodParams = new StringBuilder();
                StringBuilder param=new StringBuilder();
                StringBuilder restfulPath = new StringBuilder();

                List<FieldInfo> value = entry.getValue();
                for (int i = 0; i < value.size(); i++) {
                    FieldInfo fieldInfo = value.get(i);
                    methodName.append(StringUtils.upperCaseFirstLetter(fieldInfo.getPropertyName()));
                    methodParams.append(String.format("@PathVariable(\"%s\") %s %s",fieldInfo.getPropertyName(), fieldInfo.getJavaType(),fieldInfo.getPropertyName()));
                    param.append(fieldInfo.getPropertyName());
                    if (i != value.size() - 1) {
                        methodName.append("And");
                        methodParams.append(",");
                        param.append(",");
                    }
                    restfulPath.append(String.format("/{%s}",fieldInfo.getPropertyName()));
                }
                String methodDetail=tableInfo.getBeanName()+"By" + methodName;

                BuildComment.createFieldComment(bw, "根据" + methodName + "查询");
                bw.write(String.format("\t@GetMapping(\"get%s%s\")\n",methodDetail,restfulPath));
                bw.write("\tpublic ResponseVO get"+methodDetail + "("+methodParams+"){");
                bw.newLine();
                bw.write(String.format("\t\t%s t = %s.get%sBy%s(%s);\n",tableInfo.getBeanName(),serviceName,tableInfo.getBeanName(),methodName,param));
                bw.write("\t\treturn getSuccessResponseVO(t);\n");
                bw.write("\t}\n\n");

                BuildComment.createFieldComment(bw, "根据" + methodName + "修改");
                bw.write(String.format("\t@PutMapping(\"update%s%s\")\n",methodDetail,restfulPath));
                bw.write("\tpublic ResponseVO update"+ methodDetail + "(@RequestBody "+ tableInfo.getBeanName()+" t, "+methodParams+"){");
                bw.newLine();
                bw.write(String.format("\t\tInteger integer = this.%s.update%s(t, %s);\n",serviceName,methodDetail,param));
                bw.write("\t\treturn getSuccessResponseVO(integer);\n");
                bw.write("\t}\n\n");

                BuildComment.createFieldComment(bw, "根据" + methodName + "删除");
                bw.write(String.format("\t@DeleteMapping(\"delete%s%s\")\n",methodDetail,restfulPath));
                bw.write("\tpublic ResponseVO delete"+ methodDetail + "("+methodParams+"){");
                bw.newLine();
                bw.write(String.format("\t\tInteger integer = this.%s.delete%s(%s);\n",serviceName,methodDetail,param));
                bw.write("\t\treturn getSuccessResponseVO(integer);\n");
                bw.write("\t}\n\n");
            }

            bw.write("}");
            bw.flush();
        }catch (Exception e){
            log.error("创建controller失败",e);
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
