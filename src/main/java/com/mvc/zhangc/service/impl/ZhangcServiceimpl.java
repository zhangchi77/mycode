package com.mvc.zhangc.service.impl;

import com.mvc.zhangc.annotation.MyService;
import com.mvc.zhangc.service.ZhangcService;

/**
 * @ClassName: ZhangcServiceImpl
 * @Author: zhangchi
 * @Descriprion: TODO
 * @Date: 2020/3/15 1:49
 * @Modifier:
 **/
@MyService("ZhangcServiceImpl")
public class ZhangcServiceimpl implements ZhangcService {
    @Override
    public String query(String name, String age) {
        return "{name="+name+","+"age="+age+"}";
    }
}
