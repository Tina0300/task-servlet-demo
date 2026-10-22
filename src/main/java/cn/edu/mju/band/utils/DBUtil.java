package cn.edu.mju.band.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBUtil {
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String URL = "jdbc:mysql://localhost:3306/taskmaster?useSSL=false&serverTimezone=Asia/Shanghai";
    private static final String USER = "root";
    private static final String PWD = "Root@123456";

    static {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL,USER,PWD);
    }
    public static void close(Connection conn, PreparedStatement pstmt, ResultSet rs){
        try{if(rs!=null) rs.close();}catch (SQLException e){}
        try{if(pstmt!=null) pstmt.close();}catch (SQLException e){}
        try{if(conn!=null) conn.close();}catch (SQLException e){}
    }
    public static void close(Connection conn, PreparedStatement pstmt){
        close(conn,pstmt,null);
    }
}