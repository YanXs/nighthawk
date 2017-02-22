package com.nightawk.test.service;

import com.nightawk.test.entity.Employee;

import java.util.List;

public interface FirstService {

    Employee getEmployee(Integer id);

    List<Employee> getEmployees();

}
