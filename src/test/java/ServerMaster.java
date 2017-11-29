import com.ifeng.hippo.contances.ActionType;
import com.ifeng.hippo.contances.Platform;
import com.ifeng.hippo.contances.TaskType;
import com.ifeng.hippo.entity.*;
import com.ifeng.hippo.mongo.MongoFactory;
import com.ifeng.hippo.redis.RedisFactory;
import com.ifeng.hippo.task.TaskAction;
import com.ifeng.hippo.utils.DateUtil;
import com.ifeng.mongo.MongoCli;
import com.ifeng.mongo.MongoSelect;
import com.ifeng.mongo.query.Where;
import com.ifeng.redis.RedisClient;
import com.mongodb.client.MongoCursor;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ifeng.hippo.contances.RedisPrefix.UA_ID;

/**
 * Created by zhanglr on 2016/9/19.
 */
public class ServerMaster {
    private MongoCli mongoClient = MongoFactory.createMongoClient();
    private RedisClient redisClient = RedisFactory.newInstance();

    @Test
    public void main() throws Exception {
        mongoClient.changeDb("hippo");
//        mongoClient.getCollection("standard_line");
//        MongoCursor cursor = mongoClient.selectAllCursor(new MongoSelect());
//        while (cursor.hasNext()) {
//            Document doc = (Document) cursor.next();
//            int num = doc.getInteger("num");
//            int time = doc.getInteger("time");
//            Map map = new HashMap<>();
//            map.put("num",num/100);
//            mongoClient.update(map,new Where("time",time));
//        }
//        mongoClient.getCollection("tasks");
//        TaskInfo taskInfo = new TaskInfo();
//        taskInfo.setTaskId(10);
//        taskInfo.setTaskName("job10");
//        taskInfo.setUrl("http://c1.ifengimg.com/iamsImg/2017/08/11/tech/index/rectangle01/index.html");
//        taskInfo.setStatus(0);
//        List<TimePair> list = new ArrayList<>();
//        list.add(new TimePair(900,2359));
//        taskInfo.setTimePairs(list);
//
//        List<String> filtration = new ArrayList<>();
//        filtration.add("2017-08-25");
//        filtration.add("2017-08-26");
//        filtration.add("2017-08-27");
//        filtration.add("2017-08-28");
//        filtration.add("2017-08-30");
//        filtration.add("2017-08-31");
//        filtration.add("2017-09-02");
//        filtration.add("2017-09-03");
//        filtration.add("2017-09-04");
//        filtration.add("2017-09-06");
//        filtration.add("2017-09-07");
//        filtration.add("2017-09-09");
//        filtration.add("2017-09-10");
//        filtration.add("2017-09-11");
//        filtration.add("2017-09-13");
//        filtration.add("2017-09-14");
//        filtration.add("2017-09-16");
//        filtration.add("2017-09-17");
//        filtration.add("2017-09-18");
//        filtration.add("2017-09-20");
//        filtration.add("2017-09-21");
//        filtration.add("2017-09-23");
//        filtration.add("2017-09-24");
//        filtration.add("2017-09-25");
//        filtration.add("2017-09-27");
//        filtration.add("2017-09-28");
//        taskInfo.setFiltration(filtration);
//
////        List<TaskAction> actions = new ArrayList<>();
////        TaskAction ta = new TaskAction(ActionType.PAUSE,1);
////        actions.add(ta);
////        ta = new TaskAction(ActionType.PAUSE,1);
////        actions.add(ta);
////        taskInfo.setActions(actions);
//
//        taskInfo.setBeginDate(DateUtil.parseDate("2017-08-24"));
//        taskInfo.setEndDate(DateUtil.parseDate("2017-09-29"));
//        taskInfo.setCreateTime(DateUtil.parseDate(DateUtil.now()));
//        taskInfo.setIpReusedTimes(10);
//        taskInfo.setLatestModifyTime(System.currentTimeMillis());
//        taskInfo.setParentId(0);
//        taskInfo.setPvToUvRatio(30);
//        taskInfo.setTargetPv(100000);
//        taskInfo.setTaskDescription("");
//        taskInfo.setWaitTimeout(5);
//        taskInfo.setTaskType(TaskType.EV);
//        taskInfo.setPlatform(Platform.PC);
//        taskInfo.setReferer("http://tech.ifeng.com/");
//        TaskInfo taskInfo1 = new TaskInfo();
//        taskInfo1.setTaskId(7);
//        taskInfo1.setTaskName("job5");
//        taskInfo1.setUrl("http://dol.deliver.ifeng.com/s?z=ifeng&c=1&l=58053");
//        taskInfo1.setStatus(0);
//        List<TimePair> list1 = new ArrayList<>();
//        list1.add(new TimePair(900,2359));
//
//        taskInfo1.setTimePairs(list1);
////        actions = new ArrayList<>();
////        ta = new TaskAction(ActionType.PAUSE,1);
////        actions.add(ta);
////        ta = new TaskAction(ActionType.PAUSE,1);
////        actions.add(ta);
////        taskInfo1.setActions(actions);
//
//        taskInfo1.setBeginDate(DateUtil.parseDate("2017-08-23"));
//        taskInfo1.setEndDate(DateUtil.parseDate("2017-08-23"));
//        taskInfo1.setCreateTime(DateUtil.parseDate(DateUtil.now()));
//        taskInfo1.setIpReusedTimes(1);
//        taskInfo1.setLatestModifyTime(System.currentTimeMillis());
//        taskInfo1.setParentId(6);
//        taskInfo1.setPvToUvRatio(30);
//        taskInfo1.setTargetPv(120);
//        taskInfo1.setTaskDescription("");
//        taskInfo1.setWaitTimeout(20);
//        taskInfo1.setTaskType(TaskType.CLICK);
//        taskInfo1.setPlatform(Platform.PC);
//        List<TaskInfo> subTasks = new ArrayList<>();
//        subTasks.add(taskInfo1);
//        taskInfo.setSubTasks(subTasks);
//
//
//        TaskInfo taskInfo2 = new TaskInfo();
//        taskInfo2.setTaskId(8);
//        taskInfo2.setTaskName("job5");
//        taskInfo2.setUrl("http://dol.deliver.ifeng.com/c?z=ifeng&la=0&si=2&cg=1&c=1&ci=2&or=15477&l=58053&bg=58053&b=96200&u=http://s.trafficjam.cn/m,CCla7nPCPX4wP4DDHC81,uuid=__UUID__");
//        taskInfo2.setStatus(0);
//        List<TimePair> list2 = new ArrayList<>();
//        list2.add(new TimePair(900,2359));
//
//        taskInfo2.setTimePairs(list2);
////        actions = new ArrayList<>();
////        ta = new TaskAction(ActionType.PAUSE,1);
////        actions.add(ta);
////        ta = new TaskAction(ActionType.PAUSE,1);
////        actions.add(ta);
////        taskInfo1.setActions(actions);
//
//        taskInfo2.setBeginDate(DateUtil.parseDate("2017-08-23"));
//        taskInfo2.setEndDate(DateUtil.parseDate("2017-08-23"));
//        taskInfo2.setCreateTime(DateUtil.parseDate(DateUtil.now()));
//        taskInfo2.setIpReusedTimes(1);
//        taskInfo2.setLatestModifyTime(System.currentTimeMillis());
//        taskInfo2.setParentId(6);
//        taskInfo2.setPvToUvRatio(30);
//        taskInfo2.setTargetPv(120);
//        taskInfo2.setTaskDescription("");
//        taskInfo2.setWaitTimeout(20);
//        taskInfo2.setTaskType(TaskType.CLICK);
//        taskInfo2.setPlatform(Platform.PC);
//        List<TaskInfo> subTasks2 = new ArrayList<>();
//        subTasks2.add(taskInfo2);
//        taskInfo1.setSubTasks(subTasks2);
//
//
//        mongoClient.insert(taskInfo);
//

////
//        File file = new File("C:\\PRD\\app_browser_percents.txt");
//        InputStreamReader is = new InputStreamReader(new FileInputStream(file));
//        BufferedReader reader = new BufferedReader(is);
//
//        String line = null;
//        while ((line = reader.readLine()) != null){
//            String[] str = line.split(" ");
//            PercentEntity pe = new PercentEntity();
//            pe.setBegin(Integer.valueOf(str[1]));
//            pe.setEnd(Integer.valueOf(str[2]));
//            pe.setKey(str[0].toLowerCase());
//            pe.setType("app");
//            mongoClient.insert(pe);
//        }



        mongoClient.getCollection("standard_line");



        File file = new File("C:\\Users\\zhanglr\\Desktop\\手凤标准线0-9.txt");
        InputStreamReader is = new InputStreamReader(new FileInputStream(file));
        BufferedReader reader = new BufferedReader(is);

        String line = null;
        while ((line = reader.readLine()) != null){
            String[] str = line.split("\\s+");
            TemplateData tld = new TemplateData();
            tld.setPlatform("wap");
            tld.setNum(Integer.valueOf(str[0]));
            tld.setTime(Integer.valueOf(str[1].replace(":","")));
            mongoClient.insert(tld);
        }

    }
}