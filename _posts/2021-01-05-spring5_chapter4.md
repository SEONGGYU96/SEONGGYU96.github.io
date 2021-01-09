---
layout: post
title: "Spring5 : 의존 자동 주입"
subtitle: "Spring5 study (4)"
author: "GuGyu"
header-style: text
tags:
  - Spring
  - Spring5
  - Java
  - Study
  - Server
  - DI
  - DependencyInjection
  - Autowired
---

[이전 포스팅](https://seonggyu96.github.io/2020/12/31/spring5_chapter3/)에서 스프링에서의 DI 설정에 대해 다뤘다. 개중에는 `@Autowired` 어노테이션을 사용하여 의존성을 자동으로 찾아 주입해주는 방법도 있었다. 스프링 3~4 버전에서는 이런 의존 자동 주입에 호불호가 있었으나 스프링 부트가 나오면서 의존 자동 주입을 사용하는 추세로 바뀌었다고 한다. 조금 더 자세히 알아보자.

# @Autowired를 이용한 의존 자동 주입

## Field Injection

의존 객체를 참조할 필드에 `@Autowired` 어노테이션을 붙여주면 스프링이 알아서 의존 객체를 찾아서 주입한다. 즉, 설정 클래스에 의존 객체를 명시적으로 주입하지 않아도 된다는 것이다.  

```java
public class ChangePasswordService {
    @Autowired
    private MemberDao memberDao; //해당 필드는 setter나 생성자를 사용하지 않아도 스프링이 알아서 찾아 넣어준다.

    public void setMemberDao(MemberDao memberDao) {
        this.memberDao = memberDao;
    }
    ...
}

@Configuration
public class AppContext {
    @Bean
    public MemberDao memberDao() {
        return new MemberDao();
    }

    @Bean
    publick ChangePasswordService changePasswordService() {
        ChangePasswordService passwordService = new ChangePasswordService();
        //passwordService.setMemberDao(memberDao()); 직접 주입하지 않아도 스프링이 찾아 넣어줄 것이다.
        return passwordService
    }
    ...
}
```  
<br>

## Setter Injection

`@Autowired` 어노테이션을 세터 메서드에 붙여 의존 자동 주입을 할 수 있다. 결과는 동일하다.  

```java
public class ChangePasswordService {
    
    private MemberDao memberDao;

    @Autowired //MemberDao 맴버 변수에 @Autowired 어노테이션을 붙인 것과 동일하게 스프링이 알아서 객체를 찾아 넣어준다.
    public void setMemberDao(MemberDao memberDao) {
        this.memberDao = memberDao;
    }
    ...
}
```

## Constructor Injection

`@Autowired` 어노테이션을 생성자에 붙여 의존 자동 주입을 한다. 단일 생성자인 경우에는 `@Autowired` 어노테이션을 생략해도 된다.

```java
public class ChangePasswordService {
    
    private MemberDao memberDao;

    //@Autowired 어노테이션을 붙여도 되고 생략해도 된다.
    public hangePasswordService(MemberDao memberDao) {
        this.memberDao = memberDao;
    }
    ...
}
```

하지만 사실 생성자에 `@Autowired`를 사용하면 설정 클래스에서 빈 설정 시, 의존 객체를 명시해서 적어주어야 하기 때문에 큰 체감이 없다. 즉 `@Autowired`를 붙이나 붙이지 않으나 결과가 동일하다.

```java
public class AppContext {
    ...
    @Bean
    public MemberDao memberDao() {
        return new MemberDao();
    }

    @Bean
    public ChangePasswordService changePasswordService() {
        return new ChangePasswordService(memberDao());
    }
}
```

하지만 컴포넌트 스캔을 함께 사용하면 `@Autowired`는 아주 큰 시너지를 낼 수 있다.  

```java
@Component //자동 빈 등록
public class MemberDao {
    ...
}

@Component //자동 빈 등록
public class ChangePasswordService {
    
    private MemberDao memberDao;

    @Autowired
    public hangePasswordService(MemberDao memberDao) {
        this.memberDao = memberDao;
    }
    ...
}

@Configuration
@ComponentScan
public class AppContext {
    //빈 설정 클래스를 작성하지 않아도 됨
}

public class Main {

    public static void main(String[] args) throws IOException {
        ApplicationContext context = new AnnotationConfigApplicationContext(AppContext.class);

        //정상 작동
        context.getBean("changePasswordService", ChangePasswordService.class) 
        ...
    }
}
```

## 차이점과 권장 방법

위 세 가지 의존 자동 주입 방법 중, 생성자 주입 방식을 권장하고 있다. 필드 주입 방식과 세터 메서드 주입 방식은 의존 객체를 주입하지 않아도 빈 객체 생성은 가능하기 때문에 NPE등 여러가지 문제점에 취약하고, 무엇보다 순환 참조 여부를 비즈니스 로직 실행 전까지는 알 수 없다. 하지만 생성자 주입 방식은 빈 생성 과정에서 의존 객체를 주입하기 때문에 순환 참조 여부를 바로 알 수 있어 디버깅에 유리하다. 또한 테스트도 생성자 주입 방식이 용이하다고 하여 스프링 4.3 부터는 생성자 주입 방식을 "항상" 권장하고 있다.  

## 에러

`@Autowried` 어노테이션을 적용한 대상에 일치하는 빈이 없으면 `NoSuchBeanDefinitionException`, 일치하는 빈이 두 개 이상이면 `NoUniqueBeanDefinitionException` 런타임 에러가 발생한다. 후자의 경우, `@Qualifier` 어노테이션으로 해결할 수 있다.  
<br>

# @Qualifier 어노테이션을 이용한 의존 객체 선택

자동 주입이 가능한 빈이 두 개 이상이면 `@Qualifier`를 **추가로** 사용하여 주입 대상 빈을 **한정**할 수 있다. `@Qulifier`의 기능은 다음과 같다.

- 빈 객체에 한정자 추가
```java
@Configuration
public class AppContext {
    ...
    @Bean
    @Qualifier("memberDao1") //memberDao 빈에 "memberDao1" 이라는 한정자를 추가
    publick MemberDao memberDao() {
        return new MemberDao();
    }
    ...
}
```
<br>

- `@Autowired`와 함께 사용해 주입할 대상 빈을 한정  
```java
public class ChangePasswordService {
    @Autowired
    @Qualifier("memberDao1") //한정자 이름이 "memberDao1" 인 빈 객체를 찾아 주입됨
    private MemberDao memberDao; 

    public void setMemberDao(MemberDao memberDao) {
        this.memberDao = memberDao;
    }
    ...
}
```  

여기서 한정자와 빈 이름이 조금 헷갈릴 수 있다. 한정자와 빈 이름은 다른 개념이다. 빈 이름은 스프링 컨테이너가 빈 객체를 관리할 때 사용하는 이름이며, 한정자는 의존 객체를 자동으로 주입할 때 이를 구분짓기 위해 사용되는 이름이다. 따라서 만약 `@Qualifier`를 사용하지 않으면 해당 빈 객체의 한정자는 빈 객체의 이름으로 초기화된다.  


|빈 이름|@Qualifier|한정자|
|:------|:---|:---|
|memberDao|memberDao1|memberDao1|
|memberDao2|사용안함|memberDao2|
|changePasswordService|사용안함|changePasswordService|   

<br>

# 상위/하위 타입 관계와 자동주입

`@Qualifier`를 사용해 한정자를 적용하지 않으면 스프링은 `@Autowired`가 붙어있는 맴버 변수/메서드에 대상 객체를 주입하기 위해 동일한 **타입**을 찾는다. 하지만 동일한 타입의 빈 객체가 두 개 이상인 경우 예외를 발생한다고 설명했다. 여기서 하나 주의할 점이 있다.  

```java
public class CachedMemberDao extends MemberDao {
    ...
}
```

위와 같이 `MemberDao`를 상속하는 `CachedMemberDao`가 있다고 하자.  

```java
@Configuration
public class AppContext {
    ...
    @Bean
    public MemberDao memberDao1() {
        return new MemberDao();
    }

    @Bean
    public CachedMemberDao memberDao2() {
        return new CachedMemberDao(); //CachedMemberDao 타입이기도 하지만 MemberDao 타입이기도 함
    }
    ...
}  
```  
```java
public class ChangePasswordService {
    @Autowired
    private MemberDao memberDao; //NoUniqueBeanDefinitionException 예외 발생
    ...
}
```
설정 클래스에서 `MemberDao` 타입의 빈 객체는 `memberDao1` 뿐이라며 문제 `@Qualifier`를 사용하지 않으면 또 `NoUniqueBeanDefinitionException` 예외를 뱉어낸다. `CachedMemberDao`는 `MemberDao`를 상속한 클래스이기 때문에 `MemberDao` 타입이라고도 볼 수 있기 때문이다. 따라서 빈 객체의 상속 관계를 살펴보고 `@Qualifier`의 사용을 고려해야한다.  
<br>

# Nullable한 맴버 변수와 @Autowired

만약 의존 객체를 가리키는 멤버 변수가 `Nullable`한 경우에 `@Autowired`를 사용하면 어떻게 될까? 멤버 변수가 `Nullable`하다는 것은 해당 변수의 값이 `null`이어도 모든 메서드들이 정상적으로 작동할 수 있도록 설계되었을 것이다. 즉 반드시 의존 객체를 주입할 필요가 없고, 상황에 따라 주입할 빈 객체가 아직 존재하지 않을 수도 있다.

하지만 `@Autowired`는 기본적으로 주입할 빈이 존재하지 않으면 예외를 발생시킨다. 이때, 예외 대신 그냥 `null`로 상태로 두고 싶다면 `required` 속성을 사용할 수 있다.  
```java
public class MemberPrinter {
    private DateTimeFormatter dateTimeFormatter; //Nullable

    public void print(Membeer member) {
        if (dateTimeFormatter == null) {
            ...//기본 포맷
        } else {
            ...//설정된 포맷
        }
    }

    @Autowired(requierd = false) 
    //DateTimeFormater 타입의 빈 객체가 존재하지 않으면 null로 초기화
    public void setDateFormatter(DateTimeFormmater dateTimeFormatter) {
        this.dateTimeFormatter = dateTimeFormatter;
    }
    ...
}
```  
<br>

스프링 5 버전부터는 `required = false` 대신, 의존 주입 대상에 자바 8의 `Optional`을 사용해도 된다.  
```java
public class MemberPrinter {
    private DateTimeFormatter dateTimeFormatter; //Nullable

    public void print(Membeer member) {
        ...
    }

    public void setDateFormatter(Optional<DateTimeFormmater> optionalFormatter) {
        if (optionalFormatter.isPresent()) {}
            this.dateTimeFormatter = optionalFormatter.get();
        } else {
            this.dateTimeFormatter = null;
    }
    ...
}
```  
<br>

아니면 그냥 `@Nullable` 어노테이션을 붙이는 방법도 있다.
```java
public class MemberPrinter {
    private DateTimeFormatter dateTimeFormatter; //Nullable

    public void print(Membeer member) {
        ...
    }

    public void setDateFormatter(@Nullable DateTimeFormmater dateTimeFormatter) {
        this.dateTimeFormatter = dateTimeFormatter;
    }
    ...
}
```  
<br>

다만 `required = false`을 사용하면 주입할 빈이 존재하지 않을 때 세터 메서드가 아예 실행되지 않지만 나머지 방법을 사용하면 세터 메서드가 실행은 된다는 점에서 차이가 있다. 그리고 이 말은 즉, `required = false`를 사용했고 주입할 빈 객체가 존재하지 않으면 <b>`null`로 초기화하는 것이 아니라 초기화 과정을 진행하지 않는다</b>는 것을 뜻한다. 다른 방법들은 세터의 내용에 따라 맴버 변수를 `null`로 초기화를 진행한다.  

# 자동 주입과 명시적 의존 주입 간의 관계

그런데 만약 의존 자동 주입 대상을 설정 클래스에서 직접 주입하게되면 어떻게 될까? 이 경우, 자동 주입을 통해 일치하는 빈을 주입한다. 아래 예제에서는 세터 메서드를 통해 `memberDao1`을 명시적으로 주입하였으나, `@Autowired`로 인해 자동으로 `memberDao`가 주입된다.
```java
public class AppContext {
    ...
    @Bean
    public MemberDao memberDao1() {
        return new MemberDao();
    }

    @Bean
    @Qualifier("memberDao")
    public CachedMemberDao memberDao2() {
        return new CachedMemberDao();
    }

    @Bean
    public ChangePasswordService changePasswordService() {
        ChangePasswordService passwordService = new ChangePasswordService();
        //명시적으로 "memberDao1" 이름의(한정자의) 빈 객체 주입
        passwordService.setMemberDao(memberDao1());
        return passwordService;
    }
    ...
}

public class ChangePasswordService {
    ...
    @Autowired
    @Qualifier("memberDao") // "memberDao" 한정자를 가진 빈 객체 자동 주입 대상
    public void setMemberDao(MemberDao memberDao) {
        this.memberDao = memberDao;
    }
    ...
}
```  





<br>
<br>


--- 
해당 포스팅은 [초보 웹 개발자를 위한 스프링5 프로그래밍 입문 - 최범균 저](https://www.aladin.co.kr/shop/wproduct.aspx?ItemId=157472828)를 정리하며 작성하였습니다.

참고 :  
[https://engkimbs.tistory.com/683](https://engkimbs.tistory.com/683)