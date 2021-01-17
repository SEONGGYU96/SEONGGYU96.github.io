---
layout: post
title: "안드로이드 Thread pool과 활용"
subtitle: "안드로이드 백그라운드 스레드 구현 방법"
author: "GuGyu"
header-style: text
tags:
  - Android
  - AndroidFramework
  - Study
  - Thread
  - ThreadPool
  - Background
---

백그라운드 스레드를 만들기 위한 다양한 방법이 있다. 이번에는 그 중에서 스레드 풀을 사용한 방법에 대해 알아보고자 한다. 스레드 풀은 대기 상태의 스레드를 유지해서 스레드를 생성하거나 종료하는 오버헤드를 줄임으로써, 많은 개수의 비동기 작업을 실행할 때 퍼포먼스를 향상시킨다. 게다가 스레드 풀은 스레드를 포함한 리소스를 제한하고 관리하는 기능도 제공하니, 백그라운드에서 처리해야할 작업이 많다면 스레드 풀은 좋은 선택이 될 수 있다. 

## ThreadPoolExecutor 클래스

자바에서는 스레드 풀이 `ThreadPoolExecutor` 클래스로 구현되어있다. 이 클래스의 한 생성자를 살펴보자. (`ThreadFactory`는 제외하였다)

```java
ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler)
```

### corePoolSize와 maximumPoolSize 파라미터

`int corePoolSize`와 `int maximumPoolSize`는 각각 스레드의 기본 개수와 최대 개수를 나타낸다. 스레드 풀의 스레드 개수가 corePoolSize 보다 커지면, 초과하는 개수 만큼의 태스크는 끝나고 나서 스레드를 유지하지 않는다.  

하지만 그렇다고 스레드 풀이 미리 corePoolSize만큼 스레드를 만들어두지는 않는다. 대신, ThreadPoolExecutor는 기본적으로 `execute()`나 `submit()`을 호출하는 순간에 작업 중인 스레드 개수가 corePoolSize보다 적으면 스레드를 새로 추가하는 합리적인 방법을 사용한다. 만약 스레드를 미리 만들어놓아야한다면 `prestartCoreThread()`를 사용하자.  

### keepAliveTime와 unit 파라미터

`long keepAliveTime`와 `TimeUinit unit`은 태스크가 종료될 때 바로 제거하지 않고 대기하는 시간을 나타낸다. 보통 unit에는 `TimeUnit.SECOND`나 `TimeUnit.MINUTE`를 사용한다.  

### workQueue 파라미터

스레드 풀에서는 스레드를 corePoolSize 개수만큼 유지시키기 위해 그보다 많은 요청이 들어오면 workQueue에 담는다. 여기에 쓸 수 있는 자료구조는 3가지이다.

#### ArrayBlockingQueue
개수 제한이 있으며, 큐가 꽉 차서 더 넣을 수 없어지면 maximumPoolSize가 될 때까지 스레드를 하나씩 추가해서 사용한다.  

#### LinkedBlockingQueue
개수 제한이 없으며 들어오는 요청마다 큐에 모두 쌓기 때문에 maximumPoolSize가 의미가 없다. 생성자를 이용해 개수를 제한할 수는 있다.

#### SyncronousQueue
요청을 큐에 쌓지 않고 준비된 스레드로 바로 처리한다. 즉 큐를 사용하지 않는다는 의미이다. 모든 스레드가 작업 중이라면 maximumPoolSize까지 스레드를 생성하여 처리한다.

### handler 파라미터

ThreadPoolExecutor가 정지(shutdown)되거나, mximumPoolSize + workQueue 개수를 초과할 때는 태스크가 애초에 거부된다. 이때 거부하는 방식을 결정하는 것이 `RejectedExecutionHandler handler` 이다. ThreadPoolExecutor의 내부에 미리 정의된 4가지 핸들러가 있다.

- `ThreadPoolExecutor.AbortPolicy` : RejectedExecutionException 런타임 예외를 발생시킨다.
- `ThreadPoolExecutor.CallerRunsPolicy` : 스레드를 생성하지 않고 태스크를 호출하는 스레드에서 바로 실행된다.
- `ThreadPoolExecutor.DiscardPolicy` : 태스크가 쥐도 새도 모르게 제거된다.
- `ThreadPoolExecutor.DiscardOldestPolicy` : workQueue에서 가장 오래된 태스크를 제거한다.  

 
### DiscardOldestPolicy 적용

