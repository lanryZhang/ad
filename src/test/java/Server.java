import com.ifeng.hippo.contances.RedisPrefix;
import com.ifeng.hippo.entity.TaskFragment;
import com.ifeng.hippo.redis.RedisFactory;
import com.ifeng.hippo.task.IExecutor;
import com.ifeng.hippo.task.TaskExecutorExec;
import com.ifeng.hippo.worker.WorkerDescriptor;
import com.ifeng.hippo.worker.WorkerExecutor;
import com.ifeng.redis.RedisClient;
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

        Thread t = new Thread(new InnerClass());
        t.start();


        Thread.sleep(1000 * 5);

        flag = false;


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
