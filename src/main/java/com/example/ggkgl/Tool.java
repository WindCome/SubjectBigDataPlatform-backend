package com.example.ggkgl;

import jdk.internal.dynalink.beans.StaticClass;
import org.apache.tomcat.util.bcel.Const;

public class Tool {
    public static String transfer(String str)
    {
        return str.replaceAll("\\\\","");
    }
}
