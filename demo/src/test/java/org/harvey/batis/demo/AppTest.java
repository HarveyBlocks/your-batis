package org.harvey.batis.demo;


import org.harvey.batis.config.Configuration;
import org.harvey.batis.executor.result.ResultSetHandler;
import org.harvey.batis.executor.result.ResultSetWrapper;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;

public class AppTest {
    @Test
    public void test() throws IOException {
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources("org/harvey/batis/demo/mapper");
        Iterator<URL> iterator = resources.asIterator();
        ArrayList<URL> list = Collections.list(resources);
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
        for (URL url : list) {
            System.out.println(url);
        }
    }

    @Test
    public void testJdbc() throws SQLException, ClassNotFoundException {

        //注册驱动,导入包
        Class.forName("com.mysql.cj.jdbc.Driver");
        //获取连接
        String url = "jdbc:mysql://redis:3306/employee?useUnicode=true&serverTimezone=UTC&characterEncoding=utf8&useSSL=true&allowMultiQueries=true";
        String username = "root";
        String password = "123456";
        //以上这些可以写道属性文件.properties里去

        Connection conn = DriverManager.getConnection(url, username, password);

        //定义sql指令,结尾分号可写可不写
        String sql =
                "SELECT id,name FROM tb_employee where id=1 ;" +
                        "SELECT  name FROM tb_employee where id=2;" +
                        "delete from tb_employee where id=12 ;" +
                        "SELECT  name FROM tb_employee where id=2;";
        System.out.println(sql.length());

        //获取执行Sql对象Statement
        Statement stmt = conn.createStatement();

        //执行sql

        stmt.execute(sql);
        ResultSet rs = stmt.getResultSet();
        int updateCount0 = stmt.getUpdateCount();
        //返回影响的行数
        while (rs.next()) {
            int anInt = rs.getInt("id");
            String name = rs.getString("name");
            System.out.println(anInt);
            System.out.println(name);
        }
        boolean moreResults = stmt.getMoreResults();
        int updateCount = stmt.getUpdateCount();
        ResultSet rs2 = stmt.getResultSet();

        //返回影响的行数
        while (rs2.next()) {
            String name = rs2.getString("name");
            System.out.println(name);
        }
        boolean moreResults1 = stmt.getMoreResults();
        int updateCount1 = stmt.getUpdateCount();
        ResultSet rs3 = stmt.getResultSet();

        boolean moreResults2 = stmt.getMoreResults();
        int updateCount2 = stmt.getUpdateCount();
        ResultSet rs4 = stmt.getResultSet();

        //释放资源
        //后开stmt,先释放stmt
        stmt.close();
        //先开conn,后释放conn
        conn.close();
    }
}
