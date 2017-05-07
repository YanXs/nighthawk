# nightawk

nightawk是一个分布式服务追踪框架，利用[Zipkin](http://zipkin.io/)收集分布式链路数据，以zipkin UI展示，Zipkin提供多种客户端,nightawk使用java客户端[brave](https://github.com/openzipkin/brave)收集数据
nightawk提供多种追踪功能：

## 目前支持的追踪功能
* nightawk-dubbo 提供[dubbo3](https://github.com/YanXs/dubbo3)框架链路追踪的功能，具体开启方式见[dubbo3](https://github.com/YanXs/dubbo3)教程
* nightawk-redis 提供redis调用监控功能
* nightawk-jdbc  提供如下几种方式监控数据库:

    -- nightawk-commons-jdbc   提供对commons-dbcp BasicDataSource的监控  
    -- nightawk-mybatis 利用mybatis Interceptor机制提供数据库查询监控功能  
    -- nightawk-mysql   提供mysql数据库查询监控功能，如果程序中使用了mybatis推荐使用nightawk-mybatis(不可重复使用)  
    -- nightawk-druid   利用[druid](https://github.com/alibaba/druid)数据源Filter机制，实现TracingFilter监控数据库(与nightawk-mybatis不可重复使用)  
    -- nightawk-tomcat-jdbc  提供对tomcat-jdbc的监控  
* nightawk-mq    提供消息中间件的追踪功能，目前支持kafka,rabbitmq


## 使用方法
* nightawk-dubbo
night-dubbo没有使用dubbo2的Filter做扩展，Filter不够灵活，所以我修改了dubbo2同步请求应答实现方式，并添加了Interceptor接口，[dubbo3](https://github.com/YanXs/dubbo3)
通过Interceptor修改请求，dubbo协议请求对应的Interceptor为BraveDubboClientRequestInterceptor

在spring中配置方式

方式一：
```xml
<dubbo:tracker address="zipkin://127.0.0.1:9411" transport="http" sampler="counting" samplerate="1.0" flushinterval="2"/>
```

方式一的局限是在dubbo内部创建brave收集调用数据，无法收集方法内的数据请求或者redis请求的响应延迟；

方式二(推荐)：
```xml
<dubbo:tracker protocol="zipkin" ref="braveRpcTrackerEngine"/>

<bean id="brave" class="com.github.nightawk.core.brave.BraveFactoryBean">
    <property name="serviceName" value="simpleService1"/>
    <property name="transport" value="http"/>
    <property name="transportAddress" value="127.0.0.1:9411"/>
</bean>

<bean id="braveRpcTrackerEngine" class="com.github.nightawk.dubbo.spring.BraveRpcTrackerEngineFactoryBean">
    <property name="brave" ref="brave"/>
</bean>
```
方式二在外部创建了brave，整个应用可以共享brave，可以收集方法内部的数据库请求等


* nightawk-redis

nightawk-redis使用[Byte Buddy](http://bytebuddy.net/#/)代理机制拦截jedis的请求，详见JedisInterceptor。类JaRedisFactory代替JedisFactory，
JaRedisPool代替JedisPool，JaRedisSentinelPool代替JedisSentinelPool实现

```java
JaRedis.Builder builder = new JaRedis.Builder();
Jedis jedis = builder.build();
String key = UUID.randomUUID().toString();
String value = UUID.randomUUID().toString();
jedis.set(key, value);

JedisPoolConfig config = new JedisPoolConfig();
JaRedisPool pool = new JaRedisPool(config, "127.0.0.1", 6379);
Jedis jedis = pool.getResource();
jedis.set("hello", "world");
```

* nightawk-mybatis

nightawk-mybatis利用mybatis的Interceptor实现扩展点，详见TracingInterceptor类，目前支持mysql、oracle、DB2 SQL查询和更新时间，
如果要支持支持其他数据库需要扩展DBUrlParser

```xml
<bean id="datasourceMySqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
    <property name="dataSource" ref="dataSource_mysql"/>
    <property name="plugins">
        <bean class="com.github.nightawk.jdbc.mybatis.MybatisTracingInterceptor">
            <property name="statementTracer" ref="statementTracer"/>
        </bean>
    </property>
</bean>

<bean id="brave" .../>

<bean id="statementTracer" class="com.github.nightawk.jdbc.StatementTracer">
    <property name="clientTracer" value="#{brave.clientTracer()}"/>
</bean>

<bean id="basicDataSourceInterceptor" class="com.github.nightawk.jdbc.basic.BasicDataSourceTracingInterceptor">
    <constructor-arg ref="statementTracer"/>
</bean>

<bean id="dataSource_mysql" class="com.github.nightawk.jdbc.mybatis.BasicDataSourceAdapter">
    <constructor-arg ref="dataSource_mysql_origin"/>
</bean>

<bean id="dataSource_mysql_origin" class="org.apache.commons.dbcp.BasicDataSource">
    <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
    <property name="url" value="jdbc:mysql://127.0.0.1:3306/test?autoReconnect=true"/>
    <property name="username" value="root"/>
    <property name="password" value="root"/>
</bean>
```

* nightawk-mysql

如果项目中没有使用mybatis框架，对于mysql数据库可以利用MYSQL-JDBC中的StatementInterceptorV2机制拦截statement的执行，nightawk-mysql代码与
[brave-mysql](https://github.com/openzipkin/brave)相同，稍作修改的地方是只关心PreparedStatement的执行情况

```xml
<bean id="brave" .../>

<bean id="statementTracer" class="com.github.nightawk.sphex.StatementTracer">
    <property name="clientTracer" value="#{brave.clientTracer()}"/>
</bean>
    
<bean id="preparedStatementInterceptor" class="com.github.nightawk.sphex.mysql.PreparedStatementInterceptor">
    <property name="statementTracer" ref="statementTracer"/>
</bean>
```
修改URL: database.url=jdbc:mysql://127.0.0.1:3306/test?statementInterceptors=com.nightawk.mysql.PreparedStatementInterceptor

* nightawk-druid

如果项目中使用[druid](https://github.com/alibaba/druid)作为数据源，可以使用TracingFilter监控追踪数据库

```xml
<bean id="brave" .../>

<bean id="statementTracer" class="com.github.nightawk.sphex.StatementTracer">
    <property name="clientTracer" value="#{brave.clientTracer()}"/>
</bean>

<bean id="dataSource_mysql" class="com.alibaba.druid.pool.DruidDataSource" init-method="init"
          destroy-method="close">
    <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
    <property name="url" value="jdbc:mysql://127.0.0.1:3306/test?autoReconnect=true"/>
    <property name="username" value="root"/>
    <property name="password" value="root"/>
    <property name="proxyFilters">
        <list>
           <ref bean="tracingFilter"/>
        </list>
    </property>
</bean>

<bean id="tracingFilter" class="com.github.nightawk.sphex.druid.TracingFilter">
    <property name="statementTracer" ref="statementTracer"/>
</bean>
```

* nightawk-basic
BasicDataSource本身并没有提供Interceptor或者Filter机制对Statement进行拦截，对BasicDataSource进行扩展，扩展类SphexBasicDataSource
以及后面对于DelegatingPreparedStatement的扩展

```xml
<bean id="brave" .../>

<bean id="statementTracer" class="com.github.nightawk.jdbc.StatementTracer">
    <property name="clientTracer" value="#{brave.clientTracer()}"/>
</bean>

<bean id="basicDataSourceInterceptor" class="com.github.nightawk.jdbc.basic.BasicDataSourceTracingInterceptor">
    <constructor-arg ref="statementTracer"/>
</bean>

<bean id="dataSource_mysql_origin" class="com.github.nightawk.jdbc.basic.SphexBasicDataSource">
    <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
    <property name="url" value="jdbc:mysql://127.0.0.1:3306/test?autoReconnect=true"/>
    <property name="username" value="root"/>
    <property name="password" value="root"/>
    <property name="interceptors">
        <list>
            <ref bean="basicDataSourceInterceptor"/>
        </list>
    </property>
</bean>
```

* nightawk-tomcat-jdbc
基于tomcat-jdbc的JdbcInterceptor扩展点，实现对数据库的监控

```xml
<bean id="brave" .../>

<bean id="statementTracer" class="com.github.nightawk.jdbc.StatementTracer">
    <property name="clientTracer" value="#{brave.clientTracer()}"/>
</bean>

<bean id="tracingMBean" class="com.github.nightawk.jdbc.tomcat.TracingInterceptorMBean">
    <constructor-arg ref="statementTracer"/>
</bean>

<bean id="dataSource_tomcat_jdbc" class="org.apache.tomcat.jdbc.pool.DataSource">
    <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
    <property name="url" value="jdbc:mysql://127.0.0.1:3306/test?autoReconnect=true"/>
    <property name="username" value="root"/>
    <property name="password" value="root"/>
    <property name="jdbcInterceptors" value="com.github.nightawk.jdbc.tomcat.TomcatDataSourceTracingInterceptor"/>
</bean>
```

* nightawk-mq:kafka  
Consumer:

```java
Consumer<String, byte[]> consumer = new KafkaConsumer<>(props);
ListenableTracingConsumer<String, String> listenableTracingConsumer =
      new ListenableTracingConsumer<>(consumer, Pattern.compile("test"), new StringDeserializer());
        
// create brave
Brave brave = ...
listenableTracingConsumer.addListener(new AbstractTracingListener<String, String>(brave) {
            @Override
            public void onPayload(Payload<String, String> payload) {
                try {
                    Sleeper.JUST_SLEEP.sleepFor(2000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
listenableTracingConsumer.start();
```

Producer:

```java
Properties props = new Properties();
props.put("tracing.component", brave);
props.put("value.serializer", "com.github.nightawk.mq.kafka.TracingSerializer");
KafkaProducer<String, byte[]> producer = new KafkaProducer<>(props);
......
```

* nightawk-mq:rabbit





