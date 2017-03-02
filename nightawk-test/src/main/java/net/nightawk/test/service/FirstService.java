package net.nightawk.test.service;

import net.nightawk.test.entity.Employee;

import java.util.List;

public interface FirstService {

    Employee getEmployee(Integer id);

    List<Employee> getEmployees();

}
