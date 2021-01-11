---
layout: post
title: "메인 스레드와 Handler (2)"
subtitle: "UI 변경 메커니즘과 ANR"
author: "GuGyu"
header-style: text
tags:
  - Android
  - AndroidFramework
  - Study
  - Thread
  - Handler
  - UI
  - ANR
---
# 5. UI 변경 메커니즘

메인 스레드에서 UI를 변경할 때의 메커니즘에 대해 알아보자. UI를 변경하는 메서드는 주로 `setXXX()`으로 되어있다. (ex. `setText()`, `setBackgroundResource()`...) 이 세터 메서드 변경하고 싶은 값을 넘겨주면 내부적으로 `invalidate()` 메서드가 실행된다.

### invalidate()

UI의 변경 사항들을 반영하여 다시 그리도록 요청하는 메서드이다. 이 메서드를 호출해야만 메인 Looper의 MessageQueue에 들어가서, 다음 타이밍에 화면을 갱신해준다. 이때 `onDraw()`가 호출된다. 따라서 커스텀뷰를 만들 때 세터 메서드를 작성한다면 `invalidate()`를 꼭 호출하여야한다. 

```java
public void setTitle(String title) {
    this.title = title;
    invalidate(); //호출해야 변경 사항을 갱신할 수 있음
}
```

### invalidate()의 호출 스택

invalidate()부터 시작하는 메서드를 따라가보자.

1. `View`의 invalidate() 메서드는 자신이 포함된 `ViewGroup`에게 자신의 영역을 다시 그려달라고 요청하기 위해서 ViewGroup의 `invalidateChild(View child, final Rect dirty)`를 호출한다.

2. `invalidateChild()` 메서드는 `do while`문에서 `parent` 변수에 `parent.invalidateChildInParent(location, dirty)`를 대입하고, parent가 null이 아닌 동안에 계속 호출한다. <br>
여기서 `ViewParent` 인터페이스가 등장한다. 의외로 `View`에는 `getViewGroup()`같은 메서드는 존재하지 않고, 대신 `getParent()`가 존재한다. 이 메서드는 이름처럼 `ViewParent`를 리턴한다. 따라서 어떤 View의 상위 ViewGroup을 참조하기 위해서는 `getParent()`로 상위 ViewGroup의 ViewParent를 가져와 ViewGroup으로 캐스팅해야한다.<br>
`do while` 문에서 View/ViewGroup이 invalidate 작업을 계속 상위로 전달하다보면 최상위 ViewGroup까지 닿을 것이다. 하지만 여기서 뷰 갱신에 대한 Message를 보내는 것이 아니라 로직을 분리하기위해 가상의 상위 ViewGroup인 `ViewRootImpl`까지 전달한다.<br>
ViewGroup과 ViewRootImpl은 모두 ViewParent인터페이스를 구현한 것으로 `invalidateChildInParentI()`는 바로 ViewParent 인터페이스의 메서드이다.

3. 결과적으로 invalidate() 메서드는 do while문을 거쳐 ViewRootImpl의 `invalidateChildInParent(int[] location, Rect dirty)`를 호출한다.

4. 상위로 뷰 갱신 요청을 전달하는 View/ViewGroup과는 다르게, ViewRootImpl에서 구현된 `invalidateChildInParent()`의 주 작업은 `scheduleTraversals()` 메서드를 호출하는 것이다. 이 메서드는 무효화된(invalidated) 뷰 영역들을 다시 그리기 위한 순회(traversal) 작업을 스케줄링해준다. 이 스케줄링 작업은 `Choreographer`에 위임하여 메인 Looper의 MessageQueue에 들어간다.

<img src="https://user-images.githubusercontent.com/57310034/104166929-e3b61a80-543e-11eb-95f3-0963e9718d6b.png"/>

### invalidate()의 중복 호출

```java
public void onClick(View view) {
  for (int i = 0; i < 5; i++) {
    currentValue.setText("Current Value = " + i);
    System.Clock.sleep(1000);
  }
}
```

위 코드는 TextView의 `setText()`를 1초에 한 번씩 5초에 걸쳐 내용을 수정하는 코드이다. 알다시피 `setText()`의 내부서에는 `invalidate()`가 호출된다. 하지만 이를 실행해보면 화면이 1초마다 변경되지 않고 5초 후 마지막 변경 값인 "Current Value = 4"만 보이는 것을 확인할 수 있다. 왜 그럴까?

