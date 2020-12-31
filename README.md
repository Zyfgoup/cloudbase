使用SpringCloud Alibaba搭建项目的一些基础模块
sentinal是流量监控 限流等
common 就是一些公共类的模块
consumer provider就是消费者服务提供者  consumer中使用了restTamplate feign两种调用方式
gateway就是网关  
auth是去权限模块  只做登录返回token 拿到对应权限后 其他的请求在gateway中拦截做判断
数据都是写死的 简单修改即可


