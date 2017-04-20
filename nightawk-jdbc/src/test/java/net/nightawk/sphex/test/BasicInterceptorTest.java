package net.nightawk.sphex.test;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:appContext-basic-tracing.xml"})
public class BasicInterceptorTest extends TestBase {
}
