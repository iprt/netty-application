# netty用法

## `ctx.writeAndFlush()` 和 `channel.writeAndFlush()` 的区别

### 1. 基本定义

- **`ctx`**：即 ，代表当前 handler 与 pipeline 的上下文。 `ChannelHandlerContext`
- **`channel`**：即 `Channel`，表示底层的网络连接。

### 2. 作用范围

#### `ctx.writeAndFlush(msg)`

- **事件传播从当前 handler 往后** 传播。
- 数据从 **当前 handler** 后面的 handler 继续处理（例如编码、加密等）。
- 通常用于希望消息继续在 pipeline 里被其它 handler 处理的场景（如解码、日志等）。

#### `ctx.channel().writeAndFlush(msg)` 或 `channel.writeAndFlush(msg)`

- **直接作用于整个 channel**
- 数据会从 pipeline 的头部或尾部（取决于实际实现）发送出去，有可能再次经过所有 outbound handler。
- 无论在哪个 handler 里调用，都是基于该 channel 对象整体发送数据。

### 3. 场景差异

- 如果你在 handler 内，**且希望消息按正常 pipeline 顺序流转传递**，\
  推荐用 `ctx.writeAndFlush()`。
- 如果你想**从 pipeline 最前面或整体统一地写消息**，可以用 `channel.writeAndFlush()`。

### 4. 举例说明

- `ctx.writeAndFlush(msg)`

> 在 handler A 里调用，则消息会从 handler A 后一个 outbound handler 开始处理。
>

- `ctx.channel().writeAndFlush(msg)`

> 消息会从 pipeline 的尾部（第一个 outbound handler）开始处理。
>

### 5. 总结表

| 方法                      | 消息传播起点                      | 推荐使用场景             |
|-------------------------|-----------------------------|--------------------|
| ctx.writeAndFlush()     | 当前 handler 的下一个 handler     | 继续经过 pipeline 处理逻辑 |
| channel.writeAndFlush() | pipeline 最前或最后（取决于 pipe 结构） | 直接对整个 channel 写出   |

### 6. 小结

> **原则：在 handler 内推荐优先使用 `ctx.writeAndFlush()`，这样更灵活、事件链不断；除非确有需要绕过部分 handler
或全通道发送，才用 `channel.writeAndFlush()`。**

## `ChannelHandlerContext.close()` 与 `Channel.close()` 的区别

### 1. 基本定义

- **(`ctx`)`ChannelHandlerContext`**：
  表示当前 handler 与 pipeline 的上下文。
- **`Channel`**：
  代表底层的网络通道（Socket 连接本身）。

### 2. 两者的作用

#### `ctx.close()`

- 关闭的是**当前 context 所属 channel**，同时会从当前 handler 节点开始向后在 pipeline 中传播 `close` 事件。
- 事件传播链：
  - 只会经过**当前 handler 之后的 handler**，执行各自的 `close` 相关事件回调（如 `handlerRemoved` 等）。

#### `channel.close()`

- 直接针对整个 channel 进行关闭处理，并从**pipeline 的头部**开始向后广播 `close` 事件。
- 事件传播链：
  - 会经过**pipeline 的所有 handler**，每个 handler 都能感知到 channel 的关闭。

### 3. 区别总结

| 比较项  | `ctx.close()`            | `channel.close()`      |
|------|--------------------------|------------------------|
| 作用对象 | 当前 handler 所属的 channel   | 当前 handler 所属的 channel |
| 事件起点 | context 的当前节点（handler）后续 | pipeline 起始节点          |
| 影响范围 | 当前 handler 后面的所有 handler | pipeline 中所有 handler   |
| 推荐用法 | 在 handler 内关闭连接时通常使用     | 一般在需要整体关闭 channel 时使用  |

### 4. 举例说明

- **`ctx.close()`**
  在 handler 里异常时调用，只通知当前 handler 及其后的其它 handler，通常更高效、影响范围更小。
- **`channel.close()`**
  直接对 channel 整体关闭，适合在需要所有 handler 都收到 channel 关闭通知时使用。

### 5. 小结

> **大多数情况下，两者都会导致连接关闭，但事件传播的起点不同，影响到 pipeline 中 handler 能否全部感知到该事件。**

## `pipeline` 的动态添加和移除 Handler 的正确顺序

关于 `pipeline.remove(this)` 和 `pipeline.addLast()` 的顺序，正确的做法是：

> **应当先 `pipeline.addLast(newHandler)`，再 `pipeline.remove(this)`。**
>

### 1. 执行顺序

``` java
ctx.pipeline().

addLast(newHandler);
ctx.

pipeline().

remove(this);
```

- **先添加新 Handler**：让 pipeline 保持完整，确保下一个处理流程有 handler 可以继续处理事件。
- **再移除当前 Handler**：当前 handler 完成使命后，再优雅地退出。

### 2. 如果顺序反过来会怎样？

- 如果**先 remove**，此 handler 立刻从 pipeline 移除，`pipeline.addLast()` 再添加新的 handler 时，当前 handler 已不再
  pipeline 里。如果方法后还有事件传递或代码依赖当前 handler，容易引发空指针异常或事件流断裂。
- 并发环境下，可能导致 pipeline 空档，某些事件来不及处理。

### 3. Netty 执行机制

Netty 的 pipeline 结构本质上是一个 handler 链表，事件会沿 handler 顺序依次传递。如果 pipeline 某时刻没有任何合适的 handler
存在，会导致后续的数据或事件无法正常流转，出现异常行为。

## 结论

- **始终保持 pipeline 的 handler 连续性，保证事件流畅传递**
- **正确顺序：先 `addLast`，再 `remove`**

``` java
// 推荐顺序
ctx.pipeline().

addLast(newHandler);
ctx.

pipeline().

remove(this);
```

**一句话总结**：

> 在 Netty pipeline 中，动态替换 handler 时，必须先加后删，避免事件链“空档”
>
