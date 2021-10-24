CREATE TABLE IF NOT EXISTS user (
 id int primary key auto_increment,
 uname varchar(50) null,
 pwd varchar(20) null,
 rank varchar(10) null,
 status varchar(10) null,
 did varchar(10) null,
 dname varchar(500) null
);
CREATE TABLE IF NOT EXISTS depart (
 id int primary key auto_increment,
 dname varchar(500) null,
 pid int(10) null,
 url varchar(100) null,
 nop int(10) null
);
CREATE TABLE IF NOT EXISTS logs (
 id int primary key auto_increment,
 userid varchar(10) null,
 uname varchar(100) null,
 log varchar(600) null,
 time varchar(30) null
);
CREATE TABLE IF NOT EXISTS filerecord (
 id int primary key auto_increment,
 orgname varchar(300) null,
 curname varchar(300) null,
 uname varchar(100) null,
 url varchar(400) null,
 length bigint null,
 size varchar(20) null,
 type varchar(20) null,
 time varchar(30) null
);


