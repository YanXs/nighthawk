package com.nightawk.test.service;

import com.nightawk.test.entity.Employee;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface EmployeeDao {

    Employee getEmployee(@Param("id") Integer id);

    List<Employee> getEmployees();

}
