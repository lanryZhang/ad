package com.ifeng.hippo.worker;

import com.ifeng.configurable.Configurable;
import com.ifeng.configurable.Context;
import com.ifeng.core.clean.CleanupAware;
import com.ifeng.core.clean.ShutdownManager;
import com.ifeng.core.distribute.codec.MessageDecode;
import com.ifeng.core.distribute.codec.MessageEncode;
import com.ifeng.core.distribute.handlers.HeartBeatRespHandler;
import com.ifeng.hippo.handlers.TaskAssignmentReqHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhanglr on 2016/8/28.
 */
public class Worker implements Configurable,CleanupAware{
    private String hostIp = "127.0.0.1";
    private int port = 8888;
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
    private Context context;
    private static Logger logger = Logger.getLogger(Worker.class);

    public void start() {
        WorkServer worker = new WorkServer();
        ShutdownManager.regist(worker);
        executorService.submit(worker);
    }

    @Override
    public void cleanup() {
        if (!executorService.isShutdown())
            executorService.shutdown();
    }

    class WorkServer implements Runnable, CleanupAware{
        public void connect() {
            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(eventLoopGroup)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.SO_BACKLOG, 10000)
                        .option(ChannelOption.TCP_NODELAY, true)
                        .option(ChannelOption.SO_REUSEADDR, true)
                        .option(ChannelOption.SO_RCVBUF, 1024 * 1024)
                        .option(ChannelOption.SO_SNDBUF,  1024 * 1024)
                        .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator())
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel arg0) throws Exception {
                                arg0.pipeline().addLast(new MessageDecode());
                                arg0.pipeline().addLast(new MessageEncode());
                                arg0.pipeline().addLast(new IdleStateHandler(10,10,20,TimeUnit.SECONDS));
//                                arg0.pipeline().addLast(new HeartBeatRespHandler());
                                /** 用于客户端与服务端通信 */
                                arg0.pipeline().addLast(new TaskAssignmentReqHandler(context));
                            }
                        });

                ChannelFuture f = bootstrap.connect(hostIp, port).sync();
                f.channel().closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                /** 如果报错，每5秒重连一次 */
                executorService.execute(() -> {
                    try {
                        TimeUnit.SECONDS.sleep(5);
                        try {
                            connect();
                            logger.info("reconnect server.");
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                });
            }
        }

        @Override
        public void run() {
            connect();
        }

        @Override
        public void cleanup() {
            if (!executorService.isShutdown())
                executorService.shutdown();
        }
    }

    @Override
    public void config(Context context) {
        this.hostIp = context.getString("hostIp");
        this.port = context.getInt("port");
        this.context = context;
    }
}
