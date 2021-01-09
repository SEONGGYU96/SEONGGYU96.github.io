---
layout: post
title: "Spring5 : 컴포넌트 스캔"
subtitle: "Spring5 study (5)"
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
---

[이전 포스팅](https://seonggyu96.github.io/2021/01/05/spring5_chapter4/)에서 의존 자동 주입을 위한 `@Autowired`에 대해서 알아보았다. 스프링에는 자동주입과 함께 사용하는 `컴포넌트 스캔`이라는 기능이 있다. 이는 설정 클래스에서 빈으로 등록하지 않아도 원하는 클래스를 빈으로 등록할 수 있어 설정 클래스의 코드를 많이 줄일 수 있다.

## @Component 

클래스에 `@Component`어노테이션을 붙이면 스프링이 해당 클래스를 알아서 빈으로 등록한다. `@Bean`을 설정 메서드에 사용할 때는 메서드 이름을 빈 이름으로 사용하였으나 
`@Component`을 클래스에 사용할 때는 일반적으로 클래스 이름의 첫글자를 소문자로 바꿔 빈 이름으로 사용된다. 하지만 괄호 안에 직접 빈 이름을 지정할 수도 있다.

```java
@Component
public class MemberDao {
    ...
}

@Component("memberRegister") //빈 이름을 직접 지정할 수도 있다.
public class MemberRegisterService {
    ...
}
```  
<br>

## @ComponentScan 

`@Component` 어노테이션을 붙인 클래스를 스캔해서 스프링 빈으로 등록하기 위해서는 설정 클래스에 `@ComponentScan` 어노테이션을 적용해야한다.  

```java
@Configuration
@ComponentScan(basePackage = {"spring"})
public class AppContext {
    @Bean
    public changePasswordService() {
        return new ChangePasswordService();
    }
    ...
}
```
<br>

### basePackages 속성

@ComponentScan 어노테이션을 사용할 때는 `basePackages` 속성을 지정할 수 있다. 이는 "스캔 대상 패키지 목록"을 의미한다. 즉 위 코드에서는 `spring` 패키지와 그 하위 패키지에 속한 클래스들 중에서 @Component 어노테이션을 사용한 클래스를 빈 객체로 등록하게 된다. basePackage는 배열 형태의 값을 가지기 때문에 여러 패키지를 나열해서 스캔 대상으로 등록할 수 있다.

### excludeFilters 속성

`excludeFilters` 속성을 사용하면 스캔할 때 특정 대상을 자동 등록 대상에서 제외할 수 있다.  

```java
@Configuration
@ComponentScan(
    basePackage = {"spring"}, 
    excludeFilters = @Filter(type = FilterType.REGEX, pattern = "spring\\..*Dao")
)
public class AppContext {
    @Bean
    public changePasswordService() {
        return new ChangePasswordService();
    }
    ...
}
```

`excludeFilters` 속성은 `@Filter`을 값으로 갖고 있는데 `@Filter`는 또 `type` 속성 등을 가지고 있다.

- FilterType.REGEX : 정규표현식을 사용해서 제외 대상 지정
    - `@Filter(type = FilterType.REGEX, pattern = "spring\\..*Dao")`
- FilterType.ASPECTJ : AspectJ 패턴을 사용해서 제외 대상을 지정
    - `@Filter(type = FilterType.ASPECTJ, pattern = "spring.*Dao"`
- FilterType.ANNOTATION : 특정 어노테이션을 붙인 클래스은 제외 대상으로 지정
    - `@Filter(type = FilterType.ANNOTATION, classes = {NoProdect.class, ManualBean.class}`
- FilterType.ASSIGNABLE_TYPE : 특정 타입이나 그 하위타입 클래스를 제외 대상으로 지정
    - `@Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MemberDao.class)`

> `AspectJ`는 스프링 AOP를 구현하기 위해 사용되는 라이브러리이다.<br>따라서 패턴이 동작하려면 의존 대상에 `aspectjweaver` 모듈을 추가해야한다.  

`pattern`이나 `classes` 속성은 배열의 형태로 하나 이상의 값을 적용할 수 있으며 `excludeFilters` 속성 자체도 `@Filter`를 하나 이상 적용할 수 있다.

## 기본 스캔 대상

사실 @Component 어노테이션뿐만 아니라 다음 어노테이션을 붙인 클래스는 모두 컴포넌트 스캔 대상에 포함된다.

- `@Component`
- `@Controller`
- `@Service`
- `@Repository`
- `@Aspect`
- `@Configuration`

`@Aspect` 어노테이션을 제외하고는 `@Component` 어노테이션을 포함하는 특수 어노테이션이다. 따라서 @Component 어노테이션과 동일하게 컴포넌트 스캔 대상에 포함된다.  

## 충돌 처리

컴포넌트 스캔 기능을 사용해서 자동으로 빈을 등록할 때에는 빈 이름 충돌, 수동 등록과의 충돌 등 다양한 문제가 발생할 수 있어 주의해야한다.  

### 빈 이름 충돌

서로 다른 패키지 내에 동일한 이름의 클래스가 존재하고, 두 클래스 모두 `@Component` 어노테이션을 사용하면 `ConflictingBeanDefinitionException` 예외가 발생한다. 동일한 이름을 사용해야한다면 둘 중 하나는 명시적으로 빈 이름을 다르게 지정해주어야한다.

### 수동 등록한 빈과 충돌

`@Component` 어노테이션을 사용한 클래스를 설정 클래스에서 `@Bean`을 사용하여 수동으로 빈을 등록하면 어떻게 될까? 스캔할 때 사용하는 빈 이름과 수동으로 등록한 빈의 이름이 같다면 수동으로 등록한 빈을 우선 등록한다. 스캔으로 찾은 같은 이름의 빈은 등록되지 않는다.

<br>
<br>


--- 
해당 포스팅은 [초보 웹 개발자를 위한 스프링5 프로그래밍 입문 - 최범균 저](https://www.aladin.co.kr/shop/wproduct.aspx?ItemId=157472828)를 정리하며 작성하였습니다.