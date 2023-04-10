package com.example.androidloginregister;

import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MySQLConnection {
    //要连接MySQL数据库的URL    URL_MySQL="jdbc:mysql://外网地址:端口/数据库名称"
    public static final String URL_MySQL="jdbc:mysql://外网地址:端口/数据库名称";
    //要连接MySQL数据库的用户名  NAME_MySQL="MySQL用户名"
    public static final String NAME_MySQL="MySQL用户名";
    //要连接MySQL数据库的密码    PASSWORD_MySQL="MySQL密码"
    public static final String PASSWORD_MySQL="MySQL密码";

    //使用PreparedStatement来执行SQL语句查询
    public static PreparedStatement preparedStatement;
    //使用Resultset接收JDBC查询语句返回的数据集对象
    public static ResultSet resultSet;
    //连接数据库
    public static Connection connection;

    public static void connect()
    {
        //开启连接数据库
        Log.d("注意","开启连接数据库中......");
        connection=null;
        try {
            //加载驱动
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            //获取与数据库的连接
            connection= DriverManager.getConnection(URL_MySQL,NAME_MySQL,PASSWORD_MySQL);
        }catch (Exception e){
            //对异常情况进行处理
            e.printStackTrace();
        }
    }

    public static void close()
    {
        Log.d("注意","正在关闭数据库连接......");
        try{
            if(resultSet!=null){
                resultSet.close();//关闭接收
                resultSet=null;
            }
            if(preparedStatement!=null){
                preparedStatement.close();//关闭sql语句查询
                preparedStatement=null;
            }
            if(connection!=null){
                connection.close();//关闭数据库连接
                connection=null;
            }
        }catch (Exception e){
            //对异常情况进行处理
            e.printStackTrace();
        }
    }
}
