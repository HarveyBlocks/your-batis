package org.harvey.batis;

import org.harvey.batis.io.Resources;
import org.harvey.batis.session.SqlSessionFactory;
import org.harvey.batis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-07-25 16:11
 */
public class App {
    private static SqlSessionFactory sqlSessionFactory;

    static {
        try {
            String resource = "YourBatis-config2.xml";// mybatis核心配置文件
            Reader reader = Resources.getResourceAsReader(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    public static SqlSessionFactory getFactory() {
        return sqlSessionFactory;
    }

    public static void main(String[] args) {
        System.out.println("Hello World");
        // jdbcDemo();
    }

    private static void jdbcDemo() throws ClassNotFoundException, SQLException {
        //注册驱动,导入包
        Class.forName("com.mysql.cj.jdbc.Driver");

        //获取连接
        String url = "jdbc:mysql://redis:3306/employee";
        String username = "root";
        String password = "123456";
        //以上这些可以写道属性文件.properties里去

        Connection conn = DriverManager.getConnection(url, username, password);

        //定义sql指令,结尾分号可写可不写
        String sql = "insert into tb_employee(name,age,salary) values('C君',36,12300.00); ";

        //获取执行Sql对象Statement
        Statement stmt = conn.createStatement();

        //执行sql
        int count = stmt.executeUpdate(sql);
        //返回影响的行数

        System.out.println(count + " hava been done.");

        //释放资源
        //后开stmt,先释放stmt
        stmt.close();
        //先开conn,后释放conn
        conn.close();
    }
}
