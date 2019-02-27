package com.example.ggkgl.AssitClass;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
* 异常辅助类
 **/
public class ExceptionHelper {
    public static String getExceptionAllInfo(Exception ex) {
        ByteArrayOutputStream out;
        PrintStream pout = null;
        String ret;
        try {
            out = new ByteArrayOutputStream();
            pout = new PrintStream(out);
            ex.printStackTrace(pout);
            ret = new String(out.toByteArray());
            out.close();
        }
        catch (Exception e) {
            return ex.getMessage();
        }
        finally {
            if (pout != null) {
                pout.close();
            }
        }
        return ret;
    }
}
