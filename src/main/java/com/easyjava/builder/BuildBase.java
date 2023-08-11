package com.easyjava.builder;

import com.easyjava.bean.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class BuildBase {

    private static final Logger log=LoggerFactory.getLogger(BuildBase.class);

    public static void execute(){
        List<String> headerInfoList=new ArrayList<>();

        headerInfoList.add(String.format("package %s;",Constants.PACKAGE_ENUMS));
        build(headerInfoList,"DateTimePatternEnum", Constants.PATH_ENUMS);

        headerInfoList.clear();
        headerInfoList.add(String.format("package %s;",Constants.PACKAGE_UTILS));
        build(headerInfoList,"DateUtils", Constants.PATH_UTILS);

        headerInfoList.clear();
        headerInfoList.add(String.format("package %s;",Constants.PACKAGE_ENUMS));
        build(headerInfoList,"PageSize", Constants.PATH_ENUMS);

        headerInfoList.clear();
        headerInfoList.add(String.format("package %s;",Constants.PACKAGE_ENUMS));
        build(headerInfoList,"ResponseCodeEnum", Constants.PATH_ENUMS);

        headerInfoList.clear();
        headerInfoList.add(String.format("package %s;",Constants.PACKAGE_EXCEPTION));
        headerInfoList.add(String.format("import %s.ResponseCodeEnum;\n",Constants.PACKAGE_ENUMS));
        build(headerInfoList,"BusinessException", Constants.PATH_EXCEPTION);


        headerInfoList.clear();
        headerInfoList.add(String.format("package %s;",Constants.PACKAGE_VO));
        build(headerInfoList,"PaginationResultVO", Constants.PATH_VO);

        headerInfoList.clear();
        headerInfoList.add(String.format("package %s;",Constants.PACKAGE_VO));
        build(headerInfoList,"ResponseVO", Constants.PATH_VO);

        headerInfoList.clear();
        headerInfoList.add(String.format("package %s;",Constants.PACKAGE_CONTROLLER));
        headerInfoList.add(String.format("import %s.ResponseVO;",Constants.PACKAGE_VO));
        headerInfoList.add(String.format("import %s.ResponseCodeEnum;",Constants.PACKAGE_ENUMS));
        headerInfoList.add(String.format("import %s.BusinessException;",Constants.PACKAGE_EXCEPTION));
        build(headerInfoList,"AGlobalExceptionHandlerController", Constants.PATH_CONTROLLER);

        headerInfoList.clear();
        headerInfoList.add(String.format("package %s;",Constants.PACKAGE_QUERY));
        headerInfoList.add(String.format("import %s\n",Constants.PACKAGE_ENUMS+".PageSize;"));
        build(headerInfoList,"SimplePage", Constants.PATH_QUERY);

        headerInfoList.clear();
        headerInfoList.add(String.format("package %s;",Constants.PACKAGE_CONTROLLER));
        headerInfoList.add(String.format("import %s.ResponseVO;",Constants.PACKAGE_VO));
        headerInfoList.add(String.format("import %s.ResponseCodeEnum;\n",Constants.PACKAGE_ENUMS));
        build(headerInfoList,"ABaseController", Constants.PATH_CONTROLLER);

        headerInfoList.clear();
        headerInfoList.add(String.format("package %s;",Constants.PACKAGE_QUERY));
        build(headerInfoList,"BaseQuery", Constants.PATH_QUERY);
//        生成BaseMapper
        headerInfoList.clear();
        headerInfoList.add(String.format("package %s;",Constants.PACKAGE_MAPPER));
        build(headerInfoList,"BaseMapper",Constants.PATH_MAPPER);
    }

    private static void build(List<String> headerInfoList, String fileName, String outputPath){
        File folder = new File(outputPath);
        if (!folder.exists()){
            folder.mkdirs();
        }

        File javaFile=new File(outputPath,fileName+".java");

        OutputStream out=null;
        OutputStreamWriter osw=null;
        BufferedWriter bw=null;

        InputStream in=null;
        InputStreamReader isr=null;
        BufferedReader br=null;

        try{
            out= Files.newOutputStream(javaFile.toPath());
            osw=new OutputStreamWriter(out, StandardCharsets.UTF_8);
            bw=new BufferedWriter(osw);

            String tmpFile=BuildBase.class.getClassLoader().getResource("template/"+fileName+".txt").getPath();

            in= new FileInputStream(tmpFile);
            isr=new InputStreamReader(in, StandardCharsets.UTF_8);
            br=new BufferedReader(isr);

            for (String s : headerInfoList) {
                bw.write(s);
                bw.newLine();
                if (s.contains("package")){
                    bw.newLine();
                }
            }

            String line;
            while ((line=br.readLine())!=null){
                bw.write(line);
                bw.newLine();
            }
            bw.flush();
        } catch (IOException e) {
            log.error("生成基础类:{} 失败",fileName,e);
        }finally {
            if (br!=null){
                try {
                    br.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (isr!=null){
                try {
                    isr.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            if (in!=null){
                try {
                    in.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }



            if (bw!=null){
                try {
                    bw.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            if (osw!=null){
                try {
                    osw.close();
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
