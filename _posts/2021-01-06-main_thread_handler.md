---
layout: post
title: "메인 스레드와 Handler"
subtitle: "UI 이벤트와 사용자 메시지 처리"
author: "GuGyu"
header-style: text
tags:
  - Android
  - AndroidFramework
  - Study
  - Thread
  - Handler
---

`Handler`는 메인 `Looper`와 연결되어 메인 스레드에서 `Message`를 처리하는 중심 역할을 한다. 이번 포스팅에서는 메인 스레드와 `Handler`에 대해 조금 깊게 다뤄보고자 한다.  

<br>

# UI 처리를 위한 메인 스레드

안드로이드 어플리케이션은 CPU의 성능을 최대한 끌어올리기 위해 멀티 스레드를 활용하지만, UI를 업데이트하는 데는 <b>단일 스레드 모델 (single thread model)</b>이 적용된다. UI 자원은 어떤 스레이드에서도 동일하기 때문에 멀티 스레드로 업데이트를 하면 `Deadlock`, `Race Condition` 등 여러 문제를 야기할 수 있기 때문이다. 따라서 UI 업데이트는 메인 스레이드에서만 혀용된다.  

## 안드로이드에서의 메인 스레드

일반적인 자바 어플리케이션에서 main() 메서드로 실행되는 것이 바로 메인 스레드이다. 안드로이드 어플리케이션에서의 메인 스레드도 동일하다. 안드로이드 프레임워크 내부 클래스인 `android.app.ActivityThread`가 어플리케이션의 메인 클래스라고 할 수 있고 `ActivityThread#main()` 메서드가 어플리케이션의 시작 지점이다. 해당 클래스의 이름만 보면 액티비티 관련 클래스 같지만 모든 컴포넌트들과 관련이 있는 클래스이다.  

```java
public static void main(String[] args) {
    Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "ActivityThreadMain");
    AndroidOs.install();
    CloaseGuard.setEnabled(false);
    Environment.initForCurrentUser();
    final File configDir = Environment.getUserConfigDirectory(UserHandle.myUserId());
    TrustedCertificateStore.setDefaultUserDirectory(configDir);
    initializeMainlineModules();
    Process.setArgV0("<pre-initialized>");

    Looper.prepareMainLooper(); //메인 Looper를 준비

    ...

    ActivityThread thread = new ActivityThread();
    thread.attach(false);

    if (sMainThreadHandler == null) {
        sMainThreadHandler = thread.getHander();
    }

    Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);

    // 여기서 UI Message를 처리한다. 
    // Looper.loop() 메서드 안에 무한 반복문이 있기 때문에 프로세스가 종료될 때까지 main() 메서드는 끝나지 않는다.
    Looper.loop(); 

    throw new RuntimeException("Main thread loop unexpectedly exited");
}
```  
<br>

# Looper 클래스

메인 스레드의 동작을 이해하기 위해서 `Looper`를 먼저 이해해보자.  

## 생성

`Looper`는 *TLS(thread local sorage)에 저장되고 꺼내어진다. 접근하는 스레드에 따라 저장하는 위치와 반환하는 데이터가 다르다.

> TLS는 각 스레드 별로 다른 값을 가지는 전역 변수이다. 기존의 전역 변수는 모든 스레드가 공유하므로 접근 시 `Race Condition`이 발생할 수 있다. 따라서 스레드마다 개별적으로 사용할 수 있는(thread-local) 특수한 전역 변수를 사용한다.

<img src="https://user-images.githubusercontent.com/57310034/103731838-76af1900-5029-11eb-82df-650317cf4741.png"/>  

```java
//set
public void set(T value) {
    Thread t = Thread.currentThread(); //현재 스레드 가져오기
    ThreadLocalMap map = getMap(t); //현재 스레드의 Local storage
    if (map != null)
        map.set(this, value);
    else
        createMap(t, value);
}

//get
public T get() {
    Thread t = Thread.currentThread(); //현재 스레드 가져오기
    ThreadLocalMap map = getMap(t); //현재 스레드의 Local storage
    if (map != null) {
        ThreadLocalMap.Entry e = map.getEntry(this);
        if (e != null) {
            @SuppressWarnings("unchecked")
            T result = (T)e.value; //T로 캐스팅하여 반환
            return result;
        }
    }
    return setInitialValue(); 
}
```  

