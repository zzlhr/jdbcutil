package com.lhrsite.jdbc;


import net.sf.json.JSONArray;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

/**
 * 封装存储过程调用函数
 * 按照构造器创建该类，
 * --出参
 * 如果带出参，待commit()执行完毕
 * 通过getOutParamters()方法获取出参对象。
 * 备注：如果未执行commit()调用getOutParamters()方法
 * 虽然可以拿到集合，但是该集合中Param的data属性为空。
 * --结果集
 * 同上，获取必须先执行commit();
 * 结果集中对所有查询进行封装（包括空结果集），
 * 循环进行rs处理时，请自行进行非空判断。
 * 返回ResultSet集合。
 *
 * @author 刘浩然
 * @date 2017/7/27
 *
 * ==============================================
 * 1.0.0
 * @data 2017-10-23
 * @author lhr
 *
 * 1. 自正式版(此版本)以后传入dataSource分两种形式，方法参数形式和类属性形式。
 * 2. getResultArray()方法取消格式化时间,同时maven取消Gson依赖。
 * 3. 推荐使用ORM框架，本工具兼容一切开放DataSource的框架。
 *
 *
 */
public class Procedure {

    private javax.sql.DataSource dataSource;
    /**
     * 入参
     */
    private List<Param> comeParameters;
    /**
     * 参数名
     */
    private String procedureName;
    /**
     * 参数出参
     */
    private List<Param> outParameters;
    /**
     * 结果集json形式
     */
    private JSONArray resultArray;

    /**
     * 结果集list形式
     */
    private List resultList;

    /* 参数总数 */
    private int paramsNumber;

    /** 存储过程返回参数 */
    private String returnStr;

    /** 如果该值为ture 则存储过程有返回值 */
    private Boolean isReturn = false;

    private Connection conn;

    private CallableStatement cstm;


    /**
     * 带入参存储过程
     * @param procedureName     存储过程名
     * @param comeParameters    入参集合
     *
     * =====================================
     * @updateTime 2017-10-23
     * @param dataSource dataSource对象
     */
    @Deprecated
    public Procedure(String procedureName, List<Param> comeParameters, DataSource dataSource){
        this.procedureName = procedureName;
        this.comeParameters = comeParameters;
        this.paramsNumber = comeParameters.size();
        this.dataSource = dataSource;
    }

    /**
     *
     *
     * 带入参存储过程
     * @param procedureName     存储过程名
     * @param comeParameters    入参集合
     * @author lhr
     * @date 2017-10-23
     */
    public Procedure(String procedureName, List<Param> comeParameters){
        this.procedureName = procedureName;
        this.comeParameters = comeParameters;
        this.paramsNumber = comeParameters.size();
    }



    /**
     * 不带入参的存储过程
     * @param procedureName     存储过程名
     */
    @Deprecated
    public Procedure(String procedureName, DataSource dataSource){
        this.procedureName = procedureName;
        this.paramsNumber = 0;
        this.dataSource = dataSource;
    }


    /**
     * 不带入参的存储过程
     * @param procedureName     存储过程名
     * @author lhr
     * @data 2017-10-23
     */
    public Procedure(String procedureName){
        this.procedureName = procedureName;
        this.paramsNumber = 0;
    }

    /**
     * 带入参和出参（过时）
     * @param procedureName     存储过程名
     * @param comeParameters    入参
     * @param outParameters     出参
     * @param dataSource
     */
    @Deprecated
    public Procedure(String procedureName,
                     List<Param> comeParameters,
                     List<Param> outParameters,
                     DataSource dataSource){
        this.procedureName = procedureName;
        this.comeParameters = comeParameters;
        this.outParameters = outParameters;
        this.paramsNumber = comeParameters.size() + outParameters.size();
        this.dataSource = dataSource;
    }




    /**
     * 带入参和出参
     * @param procedureName     存储过程名
     * @param comeParameters    入参
     * @param outParameters     出参
     */
    public Procedure(String procedureName,
                     List<Param> comeParameters,
                     List<Param> outParameters){
        this.procedureName = procedureName;
        this.comeParameters = comeParameters;
        this.outParameters = outParameters;
        this.paramsNumber = comeParameters.size() + outParameters.size();
    }



