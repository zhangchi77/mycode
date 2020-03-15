package com.mvc.zhangc.servlet;

import com.mvc.zhangc.annotation.*;
import com.mvc.zhangc.controller.ZhangcController;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @ClassName: DispatcherServlet
 * @Author: zhangchi
 * @Descriprion: TODO
 * @Date: 2020/3/14 3:08
 * @Modifier:
 **/
public class DispatcherServlet extends HttpServlet{

    List<String> classUrls=new ArrayList<>();//保存实例化所有类的路径
    Map<String,Object> ioc=new HashMap<>();//ioc容器
    Map<String,Object> urlHandlers =new HashMap<>();//地址映射

    //tomcat初始化IOC
    public void init(ServletConfig config) throws ServletException{
        doScanPackage("com.mvc");//扫描特殊注解的类
        doInstance();//类实例化
        doAutowired();//处理依赖
        doMapping();//路径映射
    }

    //扫描文件
    public void doScanPackage(String basePackage){
        //寻找com.mvc下所有.class的文件
        URL url = this.getClass().getClassLoader().getResource("/"+basePackage.replaceAll("\\.","/"));
        String fileStr = url.getFile();
        File file = new File(fileStr);
        String[] filesStr = file.list ();
        for(String path: filesStr){
            File filePath = new File(fileStr+path);
            if(filePath.isDirectory()){
                //文件夹
                doScanPackage(basePackage+"."+path);
            }else{
                //.class 结束的文件
                //classUrls = {com.mvc.zhangc.controller.zhangcController,com.mvc.zhangc.controller.
                classUrls.add(basePackage + "."+ filePath.getName().replace(".class",""));
            }
        }
    }


    public void doInstance(){
        for(String classurl:classUrls){
            try {
                Class<?> clazz=Class.forName(classurl);//class对象，必须是包名+类名
                if(clazz.isAnnotationPresent(MyController.class)){
                    //是controller注解的类
                    Object instance =clazz.newInstance();//反射获取类的对象
                    MyRequestMapping map1=clazz.getAnnotation(MyRequestMapping.class);//取到MyRequestMapping注解
                    ioc.put(map1.value(),instance);//用MyRequestMapping里面的值作为key

                }else if(clazz.isAnnotationPresent(MyService.class)){
                    //是service注解的类
                    Object instance1 =clazz.newInstance();
                    MyService map2=clazz.getAnnotation(MyService.class);
                    ioc.put(map2.value(),instance1);//用MyService注解里面的值作为key
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }

    }

    public void doAutowired(){
        //遍历之前组装好的ioc容器
        for(Map.Entry<String,Object> entry:ioc.entrySet()){
            Object instance = entry.getValue();
            Class clazz=instance.getClass();//获取实例的类对象

            if(clazz.isAnnotationPresent(MyController.class)){
                //是MyController控制类
                Field[] fields=clazz.getDeclaredFields();//获取类里面所有的方法
                for(Field field:fields){
                    if(field.isAnnotationPresent(MyAutowired.class)){
                        //方法上面有MyAutowired的注解
                        MyAutowired autowired=field.getAnnotation(MyAutowired.class);
                        String key =autowired.value();
                        Object object =ioc.get(key);//从IOC中获取待注入的对象
                        field.setAccessible(true);//权限打开，因为注入的类是privite的
                        try {
                            field.set(instance,object);//
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }

        }
    }


    public void doMapping(){
        //
        for(Map.Entry<String,Object> entry:ioc.entrySet()){
            Object instance = entry.getValue();
            Class<?> clazz=instance.getClass();
            if(clazz.isAnnotationPresent(MyController.class)){
                //是控制类(有MyController注解)
                MyRequestMapping map1 = clazz.getAnnotation(MyRequestMapping.class);
                String classurl = map1.value();//类上面requstmapping里面的路径
                Method[] methods= clazz.getMethods();//获取类里面所有的方法
                for(Method method:methods){
                    if(method.isAnnotationPresent(MyRequestMapping.class)){
                        MyRequestMapping map2=method.getAnnotation(MyRequestMapping.class);
                        String methodurl=map2.value();//获取方法上面requestMapping里面的路径
                        urlHandlers.put(classurl+methodurl,method);//类上面requstmapping里面的值径和方法上requstmapping里面的值作为key,和方法绑定
                    }else{
                        continue;
                    }
                }

            }

        }

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        this.doPost(request,response);
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         String uri =request.getRequestURI();//获取ip+端口后面全路径
         String context = request.getContextPath();
         String path;
         //判断是否有别名
         if(context.isEmpty()){
             path = uri;
         }else{
             path = uri.replace(context,"");//
         }
         Method method = (Method) urlHandlers.get(path);
         String key ="/"+path.split(Pattern.quote("/"))[1];
         ZhangcController instance =(ZhangcController)ioc.get(key);

        try {
            Object[] args=hand(request,response,method);
            method.invoke(instance,args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }


    //策略模式获取方法的参数
    private static Object[] hand(HttpServletRequest request,HttpServletResponse response,Method method){
        //拿到当前执行类方法有哪些参数
        Class[] paramClazzs = method.getParameterTypes();
        //根椐参数的个数， new一个参数的数组，将方法里的所有参数赋值到 args 来
        Object[] args = new Object[paramClazzs.length];

        int args_i = 0;
        int index = 0;
        for (Class<?> paramClazz : paramClazzs) {
            if (ServletRequest.class.isAssignableFrom(paramClazz)) {
                args[args_i++] = request;
            }
            if (ServletResponse.class.isAssignableFrom(paramClazz)) {
                args[args_i++] = response;
            }
            // 从 0-3 判断有没有RequestParam 注解，很明显paramClazz为0和1时不是，
            // 当为 2 和 3 时@RequestParam,需解析
            // [@com.mvc.zhangc.annotation.RequestParam(value=name)]
            Annotation[] paramAns = method.getParameterAnnotations()[index];
            if (paramAns.length > 0) {
                for (Annotation paramAn : paramAns) {
                    if (MyRequestParam.class.isAssignableFrom(paramAn.getClass())){
                        MyRequestParam rp = (MyRequestParam) paramAn;
                        // 授到注解 1 ft 的 name 和 age
                        args[args_i++] = request.getParameter(rp.value());
                    }
                }
            }
            index++;
        }
        return args;
    }
}