메인 스레드의 메인 Looper는 `ActivityThread#main()` 메서드에서 `Looper.prepareMainLooper()`를 호출하여 생성된다.  

```java
public final class Looper {

    private static Looper sMainLooper; //메인 Looper 인스턴스

    static final ThreadLocal<Looper> sThreadLocal = new ThreadLocal<Looper>(); //TLS에 저장하고 꺼내기 위한 ThreadLocal<Looper> 인스턴스
    ...

    // 메인 Looper 준비
    public static void prepareMainLooper() {
        prepare(false);
        synchronized (Looper.class) {
            if (sMainLooper != null) {
                throw new IllegalStateException("The main Looper has already been prepared.");
            }
            sMainLooper = myLooper(); //메인 Looper 인스턴스 초기화
        }
    }

    //메인 Looper 인스턴스 초기화
    public static @Nullable Looper myLooper() {
        return sThreadLocal.get(); //ThreadLocal<Looper>를 이용하여 TLS에서 해당 스레드의 Looper를 생성/반환
    }
    ...
}
```  


## MessageQueue

Looper는 각각의 `MessageQueue`를 가진다. 특이 메인 스레드에서는 이 `MessageQueue`를 통해서 UI 작업에서 `Race Condition`을 해결한다. 안드로이드 개발 중에 문제 해결을 위해 스레드 별로 다른 큐 구조가 필요하다면 직접 구현하는 방법도 있지만 `Looper`를 사용해 더욱 단순히 구현할 수도 있다.  

<img src="https://user-images.githubusercontent.com/57310034/103733505-3f426b80-502d-11eb-9a45-4524820b6b9c.png"/>  


## Looper.loop()

Looper는 그 이름도 그렇듯이 기본적으로 무한히 반복하며 메시지를 처리한다. 이 무한 반복을 시작하기 위해 `Looper.loop()` 메서드를 사용한다.  

```java
public static void loop() {
    final Looper me = myLooper(); //호출한 스레드의 Looper를 TLS로 부터 가져온다.
    if (me == null) { //prepare()를 통해 생성하지 않았다면 예외 발생
        throw new RuntimeException("No Looper; Looper.prepare() wasn't called on this thread.");
    }
    ...

    final MessageQueue queue = me.mQueue; //호출한 스레드의 MessageQueue
    ...

    for (;;) {
        Message msg = queue.next(); //MessageQueue의 첫 Message를 꺼냄
        if (msg == null) { //꺼낸 메시지가 null이면 Loop를 종료한다. --> quit(), quitSafely() 호출 시 발생
            return;
        }
        ...
        //꺼낸 Message를 처리
        //여기서 "target" 은 메시지에 함께 담겨 온 Handler 인스턴스
        //Handler 인스턴스가 dispatchMessage()를 통해 메시지를 처리한다.
        msg.target.dispatchMessage(msg); 
        ...
        msg.recycleUnchecked();
        }
    }

//Looper를 종료
public void quit() {
    //아직 처리되지 않은 Message를 모두 제거한다
    mQueue.quit(false);
}

//Looper를 종료
public void quitSafely() {
    //해당 메서드를 실행하는 시점에 현재 시간보다 타임스탬프가 뒤에 있는 Message를 제거하고 그 앞에 있는 Message는 계속해서 처리
    mQueue.quit(true);
}
```  
<br>

# Message와 MessageQueue

`MessageQueue`는 `Message`를 담는 FIFO 자료구조이다. MssageQueue의 구조는 다음 노드의 주소 값을 변수로 갖고 있는 `LinkedBlockingQueue`와 유사하다. 따라서 일반적으로 개수 제한이 없고 삽입/삭제가 빠르다.  

## Message Class

