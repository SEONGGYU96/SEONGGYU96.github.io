---
layout: post
title: "Spring5 : AOP 프로그래밍 (1)"
subtitle: "Spring5 study (7)"
author: "GuGyu"
header-style: text
tags:
  - Spring
  - Spring5
  - Java
  - Study
  - Server
  - AOP
---

## Proxy 패턴

AOP를 알아보기에 앞서, Proxy 패턴에 대해 먼저 알아야한다.  

Proxy 패턴은 어떤 객체에 대한 접근을 제어하거나 공통적인 기능 확장을 용도로 Proxy 객체를 만들어서 클라이언트가 실제 대상 객체를 간접적으로 이용할 수 있게 작성하는 디자인 패턴이다. 대상 객체가 민감한 정보를 가지고 있거나 접근하기 위해 많은 시스템 리소스나 시간을 필요로 하는 경우에 많이 사용된다. 프록시 객체는 클라이언트와 대상 객체 사이에서 대상 객체의 자원을 캐싱하거나 흐름을 제어하고, 부가적인 기능을 확장할 수 있다.  
<br>

<img src="https://user-images.githubusercontent.com/57310034/104413031-de78dd00-55b0-11eb-99d0-eb103488e03e.png"/>

위 처럼 프록시 객체와 실제 대상 객체는 동일한 인터페이스를 구현하고 있기 때문에 클라이언트의 입장에서는 어떤 객체와 상호작용하고 있는지 모른다. 다만 프록시 객체는 실제 기능을 수행하는 역할을 하지 않고 민감하거나 유효하지 않은 요청에 대한 흐름을 제거하거나, 핵심 기능을 수행하기 전이나 후에 부가적으로 수행할 기능들을 구현하고 있다. 핵심 기능은 실제 대상 객체에게 요청해 받아오는데, 프록시 객체는 이 값을 절대로 수정해서는 안된다.   
<br>


<img src="https://user-images.githubusercontent.com/57310034/104413720-2fd59c00-55b2-11eb-9858-613eb6d39172.png"/>

따라서 프록시 패턴의 실행 흐름은 위와 같다.  

프록시 객체와 대상 객체는 동일한 인터페이스를 구현하고 있으므로 대상 객체를 다른 객체로 바꾸더라도 프록시는 동일한 부가 기능을 수행한다. 즉 어떠한 핵심 기능을 수행하는 객체들에 대해 프록시 공통적으로 동일한 기능을 확장하는 역할을 한다. 이 공통적인 확장 기능을 수정하기 위해서는 모든 대상 객체를 수정하는 것이 아니라 프록시 객체만 수정하면 된다.  


## AOP 란

AOP는 Aspect Oriented Programming의 약자로, 여러 객체에 공통적으로 적용할 수 있는 기능을 분리해서 재사용성을 높여주는 프로그래밍 기법을 말한다. 이렇게 분리한 <b>핵심 기능에 공통 기능을 삽입</b>하는 것을 기본 개념으로 삼고 있는데, 삽입되는 때에 따라 세 가지 방법으로 나눌 수 있다.

- 컴파일 시점 : AOP 개발 도구가 소스 코드를 컴파일 하기 전에 공통 구현 코드를 소스에 삽입하는 방식. AspectJ 등 AOP 도구 필요.  
- 클래스 로딩 시점 : 클래스를 로딩할 때 바이트 코드에 공통 기능을 삽입하는 방식. AspectJ 등 AOP 도구 필요.
- 런타임 시점 : 란타임에 프록시 객체를 생성해서 공통 기능을 삽입하는 방식. 스프링 자체 제공.  

스프링은 런타임 시점에 공통 기능을 삽입하는 방식을 자체적으로 제공한다. 프록시 객체를 자동으로 생성해주기 때문에 상위 타입의 인터페이스를 구현한 프록시 클래스를 직접 구현할 필요는 없고 공통 기능을 구현한 모듈 클래스만 알맞게 작성하면 된다.

