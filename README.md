### 使用SpringCloud Alibaba来搭建项目的一些基础模块 拿来即用

### 版本

Nacos(自行下载) 1.2.1 
SpringBoot 2.2.5 
SpringCloud Hoxton.SR1
springcloud alibaba 2.2.0


### 更新
2021.1.15
整合Swagger2以及swagger-bootstarp-ui
详情请看这篇别人写的博文[gateway整合swagger教学](https://blog.csdn.net/xb895465169/article/details/97967668)
有一些改动 我截图放在我下面详情里的博文里了

2021.1.13
提交了一个根据vue-admin-template修改的一个基本的前端架子
然后跟本项目做了联调 实现了登录注销等、用户信息的获取是在consumer模块中 把信息写死了 admin角色
[前端仓库-VueAdmin](https://github.com/Zyfgoup/VueAdmin)


### 模块描述
aliyunvod 阿里云视频点播相关模块(使用的话 将配置文件里的AccessKey替换成自己的即可)

sentinal 流量监控 限流等（自行下载sentinal dashboard）

common 公共模块  没有使用web依赖所以引用了lombok但是无法使用@Slf4j (要引用依赖logback-classic)

consumer provider 消费者、服务提供者  consumer中使用了restTamplate feign两种调用方式

gateway 网关 前端的请求都是到网关  由网关再去找对应的服务或者是消费者 

auth 权限模块 前后端分离的情况下 使用token  这个模块只做登录注册注销 授权在gatewat拦截请求判断权限
session问题 gateway的webflux的session与http的session不同  我也不知道如何解决 所以没有用到session了

exception 全局异常处理模块（能够处理Controller、Filter等抛出的异常  Filter抛出的异常处理见详解）


### 其他
数据都是写死的 简单修改即可 并没有使用到数据库

每个模块就是一个SpringBoot项目  更多框架的使用见[SpringBoot学习](https://github.com/Zyfgoup/springboot_study)


### 详解
图片看不到 看博客里的吧
[博文](https://blog.csdn.net/qq_40799202/article/details/112189687)