```java
public final class Message implements Parcelable {
    //아래 다섯가지 public 변수에 Message에 담을 정보를 저장한다.
    public int what; //사용자 정의 식별자

    //간단한 정수를 저장하고자 할 때는 arg1, arg2를 이용할 수 있다. setData()보다 훨씬 가볍다.
    public int arg1; 

    public int arg2;

    //parcelable한 객체를 메시지에 담을 때 사용
    //그 외의 객체는 setData()를 이용
    public Object obj; 

    //해당 메시지에 응답을 보내려면 해당 변수를 사용
    public Messenger replyTo;
    ...

    //아래 다섯가지 변수는 패키지 프라이빗 변수
    //android.os 패키지 아래에 함께 있는 Looper, MessageQueue, Handler 등이 직접 접근하여 사용한다.
    @UnsupportedAppUsage
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    //sendxxx(), postxxx()를 호출할 때 실행시간 저장
    //나중에 호출된 것이라도 타임스탬프가 빠르면 큐 중간에 삽입
    public long when; 

    /*package*/ Bundle data;

    @UnsupportedAppUsage
    /*package*/ Handler target; //메시지를 전달한 핸들러를 담음

    @UnsupportedAppUsage
    /*package*/ Runnable callback; //메시지를 처리할 때 실행될 콜백

    @UnsupportedAppUsage
    /*package*/ Message next;
    ...
}
```

`postxxx()`, `sendxxx()`메서드에서 실행 시간을 `public long when` 변수에 담아 이미 큐에 존재하는 Message 보다 타임스탬프가 빠르면 큐 중간에 삽입된다. 삽입이 빠른  `MessageQueue`의 링크 구조는 이런 상황에 상당히 유리하다.  

## obtain() 메서드를 통한 Message 생성

Message를 생성하기 위한 방법은 다음과 같다.

1. `new Message()`
2. `Message#obtain()` 
3. `Handler#obtainMessage()` 

어플리케이션이 실행되는 동안 수 많은 Message가 빈번하게 생성/삭제된다. 이런 상황에서 `new` 키워드로 매번 Message를 생성하는 것은 좋은 선택이 아니다. `Message.obtain()` 은 오브젝트 풀 패턴을 사용하고 있고, `Handler#obtainMessage()` 또한 내부적으로 `Message#obtain()`을 호출하고 있기 때문에 일반적으로 이 두 가지 방법을 권장한다.

> 오브젝트 풀 패턴 : 메모리 사용 성능을 개선하기 위해 객체를 매번 할당, 해제하지 않고 고정 크기 풀에 들어 있는 객체를 재사용할 수 있는 구조

```java
//메시지들이 LinkedList의 형태로 풀을 이루고 있고, 그 Head를 가리킴
private static Message sPool; 
...
private static final int MAX_POOL_SIZE = 50; //메시지 풀의 사이즈. 최대 50개
...

public static Message obtain() {
    synchronized (sPoolSync) {
        if (sPool != null) {
            Message m = sPool; //메시지 풀의 head를 가져온다
            sPool = m.next; //원래 head 바로 다음에 있던 메시지를 head로 승격
            m.next = null; //떼어낸 메시지의 다음은 null로 지워준다
            m.flags = 0; // 해당 메시지 객체는 사용중임을 표시
            sPoolSize--; //풀에서 메시지를 하나 꺼냈기 때문에 카운트 감소
            return m;
        }
    }
    return new Message(); //풀이 비어있으면 new 키워드로 새로 생성
}

//메시지를 모두 사용하고 호출하는 메서드
void recycleUnchecked() {
    //맴버 변수들을 초기값으로 되돌림
    flags = FLAG_IN_USE;
    what = 0;
    arg1 = 0;
    arg2 = 0;
    obj = null;
    replyTo = null;
    sendingUid = UID_NONE;
    workSourceUid = UID_NONE;
    when = 0;
    target = null;
    callback = null;
    data = null;

    synchronized (sPoolSync) {
        if (sPoolSize < MAX_POOL_SIZE) { //메시지 풀이 가득 찼는지 확인
            next = sPool; //기존 풀의 head를 현재 메시지 뒤에 연결
            sPool = this; //head를 현재 메시지로 변경
            sPoolSize++; //풀 사이즈 증가
        }
    }
}
```  

