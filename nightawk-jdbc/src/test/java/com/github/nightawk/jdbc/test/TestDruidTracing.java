package com.github.nightawk.jdbc.test;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Xs.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:appContext-druid-tracing.xml"})
public class TestDruidTracing extends TestBase{
}
