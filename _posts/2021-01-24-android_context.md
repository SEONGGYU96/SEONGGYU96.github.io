---
layout: post
title: "안드로이드 Context 클래스"
subtitle: "포브스 선정, 많이 사용하지만 무엇인지 모르는 클래스 1위"
author: "GuGyu"
header-style: text
tags:
  - Android
  - AndroidFramework
  - Study
  - Context
---

아무리 간단한 안드로이드 개발 예제라도 `Context` 클래스는 대부분 사용하고 있다. 즉 Context는 안드로이드 개발에 필수적인 클래스이다. Context가 없으면 액티비티를 시작할 수도, 브로드캐스트를 발생시킬 수도, 서비스를 시작할 수도 없다. 리소스(Color, String, ..)에 접근할 때도 Context를 통해서만 가능하다. 이렇게 안드로이드 개발의 중추 역할을 하고 있지만 대부분의 초보 개발자들은 Context가 정확히 무슨 역할을 하는지도 모른채 사용하고 있다. Context는 여러 컴포넌트의 상위 클래스이면서 Context를 통해 여러 컴포넌트가 연결되어 있으므로 Context 자체를 살펴보고 이해한다면, 컴포넌트를 이해하고 다루는 것에도 큰 도움이 될 것이다.  

## Context  

Context란 도대체 무엇일까? Context라는 이름은 "맥락"을 의미하는데 얼추 비슷하게 생각할 수 있다. 현재 다루고 있는 컴포넌트의 맥락, 흐름 등의 정보를 가지고 있으며 이 맥락과 흐름을 컨트롤 할 수 있는 기능도 제공한다. 하지만 직접 코드를 살펴보면 Context는 메서드 구현이 거의 없는, 상수 정의와 추상 메서드들로 이루어진 추상 클래스이다.

```java
public abstract class Context {
    ...
    public abstract Resources getResources();
    ...
    public abstract Looper getMainLooper();
    ...
    public abstract String getPackageName();
    ...
    public abstract Context getApplicationContext();
    ...
    public abstract Context getBaseContext();
    ...
    public abstract void startActivity(Intent intent);
    ...
    public abstract void sendBroadcast(Intent intent);
    ...
    public abstract ComponentName startService(Intent service);
    ...
}
```

 안드로이드 어플리케이션은 하나의 맥락으로 볼 수 있는데 이 추상 메서드들을 잘 보면 안드로이드 어플리케이션의 "맥락"과 관련이 있음을 알 수 있다. 이 어플리케이션이 가지고 있는 리소스에 대한 접근(`getResource()`), 이 어플리케이션의 id(`getPackageName()`), 액티비티, 서비스 컴포넌트의 시작, 브로드캐스트 전송 등 모두 맥락을 파악하고 컨트롤할 수 있는 것들이다. 그런데 왜 구현되어 있지 않고 추상 클래스의 형태로 정의되어있을까? 이유는 안드로이드 어플리케이션 내에서 생성되는 컴포넌트들도 그 각자의 "맥락"을 가질 수 있기 때문이다.  

액티비티는 액티비티 그 자체의 맥락을 또 가진다. 액티비티의 생명주기만 살펴봐도 그렇다. 어플리케이션은 아까부터 생성되어 쭉 살아있지만, 액티비티는 이제 막 생성되어 화면을 그리기 시작하였고, 어느순간 화면에서 사라져 소멸된다. 어플리케이션의 맥락보다는 짧은 그 자체의 맥락이 존재하는 것이다. 서비스도 마찬가지다. 따라서 Context는 추상 클래스의 형태로 존재하여 어플리케이션은 어플리케이션만의 맥락을, 액티비티와 서비스는 그 각자의 맥락을 Context를 상속하여 구현할 수 있게끔 설계되었다.  

