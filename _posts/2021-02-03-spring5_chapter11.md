---
layout: post
title: "Spring5 : MVC 프로그래밍 (2)"
subtitle: "요청 매핑, 커맨드 객체, 리다이렉트, 폼 태그, 모델"
author: "GuGyu"
header-style: text
tags:
  - Spring
  - Spring5
  - Java
  - Study
  - Server
  - MVC
  - RequstMapping
  - Redirection
  - FormTag
---

[Spring5: MVC 프로그래밍 (1)](https://seonggyu96.github.io/2021/02/02/spring5_chapter11/)  

## 주요 에러  

#### HTTP Status 404 - Not Found  

발생 원인

1. 요청 경로를 처리할 컨트롤러가 존재하지 않을 때
2. `WebMvcConfigurer`를 이용한 매핑 설정이 없을 때
3. 컨트롤러가 리턴한 뷰 이름에 해당하는 JSP 파일이 존재하지 않을 때

확인 사항  
1. 요청 경로가 올바른가
2. 컨트롤러에 설정한 경로가 올바른가
3. 컨트롤러 클래스를 빈으로 등록하였는가
4. 컨트롤러 클래스에 `@Controller` 어노테이션을 적용했는가  

<br>

#### HTTP Status 405

발생 원인 : 지원하지 않는 전송 방식(method)를 사용한 경우  

<br>

#### HTTP Status 400 - Bad Request

1. 필요한 파라미터의 값이 부재한 경우 : `@ReqeustParam` 어노테이션을 사용하고 있는 파라미터기 필수이면서 기본값이 없을 때, 해당 파라미터를 전송하지 않고 메서드를 호출한다면 400에러가 발생한다.  

2. 요청 파라미터의 값을 메서드에서 정의한 파라미터의 타입으로 변환할 수 없는 경우 : 혹은 Boolean 타입의 파라미터를 필요로 하나, true1 과 같은 문자열로 요청된 경우, 자동으로 타입을 변환할 수 없어 400에러가 발생한다.  

<br>

## 커맨드 객체 : 중첩, 콜렉션 프로퍼티  

스프링 MVC는 커맨드 객체가 리스트 타입의 프로퍼티를 가졌거나 중첩 프로퍼티를 가진 경우에도 요청 파라미터의 값을 알맞게 커맨드 객체에 설정해주는 기능을 제공하고 있다. 다만 이를 가능하게 하도록 하기 위해서 다음의 규칙을 따라야한다.

- HTTP 요청 파라미터 이름이 "프로퍼티이름[인덱스]" 형식이면 List 타입 프로퍼티의 값 목록으로 처리한다.  
- HTTP 요청 파라미터 이름이 "프로퍼티이름.프로퍼티이름"과 같은 형식이면 중첩 프로퍼티 값을 처리한다.  

예를 들어 이름이 data 이고 List 타입인 프로퍼티를 위한 요청 파라미터의 이름으로 "data[0]", "data[1]" 을 사용하면 각각 0번 인덱스와 1번 인덱스의 값으로 사용된다.  

중첩 프로퍼티의 경우는 다음 코드를 살펴보자.  

```java
public class People {
    People parent;
    String name;
}
```

People 타입의 커맨드 객체 안에 또 People 타입의 객체가 중첩되어 프로퍼티로 정의되어 있다. 이때 parent의 이름에 접근하고 싶다면 요청 파라미터의 이름을 "parent.name"으로 지정하면 다음과 유사한 방식으로 커맨드 객체에 파라미터의 값을 설정한다.  

`commandObj.getParent().setName(request.getParameter("parent.name"));`

<br>

## Model을 통해 컨트롤러에서 뷰에 데이터 전달하기  

컨트롤러는 뷰가 응답 화면을 구성하는데 필요한 데이터를 생성해서 전달해야한다. 이때 사용하는 것이 Model이다.  

```java
@Controller
public class HelloController {

    @GetMapping("hello")
    public String hello(Model model, @RequestParam(value = "name", required = false) String name) {
        model.addAttribute("greeting", "안녕하세요, " + name);
        return "hello";
    }
}
```

뷰에 데이터를 전달하려는 컨트롤러는 위 `hello()` 메서드처럼 다음 두 가지를 하면 된다.  

- 요청 매핑 어노테이션이 적용된 메서드의 파라미터 Model을 추가  
- Model 파라미터의 `addAttribute()` 메서드로 뷰에서 사용할 데이터 전달  

`addAttribute()` 메서드의 첫 번째 파라미터는 속성 이름이다. 뷰 코드는 이 이름을 사용해서 데이터에 접근한다. JSP는 `${greeting}`과 같은 표현식을 사용해서 속성값에 접근한다.  

<br>

### ModelAndView를 통한 뷰 선택과 모델 전달  

지금까지 구현한 컨트롤러는 두 가지 특징이 있다.  

- Model을 이용해서 뷰에 전달할 데이터 설정
- 결과를 보여줄 뷰 이름을 리턴  

`ModelAndView` 를 사용하면 이 두 가지를 한 번에 처리할 수 있다. 요청 매핑 어노테이션을 적용한 메서드는 String 타입 대신 `ModelAndView`를 리턴할 수 있다.  

```java
@Controller
public class HelloController {

    @GetMapping("hello")
    public ModelAndView hello(@RequestParam(value = "name", required = false) String name) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("greeting", "안녕하세요, " + name);
        modelAndView.setViewName("hello");
        return modelAndView;
    }
}
```

<br>

## 주요 폼 태그 

스프링 MVC는 `<form:form>`, `<form:input>` 등 HTML 폼과 커맨드 객체를 연동하기 위한 JSP 태그 라이브러리를 제공한다. 이 외에도 다양한 폼 태그들이 제공되는데 하나씩 알아보자.  

### `<form>` 태그를 위한 커스텀 태그 : `<form:form>`

`<form:form>` 커스텀 태그는 `<form>` 태그를 생성할 때 사용된다.  

```jsp
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
...
<form:form>
...
    <input type="submit" value="가입 완료">
</form:form>
```

`<form:form>` 태그의 method 속성과 action 속성을 지정하지 않으면 method 속성값은 "post"로 설정되고 action 속성값은 현재 요청 URL로 설정된다.  

id 속성은 입력 폼의 값을 저장하는 커맨드 객체의 이름을 사용한다. 커맨드 객체 이름이 기본값인 "command"가 아니면 modelAttribute 속성값으로 커맨드 이름을 설정해야한다.  

```jsp
<form:form modelAttribute="loginCommand">
...
</form:form>
```
<br>

`<form: form>` 태그의 몸체는 `<input>` 태그나 `<select>` 태그와 같이 입력 폼을 출력하는데 필요한 HTML 태그를 입력할 수 있다. 이때 입력한 값이 잘못되어 다시 값을 입력해야하는 경우 다음과 같이 커맨드 객체의 값을 사용해서 이전에 입력한 값을 출력할 수 있다.  

```jsp
<form:form modelAttribute="loginCommand">
    ...
    <input type="text" name="id" value="${loginCommand.id}"/>
    ...
</form:form>
```   

<br>

### `<input>` 관련 커스텀 태그

#### `<form:input>`  

`<form:input>` 커스텀 태그는 다음과 같이 path 속성을 사용해서 연결할 커맨드 객체의 프로퍼티를 지정한다.  

```jsp
<form:form modelAttribute="loginRequest" action="login">
    <!-- <input id="emial" name="email" type="text" value=""/> 와 동일-->
    <form:input path="email"/>
    ...
</form:form>
```

기존 `<input>` 태그의 id 속성과 name 속성의 값은 프로퍼티의 이름으로 설정하고 value 속성에는 `<form:input>` 커스텀 태그의 path 속성으로 지정한 커맨드 객체의 프로퍼티 값이 출력된다.  

#### `<form:password>`, `<form:hidden>`

`<form:password>` 커스텀 태그는 password 타입의 `<input>` 태그를 생성하고, `<form:hidden>` 커스텀 태그는 hidden 타입의 `<input>` 태그를 생성한다. 두 태그 모두 path 속성을 사용하여 연결할 커맨드 객체의 프로퍼티를 지정한다.  

```jsp
<form:form modelAttribute="loginCommand">
    <form:hidden path="defaultSecurityLevel"/>
    ...
    <form:password path="password>"/>
</form:form>
``` 

<br>

### `<select>` 관련 커스텀 태그  

#### `<form:select>`  

`<select>`와 `<option>` 태그는 선택 옵션을 제공할 때 주로 사용한다. 일반적으로 많은 옵션을 제공할 때는 옵션 정보를 컨트롤러에서 생성해서 뷰에 전달하는 경우가 많다. 이때 `<form:select>` 커스텀 태그를 사용하면 뷰에 전달한 모델 객체를 갖고 간단하게 `<select>`와 `<option>` 태그를 생성할 수 있다.  

```jsp
<form:form modelAttribute="join">
    <form:select path="job" items="${jobTypes}"/>
    ...
</form:form>
```

path 속성은 커맨드 객체의 프로퍼티 이름을 입력하며 items 속성에는 `<option>` 태그를 생성할 때 사용할 컬렉션 객체를 지정한다.  

#### `<form:options>`

아니면 `<form:option>` 태그를 사용해도 위와 같은 효과를 낼 수 있다. `<form:select>` 커스텀 태그에 `<form:option>` 커스텀 태그를 중첩해서 사용한다. `<form:options>` 커스텀 태그의 items 속성에 값 목록으로 사용할 모델 이름을 설정한다.  

```jsp
<form:select path="jop">
    <form:option items="${jopTypes}">
</form:form>
```

```java
public class Code {
    private String code;
    private String label;
    ...
}
```

위와 같은 컬렉션 객체를 사용한다고 했을 때, 컨트롤러는 코드 목록을 표시하기위해 `List<Code>` 객체를 뷰에 전달한다. 뷰는 Code 객체의 code 프로퍼티와 label 프로퍼티를 각각 `<option>` 태그의 value 속성과 텍스트로 사용해야한다. 이렇게 컬렉션에 저장된 객체의 특정 프로퍼티를 사용하는 경우 itemView 속성과 itemLabel 속성을 사용한다.  

```jsp
<form:select path="jobCode">
    <form:option items="${jobCodes}" itemLabel="label" itemValue="code"/>
</form:form>
```

그럼 보이는 텍스트로는 label에 해당하는 문자열이 보이고, 선택 후 커맨드 객체로 넘겨질 value 값은 code에 해당하는 문자열이 된다.  

`<form:select>` 태그도 itemView, itemLabel 속성을 사용할 수 있다.  

<br>

### 체크박스 관련 커스텀 태그  

#### `<form:checkboxes>`

`<form:checkboxes>` 커스텀 태그는 items 속성을 이용하여 값으로 사용할 컬렉션을 지정한다. path 속성으로 커맨드 객체의 프로퍼티를 지정한다.  

```jsp
<form:checkboxes items="${possibleLanguage}" path="possibleLanguage"/>
```

여기서도 컬렉션 객체가 String이 아닐 경우 itemValue, itemLabel 속성을 이용해서 값과 텍스트로 사용할 객체의 프로퍼티를 지정할 수 있다.  

#### `<form:checkbox>`

`<form:checkbox>` 커스텀 태그는 한 개의 checkbox를 생성할 때 사용한다.  

```java
<form:checkbox path="possibleLanguste" value="java" label="자바">
```

`<form:checkbox>` 커스텀 태그는 연결되는 값 타입에 따라 처리 방식이 달라진다. 컬렉션 객체가 boolean 타입의 프로퍼티를 포함하고 path 속성에 해당 프로퍼티를 설정해주면 checked 속성을 사용한다. 프로퍼티의 값이 true이면 checked 속성이 추가되고 false 이면 추가되지 않는다.  

<br

### 라디오버튼 관련 커스텀 태그  

#### <form:radiobuttons>  

`<form:radiobuttons>` 커스텀 태그는 다음과 같이 items 속성에 값으로 사용할 컬렉션을 전달받고 path 속성에 커맨드 객체의 프로퍼티를 지정한다.  

```jsp
<form:radiobuttons items="${favoritIDEs} path="pavoritIDE">
```

#### `<form:radiobutton>`

`<form:radiobutton>` 커스텀 태그는 1개의 라디오 타입 태그를 생성할 때 사용하며 value 속성과 label 속성을 이요하여 값과 텍스트를 설정한다. 사용 방법은 `<form:checkbox>` 태그와 동일하다.  

<br>

### `<textarea>` 태그를 위한 커스텀 태그  

#### `<form:textarea>`

게시글 내용과 같이 여러 줄을 입력받아야하는 경우 `<form:textarea>` 커스텀 태그를 사용할 수 있다.  

```jsp
<form:textarea path="etc" cols="20" rows="3"/>
```

<br>

### CSS 및 HTML 태그와 관련된 공통 속성  

`<form:input>`, `<form:select>` 등 입력 폼과 관련해서 제공하는 스프링 커스텀 태그는 HTML의 CSS 및 이벤트 관련 속성을 제공하고 있다. 먼저 CSS와 관련된 속성은 다음과 같다.  

- cssClass : HTML 의 class 속성값
- cssErrorClass : 폼 검증 에러가 발생했을 때 사용할 HTML의 class 속성값
- cssStyle : HTNL 의 style 속성값  

스프링은 폼 검증 기능을 제공하는데 이는 다음 챕터에서 다뤄본다.  

HTNL 태그가 사용하는 다음 속성들도 모두 사용 가능하다.  

- id, title, dir
- disabled, tabindex
- onfocus, onblur, onchange
- onclick, ondblclick
- onkeydown, onkeypress, onkeyup
- onmousedown, onmousemove, onmouseup
- onmouseout, onmouseover

또한 각 커스텀 태그는 htmlEscape 속성을 사용해서 커맨드 객체의 값이 포함된 HTML 특수 문자를 엔타티 레퍼런스로 변환할지 결정할 수 있다.  









<br>
<br>


--- 
해당 포스팅은 [초보 웹 개발자를 위한 스프링5 프로그래밍 입문 - 최범균 저](https://www.aladin.co.kr/shop/wproduct.aspx?ItemId=157472828)를 정리하며 작성하였습니다.  
