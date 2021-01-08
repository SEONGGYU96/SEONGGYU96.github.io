---
layout: post
title: "Spring5 : Bean 라이프사이클과 범위"
subtitle: "Spring5 study (5)"
author: "GuGyu"
header-style: text
tags:
  - Spring
  - Spring5
  - Java
  - Study
  - Server
  - Bean
  - LifeCycle
---

## 컨테이너의 라이프사이클

스프링 컨테이너는 초기화와 종료라는 라이프사이클을 갖는다.

```java
//1. 컨테이너 초기화
AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppContext.class);

//2. 컨테이너에서 빈 객체를 구해서 사용
Greeter greeter = context.getBean("greeter", Greeter.class);
String msg = greeter.greet("spring");
System.out.println(msg);

//3. 컨테이너 종료
context.close();
```

### 컨테이너 초기화
`AnnotationConfigApplicationContext`의 생성자를 이용해서 객체를 생성할 때 스프링 컨테이너를 초기화한다. 파라미러로 입력한 설정 클래스의 정보를 읽어와 빈 객체를 생성하고 각 빈을 연결(의존 주입)한다.

### 컨테이너 사용

`getBean()`과 같은 메서드를 사용하여 컨테이너에 보관된 빈 객체를 구하여 용도에 맞게 사용한다.

### 컨테이너 종료

`close()` 를 이용하여 컨테이너를 종료한다. `close()`는 부모 클래스인 `AbstractApplicationContext` 클래스에 정의되어 있어 자바, XML, Groovy를 사용해 생성한 컨테이너 객체에서 모두 사용할 수 있다.
<img src="https://user-images.githubusercontent.com/57310034/103868495-4f7d4800-510c-11eb-849b-f7a10784c7e6.png"/>

컨테이너 종료 시, 빈 객체들을 모두 소멸시킨다.

## Bean 객체의 라이프사이클

빈 객체의 라이프사이클은 스프링 컨테이너가 관리한다. 크게 객체 생성, 의존 주입, 초기화, 소멸 네 가지 단계로 나눌 수 있다.

<img src="https://user-images.githubusercontent.com/57310034/103980277-f3c1c600-51c2-11eb-8df3-555f3b8cc498.png"/>

### 빈 객체의 초기화와 소멸

스프링은 라이프사이클에 특화된 인터페이스를 제공한다. 

#### `InitializingBean`

빈 객체의 클래스가 해당 인터페이스를 구현하고 있다면, 스프링 컨테이너가 빈 객체를 초기화할 때 해당 인터페이스의 메서드를 호출한다.

```java
public interface InitializingBean {
  void afterPropertiesSet() throws Excption;
}
```

#### `DisposableBean`

마찬가지로 빈 객체를 소멸시킬때 사용되는 메서드를 가진 인터페이스로, 빈 객체 소멸 시 스프링 컨테이너가 호출한다.

```java
public interface DisposableBean {
  void destroy() throws Exception;
}
```

빈 객체를 초기화하고 소멸할 때 필요한 작업이 있다면 위 인터페이스들을 빈 객체 클래스에서 구현해주면 된다.  
예) 데이터베이스 커넥션 풀을 위한 빈 객체, 소켓

#### 커스텀 메서드

위 인터페이스들을 구현하고싶지 않거나 구현할 수 없는 경우에는 스프링 설정에서 직접 초기화와 소멸에 사용될 메서드들을 지정할 수 있다.

```java
public class Client {
  private String host;

  public void setHost(String host) {
    this.host = host;
  }

  public void connect() { //초기화 시 수행하고 싶은 작업
    ...
  }

  public void close() { //소멸 시 수행하고 싶은 작업
    ...
  }
}

//초기화, 소멸 메서드 지정
@Bean(initMethod = "connect", destroyMethod = "close") 
public Client client() {
  Client client = new Client();
  client.setHost("host");
  return client;
}
```

## 빈 객체의 생성과 범위

빈 객체는 기본적으로 싱글턴 범위를 갖는다. 하지만 필요한 경우 프로토타입 범위로 설정할 수 있다. 그럼 `getBean()` 때 마다 새로운 객체를 리턴한다.

```java
@Configuration
public class AppContext {
  @Bean
  @Scope("prototype") //프로토타입 범위로 지정
  public Client client() {
    Client client = new Client();
    client.setHost("host");
    return client;
  }
}
```

그리고 굳이 싱글턴임을 명시하고 싶다면 `@Scope("singleton")`을 붙여주어도 된다.  

프로토타입 범위를 갖는 빈을 사용하면 객체 생성과 초기화는 스프링 컨테이너가 수행해주지만, 소멸 메서드까지 실행해주지는 않는다. 따라서 프로토타입 범위의 빈을 사용할 때에는 소멸 처리를 코드에서 직접 해주어야한다.  

<br>
<br>

--- 
해당 포스팅은 [초보 웹 개발자를 위한 스프링5 프로그래밍 입문 - 최범균 저](https://www.aladin.co.kr/shop/wproduct.aspx?ItemId=157472828)를 정리하며 작성하였습니다.

참고:  
Bean Lifecycle  
[https://blog.naver.com/PostView.nhn?blogId=dktmrorl&logNo=222170823315&parentCategoryNo=&categoryNo=48&viewDate=&isShowPopularPosts=false&from=postView](https://blog.naver.com/PostView.nhn?blogId=dktmrorl&logNo=222170823315&parentCategoryNo=&categoryNo=48&viewDate=&isShowPopularPosts=false&from=postView)  

BeanNameAware, ApplicationContextAware  
[https://blog.naver.com/PostView.nhn?blogId=dktmrorl&logNo=222175972129](https://blog.naver.com/PostView.nhn?blogId=dktmrorl&logNo=222175972129)