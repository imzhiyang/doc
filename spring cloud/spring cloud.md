## Eurake
服务注册中心，提供所有的provider可用列表。检测可用的列表，移除不可用列表
## provider
spring.profiles.active 来激活spring的环境配置信息；默认在classpath:application-profile.yml或者classpath:application-profile.yaml等文件可配置

## ribbon （服务消费者）

    ClientRequest.execute
        1.interceptors.execute
          1.1 RibbonLoadBalancerClient.execute
              1.1.1 ZoneAwareLoadBalancer.chooseServer来获取server实例
                    BaseLoadBalancer中存储了providers列表，并直接每次都会强制去ping策略，来设置服务器列表
                    AsyncResolver backgroundTask来同步eurake的配置信息，从config.getEurekaServiceUrlPollIntervalSeconds的进行后台任务检测(timer)
     那么config如果有更新了，是怎么通知ribbon的呢?
     目前看来是没有启动远程config的配置。ribbon首先获取对应的provider列表在本地，并通过心跳与provider之间进行健康监测。


## Spring Boot
### @SpringBootApplication
表示当前是一个spring boot的应用
### CommandLineRunner
在spring boot启动完成之后，会执行一些初始化操作
### RestController与@Controller的差别
在@RestController的注解之后，为什么可以在访问时默认都是json数据，即每次都是RequestResponseBody的方式去处理呢？ 可以查看@RestController的源码可以发现，默认是在类的注解上面是@ResponseBody，所以都会以json格式返回。如果有兴趣，可以具体跟踪源码


    