<img src="https://user-images.githubusercontent.com/57310034/104416474-39153780-55b7-11eb-8145-93d26dcd5415.png"/>  
<br>
<br>

AOP에서 공통 기능을 Aspect라고 하는데 이 외에 알아두어야 할 용어들은 다음과 같다.

- Advice : 언제 공통 기능을 핵심 기능에 적용할지를 정의한다. (ex. 메서드를 호출하기 전 트랜잭션 시작한다)
- Joinpoint : Advice를 적용 가능한 지점(ex 메서드 호출, 필드 값 변경 등)을 의미한다. 스프링은 프록시를 이용해서 AOP를 구현하기 때문에 메서드 호출이 대한 Joinpoint만 지원한다. 
- Pointcut : Joinpoint의 부분집합으로서 실제 Advice가 적용되는 Joinpoint를 나타낸다. 스프링에서는 정규표현식이나 AspectJ 문법을 이용하여 이를 정의한다.
- Weaving : Advice를 핵짐 기능 코드에 적용하는 것.
- Aspect : 여러 객체에 공통으로 적용되는 기능(ex 트랜잭션, 보안 등)  
<br>

### Advice의 종류

스프링은 프록시를 이용해서 메서드 호출 지점에 Aspect를 적용하기 때문에 구현 가능한 Advice는 다음으로 한정되어있다.
- Before Advice : 대상 객체의 메서드 호출 전
- After Returning Advice : 대상 객체의 메서드가 예외 없이 정상 실행된 후
- After Throwing Advice : 대상 객체의 메서드를 실행하는 도중 예외가 발생한 경우
- After Advice : 예외 발생 여부에 상관없이 대상 객체의 메서드 실행 후
- Around Advice : 대상 객체의 메서드 실행 전과 후, 예외 발생 시점 모두  

다양한 시점에 원하는 기능을 삽입할 수 있기 때문에 Around Advice가 가장 많이 사용된다. 캐싱, 성능 모니터링등의 Aspect를 구현할 때에도 Around Advice를 주로 이용한다.  

## 스프링 AOP 구현

스프링 AOP를 이용해서 공통 기능을 구현하고 적용하려면 다음과 같은 절차를 따르면 된다.
- Aspect로 사용할 클래스에 `@Aspect` 어노테이션을 붙인다.
- `@Pointcut` 어노테이션으로 공통 기능을 적용할 Pointcut을 정의한다.
- 공통 기능을 구현한 메서드에 `@Around` 어노테이션을 적용한다.

개발자는 공통 기능을 제공하는 Aspect 구현 클래스를 만들고 자바 설정을 이용해서 Aspect를 어디에 적용할지 설정하면 된다. 프록시는 스프링 프레임워크가 알아서 만들어준다.

```java
@Aspect
public class ExecuteTimeAspect {

    //공통 기능을 적용할 대상 설정
    @Pointcut("execution(public * spring..*(..))") 
	private void publicTarget() { }

    //Around Advice 설정. publicTarget() 메서드에 정의한 pointcut에 적용한다.
	@Around("publicTarget()")
	public Object measure(ProceedingJoinPoint joinPoint) throws Throwable {
		long start = System.nanoTime(); //대상 메서드 실행 전 시간 측정
		try {
			Object result = joinPoint.proceed(); //대상 메서드 실행
			return result;
		} finally {
			long finish = System.nanoTime(); //대상 메서드 실행 후 시간 측정
			Signature sig = joinPoint.getSignature(); //대상 메서드의 시그니처

            //대상 객체의 각종 정보와 실행 시간을 출력
			System.out.printf("%s.%s(%s) 실행 시간 : %d ns\n", 
					joinPoint.getTarget().getClass().getSimpleName(),
					sig.getName(), Arrays.toString(joinPoint.getArgs()),
					(finish - start));
		}
	}
}
```

