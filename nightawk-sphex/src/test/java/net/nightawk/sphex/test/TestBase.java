package net.nightawk.sphex.test;

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
        employeeDao.getEmployees();
    }

    @Test
    public void test_insert_tracing() {
        Employee e = new Employee();
        e.setId(123);
        e.setName("hello");
        e.setZip("103203");
        employeeDao.insert(e);
    }
}
