package com.ifeng.hippo.proxy;

/**
 * Created by zhanglr on 2016/8/30.
 */
public class Proxy {
    private String proxyIp;
    private int port;
    private String proxyRealIp;
    private ProxyType proxyType;
    private String userName;
    private String password;
    private String netName;
    private String vpsHost;
    private long timestamp;
    private String partner;
    private long expire;
    private String addr;
    private String proxyId;

    public Proxy(){
        proxyIp = "";
        proxyRealIp = "";
        port = 0;
        proxyType = ProxyType.HTTP;
        userName = "";
        password = "";
        netName = "";
        vpsHost = "";
        partner = "";
        addr = "";
        expire = 15 *1000L;
        proxyId = "";
        timestamp = System.currentTimeMillis();
    }

    public String getProxyId() {
        return proxyId;
    }

    public void setProxyId(String proxyId) {
        this.proxyId = proxyId;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public long getExpire() {
        return expire;
    }

    public void setExpire(long expire) {
        this.expire = expire;
    }

    public String getPartner() {
        return partner;
    }

    public void setPartner(String partner) {
        this.partner = partner;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getNetName() {
        return netName;
    }

    public void setNetName(String netName) {
        this.netName = netName;
    }

    public String getVpsHost() {
        return vpsHost;
    }

    public void setVpsHost(String vpsHost) {
        this.vpsHost = vpsHost;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ProxyType getProxyType() {
        return proxyType;
    }

    public void setProxyType(ProxyType proxyType) {
        this.proxyType = proxyType;
    }

    public void setProxyIp(String proxyIp){
        this.proxyIp = proxyIp;
    }

    public String getProxyIp(){
        return this.proxyIp;
    }

    public void setProxyRealIp(String proxyRealIp){
        this.proxyRealIp = proxyRealIp;
    }

    public String getProxyRealIp(){
        return proxyRealIp;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    /**
     * {proxy.hostIp}#{proxy.port}#{proxy.proxyType}#{proxy.netName}#{proxy.vpsHost}#{proxy.partner}#{proxy.addr}#{proxy.username}#{proxy.password}#{proxy.realIp}#{proxy.expire}#timstamp
     * @return
     */
    public String toString(){
        return new StringBuilder(this.proxyRealIp).append(":").append(this.port)
                .append("#").append(this.proxyType.toString())
                .append("#").append(this.netName.toLowerCase())
                .append("#").append(this.vpsHost)
                .append("#").append(this.partner)
                .append("#").append(this.addr)
                .append("#").append(this.userName)
                .append("#").append(this.password)
                .append("#").append(this.expire)
                .append("#").append(timestamp).toString();
    }
}
