package mysql;

import org.hibernate.dialect.MySQL5Dialect;

public class CustomMySQL5Dialect extends MySQL5Dialect {
    public String getTableTypeString() {
        return " engine=ndb";
    }
}