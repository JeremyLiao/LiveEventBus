# LiveDataBus

### Android消息总线，基于LiveData，具有生命周期感知能力

## 使用方法
- Fork本项目
- 或者直接拷贝源码：[LiveDataBus.java](https://github.com/JeremyLiao/LiveDataBus/blob/master/LiveDataBus/livedatabus/src/main/java/com/jeremyliao/livedatabus/LiveDataBus.java)

## 依赖
依赖Android Architecture Components，具体可参见gradle文件[build.gradle](https://github.com/JeremyLiao/LiveDataBus/blob/master/LiveDataBus/livedatabus/build.gradle)

## 示例及Demo

#### 订阅消息
- **observe模式**
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
- **observeForever模式**
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
- **set模式**
订阅者会在当前线程收到消息
```java
LiveDataBus.get().with("key_name").setValue(value);
```
- **post模式**
订阅者会在主线程收到消息
```java
LiveDataBus.get().with("key_name").postValue(value);
```

简单的Demo可参见：[LiveDataBusDemo.java](https://github.com/JeremyLiao/LiveDataBus/blob/master/LiveDataBus/livedatabus/src/main/java/com/jeremyliao/livedatabus/LiveDataBusDemo.java)

## 文档
#### LiveDataBus实现原理
LiveDataBus的实现原理可参见作者在美团技术博客上的博文：
[Android消息总线的演进之路：用LiveDataBus替代RxBus、EventBus](https://tech.meituan.com/Android_LiveDataBus.html)

## 其他
- 欢迎提Issue与作者交流
- 欢迎提Pull request，帮助 fix bug，让LiveDataBus变得更强大、更好用