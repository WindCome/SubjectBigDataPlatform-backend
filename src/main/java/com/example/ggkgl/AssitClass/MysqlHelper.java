package com.example.ggkgl.AssitClass;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
*Mysql工具类
 **/
public class MysqlHelper {
    public static Connection connectToRemoteMysqlServer(String host,int port,String schema , String user, String password) throws SQLException {
        String url = "jdbc:mysql://"+host+":"+port+"/"+schema+"?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8";
        return connectToRemoteMysqlServer(url,user,password);
    }

    public static Connection connectToRemoteMysqlServer(String url, String user, String password) throws SQLException {
        final String driver="com.mysql.jdbc.Driver";
        return DriverManager.getConnection(url,user,password);
    }

    /**
     * 获取表的所有字段名称
     * @param connection    连接实体
     * @param tableName     表名
     * @return     字段名称列表
     */
    public static List<String> getColumnNameOfTable(Connection connection,String tableName) throws SQLException {
        String sql = "select DISTINCT(COLUMN_NAME) from information_schema.columns where table_name= ?";
        try(PreparedStatement preStmt=connection.prepareStatement(sql)){
            preStmt.setString(1,tableName);
            ResultSet rs=preStmt.executeQuery(sql);
            List<String> result = new ArrayList<>();
            while (rs.next()){
                result.add(rs.getString(1));
            }
            return result;
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 获取字段类型
     * @param connection 连接实体
     * @param tableName     表名
     * @param colName       字段名称
     * @return  类型
     */
    public static Class getColumnType(Connection connection,String tableName,String colName) throws SQLException {
        String sql = "select DATA_TYPE from information_schema.columns where table_name=? " +
                "        AND COLUMN_NAME=? " +
                "        AND TABLE_SCHEMA =(select database()) ";
        try(PreparedStatement preStmt=connection.prepareStatement(sql)){
            preStmt.setString(1,tableName);
            preStmt.setString(2,colName);
            ResultSet rs=preStmt.executeQuery(sql);
            String type = null;
            if(rs.next()){
                type = rs.getString(1);
            }
            return mysqlTypeMapping(type);
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 查询所有数据
     * @param connection    连接实体
     * @param tableName     表名
     * @return  数据列表
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    public static List<HashMap> selectAll(Connection connection,String tableName) throws SQLException {
        List<String> colNames = getColumnNameOfTable(connection,tableName);
        HashMap<String,Class> typeMap = new HashMap<>(colNames.size());
        for(String col:colNames){
            typeMap.put(col,getColumnType(connection,tableName,col));
        }
        String sql = "select * from "+tableName;
        try{
            Statement stmt=connection.createStatement();
            ResultSet rs=stmt.executeQuery(sql);
            List<HashMap> result = new ArrayList<>(rs.getFetchSize());
            while (rs.next()){
                HashMap<String,Object> data = new HashMap<>(colNames.size());
                for(String col:colNames){
                    data.put(col,rs.getObject(col,typeMap.get(col)));
                }
                result.add(data);
            }
            return result;
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static final HashMap<String,Class> mysql2JavaDataType = new HashMap<>();
    static {
        mysql2JavaDataType.put("varchar",String.class);
        mysql2JavaDataType.put("text",String.class);
        mysql2JavaDataType.put("integer",Long.class);
        mysql2JavaDataType.put("tinyint",Integer.class);
        mysql2JavaDataType.put("float",Float.class);
        mysql2JavaDataType.put("boolean",Boolean.class);
        // 其他类型参考https://www.cnblogs.com/jerrylz/p/5814460.html
    }

    public static Class mysqlTypeMapping(String type){
        if (type == null || !mysql2JavaDataType.containsKey(type)) {
            return null;
        }
        return mysql2JavaDataType.get(type);
    }
}
