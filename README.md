# nightawk

nightawk是一个分布式服务追踪框架，利用[Zipkin](http://zipkin.io/)收集分布式链路数据并以瀑布图的形式展示，

nightawk提供多种插件：

## 目前支持的插件
* nightawk-dubbo 该插件提供[dubbo3](https://github.com/YanXs/dubbo3)框架链路追踪的功能，具体开启方式见[dubbo3](https://github.com/YanXs/dubbo3)教程
* nightawk-mybatis 插件利用mybatis Interceptor机制提供数据库查询监控功能
* nightawk-redis 该插件提供redis调用监控功能
* nightawk-mysql 该插件提供mysql数据库查询监控功能，如果程序中使用了mybatis推荐使用nightawk-mybatis(不可重复使用)

##使用方法


