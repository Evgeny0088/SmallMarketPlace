package com.marketplace.itemstorageservice.utilFunctions;

import org.jetbrains.annotations.NotNull;

public class HelperFunctions {

    public static String requestBody(String serial, String brandId, String parentId, String itemType){
        String initString = String.format("""
                { \s
                    "serial": %s,
                    "brandName": {
                        "id": %s
                                 },
                    "parentItem": {
                        "id": %s
                    },
                    "item_type": "%s"
                    }
                """, serial, brandId, parentId, !itemType.equals("ITEM") && !itemType.equals("PACK") ? "" : itemType);
        StringBuilder builder = new StringBuilder();
        return stringHandler(initString.replaceAll("[\n\r]","").replaceAll(" ", ""), builder);
    }

    private static String stringHandler(@NotNull String initString, StringBuilder builder){
        String nullString = "null";
        if (!initString.contains(nullString)){
            return builder.append(initString).toString();
        }
        String headString = initString.substring(0,initString.indexOf(nullString)+nullString.length());
        initString = initString.substring(headString.length());
        if (initString.charAt(0) == '}' && initString.indexOf('}')<initString.length()-1){
            initString = initString.substring(1);
        }
        int i = headString.length()-1;
        int j = -1;
        while (i>-1){
            if (headString.charAt(i) == '}') j = i;
            if (headString.charAt(i) == '{' && i>0 && i>j){
                builder.append(headString, 0, i).append(nullString);
                return stringHandler(initString, builder);
            }
            i--;
        }
        builder.append(headString);
        return stringHandler(initString, builder);
    }
}