위 코드는 특정 핵심 기능을 수행할 때 실행 시간을 측정해 함께 출력해주는 "공통 기능"이다. `@Pointcut` 어노테이션의 `execution` 명시자로 정의된 대상 메서드가 실행될 때, `measure()` 메서드도 함께 실행되는 것이다. 엄밀히 말하면 클라이언트의 대상 메서드 실행 시, `measure()` 메서드가 대신 실행되어 부가 공통 기능을 수행하고 그 과정에서 대상 메서드를 실행한다. 여기서는 spring 패키지나 그 하위 패키지에 속한 빈 객체의 public 메서드가 그 대상이다.  

`measure()` 메서드는 `ProcedingJoinPoint` 타입의 파라미터를 가진다. 이 파라미터를 사용해 대상 객체의 메서드를 호출하거나 대상 객체의 각종 정보들을 알아낼 수 있다.  

```java
package spring;

public interface Calculator {

	public long factorial(long num);

}
```
위 처럼 spring 패키지에 팩토리얼을 계산할 수 있는 계산기 인터페이스가 있다고 하자.  
<br>
<br>

```java
package spring;

public class RecCalculator implements Calculator {

	@Override
	public long factorial(long num) {
        if (num == 0)
            return 1;
        else
            return num * factorial(num - 1);
	}

}
```
동일한 패키지에 `Calculator`를 구현하는 `RecCalculator`를 생성했다. 위 클래스로 생성한 빈 객체에 아까 정의한 Aspect를 적용해보자.  
<br>
<br>

```java
@Configuration
@EnableAspectJAutoProxy //Aspect 클래스를 사용하려면 붙여야한다.
public class AppContext {
	@Bean //아까 작성한 Aspect 클래스를 빈으로 등록
	public ExecuteTimeAspect executeTimeAspect() {
		return new ExecuteTimeAspect();
	}

	@Bean //measure()가 적용될 수 있는 빈 객체
	public Calculator calculator() {
		return new RecCalculator(); 
	}

}
```
`@EnableAspectJAutoProxy` 어노테이션을 설정 클래스에 붙이면 컨테이너 및 빈 객체 초기화 시, `@Aspect` 어노테이션이 붙은 빈 객체를 찾아서 빈 객체의 `@Pointcut` 설정과 `@Around` 설정을 사용한다. 그리고 여기서 설정된 `calculator` 빈 객체는 앞서 Aspect 클래스에서 Pointcut으로 정의한 대상 경로에 포함되기 때문에 `calculator` 빈에 `measure()`가 적용된다.  
<br>
<br>

```java
public static void main(String[] args) {
	AnnotationConfigApplicationContext context = 
	    new AnnotationConfigApplicationContext(AppContext.class);

    //calculator 빈 객체 가져오기
	Calculator calculator = context.getBean("calculator", Calculator.class);
	long fiveFact = calculator.factorial(5); //메서드 실행

	System.out.println("calculator.factorial(5) = " + fiveFact);
	System.out.println(calculator.getClass().getName());

    context.close();
}
```
```
RecCalculator.factorial([5]) 실행 시간 : 50201 ns
cal.factorial(5) = 120
com.cun.proxy.$Proxy17
```
메인 클래스에서의 사용과 그에 따른 결과이다. 결과에서 첫 번째 줄은 `ExecuteTimeAspect` 클래스의 `measure()` 메서드가 출력한 것이고 두 번째 줄과 세 번째 줄은 메인 메서드에서 출력한 것이다. 우리가 원한대로 Calculator의 메서드(핵심 기능)가 실행될 때 실행 시간(공통 기능)을 측정했다.  

그런데 세 번째 줄의 출력이 이상하다. `calculator` 빈 객체의 이름을 출력하도록 했는데 엉뚱한 이름이 출력되었다. 이는 스프링이 런타임에 자동으로 생성한 프록시 객체이다. 이때의 흐름을 살펴보자.  

<img src="https://user-images.githubusercontent.com/57310034/104437920-bac68e80-55d2-11eb-8c12-3a70f3fb5313.png"/>

