---
layout: post
title: "Spring5 : MVC 프로그래밍 (5)"
subtitle: "날짜 값 변환, @PathVariable, 예외처리"
author: "GuGyu"
header-style: text
tags:
  - Spring
  - Spring5
  - Java
  - Study
  - Server
---

[Spring5: MVC 프로그래밍 (4)](https://seonggyu96.github.io/2021/02/23/spring5_chapter13/)  

<br>

## 날짜 값 변환

스프링은 입력 값을 `long`이나 `int`와 같은 기본 데이터 타입으로는 자동으로 변환을 해주지만, `LocalDateTime` 타입으로의 변환은 자동으로 해주지 않는다. 이를 위해서는 별도의 추가 설정이 필요하다.  

폼에 입력한 특정 날짜 값을 가져오기 위한 커맨드 객체를 설계할 때, 다음과 같이 작성할 수 있다.  

```java
public class DateListCommand {
    @DateTimeFormat(pattern = "yyyyMMddHH")
    private LocalDateTime from;

    @DateTimeFormat(pattern = "yyyyMMddHH")
    private LocalDateTime to;
    ...
}
```

커맨드 객체에 `@DateTimeFormat` 어노테이션이 적용되어 있으면 속성으로 지정한 형식을 이용해서 문자열을 `LocalDateTime` 타입으로 변환한다. 예를 들어 위 코드는 "2021030414"를 "2021년 3월 4일 14시" 값을 갖는 `LocalDateTime` 객체로 변환해준다.  

<br>

### LocalDateTime 출력

마찬가지로 `LocalDateTime`을 뷰에 출력할 때에도 추가적인 설정이 필요할 수 있다. 입력 폼과 같은 경우에는 커맨드 객체와 매칭이 되기 때문에 `@DateTimeFormat` 어노테이션이 출력 변환도 맡아준다. 하지만 그 외의 경우, 아쉽게도 `JSTL`이 제공하는 날짜 형식 태그는 LocalDateTime을 지원하지 않기 때문에 커스텀 태그 파일을 작성해야한다.  

```jsp
<%@ tag body-content="empty" pageEncoding="utf-8" %>
<%@ tag import="java.time.format.DateTimeFormatter" %>
<%@ tag trimDirectiveWhitespaces="true" %>
<%@ attribute name="value" required="true" type="java.time.temporal.TemporalAccessor" %>
<%@ attribute name="pattern" type="java.lang.String" %>
<% if (pattern == null) pattern = "yyyy-MM-dd"; %>
<%= DateTimeFormatter.ofPattern(pattern).format(value) %>
```

위 코드를 `~/src/main/webapp/WEB-INF/tags/formatDatTime.tag` 에 저장하도록 하자. 그럼 JSP 에서 `<formatDateTime ~/>`형태로 태그를 사용할 수 있다.

```jsp
<$@ prefix="tf" tagdir="/WEB-INF/tags" %>
...
<table>
    <tr>
        <th>아이디</th><th>이메일</th><th>이름</th><th>가입일</th>
    </tr>
    <c: forEach var="member" items="${members}">
    <tr>
        <td>${member.id}</td>
        <td>${member.email}</td>
        <td>${member.name}</td>
        <td><tf:formatDateTime value="${member.registerDateTime}" pattern="yyyy-MM-dd"/></td>
    </tr>
    </c:forEach>
</table>
...
```

<br>

### 변환 에러 처리  

만약 `@DateTimeFormat`에서 지정한 형식과 다르게 입력하면 어떻게 될까? 이때는 내부적으로 변환하는 과정에서 예외가 발생하므로 400 에러를 발생시킨다. 따라서 이런 경우에 대한 예외 처리가 필요하다.  

```java
@Controller
public class MemberListController {
    ...
    @RequestMapping("/members")
    public String list(
        @ModelAttribute("command") ListCommand listCommand,
        Errors errors, Model model) {
            if (errors.hasErrors()) {
                return "member/memberList";
            }
        }
        ...
    )
    ...
}
```

요청 매핑 어노테이션 적용 메서드가 `Errors` 타입 파라미터를 가질 경우 `@DateTimeFormat`에 지정한 형식에 맞지 않으면 `Errors` 객체에 "typeMismatch" 에러 코드를 추가한다. 따라서 위 처럼 에러 코드가 존재하는지 확인해서 알맞은 처리를 할 수 있다.  

에러 코드로 "typeMismatch"를 추가하므로 메시지 프로퍼티 파일에 해당 메시지를 추가하면 에러 메시지를 보여줄 수 있다. 예를 들어 다음과 같은 메시지를 추가할 수 있다.  

```properties
typeMismatch.java.time.LocalDateTime=잘못된 형식입니다
```

<br>

## 변환 처리에 대한 이해  

`@DateTimeFormat` 어노테이션을 사용하면 지정한 형식의 문자열을 `LocalDateTime` 타입으로 변환해준다는 것은 알겠다. 그런데 누가 언제 문자열을 `LocalDateTime`으로 변환해주는 것일까? 답은 `WebDataBinder`에 있다. 이는 지난 포스팅에서 로컬 범위 `Validator`를 추가할 때에도 언급되었다.  

스프링 MVC는 요청 매핑 어노테이션 적용 메서드와 `DispatcherServlet` 사이를 연결하기 위해 `RequestMappingHandlerAdapter` 객체를 사용한다. 이 핸들러 어댑터 객체는 요청 파라미터와 커맨드 객체 사이의 변환 처리를 위해 `WebDataBinder`를 이용한다.  

`WebDataBinder`는 커맨드 객체를 생성한다. 그리고 커맨드 객체의 프로퍼티와 같은 이름을 갖는 요청 파라미터를 이용해서 프로퍼티 값을 생성한다.  

<img src="https://user-images.githubusercontent.com/57310034/109924472-aade4780-7d03-11eb-9fdd-56e3caee9f16.png"/>

그러나 `WebDataBinding`은 변환 작업을 직접 하지 않고 위 그림 처럼 `ConversionService`에 그 역할을 위임한다. 스프링 MVC를 위한 설정인 `@EnableWebMvc` 어노테이션을 사용하면 `DefaultFormattingConversionService`를 `ConversionService`로 사용한다. `DefaultFormattingConversionService`는 int, long과 같은 기본 데이터 타입뿐만 아니라 `@DateTimeFormat` 어노테이션을 사용한 시간 관련 타입 변환 기능을 제공한다.  

`WebDataBinder`는 `<form:input>`에도 사용된다. `<form:input>` 태그를 사용하면 path 속성에 지정한 프로퍼티 값을 String으로 변환해서 `<input>` 태그의 value 속성 값으로 생성한다. 이때 프로퍼티 값을 String으로 변환할 때 `WebDataBinder`의 `ConversionService`를 사용한다.  

<img src="https://user-images.githubusercontent.com/57310034/109925842-98fda400-7d05-11eb-80b0-fd67157df661.png"/>

<br>

## @PathVariable을 이용한 경로 변수 처리  

ID가 10인 회원의 정보를 조회하기 위한 경로가 다음과 같다고 가정해보자.  

> `http://localhost:8080/members/10`

이 형식의 url을 사용하면 각 회원마다 경로의 마지막 부분이 달라진다. 이렇게 경로의 일부가 고정되어 있지 않고 달라질 때 사용할 수 있는 것이 `@PathVariable`이다. 

```java
@Controller
public class MemeberDetailController {
    ...

    @GetMapping("/members/{id}")
    public String detail(@PathVariable("id") Long memberId, Model model) {
        Member member = memberDao.selectById(memberId);
        ...
    }
    ...
}
```

매핑 경로에 '{경로변수}'와 같이 중괄호로 둘러 쌓인 부분을 경로 변수라고 부른다. 여기에 해당하는 값은 `@PathVariable` 파라미터에 전달된다. 위 코드의 경우 `/members/{id}`에서 {id}에 해당하는 10이 `memberId` 파라미터에 값으로 전달된다.  

### @PathVariable vs @RequestParams

그런데 이전 포스팅에서 `@RequestParams`를 이용하여 요청 경로에 포함된 변수를 사용한 적이 있다. 이 방법과 어떤 차이가 있을까?  

결과물만 보면 아무런 차이가 없다. 결국 원하는 변수가 컨트롤러 메서드로 들어오기 때문에 동일한 처리가 가능하다. 다만 용도에 차이를 두고 이를 구분해서 사용하면 조금 더 체계적이고 효율적인 개발이 가능해진다. 예를 들어 resource를 식별하고 싶으면 `@PathVariable`을 사용하고, 정렬이나 필터링을 한다면 `@RequestParams`을 사용하는 방법이 있다.  

<br>

## 컨트롤러 예외 처리하기  

앞서 소개한 예제에 이어서, 만약 존재하지 않는 memberId를 경로 변수로 사용한다면 어떻게 될까? Service 객체를 어떻게 구현했냐에 따라 다르겠지만 대게 데이터가 존재하지 않는다는 예외를 발생시킬 것이다. 이 경우, 500 에러를 발생시킬 가능성이 높다. 또한 숫자가 아닌 문자를 경로 변수로 사용하면 문자를 `Long` 타입으로 변환할 수 없기 때문에 400 에러가 발생한다.  

전자의 경우는 try-catch 문으로 예외를 먼저 잡고 안내 화면을 보여주면 될 것 같은데, 후자의 경우는 어떻게 해야할까? 이 경우에 유용하게 사용할 수 있는 것이 바로 `@ExceptionHandler` 어노테이션이다. 같은 컨트롤러에 `@ExceptionHandler` 어노테이션을 적용한 메서드가 존재하면 그 메서드가 예외를 처리해준다.  

```java
@Controller
public class MemeberDetailController {
    ...

    @GetMapping("/members/{id}")
    public String detail(@PathVariable("id") Long memberId, Model model) {
        Member member = memberDao.selectById(memberId);
        ...
    }
    
    //타입 변환 예외가 발생하면 해당 메서드를 실행하여 처리
    @ExceptionHandler(TypeMismatchException.class)
    public String handleTypeMismatchException() {
        return "member/invalid";
    }

    //해당하는 데이터를 찾지 못하면 해당 메서드를 실행하여 처리
    @ExceptionHandler(MemberNotFoundException.class)
    public String handleTypeMismatchException() {
        return "member/noMember";
    }
}
```

<br>

### @ControllerAdvice를 이용한 공통 예외 처리  

`@ExceptionHandler` 어노테이션은 해당 컨트롤러에서 발생한 예외만을 처리할 수 있다. 하지만 다수의 컨트롤러에서 동일 타입의 예외가 발생할 수도 있다. 이때 예외 처리 코드가 동일하다면 어떻게 중복 코드를 해결할 수 있을까? 정답은 `@ControllerAdvice`이다.  

```java
@ControllerAdvice("spring")
public class CommonExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException() {
        return "error/commonException";
    }
}
```

`@ControllerAdvice` 어노테이션이 적용된 클래스는 지정한 범위의 컨트롤러에 공통적으로 사용될 설정을 지정할 수 있다. 위 코드를 예로 들면 "spring" 패키지와 그 하위 패키지에 속한 컨트롤러에서 `RuntimeException`이 발생하면 `handleRuntimeException()` 메서드를 통해 예외를 처리한다.  

<br>

### @ExceptionHandler 적용 메서드의 우선 순위  

`@ControllerAdvice` 클래스에 있는 `@ExceptionHandler` 메서드와 컨트롤러 클래스에 적용된 `@ExceptionHandler` 메서드 중에서는 컨트롤러 클래스에 적용된 것이 우선된다. 즉 컨트롤러의 메서드를 실행하는 과정에서 예외가 발생하면 다음 순서로 이를 처리할 메서드를 찾는다.  

1. 같은 컨트롤러에 위치한 `@ExceptionHandler` 메서드 중 해당 예외를 처리할 수 있는 메서드를 검색
2. 같은 클래스에 위치한 메서드가 예외를 처리할 수 없을 경우 `@ControllerAdvice` 클래스에 위치한 `@ExceptionHandler` 메서드를 검색

`@ControllerAdvice` 어노테이션은 공통 설정을 적용할 대상을 지정하기 위해 다음과 같은 속성을 제공한다.  

|속성|타입|설명|
|:--|:--|:--|
|value<br>basePackage|String[]|공통 설정을 적용할 컨트롤러가 속하는 기준 패키지|
|annotations|Class<? extends Annotation>[]|특정 어노테이션이 적용된 컨트롤러|
|assignableTypes|Class<?>[]|특정 타입 또는 그 하위 타입인 컨트롤러 대상|

<br>

### @ExceptionHandler 어노테이션 적용 메서드의 파라미터와 리턴 타입  

`@ExceptionHandler` 어노테이션을 붙인 메서드는 다음 파라미터를 가질 수 있다.  

- `HttpServletRequest`, `HttpServletResponse`, `HttpSession`
- `Model`
- 예외  

리턴 가능한 타입은 다음과 같다.

- `ModelAndView`
- String (뷰 이름)
- (@ResponseBody 어노테이션을 붙인 경우) 임의 객체  
- `ResponseEntity`   

<br>
<br>



--- 
해당 포스팅은 [초보 웹 개발자를 위한 스프링5 프로그래밍 입문 - 최범균 저](https://www.aladin.co.kr/shop/wproduct.aspx?ItemId=157472828)를 정리하며 작성하였습니다.  

<br>

참고 :  

[Path Variable과 Query Parameter는 언제 사용해야 할까?](https://ryan-han.com/post/translated/pathvariable_queryparam/)