앱에서 ThreadPoolExecutor를 사용할 때 가장 쓸모 있는 RejectedExecutionHandler는 DiscardOldestPolicy이다. RecyclerView, ScrollView, ViewPager2 등에서 화면을 스크롤 하면서 이동할 때, 이미 지나가버린 화면보다 새로 보이는 화면이 상대적으로 더 중요하다. DiscardOldestPolicy는 이런 상황에 유용한데, 오래된 태스크를 workQueue에서 제거하고 최신 태스크를 추가한다.  

아래는 ImageView에 표시할 이미지 파일을 다운로드해서 보여줄 때의 예시이다.

```java
private static final int FIXED_THREAD_SIZE = 4;
private static final int QUEUE_SIZE = 20;

private ThreadPoolExecutor executor = new ThreadPoolExecutor(
    FIXED_THREAD_SIZE, //corePoolSize
    FIXED_THREAD_SIZE, //maxPoolSize
    0L, //keepAliveTime
    TimeUnit.MILLISECONDS, //unit
    new LinkedBlockingQueue<Runnable>(QUEUE_SIZE), //workQueue
    new ThreadPoolExecutor.DiscardOldestPolicy() //handler
);

private void queueDownload(ImageView imageView, String url) {
    executor.submit(new ImageDownloadTask(imageView, url));
}
```

예를 들어 RecyclerView의 각 아이템에 ImageView가 있고 url을 다운로드해서 ImageView에 보여주는 Runnable을 `ImageDownloadTask`로 감쌌다. 그럼 스크롤된 위치에서 ImageDownloadTask가 workQueue의 마지막에 들어가고 workQueue 사이즈를 초과하는 경우 스크롤된지 오래된 (아마 지금 쯤 포커스를 갖고 있지 않는) ImageDownloadTask는 제거된다.  

기본과 최대 스레드는 4개이고 workQueue의 크기는 20이니, 동시에 24개까지는 스레드 풀에 태스크를 넣을 수 있으나 그 이상으로는 workQueue의 오래된 태스크부터 제거된다.

### 작업 생성과 처리 요청

위 코드에서 태스크 처리 요청을 위해 `submit()` 메서드를 사용했다. ThreadPoolExecutor는 태스크 처리 요청을 위해 `execute()`와 `submit()`메서드를 제공한다. 그럼 태스크는 어떤 형태로 생성되는 것일까? 위에서도 언급했지만 `Runnable`이다.  

그러나 ThreadPoolExecutor는 `Callable` 타입의 태스크도 지원하는데 `Runnable`과 동일하지만 유일한 차이점은 `Callabe`은 리턴 값이 있으며, 예외 처리가 가능하다는 것이다.

```java
Runnable task = new Runnable() {
    @Override
    public void run() {
        ...
    }
}

Callable<T> task2 = new Callable<T>() {
    @Oveeride
    public T call() throws Exception {
        ...
        return T;
    }
}
```

스레드 풀은 workQueue에서 Runnable이나 Callable 타입의 태스크를 가져와 `run()` 혹은 `call()`을 호출한다.

그렇다면 태스크 처리 요청에 사용되는 `submit()`과 `execute()` 의 차이는 무엇일까? `execute()`는 Runnable 객체만을 취급하며 workQueue에 태스크를 쌓아준다. 하지만 처리 결과는 호출 부에서 알 수 없다.  

그에 비해 `submit()`는 Runnable 객체와 Callable 객체를 모두 취급하고 workQueue에 태스크를 쌓아준다. 그리고 `Future` 타입의 리턴 값을 통해 호출부에서 처리 결과를 얻을 수 있다.  
<br>

## ScheduledThreadPoolExecutor

지연/반복 잡업에 대해서는 `ScheculedThreadPoolExecutor`를 사용할 수도 있다. Handler도 해당 작업을 수행할 수는 있으나 UI 갱신에 조금 더 적합한 구조이고, 백그라운드 스레드에서 네트워크 통신이나 DB 작업 등이 지연/반복 실행되는 경우는 ScheduledThreadPoolExecutor를 고려하는 것이 좋다. ThreadPoolExecutor를 상속한 클래스기 때문에 사용 방법은 유사하다.  

ScheduledThreadPoolExecutor의 4개의 생성자 중 `ThreadFactory`를 뺀 한가지 살펴보면 다음과 같다.  
`ScheduledThreadPoolExecutor(int corePoolSize, RejectedExecutionHandler handler)`  