스프링은 `RecCalculator`의 메서드가 `ExecuteTimeAspect`의 `measure()`메서드가 삽입될 대상인 것을 알고, 런타임에 `RecCalculator`가 구현한 `Calculator` 인터페이스를 구현하는 `$Proxy17` 클래스를 생성하였다. 그리고 `calculator`의 이름으로 `RecCalculator`가 아닌 `$Proxy17` 빈 객체를 등록하였고, `$Proxy17`의 `factorial()` 구현부에서는 `ExecuteTimeAspect`의 `measure()`를 호출하도록 했다. 이때, `$Proxy17`은 `measure()`를 호출할 때 파라미터로 `ProceedingJoinPoint` 객체를 넘겨주었는데 이는 실제 핵심 기능을 수행할 `RecCalculator`의 각종 정보와, `factorial()`메서드, 그리고 시그니처를 포함하고 있다. 따라서 `measure()`는 `ProceedingJoinPoint`를 이용해 `RecCalculator`의 `factorial()`를 실행하였고, 그 전후로 시간을 측정해 실행 시간을 계산해낸 것이다. 

AOP를 적용하지 않았다면 위 그림에서 중간의 세 단계는 쏙 빠지게 된다. 반대로 말하면 중간의 세 단계를 스프링이 자동으로 만들어준 것이다. 따라서 `calculator` 이름으로 등록된 빈 객체는 `$Proxy17`로 조회가 되며, 실제로 설정 클래스에서 `executeTimeAspect()` 빈 설정 메서드를 지우고 실행하면 `calculator.getClass().getName()`은 `$Proxy17`이 아닌 `RecCalculator`를 출력한다.
<br>

### ProceedingJoinPoint

위에서 Around에 해당하는 메서드(핵심 기능에 삽입할 공통 기능)은 ProceedingJoinPoint 타입의 파라미터로 가진다고 했다. 해당 객체의 `proceed()` 메서드를 호출하면, 실제로 클라이언트가 요구한 핵심 기능을 수행하는 대상 객체의 메서드를 호출하게 된다.  

ProceedingJoinPoint는 이 뿐만 아니라 호출되는 대상 객체의 정보, 실행되는 메서드에 대한 정보, 메서드를 호출할 때 전달된 인자에 대한 정보를 모두 포함하고 있는 인터페이스이다.
- `Signature getSignature()` : 호출되는 메서드에 대한 정보를 리턴
- `Object getTarget()` : 대상 객체를 리턴
- `Object[] getArgs()` : 파라미터 목록을 리턴

메서드에 대한 정보를 가지고 있는 `Signature` 인터페이스의 메서드는 다음과 같다.
- `String getName()` : 메서드의 이름을 반환
- `String toLongString()` : 메서드의 이름을 완전하게 표현하여 반환 (리턴타입, 파라미터 타입 등 포함)
- `String toShortString()` : 메서드의 이름을 축약하여 반환  

<br>

## 프록시 생성 방식

스프링은 런타임에 대상 객체가 상속하고 있는 인터페이스가 있다면, 해당 인터페이스를 상속하는 프록시 객체를 생성한다. 따라서 다음과 같은 코드는 에러를 일으킨다.
```java
//기존
Calculator calculator = context.getBean("calculator", Calculator.class);
//수정 후
RecCalculator calculator = context.getBean("calculator", RecCalculator.class);
```  
<br>

calculator 이름으로 등록된 빈은, `Calculator` 인터페이스를 상속하는 프록시 객체임으로 대상 객체인 RecCalculator	와는 다른 타입을 가진다. 따라서 프록시 객체를 RecCalculator 타입으로 캐스팅할 수 없기 때문에 예외가 발생하며 종료된다.  

만약 프록시 객체를 대상 객체가 상속하는 인터페이스를 상속하여 만드는 대신, 대신 객체를 직접 상속하고 싶다면 다음과 같이 설정하면 된다.  
```java
@Configuration
@EnableAspectJAutoProxy(projxyTargetClass = true) //속성 추가
public class AppContext {
	...
}
```  
<br>

그럼 아까와 같이 RecCalculator 타입으로 빈 객체를 찾아도 예외가 발생하지 않는다. 프록시 객체는 RecCalculator를 상속하여 만들어졌기때문이다.  
<br>

