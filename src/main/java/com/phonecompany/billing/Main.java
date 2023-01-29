package com.phonecompany.billing;

import java.io.*;
import java.math.BigDecimal;

public class Main {
    public static void main(String[] args) {
        String fileName = "calllogtestdata.csv";
        try{
            String phoneLog = readFileToString(fileName);
            CallsInList callsInList = new CallsInList();
            BigDecimal phoneLogBill = callsInList.calculate(phoneLog);
            System.out.println("Main has finished successfully. Bill for phone log is " + phoneLogBill + " Kč");
        }
        catch (Exception e){
            System.out.println("Error in " + fileName +  " Telephone Billing! " + e.getMessage());
        }
    }

    private static String readFileToString(String fileName) throws Exception {
        System.out.println("Reading file: " + fileName);
        StringBuilder str = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(fileName)) {
            int content;
            // reads a byte at a time, if it reached end of the file, returns -1
            while ((content = fis.read()) != -1) {
                //System.out.print((char)content);
                str.append((char) content);
            }
        } catch (Exception e) {
            System.out.println("Error in reading file.");
            throw e;
        }
        return str.toString();
    }
}