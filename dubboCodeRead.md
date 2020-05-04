Dubbo源码解析(一)---架构概述
https://blog.csdn.net/qq_33223299/article/details/91428407
dubbo核心模块
![](https://img-blog.csdnimg.cn/20190611133705407.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzMzMjIzMjk5,size_16,color_FFFFFF,t_70)


核心模块职责介绍：

dubbo ­common 
 通用模块，定义了几乎所有dubbo模块都会使用到的一些通用与业务领域无关的工具类（io处理、日志处理、配置处理、类处理等等），线程池扩展、二进制代码处理、class编译处理、json处理、数据存储接口，系统版本号等等通用的类和接口。   
 
dubbo ­rpc ­api 
   分布式服务框架的核心是rpc，这是最基本的功能，这个模块定义了rpc的一些抽象的rpc接口和实现类，包括服务发布，服务调用代理，远程调用结果及异常，rpc调用网络协议，rpc调用监听器和过滤器等等。该模块提供了默认的基于dubbo协议的实现模块，还提供了hessian、http、rest、rmi、thrift和webservice等协议的实现，还实现了injvm的本地调用实现，灵活性强，非常通用，能够满足绝大多数项目的使用需求，而且还可以自行实现rpc协议。
   
dubbo­ registry ­api 
   注册中心也是最重要的组成部分，它是rpc中的consumer和provider两个重要角色的协调者。该项目定义了核心的注册中心接口和实现。具体实现留给了其它项目。有一个默认的实现模块，组册中心提供了mutilcast、redis和zookeeper等多种方式的注册中心实现，用于不同的使用场景。
   
dubbo­ remoting ­api 
   该模块是dubbo中的远程通讯模块。rpc的实现基础就是远程通讯，consmer要调用provider的远程方法必须通过网络远程通讯实现。该模块定义了远程传输器、终端（endpoint）、客户端、服务端、编码解码器、数据交换、缓冲区、通讯异常定义等等核心的接口及类构成。他是对于远程网络通讯的抽象。提供了诸如netty、mina、grizzly、http、p2p和zookeeper的协议和技术框架的实现方式。
   
dubbo ­monitor ­api 
    该模块是dubbo的监控模块，通过该模块可以监控服务调用的各种信息，例如调用耗时、调用量、调用结果等等，监控中心在调用过程中收集调用的信息，发送到监控服务，在监控服务中可以存储这些信息，对这些数据进行统计分析，最终可以产生各种维护的调用监控信息。dubbo默认提供了一个实现，该实现非常简单，只是作为默认的实现范例，生产环境使用价值不高，需要自行实现自己的监控。
    
dubbo ­container ­api 
   dubbo服务运行容器api模块。定义了启动容器列表的包含应用程序入口main方法的类Main；定义了容器接口Container，该接口包含了启动和停止方法定义；还有一些通用的分页功能的相关类。dubbo内置了javaconfig、jetty、log4j、logback和spring几种容器的实现。
   
dubbo ­config ­api 
   从图中可以看出改模块依赖了几乎所有的其它模块，他是dubbo的配置模块，通过它的配置和组装将dubbo组件的多个模块整合在一起给最终的开发者提供有价值的分布式服务框架。通过它的配置可以让开发者选择符合自己需求和使用场景的模块和技术，它定义了面向dubbo使用者的各种信息配置，比如服务发布配置、方法发布配置、服务消费配置、应用程序配置、注册中心配置、协议配置、监控配置等等。另外还有一个spring的配置模块，定义了一些spring的XML Schema，能够大大简化使用dubbo的配置，可以大大降低spring使用场景的学习和配置成本。
   
dubbo ­cluster 
   该模块是dubbo实现的集群模块。支持远程服务的集群，支持多种集群调用策略，包括failover,failsafe,failfast,failback,forking等。并且支持目录服务，注册中心就是目录服务的一种实现，支持负载均衡，该模块还实现了路由器特性，此外还包括合并技术，当将调用请求分发给所有的服务提供者，则会返回多个结果，则将多个结果合并需要用到合并器的实现，该模块也是非常重要的一个模块。
   
dubbo ­admin 
该项目是一个web应用，可以独立部署，它可以管理dubbo服务，通过该管理应用可以连接注册中心，重点是读取注册中心中的信息，也可以通过该应用改写注册中心的信息，从而实现动态的管控服务。该模块的功能也非常简单，对于实际的生产使用场景，还需要对该应用的功能进行扩展和定制，以满足实际的使用场景

服务暴露流程源码解析：
![](https://img-blog.csdnimg.cn/20190115194343846.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzMzNDA0Mzk1,size_16,color_FFFFFF,t_70)