View에서는 `mPrivateFlags`라는 플래그를 사용해서 invalidate()를 여러 번 호출해도 첫 번째 호출만 ViewRootImpl까지 전달할 수 있도록 한다. 첫 번째 invalidate() 호출 시 `invalidateInternal()`메서드의 if문 내에서 플래그를 변경하기 때문에 그 다음 invalidate() 호출은 여기서 걸러진다. 하지만 Message를 이미 처리해 화면에 반영되었다면 invalidate()는 유효하게 호출이 되어야 할 것이다. `mPrivateFlags`는 패키지 프라이빗 변수로, 같은 패키지의 `View`, `ViewGroup`, `ViewImpl` 세 군데에서 적절하게 이 값을 변경해 이런 문제를 해결하고 있다.

참고로 하나의 뷰 내에서 invalidate()를 계속해서 호출하는 것이 아니라 각각 다른 뷰의 invalidate()를 호출해도 결과는 마찬가지이다.

```java
public void onClick(View view) {
  for (int i = 0; i < 5; i++) {
    currentValue.setText("Current Value = " + i);
    editText.setText("current Editable Value = " + i);
    System.Clock.sleep(1000);
  }
}
```

두 뷰에서 각각 invalidate()를 호출할 이고, 둘다 ViewRootImal에 도달하지만 ViewRootImpl에서는 `mTraversalScheduled` 변수를 가지고 이미 스케줄링을 한 상태인지 또 체크한다. 한 번 스케줄링되었다면 다시 넣지 않는다.  

<br>

# 6. ANR

> "Application Not Responding"(ANR)

개발을 하거나 어플리케이션 사용 중에 자주 볼 수 있는 메시지이다. 어떤 동작에서 메인 스레드를 오랫동안 점유하고 있다는 의미이다. 따라서 메인 스레드 점유가 끝날 때 까지 대기할 것인지, 프로세스를 종료할 것인지 사용자에게 묻는 과정을 거친다. 아무리 허점없이 코드를 작성해도 단말기기의 상태가 좋지 않으면 ANR이 발생할 수 있다. 그러니 ANR을 완벽히 없앤다기보다는 최대한 줄이는 것을 목표로 개발을 해야한다.  

### ANR 타임아웃

ANR 타임아웃을 프레임워크 소스에서 직접 확인해보면 다음과 같다. (com.android.servier.am.ActivityManagerService.java)

```java
// How long we allow a receiver to run before giving up on it.
static final int BROADCAST_FG_TIMEOUT = 10 * 1000;
static final int BROADCAST_BG_TIMEOUT = 60 * 1000;

// How long we wait until we timeout on key dispatching.
static final int KEY_DISPATHING_TIMEOUT = 5 * 1000;
```

Broadcast 리시버 타임아웃은 포그라운드와 백그라운드가 다르다. `sendBroadcast()`에 전달되는 `Intent`에 `Intent.FLAG_RECEIVER_FOREGROUND` 플래그를 추가하면 포그라운드에서 실행된다. `ActivityManagerService`에는 포그라운드/백그라운드 용도의 `BroadcastQueue`가 각각 있는데, 큐에 쌓인 순서에 상관없이 포그라운드 용도의 큐를 먼저 처리한다.

com.android.server.am.ActiveService.java 파일을 살펴보면 서비스에 대한 타임아웃도 있다.
```java
// How long we wait for a service to finish executing.
static final int SERVICE_TIMEOUT = 20 * 1000;
```

이렇게 타임아웃은 크게 3가지로 나눌 수 있다. BroadcastReceiver와 Service, 그리고 InputDispatching(액티비티에서 발생함으로 액티비티 타임아웃이라고도 함) 타임아웃이다.

### ANR 판단

ANR 발생 시 `ActivityManagerService`의 `appNotResponding()` 메서드에서 관련 다이얼로그를 띄우는 등의 일을 처리한다.

#### BroadcastReceiver와 Service

브로드캐스트 리시버와 서비스는 시작 전에 Handler의 `sendMessageAtTime()` 메서드를 사용해서 Message를 보내고, 타임아웃이 되면 `appNotResponding()` 메서드를 호출한다. 메시지가 처리되기 전에 작업을 모두 끝내면 해당 Message를 제거하는 식으로 동작한다.

그런데 BroadReceiver의 `onReceive()`가 1분 이상 지속된다고 해도 메인 스레드를 점유하고 있으니 타임아웃을 처리할 Message도 Queue에서 빠져나오지 못하고 있진 않을까? 하지만 ActivityManagerService는 `system_server`로 떠있는 별도의 프로세스에서 동작하기 때문에 여기서 보낸 Handler는 앱 프로세스의 메인 프로세스와는 전혀 관련이 없다.

