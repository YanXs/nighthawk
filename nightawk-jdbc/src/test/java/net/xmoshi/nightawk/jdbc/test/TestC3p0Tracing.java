package net.xmoshi.nightawk.jdbc.test;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:appContext-c3p0-tracing.xml"})
public class TestC3p0Tracing extends TestBase{
}