뷰는 액티비티의 맥락 속에서 그려져야하고, 서비스는 뜬금없이 액티비티를 시작시킬 수 없듯이 컴포넌트 각자의 맥락이 존재하지만, 어플리케이션이나 액티비티나 서비스나 동일한 맥락을 갖고 있는 경우도 많다. 어플리케이션의 Id는 어느 액티비티에서나 동일하고 메인 루퍼는 어플리케이션에서 단 하나만 존재하므로 어디에서 접근하든 동일한 인스턴스에 접근한다. 각 컴포넌트의 맥락을 초월하여 어디서든 동일한 맥락이 바로 어플리케이션의 맥락, `ApplicationContext`이다. 그림이 조금 그려지는가?  

### 상위 맥락의 참조

하지만 액티비티와 서비스는 자신의 맥락 그 이상의 어플리케이션 맥락에 접근해야할 때도 있다. 예를 들어 어플리케이션 단위로 하나만 존재하는 싱글턴 객체를 생성할 때 액티비티의 `Context`를 사용하면 어떻게 될까? 액티비티가 소멸되고 그 맥락은 끝이나야하지만 싱글턴 객체는 계속 살아있고 액티비티의 Context를 참조하고 있기 때문에 가비지컬렉터는 액티비티의 맥락을 소멸시키지 못한다. 끝났으나 끝나지 않은 맥락이다. 이는 메모리 누수로 이어지기 때문에 이 경우에는 액티비티 자신의 맥락에서 싱글턴 객체를 생성할 것이 아니라 어플리케이션 맥락에서 객체를 생성해야할 것이다. 이렇게 자신의 맥락이냐, 상위 어플리케이션의 맥락이냐를 구분하여 접근하고자 할 때 `getBaseContext()`와 `getApplicationContext()` 메서드를 사용할 수 있다. `getBaseContext()`는 자신의 맥락을, `getApplication()`은 어플리케이션의 맥락에 접근한다.

### 시스템 서비스 참조
그런데 과연 어플리케이션이 최상위 맥락일까? 어플리케이션도 결국 안드로이드 OS 내에서 실행되고 소멸되는 프로세스에 불과하다. 그러나 어플리케이션 이상은 시스템 레벨이므로 여기서 다루는 `Context`의 레벨은 넘어선다. 어플리케이션은 시스템 레벨에서 관리되는 카메라, 알람, 오디오, 센서, 파워 등 다양한 구성요소에 대한 접근이 필요할 때가 있는데, 이럴 때는 `getSystemService()` 메서드를 통해 필요한 서비스 객체에 접근할 수 있다. 그리고 해당 객체에게 필요한 행위를 요구하면 시스템이 이를 적절하게 수용해준다. 서비스 객체는 컴포넌트가 생성되여 자신의 Context 객체가 최초 로딩될 때 `XXX_SERVICE`와 같은 상수명으로 매핑되어 있어 `getServiceService(Context.ALARM_SERVICE)`와 같이 매핑된 시스템 서비스를 가져다 쓸 수 있다.



### 그래서 뭘 할 수 있나?  

정리하자면 Context를 이용하여 전반적인 안드로이드 프레임워크 맥락에 해당하는 작업들을 할 수 있다.

- 앱 피키지 정보를 제공하거나 내/외부 파일, SharedPreference, 데이터베이스 등을 사용할 수 있다.
- `Activity`, `BroadcastReceiver`, `Service`와 같은 컴포넌트를 시작하거나 퍼미션을 체크할 수 있다.
- 시스템서비스에 접근하고 이용할 수 있다.

<br>

## ContextWrapper 

안드로이드 어플리케이션의 컴포넌트들이 각자의 맥락을 구현하기 위해서 안드로이드 프레임워크는 `ContextWrapper` 라는 중간 클래스를 만들어냈다. `ContextWrapper`는 Context와 컴포넌트 사이에 위치하며, 그 이름처럼 Context를 래핑(wrapping)한 생성자를 갖고 있다. 여기서 래핑하고 있는 Context는 각자의 컴포넌트의 맥락을 구현해낸 `ContextImpl` 객체이다.