### excution 명시자 표현식

Aspect를 적용할 위치를 지정할 때 사용한 Pointcut 설정을 보면 execution 명시자를 사용했다.  
```java
@Pointcut("execution(public * chap07..*(..))")
private void publicTarget() { }
```  
<br>

execution 명시자의 기본 형식은 다음과 같다.
```
execution([수식어] 리턴타입 [클래스이름].메서드이름(파라미터))
```  
<br>

"?" 가 붙은 속성은 생략이 가능하고, 공백으로 구분되어 있는 속성들은 나열할 때 공백을 삽입해주어야한다.  

- 수식어 : public, private 등 수식어를 명시하지만 스프링 AOP에서는 private만 가능하다. (생략 가능)
- 리턴타입 : 리턴 타입을 명시
- 클래스이름 및 메서드이름 : 클래스이름과 메서드 이름을 명시 (클래스 이름은 풀 패키지명으로 명시한다.)
- 파라미터 : 메서드의 파라미터를 명시
- "*" : 모든 값을 표현
- ".." : 0개 이상을 의미  

`execution(public Integer spring.*.*(*))`
 - spring 패키지에 속해있고, 파라미터가 1개인 모든 메서드

`execution(* pring..*.get*(..))`
 - spring 패키지 및 하위 패키지에 속해있고, 이름이 get으로 시작하는 파라미터가 0개 이상인 모든 메서드 

`execution(* spring..*Service.*(..))`
 - spring 패키지 및 하위 패키지에 속해있고, 이름이 Service르 끝나는 인터페이스의 파라미터가 0개 이상인 모든 메서드

`execution(* spring.BoardService.*(..))`
 - spring.BoardService 인터페이스에 속한 파마리터가 0개 이상인 모든 메서드

`execution(* some*(*, *))`
 - 메서드 이름이 some으로 시작하고 파라미터가 2개인 모든 메서드
 
`excecution(* read*(Integer, ..))`
 - 메서드 이름이 read로 시작하고 첫 번째 파라미터 타입이 Integer이며 한 개 이상의 파라미터를 갖는 메서드

<br>

### Advice 적용 순서

한 Pointcut에 여러 Advice를 적용할 수도 있다.  
<Br>

```java
@Aspect
public class CacheAspect {

	//캐시로 사용할 해시맵
	private Map<Long, Object> cache = new HashMap<>();

	@Pointcut("execution(public * spring..*(long))")
	public void cacheTarget() {
	}
	
	@Around("cacheTarget()")
	public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
		//대상 객체를 호출할 때 사용한 첫 번째 파라미터를 가져온다
		Long num = (Long) joinPoint.getArgs()[0]; 

		//캐시된 값이면 캐시 값을 반환한다.
		if (cache.containsKey(num)) {
			System.out.printf("CacheAspect: Cache에서 구함[%d]\n", num);
			return cache.get(num);
		}

		//캐시되지 않은 값이라면 대상 객체의 메서드를 호출해 값을 구한다.
		//구한 값을 캐시에 저장한다.
		Object result = joinPoint.proceed();
		cache.put(num, result);
		System.out.printf("CacheAspect: Cache에 추가[%d]\n", num);
		return result;
	}
}
```
CacheAspect 클래스는 간단하게 캐시를 구현한 공통 기능이다. 새로운 Aspect를 만들었으므로 설정 클래스에도 추가해주어야한다.  
<br>

```java
@Configuration
@EnableAspectJAutoProxy
public class AppContex {

	@Bean
	public CacheAspect cacheAspect() {
		return new CacheAspect();
	}

	@Bean
	public ExeTimeAspect exeTimeAspect() {
		return new ExeTimeAspect();
	}

	@Bean
	public Calculator calculator() {
		return new RecCalculator();
	}

}
```  
이제 이를 메인 메서드에서 사용해보자.  
<br>

