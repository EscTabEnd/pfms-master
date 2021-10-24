## 简介
pfms，中文名称：私人文件管理系统，它是基于 [EasyFS](https://gitee.com/whvse/easy-fs) 三次升级改造，
改造后更方便灵活、文件以及文件夹的管理、定时分享文件，且支持人员部门权限管理
轻量级文件服务器，支持缩略图，下载支持中文名，不依赖其它容器，可独立部署，
使用Java语言、SpringBoot框架、Thymeleaf模板引擎、Layer前端、Java内存缓存开发.

## 平台支持
1. Windows
2. Linux

## 预览
![image](https://raw.githubusercontent.com/MrCinco/images/master/pfms/01.png)
![image](https://raw.githubusercontent.com/MrCinco/images/master/pfms/02.png)
![image](https://raw.githubusercontent.com/MrCinco/images/master/pfms/03.png)
![image](https://raw.githubusercontent.com/MrCinco/images/master/pfms/04.png)
![image](https://raw.githubusercontent.com/MrCinco/images/master/pfms/05.png)
![image](https://raw.githubusercontent.com/MrCinco/images/master/pfms/06.png)
![image](https://raw.githubusercontent.com/MrCinco/images/master/pfms/07.png)
![image](https://raw.githubusercontent.com/MrCinco/images/master/pfms/08.png)

## 使用场景
私人文件在线管理，主要功能有：  
文件上传、下载、查看、删除、重命名、新建文件夹、定时分享文件、人员部门管理等...

## Linux独立部署
0. 本地修改配置打包，上传jar包到linux服务器
1. 运行命令: nohup java -jar fms-1.0.jar &
2. 访问：http://ip:8081
3. 你也可以直接使用IDEA导入源码运行
##windows部署
0.打成war包，把war包放入tomcat下面webapps下
1、解压war包到当前目录
3、启动tomcat
4、访问tomcat端口即可

## 使用指南
配置文件中以下两个参数标识管理员账号和密码：
> admin.uname=root  
  admin.pwd=admin  
  
PS: 如果是图片、音频、视频、pdf、网页、文本类型的文件会在浏览器直接打开，其他类型弹出下载框。

## 服务器参数配置
fs.dir配置为上传到硬盘的指定文件夹中，并且会生成图片缩略图。

## 其他可选参数
> fs.dir：文件上传位置  
fs.uuidName：文件是否使用uuid命名  
fs.useSm：文件是否生成缩略图  
fs.useNginx：文件是否使用nginx做转发  
fs.nginxUrl：nginx服务器地址

##启动说明
有两种启动模式，一种是H2内存启动模式一种是持久化启动模式
当前application的spring.datasource.url配置为持久化模式，持久化的文件存储路径为：E:/fms/h2/
如果你的电脑上没有E盘，请自己定义路径

如果使用的是内存启动模式，那么在data.sql文件中的插入语句可能会不生效或者报错，如果报错，换成下面的语句
#INSERT into `user`(uname,pwd,rank,status) select 'root','admin','0','1' from user where not exists (select * from user where uname='root' )
#INSERT into `user`(uname,pwd,rank,status,did,dname) values('root','admin','0','1',0,'根部门')
#INSERT into `depart`(id,dname,pid,url,nop) values(0,'根部门',-1,'/',0)

启动过后就可以使用插入的账号密码进行登录了。

fs.dir参数是整个文件系统上传的文件所存放的根目录，可以自己定义。