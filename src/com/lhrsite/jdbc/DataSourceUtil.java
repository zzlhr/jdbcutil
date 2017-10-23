package com.lhrsite.jdbc;

import javax.sql.DataSource;

/**
 * @author lhr
 * @create 2017/10/23
 */
public class DataSourceUtil {

    private static DataSource dataSource = null;


    public static DataSource getDataSource() {
        return dataSource;
    }

    public static void setDataSource(DataSource dataSource) {
        DataSourceUtil.dataSource = dataSource;
    }


}
