/*
* TaskAssignmentRespHandler.java 
* Created on  202017/5/26 14:51 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.handlers;

import com.ifeng.core.distribute.message.BaseMessage;
import com.ifeng.core.distribute.message.MessageFactory;
import com.ifeng.core.distribute.message.MessageType;
import com.ifeng.hippo.entity.KeyValuePair;
import com.ifeng.hippo.entity.TaskFragment;
import com.ifeng.hippo.core.data.DataManager;
import com.ifeng.hippo.master.Master;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class TaskAssignmentRespHandler extends SimpleChannelInboundHandler<BaseMessage> {
    private final static Logger logger = Logger.getLogger(TaskAssignmentRespHandler.class);

//    private ConcurrentLinkedQueue<List<TaskFragment>> queue;
    public TaskAssignmentRespHandler(){
//        this.queue = queue;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, BaseMessage msg) throws Exception {
        try {
            if (msg.getHeader().getType() == MessageType.TASK_ASSIGN_REQ) {

                int taskParallel = 1;
                try {
                    //数据内容：int[] { taskParallel,taskId,errorCount,successCount}
                    List<KeyValuePair<String, Integer>> list = (List<KeyValuePair<String, Integer>>) msg.getBody();

                    for (KeyValuePair<String, Integer> item : list) {
                        /** worker 的并行度（最大运行数量） */
                        if ("taskParallel".equals(item.getK())) {
                            taskParallel = item.getV();
                        } else if (DataManager.getProcessCount().containsKey(item.getK())) {
                            DataManager.getProcessCount().get(item.getK()).addAndGet(item.getV());
                            String[] arr = item.getK().split("_");
                            if (arr.length > 1) {
                                logger.info("get worker " + ctx.channel().remoteAddress().toString() + " report " + item.getK() + " task id:" + arr[0] + " num:" + item.getV() +
                                        " total " + arr[1] + " num:" + DataManager.getProcessCount().get(item.getK()).get());
                            } else {
                                logger.error("error report data format from :" + ctx.channel().remoteAddress().toString() + ",Key:" + item.getK() + " value:" + item.getV());
                            }
                        } else {
                            DataManager.getProcessCount().put(item.getK(), new AtomicInteger(item.getV()));
                        }
                    }
                } catch (Exception er) {
                    taskParallel = 1;
                } finally {
                }
                List<TaskFragment> res = new ArrayList<>();


                int taskCount = Master.getTaskAssignAvg();
                if (taskCount >= taskParallel) {
                    taskCount = taskParallel;
                }
                if (taskCount == 0) {
                    taskCount = 1;
                }

                for (int i = 0; i < taskCount; i++) {
                    /** 取出任务返回给客户端 */
                    TaskFragment tf = DataManager.getMasterQueue().poll();
                    if (tf != null) {
                        res.add(tf);
                    } else {
                        break;
                    }
                }
                logger.info("get worker request from :" + ctx.channel().remoteAddress().toString() + " for new tasks, task num : " + (res == null ? 0 : res.size()));
                ctx.writeAndFlush(MessageFactory.createTaskAssignmentRespMessage(res));
            } else {
                ctx.fireChannelRead(msg);
            }
        } finally {
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //ctx.fireExceptionCaught(cause);
        cause.printStackTrace();
    }

}