```java
public class MainAspectWithCache {
	
	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = 
				new AnnotationConfigApplicationContext(AppcontextWithCache.class);

		Calculator calator = context.getBean("calculator", Calculator.class);
		calator.factorial(7); //(1)
		calator.factorial(7); //(2)
		calator.factorial(5); //(3)
		calator.factorial(5); //(4)
		context.close();
	}
}
```
```
RecCalculator.factorial([7]) 실행 시간 : 26775 ns // -- (1)
CacheAspect: Cache에 추가[7] // -- (1)
CacheAspect: Cache에서 구함[7] // -- (2)
RecCalculator.factorial([5]) 실행 시간 : 6247 ns // -- (3)
CacheAspect: Cache에 추가[5] // -- (3)
CacheAspect: Cache에서 구함[5] // -- (4)
```

결과를 보면 첫 번째 `factorial(7)`을 실행할 때와 두 번째 `factorial(7)`을 실행할 때 콘솔에 출력되는 내용이 다르다. 처음 실행 때에는 실행 시간을 구하고 캐시에 저장하였으나 두 번째 실행 때에는 캐시에서 값을 구하기만 하였다. "CacheAspect프록시 --> ExecuteTimeAspect프록시 --> 대상 객체" 의 순서로 Advice가 적용되었기 때문이다.  
즉 `getBean()`으로 구한 Calculator 객체는 CacheAspect 프록시 객체이며, CacheAspect 프록시 객체의 대상 객체는 ExecuteTimeAspect이다. 그리고 ExecuteTimeAspect의 대상 객체는 RecCalculator인 것이다.  

어떤 Aspect가 먼저 적용될지는 스프링 프레임워크나 자바 버전에 따라 달라질 수 있기 때문에 적용 순서가 중요하다면 `@Order` 어노테이션을 통해 직접 순서를 지정해주어야한다.

```java
@Aspect
@Order(1) //대상 객체에 가깝게 적용
public class ExecuteTimeAspect {
	...
}

@Aspect
@Order(2) //대상 객체에 멀게 적용
public class CachedAspect {
	...
}
```  
<br>

### @Around와 Pointcut 설정과 @Pointcut 재사용

`@Pointcut` 어노테이션이 아닌 `@Around` 어노테이션에 execution 명시자를 직접 지정할 수도 있다.

```java
@Around("execution(public * spring..*(..))") //명시자를 Around 어노테이션에 명시
public Object execute(ProceedingJoinPoint joinPoin) throws Throwable {
	...
}
```  
<br>

그리고 같은 Pointcut을 여러 Advide가 함께 사용할 경우도 많을 것이다. 이 경우 하나의 Pointcut을 만들어두고 여러 개의 Advice가 참조하면 된다. 만약 다른 클래스에 있는 Advice에서 참조하고 싶다면 Pointcut 정의 메서드를 public으로 만들어주면 된다.  

그리고 이왕이면, 여러 Aspect에서 공통으로 사용하는 Pointcut이 있을 때는 Pointcut을 모아두는 별도의 클래스를 두고, 각 Aspect 클래스에서 이를 참조하도록 구성하면 관리가 편해진다.
```java
public class CommonPointcut {
	@Pointcut("execute(public * spring..*(..))")
	public void commonTarget() { }
}
```
```java
@Aspect
public class CacheAspect {
	private Map<Long, Object> cache = new HashMap<>();

	@Around("CommonPointcut.commonTarget()")
	public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
		...
	}
}
```
```java
@Aspect
public class ExecuteTimeAspect {

	@Around("publicTarget()")
	public Object measure(ProceedingJoinPoint joinPoint) throws Throwable {
		...
	}
}
```

이 경우 `CommonPointcut`은 설정 클래스에서 따로 빈으로 등록할 필요는 없다. Aspect 클래스에서 접근이 가능한 상태(public) 이기만 하면 된다.



<br>
<br>

--- 
해당 포스팅은 [초보 웹 개발자를 위한 스프링5 프로그래밍 입문 - 최범균 저](https://www.aladin.co.kr/shop/wproduct.aspx?ItemId=157472828)를 정리하며 작성하였습니다.

참고:  
