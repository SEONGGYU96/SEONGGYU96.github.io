---
layout: post
title: "안드로이드 액티비티 (Activity) - 1"
subtitle: "생명주기와 구성(Configuration)"
author: "GuGyu"
header-style: text
tags:
  - Android
  - AndroidFramework
  - Study
  - Activity
  - LifeCycle
  - Configuration
---

액티비티는 안드로이드 4대 컴포넌트 중 하나로 앱에서 화면의 기본 단위가 된다. 따라서 가장 많이 쓰이는 컴포넌트인데 이번 포스팅에서는 액티비티에 대한 여러가지 이슈를 알아보고자 한다.  

## 들어가기 전에  

액티비티는 다른 컴포넌트와 마찬가지로 `AndroidManifest.xml`에 선언해야한다. 단순히 설정 파일에 액티비티를 추가할 뿐이지만 개수가 많아지만 관리하기 어려우므로 불필요하게 많이 만드는 것은 권장하지 않는다. 최근에는 이런 단점을 개선하고 퍼포먼스도 향상시킬 수 있는 싱글 액티비티 기법도 많이 사용되고 있다.  

액티비티에 대해 많은 입문자가 오해하고 있는 부분이 있다. 바로, 액티비티는 반드시 하나의 화면과 1:1 매칭된다는 오해이다. 액티비티 하나는 특정 레이아웃 파일과 묶여있다고 생각하기 쉬운데, 액티비티에서는 레이아웃을 지정하는 `setContentView()`를 사용하지 않아도 정상 동작한다. 때에 따라 `setContentView()`를 실행하지 않고 로직에 따라 분기해서 다른 액티비티를 띄우는 용도로 사용하기도 한다.  

마찬가지로 하나의 레이아웃 파일은 하나의 액티비티에 종속되는 것이 아니다 다른 어떠한 액티비티에도 사용될 수 있으며 프래그먼트의 레이아웃으로도 재사용될 수 있다. 또한 `inflate`되어 다이얼로그나 특정 뷰 그룹 내에 attach 될 수도 있으며 다양한 곳에서 재사용할 수 있으니 이 점을 고려해서 레이아웃 파일을 작성하도록 하자.  

<br>

## 생명주기  

액티비티의 생명주기를 정확히 이해하는 것은 중요하다. 생명주기를 이해하지 못했을 때 리소스가 반납되지 않을 수도 있고, 필요한 데이터를 읽어들이지 못할 수도 있다.  

### 액티비티 생명주기 다이어그램 

