# LiveEventBus
![license](https://img.shields.io/github/license/JeremyLiao/LiveEventBus.svg) [![version](https://img.shields.io/badge/JCenter-v1.4.2-blue.svg)](https://mvnrepository.com/artifact/com.jeremyliao/live-event-bus)

LiveEventBus是一款Android消息总线，基于LiveData，具有生命周期感知能力，支持Sticky，支持AndroidX，支持跨进程，支持跨APP

![logo](/images/logo.png)

## LiveEventBus的特点
- [x] 生命周期感知，消息随时订阅，自动取消订阅
- [x] 支持Sticky粘性消息
- [x] 支持AndroidX
- [x] 支持跨进程通信
- [x] 支持跨APP通信
- [x] 支持设置LifecycleObserver（如Activity）接收消息的模式：
1. 整个生命周期（从onCreate到onDestroy）都可以实时收到消息
2. 激活状态（Started）可以实时收到消息，非激活状态（Stoped）无法实时收到消息，需等到Activity重新变成激活状态，方可收到消息

## 在工程中引用
Via Gradle:

```
implementation 'com.jeremyliao:live-event-bus:1.4.2'
```
For AndroidX:
```
implementation 'com.jeremyliao:live-event-bus-x:1.4.2'
```

## 配置
在Application.onCreate方法中配置：

```
LiveEventBus.get()
        .config()
        .supportBroadcast(this)
        .lifecycleObserverAlwaysActive(true);
```
- **supportBroadcast**

配置支持跨进程、跨APP通信

- **lifecycleObserverAlwaysActive**

配置LifecycleObserver（如Activity）接收消息的模式：
1. true：整个生命周期（从onCreate到onDestroy）都可以实时收到消息
2. false：激活状态（Started）可以实时收到消息，非激活状态（Stoped）无法实时收到消息，需等到Activity重新变成激活状态，方可收到消息

## 使用方法
#### 以生命周期感知模式订阅消息
- **observe**

具有生命周期感知能力，LifecycleOwner销毁时自动取消订阅，不需要调用removeObserver

```java
LiveEventBus.get()
	.with("key_name", String.class)
	.observe(this, new Observer<String>() {
	    @Override
	    public void onChanged(@Nullable String s) {
	    }
	});
```

#### 以Forever模式订阅和取消订阅消息
- **observeForever**

Forever模式订阅消息，需要调用removeObserver取消订阅

```java
LiveEventBus.get()
	.with("key_name", String.class)
	.observeForever(observer);
```

- **removeObserver**

取消订阅消息

```java
LiveEventBus.get()
	.with("key_name", String.class)
	.removeObserver(observer);
```

#### 发送消息
- **post**

发送一个消息，支持前台线程、后台线程发送

```java
LiveEventBus.get()
    .with("key_name")
    .post(value);
```

- **postDelay**

延迟发送一个消息，支持前台线程、后台线程发送

```java
LiveEventBus.get()
    .with("key_name")
    .postDelay(value, 1000);
```

#### 跨进程、跨APP发送消息
- **broadcast**

跨进程、跨APP发送消息，支持前台线程、后台线程发送。需要设置supportBroadcast

```java
LiveEventBus.get()
        .with("key_name")
        .broadcast(value);
```

#### Sticky模式
支持在订阅消息的时候设置Sticky模式，这样订阅者可以接收到之前发送的消息。

- **observeSticky**

以Sticky模式订阅消息，具有生命周期感知能力，LifecycleOwner销毁时自动取消订阅，不需要调用removeObserver

```java
LiveEventBus.get()
        .with("sticky_key", String.class)
        .observeSticky(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s){
            }
        });
```
- **observeStickyForever**

Forever模式订阅消息，需要调用removeObserver取消订阅，Sticky模式

```java
LiveEventBus.get()
        .with("sticky_key", String.class)
        .observeStickyForever(observer);
```

#### 如果使用1.3.X及以下版本，请参考[老版使用方法](docs/OLD_DIRECTION.md)

## 混淆规则

```
-dontwarn com.jeremyliao.liveeventbus.**
-keep class com.jeremyliao.liveeventbus.** { *; }
-keep class android.arch.lifecycle.** { *; }
-keep class android.arch.core.** { *; }
```
for androidx:
```
-dontwarn com.jeremyliao.liveeventbus.**
-keep class com.jeremyliao.liveeventbus.** { *; }
-keep class androidx.lifecycle.** { *; }
-keep class androidx.arch.core.** { *; }
```

## 其他分支版本
#### [AndroidX](/branchs/live-event-bus-x/liveeventbus-x/src/main/java/com/jeremyliao/liveeventbus)
- [x] 支持AndroidX
- [x] 同master版本一致

#### [classic](/branchs/live-event-bus-classic/liveeventbus-classic/src/main/java/com/jeremyliao/liveeventbus/LiveEventBus.java)
- [x] 经典实现版，整个实现就一个java文件
- [x] 只支持激活状态（Started）可以实时收到消息，非激活状态（Stoped）无法实时收到消息，需等到Activity重新变成激活状态，方可收到消息
- [x] 不支持跨进程通信

#### [v2](/branchs/live-event-bus-v2/liveeventbus-v2/src/main/java/com/jeremyliao/liveeventbus)
- [x] v2版，历史版本，已废弃
- [x] 为了解决非激活态不能实时收到消息的问题，采用修改LiveData源码的方式实现

## [示例和DEMO](docs/DEMO.md)

## 文档
#### 实现原理
- LiveEventBus的实现原理可参见作者在美团技术博客上的博文：
[Android消息总线的演进之路：用LiveDataBus替代RxBus、EventBus](https://tech.meituan.com/Android_LiveDataBus.html)
- 该博文是初版LiveEventBus的实现原理，与当前版本的实现可能不一致，仅供参考

## 质量
- [x] 编写了30个测试用例以确保LiveEventBus能够正常运行。
- [x] 具体测试用例参见[LiveEventBusTest](/live-event-bus/app/src/androidTest/java/com/jeremyliao/lebapp/LiveEventBusTest.java)

## 版本

版本 | 功能
---|---
1.4.x | 简化对外暴露的接口，重构核心实现，支持前后台线程调用
1.3.x | 支持跨进程、跨APP通信
1.2.x | 支持接收消息的模式，支持AndroidX
1.1.x | 修复了一些问题
1.0.x | 初版，支持基本功能

## 主要功能提交记录
1. 主要功能完成（Jul 11, 2018）
2. 支持Sticky（Aug 8, 2018）
3. 修复在后台线程PostValue会丢失消息的问题（Aug 9, 2018）
4. 解决发送给Stop状态Observer消息无法及时收到的问题（Aug 18, 2018）
5. 解决了Resumed状态的Activity发生订阅，订阅者会收到订阅之前发布的消息的问题。特别感谢@MelonWXD发现了这个问题（Dec 8，2018）
6. 在removeObserver的时候，检查livedata上有没有observer，没有则删除这个livadata，以减少内存占用。特别感谢@GreenhairTurtle提供的解决方案（Dec 27，2018）
7. 支持设置LifecycleObserver接收消息的模式，支持在整个生命周期实时接收消息和只在激活态实时接收消息两种模式（Jan 22，2019）
8. 支持AndroidX（Mar 8，2019）
9. 支持跨进程、跨APP（Mar 26，2019）
10. 简化对外暴露的接口，重构核心实现，支持前后台线程调用（Apr 4，2019）

## 其他
- 欢迎提Issue与作者交流
- 欢迎提Pull request，帮助 fix bug，增加新的feature，让LiveEventBus变得更强大、更好用

## More Open Source by JeremyLiao

1. [FastSharedPreferences](https://github.com/JeremyLiao/FastSharedPreferences) 一个Android平台的高性能key-value组件
2. [tensorflow-lite-sdk](https://github.com/JeremyLiao/tensorflow-lite-sdk) 一个更加通用的Tensorflow-Lite Android SDK
3. [android-modular](https://github.com/JeremyLiao/android-modular) 一个组件化实施方案的Demo
4. [MessageBus](https://github.com/JeremyLiao/MessageBus) 一个android平台的基于订阅-发布模式的消息框架，支持跨进程消息通信
5. [persistence](https://github.com/JeremyLiao/persistence) 一个android平台的key-value storage framework
6. [LightRxAndroid](https://github.com/JeremyLiao/LightRxAndroid) 另辟蹊径，利用Android Handler实现了一个类似RxJava的链式框架
7. [rxjava-retry](https://github.com/JeremyLiao/rxjava-retry) 封装了几个处理RxJava Retry操作的类
8. [retrofit-mock](https://github.com/JeremyLiao/retrofit-mock) 一个用于Retrofit mock response数据的工具
9. [jacoco-android-demo](https://github.com/JeremyLiao/jacoco-android-demo)  AndroidStudio运行jacoco计算测试覆盖率的Demo
10. [android-gradle-study](https://github.com/JeremyLiao/android-gradle-study) 深入浅出Android Gradle
11. [invoking-message](https://github.com/JeremyLiao/invoking-message) 消息总线框架，基于LiveEventBus实现。它颠覆了传统消息总线定义和使用的方式，通过链式的方法调用发送和接收消息，使用更简单
12. [DataLoader](https://github.com/JeremyLiao/DataLoader) 一个Android异步数据加载框架，用于Activity打开之前预加载数据，页面启动速度优化利器
