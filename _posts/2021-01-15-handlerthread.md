---
layout: post
title: "안드로이드 HandlerThread와 활용"
subtitle: "안드로이드 백그라운드 스레드 구현 방법"
author: "GuGyu"
header-style: text
tags:
  - Android
  - AndroidFramework
  - Study
  - Thread
  - Handler
  - HandlerThread
  - Background
---

메인 스레드는 UI를 그리는데 사용되기 때문에, 이 외의 작업은 백그라운드에서 수행하여 성능을 향상시킬 수 있다. HandlerThread 를 사용하여 백그라운드 스레드를 활용할 수 있는 방법에 대해 간단히 알아보고자 한다.

## HandlerThread 클래스

`HandlerThread`는 `Thread`를 상속하고, 내부에서 `Looper.prepare()`와 `Looper.loop()`를 실행하는 Looper 스레드이다. 이름만 보면 Handler를 사용할 것 같지만 그렇지 않고, Looper를 가지고 있으며 Handler에서 사용하기 위한 스레드이다. 여기에서 만든 Looper를 Handler의 생성자(`Handler(Looper looper)`)로 넘겨 연결할 수 있다.  

HandlerThread를 사용하지 않았을 때의 코드를 살펴보자.  
<br>

```java
class LooperThread extend Thread {
    public Handler handler;

    public void run() {
        Looper.prepare();
        Looper looper = Looper.myLooper();
        if (looper == null) {
            return;
        }
        handler = new Handler(looper) {
            public boolean handleMessage(Message msg) {
                //Message 처리
            }
        };
        Looper.loop(); //여기서 무한 루프 실행
    }
}
```
위 코드를 보면 `Looper.loop()` 부터는 무한 루프에 빠져 더 이상 진행되지 않는다. 따라서 해당 반복문을 종료하려면 여기서 사용된 `Looper` 객체를 다른 스레드에서 접근해 `Looper.quit()`를 호출해줘야한다.  

이렇게 설계하기 위해 `HandlerThread`를 사용할 수 있다. HandlerThread는 `Looper.loop()`만 돌고 있는 백그라운드 스레드를 하나 생성한다. 이 스레드는 명시적으로 종료하기 전까지는 계속해서 살아있고, 들어온 Message들을 계속해서 처리한다.

Message를 HandlerThread로 보내주기 위해 스레드 외부에서 Handler를 생성해야하는데 이때 `HandlerThread#getLooper()`를 사용해 Handler와 Looper를 연결해줄 수 있다.

```java
private HandlerThread handerThread;

public Processor() {
    handlerThread = new HandlerThread("Message Thread");
    handlerThread.start(); //백그라운드 스레드가 생성되어 무한 루프 중
}

public void process() {
    ...
    //생성된 HandlerThread의 Looper와 연결된 핸들러를 스레드 외부에서 생성
    //메시지를 보내 처리하도록 함
    new Handler(handerThread.getLooper()).post(new Runnuble() {
        @Override
        public void run() {
            ...
        }
    });
}
```  

위 처럼 HandlerThread는 별도 백그라운드에서 계속 돌아가고 있고, 메인 스레드에서 Handler를 생성해 HandlerThread로 메시지 처리를 요청할 수 있다.
<br>

### HandlerThread 프레임워크 소스

```java
public class HandlerThread extends Thread {

    Looper mLooper;
    ...

    @Override
    public void run() {
        Looper.prepare();
        synchronized (this) {
            //Looper를 맴버 변수로 저장
            mLooper = Looper.myLooper();
            notifyAll(); //대기 중인 스레드를 깨움
        }
        ...
        Looper.loop();
        ...
    }

    public Looper getLooper() {
        //Thread에서 start()를 호출했는지 여부
        if (!isAlive()) {
            return null;
        }
        ...

        synchronized (this) {
            //start()이후 run()이 실행될 때 까지 반복문을 돌려 기다림
            while (isAlive() && mLooper == null) {
                try {
                    wait(); //대기 (notifyAll()을 기다림)
                } catch (InterruptedException e) {
                   ...
                }
            }
        }
        ...

        return mLooper;
    }

    public boolean quit() {
        //getLooper()를 거쳐서 종료
        Looper looper = getLooper();
        if (looper != null) {
            looper.quit();
            return true;
        }
        return false;
    }
    ...
}
```
위 코드를 보면 `run()` 내에서 `Looper.prepare()` 이후 Looper 객체를 `mLooper`에 저장하는 것을 볼 수 있다. 이는 이후로 `getLooper()` 메서드를 통해 외부에서나 내부에서 참조할 수 있다. 따라서 스레드 외부에 있던 Handler의 생성자에도 사용할 수 있었던 것이다.  

그런데 내부에서는 왜 직접 `mLooper`에 접근하지 않고 `getLooper()`를 거칠까? `getLooper()`는 단순히 `mLooper`를 리턴하는데 그치지 않고 여러가지 검사를 한다. 우선은 Thread에서 `start()`를 호출하였는지 검사하고, 호출되지 않았다면 null을 반환한다. 그런데 `start()`가 호출되었다고 해서 바로 `run()`이 실행되는 것은 아니다. 따라서 while 문을 사용해 mLooper가 null인지를 계속해서 체크하며 대기한다. mLooper가 초기화되고 나서야 반복문을 빠져나와 mLooper를 반환한다.  

<br>

### 활용

HandlerThread는 주로 UI와 관련없지만 단일 스레드에서 순차적인 작업이 필요할 때 사용된다. 

예를 들어 인스타그램의 좋아요 기능을 살펴보자. 사용자가 좋아요 버튼을 누르더라도 UI를 블로킹하지 않도록 별도 스레드에서 DB에 반영하도록 한다. 사용자가 좋아요 버튼을 마구 눌러 바꾸기도 하기 때문에 좋아요 유무가 바뀔 때 마다 스레드를 생성하면 순서가 바뀔 수도 있다. 스레드가 무조건 `start()`를 호출한 순서대로 실행되지는 않기 때문이다.  

이런 경우에는 실행 순서를 반드시 순차적으로 맞춰야한다. 따라서 `HandlerThread`를 사용하면 이를 단일 스레드에서 순차적으로 처리할 수 있다.  

```java
private Handler favoritHandler;
private HandlerThread handlerThread;

@Override
public void onCreate(Bundle savedInstanceState) {
    ...
    handlerThread = new HandlerThread("Favorit Processing Threaed");
    handlerThread.start(); //좋아요 처리할 단일 스레드 작동

    //위 스레드와 연결된 핸들러 생성
    favoritHandler = new Handler(handlerThread.getLooper()) {
        @Override
        public void handleMessage(Message msg) {
            //메시지 처리 방식 --> DB 저장
            Favorite favorite = (Favorite) msg.obj;
            FavoritDao.updateFavorit(favorite.id, favorite.favorite);
        }
    };
}
```
```java
...
//좋아요 버튼 누를 때
holder.favorite.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        Message message = favoritHandler.obtainMessage();
        //메시지에 좋아요 정보 담기
        message.obj = new Favorite(item.id, checked);
        //좋아요 처리 스레드(HandlerThread)로 보내기
        favoriteHandler.sendMessage(message);
    }
});
...
```










<br>
<br>


--- 
해당 포스팅은 [안드로이드 프로그래밍 Next Step - 노재춘 저](http://www.yes24.com/Product/Goods/41085242) 을 바탕으로 내용을 보충하여 작성되었습니다.


참고  
