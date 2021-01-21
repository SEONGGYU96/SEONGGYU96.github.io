---
layout: post
title: "Spring5 : DB 연동 Exception과 Transaction"
subtitle: "Spring5 study (8) - 2"
author: "GuGyu"
header-style: text
tags:
  - Spring
  - Spring5
  - Java
  - Study
  - Server
  - DB
  - Database
  - JDBC
  - Transaction
---

## DB 연동 과정에서 발생 가능한 Exception

DB 연동 과정에서는 다양한 Exception이 발생한다. 연결 정보가 올바르지 않아서 발생하기도 하고 DB가 실행되지 않았거나, 방화벽에 막혀 연결이 불가능한 경우에도 발생한다. 그리고 물론 쿼리를 잘못 사용했을 때도 비번하게 발생한다.

- `CannotGetJdbcConnectionException`
- `BadSqlGrammerException`
- `DuplicateKeyException`
- `QueryTimeoutException`
- `DataAccessException`
- etc

그런데 위 예외들은 모두 스프링에서 변환 처리한 예외들이다. 이게 무슨 말이냐 하면 JDBC API가 발생시킨 예외에 따라 스프링이 따로 정의한 예외를 발생시킨다는 것이다. 왜 그렇게 하는 것일까?  

주된 이유는 연동 기술에 상관없이 동일한 예외를 처리할 수 있도록 하기 위함이다. 스프링은 JDBC 뿐만 아니라 JPA, 하이버네이트 등에 대한 연동을 지원하고 MyBatis는 자체적으로 스프링 연동 기능을 제공한다. 그런데 각각의 구현 기술마다 예외를 다르게 처리해야한다면, 우리는 연동 기술마다 예외 처리 코드를 작성해야할 것이다. 따라서 스프링은 각 연동 기술에 따라 발생하는 예외들을 스프링만의 예외로 변환함으로써 연동 기술에 상관없이 동일한 코드로 예외를 처리할 수 있게 된다.  

```java
try {
    // JDBC 연동 코드
} catch (SQLExeption exception) {
    ...
}

try {
    // 하이버네이트 연동 코드
} catch (HibernateException exception) {
    ...
}

try {
    // JPA 연동 코드
} catch (PersistenceException exception) {
    ...
}
```
위와 같은 각각의 예외 처리를 아래와 같이 하나로 통합할 수 있다.
```java
try {
    // DB 연동 코드
} catch(DataAccessException exception) {
    ..
}
```  
<br>

그러나 스프링을 사용하지 않고 그냥 JDBC를 사용한다면 JDBC의 예외 처리를 알맞게 해주어야 할 것이다.  

<br>

## 트랜잭션 처리

트랜잭션은 두 개 이상의 쿼리를 한 작업으로 실행해야 할 때 사용한다. 한 트랜잭션으로 묶인 쿼리 중 하나라도 실패하면 전체 쿼리를 실패로 간주하고 이미 실행된 쿼리를 모두 취소한다. 이렇게 DB를 쿼리 실행 전의 상태로 되돌리는 것을 롤백(rollback)이라고 부른다. 반면에 트랜잭션으로 묶인 모든 쿼리가 성공해서 쿼리 결과를 DB에 실제로 반영하는 것을 커밋(commit)이라고 한다.  

JDBC을 직접 사용할 때는 `Connection#setAutoCommit(false)`를 이용해서 트랜잭션을 시작하고, `commit()`과 `rollback()`을 이용해서 트랜잭션을 커밋하거나 롤백해야한다.  

```java
Connection connection = null;
try {
    connection = DriverManager.getConnection(jdbcUrl, user, pw);
    connection.setAutoCommit(false); //트랜잭션 범위 시작
    ... //쿼리 실행
    connection.commit(); //트랜잭션 범위 종료 : 커밋
} catch(SQLException exeption) {
    if (connection != null) {
        //트랜잭션 범위 종료 : 롤백
        try {
            connection.rollback();
        } catch (SQLException exeption1) { }
    }
    ...
}
```

이 경우, 직접 트랜잭션 범위를 관리하기 때문에 커밋이나 롤백 코드를 누락하기 쉽다. 게다가 구조적으로 반복되는 코드도 발생한다. 스프링이 제공하는 트랜잭션 기능을 사용하면 중복이 없는 매우 간단한 코드로 트랜잭션 범위를 지정할 수 있다.  

### @Transactional을 이용한 트랜잭션 처리

스프링에서는 @Transactional 어노테이션을 붙이기만 하면 트랜잭션 범위가 지정된다.  

