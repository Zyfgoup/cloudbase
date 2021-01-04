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
图片看不到 看博客里的吧
[博文](https://blog.csdn.net/qq_40799202/article/details/112189687)
