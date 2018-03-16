import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.ifeng.hippo.contances.RedisPrefix;
import com.ifeng.hippo.entity.TaskFragment;
import com.ifeng.hippo.redis.RedisFactory;
import com.ifeng.hippo.task.IExecutor;
import com.ifeng.hippo.task.TaskExecutorExec;
import com.ifeng.hippo.worker.WorkerDescriptor;
import com.ifeng.hippo.worker.WorkerExecutor;
import com.ifeng.redis.RedisClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by zhanglr on 2016/9/19.
 */
public class Server {

    private boolean flag = true;

    class InnerClass implements Runnable{
        @Override
        public void run() {
            while (flag){
                try {
                    System.err.println("running");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @Test
    public void main() throws Exception {
//        HttpClient httpClient = null;
//        HttpGet request = new HttpGet("https://is.snssdk.com/service/2/app_log/?iid=27364366529&device_id=38567839847&ac=wifi&channel=vivo&aid=13&app_name=news_article&version_code=662&version_name=6.6.2&device_platform=android&ab_version=285053%2C249666%2C249686%2C249676%2C283499%2C289640%2C249670%2C229305%2C249671%2C285963%2C286335%2C283849%2C277468%2C290602%2C290496%2C285404%2C286426%2C239096%2C286955%2C170988%2C288547%2C288955%2C279386%2C290973%2C281391%2C276203%2C289786%2C289226%2C285340%2C291024%2C257281%2C281471%2C290794%2C277718%2C284909%2C259491%2C284439%2C280773%2C290099%2C290799%2C285173%2C286973%2C251712%2C283794%2C290254%2C290244%2C285222%2C284809%2C289006%2C289266%2C282027%2C264033%2C258356%2C247848%2C280447%2C281300%2C290603%2C249045%2C288651%2C270164%2C275612%2C283491%2C264612%2C287591%2C288418%2C290196%2C260655%2C286223%2C261945%2C286477%2C241181%2C289713%2C271178%2C252766%2C249828%2C246859&ab_client=a1%2Cc4%2Ce1%2Cf2%2Cg2%2Cf7&ab_group=100170&ab_feature=102749%2C94563&abflag=3&ssmix=a&device_type=vivo+Xplay5A&device_brand=vivo&language=zh&os_api=22&os_version=5.1.1&uuid=861406031845362&openudid=3c4eac8ed1cd56c2&manifest_version_code=662&resolution=1440*2560&dpi=640&update_version_code=66209&_rticket=1520300455499&plugin=10574&fp=LSTqFrD5FScOFlHIc2U1FYFIFrwW&pos=5r_-9Onkv6e_eDMHdTgieCUfv7G_8fLz-vTp6Pn4v6esramzqKytr6qssb_x_On06ej5-L-nrqizqK-vpKuq4A%3D%3D&rom_version=funtouch+os_3.0+lite_pd1522a_a_3.8.1&tt_data=a&ts=1520300463&as=a2259f796f5a7a712d3065&mas=00dfc7517bc5cbc8a144e39c350eca70ad4a8ceaa6c8a46290");//这里发送get请求
//        // 获取当前客户端对象
//        RequestConfig config = RequestConfig.custom().setConnectTimeout(2 * 1000).setSocketTimeout(2000)
////                    .setProxy(new HttpHost(proxy.getProxyIp(), proxy.getPort()))
//                .build();
//        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
//        request.setHeader("Accept", "Accept text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
//        request.setHeader("Accept-Charset", "GB2312,utf-8;q=0.7,*;q=0.7");
//        request.setHeader("Accept-Encoding", "gzip, deflate");
//        request.setHeader("Accept-Language", "zh-cn,zh;q=0.5");
//
//        HttpResponse response = httpClient.execute(request);
//        response.getEntity().getContent();
//        String res = EntityUtils.toString(response.getEntity());
//        JSONObject jsonObject = null;
//
//        try {
//            /**把json字符串转换成json对象**/
//            jsonObject = JSON.parseObject(res);
//        } catch (JSONException e1) {
//            e1.printStackTrace();
//            return;
//        }
//        return jsonObject.toString();



//       double i = 1.9;
//        int k = (int)i;
//
////        System.out.println(k);
//        byte[] byteContent  = "asdasdaddddddddddddddddddddddddaaaaaaaaaaaaaaaaaaaaaa".getBytes(Charset.forName("ASCII"));
//        Cipher cipher = Cipher.getInstance("AES");
//        KeyGenerator kgen = KeyGenerator.getInstance("AES");
//        kgen.init(128, new SecureRandom("as".getBytes()));
//        SecretKey secretKey = kgen.generateKey();
//        byte[] enCodeFormat = secretKey.getEncoded();
//        SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
//        cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
//        byte[] result = cipher.doFinal(byteContent);
//
////        EIPProcessJob eipProcessJob = new EIPProcessJob();
////        eipProcessJob.execute();
////        Master master = new Master();
////        master.start();
//
////        Worker worker = new Worker();
////        worker.connect();
//        RedisClient redisClient = RedisFactory.newInstance();
//        FileReader fr = new FileReader("C:\\Users\\zhanglr\\Desktop\\ss.txt");
//        BufferedReader bf = new BufferedReader(fr);
//        String b = "";
//        while((b=bf.readLine())!= null){
//            redisClient.rpushString(RedisPrefix.PROXY_IP_LIST_EV,b);
//            redisClient.rpushString(RedisPrefix.PROXY_IP_LIST,b);
//        }

//
//        PriorityBlockingQueue<Task> pbq = new PriorityBlockingQueue<>();
//
//        pbq.offer(new Task(1));
//        pbq.offer(new Task(3));
//        pbq.offer(new Task(5));
//        pbq.offer(new Task(2));
//        pbq.offer(new Task(7));
//        pbq.offer(new Task(0));
//        pbq.offer(new Task(10));
//        pbq.offer(new Task(100));
//
//
//        Task v = null;
//        while ((v = pbq.poll()).getV() != 10){
//            System.err.println(v.getV());
//        }
//
//        for (int i = 0; i < 100;i++)
//            pbq.offer(new Task((int)(Math.random() * 100)));
//
//        while ((v = pbq.poll()) != null){
//            System.err.println(v.getV());
//        }

    }

    public  class Task implements Comparable{
        private int v;
        public Task(int v){
            this.v = v;
        }

        public int getV() {
            return v;
        }


        @Override
        public int compareTo(Object o) {
            Task t = (Task) o;
            if (this.getV() < t.getV()){
                return -1;
            }
            if (this.getV() > t.getV()){
                return 1;
            }
            return 0;
        }
    }
}
