package com.dsgroup4.httphandler.mysqlDialect;

import org.hibernate.dialect.MySQL5Dialect;

public class CustomMySQL5Dialect extends MySQL5Dialect {
    public String getTableTypeString() {
        return " engine=ndb";
    }
}