```java
@Transactional
public void changePassword(String email, String oldPassword, String newPassword) {
    Member member = memberDao.selectByEmail(email);
    if (member == null) {
        throw new MemberNotFoundException();
    }
    member.changePassword(oldPassword, newPassword);
    memberDao.update(member);
}
```  

스프링은 @Transactional 어노테이션이 붙은 메서드를 동일한 트랜잭션 범위 내에서 실행한다. 따라서 `memberDao.selectByEmail()`에서 실행하는 쿼리와 `memberDao.update(member)` 에서 실행하는 쿼리는 한 트랜잭션에 묶인다.  

@Transactional 어노테이션이 제대로 동작하려면 다음의 두 가지 내용을 스프링 설정 클래스에 추가해야한다.  

- 플랫폼 트랜잭션 매니저 빈 설정
- @Transactional 어노테이션 활성화

```java
@Configuration
@EnableTransactionManagement //@Transactional 어노테이션 활성화
public class AppConext {
    @Bean
    public DataSource datasource() {
        ...
    }

    @Bean //플랫폼 트랜잭션 매니저 빈 설정
    public PlatformTransactionManager transactionManager() {
        DataSourceTransactionManager manager = new DataSourceTransactionManager();
        manager.setDataSource(dataSource())
        return manager;
    }
}
```

`PlatformTransactionManager`는 스프링이 제공하는 트랜잭션 매니저 인터페이스이다. JDBA든 JPA든 DB 연동 기술에 상관없이 동일한 방식으로 트랜잭션을 처리하기 위해서 사용한다. JDBC는 `DataSourceTransactionManager` 클래스를 `PlatformTransactionManager`로 사용하기 때문에 `setDataSource()`를 이용해 트랜잭션 연동에 필요한 DataSource를 지정한다.  

#### Logback

그런데 실제로 트랜잭션이 시작되고 커밋되었는지는 어떻게 확인할 수 있을까? 이를 확인하기 위해서는 스프링이 출력하는 로그 메시지를 확인하면 된다. 이를 위해서 `Logback`을 사용해보자.

```gradle
dependencies {
    ...
    implementation "org.slf4j:slf4j-api:$rootProject.slf4jVersion"
}
```

Logback은 로그 메시지 형식과 기록 위치를 설정 파일에서 읽어온다. 이 설정 파일을 `src/main/resource`에 `logback.xml`의 이름으로 작성하자.  

```xml
<?xml version="1.0" encoding="UTF-8"?>

<configuration>
	<appender name="stdout" class="ch.qos.logback.core.ConsoleAppender">
		<encoder>
			<pattern>%d %5p %c{2} - %m%n</pattern>
		</encoder>
	</appender>
	<root level="INFO">
		<appender-ref ref="stdout" />
	</root>
    <!-- 스프링의 JDBC 관련 모듈에서 출력하는 메시지 로그를 "DEBUG" 레벨까지 보도록 함-->
	<logger name="org.springframework.jdbc" level="DEBUG" />
</configuration>
```

이제 아까 작성한 @Transaction이 붙은 메서드를 실행하면 대략 아래와 같은 로그들이 출력되는 것을 확인할 수 있다.

```
2021-01-21 14:36:54,211 DEBUG o.s.j.d.DataSourceTransactionManager - Switching JDBC Connection...  
...
2021-01-21 14:36:54,291 DEBUG o.s.j.d.DataSourceTransactionManager - Initiating transaction commit...  
...
2021-01-21 14:36:54,291 DEBUG o.s.j.d.DataSourceTransactionManager - Committing JDBC transaction on Connection...  
...
```

만약 트랜잭션에 실패해 롤백이 발생했다면 위 로그에서 commit 대신 rollback 단어로 대체되어 출력될 것이다.  

그런데 우리는 이 로그들을 직접 찍어주지 않았다. 어디서 이 로그들이 찍히는 것일까? 트랜잭션은 누가 시작하고 커밋하고 롤백 해주는 것일까?  

<br>

### @Transactional과 프록시

