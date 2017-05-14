# nightawk

nightawk是一个分布式服务追踪库，以[Zipkin](http://zipkin.io/)为基础实现分布式链路追踪

## 目前支持的追踪功能
* http zipkin的[brave](https://github.com/openzipkin/brave)提供了基于http消息追踪的类库
* nightawk-dubbo3 提供[dubbo3](https://github.com/YanXs/dubbo3)框架链路追踪的功能，具体开启方式见[dubbo3](https://github.com/YanXs/dubbo3)教程
* nightawk-redis 提供redis调用监控功能
* nightawk-jdbc  提供如下几种方式监控数据库:

    -- nightawk-commons-jdbc   提供对commons-dbcp BasicDataSource的监控  
    -- nightawk-mybatis 利用mybatis Interceptor机制提供数据库查询监控功能  
    -- nightawk-mysql   提供mysql数据库查询监控功能，如果程序中使用了mybatis推荐使用nightawk-mybatis(不可重复使用)  
    -- nightawk-druid   利用[druid](https://github.com/alibaba/druid)数据源Filter机制，实现TracingFilter监控数据库(与nightawk-mybatis不可重复使用)  
    -- nightawk-tomcat-jdbc  提供对tomcat-jdbc的监控  
* nightawk-mq    提供消息中间件的追踪功能，目前支持kafka,rabbitmq

# ScreenShots

* query trace

![image](http://opvsp0g0q.bkt.clouddn.com/find-trace.png)

* tracing details

![image](http://opvsp0g0q.bkt.clouddn.com/overview.png)

* dependencies

![image](http://opvsp0g0q.bkt.clouddn.com/dp.png)

* span

![image](http://opvsp0g0q.bkt.clouddn.com/dubbo-s2.png)

## 使用文档
* [wiki](https://github.com/YanXs/nighthawk/wiki)