maximumPoolSize, keepAliveTime, unit, workQueue는 ScheduledThreadPoolExecutor에서 고정되어 있기 때문에 생성자 파라미터에 없다.  

- maximumPoolSize - Integer.MAX_VALUE
- keepAliveTime - 0
- workQueue - 내부 클래스인 `DelayWorkQueue`

DelayWorkQueue의 기본 사이즈는 16인데, 태스크가 많아지면 제한 없이 계속 사이즈가 커진다.  

ScheduledThreadPoolExecutor에 태스크를 전달하는 메서드는 네 가지가 있다.

#### `schedule(Runnable command, long delay, TimeUnit unit)`

작업을 delay 만큼 뒤에 한 번 실행한다.

#### `schedule(Callable command, long delay, TimeUnit unit)`

작업을 delay 만큼 뒤에 한 번 실행하고 결과를 리턴한다.

#### `scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit)`

작업을 initalDelay 시간 뒤를 시작으로, period 주기만큼 반복적으로 실행시킨다.

#### `scheduleWithFixedDelay(Runnable command, long initialDelay, long period, TimeUnit unit)`

작업이 initDelay 시간 뒤에 작업을 실행하고, 완료되면 period 시간 뒤에 다시 실행시킨다.  

<br>

## Executors 클래스

ThreadPoolExecutor 와 ScheduledThreadPoolExecutor는 직접 생성하는 것 보다는 `Executors`의 팩토리 메서드로 생성하는 경우가 많다. 용도에 맞는 팩토리 메서드가 없다면 저들을 직접 생성하는 방법을 사용한다.  

Executors에서 리턴하는 `ExecutorService`, `ScheduledExecutorService`는 각각 ThreadPoolExecutor, ScheduledThreadPoolExecutor의 상위 인터페이스이다.  

<img src="https://user-images.githubusercontent.com/57310034/104837800-3dfd2280-58fa-11eb-9205-9b9ce2b8996a.png"/>  

Executors에서 자주 쓰이는 팩토리 메서드는 다음과 같다.

#### `newFixedThreadPool(int nThreads)

workQueue의 크기 제한 없이, nThreads 개수까지 스레드를 생성한다.

```java
new ThreadPoolExecutor(
    nThreads, 
    nThreads, 
    0L, 
    TimeUint.MILLISECONDS,
    new LinkedBlockingQueue<Runnable>()
)
```  

<br>

#### `newCachedThreadPool()`

필요할 때 스레드를 생성하는데, 스레드 개수에는 제한이 없다. keepAliveTime이 60초로 매우 길어 Cached라는 수식어가 붙었다.

```java
new ThreadPoolExecutor(
    0, 
    Integer.MAX_VALUE, 
    60L, 
    TimeUnit.SECONDS, 
    new SynchronousQueue<Runnable>()
)
```  

<br>

#### `newSingleThreadExecutor()`

단일 스레드를 사용해서 순차적으로 처리한다. workQueue에는 크기 제한이 없다.

```java
new FinalizableDelegatedExecutorService(
    new ThreadPoolExecutor(
        1,
        1,
        0L,
        TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<Runnable>()
    )
)
```

바로 위 코드는 `newFixedThreadPool(1)` 과 동일한 결과를 리턴할 것 같은데, `FinalizableDelegateExecutorService`로 다시 래핑한 것이다. 이렇게 래핑함으로써 스레드 개수를 1이 아닌 다른 값으로 변경할 수 없도록 한다. 래핑하지 않았다면 ThreadPoolExecutor로 캐스팅하여 `setCorePoolSize()`나 `setMaximumPoolSize()` 메서드로 스레드 개수를 변경할 수 있어 목적에 어긋날 수 있다.  

<br>

#### `newScheduledThreadPool(int corePoolSize)`

corePoolSize 개수의 ScheduledThreadPoolExecutor를 만든다.

```java
new ScheduledThraedPoolExecutor(corePoolSize)
```

<br>
<br>


--- 
해당 포스팅은 [안드로이드 프로그래밍 Next Step - 노재춘 저](http://www.yes24.com/Product/Goods/41085242) 을 바탕으로 내용을 보충하여 작성되었습니다.


참고  
ScheduledThreadPoolExecutor :   
[https://codechacha.com/ko/java-scheduled-thread-pool-executor/](https://codechacha.com/ko/java-scheduled-thread-pool-executor/)