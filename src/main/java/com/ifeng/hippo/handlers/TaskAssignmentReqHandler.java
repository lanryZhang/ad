/*
* TaskAssignmentHandler.java 
* Created on  202017/5/26 14:50 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.handlers;

import com.ifeng.configurable.Context;
import com.ifeng.core.clean.ShutdownManager;
import com.ifeng.core.distribute.message.BaseMessage;
import com.ifeng.core.distribute.message.MessageFactory;
import com.ifeng.core.distribute.message.MessageType;
import com.ifeng.hippo.entity.TaskFragment;
import com.ifeng.hippo.core.data.DataManager;
import com.ifeng.hippo.worker.WorkerExecutor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.log4j.Logger;
import java.util.List;
import java.util.concurrent.*;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class TaskAssignmentReqHandler extends SimpleChannelInboundHandler<BaseMessage> {
    private volatile Future workFuture;

    private Context context;
    private final static Logger logger = Logger.getLogger(TaskAssignmentReqHandler.class);
    private ExecutorService es = Executors.newFixedThreadPool(1);
    private WorkerExecutor we;
    public TaskAssignmentReqHandler(){}
    public TaskAssignmentReqHandler(Context context){
        this.context = context;
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        context.put("ctx",ctx);

        if (workFuture == null && we == null){
            we = new WorkerExecutor(context);
            ShutdownManager.regist(we);
            workFuture = es.submit(we);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (we != null){
            try {
                if (workFuture != null)
                    workFuture.cancel(true);
                we.cleanup();
                we = null;
                workFuture = null;
            }catch (Exception er){
                logger.error(er);
            }
        }
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, BaseMessage msg) throws Exception {
        try {
           if (msg.getHeader() != null && msg.getHeader().getType() == MessageType.TASK_ASSIGN_RESP) {
                List<TaskFragment> res = (List<TaskFragment>) msg.getBody();
               if (res != null) {
                   logger.debug("get master response, task num : " + res.size());
                   for (TaskFragment item : res) {
                       DataManager.getWorkerQueue().offer(item);
                   }
               }
            } else {
                ctx.fireChannelRead(msg);
            }
        } catch (Exception er){
            logger.error(er);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
            BaseMessage heartBaseMessage = MessageFactory.createHeartBeatReqMessage();
            ctx.writeAndFlush(heartBaseMessage);
        }
    }
}
