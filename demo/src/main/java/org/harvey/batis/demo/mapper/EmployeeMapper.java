package org.harvey.batis.demo.mapper;


import org.harvey.batis.annotation.Param;
import org.harvey.batis.demo.entity.Employee;

import java.util.List;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-07-31 23:21
 */
public interface EmployeeMapper {
    List<Employee> selectAll();

    List<Employee> select(Employee employee);

    List<Employee> selectById(@Param("id") int id);

    List<Employee> selectByIds(@Param("ids") int[] id);

    int delById(int id);
/*暂不测试    List<Employee> orderByAsc(@Param("column") String column);

    List<Employee> orderByDesc(@Param("column") String column);

    List<Employee> dividePage(@Param("start") int start, @Param("len") int len);

    int getLen();

    int addEmployee(Employee employee);

    int delById(int id);

    int delByIds(int[] ids);

    int updateByMessage(@Param("id") int id, @Param("customerId") int customerId, @Param("employeeDate") Date employeeDate);

    int update(Employee employee);*/

}
