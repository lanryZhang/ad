/*
* IpUtils.java 
* Created on  202018/1/25 14:55 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class IpUtils {
    static List<IpAddr> ipAddrs = new ArrayList<>();
    static HashMap<Long, String> areaMap = new HashMap<>();
    //将127.0.0.1形式的IP地址转换成十进制整数，这里没有进行任何错误处理
    public static long ipToLong(String strIp) {
        long[] ip = new long[4];
        //先找到IP地址字符串中.的位置
        int position1 = strIp.indexOf(".");
        int position2 = strIp.indexOf(".", position1 + 1);
        int position3 = strIp.indexOf(".", position2 + 1);
        //将每个.之间的字符串转换成整型
        ip[0] = Long.parseLong(strIp.substring(0, position1));
        ip[1] = Long.parseLong(strIp.substring(position1+1, position2));
        ip[2] = Long.parseLong(strIp.substring(position2+1, position3));
        ip[3] = Long.parseLong(strIp.substring(position3+1));
        return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];
    }

    //将十进制整数形式转换成127.0.0.1形式的ip地址
    public static String longToIP(long longIp) {
        StringBuffer sb = new StringBuffer("");
        //直接右移24位
        sb.append(String.valueOf((longIp >>> 24)));
        sb.append(".");
        //将高8位置0，然后右移16位
        sb.append(String.valueOf((longIp & 0x00FFFFFF) >>> 16));
        sb.append(".");
        //将高16位置0，然后右移8位
        sb.append(String.valueOf((longIp & 0x0000FFFF) >>> 8));
        sb.append(".");
        //将高24位置0
        sb.append(String.valueOf((longIp & 0x000000FF)));
        return sb.toString();
    }

    static class IpAddr implements Comparable<IpAddr>{
        private long startIp;
        private long endIp;
        private long area;

        public long getStartIp() {
            return startIp;
        }

        public void setStartIp(long startIp) {
            this.startIp = startIp;
        }

        public long getEndIp() {
            return endIp;
        }

        public void setEndIp(long endIp) {
            this.endIp = endIp;
        }

        public long getArea() {
            return area;
        }

        public void setArea(long area) {
            this.area = area;
        }

        @Override
        public int compareTo(IpAddr o) {
            return this.getStartIp() > ((IpAddr) o).getStartIp() ? 1:-1;
        }
    }

    public static String getIpArea(String ip){
        init();
        IpAddr en = new IpAddr();
        en.setStartIp(ipToLong(ip));
        int v = Collections.binarySearch(ipAddrs, en);
        en = ipAddrs.get(-v - 2);
        if (areaMap.containsKey(en.getArea())){
            return areaMap.get(en.getArea());
        }else{
            return "";
        }
    }

    public static void init(){
        try {
            FileReader fr;
            BufferedReader br;
            String line = null;
            if (areaMap.size() == 0) {
                fr = new FileReader("/data/programs/hippo/conf/area.txt");
                br = new BufferedReader(fr);

                while ((line = br.readLine()) != null) {
                    try {
                        String[] arr = line.split("\\s+");
                        if ("中国大陆".equals(arr[2])) {
                            areaMap.put(Long.parseLong(arr[0]), arr[1] + arr[1]);
                        } else {
                            areaMap.put(Long.parseLong(arr[0]), arr[2] + arr[1]);
                        }
                    } catch (Exception ex) {

                    }
                }
            }

            if (ipAddrs.size() == 0) {
                fr = new FileReader("/data/programs/hippo/conf/ip.txt");
                br = new BufferedReader(fr);
                line = null;
                while ((line = br.readLine()) != null) {
                    try {
                        String[] arr = line.split("\\s+");
                        long ips = ipToLong(arr[0]);
                        long ipe = ipToLong(arr[1]);

                        IpAddr ipAddr = new IpAddr();
                        ipAddr.setStartIp(ips);
                        ipAddr.setEndIp(ipe);
                        ipAddr.setArea(Long.valueOf(arr[2]));
                        ipAddrs.add(ipAddr);
                    } catch (Exception ex) {

                    }
                }
                Collections.sort(ipAddrs);
            }

        } catch (Exception er){ }
    }
}