```java
Context mBase;

public ContextWrapper(Context base) {
    mBase = base;
}

protected void attachBaseContext(Context base) {
    if (mBase != null) {
        throw new IllegalStateException("Base context already set");
    }
    mBase = base;
}
...

@Override
public Context getApplicationContext() {
    return mBase.getApplicationContext();
}
...

@Override
public void startActivity(Intent intent) {
    mBase.startActivity(intent);
}
...

@Override
public void sendBroadcast(Intent intent) {
    mBase.sendBroadcast(intent);
}
...
@Override
public Resource getResource() {
    return mBase.getResource();
}
...
```

`ContextWrapper`의 코드를 간략하게 살펴보면, 각자의 `ContextImpl` 인스턴스가 담겨질 `mBase` 맴버 변수를 초기화할 수 있는 생성자와 `attachBaseContext()`가 구현되어있고, 나머지 여러 메서드들은 자신의 Context의 메서드들을 그대로 다시 호출하고 있다. 따라서 각 컴포넌트에서 위와 같은 메서드들을 호출하면 자신의 맥락에 맞는 반환값을 리턴하거나, 자신의 맥락을 컨트롤 할 수 있게 된다.

생성자와 `attachBaseContext()` 메서드 모두 `mBase`를 초기화하는 역할을 하고 있으나 액티비티, 서비스, 어플리케이션을 생성할 때에는 생성자를 사용하지 않고, `attachBaseContext()` 메서드를 사용한다. 액티비티, 서비스, 어플리케이션은 모두 내부적으로 `ActivityThead`에서 컴포넌트가 시작되는데, 이때 각 컴포넌트의 `attach()` 메서드를 실행하고, `attach()` 메서드에서 또다시 `attachBaseContext()` 메서드를 호출하기 때문이다. 

<br>

## Context 다이어그램  

지금까지 이야기한 내용을 클래스 다이어그램으로 간단히 나타내면 다음과 같다.

<img src="https://user-images.githubusercontent.com/57310034/105811471-096f2200-5ff0-11eb-98f6-ce05019f00d6.png"/>  

이렇게 객체들이 `ContextImpl`을 직접 상속하지 않고 `ContextWrapper`를 통해 `ContextImpl`의 메서드를 호출하는 구조는, 객체 지향의 원칙에서 상속보다는 구성을 사용하라는 권장 사항을 따른 것이다. 이렇게 설계하면 `ContextImpl`의 변수가 노출되지 않고 `ContextWrapper`에서는 `ContextImple`의 공개 메서드만 호출하게 된다. 또한 각 컴포넌트별로 사용하는 기능을 제어하기도 단순해진다.  

<br>

## 코드에서 사용하는 Context

코드에서 Context를 사용하는 방법을 알아보자. Activity에서 사용할 때를 예로 들면 다음과 같은 방법이 있다.

- Activity 인스턴스 자신(this)
- `getBaseContext()`를 통해 가져오는 `ContextImpl` 인스턴스
- `getApplicationContext()`를 통해 가져오는 `Application` 인스턴스
    - Activity의 `getApplication()` 메서드로 가져오는 인스턴스와 동일하다.

위 세 개의 인스턴스는 모두 다르기 때문에 함부로 캐스팅해서는 안된다. 예를 들어 `getBaseContext()`로 가져온 것을 Activity로 캐스팅하면 에러가 발생한다. 

참고로 뷰에 들어있는 Context는 Activity 인스터스이다. `setContent()` 메서드에서 사용하는 `LayoutInflater`에 Activity 인스턴스가 전달되고, `View` 생성자의 `Context` 파라미터에 `Activity` 인스턴스가 전달된다.


<br>
<br>


--- 
해당 포스팅은 [안드로이드 프로그래밍 Next Step - 노재춘 저](http://www.yes24.com/Product/Goods/41085242) 을 바탕으로 내용을 보충하여 작성되었습니다.


참고  
[https://arabiannight.tistory.com/284](https://arabiannight.tistory.com/284)