## Console接口

```
Console.getInfo()
```
可以通过Console.getInfo()获取LiveEventBus的实时信息，便于问题查找。可以通过以下两种方式获取总线信息：
1. 日志打印
2. 断点调试

#### 获取到的信息示例

```
*********Base info*********
lifecycleObserverAlwaysActive: true
autoClear: false
logger enable: true
logger: com.jeremyliao.liveeventbus.logger.DefaultLogger@e67bedd
Receiver register: true
Application: com.jeremyliao.lebapp.app.DemoApplication@9f2d59b
*********Event info*********
Event name: key_test_delay_life
    version: -1
    hasActiveObservers: true
    hasObservers: true
    ActiveCount: 1
    ObserverCount: 1
    Observers: 
        [com.jeremyliao.liveeventbus.core.LiveEventBusCore$ObserverWrapper@992681d=android.arch.lifecycle.ExternalLiveData$ExternalLifecycleBoundObserver@bc258f4]
Event name: key_test_active_level
    version: -1
    hasActiveObservers: true
    hasObservers: true
    ActiveCount: 1
    ObserverCount: 1
    Observers: 
        [com.jeremyliao.liveeventbus.core.LiveEventBusCore$ObserverWrapper@8a50406=android.arch.lifecycle.ExternalLiveData$ExternalLifecycleBoundObserver@c99bee1]
Event name: key_test_observe_forever
    version: -1
    hasActiveObservers: true
    hasObservers: true
    ActiveCount: 1
    ObserverCount: 1
    Observers: 
        [com.jeremyliao.liveeventbus.core.LiveEventBusCore$ObserverWrapper@2f67d2e=android.arch.lifecycle.LiveData$AlwaysActiveObserver@2266bd9]
Event name: key_test_msg_set_before_on_create
    version: 0
    hasActiveObservers: true
    hasObservers: true
    ActiveCount: 1
    ObserverCount: 1
    Observers: 
        [com.jeremyliao.liveeventbus.core.LiveEventBusCore$ObserverWrapper@1726919=android.arch.lifecycle.ExternalLiveData$ExternalLifecycleBoundObserver@6261060]
Event name: key_test_multi_thread_post
    version: -1
    hasActiveObservers: true
    hasObservers: true
    ActiveCount: 1
    ObserverCount: 1
    Observers: 
        [com.jeremyliao.liveeventbus.core.LiveEventBusCore$ObserverWrapper@9895aeb=android.arch.lifecycle.ExternalLiveData$ExternalLifecycleBoundObserver@3f13e3a]
Event name: key_test_observe
    version: -1
    hasActiveObservers: true
    hasObservers: true
    ActiveCount: 1
    ObserverCount: 1
    Observers: 
        [com.jeremyliao.liveeventbus.core.LiveEventBusCore$ObserverWrapper@3465073=android.arch.lifecycle.ExternalLiveData$ExternalLifecycleBoundObserver@29f4e2]
Event name: key_test_close_all_page
    version: -1
    hasActiveObservers: true
    hasObservers: true
    ActiveCount: 1
    ObserverCount: 1
    Observers: 
        [com.jeremyliao.liveeventbus.core.LiveEventBusCore$ObserverWrapper@55f1a5c=android.arch.lifecycle.ExternalLiveData$ExternalLifecycleBoundObserver@cab80cf]

```

