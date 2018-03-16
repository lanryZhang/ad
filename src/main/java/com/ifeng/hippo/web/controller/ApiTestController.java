package com.ifeng.hippo.web.controller;

import com.ifeng.configurable.Context;
import com.ifeng.core.MessageProcessor;
import com.ifeng.core.distribute.annotions.RequestMapping;
import com.ifeng.core.distribute.annotions.RequestMethod;
import com.ifeng.core.distribute.annotions.WebController;

/**
 * ApiTestController.java
 *
 * @author zhusy
 * @date 2018-2-7 17:29.
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved.
 */
@WebController
public class ApiTestController implements MessageProcessor {

    @RequestMapping(value = "get_api_result",method= {RequestMethod.GET, RequestMethod.POST })
    @Override
    public Object process(Context context) {
        String res = "{\"hrefURL\":\"http://iis1.deliver.ifeng.com/c?p=Nhlv7vNMGcmmx_Kfq_-MZPm-2fW6WLzZAH-imxxM7U0Y5pbku1J-0mupD-MIJj879QT8bypbUA7Ps6XqLzNSGx6GSlyKvewkRpRYsinUW-5Mr8sKCCrKwPQzc5ub8QJUQkQJtlAD2bVkUMuaQQMxi0pVZChzU0fsmpYYKm--JjZvKXFgb28RIo3XivwLeVX3SSzqhQkA4J1BEt4zB0z_7g&u=http://pmptrack-ifeng.gentags.net/ifeng/ck?oadest=http%3A%2F%2Fe.cn.miaozhen.com%2Fr%2Fk%3D2069213%26p%3D7C9dV%26dx%3D__IPDX__%26rt%3D2%26ns%3D__IP__%26ni%3D__IESID__%26v%3D__LOC__%26xa%3D__ADPLATFORM__%26tr%3D__REQUESTID__%26ro%3Dsm%26o%3Dhttps%3A%2F%2Fm.buick.com.cn%2Fenvision%2F%3Futm_source%3Dmifeng%26utm_medium%3DAPP%26utm_term%3DSP-MS1700170_HS-2017121167_INT32-61_HY201800035%26utm_content%3DSGMMRK2017000362%26utm_campaign%3Denvision2018nianyue_mifeng_MDC17140M29050D20180202&bidid=000000000000000000000000000000001517873609962&price=100&bcc_type=0&pid=13605&bcc_id=708&crt_id=11506&mtid=45791&uidt=cookie_ori&region=1156330000&city=1156330800&fq=260&vusr=10000074&dmp=1&aud=_11&pl=3&check=129376602\",\"imgURL\":[\"http://image.gentags.com/pmp/20180202/5bb957e4044f4c6b08964ab3e4b59341.jpg\"],\"impURL\":[\"http://iis1.deliver.ifeng.com/i?p=ORfJirhhQwtbVrjZlNG2FEL5D2RTzY13yqGsmWuHbxMUC5Jm7sM5NwUBntsxsisE6fbsfINDWNdxwDSyoc_lu5uitwCryNgXVsvI71u8xG_-UuEaaMoe7RafCafS3cW6J3mHrJldf7wfFWefeMoTvVONx7eTbySZsv4r8Og1zF7O6cxEMvg5NafNBRWDZLcwB5IjaDftq49H0FMc-qOV3v3HPDvmbJ3FdyVB5rBlMug\"],\"text\":\"2018款别克新昂科威全新上市\"}";
        return res;
    }
}
