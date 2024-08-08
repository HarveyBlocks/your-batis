package org.harvey.batis.demo;

import org.harvey.batis.demo.entity.Employee;
import org.harvey.batis.demo.mapper.EmployeeMapper;
import org.harvey.batis.demo.utils.SqlSessionFactoryUtils;
import org.harvey.batis.session.SqlSession;
import org.harvey.batis.session.SqlSessionFactory;

import java.util.List;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-07-25 16:11
 */
public class App {
    private static final SqlSessionFactory SQL_SESSION_FACTORY = SqlSessionFactoryUtils.getFactory();
    public List<Employee> selectAll() {
        try (SqlSession sqlSession = SQL_SESSION_FACTORY.openSession()) {
            // 获取BillMapper接口的代理对象
            EmployeeMapper employeeMapper = sqlSession.getMapper(EmployeeMapper.class);
            return employeeMapper.selectAll();
        }
    }

    public static void main(String[] args) {
         System.out.println(new App().selectAll());
    }
}
