package net.nightawk.jdbc.test;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Xs
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:appContext-tomcat-jdbc-tracing.xml"})
public class TestTomcatJdbcTracing extends TestBase{
}
