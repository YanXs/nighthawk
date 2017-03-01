package com.nightawk.mybatis.test;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface EmployeeDao {

    Employee getEmployee(@Param("id") Integer id);

    List<Employee> getEmployees();

    void insertEmployee(Employee employee);

}
