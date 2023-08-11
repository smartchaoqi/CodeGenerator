package com.easyjava.builder;

import com.easyjava.bean.Constants;
import com.easyjava.utils.DateUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Date;

public class BuildComment {
    public static void createClassComment(BufferedWriter bw,String classComment) throws IOException {
        bw.write("/**");
        bw.newLine();
        bw.write(" * @Description: "+classComment);
        bw.newLine();
        bw.write(" * @author: "+ Constants.AUTHOR_COMMENT);
        bw.newLine();
        String format = DateUtils.format(new Date(), DateUtils._YYYYMMDD);
        bw.write(" * @date: "+format);
        bw.newLine();
        bw.write(" */");
        bw.newLine();
    }

    public static void createFieldComment(BufferedWriter bw,String fieldComment) throws IOException {
        bw.write("\t/**");
        bw.newLine();
        bw.write("\t * "+fieldComment);
        bw.newLine();
        bw.write("\t */");
        bw.newLine();
    }

    public static void createMethodComment(BufferedWriter bw){

    }
}
