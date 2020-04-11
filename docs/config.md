## 配置
在Application.onCreate方法中配置：

```
LiveEventBus
        .config()
        ...
```

- **lifecycleObserverAlwaysActive**

配置LifecycleObserver（如Activity）接收消息的模式（默认值true）：
1. true：整个生命周期（从onCreate到onDestroy）都可以实时收到消息
2. false：激活状态（Started）可以实时收到消息，非激活状态（Stoped）无法实时收到消息，需等到Activity重新变成激活状态，方可收到消息

- **autoClear**

配置在没有Observer关联的时候是否自动清除LiveEvent以释放内存（默认值false）

- **setJsonConverter**

配置JsonConverter（默认使用gson）

- **setLogger**

配置Logger（默认使用DefaultLogger）

- **enableLogger**

配置是否打印日志（默认打印日志）

- **setContext**

如果广播模式有问题，请手动传入Context，需要在application onCreate中配置
