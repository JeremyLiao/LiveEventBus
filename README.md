# LiveDataBus

### Android消息总线，基于LiveData，具有生命周期感知能力

## 使用方法
- Fork本项目
- 或者直接拷贝源码：[LiveDataBus.java](https://github.com/JeremyLiao/LiveDataBus/blob/master/LiveDataBus/livedatabus/src/main/java/com/jeremyliao/livedatabus/LiveDataBus.java)

## 依赖
依赖Android Architecture Components，具体可参见gradle文件[build.gradle](https://github.com/JeremyLiao/LiveDataBus/blob/master/LiveDataBus/livedatabus/build.gradle)

## 示例及Demo

#### 订阅消息

```java
LiveDataBus.get()
	.with("key_name", String.class)
	.observe(this, new Observer<String>() {
	    @Override
	    public void onChanged(@Nullable String s) {
	       
	    }
	});
```

#### 发送消息

```java
LiveDataBus.get().with("key_name").setValue(value);
```

简单的Demo可参见：[LiveDataBusDemo.java](https://github.com/JeremyLiao/LiveDataBus/blob/master/LiveDataBus/livedatabus/src/main/java/com/jeremyliao/livedatabus/LiveDataBusDemo.java)

## 原理

LiveDataBus的实现原理可参见作者在美团技术博客上的博文：[Android消息总线的演进之路：用LiveDataBus替代RxBus、EventBus](https://tech.meituan.com/Android_LiveDataBus.html)
