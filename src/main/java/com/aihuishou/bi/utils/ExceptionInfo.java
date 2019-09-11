package com.aihuishou.bi.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionInfo {

    public static String toString(Throwable e){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        e.printStackTrace(pw);
        pw.flush();
        sw.flush();
        return sw.toString();
    }
}