<img src="https://developer.android.com/guide/components/images/activity_lifecycle.png?hl=ko/">  
[출처 - 안드로이드 공식 문서](https://developer.android.com/guide/components/activities/activity-lifecycle?hl=ko#alc)

위 그림은 액티비티 생명주기를 설명하는 가장 기본적인 그림이다. `onCreate()` 부터 `onDestroy()` 까지 아래로 향하는 화살표의 흐름은 시간의 경과에 따른 것이다. 물론 상황에 따라 `onPause()`나 `onStop()`에서 위로 거슬러 올라가는 흐름도 있으니 하나씩 따져보자.  

#### 다른 액티비티에 의해 가려지는 경우  

현재 보여지고 있던 액티비티가 조금이라도 가려지기 시작하면 `onPause()`이나 `onStop()`까지 실행된다. 다른 액티비티에 의해서 현재 액티비티의 일부를 가리면 `onPuase()`가 실행되고 다른 액티비티에 의해서나 스마트폰의 홈 화면으로 나가 현재 액티비티가 완전히 가려지면 `onStop()`까지 실행된다. 이때 어디까지 가려졌냐에 따라 원래 액티비티를 완전히 복구할 때 호출되는 생명주기가 다르다. 일부만 가려져 `onPause()`까지만 실행됐던 경우, 원래 액티비티로 돌아올 때 `onResume()`이 호출되고 완전히 가려져 `onStop()`까지 실행됐던 경우, 원래 액티비티로 돌아올 때 `onStart()`부터 호출된다.  

#### 우선순위가 더 높은 앱을 위한 메모리 확보  

위 그림에서 위로 올라가는 좌측 화살표를 살펴보자. 우선순위가 더 높은 앱을 실행해야하는데 메모리가 부족하다면 OS는 우선순위가 비교적 낮은 앱을 언제든 종료시킬 수 있다. 이는 `onPause()` 단계 부터 해당되기 때문에 상황에 따라 `onStop()`이나 `onDestroy()`가 실행되지 않을 수 있다. 따라서 이 두 생명주기 메서드는 오버라이딩 빈도가 낮다. 주로 리소스가 자동으로 회수될 것이긴 하나 안전하게 정리하고 싶을 때 `onStop()`이나 `onDestroy()`에 안전 장치로 코드를 추가하는 용도로 사용된다.  

`onDestory()`는 아래 두 가지 경우에 실행된다.  

- 사용자가 액티비티를 완전히 닫거나(뒤로 버튼을 누른 경우) `finish()`가 호출되는 경우
- configuration 변경(ex 기기 회전, 멀티 윈도우 모드)으로 인해 시스템이 일시적으로 액티비티를 소멸시키는 경우  

<br>

## 생명주기 메서드 호출 시점  

케이스별로 생명주기 메서드가 호출되는 순서를 알아보자.  

#### 시작할 때  

onCreate -> onStart -> onResume

#### 기기 회전할 때  

onPause -> onStop -> onDestroy -> onCreate -> onStart -> onResume  

#### 다른 액티비티가 위에 뜰 때 / 잠근 버튼으로 기기 화면을 끌 때 / 홈 키를 눌렀을 때  

onPause -> onStop()

#### 백 키로 액티비티 종료  

onPause -> onStop -> onDestroy

#### 백 키로 이전 액티비티로 돌아갈 때 / 잠금 화면을 풀고 액티비티로 돌아왔을 때 / 홈화면에서 돌아왔을 때  

onRestart -> onStart -> onResume  

#### 다이얼로그 테마 액티비티나 투명 액티비티가 위에 뜰 때  

onPause  

<br>

### 액티비티 라이프타임  

액티비티의 생명주기는 3가지 라이프타임으로 구분할 수 있다.  

- 전체 라이프타임 : `onCreate()` ~ `onDestroy()`
- 가시(visible) 라이프타임 : `onStart()` ~ `onStop()`  
- 포그라운드 라이프타임 : `onResume()` ~ `onPause()`  

여기의 from ~ to는 from <= 라이프타임 <= to 관계가 아니다. 등호가 빠져있는 from < 라이프타임 < to 관계이다. 즉 `onPause()` 이전까지가 포그라운드 상태이고 `onPause()`가 실행되면서 백그라운드 상태로 간주된다. 마찬가지로 `onStop()` 직전까지는 화면이 보이긴하지만 `onStop()`이 실행되면서 순간 더 이상 보이지 않는다.  

또 하나 혼동되는 것이, `onCreate()` 메서드에서 `setContentView()`에 전달된 레이아웃은 가시 라이프타임의 시작인 `onStart()`에서 처리하는 것일까? 그렇지 않다. `onCreate()` 부터 `onResume()` 까지는 하나의 Message에서 처리하므로 `setContentView()`의 결과는 `onResume()` 이후에 보인다. `onStart()` 부터 가시 라이프타임이라는 것은 액티비티가 화면에 보이지 않다가 다시 보일 때는 여기부터 실행된다는 의미이다.  

<br>

### 액티비티 전환 시 생명주기 메서드 호출  

액티비티가 전환되면서 생명주기 메서드가 어떻게 호출되는지 알아보자.  

#### 액티비티에서 다른 액티비티를 시작할 때  

액티비티A에서 액티비티B를 시작할 때, 액티비티A는 `onPause()`와 `onStop()`을 실행할 것이고 액티비티B는 `onCreate()`와 `onStart()`, `onResume()`을 실행할 것이다. 그러나 액티비티A가 `onStop()`까지 실행한 후에 액티비티B의 `onCreate()`가 실행되는 것은 아니다.  

<img width=300 src="https://user-images.githubusercontent.com/57310034/106864040-b6dae780-670c-11eb-9fda-ae94c4e3819a.png"/>  

1. 액티비티A는 `onPause()` 메서드를 실행한다. (백그라운드로 이동)
2. 액티비티B는 `onCreate()`, `onStart()`, `onResume()` 메서드를 실행하고 포커스를 갖는다. (포그라운드로 이동)  
3. 액티비티A는 `onStop()` 메서드를 실행한다. (액티비티B가 전체 화면을 덮어서 액티비티A는 보이지 않는 상태) 액티비티B가 투명하거나 화면을 일부만 덮는 경우에는 `onStop()`을 실행하지 않는다. (액티비티A는 아직 보이기 때문)  

위와 같이 `onStop()`이 나중에 호출되는 이유는 무엇일까? 아직은 피호출 액티비티가 일부만 가리는지, 투명한지 알 수 없기 때문이다.  

평소에는 해당 순서를 크게 신경 쓸 필요는 없지만 만약 액티비티를 전환할 때 마다 해당 액티비티에서 발생한 내용을 저장하고 다음 액티비티에서 해당 값을 가져다 써야한다면, `onStop()`이 아니라 `onPause()`에서 저장해야 다음 액티비티에서 해당 정보를 정상적으로 사용할 수 있다.  

#### 포그라운드 액티비티가 닫힐 때  

액티비티A에서 액티비티B를 시작한 상태에서, 액티비티B를 닫으면 액티비티A가 다시 보인다.  

<img width=300 src="https://user-images.githubusercontent.com/57310034/106865175-2b625600-670e-11eb-908c-642084a44f92.png"/>

1. 액티비티B는 `onPause()` 메서드를 실행한다. (백그라운드로 이동)
2. 액티비티A는 `onRestart()`, `onStart()`, `onResume()` 메서드를 실행한다. (포그라운드로 이동)
3. 액티비티B는 `onStop()`, `onDestroy()` 메서드를 실행한다. (종료)

<br>

### 생명주기 메서드 사용 시 주의사항  

#### 리소스 생성 및 제거는 대칭적으로 실행하라  

`onCreate()`에서 리소스를 생성했다면 `onDestroy()`에서 제거하고, `onResume()`에서 생성했다면 `onPause()`에서 제거한다. 주로 사용되는 예는 `onResume()`에서 `registerReceiver()`를 실행하고, `onPause()`에서 `unregisterReceiver()`를 실행하는 것이다. 즉 포그라운드에 있을 때만 브로드캐스트 이벤트를 처리한다는 것이다.  

하지만 이렇게 대칭적으로 리소스를 생성/제거 하지 않으면 어떻게 될까? `onCreate()`에서 DB를 열었는데 `onPause()`에서 DB를 닫는다면, 다른 액티비티로 전환되었다가 돌아왔을 때 `onCreate()`는 호출되지 않기 때문에 DB는 계속 닫혀 있어 쿼리를 실행할 수 없다.  

#### super.onXxx() 호출 순서  

`onCreate()`, `onStart()`, `onResume()` 에서는 `super.onCreate()`, `super.onStart()`, `super.nResume()`을 가장 먼저 실행하고, `onPause()`, `onStop()`, `onDestroy()` 에서는 `super.onPasue()`, `super.onStop()`, `super.onDestroy()`를 가장 나중에 실행하는 것을 권장한다.  

```java
@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState)
    ...
}

@Override
public void onStart() {
    super.onStart()
    ...
}

@Override
public void onResume() {
    super.onResume()
    ...
}

@Override
public void onPause() {
    ...
    super.onPause()
}

@Override
public void onStop() {
    ...
    super.onStop()
}

@Override
public void onDestroy() {
    ...
    super.onDestroy()
}
```

생명주기를 시작할 때는 뭔가를 만들어내는 일이 많고, 끝날 때는 정리하는 일이 많다는 것을 생각하자. 많은 문서나 샘플에서도 이런 규칙은 없고, 여기에 맞게 작성하지도 않지만(구글에서 만든 소스도 마찬가지) [Effective Aandroid](http://orhanobut.github.io/effective-android/)에서는 이와 같은 내용을 찾아볼 수 있다.  

<img width="400" src="https://user-images.githubusercontent.com/57310034/106866763-328a6380-6710-11eb-8c50-dadbb0a9bc29.png"/>  

특히 많은 앱에서는 리소스를 생성해서 사용하고 반납 또는 제거하는 공통 로직이 많이 사용된다. 예를 들어 화면 로딩 속도를 체크한다거나 네트워크 상태에 따른 다이얼로그를 띄우는 등의 공통 로직을 갖는 앱이라면 상위 클래스로서 `BaseActivity`를 사용하는 경우가 많다. 상위 클래스의 `onResume()` 메서드에서 객체 인스턴스를 생성한다고 했을 때, `super.onResume()` 전에 해당 객체에 접근하면 NPE가 발생할 것이다. 또한 `onPause()` 메서드에서 `super.onPause()`를 호출한 다음 다른 로직을 실행하면 이미 반납된 자원을 사용하는 실수를 할 수도 있다.  

따라서 투입된 프로젝트의 상위 액티비티 클래스가 어떤 공통 기능을 구현하고 있는지 정확하게 파악하기 전에는 해당 순서의 규칙을 지키는 습관을 들이는 게 좋다.  

#### finish() 메서드를 호출한 후에는 리턴하라.  

간혹 `onCreate()`에서 유효성 여부를 체크하고 문제가 있을 때, 관련 다이얼로그나 로그만을 남긴 뒤 `finish()`를 호출하고서 곧바로 리턴하지 않는 경우가 있다. `finish()` 메서드는 리턴을 대신한 것도 아니고 리턴을 포함한 것도 아니다. 그저 액티비티를 종료하라고 Message를 보내는 것일 뿐이다.  

`startActivity()`도 마찬가지이다. 특정 조건을 만족하여 액티비티를 전환하기 위해 `startActivity()`를 호출하였다면 리턴하여 해당 메서드를 종료시켜야한다. `finish()`나 `startActivity()`를 호출한 후에도 그 아래 로직이 실행되면 에러가 발생하거나 엉뚱한 결과를 보여줄 수도 있다. 해당 호출이 메서드 끝에 위치한다면 리턴은 필요없지만 중간에 어떤 조건으로 인해 실행된다면 리턴은 반드시 필요하다.  

```java
@Override
public void Create(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    address = getIntent().getPacelableExtra(EXTRA_DATA_ADDRESS);
    if (address == null) {
        Log.d(TAG, "Address not exist.");
        finish();
        return; //리턴 필요
    }
    //리턴이 없으면 NPE 발생
    Log.d(TAG, "address = " + address.city);
    ...
}
```  

<br>

## 구성 변경  

구성(Configuration)은 컴포넌트에서 어떤 리소스를 사용할지 결정하는 조건이고, 이 조건 항목은 프레임워크에서 정해져 있다. 화면 방향(orientation)이 구성의 가장 대표적인 항목이다. 구성의 항목들은 `android.content.res.Configuration`의 멤버변수로 확인할 수 있다.  

- densityDpi, fontScale, hardKeyboardHidden, keyboard, keyboardHidden
- locale, mcc, mnc, navigation, navigationHidden, orientation
- screehHeightDP, screenLayout, screenWidthDp
- smallestScreenWidthDp, touchScreen, uiMode  

fontScale과 locale은 단말의 환경 설정에서 정할 수 있는 사용자 옵션이고 나머지는 단말의 현재 상태이다.  

### 리소스 반영  

구성은 컴포넌트에서 사용하는 리소스를 결정하기 때문에 구성이 변경되면 컴포넌트에서 사용하는 리소스도 변경된다. 단말의 언어를 변경하면 해당 언어의 `strings.xml`의 문자열을 보여주어야하는데, 화면에서 하나씩 문자열을 찾아서 변경하는 게 아니라 액티비티를 재시작해서 변경된 리소스를 사용한다. 화면 회전도 마찬가지다. 화면 회전에 따라 `/res/layout-port`와 `/res/layout-land` 디렉터리의 레이아웃을 교체하려면 액티비티를 재시작한다. 참고로 액티비티 외에 다른 컴포넌트는 구성이 변경되어도 재시작하지 않는다.  

구성이 변경되어 액티비티를 재시작하면 하나의 인스턴스를 가지고 새로 초기화해서 재사용하는 것이 아니라 기존 인스턴스를 `onDestroy()`까지 실행하고 완전히 새로운 인스턴스를 생성해 `onCreate()` 부터 시작한다.  

#### 메모리 누수 가능성  

액티비티가 재시작하면서 `onDestory()`가 실행되었지만 기존 액티비티 인스턴스에 대한 참조가 남아있다면 GC되지 않고 메모리를 계속 차지한다. 화면을 회전할 때 자꾸 `OutOfMemoryError`가 발생한다면 원인은 메모리 누수 때문이다.  

**액티비티의 내부 클래스나 익명 클래스 인스턴스**  

액티비티의 내부 클래스나 익명 클래스의 인스턴스가 액티비티에 대한 참조를 갖고 있다면, 이들 인스턴스를 외부에 리스너로 등록한 경우에 해제도 반드시 되어야 한다. 내부 클래스에서 `SomActivity.this`를 쓸 수 있는 상황이면 액티비티에 대한 참조를 갖고 있는 것이다. 이때 단순 내부 클래스라면 정적 내부 클래스를 만들어 생성자에 `WeekReference`로 액티비티를 전달하는 방법을 사용하여 문제를 예방할 수도 있다.  

<br>

**싱글턴에서 액티비티 참조**

싱글턴에 `Context`가 전달되어야 하는데 `Activity` 자신을 전달한 경우에 메모리 누수가 발생할 수 있다. 대응 패턴은 다음에 제대로 다뤄본다.  

<br>

### 구성 업데이트 호출 스택  

구성이 변경되면 `system_server`에서 동작하는 `ActivityManagerService`에서 앱 프로세스의 메인 클래스인 `ActivityThread`에 새로운 `Configuration`을 전달한다. 결과적으로 하는 일은 `AssertManager`의 네이티브 메서드(C로 작성된)인 `setConfiguration()`을 실행하는 것이다. 해당 네이티브에서는 리소스 테이블을 유지하고 있는데, 현재 `Configuration`에 맞는 리소스를 선택해서 가져온다.  

<img src="https://developer.android.com/images/resources/res-selection-flowchart.png/">

리소스 선택 로직은 내부적으로 최적화되어 있다. 예를 들어 현재 사용 가능한 리소스는 다음과 같다고 하자.  

```
drawable/  
drawable-en/  
drawable-fr-rCA/  
drawable-en-port/  
drawable-en-notouch-12key/  
drawable-port-ldpi/  
drawable-port-notouch-12key/  
```

그리고 현재 단말의 구성은 다음과 같다.  

```
Locale = en-GB
Screen orientation = port
Screen pixel density = hdpi
Touchscreen type = notouch
Primary text input method = 12key
```

그럼 구성을 업데이트 할 때 사용가능한 리소스 중에서 현재 구성에 맞는 리소스를 선택해야 한다. 우선은 `drawable-fr-rCA/` 리소스가 배제된다. 현재 구성은 `en-GB` 이기 때문에 `fr` 한정자를 가진 리소스는 선택되지 않기 때문이다. 

이 과정을 반복해 그 다음 우선순위를 가지는 한정자를 비교해 현재 구성과 맞지 않는 리소스를 계속 배제해 나간다. 그러다보면 하나의 리소스 디렉터리만 남게 된다.  

또한 한정자 비교 뿐만 아니라 시스템 자체적으로 구성을 확인하고 절대 선택될 일이 없는 리소스는 리소스 테이블에 올리지도 않는 등 추가적인 과정이 존재한다.

<br>

### 구성 한정자  

리소스 디렉터리명을 구성하는 구성 한정자를 우선순위대로 나열하면 다음과 같다.  


|구성 한정자|샘플|Configuration 필드|
|:--|:--|:--|
|MCC 및 MNC|mcc310, mcc310-mcc004|mcc, mnc|
|언어 및 지역|en, fr, en-rUS, fr-rFR|locale|
|레이아웃 방향|ldrtl, ldltr|locale|
|가장 짧은 너비|sw320dp, sw600dp, sw720dp|smallestScreenWidthDp|
|이용 가능한 너비|w720dp, w1024dp|screenWidthDp|
|이용 가능한 높이|h720dp, h1024dp|screenHeightDp|
|화면 크기|small, normal, large, xlarge|screenLayout|
|화면 비율|long, notlong|screenLayout|
|화면 방향|port, land|orientation|
|야간 모드|night, notnight|uiMode|
|화면 픽셀 밀도(dpi)|ldpi, mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi, nodpi, tvdpi|densityDpi|  

- 플랫폼 버전도 리소스 선택에 영향을 주지만 `Configuration` 멤버 변수에는 플랫폼 버전 값이 없다. 이는 숨겨진 멤버 변수인 `Build.VERSION.RESOURCES_SDK_INT`에 상수로 되어 있다.  
- `Configuration`의 멤버 변수 가운데서 `fontScale`은 구성 한정자와 관련된 것이 없다. 즉 `fontScale`은 리소스 선택 로직에는 영향을 주지 않고 액티비티를 재시작할 때 화면에서 `sp` 단위로 된 문자열의 크기를 변경할 뿐이다.  
- 언어 설정을 아랍어, 히브리어, 또는 페르시아어로 변경하면 RTL(right-to-left)로 레이아웃 방향이 변경된다. (`AndroidMenifest.xml`에서 `supportsRtl` 속성이 true이고, `targetSdkVersion`이 17 이상일 때)  

<br>

### 데이터 복구  

구성 변경으로 액티비티가 재시작되어돋 기본에 보던 화면과 입력한 내용 등을 그대로 유지하는 게 당연히 좋다. 이때 상태를 임시 저장하고 복구하는 메서드인 `onSavedInstanceState()`와 `onRestoreInstanceState()`를 사용할 수 있다.  

`onSavedInstanceState()` 메서드는 구성이 변경되거어 재시작할 때나, 메모리 문제로 시스템이 액티비티를 강제로 종료하는 경우에 호출되는 콜백이다. 이때 데이터들을 파라미터로 주어진 `Bundle` 객체에 저장할 수 있다.  

`onRestoreInstanceState()` 메서드는 액티비티가 재시작 될 때, 저장된 `Bundle` 데이터가 있다면 호출된다. `onStart()`이후에 호출되며, 만약 저장된 데이터가 없다면 호출되지 않기 때문에 null 체크를 하지 않아도 된다. 저장된 `Bundle`객체는 `onCreate()`에도 전달되지만, `onSavedInstanceState()`와의 대칭을 맞추기 위해 `onRestoreInstanceSate()`에서 복구 작업을 진행하곤 한다.  

하지만 위 방법은 `EditText` 내 텍스트 또는 `RecyclerView`의 스크롤 위치와 뷰에 대한 임시 정보 등 가볍고 간단한 데이터를 저장하는 데에만 쓰기를 권장한다. 객체 따위를 저장하기 위해서는 직렬화 과정이 필요하고, 데이터를 복구 할 때에도 역직렬화를 해야하므로 많은 리소스를 소비한다. 따라서 복잡한 데이터의 경우에는 `ViewModel`의 사용을 고려하자.  

그런데 만약 액티비티A에서 액티비티B로 액티비티를 전환하고서 화면을 회전하면 2개의 액티비티에서 한꺼번에 `onSaveInstanceState()`가 호출될까? 그렇지 않다. 화면을 회전하는 그 순간에는 현재 포그라운드에 나와있는 액티비티의 `onSaveInstanceState()`만 호출된다.

액티비티A에서 액티비티B로 전환하면서 액티비티A는 `onStop()`이 호출되는데, `onStop()` 이전에 `onSaveInstanceState()`가 호출된다. 즉 액티비티B가 포그라운드에 있을 때는 액티비티B의 `onSaveInstanceState()`가 호출된 상태이다.  

게다가 액티비티A는 가시 상태가 아니기 때문에 화면이 회전한다고 해서 함께 재시작되지 않는다. 회전된 상태로 백키를 눌러 액티비티A로 돌아가면 `onSaveInstance()`가 이미 호출된 상태로 그제서야 재시작한다.  

반면에 다이얼로그 액티비티를 사용하는 경우에는 다이얼로그가 뜰 때 액티비티의 `onSaveInstanceState()`가 실행된다. 액티비티가 아직 배경으로 보이기 때문에 화면을 회전하면 다이얼로그 액티비티의 `onSaveInstanceState()`를 호출 후 재시작한 다음, 그 이후에 액티비티를 재시작한다.  

<br>

#### android:screenOrientation   

해당 속성을 `portrait`나 `landscape`로 고정하면 회전해도 화면은 그대로이고 재시작하지 않는다. 게임 앱 처럼 용도가 확실한 요구사항이 있다면 화면 방향을 고정해서 화면 회전 시에도 구성이 애초에 변경되지 않도록 설정할 수 있다.  

<br>

### android:configChanges 속성  

구성이 변경되어도 필요에 따라 액티비티를 재시작하지 않을 수도 있다. 구성 변경 가운데서 가장 빈번하게 발생하는 것은 화면 회전인데, 매번 액티비티를 재시작해서 상태를 저장/복구하는 것이 번거롭거나 메모리 사용량 및 속도 측면에서 불필요하다고 판단하면 재시작을 하지 않는 게 의미가 있다. 이때는 `AndroidMenifest.xml`의 액티비티 선언에 속성을 지정할 수 있다.  

다만 액티비티의 `onConfigurationChanged()` 메서드가 호출되는데 이때 원하는 작업을 직접 처리해줄 수 있다. 즉 구성 변경으로 인한 처리를 시스템에게 맡기지 않고 프로그래머가 직접 처리하겠다는 의미이다.  

`android:configChanges` 속성에 들어갈 수 있는 값은 다음과 같다. ([2020년 9월 8일 기준](https://developer.android.com/guide/topics/manifest/activity-element))

- mcc, nnc, locale, touchscreen, keyboard, keyboardHidden
- navigation, orientation, screenLayout, uiMode, screenSize, smallestScreenSize
- layoutDirection, fontScale, colorMode, density

위 속성 값들을 `| (or)` 연산자를 이용해 여러가지를 동시에 적용할 수 있다.  

<br>

### onConfigurationChanged()  

위에서도 언급했지만 `android:configChanges` 속성을 사용하는 것은 해당 항목의 구성이 변경될 때 직접 처리하겠다는 의미이다. 따라서 `onConfigurationChanged()`가 불린 이후에는 화면을 다시 그린다. `onConfigurationChanged()`를 오버라이딩하지 않아도 해당 메서드가 호출된 다음에야 화면이 다시 그려진다. `android:configChanges`에 항목들을 넣어서 활용하는 방법들을 알아보자.  

#### 화면 회전 대응  

`ConstaraintLayout` 등을 사용하여 레이아웃을 그나마 반응형으로 만들어뒀다면, 별도의 처리를 하지 않아도 화면이 회전함에 따라 뷰가 다시 그려지면서 너비와 높이가 새롭게 계산된다. 따라서 전체 레이아웃이 바뀌는 것이 아니라면 별도의 xml 파일을 만들 필요가 없다. 하지만 가로 모드에서 특별히 조금 더 여유롭게 너비를 갖고싶은 뷰가 있다면 `/res/values-port` 와 `/res/values-land` 디렉터리 각각의 `dimens.xml`에 동일한 name으로 값을 넣고 레이아웃에서 name을 참조하면 된다.  

그러나 `android:configChanges` 속성에 `orientation`을 적용하면 화면을 회전해도 액티비티를 재시작하지 않는다. 따라서 이 경우에는 `onConfigurationChanged()` 메서드를 오버라이드해서 크기를 직접 변경해주어야한다.  

```java
private View left;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.view_list);
    left = findViewById(R.id.left);
}

@Override
public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    ViewGroup.LayoutParams lp = left.getLayoutParams();
    //변경된 구성에 따른 리소스를 참조
    lp.width = getResource().getDimensionPixelSize(R.dimen.left.width);
    left.setLayoutParams(lp);
}
```

위 코드에서 사용된 `getResource().getXXX()` 메서드는 변경된 Configuration에 대응하는 값을 가져오기 때문에 화면 회전에 따라 그에 맞는 `dimen.xml`의 값을 사용한다.  

그런데 `onConfigurationChanged()`가 호출된 다음에는 액티비티가 재시작 되지 않더라도 화면은 새로 그린다. 그럼 굳이 변경된 구성에 따른 리소스를 참조하는 코드를 작성할 필요가 있을까?  

결론부터 말하자면 액티비티를 재시작하지 않는다면 변경된 구성에 따른 리소스를 참조하여 뷰의 설정을 변경하는 코드를 작성할 필요가 있다. 왜냐하면 `setContentView()`에서 내부적으로 사용하는 `LayoutInflater`의 `inflate()` 메서드에서 뷰의 크기를 모두 반영한다. 즉, 이때 이미 android 네임스페이스에 있는 값들(`android:layout_width` 등)은 대입이 되었고, Configuration이 변경된다고 해서 다시 대입되지 않는다. 이 때문에 `onConfigurationChanged()`에서 변경된 값을 직접 대입해주어야 하는 것이다.  

<br>

#### 폰트 크기 변경 대응  

폰트 크기의 단위로 sp를 쓰지 않는다면 `android:configChanges`에 `fontScale`을 추가하여 불필요한 액티비티 재시작을 방지할 수 있다. UI가 단순한 경우에는 권장대로 sp를 써도 되지만 UI가 복잡한 경우, 디자인 문제로 dp를 사용하는 경우가 많다. dp만 사용한다면 환경 설정에서 글꼴 크기를 변경해도 화면에 영향이 없으므로 재시작할 필요가 없다.  

<br>

#### 로케일 변경 대응  

앱에서 다국어를 대응하지 않는다면 `locale`을 추가하는 것도 좋은 방법이다. 그리고 만약 일부만 다국어 대응을 한다면 `onConfigurationChanged()`메서드를 활용하는 것을 고려해보자. 예를 들어 화면에서 제목만 다국어 대응을 한다면, `onConfigurationChanged()` 메서드에서 `titleText.setText(R.string.title)`과 같이 변경된 언어로 문자열을 적용할 수 있다.  

<br>

### 두 개 이상의 구성 변경  

`android:configChanges` 에 여러 개의 구성을 OR 연산자를 이용해 적용할 수 있다고 했다. 여기서 주의할 점은, 변경된 구성이 모두 여기에 적용되어 있어야 액티비티를 재시작하지 않는다는 것이다.  

예를 들어 환경 설정에서 언어를 바꾸고 화면을 회전하고서 액티비티가 포그라운드로 돌아오면 `locale`과 `orientation`이 한꺼번에 바뀐다. 이때 `android:configChanges`에 `orientation`만 있고 `locale`이 포함되지 않는다면 액티비티는 재시작한다.  

이 때문에 항상 함께 붙어다녀야만 하는 항목들이 생겼다. `android:configChanges`에 가장 보편적으로 들어가는 게 `orientation`인데 API 13레벨 이상에서는 화면을 회전할 때 화면 크기도 같이 변경된다. 따라서 화면 회전을 정상적으로 처리하고자 한다면 `screenSize`도 함께 포함해서 `orientation|screenSize`를 적용해야한다.  

참고로 Activity에는 `setRequestOrientation(int requestOrientation)` 메서드가 있다. 방향 센서를 통해서 화면을 회전하는 것이 아니라, 화면을 특정 방향으로 변경해서 보여주는 메서드이다. 이 메서드를 호출하면 기본적으로 액티비티가 재시작되는데 이때도 `android:configChanges`에 `orientation` 항목이 있다면 액티비티를 재시작하지 않고 `onConfigurationChanged()`가 불린다.  

아무쪼록 `android:configChanges`에 값을 넣을 때는 '최대'보다는 '최소'를 원칙으로 하는 게 좋다. 여기에 적용할 수 있는 14개의 구성을 모두 넣는다고 해서 액티비티가 재시작되는 것을 방지할 수 있는 것도 아니다. 액티비티가 재시작되는 이유는 이 외에도 많기 때문에 완벽하게 대응할 수 없다. `mcc`나 `mnc`와 같은 옵션은 유심 카드가 새로 발견된 경우를 말하는데 이처럼 굳이 대응할 필요가 없는 경우도 있다. 유심이 바뀐다면 단말 재부팅하기도 바쁜데 앱에서 액티비티가 재시작되는지 신경 쓸 유저는 없을 것이다.  

<br>

### Configuration 클래스의 변수 확인  

마지막으로 `Configuration` 클래스의 변수를 확인하고, Configuration이 변경되는 타이밍에 대해서 더 알아보자. Context 인트선스에서 `getResource().getConfiguration()`으로 Configuration을 가져와서 `toString()`메서드로 출력해보면 다음과 같다.  

```
{0.9 450mcc8mnc [ko_KR,en_US] ldltr sw411dp w411dp h773dp 420dpi nrml long hdr port finger -keyb/v/h -nav/h winConfig={ mBounds=Rect(0, 0 - 1080, 2220) mAppBounds=Rect(0, 0 - 1080, 2094) mWindowingMode=fullscreen mDisplayWindowingMode=fullscreen mActivityType=standard mAlwaysOnTop=undefined mRotation=ROTATION_0} s.1 desktop/d dc/d bts=0 ff=0 bf=0 themeSeq=0}
```

위 내용은 기본적으로 공백으로 구분된 14개의 구성을 나타내는데, 단말기기마다 커스텀 데이터가 더 나오기도 한다.  

- 첫 번째 `0.9`는 fontScale 값이다.
- 두 번째 `450mcc8mnc`는 mcc, mnc 값이다. 450은 한국 국가 코드이고 뒤에 8은 통신사 코드이다. (KT)
- 세 번째 `[ko_KR,en_US]`은 locale 값이다.
- 네 번째 `ldltr`은 screenLayout 값이다. (left-to_right)
- 다섯 번째 `sw411dp`는 smallestScreenWidthDp 값이다. 화면을 회전해도 이 값은 변하지 않는다.  
- 여섯 번째와 일곱 번째 `w411dp`, `h773dp`는 screenWidthDp와 screenHeightDp이다. 화면 회전 시에 너비와 높이가 서로 바뀌기만 할 것 같지만 상단의 상태바와 소프트 키 영역 때문에 차이가 생긴다.  
- 여덟 번째 `420dpi` 는 densityDpi 값이다.
- 아홉 번째 `nrml`은 screenLayout 값이다.
- 열세 번째 `finger`는 touchscreen 값이다.  
- 열네 번째 `-keyb/v/h`는 keyboard, keyboardHidden, hardKeyboardHidden 값이다. v나 h는 visible/hidden을 의미한다. -keyb는 키보드가 없다는 의미다.  
- 열다섯 번째 `-nav/h`는 navigation과 navigationHidden 값이다. 마이너스가 있으므로 navigation 타입이 없다는 의미다.
- 열일곱 번째 `s.1`은 seq 으로, 변경 횟수를 나타낸다. 구성이 바뀔 때 마다 값이 증가한다.

위에서 건너뛰거나 작성하지 않은 내용은 커스텀 구성이다.  

<br>

### 포그라운드 액티비티 기준의 구성 변경  

화면 방향과 관련해서 구성이 바뀌는 것은 포그라운드에 있는 액티비티가 기준이다. 만약 화면 고정인 액티비티가 포그라운드에 있다면 아무리 화면을 회전해도 `onConfigurationChanged()`조차도 호출되지 않는다. 그러나 만약 가로 모드 고정인 액티비티에서 화면 고정을 하지 않은 액티비티로 전환을 하고, 그때 단말기를 가로로 돌려두었다면 orientation 이 `ORIENTATION_LANDSCAPE` 로 변함이 없으므로 seq가 늘어나지 않는다. 하지만 세로로 다시 기울이면 seq는 N+1 이 되고, 이 상태로 다시 백 키를 눌러 가로 고정인 액티비티로 돌아간다면 또 seq가 증가한다. 그리고 seq가 변경될 때마다 `onConfigurationChanged()`가 호출된다.  

<br>

### Configuration의 유일성  

`Configuration`은 한순간에 1개의 값만 존재한다. 예를 들어 액티비티A위에 다이얼로그 테마 액티비티인 액티비티B가 있다고 하자. 이때 액티비티A는 가로로 고정하고 액티비티 B를 세로로 지정하면 어떻게 될까? A만 있을 때는 가로 고정으로 잘 보였지만 B를 시작하면 배경에 있는 A까지 세로로 바뀐다. 이런 특별한 경우에는 A입장에서는 방향을 고정한 게 의미가 없다.  

그리고 만약 A에서 `android:configChanges`에 `orientation`을 추가해서 방향 회전에 대응했다면 `onConfigurationChanged()`까지도 호출된다. 따라서 고정된 화면 방향이 바뀔 수도 있는 특이한 케이스도 있다는 점을 염두에 두어야 한다.  


<br>
<br>


--- 
해당 포스팅은 [안드로이드 프로그래밍 Next Step - 노재춘 저](http://www.yes24.com/Product/Goods/41085242) 을 바탕으로 내용을 보충하여 작성되었습니다.

참고 :  
[provides-resources](https://developer.android.com/guide/topics/resources/providing-resources#BestMatch)  

[runtime-change](https://developer.android.com/guide/topics/resources/runtime-changes)