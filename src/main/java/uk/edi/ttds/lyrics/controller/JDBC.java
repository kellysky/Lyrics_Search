package uk.edi.ttds.lyrics.controller;


import java.sql.*;

public class JDBC {
    public static void main(String[] args) {
        System.out.println("MySQL JDBC Example.");
        Connection conn = null;
        String url = "jdbc:mysql://8.209.74.127:3306/lyrics?autoReconnect=true&useSSL=false";
        String driver = "com.mysql.jdbc.Driver";
        String userName = "root";
        String password = "password";
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, userName, password);

            stmt = conn.createStatement();
            String sql = "select * from Song";
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("singer");
                System.out.println("id = " + id + ", name = " + name);
            }
            // 关闭资源
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) {
                } // ignore
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) {
                } // ignore
            }
        }
    }
}