    public void commit() throws SQLException {
        //拼接存储过程执行sql
        /* 获取基本sql */
        String baseSql = this.getBaseSql();
        if (dataSource == null){
            // 如果此时dataSource为null表示开发者使用setDataSource方式
            // 初始化的dataSource.
            // 为了兼容之前的版本，使用传参方式初始化所以再次加入判断
            dataSource = DataSourceUtil.getDataSource();
            if (dataSource == null){
                throw new RuntimeException("未初始化DataSource.");
            }
        }
        /* 创建CallableStatement对象 */

        conn = dataSource.getConnection();
        cstm = conn.prepareCall(baseSql);
        /*
            拼接参数,当参数大于一个，即有参数时执行拼接参数;
        */
        if (paramsNumber > 0){
            cstm = this.setParam(cstm);
        }
        /*
            提交
        */
        Boolean hashResult = false;
        try {
            hashResult = cstm.execute();
        }catch (SQLException e){
            throw e;
        }

        /*
            获取结果集
        */
        getResult(hashResult);

        /* 获取出参 */
        getOutParam();



    }
    /**
     * 根据出餐和入参个数获取存储过程sql{call xxxx(?,?,?)}类型
     * @return
     */
    private String getBaseSql(){
        /* 创建参数模板 */
        String procedureModel = "{call "+procedureName+"(#{params})}";

        if (isReturn){
            procedureModel = procedureModel.replace("call","? = call");
        }


        /* 计算参数 */
        paramsNumber = 0;
        if (comeParameters != null){
            paramsNumber += comeParameters.size();
        }
        if (outParameters != null){

            paramsNumber += outParameters.size();
            if (isReturn){
                paramsNumber--;
            }
        }
        if (paramsNumber == 0){
            return procedureModel.replace("#{params}","");
        }
        /**替换params为?,?,?形式*/
        String param = "";
        for (int i = 0; i < paramsNumber; i++){
            param += "?,";
        }
        if (param.length() >= 1){
            //去掉最后的，
            param = param.substring(0,param.length()-1);
        }
        return procedureModel.replace("#{params}",param);
    }
    /**
     * 植入参数
     * @param cstm
     * @return
     * @throws SQLException
     */
    private CallableStatement setParam(CallableStatement cstm) throws SQLException {
        /* 配置入参 */
        if (comeParameters != null){
            String str = "";
            for (int i = 0; i < comeParameters.size(); i++){
                Param param = comeParameters.get(i);

                if (param.getData() == null){
                    cstm.setString(param.getLocation(),null);
                }else {
                    cstm.setString(param.getLocation(), param.getData().toString());
                }

            }
        }
        /* 配置出参 */
        if (outParameters != null){
            for (int i = 0; i < outParameters.size(); i++){
                Param param = outParameters.get(i);
                cstm.registerOutParameter(param.getLocation(),param.getType());
            }
        }
        return cstm;
    }


    /**
     * 结果集转数组
     * @param rs       结果集
     * @return          list
     * @throws SQLException
     */
    public static List resultSetToList(ResultSet rs) throws SQLException {
        if (rs == null) {
            return Collections.EMPTY_LIST;
        }
        //得到结果集(rs)的结构信息，比如字段数、字段名等
        ResultSetMetaData md = rs.getMetaData();
        //返回此 ResultSet 对象中的列数
        int columnCount = md.getColumnCount();
        List list = new ArrayList();
        Map rowData = null;
        while (rs.next()) {
            rowData = new HashMap(columnCount);
            for (int i = 1; i <= columnCount; i++) {
                rowData.put(md.getColumnName(i), rs.getObject(i));
            }
            list.add(rowData);
        }
        return list;
    }


    public JSONArray getResultArray(){
        return JSONArray.fromObject(this.resultList);
    }


    /**
     * 关闭连接，用连接池务必调用该方法，释放连接。
     */
    public void close(){

        try {
            if (cstm != null){
                    cstm.close();
                    cstm = null;
            }
            if (conn != null){
                    conn.close();
                    conn = null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    /**
     * 获取结果集
     * @param hashResult 执行提交返回的值
     * @throws SQLException
     */
    public void getResult(Boolean hashResult) throws SQLException {
        int i = 0;
        resultList = new ArrayList<>();

        while (true) {
            //判断本次循环是否为数据集
            i++;
            if (hashResult) {
                ResultSet rs = cstm.getResultSet();
                // Do something with resultset ...
                List list = JdbcUtil.resultSetToList(rs);
                resultList.add(list);
                rs.close();
                rs = null;
            } else {
                int updateCount = cstm.getUpdateCount();
                if (updateCount == -1) {
                    /*
                        当updateCount为-1时，
                        代表存储过程返回的最后一条数据集
                        跳出循环
                    */
                    break;
                }
                //如果需要加入空结果集打开下面注释
//                resultSets.add(null);
                // Do something with update count ...
            }
             // 每次判断下一个是否为了数据集,
            // 为true表示下一次循环为数据集，false为空.
            hashResult = cstm.getMoreResults();
        }

    }


    /**
     * 获取出参
     * @throws SQLException
     */
    public void getOutParam() throws SQLException {
        /*
            获取出参
         */
        if (outParameters != null && outParameters.size() > 0){
            for (int i = 0; i < outParameters.size(); i++){
                Param param = outParameters.get(i);
                param.setData(cstm.getObject(param.getLocation()));
                outParameters.set(i,param);
            }
        }
    }

}
