package com.seventyseven.adskiper.db;

public class RecordTable {

    public static final String NAME = "recordTable";
    public static final String PACKAGE_NAME = "package_name";
    public static final String APP_NAME = "app_name";
    public static final String TIMES = "times";

    public static final int ID_PACKAGE_NAME = 0;
    public static final int ID_APP_NAME = 1;
    public static final int ID_TIMES = 2;

    public static final String CREATE_TABLE = "create table if not exists " + NAME + "(" +
            PACKAGE_NAME + " text primery key, " +
            APP_NAME + " text, " +
            TIMES + " integer)" ;

}
