---
layout: post
title: "안드로이드 프레임워크에 대해 - (1)"
subtitle: "안드로이드 아키텍처 소프트웨어 스택 분석"
author: "GuGyu"
header-style: text
tags:
  - Android
  - AndroidFramework
  - Study
---

이번 포스팅에서는 안드로이드(플랫폼) 아키텍처의 소프트웨어 스택을 살펴보고, 조금 더 깊이 있는 프레임워크 소스 활용 방법에 대해서 공부하고자 한다.  
<br>
  
# 안드로이드 아키텍처 개요

<img width="600" src="https://developer.android.com/guide/platform/images/android-stack_2x.png?hl=ko"/>  

출처 - [안드로이드 공식 문서](https://developer.android.com/guide/platform?hl=ko)  

안드로이드 플랫폼의 아키텍처를 살펴보면 위와같이 간략하게 나타낼 수 있다.

## System Applications

처음 안드로이드 기기를 사용할 때, 기본적으로 설치되어 있는 앱들이 있다. 홈, 카메라, 전화, 브라우저 등이 여기에 포함된다. 그리고 사용자가 직접 설치할 수 있는 앱들도 있다. 이 모든 앱들은 모두 이 `Application` 스택에 존재한다. 이 어플리케이션 스택은 `Java API Framework` 스택 위에서 동작하게 된다. 그런데 기본 앱들과 사용자 설치 앱들이 같은 어플리케이션 스택에 존재하지만 미묘한 차이를 느껴본 적이 없는가? 우선, 기본 앱들은 삭제할 수 없었던 경험이 있을 것이다. 요점은, 기본 앱과 사용자 설치 앱은 동일한 스택에 위치하지만 분명 차이점이 존재한다는 것이다.

선탑재된 기본 앱은 <b>시스템 권한을 사용</b>할 수 있고 <b>프로세스 우선 순위를 높일 수 있다</b>. 따라서 단말 기기의 메모리가 아무리 부족해도 전화 앱이 종료되지 않도록 우선 순위를 높혀둘 수 있다. 반면에 사용자 설치 앱은 허용된 권한을 제한적으로 사용할 수 있고 프로세스 우선 순위에 밀려 종료되지 않도록 발버둥 치고 있다.  
<br>

## Java API Framework

`Java API Framework`는 안드로이드 OS 위에서 어플리케이션의 기반이 되는 기본 구조다. 사실 안드로이드 앱 개발을 해본 사람이라면 이 스택을 알고 있다. 예를 들어 액티비티를 시작할 때 생성자로 직접 액티비티 인스턴스를 생성하고, `onCreate()`와 같은 생명주기를 직접 호출한 적이 있는가? 그리고 다국어를 지원하는 앱을 개발할 때, 각 언어와 국가에 맞는 `strings.xml`을 직접 찾아온 적도 없다. 단말 기기의 언어 설정이 변경될 때 알아서 변경되었을 것이다. 이렇듯 필요한 여러 동작을 **직접** 하지 않아도 되는 것은 어플리케이션 프레임워크가 알아서 해주기 때문이다. 우리는 결국 어플리케이션 프레임워크에서 미리 정한 규칙 위에서 아주 작은 요소들을 배치하며 앱을 만들고 있을 뿐이다. 그 외 나머지는 모두 어플리케이션 프레임워크의 몫이다.  

위 이미지를 보면 여러가지 익숙한 이름의 `Manager`가 있다. 바로 이들이 어플리케이션 프레임워크의 역할을 해주고 있는데, 예를 들어 `Activity Manager`는 액티비티를 생성해서 생명주기 메서드를 적재적소에 호출하는 역할을 하고 `Resource Manager`는 우리가 원하는 리소스를 다양한 환경 설정에 맞게 찾아주는 역할을 한다.  

대부분의 `Manager`들은 자바 코드로 작성되어있지만, 하드웨어 제어(`Telephony Manager`, `Location Manager`)나 빠른 속도(`ResourceManager`)가 필요한 것들은 `JNI (Java Native Interface)`를 연결해서 네이티브 C/C++ 코드를 사용하기도 한다.  
<br>

### 씬 클라이언트와 서버

참고로 위 이미지에 나와있는 `Activity Manager`는 클라이언트인 `ActivityManager`와 서버인 `ActivityManagerService`를 모두 포괄하는 개념이다. 앱 프로세스는 컴포넌트 맘색, 액티비티 스택 관리, 서비스 목록 유지, ANR 처리 등을 직접하지 않고 컴포넌트 실행 등 최소한의 역할만 하는 `씬 클라이언트(Thin client)`이다. 대부분의 기능들은 서버 프로세스인 `system_server`에 모두 위임된다.  

`system_server`는 여러 앱을 통합해서 관리하는 이른바 "통합 문의 채널"이다. 예를 들어 `startActivity()`를 실행하면 먼저 `system_server`에서 해당 액티비티를 찾는다. 동일한 앱(프로세스) 내의 액티비티라도 `system_server`에서 찾고, 갤러리나 카카오톡 전달하기 등 다른 앱의 액티비티를 띄울 때에도 마찬가지이다. 만약 띄우려는 액티비티가 포함된 앱(프로세스)가 아직 실행 중이 아니라면 `system_server`가 해당 앱(프로세스)를 실행시켜 액티비티 스택에 요청받은 액티비티 내용을 반영한다. 하지만 앱(프로세스) 내에서 액티비티를 "띄우는 것"은 앱(프로세스)의 몫이기 때문에 해당 앱(프로세스)에게 액티비티를 띄우라고 메시지를 보낸다.  

<img src="https://user-images.githubusercontent.com/57310034/103432237-46c5d700-4c1f-11eb-8b65-c6ca797a959b.png"/>

정리하자면, 액티비티를 포함한 모든 안드로이드 컴포넌트는 `system_server`를 거쳐서 관리되고 `system_server`에서 앱 프로세스에게 다시 메시지를 보내는 방식으로 동작한다.  
<br>

### 시스템 서비스 접근

`Activity Manager` 외에 다른 `Manager`들도 클라이언트와 서버로 이루어져있고, 서버는 시스템 서비스 형태로 존재한다. 앱에서 여기에 접근할 때는 `Context#getSystemService(Sring name)` 메서드를 사용한다. 하지만 서버는 앱 프로세스와는 별도로 `system_server` 프로세스에서 실행되므로 앱에서 시스템 서비스에 접근하려면 `Binder IPC`를 이용한 프로세스 간 통신이 필요하다.  
<br>

## Native C/C++ Libraries

`ART(Android Runtime)`, `HAL(Hardware Abstraction Layer)` 등 많은 안드로이드 핵심 구성 요소가 C/C++ 네이티브 코드를 기반으로 빌드된다. 따라서 안드로이드는 이러한 일부 네이티브 라이브러리의 기능을 앱에 제공한다. 예를 들어 `OpenGL`을 사용하여 앱에 2D 및 3D 그래픽 이미지을 그리고 조작할 수 있다. 네이티브 라이브러리에는 3가지 범주가 있다.
- `Bionic` - 커스텀 C 라이브러리(libc) : 리눅스는 PC가 모태이기 때문에 스마트 기기에 최적화되어있지 않아 안드로이드에 최적화하여 자체적으로 만든 라이브러리
- `WebKit`, `SQLite`, `OpenGL` 등 기능 라이브러리
- `Surface Manager`, `Media Framework` 등 네이티브 시스템 서비스

## Android Runtime

`Android Runtime(ART)`은 앱을 컴파일할 때 사용된다. 기존에는 `JVM`을 대체해 안드로이드 프로젝트를 위해 특별히 제작되어, 단순하고 빠른 가상 머신인 `Dalvik VM`을 사용했다. 하지만 롤리팝 부터는 `Davlik VM`의 단점을 보완하여 `ART`로 대체되었다. `ART`는 가상머신이 아닌 런타임 시 사용되는 라이브러리이다.

`CoreLibraries`는 안드로이드 프레임워크 소스에서 /system/core 경로 상에 위치한다. 커널을 wrapping 하거나 추가 기능을 제공하는역할을 한다.  
<br>

## Linux Kernel

안드로이드 커널은 리눅스 커널을 기반으로하며 불필요한 부분은 제거하고 스마트 기기에 맞춰 기능을 확장한 것이다.  
<br>

### Binder IPC

위에서도 언급된 `Binder IPC`는 프로세스 간 통신에 사용된다. 여기서 많이 혼동되는 게 `Binder IPC(Inter Process Communication)`와 `Binder RPC(Remote Procedure Call)` 두 용어인데, IPC는 하부 메커니즘이고 RPC는 IPC의 용도(리모트 콜)이다.  

<img src="https://user-images.githubusercontent.com/57310034/103433551-12a9e080-4c36-11eb-9aa7-3d11ff0bf125.png"/>  

리눅스 커널 입장에서 프로세스는 하나의 작업 단위일 뿐이고, 커널 공간에서 실행하는 작업의 데이터와 코드는 서로 공유된다. 따라서 독립된 공간을 가지는 프로세스가 다른 프로세스에게 데이터를 전달하기 위해서는 이 커널 공간을 이용해야한다. 이렇게 커널 공간을 이용해 프로세스 간 데이터 통신을 실행하는 메커니즘이 `IPC`이다.  

안드로이드는 단순히 메시지를 전달하는 것 뿐만 아니라 상대방 프로세스에 존재하는 함수(procedure)까지 호출할 수 있는데, 이를 `RPC`라고 한다. 그리고 `RPC`를 구현하기 위해 사용하는 도구가 `Binder IPC`이다. 따라서 IPC는 RPC의 용도라고 설명할 수 있는 것이다.  
<br>

### Binder Thread

앱 프로세스에는 `Binder Thread`라는 네이티브 스레드 풀이 있고, 최대 16개까지 스레드가 생성된다. 다른 프로세스에서 Binder IPC 통신을 할 때 이 스레드 풀을 통해서 접근한다. `DDMS`를 통해 살펴보면 `Binder_1`, `Binder_2`와 같은 이름의 스레드를 찾을 수 있는데 이게 바로 `Binder Thread`에 속한 것이다.
<br>

# Framework Source 뜯어보기

여기서 말하는 프레임워크 소스는 `Java API Framework` 스택에 있는 자바 소스를 말한다. 안드로이드 개발을 하면서 이건 무슨 클래스지? 하며 정의부를 들여다본 경험이 있을 것이다. 안드로이드 프레임워크 소스를 뜯어보는 것은 안드로이드의 원리를 이해하거나 새로운 기능을 추가할 때 큰 도움이 된다.  

API 레벨 15 이상 버전의 프레임워크 소스는 `Android SDK Manager`에서 `Sources for Android SDK` 를 선택해서 다운로드할 수 있다. 혹은 [cs.android.com](https://cs.android.com/) 에서 확인할 수도 있다.  

<img src="https://user-images.githubusercontent.com/57310034/103434442-ad112080-4c44-11eb-8fef-167a95e0c84a.png"/>  

[cs.android.com](https://cs.android.com/)에서 커널을 제외한 전체 소스를 살펴보면 위와 같다. 주요 디렉토리만 살펴보자.
- frameworks : 안드로이드 프레임워크. `android.*` 자바 패키지 포함
- libcore : 자바 코어 패키지 포함
- system : 안드로이드 init 프로세스
- packages : 안드로이드 기본 어플리케이션
- bionic : 안드로이드 표준 C 라이브러리
- dalvik : 달빅 가상 머신
- cts : 안드로이드 호환성 테스트 관련
- build : 빌드 시 사용  

전체 구조를 모두 이해할 필요는 없으나 전체 소스를 살펴보면서 대략적인 내부 구조를 파악하고 흐름을 이해하면 안드로이드 개발에 큰 도움이 될 것이다.  
<br>

## 프레임워크 소스 레벨에서의 검증

(나를 포함하여) 많은 개발자들이 개발을 하다 궁금한 게 있을 때, 프레임워크 소스에서 확인하지 않고 스택오버플로에 의존하는 경향이 있다. 물론 스택오버플로만큼 다양한 정보가 있는 곳은 없지만, 그게 항상 정확한 정보라는 보장은 없다. 따라서 내가 찾은 정보가 맞는지 테스트를 수행하고 프레임워크 소스로 다시 한 번 검증하는 것이 좋다.  

예를 들어 `ListView`의 아이템 레이아웃에는 `CheckBox`가 있으면 아이템 클릭이 정상적으로 동작하지 않는다. 이는 공식문서나 강의 등에서 나오지 않는 내용이라 스택 오버플로에서 검색해보면 `CheckBox`의 `android:focusable` 속성을 false로 하라고 나온다. 그렇게 하면 문제가 해결되긴 한다. 하지만 또 `ImageButton`을 추가하면 아이템 클릭이 안된다. 이때는 `android:focusable` 속성을 건들여도 해결되지 않는다. 스택오버플로에서는 다양한 Trial and error로 해결한 방법들을 찾을 수 있으나 명확한 정답을 찾기는 어렵다. 따라서 `android:focuable` 속성이 `ListView`의 `OnItemClickListener`에 정확히 어떤 영향을 주고 있을까? 해당 속성 말고도 영향을 주는 조건이 있을까? 이런 의문을 가지고 프레임워크 소스를 뜯어보는 것이 좋다.  

실제로 흐름을 따라 프레임워크 소스를 타고 들어가다보면 자식 뷰의 `hasFocusable()` 값이 `true`일 때 클릭이 동작하지 않는 것을 볼 수 있다. 다른 조건은 없다. 이렇게 코드로 직접 확인하면 `CheckBox`가 있을 때 클릭이 안되던 이유를 **명확히** 알 수 있다. 또한 `ImageButton`의 경우에는 `android:focuable`을 `false`로 하더라도, 클래스 생성자를 살펴보면 `setFocusable(true)`를 실행하고 있다. 따라서 xml 상에서 속성을 변경해도 적용되지 않았던 것이다. 너무나도 명쾌하다.  

문제의 원인을 모른 채 검색으로 해소하고 넘어간다면 이런 해결책들은 각각 별개의 팁으로만 남고 잊기도 쉽다. 프레임워크 소스 레벨에서 검증해보면 이후에 비슷한 문제를 맞닥뜨려도 어디서부터 문제를 찾으면 되는지 알 수 있다.  
<br>





--- 
해당 포스팅은 [안드로이드 프로그래밍 Next Step - 노재춘 저](http://www.yes24.com/Product/Goods/41085242) 을 바탕으로 내용을 보충하여 작성되었습니다.


참고  
Bionic :  
[https://surai.tistory.com/28](https://surai.tistory.com/28)

ART/Dalvik VM : 
[https://medium.com/@logishudson0218/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-%EC%BB%B4%ED%8C%8C%EC%9D%BC-%EB%B0%A9%EC%8B%9D-dalvikvm-art-b5d64350489f](https://medium.com/@logishudson0218/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-%EC%BB%B4%ED%8C%8C%EC%9D%BC-%EB%B0%A9%EC%8B%9D-dalvikvm-art-b5d64350489f)  

Binder IPC :
[https://sihyeon-kim.github.io/android-framework-study/2019/05/16/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-%EB%B0%94%EC%9D%B8%EB%8D%94-IPC.html](https://sihyeon-kim.github.io/android-framework-study/2019/05/16/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-%EB%B0%94%EC%9D%B8%EB%8D%94-IPC.html)
