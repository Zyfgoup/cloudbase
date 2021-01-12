### 使用SpringCloud Alibaba来搭建项目的一些基础模块 拿来即用

### 版本

Nacos(自行下载) 1.2.1 
SpringBoot 2.2.5 
SpringCloud Hoxton.SR1
springcloud alibaba 2.2.0

### 模块描述
sentinal 流量监控 限流等（自行下载sentinal dashboard）

common 公共模块  没有使用web依赖 所以无法使用@Slf4j 配置日志时需单独配置日志依赖

consumer provider 消费者、服务提供者  consumer中使用了restTamplate feign两种调用方式

gateway 网关 前端的请求都是到网关  由网关再去找对应的服务或者是消费者 

auth 权限模块 前后端分离的情况下 使用token  这个模块只做登录注册注销 授权在gatewat拦截请求判断权限
session问题 gateway的webflux的session与http的session不同  我也不知道如何解决 所以没有用到session了

exception 全局异常处理模块（能够处理Controller、Filter等抛出的异常  Filter抛出的异常处理见详解）

数据都是写死的 简单修改即可 并没有使用到数据库

### 详解
图片看不到 看博客里的吧
[博文](https://blog.csdn.net/qq_40799202/article/details/112189687)
