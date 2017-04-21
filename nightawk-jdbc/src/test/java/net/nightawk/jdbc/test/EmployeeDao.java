package net.nightawk.jdbc.test;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface EmployeeDao {

    Employee getEmployee(@Param("id") Integer id);

    List<Employee> getEmployees();

    void insert(Employee employee);

    void batchInsert(List<Employee> employees);
}
