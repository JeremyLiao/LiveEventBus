## 使用方法
#### 订阅消息
- **observe**
生命周期感知，不需要手动取消订阅

```java
LiveEventBus.get()
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
LiveEventBus.get()
    .with("key_name", String.class)
    .observeForever(observer);
```

```java
LiveEventBus.get()
    .with("key_name", String.class)
    .removeObserver(observer);
```

#### 发送消息
- **setValue**
在主线程发送消息
```java
LiveEventBus.get().with("key_name").setValue(value);
```
- **postValue**
在后台线程发送消息，订阅者会在主线程收到消息
```java
LiveEventBus.get().with("key_name").postValue(value);
```

#### 跨进程、跨APP发送消息
- **broadcastValue**
```java
LiveEventBus.get()
        .with(KEY_TEST_BROADCAST)
        .broadcastValue("broadcast msg");
```
需要设置supportBroadcast

#### Sticky模式
支持在注册订阅者的时候设置Sticky模式，这样订阅者可以接收到订阅之前发送的消息

- **observeSticky**
生命周期感知，不需要手动取消订阅，Sticky模式

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
需要手动取消订阅，Sticky模式

```java
LiveEventBus.get()
        .with("sticky_key", String.class)
        .observeStickyForever(observer);
```

```java
LiveEventBus.get()
        .with("sticky_key", String.class)
        .removeObserver(observer);
```