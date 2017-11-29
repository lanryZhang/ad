/*
* RedisPrefix.java 
* Created on  202017/5/31 12:38 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.contances;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class RedisPrefix {
    /**
     * 代理IP键值
     * proxy_{ip}_{taskId}_{date}
     */
    public final static String PROXY_PREFIX = "proxy_%s_%s_%s";
    /**
     * UA总列表
     */
    public final static String UA_TOTAL_COUNT = "ua_total_count";
    /**
     * UA列表
     */
    public final static String UA_ID_PREFIX = "ua_index_%s";
    /**
     * 代理列表
     */
    public final static String PROXY_IP_LIST = "proxy_ip_list";

    /**
     * 曝光量--代理列表
     */
    public final static String PROXY_IP_LIST_EV = "proxy_ip_list_ev";


    /**
     * 代理列表--运营商
     */
    public final static String PROXY_IP_LIST_IDC = "proxy_ip_list_%s";

    /**
     * 曝光量--代理列表--运营商
     */
    public final static String PROXY_IP_LIST_EV_IDC = "proxy_ip_list_ev_%s";


    /**
     * 代理错误列表
     */
    public final static String PROXY_IP_ERROR_LIST = "proxy_ip_error_list";

    /**
     * ip cookie键值
     * ip_cookie_prefix_{taskId}_{uaId}
     */
    public final static String TASK_PROXY_COOKIE_PREFIX = "task_proxy_cookie_prefix_%s_%s";

    /**
     * 任务执行状态记录
     * task_target_pv_finished_{taskId}_{date}
     */
    public final static String TASK_TARGET_PV_FINISHED_PREFIX = "task_target_pv_finished_%s_%s";

    public final static String UA_ID = "ua_id";
    public final static String UA_VERSION = "ua_version";
    /**
     * UA键值
     * pc_ua_key_{browser}
     */
    public final static String PC_UA_KEY_PREFIX = "pc_ua_key_%s_%s";

    /**
     * UA键值
     * app_ua_key_{device}_{browser}
     */
    public final static String APP_UA_KEY_PREFIX = "app_ua_key_%s_%s_%s";

    public final static String APP_DEFAULT_UA = "app_default_ua";
    public final static String PC_DEFAULT_UA = "pc_default_ua";

    public final static String APPLE_DEFAULT_UA = "apple_default_ua";
    public final static String ANDROID_DEFAULT_UA = "android_default_ua";
    public final static String CUCC = "cucc";
    public final static String CNC = "cnc";
    public final static String CMCC = "cmcc";
    /**
     * ip ua对应关系proxy_ua_list_{ip}_{taskId}_{date}
     */
    public final static String PROXY_UA_LIST = "proxy_ua_list_%s_%s_%s";

    public final static String VPS_HOST_PREFIX = "vps_host_%s";


    public final static String SHANGHAI_RECONNECT_PROXY_LIST = "shanghai_reconnect_proxy_list";

    public final static String ASSIGN_NUMBER_PREFIX="assign_number_prefix_%s_%s";

    public final static String DEVICE_ID_IOS_PREFIX = "device_id_ios_%s";
    public final static String DEVICE_ID_ANDROID_PREFIX = "device_id_android_%s";

    public final static String DEVICE_ID_IOS_SUCCESS_PREFIX = "device_id_ios_success_%s";
    public final static String DEVICE_ID_ANDROID_SUCCESS_PREFIX = "device_id_android_success_%s";
}
