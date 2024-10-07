# auto reconnect client

## core

[Connector.java](./reconnect-client/src/main/java/io/intellij/netty/client/reconnect/Connector.java)

## code

```java
@Slf4j
public class Connector {
    private final Bootstrap bootstrap = new Bootstrap();
    private final SocketAddress serverAddr;
    private final ScheduledExecutorService SES;

    /**
     * 连接到 server 的channel
     */
    @Getter
    private volatile Channel linkChannel;


    public Connector(String host, int port, Consumer<Bootstrap> bootstrapInit) {
        this(host, port, Executors.newSingleThreadScheduledExecutor(), bootstrapInit);
    }

    public Connector(String host, int port, ScheduledExecutorService SES, Consumer<Bootstrap> bootstrapInit) {
        this(new InetSocketAddress(host, port), SES, bootstrapInit);
    }

    public Connector(SocketAddress serverAddr, ScheduledExecutorService SES, Consumer<Bootstrap> bootstrapInit) {
        this.serverAddr = serverAddr;
        this.SES = SES;
        bootstrapInit.accept(bootstrap);
    }

    public void connect() {
        this.doConnect();
    }

    public void connect(long msDelay) {
        this.connect(msDelay, TimeUnit.MILLISECONDS);
    }

    public void connect(long delay, TimeUnit unit) {
        SES.schedule(this::doConnect, delay, unit);
    }

    private void doConnect() {
        try {
            ChannelFuture channelFuture = bootstrap.connect(serverAddr);

            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(@NotNull ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        linkChannel = future.channel();
                        // 服务端断开连接
                        addCloseDetectListener(linkChannel);
                        log.info("connection established");
                    } else {
                        linkChannel = null;
                        log.error("connection lost in bootstrap.connect");
                        // bootstrap.connect(serverAddr).addListener(this);
                        connect(1000);
                    }

                }

                private void addCloseDetectListener(Channel channel) {
                    // if the channel connection is lost, the ChannelFutureListener.operationComplete() will be called
                    channel.closeFuture().addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(@NotNull ChannelFuture future) throws Exception {
                            linkChannel = null;
                            log.error("connection lost in detect listener");
                            connect(1000);
                        }
                    });
                }
            });


        } catch (Exception e) {
            log.error("do Connect Failed", e);
        }

    }

}
```