---
layout: post
title: "CoroutinContext와 CoroutineScope"
subtitle: "Coroutine 넌 누구냐"
author: "GuGyu"
header-style: text
tags:
  - Kotlin
  - Coroutin
  - Study
  - Android
---
도대체 코루틴은 정확히 어떤 역할을 하고 있을까? 코루틴 공식 가이드를 읽고 분석한 블로그가 있어 정리하며 이해해보았다.  
[참조 링크](https://medium.com/@myungpyo/reading-coroutine-official-guide-thoroughly-part-1-7ebb70a51910)  
  

## CoroutinContext와 CoroutineScope

코루틴을 이해하기 위해서는 우선 코루틴을 구성하는 두 가지 주된 요소인 `CoroutineContext`와 `CoroutineScope`에 대해 먼저 이해하여야 한다.  
`CoroutineContext`의 구현 코드를 살펴보면 4가지 메서드를 가지고 있음을 알 수 있다.  
  

-   `public operator fun <E : Element> get(key: Key<E>): E?`
    
    -   주어진 key에 해당하는 Context 요소를 반환하는 연산자 함수
-   `public fun <R> fold(initial: R, operation: (R, Element) -> R): R`
    
    -   초기 값을 시작으로 제공된 병합 함수를 이용하여 대상 Context 요소들을 병함한 후 결과로 반환.
    -   예를 들어 초기값을 0으로 하고 특정 Context 요소들만 찾는 병합 함수 (filter역할)를 넘겨주면 찾은 개수를 반환할 수 있다.
    -   혹은 초기값으로 EmptyCoroutineContext 를 넘겨주고 특정 Context 요소들만 찾아 추가하는 함수를 넘겨주면 해당 요소들만으로 구성된 코루틴 Context를 만들 수 있다.
-   `public operator fun plus(context: CoroutineContext): CoroutineContext = ...impl...`
    
    -   현재 Context와 파라미터로 주어진 다른 Context가 갖는 요소들을 모두 포함하는 Context를 반환한다.
    -   중복되는 요소는 버려진다.
-   `public fun minusKey(key: Key<*>): CoroutineContext`
    
    -   현재 Context에서 주어진 키를 갖는 요소들을 제외한 새로운 Context를 반환한다.

여기서 계속 언급하는 key는 어떤 객체일까?  
Key에 대한 인터페이스 정의 또한 포함되어 있다.  
  
  
`public interface Key<E : Element>`  
Key는 Element 타입을 제네릭 타입으로 가져야 한다. Element 타입은 또 무엇일까!  
  
  
`public interface Element : CoroutineContext`  
Element는 `CoroutineContext`를 상속하며 앞서 언급한 key 를 멤버 속성으로 갖는다.  
`CoroutineContext`를 구성하는 Element 들의 예시는 다음과 같다.

-   `CoroutineId`
-   `CoroutineName`
-   `CoroutineDispatcher`
-   `ContinuationInterceptor`
-   `CoroutineExceptionHandler`
-   등등

이런 요소(element)들은 각각의 key를 기반으로 CoroutineContext에 등록된다.  
  
  
요약하자면,

> `CoroutineContext`에는 코루틴 Context를 상속한 요소(Element) 들이 등록될 수 있고, 각 요소들이 등록될 때는 요소의 고유한 키를 기반으로 등록된다

하지만 `CotoutineContext`는 인터페이스이다.  
이를 구현한 구현제는 다음과 같다.

-   `EmptyCoroutineContext`: 특별히 Context가 명시되지 않을 경우 해당 객체가 싱글턴으로 사용된다.
-   `CombinedContext`: 두 개 이상의 Context가 명시되면 Context 간 연결을 위한 컨테이너 역할을 하는 Context.
-   `Element`: Context의 각 요소들도 `CoroutineContext`를 구현한다.

잘 이해가 가지 않는다!  
다음 그림을 살펴보자.  
![](https://miro.medium.com/max/1400/1*K9Ky5pV6CMvaULvaxenqIQ.png)

위 이미지는 우리가 `GlobalScope.launch{}`를 수행할 때 `launch` 함수의 첫 번째 파라미터인 `CoroutineContext`에 어떤 값을 넘기는지에 따라 변해가는 `CoroutineContext`의 상태를 보여준다.  
  
  
보다시피 각각의 요소를 '+' 연산자를 이용해 연결하고 있다. 이는 `CoroutineContext`가 plus 연산자를 구현하고 있기 때문에 가능하다. Element + Element + ... 는 결국 하나로 병합된 `CoroutineContext`를 만들어낸다.  
  
  
`CoroutineContext`와 Element를 묶어 하나의 `CoroutineContext`를 만들고 이는 곧 `CombinedContext`가 된다. 그리고 `ContinuationIntercepter`는 이 병합 작업이 일어날 때 항상 마지막에 위치하도록 고정된다. 이로써 인터셉터로 빠르게 접근할 수 있겠다.  
  
  
`CoroutineContext`에 대해서는 어느정도 감이 잡힌 것 같다. 그렇다면 `CoroutineScope`는 무엇일까? 또 뜯어보자.

```java
public interface CoroutineScope {
    /**
     * Context of this scope.
     */
    public val coroutineContext: CoroutineContext
}
```

`CoroutineScope`는 `CoroutineContext` 하나만을 멤버 속성으로 정의하고 있는 인터페이스이다.  
코루틴을 사용하면서 자주 보았던 `launch`, `async`, `coroutineScope`, `withContext`, 즉 코루틴 빌더들은 이 `CoroutineScope`의 확장 함수로 정의되어 있다.  
그러니까 이 빌더들은 `CoroutineScope`의 함수들인 것이고 코루틴을 생성할 때 소속된 `CoroutineScope`에 정의된 `CoroutineContext`를 기반으로 필요한 코루틴을 생성하는 것이다!  
  
  
글로만 보면 이해가 어려우니 예제를 살펴보자.  
  

```java
class MyActivity : AppCompatActivity(), CoroutineScope {
  lateinit var job: Job
  override val coroutineContext: CoroutineContext
  get() = Dispatchers.Main + job

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    job = Job()
  }

  override fun onDestroy() {
    super.onDestroy()
    job.cancel() // Cancel job on activity destroy. After destroy all children jobs will be cancelled automatically
  }

 /*
  * Note how coroutine builders are scoped: if activity is destroyed or any of the launched coroutines
  * in this method throws an exception, then all nested coroutines are cancelled.
  */
  fun loadDataFromUI() = launch { // <- extension on current activity, launched in the main thread
    val ioData = async(Dispatchers.IO) { // <- extension on launch scope, launched in IO dispatcher
      // blocking I/O operation
    }

    // do something else concurrently with I/O
    val data = ioData.await() // wait for result of I/O
    draw(data) // can draw in the main thread
  }
}
```

`MainActivity`가 `CoroutineScope`인터페이스를 상속하고 있고 `CoroutineContext` 멤서 속성을 구현하였다.  
여기서는 `CoroutineScope`의 `CoroutineContext`를 `Dispatcher.Main + job`으로 정의함으로써 `Activity`에서 생성되는 코루틴은 메인스레드로 디스패치 되고, 액티비티에서 정의한 `job`객체를 부모로 가지는 `Job`들을 생성하여 액티비티 `Job`과 운명을 같이 하게 된다.  
  
  
모르겠다. 무슨 말인지.. 일단 다 치워놓고 그럼 `GlobalScope.launch{}`가 뭔지 먼저 알아보자.  
`CoroutineScope`는 계속 설명한 것과 같이 그저 인터페이스일 뿐이다. 실제 구현은 위의 예처럼 Activitiy와 같이 생명주기를 갖는 오브젝트에서 구현하여 사용자 정의 스코프를 만들 수도 있지만 편의를 위해 코루틴 프레임워크에 미리 정의된 스코프들도 있다. 그 중 하나가 `GlobalScope`인 것이다. 이 스코프의 구현은 다음과 같이 되어있다.

```java
/ -- in CoroutineScope.kt
object GlobalScope : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = EmptyCoroutineContext
}

// -- in CoroutineContextImpl.kt
@SinceKotlin("1.3")
public object EmptyCoroutineContext : CoroutineContext, Serializable {
    private const val serialVersionUID: Long = 0
    private fun readResolve(): Any = EmptyCoroutineContext

    public override fun <E : Element> get(key: Key<E>): E? = null
    public override fun <R> fold(initial: R, operation: (R, Element) -> R): R = initial
    public override fun plus(context: CoroutineContext): CoroutineContext = context
    public override fun minusKey(key: Key<*>): CoroutineContext = this
    public override fun hashCode(): Int = 0
    public override fun toString(): String = "EmptyCoroutineContext"
}
```

우선은 싱글턴으로 구현되어 있고 `EmptyCoroutineContext`를 그 Context로 가지고 있다. 이 Context는 `CoroutineContext` 멤버 함수들에 대해서 기본 구현만 정의한 Context이다. 이 기본 Context는 어떤 생명주기에 바인딩 된 Job이 정의되어 있지 않기 때문에 어플리케이션 프로세스와 동일한 생명주기를 갖게 된다.

즉 정리하자면

> `GlobalScope.launch{}`로 실행한 코루틴은 어플리케이션이 종료되지 않는 한 필요한 만큼 실행을 계속해 나간다.