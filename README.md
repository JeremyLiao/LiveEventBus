# LiveDataBus

### Android消息总线，基于LiveData，具有生命周期感知能力，支持Sticky

### 简单之美
[LiveDataBus](https://github.com/JeremyLiao/LiveDataBus/blob/master/live-data-bus/livedatabus/src/main/java/com/jeremyliao/livedatabus/LiveDataBus.java)的整个实现就一个类，不超过150行代码。不需要过于繁杂的功能，简单好用，就是最好的：）

### 不同实现
- [**live-data-bus**](https://github.com/JeremyLiao/LiveDataBus/tree/master/live-data-bus)
基本实现，主要采用继承LiveData的方式
- [**live-event-bus**](https://github.com/JeremyLiao/LiveDataBus/tree/master/live-event-bus) 由于live-data-bus的实现存在发送给Stop状态Observer消息无法及时收到的问题，这个问题采用继承LiveData的方式无法解决，所以把LiveData源码拷贝并命名成LiveEvent类，直接修改解决，并且也解决了live-data-bus需要hook的问题

### 主要功能Commit记录
1. 主要功能完成（Jul 11, 2018）
2. 支持Sticky（Aug 8, 2018）
3. 修复在后台线程PostValue会丢失消息的问题（Aug 9, 2018）
4. 新建分支live-event，解决发送给Stop状态Observer消息无法及时收到的问题（Aug 18, 2018）
5. 两种实现合并到master分支

## 如何使用本项目

- Fork本项目
- 使用live-data-bus实现方式可以直接拷贝源码：[LiveDataBus.java](https://github.com/JeremyLiao/LiveDataBus/blob/master/live-data-bus/livedatabus/src/main/java/com/jeremyliao/livedatabus/LiveDataBus.java)

## 依赖
依赖Android Architecture Components，具体可参见gradle文件[build.gradle](https://github.com/JeremyLiao/LiveDataBus/blob/master/live-data-bus/livedatabus/build.gradle)

## 调用方式

#### 订阅消息
- **observe**
生命周期感知，不需要手动取消订阅

```java
LiveDataBus.get()
	.with("key_name", String.class)
	.observe(this, new Observer<String>() {
	    @Override
	    public void onChanged(@Nullable String s) {
	       
	    }
	});
```
- **observeForever**
需要手动取消订阅

```java
LiveDataBus.get()
	.with("key_name", String.class)
	.observeForever(observer);
```

```java
LiveDataBus.get()
	.with("key_name", String.class)
	.removeObserver(observer);
```

#### 发送消息
- **setValue**
在主线程发送消息
```java
LiveDataBus.get().with("key_name").setValue(value);
```
- **postValue**
在后台线程发送消息，订阅者会在主线程收到消息
```java
LiveDataBus.get().with("key_name").postValue(value);
```
#### Sticky模式
支持在注册订阅者的时候设置Sticky模式，这样订阅者可以接收到订阅之前发送的消息

- **observeSticky**
生命周期感知，不需要手动取消订阅，Sticky模式

```java
LiveDataBus.get()
        .with("sticky_key", String.class)
        .observeSticky(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
             
            }
        });
```
- **observeStickyForever**
需要手动取消订阅，Sticky模式

```java
LiveDataBus.get()
        .with("sticky_key", String.class)
        .observeStickyForever(observer);
```

```java
LiveDataBus.get()
        .with("sticky_key", String.class)
        .removeObserver(observer);
```

## 示例和DEMO

##### 基本功能
![基本功能](https://github.com/JeremyLiao/LiveDataBus/blob/master/images/img1.gif)

##### Sticky模式
![sticky](https://github.com/JeremyLiao/LiveDataBus/blob/master/images/img2.gif)

##### 一个简单的应用，发消息关闭所有activity
![close all](https://github.com/JeremyLiao/LiveDataBus/blob/master/images/img3.gif)

##### [**live-event-bus**](https://github.com/JeremyLiao/LiveDataBus/tree/master/live-event-bus),解决了发送给Stop状态Observer消息无法及时收到的问题
![close all](https://github.com/JeremyLiao/LiveDataBus/blob/master/images/img4.gif)
![close all](https://github.com/JeremyLiao/LiveDataBus/blob/master/images/img5.gif)


## 文档
#### LiveDataBus实现原理
LiveDataBus的实现原理可参见作者在美团技术博客上的博文：
[Android消息总线的演进之路：用LiveDataBus替代RxBus、EventBus](https://tech.meituan.com/Android_LiveDataBus.html)

## 其他
- 欢迎提Issue与作者交流
- 欢迎提Pull request，帮助 fix bug，增加新的feature，让LiveDataBus变得更强大、更好用

## More Open Source by JeremyLiao

1. [tensorflow-lite-sdk](https://github.com/JeremyLiao/tensorflow-lite-sdk) 一个更加通用的Tensorflow-Lite Android SDK
2. [android-modular](https://github.com/JeremyLiao/android-modular) 一个组件化的实施方案
3. [MessageBus](https://github.com/JeremyLiao/MessageBus) 一个android平台的基于订阅-发布模式的消息框架，支持跨进程消息通信
4. [persistence](https://github.com/JeremyLiao/persistence) 一个android平台的key-value storage framework
5. [LightRxAndroid](https://github.com/JeremyLiao/LightRxAndroid) 另辟蹊径，利用Android Handler实现了一个类似RxJava的链式框架
6. [rxjava-retry](https://github.com/JeremyLiao/rxjava-retry) 封装了几个处理RxJava Retry操作的类
