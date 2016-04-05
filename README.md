![maxbaas-logo](https://avatars2.githubusercontent.com/u/11622934?v=3&s=200)

# MyBaaS

MyBaaS是 [MaxLeap](https://www.maxleap.cn) CloudData和CloudCode部分的开源版本，更多功能请前往 [MaxLeap](https://www.maxleap.cn)。

## 1.0 功能特性
* 多应用
* CloudData
* CloudCode
* Permission & ACL
* Users

## 1.0 不包含的功能
* 邮件验证
* 短信登录
* CloudCode管理

## 快速开始
* 安装 mongodb3.0+
* 安装 maven3.0+
* 安装 git client
* 安装 JDK 1.8+
* git clone https://gitlab.maxleap.com/maxleapservices/maxleap-baas.git
* cd maxleap-baas
* ./build.sh

## 运行
* 1、修改配置文件(配置mongo地址(默认localhost)) vi build/bin/bootstrap
* 2、启动运行 ./build/bin/bootstrap
     启动运行(有cloudcode，需要将cloudcode的jar放置在build/bin目录下) ./build/bin/bootstrap-cloudcode
* 3、演示地址 http://localhost:10086 （账户密码：admin123@gmail.com/admin123）
