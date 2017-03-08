package net.nightawk.sphex.test;

import net.nightawk.test.entity.Employee;
import net.nightawk.test.service.EmployeeDao;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Xs.
 */
public abstract class TestBase {

    @Autowired
    private EmployeeDao employeeDao;

    @Test
    public void test_query_tracing() throws Exception {
        try {
            Employee employee = employeeDao.getEmployee(123);
        }catch (Exception ignored){
        }
        Thread.sleep(2000);
    }

    @Test
    public void test_insert_tracing() {
        Employee employee = new Employee();
        employee.setId(1000);
        employee.setName("cosy");
        employee.setZip("100234");
        employeeDao.insert(employee);
    }
}
