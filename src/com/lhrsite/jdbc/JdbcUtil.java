package com.lhrsite.jdbc;

import net.sf.json.JSONArray;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * jdbc操作
 *
 * @author 刘浩然
 * @date 2017/7/26
 */
public class JdbcUtil {

    /**
     * ResultSet转List
     * @param rs
     * @return
     * @throws SQLException
     */
    public static java.util.List<java.util.Map> resultSetToList(ResultSet rs) throws SQLException {
        if (rs == null) {
            return new ArrayList<>();
        }
        ResultSetMetaData md = rs.getMetaData(); //得到结果集(rs)的结构信息，比如字段数、字段名等
        int columnCount = md.getColumnCount(); //返回此 ResultSet 对象中的列数
        java.util.List array = new ArrayList();
        java.util.Map rowData = null;
        while (rs.next()) {
            rowData = new HashMap();
            for (int i = 1; i <= columnCount; i++) {
                rowData.put(md.getColumnName(i), rs.getObject(i));
            }
            array.add(rowData);

        }
        return array;
    }


    /**
     * ResultSet转JSONArray
     * @param rs
     * @return
     * @throws SQLException
     */
    public static JSONArray resultSetToJSONArray(ResultSet rs) throws SQLException{
        return JSONArray.fromObject(resultSetToList(rs));
    }


}