따라서 IDE 디버그모드에서도 ANR이 계속 발생한다. 브레이크 포인트에서 앱이 멈춰있어도 `system_server` 프로세스는 계속 동작하니 타임아웃을 측정하고 처리하기 때문이다.

#### InputDispatching (터치와 키 입력)

화면 터치나 키 입력 시에 발생하는 InputDispatching 타임아웃을 알아보기 전에 화면 터치와 키 입력을 전달하는 과정에 대해 먼저 알아보자.

터치나 키 입력에 대한 이벤트 감지는 앱 수준에서 하는 것이 아니라 커널에서 네이티브 단을 거쳐서 앱에 전달된다. 이때 `InputReader`에서 `EventHub`를 통해 커널에서 이벤트를 가져오고 InputDispacher가 이를 앱에 전달한다.

Activity는 PhoneWindow를 갖고 PhoneWindow는 ViewRootImpl과 1:1 매핑된다. ViewRootImpl의 내부 클래스인 `WindowInputEventReceiver`가 앱으로 전달된 이벤트를 받아서 하위 ViewGroup/View로 전달한다. 이때 WindowInputEventReceiver에서 전달받는 파라미터는 InputEvent로, MotionEvent와 KeyEvent의 상위 추상 클래스이다.

<img src="https://user-images.githubusercontent.com/57310034/104181807-be330c00-5452-11eb-879d-cf7ddd457844.png"/>  
<br>

InputDispatching 타임아웃은 네이티브 소스인 `InputDispatcher.cpp`에 지정되어있다. 이 네이티브 클래스의 주요 메서드는 `dispatchMotionLocked()`와 `dispatchKeyLocked()`인데 각각 터치 이벤트와 키 이벤트를 전달하는 역할을 한다. 이때 `findFocuseWindowTargetsLocked()`메서드에서 이벤트를 전달할 window를 먼저 찾는데, `isWindowReadyForMoreInputLocked()`메서드에서 기존 이벤트를 처리하느라 대기해야하는지 판단한다. 그러고 나서 이벤트를 전달하지 않고 기다리다가 타임아웃이 되어버리면 `onANRLocked()` 메서드를 호출하고 com.android.server.input.InputManagerService의 `NotifyANR()` 메서드를 거쳐서 `ActivityManagerService`의 `appNotResponding()` 메서드에 이른다.

간단하게 말하자면 메인 스레드를 어디선가 점유하고 있어서 키 이벤트를 정해진 시간 내에 전달하지 못하면 ANR이 발생한다. 키 이벤트인 볼륨 키는 눌리고서 5초 이상 지연 시 바로 ANR을 발생시킨다. 참고로 홈 키와 전원 키는 앱과 별개로 동작하기 때문에 ANR과는 무관하다.

하지만 터치 이벤트는 타임아웃이 된다고 해서 바로 ANR이 발생하지는 않는다. 첫 번째 터치 이벤트가 전달되지 못한 상황에서 두 번째 터치 이벤트가 이어서 오면, 그때부터 5초가 지나서야 ANR이 발생한다. 앱이 멈춘 것 같아 한 번 터치를 해보면 그제서야 "응답없음" 다이얼로그가 뜬 경험이 있지 않은가?  

마지막으로 주의해야할 점이 있다. 각 메시지가 모두 5초 이내로 처리되더라도 여러 개의 메시지가 쌓여있다면, 그 뒤에 들어오는 터치 이벤트는 ANR를 발생시킨다. 앞선 메시지들이 정상적으로 처리되고 있다 하더라도 뒤에 온 터치 이벤트는 타임아웃 시간 내에 전달되지 못했기 때문이다. 그러니 총합 처리 시간을 잘 고려해야한다.  

그리고 BroadcastReceiver는 기본 타임아웃이 1분이지만 이 사이에 터치 이벤트가 전달된다면 5초 내에 터치 이벤트가 전달되지 못해 ANR이 발생한다. 따라서 BroadcastReceiver나 Service도 액티비티가 떠있는 경우에는 타임아웃을 5초라고 생각하고 설계하는 것이 좋다. 가장 좋은 방법은 BroadcastReceiver에서 오래 걸리는 작업이 있다면 Service로 넘겨서 실행하고, 서비스에서는 다시 백그라운드 스레드를 이용하는 것이다.

<br>
<br>


--- 
해당 포스팅은 [안드로이드 프로그래밍 Next Step - 노재춘 저](http://www.yes24.com/Product/Goods/41085242) 을 바탕으로 내용을 보충하여 작성되었습니다.


참고  
