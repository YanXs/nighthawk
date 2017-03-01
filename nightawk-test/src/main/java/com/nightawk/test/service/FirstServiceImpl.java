package com.nightawk.test.service;

import com.nightawk.test.entity.Employee;

import java.util.List;

public class FirstServiceImpl implements FirstService {

    private SecondService secondService;

    public void setSecondService(SecondService secondService) {
        this.secondService = secondService;
    }

    @Override
    public Employee getEmployee(Integer id) {

        return secondService.getEmployee(id);
    }

    @Override
    public List<Employee> getEmployees() {
        return null;
    }
}
