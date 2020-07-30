### amoeba-mycat
#### 数据库主从复制，读写分离，负载均衡，高可用
```
1.概念
数据库主从复制，读写分离，高可用

数据库备份分为：
数据库冷备份
定期将数据库中数据转化为sql文件保存
缺点：数据不是最新的，作为最后的措施，也得用

数据库热备份
实时的同步
主机，二进制日志文件  从机，临时日志文件，io进程，sql进程

更新操作时，主机：修改操作写入二进制日志文件，
从机：io进程读二进制日志文件，存入临时日志文件，sql进程读临时日志文件，执行写入从机数据库

从机主动读主机的数据，降低主机压力

2.mysql主从复制

如果虚拟机卡死退出，重登入虚拟机，启动mysql之前，先把之前mysql进程杀死，否则启动不了

准备两台空的dsCentOS虚拟机，启动
xshell分别连接

安装mysql
两台虚拟机进行：
cd /usr/local/src/
mkdir mysql
cd mysql/
Percona-Server-5.6.24-72.2-r8d0f85b-el6-x86_64-bundle.tar拖入
tar -xvf Percona-Server-5.6.24-72.2-r8d0f85b-el6-x86_64-bundle.tar 

安装56-debuginfo,shared,client,server
rpm -ivh Percona-Server-56-debuginfo-5.6.24-rel72.2.el6.x86_64.rpm
rpm -ivh Percona-Server-shared-56-5.6.24-rel72.2.el6.x86_64.rpm
rpm -ivh Percona-Server-client-56-5.6.24-rel72.2.el6.x86_64.rpm
rpm -ivh Percona-Server-server-56-5.6.24-rel72.2.el6.x86_64.rpm

启动mysql
service mysql start
将数据库拖入mysql/下
设定用户名，密码
mysqladmin -u root password root
mysql -u root -p
source q.sql;
关闭mysql对外访问权限
grant all on *.* to 'root'@'%' identified by 'root';
exit
关闭linux防火墙
chkconfig iptables off
service iptables stop

SQLyog连接mysql主机、从机数据库
修改/etc/my.cnf数据库配置文件
vim /etc/my.cnf
server-id=1 # 数据库编号
log-bin=mysql-bin # 开启数据库二进制文件
server-id=2
log-bin=mysql-bin

重启数据库
service mysql restart
cd /var/lib/mysql
有mysql-bin.000001，表示mysql-bin.000001启动成功

从库挂载主库
主库
SHOW MASTER STATUS;
从库执行
CHANGE MASTER TO 
MASTER_HOST="192.168.89.130",
MASTER_PORT=3306,
MASTER_USER="root",
MASTER_PASSWORD="root",
MASTER_LOG_FILE="mysql-bin.000001",
MASTER_LOG_POS=120

启动主从服务
START SLAVE;
SHOW SLAVE STATUS;
选中执行

从库可以有多台

3.amoeba
读写分离/负载均衡：
[后台服务器1,后台服务器2,后台服务器3] => Amoeba代理服务器 => [写 => 主数据库，读 => 从数据库]

新建一个虚拟机，命名amoeba
安装jdk
jdk-8u51-linux-x64.tar.gz 拖入mysqlMaster /usr/local/src/
tar -xvf jdk-8u51-linux-x64.tar.gz
mv jdk1.8.0_51/ jdk1.8
配置环境变量
vim /etc/profile
JAVA_HOME=/usr/local/src/jdk1.8
JAVA_BIN=/usr/local/src/jdk1.8/bin
重新加载
source /etc/profile
检测
java -version

将amoeba-mysql-3.0.4-BETA.tar.gz拖入
tar -xvf amoeba-mysql-3.0.4-BETA.tar.gz
mv amoeba-mysql-3.0.4-BETA amoeba
绿底色是最大最高权限

cd amoeba/
配置文件
amoeba.xml：
数据库代理服务器的端口8066
writePool master，写入主数据库
readPool multiPool，读multiPool集群

dbServers.xml:
master ip:主库ip
slave01 ip:从机ip
定义multiPool负载均衡池
loadbalance负载均衡策略//1轮询，2权重，3hash
集群：slave01,master,slave01，1/3读主服务器，2/3读从

cd conf/
rm -rf amoeba.xml
rm -rf dbServers.xml
将amoeba.xml，dbServers.xml传入

关闭linux防火墙
chkconfig iptables off
service iptables stop

修改jvm.properties文件
amoeba启动时，线程大小要>217kb才能执行
cd ../
vim jvm.properties
初始化内存大小256，虚拟机最大内存大小1024，单个线程的内存空间大小196
JVM_OPTIONS="-server -Xms256m -Xmx1024m -Xss196k -XX:PermSize=16m -XX:MaxPermSize=96m"
JVM_OPTIONS="-server -Xms256m -Xmx1024m -Xss256k -XX:PermSize=16m -XX:MaxPermSize=96m"

启动amoeba
cd bin/
./launcher &
Server listening on 0.0.0.0/0.0.0.0:8066.表示成功

application.yml，连接amoeba服务，url:jdbc:mysql://amoeba服务ip:8066/xxx?

MysqlTest测试
selectUser读从主从集群
insert写主

关闭amoeba
./shutdown

4.mycat
高可用
当主库宕了，从库数据比主库多时
主库得向从库同步数据

数据互相同步

数据库双机热备，互为主从

从库
SHOW MASTER STATUS;

主库
CHANGE MASTER TO 
MASTER_HOST="192.168.89.131",
MASTER_PORT=3306,
MASTER_USER="root",
MASTER_PASSWORD="root",
MASTER_LOG_FILE="mysql-bin.000002",
MASTER_LOG_POS=549;

START SLAVE;

SHOW SLAVE STATUS;

此时有2主2从

mycat数据库分库分表中间件
遵循mysql原生协议，跨语言，跨平台，跨数据库的通用中间件代理
基于心跳自动故障切换，支持读写分离，mysql主从，集群

数据量很大的情况下，可以将数据拆分到多个数据库中
垂直拆分：
按业务拆分
水平拆分：
将一张表的数据拆分

分库分表：
c2cDB(150万):[c2cOrder(50万),c2cCart(100万)] => c2cOrder(50万),c2cCart(100万) => 
c2cOrder(50万),c2cCartA(50万),c2cCartB(50万)

如何链接数据库，连c2cDB(逻辑库)
分库分表后如何操作，主键id%n，求模确定入哪个分库

操作逻辑库：原有业务操作的库，拆分后不存在的库
服务器 => mycat代理 => c2cDB(逻辑库) => c2cOrder,c2cCartA,c2cCartB

安装和使用mycat
cd /usr/local/src/
Mycat-server-1.7.0-DEV-20170416134921-linux.tar.gz传入
tar -xvf Mycat-server-1.7.0-DEV-20170416134921-linux.tar.gz

server.xml：
默认端口8066
schemas逻辑库

schema.xml：
switchType="1"
3次心跳检测没有响应，自动主从切换
writeHost主库ip
readHost从库ip
反向配置
writeHost从库ip
readHost主库ip

cd mycat/conf/
rm -rf schema.xml
rm -rf server.xml
将schema.xml，server.xml拖入

cd ../bin/
启动./mycat start
查看状态./mycat status

application.yml：
url: jdbc:mysql://192.168.89.132:8066/a

测试高可用
挂掉主库
mysqlMaster虚拟机，关闭mysql，service mysql stop
MysqlTest.insertUser新增1条数据
再次启动数据库，service mysql start

主库向从库同步数据
```