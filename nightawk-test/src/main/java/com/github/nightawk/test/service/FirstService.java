package com.github.nightawk.test.service;

import com.github.nightawk.test.entity.Employee;

import java.util.List;

public interface FirstService {

    Employee getEmployee(Integer id);

    List<Employee> getEmployees();

}
