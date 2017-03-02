package net.nightawk.test.service;

import net.nightawk.test.entity.Employee;
import org.springframework.beans.factory.annotation.Autowired;

public class SecondServiceImpl implements SecondService {

    @Autowired
    private EmployeeDao employeeDao;

    @Override
    public Employee getEmployee(Integer id) {
        return employeeDao.getEmployee(id);
//        return new Employee();
    }
}
