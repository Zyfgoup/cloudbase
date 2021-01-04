### 使用SpringCloud Alibaba来搭建项目的一些基础模块 拿来即用
### 模块简述
sentinal是流量监控 限流等

common 就是一些公共类的模块

consumer provider就是消费者服务提供者  consumer中使用了restTamplate feign两种调用方式

gateway就是网关 前端的请求都是到网关  由网关再去找对应的服务或者是消费者 

auth是去权限模块  只做登录注册注销 授权在gatewat拦截请求判断

exception 全局异常处理模块

数据都是写死的 简单修改即可 并没有使用到数据库

### 详解
因为之前学过Eureka 所以很多基础的设置就不记录了

几个仓库供学习参考

[Spring Cloud基础教程](https://github.com/dyc87112/SpringCloud-Learning)

[涵盖大部分核心组件使用的Spring Cloud教程](https://github.com/macrozheng/springcloud-learning)

### 1.版本选择

![image-20201217095436779](https://gitee.com/hahup/image/raw/master/img/image-20201217095436779.png)

![image-20201217095452197](https://gitee.com/hahup/image/raw/master/img/image-20201217095452197.png)

### 2.Nacos作为注册中心和配置中心

**注册中心**

下载自行百度 顺便可以把sentinal-dashboard也下载了 跟Eureka不同  nacos可以直接使用jar包启动就行了 

然后访问localhost:8848/nacos 就可以进入页面了  账号密码都是nacos (sentinal的dashboard的登录账号密码都是sentinal)

只需要下面三步 启动就可以注册上nacos里了

```
  <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>
```

![image-20201229195338276](https://gitee.com/hahup/image/raw/master/img/image-20201229195338276.png)

```yaml
server:
  port: 8881
spring:
  application:
    name: cloudstudy-provider
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
```



**配置中心**

导入依赖

```pom
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>

```

```yaml
server:
  port: 9101
spring:
#配置环境
  #profiles:
    #active: dev
  application:
    name: nacos-config-client
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848 #Nacos地址
      config:
        server-addr: localhost:8848 #Nacos地址
        file-extension: yml #这里我们获取的yaml格式的配置

```

项目的配置文件要使用bootstrap.yml  配置信息如上

nacos中的配置文件的dataid的组成格式及与SpringBoot配置文件中的属性对应关系

spring.profiles.active为配置的环境

假设我当前项目用的是dev环境  项目名为consumer  后缀是yml  那么组合起来就是 consumer-dev.yml  就会根据这个去配置中心找对应的文件 读取

```yaml
${spring.application.name}-${spring.profiles.active}.${spring.cloud.nacos.config.file-extension}
```

![image-20201230113347579](https://gitee.com/hahup/image/raw/master/img/image-20201230113347579.png)



对应在nacos上创建配置文件  然后测试一下是否将配置文件记载进来  使用@Value注入

然后调用接口返回值 是否对应配置的值即可验证

![image-20201230113423377](https://gitee.com/hahup/image/raw/master/img/image-20201230113423377.png)



添加其他的配置文件 共享一些公共的配置 例如mysql redis之类的

```yaml
    #共享配置文件
        shared-configs[0]:
          data-id: commom.yml
          group: DEFAULT_GROUP
          refresh: true  #要配置刷新

# 这样也可以共享配置文件
#        extension-configs[0]:
#          - data-id: shareconfig3.yml
#            group: SHARE3_GROUP
#            refresh: true
```



看启动的控制台可以发现加载到了对应的两个配置文件

![image-20201230112237698](https://gitee.com/hahup/image/raw/master/img/image-20201230112237698.png)



### 3.服务消费的方式

**使用RestTemplate**

在之前的例子中，已经使用过`RestTemplate`来向服务的某个具体实例发起HTTP请求，但是具体的请求路径是通过拼接完成的，对于开发体验并不好。但是，实际上，在Spring Cloud中对RestTemplate做了增强，只需要稍加配置，就能简化之前的调用方式。

比如：

```Java
@EnableDiscoveryClient
@SpringBootApplication
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }

    @Slf4j
    @RestController
    static class TestController {

        @Autowired
        RestTemplate restTemplate;

        @GetMapping("/test")
        public String test() {
            String result = restTemplate.getForObject("http://alibaba-nacos-discovery-server/hello?name=didi", String.class);
            return "Return : " + result;
        }
    }

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
```

可以看到，在定义RestTemplate的时候，增加了`@LoadBalanced`注解，而在真正调用服务接口的时候，原来host部分是通过手工拼接ip和端口的，直接采用服务名的时候来写请求路径即可。在真正调用的时候，Spring Cloud会将请求拦截下来，然后通过负载均衡器选出节点，并替换服务名部分为具体的ip和端口，从而实现基于服务名的负载均衡调用。



**使用WebClient**（可以不看）

WebClient是Spring 5中最新引入的，可以将其理解为reactive版的RestTemplate。下面举个具体的例子，它将实现与上面RestTemplate一样的请求调用：

```
@EnableDiscoveryClient
@SpringBootApplication
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }

    @Slf4j
    @RestController
    static class TestController {

        @Autowired
        private WebClient.Builder webClientBuilder;

        @GetMapping("/test")
        public Mono<String> test() {
            Mono<String> result = webClientBuilder.build()
                    .get()
                    .uri("http://alibaba-nacos-discovery-server/hello?name=didi")
                    .retrieve()
                    .bodyToMono(String.class);
            return result;
        }
    }

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

}
```

可以看到，在定义WebClient.Builder的时候，也增加了`@LoadBalanced`注解，其原理与之前的RestTemplate时一样的。关于WebClient的完整例子也可以通过在文末的仓库中查看。

**使用Feign**（建议使用 与平常的SpringBoot项目类似  Controller调用Service层 只不过service层使用feign去调用对应的服务提供者的接口）

上面介绍的RestTemplate和WebClient都是Spring自己封装的工具，下面介绍一个Netflix OSS中的成员，通过它可以更方便的定义和使用服务消费客户端。下面也举一个具体的例子，其实现内容与上面两种方式结果一致：

第一步：在`pom.xml`中增加openfeign的依赖：

```
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

第二步：定义Feign客户端和使用Feign客户端：

```Java
@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }

    @Slf4j
    @RestController
    static class TestController {

        @Autowired
        Client client;

        @GetMapping("/test")
        public String test() {
            String result = client.hello("didi");
            return "Return : " + result;
        }
    }


    @FeignClient("alibaba-nacos-discovery-server")
    interface Client {

        @GetMapping("/hello")
        String hello(@RequestParam(name = "name") String name);

    }

}
```

这里主要先通过`@EnableFeignClients`注解开启扫描Spring Cloud Feign客户端的功能；然后又创建一个Feign的客户端接口定义。使用`@FeignClient`注解来指定这个接口所要调用的服务名称，接口中定义的各个函数使用Spring MVC的注解就可以来绑定服务提供方的REST接口，比如下面就是绑定`alibaba-nacos-discovery-server`服务的`/hello`接口的例子。最后，在Controller中，注入了Client接口的实现，并调用hello方法来触发对服务提供方的调用。

feign相关配置

```yaml
cloudstudy-provider: #对应FeignClient注解上的服务名
    ribbon:
      #    NFLoadBalancerRuleClassName: com.zyfgoup.config.MyRule  #自己写的只取第一个服务
      #    NFLoadBalancerRuleClassName: com.netflix.loadbalancer.RandomRule #配置规则 随机
      #    NFLoadBalancerRuleClassName: com.netflix.loadbalancer.RoundRobinRule #配置规则 轮询
      #    NFLoadBalancerRuleClassName: com.netflix.loadbalancer.RetryRule #配置规则 重试
      #    NFLoadBalancerRuleClassName: com.netflix.loadbalancer.WeightedResponseTimeRule #配置规则 响应时间权重
      NFLoadBalancerRuleClassName: com.netflix.loadbalancer.BestAvailableRule #配置规则 最空闲连接策略
      ConnectTimeout: 500 #请求连接超时时间
      ReadTimeout: 1000 #请求处理的超时时间
      OkToRetryOnAllOperations: true #对所有请求都进行重试
      MaxAutoRetriesNextServer: 2 #切换实例的重试次数
      MaxAutoRetries: 1 #对当前实例的重试次数
```



**深入思考**

不论我用的是`RestTempalte`也好、还是用的`WebClient`也好，还是用的`Feign`也好，似乎跟我用不用Nacos没啥关系？我们在之前介绍Eureka和Consul的时候，也都是用同样的方法来实现服务调用的，不是吗？

确实是这样，对于Spring Cloud老手来说，就算我们更换了Nacos作为新的服务注册中心，其实对于我们应用层面的代码是没有影响的。那么为什么Spring Cloud可以带给我们这样的完美编码体验呢？实际上，这完全归功于Spring Cloud Common的封装，由于在服务注册与发现、客户端负载均衡等方面都做了很好的抽象，而上层应用方面依赖的都是这些抽象接口，而非针对某个具体中间件的实现。所以，在Spring Cloud中，我们可以很方便的去切换服务治理方面的中间件。



### 4.sentinel 流量监控、降级、熔断

下载对应的sentinel-dashboard的jar文件 启动即可  端口号为8080  登录账号密码都是sentinel

导入依赖和相关的配置

```yaml
 <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-alibaba-sentinel</artifactId>
  </dependency>


server:
  port: 8882
spring:
  application:
    name: cloudstudy-cunsumer
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
    sentinel:
      transport:
        dashboard: localhost:8080
        port: 8719   #这是sentinel会创建一个HttpServer的端口号 默认8719 被占用则+1+1.... 当设置了一些规则后 传到这个server然后再注册到sentinel上
```



@ResourceSentinel 
value 指定资源名 可自定义 一般与请求路径一直即可
fallback：**失败调用，若本接口出现未知异常**，则调用fallback指定的接口。
blockHandler：sentinel定义的失败调用或限制调用，若本次访问被限流或服务降级，则调用blockHandler指定的接口。
要注意指定的blockHandler方法与原本的方法 **返回的参数要一致**

（假设一个接口里抛出了异常 则会去执行对应的fallback方法  如果又在sentinal设置了限流等 则调用了几次后 返回blockhandler对应的方法）

blockHandlerClass 指定自定义全局的处理服务降级熔断后的返回方法 这样就不用一个方法又对应一个熔断方法

```java
 @GetMapping("/get/{i}")
    @SentinelResource(value = "getI",blockHandler = "getI_Handler")
    public String getI(@PathVariable("i") Integer i) throws InterruptedException {
        //地址 返回值类型 参数在地址后面用数字表示 对应后面的可变长参数
        return String.valueOf(restTemplate.getForObject(PROVIDERSERVICEURL+"/get/{1}",Integer.class,i));
    }

    public String getI_Handler(Integer i,BlockException e){
        return "使用Sentinel实现服务降级";
        
    }
```

![image-20201219001101759](https://gitee.com/hahup/image/raw/master/img/image-20201219001101759.png)



控制台页面如上所示

当启动项目后 需要**先访问一次端口号 才会在控制台有显示**



可以看到有流控、降级、热点

流控是可以直接指定一个qps阈值 超过的话 则会去调用blockhandler对应的方法进行返回

降级是定义一个规则 例如RT（响应时间）超过多久就会进行降级 然后进行熔断一段时间（如下图中的时间窗口） 调用blockhandler

热点就是可以设立某个资源当传入某些某个参数时会进行降级 参数下标从0开始

![image-20201219001537064](https://gitee.com/hahup/image/raw/master/img/image-20201219001537064.png)

![image-20201219001551153](https://gitee.com/hahup/image/raw/master/img/image-20201219001551153.png)

![image-20201219001601187](https://gitee.com/hahup/image/raw/master/img/image-20201219001601187.png)



每次重启服务时，配置的各种规则都是消失，所以需要做规则的持久化

```yaml
    <dependency>
            <groupId>com.alibaba.csp</groupId>
            <artifactId>sentinel-datasource-nacos</artifactId>
    </dependency>  
    
    
    sentinel:
      transport:
        dashboard: localhost:8858
        port: 8719
      datasource:
        ds1:
          nacos:
            server-addr: localhost:8848
            dataId: cloudstudy-consumer
            groupId: DEFAULT_GROUP
            data-type: json
            rule-type: flow
```

打开nacos控制台 创建配置

![image-20201219004449501](https://gitee.com/hahup/image/raw/master/img/image-20201219004449501.png)

```json
[
    {
        "resource": "getI",
        "limitApp": "default",
        "grade": 1,
        "count": 1,
        "strategy": 0,
        "controlBehavior": 0,
        "clusterMode": false
    }
]
```

**resource**是资源名
**limitApp**是来源应用
**grade**是阈值类型，**0是线程，1是QPS**
**count**是阈值
**strategy**是流控模式，**0是直接，1是关联，2是链接**
**controlBehavior**是流控效果，**0是直接，1是warm up，2是排队**
**clusterMode**是集群模式
这样就配置成功每次运行时都会使byGlobalRescource按照QPS快速直接，阈值为1非集群环境下限流，实现了持久化。



**全局服务降级方法**

定义一个全局的服务降级处理类

```java
public class ConsumerBlockHandler {
    public static String handlerException1(BlockException e){
        return "全局服务降级方法1";
    }

    public static String handlerException2(BlockException e){
        return "全局服务降级方法2";
    }
}

//使用时 只需要对应的接口上使用注解 定义对应的处理类和处理的方法

 @SentinelResource(value = "global",blockHandlerClass = ConsumerBlockHandler.class,blockHandler = "handlerException1" )
```



**使用feign的服务降级**

（启动类要用@EnableFeignClients）

在consumer模块中 定义service层 但是不需要具体实现

只需要使用FeignClient 配置对应的服务提供的ming  要使用服务降级方法  则可以配置对应类

对应的fallback处理类 继承了对应的服务 在对应的方法上实现服务降级时的返回即可

```yaml
feign:
  sentinel:
    enabled: true
```

![image-20201229193814753](https://gitee.com/hahup/image/raw/master/img/image-20201229193814753.png)

![image-20201229193851450](https://gitee.com/hahup/image/raw/master/img/image-20201229193851450.png)



![image-20201229193937205](https://gitee.com/hahup/image/raw/master/img/image-20201229193937205.png)

### 5.Gateway 网关

[具体的一些断言、过滤可看这篇文章](https://juejin.cn/post/6844903982599684103)

需要用到的依赖

```pom
 	<dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>

        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
        </dependency>
```



创建一个启动类 启动服务注册与发现

![image-20201229192519641](https://gitee.com/hahup/image/raw/master/img/image-20201229192519641.png)



配置文件

```yaml
server:
  port: 8888
spring:
  redis:
    host: localhost
    password: 123456
    port: 6379
  application:
    name: api-gateway
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true  #表明gateway开启服务注册和发现的功能，并且spring cloud gateway自动根据服务发现为每一个服务创建了一个router，这个router将以服务名开头的请求路径转发到对应的服务。
          lowerCaseServiceId: true   #是将请求路径上的服务名配置为小写（因为服务注册的时候，向注册中心注册时将服务名转成大写的了），比如以/service-hi/*的请求路径被路由转发到服务名为service-hi的服务上。
      routes:
        - id: cloudsyudy-comsumer
          uri: lb://cloudstudy-consumer
          predicates: # 断言，路径相匹配的进行路由  test在实际应用中应为某个模块的名字 例如/user/**
            - Path=/api/test/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10 #每秒允许处理的请求数量
                redis-rate-limiter.burstCapacity: 20 #每秒最大处理的请求数量
                key-resolver: "#{@ipKeyResolver}" #限流策略，对应策略的Bean
            - StripPrefix=2  #会把/api/test/去掉  然后如果匹配上的话 请求路径就是cloudstudy-consumer的地址+/**
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
```

启动服务注册与发现和服务名配置为小写的配置（注册上nacos的服务名默认是大写的）



这里只使用了简单的一个路径匹配的断言 和 去除前缀的过滤

这样只要我浏览器访问localhost:8888/api/test/**  后面随意写 然后会去找到  cloudstudy-consumer这个服务名

然后将localhost:8888替换成 cloudstudy-cloud的地址 然后 去除/api/test  把后面的接上 去访问对应的接口

consumer中又使用feign去调用对应的provider即可。当然也可以在这里直接到provider 看个人需要



ip限制  配置文件上都有注释

```java
@Configuration
public class RedisRateLimiterConfig {
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getRemoteAddress().getHostName());
    }
}
```



问题：如果我配置了多个不同的id  每个uri都是同一个消费者，但是断言是不同的 好像是不能去匹配最匹配的



### 6.权限管理

由于使用前后端分离 所以这里使用JWT的方式来作为认证(Token详解请百度)

简单的说就是登陆的时候 根据登录用户的信息生成jwt，然后附在响应头上或者在响应体中返回给前端，vue（我用vue写的前端）中拿到token 存起来 然后每次请求都在请求头上带上token即可

之前单个SpringBoot项目 我是用Shiro来做前后端分离（[项目](https://github.com/Zyfgoup/conferenceroomback)，这个项目是之前的课设项目，就使用到了shiro+jwt），这次用SpringSecurity来实现（其实理解了跟Shiro也是差不多的）

先说大概的实现思路

1.gateway中可以定义filter 拦截所有的请求  那么我就可以将注册、登录、注销不拦截，其余的都进行拦截判断三点：

(1)请求头中和Redis是否有token

(2)是否过期（token生成时可以定义过期时间 比对当前日期）

(3)进行匹配Redis中存放的对应的用户权限（目前是将角色和资源url权限放到一起了,但是在匹配的时候匹配的是路径，所以如果需要都实现 则也可以将角色放到redis中 先判断角色 再判断url ,也可以单拿到userid获取角色  然后匹配登录成功时存放的角色权限 来比对）

那么只需要我们在登录成功后 将登录用户的的token、权限信息存到Redis中即可实现

2.在auth模块集成SpringSecurity 实现登录注册注销即可  原本应该还有授权 但是授权我们提到gateway中实现了 所以并没有什么存在意义，但是在模块中也还是实现了



**具体实现：**

具体代码、依赖就不贴了  看模块内的代码就好了

先定义相关的Security配置

![image-20210104163051344](https://gitee.com/hahup/image/raw/master/img/image-20210104163051344.png)

构造密码加密方式的Bean

![image-20210104163114884](https://gitee.com/hahup/image/raw/master/img/image-20210104163114884.png)

配置具体的用户信息实现类和密码的加密方式  

![image-20210104163158551](https://gitee.com/hahup/image/raw/master/img/image-20210104163158551.png)

实现UserDetailService这个接口  只有一个方法

![image-20210104163303270](https://gitee.com/hahup/image/raw/master/img/image-20210104163303270.png)

这个方法其实就是根据username去查询数据库 看用户是否存在，如果存在则构建一个UserDetails的实例，包含**用户名、密码、权限**

![image-20210104163335469](https://gitee.com/hahup/image/raw/master/img/image-20210104163335469.png)



再回到配置类,配置了登录、注册、注销的请求路径

还有配置两个filter（认证和授权，授权其实没啥用）和一个filter错误的统一处理（感觉没啥用）

注意：由于filter、handler构建的比context早 如果在这些类里面使用Autowire可能无效 所以用过构造器注入的方式注入要用的bean

```java
@Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                //这两个请求 不拦截
                .antMatchers(HttpMethod.POST, "/login","/register").permitAll()
                .anyRequest().authenticated()
                .and()
                .logout()
                .logoutUrl("/logout")
            //注销处理
                .logoutSuccessHandler(new MyLogoutHandler(redisTemplate,jwtUtils))
                .and()
            //这是认证
                .addFilterBefore(new JWTAuthenticationFilter(authenticationManager(), redisTemplate,jwtUtils), UsernamePasswordAuthenticationFilter.class)
            //这是授权
                .addFilterBefore(new JWTAuthorizationFilter(authenticationManager(),redisTemplate,jwtUtils), UsernamePasswordAuthenticationFilter.class)
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling().authenticationEntryPoint(new JWTAuthenticationEntryPoint());
    }
```



重点只讲认证的filter

attemptAuthentication()方法就是根据请求拿到界面输入的用户名密码 然后去认证

认证的过程其实就是对密码使用前面注入的加密的bean 去加密 然后匹配 UserDetailsService实现类方法里获取到的UserDetail实例 比对密码是否正确

由于前后端分离 数据都是json格式 所以这里要将request里的数据转换成User类

successfulAuthentication()方法就是认证成功后调用的方法，认证成功则生成token 放入响应头中

 `AuthUser authUser = (AuthUser) authResult.getPrincipal();`

这行代码就是拿的前面构建的UserDetail实例

里面有对应的权限

那么就可以将token、权限存放到redis中，key由userid来构建(token是根据userid生成的 网上也有一些可以直接根据实体类来生成 自己选择即可)

认证失败则返回对应的错误信息即可

```java
/**
     * 从请求拿到账号密码 然后走到定义的userDetailsServiceImpl的方法
     * loadUserByUsername中 根据username 去拿到数据库的user 构建成一个实例
     * 然后和token比对  密码使用配置里定义好的加密方式
     * @param request
     * @param response
     * @return
     * @throws AuthenticationException
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)  {
        //将json数据转化为User对象
        User user = jsonToUser(request);

        String username = user.getUsername();
        String password = user.getPassword();
        return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

    /**
     * 上面方法验证成功后便执行到这里  生成jwttoken 返回即可
     * @param request
     * @param response
     * @param chain
     * @param authResult
     * @throws IOException
     * @throws ServletException
     */
    @SneakyThrows
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        AuthUser authUser = (AuthUser) authResult.getPrincipal();

        //生成token
        String jwtToken = jwtUtils.generateToken(authUser.getId());
        //application/json
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Authorization",jwtToken);
        //将Authorization在响应首部暴露出来
        response.setHeader("Access-control-Expose-Headers", "Authorization");

        //token
        String key = "JWT" + authUser.getId() + ":";
        //权限
        String authKey = key + ":Authorities";

        //jwtUtils.getExpire()  配置文件配置的过期时间  使用config配置中心 可以动态改
        redisTemplate.opsForValue().set(key,jwtToken,jwtUtils.getExpire(),TimeUnit.SECONDS);

        redisTemplate.opsForValue().set(authKey, JSONObject.toJSONString(authUser.getAuthorities()), jwtUtils.getExpire() , TimeUnit.SECONDS);

        response.getWriter().write(JSONObject.toJSONString(Result.succ(jwtToken)));
    }


    /**
     * 认证失败 则到这里 根据异常判断是账号不存在 还是密码错误
     * @param request
     * @param response
     * @param failed
     * @throws IOException
     * @throws ServletException
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        log.error("登录失败",failed);
        Result result = null;
        int status = 401;
        if (failed instanceof BadCredentialsException){
            result = Result.fail(401,null, "用户名或者密码不正确");
        }
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
        response.getWriter().write(JSONObject.toJSONString(result));
    }


    private User jsonToUser(HttpServletRequest request){
        StringBuffer jb = new StringBuffer();
        String line = null;
        try {
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                jb.append(line);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        User user = JSON.parseObject(jb.toString(),User.class);
        return user;
    }
```



退出就把redis中对应的信息删除即可

![image-20210104164635975](https://gitee.com/hahup/image/raw/master/img/image-20210104164635975.png)



gateway模块中定义Filter

思路也是如上面一开始写的 判断token是否存在、过期 然后匹配权限

需要注意的是 这里还没有实现配置文件中定义的去掉一些前缀路径 所以匹配路径时需要手动去掉进行匹配

```java
@Component
@Slf4j
public class AuthFilter implements GlobalFilter, Ordered {
    AntPathMatcher antPathMatcher = new AntPathMatcher();

    /**
     * 登录注册注销放行
     */
    private static final String[] EXCLUSIONURLS = {"/api/auth/login","/api/auth/register","/api/auth/logout"};


    private JwtUtils jwtUtils = new JwtUtils();

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String headerToken = request.getHeaders().getFirst("Authorization");
        log.info("headerToken:{}", headerToken);
        //1、只要带上了token， 就需要判断Token是否有效
        if ( !StringUtils.isEmpty(headerToken) && !verifierToken(headerToken)){
            return getVoidMono(response, 401, "token无效");
        }
        String path = request.getURI().getPath();
        log.info("request path:{}", path);
        //2、判断是否是过滤的路径， 是的话就放行
        for (String exclusionurl : EXCLUSIONURLS) {
            if (path.equals(exclusionurl)){
                return chain.filter(exchange);
            }
        }

        //3、判断请求的URL是否有权限
        boolean permission = hasPermission(headerToken , path);
        if (!permission){
            //gateway不能使用web依赖
            return getVoidMono(response, 403, "无访问权限");
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private boolean verifierToken(String headerToken){
            Claims claim = jwtUtils.getClaimByToken(headerToken);
            String userid = claim.getSubject();
            //去redis找是否有  校验是否有效
            String redisToken = redisTemplate.opsForValue().get("JWT"+userid+":");
            if ("".equals(redisToken)||!redisToken.equals(headerToken)) {
                log.error("token不合法，检测不过关");
                return false;
            }
            //校验超时
            if(claim == null || jwtUtils.isTokenExpired(claim.getExpiration())) {
                // token过期了
                log.error("token已经过期");
                return false;
            }

            return true;

    }

    private boolean hasPermission(String headerToken, String path){
            if (StringUtils.isEmpty(headerToken)){
                return false;
            }

            String userid = jwtUtils.getClaimByToken(headerToken).getSubject();
            //生成Key， 把权限放入到redis中
            String key = "JWT" + userid+ ":";
            String authKey = key + ":Authorities";

            String authStr = redisTemplate.opsForValue().get(authKey);
            if (StringUtils.isEmpty(authStr)){
                return false;
            }

            //去掉前1个
            String[] str = path.split("/");
            StringBuilder newPath = new StringBuilder("/");
            //从第三位 因为/../../  第一个/前面也是有的 只是为空
            for (int i = 2; i <str.length-1 ; i++) {
                newPath.append(str[i]+"/");

            }
            newPath.append(str[str.length-1]);


            List<Authority> authorities = JSON.parseArray(authStr , Authority.class);
            return authorities.stream().anyMatch(authority -> antPathMatcher.match(authority.getAuthority(), newPath.toString()));

    }

    private Mono<Void> getVoidMono(ServerHttpResponse response, int i, String msg) {
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.setStatusCode(HttpStatus.OK);
        Result failed = Result.fail(i, null,msg);
        byte[] bits = JSON.toJSONString(failed).getBytes();
        DataBuffer buffer = response.bufferFactory().wrap(bits);
        return response.writeWith(Mono.just(buffer));
    }
}

```



**测试**

注意 数据是json格式

可以看到返回的数据和响应头都带有token

![image-20210104170228824](https://gitee.com/hahup/image/raw/master/img/image-20210104170228824.png)



在构建UserDetail实例时  模拟返回了一个url权限   /**会去匹配所有

![image-20210104170424430](https://gitee.com/hahup/image/raw/master/img/image-20210104170424430.png)



访问consumer模块的资源  在header中带着token  可以看到正常返回了（返回的信息涉及到下一节的全局异常处理）

![image-20210104170508949](https://gitee.com/hahup/image/raw/master/img/image-20210104170508949.png)

![image-20210104170600577](https://gitee.com/hahup/image/raw/master/img/image-20210104170600577.png)

没有对应权限时

![image-20210104171351659](https://gitee.com/hahup/image/raw/master/img/image-20210104171351659.png)



### 7.全局异常处理

exception模块

定义基本Exception 

![image-20210104172542787](https://gitee.com/hahup/image/raw/master/img/image-20210104172542787.png)

ErrorCode是枚举类 里面定义了所有的异常的信息

![image-20210104172630510](https://gitee.com/hahup/image/raw/master/img/image-20210104172630510.png)

自定义异常时，只需要继承BaseException 然后调用父类的构建方法传入对应的ErrorCode即可

![image-20210104172645019](https://gitee.com/hahup/image/raw/master/img/image-20210104172645019.png)



全局异常处理类

通过返回ResponseEntity这个实体类  其实就是Response.getWriter().write(...)的意思

要传入Header、HttpStatus和body  body就使用Result 传入自定义异常的自定义错误码和错误信息

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 通用  具体的异常信息在ErrorCode和对应的自定义异常类里定义
     * @param ex
     * @param request
     * @return
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<?> handleAppException(BaseException ex, HttpServletRequest request){
        return new ResponseEntity<>(Result.fail(ex.getErrorCode(),ex.getErrorMsg(),null),new HttpHeaders(),ex.getStatus());
    }

```

![image-20210104170508949](https://gitee.com/hahup/image/raw/master/img/image-20210104170508949.png)

![image-20210104170600577](https://gitee.com/hahup/image/raw/master/img/image-20210104170600577.png)



### 8.待解决

将全部魔法值 定义到相应的常量管理类 一些可配置的值放到配置文件中

在gateway中每次请求都要去解析token拿到userid  不知道解析的速度快不快  如果能存到session中是不是更快  session中没有才去解析token?








