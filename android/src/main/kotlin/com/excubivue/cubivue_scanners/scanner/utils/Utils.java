package com.excubivue.cubivue_scanners.scanner.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by Umair Adil on 18/11/2016.
 */
public class Utils {


    private static Utils ourInstance = new Utils();

    private static String loggedItem = "";

    private static String loggedLocation = "";
    private String TAG = "Utils";

    public Utils() {

    }


    public static Utils getInstance() {
        return ourInstance;
    }


    public String getStackTrace(Exception e) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return sw.toString();
        } catch (Exception e2) {
            e2.printStackTrace();
            try {
                return e.getMessage();
            } catch (Exception e3) {
                e3.printStackTrace();
                return "Error!";
            }
        }
    }

    public String getStackTrace(Throwable e) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return sw.toString();
        } catch (Exception e2) {
            e2.printStackTrace();
            try {
                return e.getMessage();
            } catch (Exception e3) {
                e3.printStackTrace();
                return "Error!";
            }
        }
    }
}

