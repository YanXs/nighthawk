# nightawk

nightawk是一个分布式服务追踪框架，利用[Zipkin](http://zipkin.io/)收集分布式链路数据并以瀑布图的形式展示，Zipkin提供多种客户端,nightawk使用java客户端[brave](https://github.com/openzipkin/brave)
nightawk提供多种插件：

## 目前支持的插件
* nightawk-dubbo 该插件提供[dubbo3](https://github.com/YanXs/dubbo3)框架链路追踪的功能，具体开启方式见[dubbo3](https://github.com/YanXs/dubbo3)教程
* nightawk-redis 该插件提供redis调用监控功能
* nightawk-sphex 该插件提供如下几种方式监控数据库读写
    nightawk-mybatis 利用mybatis Interceptor机制提供数据库查询监控功能
    nightawk-mysql   提供mysql数据库查询监控功能，如果程序中使用了mybatis推荐使用nightawk-mybatis(不可重复使用)
    nightawk-druid   利用[druid](https://github.com/alibaba/druid)数据源Filter机制，实现TracingFilter监控数据库(与nightawk-mybatis不可重复使用)

##使用方法
* nightawk-dubbo
night-dubbo没有使用dubbo2的Filter做扩展，Filter不够灵活，所以我修改了dubbo2同步请求应答实现方式，并添加了Interceptor接口，[dubbo3](https://github.com/YanXs/dubbo3)
通过Interceptor修改请求，由于nightawk使用[Zipkin](http://zipkin.io/)收集数据，dubbo协议请求对应BraveDubboClientRequestInterceptor等等，
在spring中配置方式

方式一：
```xml
<dubbo:tracker address="zipkin://127.0.0.1:9411" transport="http" sampler="counting" samplerate="1.0" flushinterval="2"/>
```
方式一在dubbo内部创建brave收集调用数据，无法收集方法内的数据请求或者redis请求；

方式二(推荐)：
```xml
<dubbo:tracker protocol="zipkin" ref="braveRpcTrackerEngine"/>

<bean id="brave" class="net.nightawk.core.brave.BraveFactoryBean">
    <property name="serviceName" value="simpleService1"/>
    <property name="transport" value="http"/>
    <property name="transportAddress" value="127.0.0.1:9411"/>
</bean>

<bean id="braveRpcTrackerEngine" class="net.nightawk.dubbo.spring.BraveRpcTrackerEngineFactoryBean">
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
    <property name="dataSource" ref="dataSource_oracle"/>
     <property name="plugins">
        <bean class="net.nightawk.sphex.mybatis.TracingInterceptor">
            <property name="statementTracer" ref="statementTracer"/>
        </bean>
     </property>
</bean>

<bean id="statementTracer" class="net.nightawk.sphex.StatementTracer">
    <property name="clientTracer" value="#{brave.clientTracer()}"/>
</bean>
   
<bean id="brave" class="net.nightawk.core.brave.BraveFactoryBean">
    <property name="serviceName" value="mybatis-tracing"/>
    <property name="transport" value="http"/>
    <property name="transportAddress" value="127.0.0.1:9411"/>
</bean>
```

* nightawk-mysql
如果项目中没有使用mybatis框架，对于mysql数据库可以利用MYSQL-JDBC中的StatementInterceptorV2机制拦截statement的执行，nightawk-mysql代码与
[brave-mysql](https://github.com/openzipkin/brave)相同，稍作修改的地方是只关心PreparedStatement的执行情况

```xml
<bean id="brave" class="net.nightawk.core.brave.BraveFactoryBean">
    <property name="serviceName" value="mybatis-tracing"/>
    <property name="transport" value="http"/>
    <property name="transportAddress" value="192.168.150.132:9411"/>
</bean>

<bean id="statementTracer" class="net.nightawk.sphex.StatementTracer">
    <property name="clientTracer" value="#{brave.clientTracer()}"/>
</bean>
    
<bean id="preparedStatementInterceptor" class="net.nightawk.sphex.mysql.PreparedStatementInterceptor">
    <property name="statementTracer" ref="statementTracer"/>
</bean>
```
修改URL: database.url=jdbc:mysql://127.0.0.1:3306/test?statementInterceptors=com.nightawk.mysql.PreparedStatementInterceptor

* nightawk-druid
如果项目中使用[druid](https://github.com/alibaba/druid)作为数据源，可以使用TracingFilter监控追踪数据库

```xml
<bean id="brave" class="net.nightawk.core.brave.BraveFactoryBean">
    <property name="serviceName" value="mybatis-tracing"/>
    <property name="transport" value="http"/>
    <property name="transportAddress" value="192.168.150.132:9411"/>
</bean>

<bean id="statementTracer" class="net.nightawk.sphex.StatementTracer">
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

<bean id="tracingFilter" class="net.nightawk.sphex.druid.TracingFilter">
    <property name="statementTracer" ref="statementTracer"/>
</bean>
```







