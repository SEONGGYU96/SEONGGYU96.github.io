---
layout: post
title: "코루틴은 정말 스레드 보다 가벼울까?"
subtitle: "궁금하다 궁금해"
author: "GuGyu"
header-style: text
tags:
  - Kotlin
  - Coroutin
  - Thread
  - Study
  - Android
---
코루틴이 하나 새로 생성되어 실행된다는 것은 새로운 스레드를 생성한다는 의미가 아니다.  
코루틴은 스케줄링이 가능한 코드 블록 혹은 코드 블록들의 집합이라고 볼 수 있다.  
다음 그림을 살펴보자.  
![](https://miro.medium.com/max/1400/1*4m9pERI0yRScxD0iWB_yog.png)  
(출처 - [@myungpyo](https://medium.com/@myungpyo/%EC%BD%94%EB%A3%A8%ED%8B%B4-%EA%B3%B5%EC%8B%9D-%EA%B0%80%EC%9D%B4%EB%93%9C-%EC%9E%90%EC%84%B8%ED%9E%88-%EC%9D%BD%EA%B8%B0-part-1-dive-2-25b21741763a))

왼쪽 그림을 보면 `CoroutineScope`가 있다. 코루틴을 실행하기 위해서는 어떤 코루틴 스코프에 속해있어야 하는데 이 그림에서 스코프는 Ui Thread에서 수행된다.  
  
  
가운데 그림은 스코프 안에서 또 하나의 코루틴을 만든 것이다. 부모 스코프의 Context를 상속받고 Dispathcer를 `ThreadPoolDispatcher`로 재정의하였다. 따라서 해당 코루틴은 워커 스레드에서 수행된다.  
  
  
이 때, `launch {}`와 같이 빌더를 실행했을 경우에 마지막으로 넘긴 코드 블럭, 즉 실제 수행하고자 하는 로직이 담긴 코드 블록은 `Continuation`이라는 단위로 만들어진다.  
`Continuation`은 또 무엇이냐!

> _어떤 일을 수행하기 위한 일련의 함수들의 연결을 각 함수의 반환값을 이용하지 않고 `Continuation` 이라는 추가 파라미터 (`Callback`)를 두어 연결하는 방식으로 `Continuation`단위로 `dispatcher`를 변경한다거나 실행을 유예한다거나 하는 플로우 컨트롤이 용이해지는 이점이 있다._

  

`Continuation`으로 변경된 코드 블럭은 최초에 `suspend` 상태로 생성되었다가 `resume()` 요청으로 인해 `resumed`상태로 전환되어 실행 된다. `Continuation`의 재개가 요청될 때 마다 현재 Context의 `dispatcher`에게 디스패치(스레즈 전환)가 필요한지 `isDispatchNeeded()` 메서드를 이용해 확인한 후, 디스패치가 필요하면 `dispatch()`함수를 호출하여 적합한 스레드로 전달하여 수행된다.  
  

위 이미지에서는 코루틴 생성 시 `Dispatcher`를 다르게 하였지만 만약 재정의하지 않고 `UI Dispatcher`를 그대로 사용했다면 어땠을까?  
일반적인 함수 호출과 동일하게 실행된다. 이것이 바로 코루틴이 가볍다고 불리는 이유이다! 코루틴은 `Dispathcer`에 의해 실행되는 환경(Thread)이 결정될 수 있지만 그 자체로는 환경을 새로 구성한다거나 변경하지 않는다.