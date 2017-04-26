package net.xmoshi.nightawk.test.service;

import net.xmoshi.nightawk.test.entity.Employee;

import java.util.List;

public interface FirstService {

    Employee getEmployee(Integer id);

    List<Employee> getEmployees();

}