`Looper#loop()` 메서드에서 Message를 처리하면 `recycleUnChecked()`를 통해 Message를 초기화해서 풀에 추가한다. 위 코드를 보면 `new Message()`를 사용해서 Message를 생성해도 재활용이 되긴하나, 풀이 금방 찰 것이다. 따라서 `Message#obtain()`를 활용하는 것이 좋다.  
<br>

# Handler

`Handler`는 Message를 MessageQueue에 넣거나 꺼내어 처리하는 기능을 모두 제공한다. Handler가 Looper, MessageQueue와 어떤 관계가 있는지 살펴보자.  

## 생성자

- `Handler()` //deprecated
- `Handler(Handler.Callback callback)` //deprecated
- `Handler(Looper looper)`
- `Handler(Looper looper, Handler.Callback callback)`

안드로이드 프레임워크 코드를 몇 번 뜯어본 사람이라면 바로 알겠지만 1~3번째 생성자는 4번째 생성자를 다시 생성한다. ([`Telescoping Constructor Pattern`](http://www.javabyexamples.com/telescoping-constructor-in-java)) 즉 Handler는 Looper(결국엔 MessageQueue)와 연결되어 있다는 것이다.  

Looper를 파라미터로 명시하지 않은 생성자는 생성자를 호출하는 스레드의 Looper를 사용한다. 따라서 메인 스레드에서 사용할 경우 `ActivityThread`에서 생성한 메인 Looper를 사용하기 때문에 UI작업을 할 때 많이 사용했다. 하지만 작업 손실, 스레드 참조 실패, 관련된 스레드와의 `Race Condetion` 등의 위험이 있어 deprecated 되었다. UI 작업을 위해 `getMainLooper()`를 사용하거나 `Loop#myLooper()`를 사용해 Looper를 명시해주어야 한다.

`Handler.Callback` 파라미터는 메시지를 처리할 콜백을 구현한 인스턴스를 필요로 한다. 생략하면 null이 된다.  

## 동작

앞서 말했듯이 Handler는 Message를 MessageQueue에 보내는 것과 처리하는 것를 모두 제공한다 (send). 그리고 대상 스레드에서 실행하고싶은 코드를 전달만 하는 기능도 함께 제공한다. 이는 `Runnable` 객체를 통해 구현할 수 있다. (post)


||send|post|
|:-|:-|:-|
|기본|sendEmptyMessage(int waht)<br>sendMessage(Message msg)|post(Runnable r)|
|-Delayed|sendEmptyMessageDelayed(int what, long delayMillis)<br>sendMessageDelayed(Message msg, long delayMillis)|postDelayed(Runnable r, long delayMillis)|
|-AtTime|sendEmptyMessageAtTime(int what, long uptimeMillis)<br>sendMessageAtTime(Message msg, long uptimeMillis)|postAtTime(Runnable r, Object token, long uptimeMillis)<br>postAtTime(Runnable r, long uptimeMillis)|
|-AtFrontOfQueue|sendMessageAtFrontOfQueue(Message msg)|postAtFrontOfQueue(Runnable r)|

- `sendEmpty~()`메서드는 Message의 `waht` 값만 전달한다. 이는 사용자가 지정한 식별자의 역할을 한다.
- `~Delayed()`메서드는 내부적으로 `~AtTime()`메서드를 호출한다. 현재 시간에서 파라미터로 전달한 `delayMillis`을 더해 `uptimeMillis` 파라미터로 전달한다.
- `~AtFrontOfQueue()`는 특별한 상황(권한 문제나 심각한 서버 문제 등 앱을 더 이상 쓸 수 없는)이 아니면 쓰지 않도록 권장하고있다.

`post~()` 메서드는 결국 내부적으로 `send~()`메서드를 호출한다. 다만 post 의 목적은 Message 처리가 아니기 때문에 `Message msg` 파라미터를 갖고 있지도 않고, 이를 처리하기 위한 복잡한 구현부를 작성할 필요도 없다. 그저 일방적으로 대상 스레드에서 실행하고 싶은 실행 부분을 담은 객체 Runnable만을 빈 Message에 채워 send 한다.

```java
public final boolean post(@NonNull Runnable r) {
    return  sendMessageDelayed(getPostMessage(r), 0); //Runnable을 담은 메시지를 send
}
...
private static Message getPostMessage(Runnable r) {
    Message m = Message.obtain();
    m.callback = r; //메시지에 Runnable만 담아서 반환
    return m;
}
```

### dispatchMessage()

Message에는 이를 전달한 Handler 포인터가 포함되어있고([`Message.target`](##Message-Class)), [`Looper#loop()`](##Looper.loop())메서드에서는 반복문 내에서 `msg.target.dispatchMessage()`를 호출해 Message를 처리한다.  

```java
public void dispatchMessage(@NonNull Message msg) {
    if (msg.callback != null) { 
        handleCallback(msg); //Runnable 이 있다면 그것을 실행
    } else {
        if (mCallback != null) { //Runnable이 없다면 Handler.Callback을 실행
            if (mCallback.handleMessage(msg)) { 
                return;
            }
        }
        handleMessage(msg); //둘 다 없다면 서브클래스에서 재정의한 메서드 실행
    }
}
```

Handler는 두 가지 콜백을 가지고 있다. 하나는 메시지를 어떻게 처리할지 구현한 `Handler.Callback` 타입의 객체이며, 나머지 하나는 대상 스레드에서 실행시킬 코드를 담은 `Runnable` 객체이다. 
- `Handler.Callback` : Handler 생성자를 통해 전달 ==> `mCallback` (Handler가 갖고 있음)
- `Runnable` : ==> `post~()`메서드를 통해 전달 ==> `msg.callback` (Message가 갖고 있음)

Handler가 가지고 있는 Handler.Callback은 해당 Hendler가 처리할 Message에 모두 사용된다. 하지만 Message가 Runnable을 갖고 있는 경우, 이는 Message를 처리하기 위함이 아니라 해당 스레드에서 실행할 코드를 `post`한 것임으로 Handler.Callback이 아닌 Runnable이 우선 실행된다.  

## 용도

### 백그라운드 스레드에서의 UI 업데이트

백그라운드 스레드에서 네트워크나 DB 작업을 하는 도중에 UI를 업데이트 하기위해서 메인 Looper에서 실행할 UI 업데이트 코드를 Runnable 객체에 담에 post한다.  

### 메인 스레드에서 다음 작업 예약

UI 작업 중에 다음 UI 갱신 작업을 MessageQueue에 넣어 예약해야할 경우가 있다. 예를 들어 액티비티에서 `onCreate()`메서드에서 하지 못하는 일 등이 바로 그것이다. 소프트 키보드를 띄우는 작업은 `onCreate()` 메서드에서는 잘 동작하지 않는다. 이때 해당 작업을 MessageQueue에 넣어두면 현재 작업이 모두 끝난 후에 Message를 처리할 것이다.

### 반복 UI 갱신

반복해서 UI를 갱신해야할 때 사용이 가능하다. `DigitalClock` 같은 시계 위젯도 `Handler`를 이용해서 현재 시간을 갱신해 보여준다. `Runnable#run()` 메서드 내에서 또다시 `postDelayed(this, DELAY_TIME);`을 호출하면 `DELAY_TIME`마다 계속 반복해 실행될 것이다.  

### 시간 제한

BLE(Bluetooth Low Energe) 디바이스를 스캔할 때 등 사용할 수 있다. 제한 시간 뒤에 `stopLeSacn()`을 호출하는 Runnable 객체를 post하면 된다. 혹은 몇 초 내에 뒤로가기 버튼을 반복해서 누를 때 앱을 종료하는 기능을 구현할 때도 사용할 수 있다.

```java
private boolean isBackPressedOnce = false;

@Override
public void onBackPressed() {
    if (isBackPressedOnce) {
        super.onBackPressed()
    } else {
        Toast.mackText(this, "뒤로가기 버튼을 한 번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show();
        isBackPressedOnce = true;
        //1초 내에 뒤로가기 버튼을 다시 누르지 않으면 Runnable이 실행되어 isBackPressOnce를 false로 만들어버린다
        timerHandler.postDelayed(timerTask, 1000);
    }
}

private final Runnable timerTask = new Runnable() {
    @Override
    public void run() {
        isBackPressedOnce = false;
    }
}
```

### H 클래스

`ActivityThread`의 내부 클래스인 `H`는 Handler를 상속하고 컴포넌트 생명주기 Message들은 모두 해당 클래스를 거쳐 실행된다. 이때 Message의 what 변수에 들어가는 식별자는 `LAUNCH_ACTIVITY`, `RESUME_ACTIVITY`, `CREATE_SERVICE`, `BIND_APPLICATION`등이 있다.

### 터치 및 그리기 이벤트 처리

Handler를 상속한 `ViewRootImpl` 클래스에서 터치나 그리기(invalidate) 등의 이벤트를 처리한다.

### runOnUiThread()

액티비티는 멤버 변수로 Handler를 가지고 있다. 액티비티 스코프 내에서 Ui 업데이트를 위해 `runOnUiThread()` 메서드를 Runnable과 함께 호출하면, 호출한 스레드가 메인 스레드라면 바로 실행, 아니라면 메인 Looper에 post한다.

## Handler의 타이밍 이슈

개발을 하다보면 원하는 동작 시점과 실제 동작 시점에서 차이가 나 곤란한 상황이 분명 생긴다. 필요한 자원이 아직 초기화되지 않아 NPE를 발생시키는 경우도 있다. 이때 Handler를 사용하면 꽤 단순히 해결할 수 있다.  

액티비티의 `onCreate()` 메서드에서 `post()` 메서드를 실행하면, 이때 넘겨준 Runnable이 실행되는 시점은 언제일까? 메인 스레드에서는 여러 작업이 엉키지 않도록 하기 위해 메인 Looper의 MessageQueue에서 Message를 하나씩 꺼내 처리한다. 그리고 마침, 메인 Looper에서 Message 하나 꺼내 실행하면 `onCreate()`부터 `onReasum()`까지 쭉 실행된다. 따라서 `onCreate()`에서 post한 Runnalbe 객체는 `onReaume()` 이후에 실행된다. 액티비티 초기화 이후에 실행되어야 할 코드를 이런식으로 예약하면 좋다.  

### 지연 Message의 처리 시점

`~Delay()`나 `~AtTime()` 메서드로 딜레이를 준 Message는 사실 정확한 시간에 실행되지는 못한다. 먼저 꺼낸 Message 처리가 오래 걸린다면 실행이 당연히 늦어진다.  

```java
Handler handler = new Handler();

//메시지를 먼저 보내지만 200ms 뒤에 실행되도록 한다.
handler.postDelayed(new Runnable() {
    @Override
    public void run() {
        Log.d("TAG", "200 delay");
    }
}, 200);

//앞선 메시지보다 늦게 보냈지만 바로 실행되어 500ms 동안 sleep한다
handler.post(new Runnable() {
    @Override
    public void run() {
        Log.d(TAG, "just");
        SystemClock.sleep(500);
    }
})
```
이 둘은 단일 스레드에서 실행되기 때문에 앞의 작업이 모두 끝나야만 뒤의 작업을 수행할 수 있다. 따라서 "200 delay" 로그는 200ms가 아닌 최소 500ms 이후에 찍히게 된다. 따라서 일정 간격을 두고 post할 경우에는 그 사이에 다른 Message가 MessageQueue에 쌓일 가능성을 염두에 두어야 한다. 생각보다 이런 실수는 흔히 발생한다.  

<br>
<br>


--- 
해당 포스팅은 [안드로이드 프로그래밍 Next Step - 노재춘 저](http://www.yes24.com/Product/Goods/41085242) 을 바탕으로 내용을 보충하여 작성되었습니다.


참고  
TLS :
[http://studyfoss.egloos.com/5259841](http://studyfoss.egloos.com/5259841)

오브젝트 풀 패턴:
[http://hajeonghyeon.blogspot.com/2017/06/object-pool.html](http://hajeonghyeon.blogspot.com/2017/06/object-pool.html)

send, post:
[https://recipes4dev.tistory.com/170](https://recipes4dev.tistory.com/170)