스프링은 여러 빈 객체에 공통으로 적용되는 기능을 구현하는 방법으로 [AOP](https://seonggyu96.github.io/2021/01/14/spring5_chapter7/)를 제공한다고 했다. 트랜잭션도 각종 다양한 쿼리 실행 메서드에 대한 공통 기능이라고 볼 수 있다. 따라서 스프링은 @Transactional 어노테이션을 이용해서 트랜잭션을 처리하기 위해 내부적으로 AOP를 사용한다. 즉, 트랜잭션 또한 프록시를 통해서 구현된다.  

우리가 설정 클래스에 @EnableTransactionManagement 어노테이션을 붙여주면, 스프링은 @Transaction 어노테이션이 적용된 빈 객체를 찾아서 알맞은 프록시 객체를 알아서 생성한다.

<img src="https://user-images.githubusercontent.com/57310034/105292769-4d32e780-5bfc-11eb-8e7f-0c6bb595a43d.png"/>

`ChangePasswordService` 빈 객체 내에 @Transactional 어노테이션이 적용된 `changePassword()` 메서드가 있을 때, 위와 같은 흐름으로 실행된다. 스프링은 ChangePasswordService 클래스에 대한 프록시 객체를 생성하고 트랜잭션을 기능을 적용한다. 따라서 대상 객체(ChangePasswordService)의 메서드가 실행되기 전에 `PlatformTransactionManager`를 사용해서 트랜잭션을 시작하고, 성공적으로 대상 객체 메서드가 리턴되면 트랜잭션을 커밋한다.  

롤백 또한 마찬가지다. 대상 객체 메서드를 실행하다가 예외가 발생하면, 프록시 객체에서는 이를 catch 하여 rollback으로 처리한다.  

<img src="https://user-images.githubusercontent.com/57310034/105306277-8e78c680-5bff-11eb-9c41-34f3eb3f1c1d.png"/>

이때 별도 설정을 추가하지 않으면 발생한 예외가 `RuntimeException`(혹은 이를 상속하는 하위 클래스) 일 때만 트랜잭션을 롤백한다. 만약 `SQLException` 등이 발생했다면 트랜잭션을 롤백하지 않는다. 이 경우에도 트랜잭션을 롤백하고 싶다면 `rollbackFor` 속성을 사용해야한다.  

```java
@Transactional(rollbackFor = SQLException.class)
public void someMethod() {
    ...
}
```

여러 예외 타입을 지정하고 싶다면 `{SQLException.class, IOException.class}`와 같이 배열로 지정할 수도 있다.  

`rollbackFor`와 반대 설정을 제공하는 속성도 있는데, 바로 `noRollbackFor` 속성이다. 이 속성을 사용하면 지정한 예외가 발생해도 롤백시키지 않고 커밋한다.  

### @Transaction의 주요 속성


|속성|타입|설명|
|:--|:--|:--|
|value|String|트랜잭션을 관리할 때 사용할 PlatformTranscationManager 빈의 이름을 지정한다. 기본 값은 " "이다.|
|propagation|Propagation|트랜잭션 전파 타입을 지정한다. 기본 값은 Propagation.REQUIRED 이다.|
|isolation|Isolation|트랜잭션 격리 레벨을 지정한다. 기본 값은 Isolation.DEFAULT 이다.|
|timeout|int|트랜잭션 제한 시간을 지정한다. 기본 값은 -1이며, 이 경우 데이터베이스 타임아웃 시간을 사용한다. 초 단위로 지정한다.|  

#### value 속성

@Transaction 어노테이션의 value 속성값이 없으면 등록된 빈 중에서 타입이 `PlatformTransactionManager`인 빈을 사용한다.  

#### propagation 속성

`Propagation` 열거 타입에 정의되어 있는 값의 목록은 다음과 같다. Propagation에 대해서는 뒤에서 자세히 알아보도록 하자.

- REQUIRED : 현재 진행 중인 트랜잭션이 존재하면 해당 트랜잭션을 사용하고, 없으면 새로운 트랜잭션을 생성한다.
- MANDATORY : REQUIERED와 유사하지만 진행 중인 트랜잭션이 없으면 예외를 발생시킨다.
- REQUIRES_NEW : 항상 새로운 트랜잭션을 시작한다. 진행 중인 트랜잭션이 존재하면 기존 트랜잭션을 일시 중지하고 새로운 트랜잭션을 시작한다.
- SUPPORTS : 진행 중인 트랜잭션이 존재하면 트랜잭션을 사용하고, 없으면 메서드를 일반적으로 실행한다.
- NOT_SUPPORTED : 진행 중인 트랜잭션이 존재하면 일시 중지하고, 메서드를 일반적으로 실행한다.
- NEVER : 진행 중인 트랜잭션이 존재하면 예외를 발생시키고, 없으면 일반적으로 실행한다.
- NESTED : 진행 중인 트랜잭션이 존재하면 기존 트랜잭션에 중첩된 트랜잭션에서 메서드를 실행한다. 진행 중인 트랜잭션이 존재하지 않으면 REQUIRED와 동일하게 동작한다.  

#### isolation 속성

Isolation 열거 타입에 정의된 값은 다음과 같다.  

- DEFAULT : 기본 설정을 사용한다.
- READ_UNCONMMITTED : 다른 트랜잭션이 커밋하지 않은 데이터를 읽을 수 있다.
- READ_COMMITTED : 다른 트랜잭션이 커밋한 데이터를 읽을 수 있다.
- REPEATABLE_READ : 처음에 읽어온 데이터와 두 번째 읽어 온 데이터가 동일한 값을 갖는다.
- SERIALIZABLE : 동일한 데이터에 대해서 동시에 두 개 이상의 트랜잭션을 수행할 수 없다.  

트랜잭션 격리 레벨은 여러 트랜잭션이 동시에 DB에 접근할 때 그 접근을 어떻게 제어할지에 대한 설정을 다룬다. SERIALIZABLE로 설정하면 동일 데이터에 100개의 연결이 접근하면 한 번에 한 개의 연결만 처리한다. 마치 100명이 한 줄로 서서 차례를 기다리는 것 처럼 말이다. 따라서 전반적인 응답 속도가 느려지는 문제가 발생할 수 있다. 따라서 적절한 설정을 하는 것이 중요하다.  
<br>

### @EnableTransactionManagement 어노테이션의 주요 속성

- proxyTargetClass : 클래스를 이용해서 프록시를 생성할지 여부를 지정한다. 기본값은 false로서 인터페이스를 이용해서 프록시를 생성한다.  
- order : AOP 적용 순서를 지정한다. 기본 값은 가장 낮은 우선 순위에 해당하는 int의 최댓값이다.  

<br>

### 트랜잭션 전파 (Propagation)

```java
public class SomeService {
    private AnyService anyService;

    @Transactional
    public void some() {
        //트랜잭션 내에서 또다른 트랜잭션을 실행
        anyService.any();
    }

    public SomeService(AnyService anyService) {
        this.anyService = anyService
    } 
}

public class AnyService {
    @Transactional
    public void any() {
        ...
    }
}
```

위 코드를 살펴보면 `SomeService` 클래스와 `AnyService` 클래스는 둘 다 @Transaction 어노테이션을 가지고 있다. 따라서 두 클래스는 빈으로 등록될 때 프록시 객체가 생성된다. 이 경우 SomeService의 `some()` 메서드를 호출하면 트랜잭션이 시작되고, AnyService의 `any()` 메서드를 호출해도 트랜잭션이 시작된다. 그런데 `some()` 메서드는 내부에서 다시 `any()` 메서드를 호출하고 있다. 그럼 어떻게 될까?  

별도의 propagation 속성이 없으므로 기본 값인 REQUIRED 가 적용되어 있다. 처음 `some()` 메서드를 호출할 때 새로운 트랜잭션이 시작되고, 내부에서 `any()` 메서드가 호출되면 이미 `some()` 메서드에 의해 시작된 트랜잭션이 존재하므로 새로운 트랜잭션을 생성하지 않는다. 대신 존재하는 트랜잭션을 그대로 사용해 `some()` 과 `any()`가 하나의 트랜잭션으로 묶이게 된다.  

만약 `any()` 메서드에 `propagation = Propagation.REQUIRED_NEW` 속성을 사용했다면 기존 트랜잭션의 존재 여부와 상관없이 항상 새로운 트랜잭션을 시작할 것이다.  

그리고 다음 경우도 살펴보자.  

```java
public class ChangePasswordService {
    ...
    @Transactional
    public void changePassword(String email, String oldPassword, String newPassword) {
        ...
        mamberDao.update(momber);
    }
}

public class MemeberDao {
    ...
    //@Transaction 없음
    public void update(Member member) {
        jdbcTemplate.update(
            "update spring.MEMBER set NAME = ?, PASSWORD = ? where EMAIL  ?", 
            member.getname(),
            member.getPassword(),
            member.getEmail()
        );
    }
}
```

트랜잭션 메서드 내에서 @Transaction 이 붙지 않은 메서드를 호출하게 되면 어떻게 처리될까? JdbcTemplate 클래스를 사용한다면 동일한 트랜잭션 범위에서 쿼리가 실행된다.  `update()` 메서드를 실행할 때, JdbcTemplat가 이미 진행 중인 트랜잭션 범위에서 쿼리를 실행시키기 때문이다. 따라서 `update()` 과정에서 예외가 발생해도 모든 과정을 롤백한다.  

<br>
<br>


--- 
해당 포스팅은 [초보 웹 개발자를 위한 스프링5 프로그래밍 입문 - 최범균 저](https://www.aladin.co.kr/shop/wproduct.aspx?ItemId=157472828)를 정리하며 작성하였습니다.
