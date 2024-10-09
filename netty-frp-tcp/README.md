# use netty mock frp

> only tcp

## 实现思路

连接事件的传递

- 用户建立连接
- 用户断开连接
- 客户端建立连接
- 客户端断开连接

数据包的传递

- 用户的input数据
- 真实服务的output数据

定义交互的协议

- 认证
- 数据交互

## 协议定义

![](.assets/frp.png)

## TODO

优化协议交互

- 提升性能
- 提升安全
