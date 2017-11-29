/*
* Province.java 
* Created on  202017/11/3 10:47 
* Copyright © 2012 Phoenix New Media Limited All Rights Reserved 
*/
package com.ifeng.hippo.entity;

import com.ifeng.data.IDecode;
import com.ifeng.data.ILoader;

/**
 * Class Description Here
 *
 * @author zhanglr
 * @version 1.0.1
 */
public class Province  implements IDecode {
    private int id;
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void decode(ILoader loader) {
            this.setId(loader.getInt("id"));
            this.setName(loader.getString("name"));
    }
}
