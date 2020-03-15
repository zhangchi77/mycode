package com.mvc.zhangc.controller;

import com.mvc.zhangc.annotation.MyAutowired;
import com.mvc.zhangc.annotation.MyController;
import com.mvc.zhangc.annotation.MyRequestMapping;
import com.mvc.zhangc.annotation.MyRequestParam;
import com.mvc.zhangc.service.ZhangcService;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @ClassName: zhangcController
 * @Author: zhangchi
 * @Descriprion: TODO
 * @Date: 2020/3/14 3:07
 * @Modifier:
 **/
@MyController
@MyRequestMapping("/zhangc")
public class ZhangcController {

    @MyAutowired ("ZhangcServiceImpl")
    private ZhangcService zhangcService;

    @MyRequestMapping("/query")
    public void query(HttpServletRequest request, HttpServletResponse response,
                      @MyRequestParam("name") String name,
                      @MyRequestParam("age") String age)
            throws IOException {

            PrintWriter pw = response.getWriter();
            String result = zhangcService.query(name,age);
            pw.write(result);

    }

}
