---
layout: post
title: "Spring5 : MVC 프로그래밍 (1)"
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

스프링 MVC를 사용해서 웹 어플리케이션을 개발한다는 것은 결국 컨트롤러와 뷰 코드를 구현한다는 것을 뜻한다. 대부분의 설정은 개발 초기에 완성되고 개발이 완료될 때까지 개발자가 작성해야하는 코드는 컨트로럴와 뷰 코드이다. 어떤 컨트롤러를 이용해서 어떤 요청 경로를 처리할지 결정하고, 웹 브라우저가 전송한 요청에서 필요한 파라미터를 구하고, 처리 결과를 JSP를 이용해서 보여주면 된다.  

<br>

## 요청 매핑 어노테이션을 이용한 경로 매핑

웹 어플리케이션을 개발하는 것은 다음 코드를 작성하는 것이다.  
- 특정 요청 URL을 처리할 코드
- 처리결과를 HTML과 같은 형식으로 응답하는 코드  

이 중 첫 번째는 `@Controller` 어노테이션을 사용한 컨트롤러 클래스를 이용해서 구현한다. 컨트롤러 클래스는 요청 매핑 어노테이션을 사용해서 메서드가 처리할 요청 경로를 지정한다.  

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

하나의 컨트롤러 클래스에 요청 매핑 어노테이션을 적용한 메서드를 두 개 이상 정의할 수도 있다. '약관 동의' -> '회원 정보 입력' -> '가입 완료' 의 과정을 따르는 회원가입 기능을 예로 들어보면 각 과정을 다음과 같은 URL로 정의해볼 수 있을 것이다.  

- 약관 동의 화면 : http://localhost:8080/[컨텍스트경로]/register/tos
- 회원 정보 입력 화면 : http://localhost:8080/[컨텍스트경로]/register/enter_profile
- 가입 처리 결과 화면 : http://localhost:8080/[컨텍스트경로]/register/done  

이렇게 여러 단계를 거쳐 하나의 기능이 완성되는 경우, 관련 요청 경로를 한 개의 컨트롤러 클래스에서 처리하면 코드 관리에 도움이 된다.  

```java
@Controller
public class RegisterController {

    @RequestMapping("/register/tos")
    public String handleTos() {
        ...
    }

    @PostMapping("/register/enter_profile")
    public String handleEnterProfile(
        ...
    }

    @PostMapping("/register/done")
    public String handleDone(RegisterRequest registerRequest) {
        ...
    }
}
```

위 예시 코드를 보면 각 요청의 경로가 "/register"로 시작한다. 이 경우 다음 코드처럼 공통되는 부분의 경로를 담은 `@RequestMapping` 어노테이션을 클래스에 적용하고 각 메서드는 나머지 경로를 값으로 갖는 요청 매핑 어노테이션을 적용할 수 있다.  

```java
@Controller
@RequestMapping("/register") //공통 경로
public class RegistController {

    @RequestMapping("/tos") //나머지 경로
    public String handleTos() {
        ...
        return "register/tos"; //리턴 값에는 적용 안됨
    }
    ...
}
```

<br>

## GET과 POST 구분 : @GetMapping, @PostMapping  

스프링 MVC는 별도 설정이 없으면 GET과 POST 방식에 상관없이 `@RequestMapping`에 저정한 경로와 일치하는 요청을 처리한다. 만약 각각의 방식 요청을 따로 처리하고 싶다면 다음과 같이 `@PostMapping`, `@GetMapping` 어노테이션을 사용해서 제한할 수 있다.  

```java
@controller
public class LoginController {
    @GetMapping("member/login")
    public String form() {
        ...
    }

    @PostMapping("/member/login")
    public String login() {
        ...
    }
}
```  

이렇게 어노테이션을 사용하면 같은 경로에 대해 GET과 POST 방식을 각각 다른 메서드가 처리하도록 설정할 수 있다.  

뿐만 아니라 `@PutMapping`, `@DeleteMapping`, `@PatchMapping` 어노테이션도 제공되니, GET, POST, PUT, DELETE, PATCH에 대한 매핑을 제한훌 수 있다.  

<br>

## 요청 파라미터 접근

컨트롤러 메서드에서 요청 파라미터를 사용하는 방법은 다음과 같다.  

#### HttpServletRequest  

컨트롤러 처리 메서드의 파라미터로 `HttpServletRequest`를 사용하고, `HttpServletRequest#getParameter()` 메서드를 이용해서 파라미터의 값을 구할 수 있다.  

```java
@Controller
@RequestMapping("/request")
public class RegisterController {
    ...
    @PostMapping("enter_profile")
    public String handleEnterProfile(HttpServletRequest request) {
        String agreeParam = request.getParameter("agree");
        if (agreeParam == null || !agreeParam.equals("true")) {
            return "register/tos";
        }
        return "register/enter_profile";
    }
    ...
}
```  

#### @RequestParam

또 다른 방법은 `@RequestParam` 어노테이션을 사용하는 것이다. 요청 파라미터 개수가 몇 개 안되면 해당 어노테이션을 사용해서 간단하게 요청 파라미터의 값을 구할 수 있다.  

```java
@Controller
@RequestMapping("/request")
public class RegisterController {
    ...
    @PostMapping("/enter_profile")
    public String handleEnterProfile(
            @RequestParam(value = "agree", defaultValue = "false") Boolean agree) {
        if (!agree) {
            return "register/tos";
        }
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register/enter_profile";
    }
    ...
}
```

`@RequestParam` 어노테이션의 속성은 다음과 같다.  

|속성|타입|설명|
|:--|:--|:--|
|value|String|HTTP 요청 파라미터의 이름을 지정한다.|
|required|boolean|필수 여부를 지정한다. 이 값이 true이면서 해당 요청 파라미터에 값이 없으면 익셉션이 발생한다. 기본 값은 true이다.|
|defaultValue|String|요청 파라미터가 값이 없을 때 사용할 문자열 값을 지정한다. 기본 값은 없다.|  

`defaultValue` 속성의 타입은 String 이지만, 원하는 요청 파라미터의 타입에 따라 자동으로 변환해준다. 다만 이 경우, 래퍼 타입에 대한 변환을 지원하기 때문에 위 예시 코드에서도 `Boolean agree`로 요청 파라미터를 받고 있다.  

<br>

## 리다이렉트 처리  

POST 방식의 요청으로만 접근할 수 있는 경로를 웹 브라우저에 직접 입력하면 어떻게 될까? 주소창에 경로를 직접 입력하는 것은 GET 요청이므로 이를 처리할 수 있는 메서드가 없어서 405 에러를 발생시킨다.  

잘못된 전송 방식으로 요청이 왔을 때 에러 화면을 띄우는 것보다 알맞은 경로로 리다이렉트하는 것이 더 좋을 것이다. 예를 들어 약관동의를 거쳐서 회원 정보 입력 화면으로 넘어가야하지만, 주소 입력으로 회원 정보 입력 화면으로 접속을 시도하면 에러가 발생할 것이다. 약관 동의 여부를 포함한 POST 요청만 핸들링할 수 있기 때문이다. 이 경우, 에러 화면 대신 약관 동의 화면으로 이동하도록 구현하면 조금 더 유연한 플로우가 될 수 있다.  

```java
@Controller
@RequestMapping("/register")
public class RegisterController {
    ...
    @GetMapping("enter_profile")
    public String handleEnterProfileGet() {
        return "redirect:/register/tos"; //약관 동의 화면으로 리다이렉트
    }
}
```

위 처럼 구현하면 "~/register/enter_profile" 주소를 입력했다하더라도 "~/register/tos" 주소로 리다이렉트되는 것을 확인할 수 있다.  

리다이렉트 경로를 /로 시작하지않으면 현재 경로를 기준으로 상대 경로를 사용한다. 예를 들어 `return "redirect:tos"`을 리턴하면 현재 요청 경로인 "~register/enter_prifile" 를 기준으로 상대 경로인 "~/register/tos" 을 리다이렉트 경로로 사용한다.  

물론 "redirect:http://localhost:8080/~"으로 완전한 URL을 사용할 수도 있다.  

<br>

## 커맨드 객체를 이용해서 요청 파라미터 사용하기  

경우에 따라서 요청 파라미터의 개수가 상당히 늘어날 수 있다. 가령 20개를 넘어가는 파라미터를 처리해야하는 경우, 위에서 소개한 `HttpServletRequest` 객체를 사용하거나 `@RequesetParam`을 사용하면 코드가 상당히 길고 복잡해진다.  

스프링은 이런 불편함을 줄이기 위해 요청 파라미터의 값을 커맨드 객체에 담아주는 기능을 제공한다. 예를 들어 이름이 name인 요청 파라미터의 값을 커맨드 객체의 `setName()` 메서드를 사용해서 커맨드 객체에 전달할 수 있다.  

커맨드 객체라고 해서 특별한 코드를 작성하거나 인터페이스를 구현해야하는 것은 아니고, 요청 파리미터의 값을 전달받을 수 있는 세터 메서드를 포함하는 객체를 커맨드 객체로 사용하면 된다.

예를 들어 회원 가입 화면에서 사용자가 입력하는 정보가 다음과 같다고 하자.
- email
- name
- password
- confirmPassword  

이 경우에는 아래와 같은 객체를 커맨드 객체로 사용할 수 있다.

```java
public class RegisterRequest {

    private String email;
    private String password;
    private String confirmPassword;
    private String name;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPasswordEqualToConfirmPassword() {
        return password.equals(confirmPassword);
    }
}
```

<br>

그리고 커맨드 객체는 요청 매핑 어노테이션이 적용된 메서드의 파라미터에 위치시키면 된다.

```java
@PostMapping("/done")
public String handleDone(RegisterRequest registerRequest) {
    ...
}
```

그럼 스프링이 커맨드 객체의 세터 메서드를 이용해서 파라미터를 초기화하고 요청 처리 메서드의 파라미터로 전달해준다.  

<br>

## JSP 코드에서 커맨드 객체 사용하기  

회원 가입할 때 입력했던 정보를 완료 화면에서 다시 한 번 보여주면 조금 더 친절한 화면을 구성할 수 있다. 이때 커맨드 객체를 사용해서 정보를 표시할 수도 있다.  

```jsp
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>회원가입</title>
</head>
<body>
<p><strong>${registerRequest.name}님</strong>
    회원 가입을 완료했습니다.</p>
</body>
</html>
```

`${registerRequest.name}` 코드에서 registerRqeuest가 커맨드 객체에 접근할 때 사용한 속성 이름이다. 스프링 MVC는 커맨드 객체의 (카멜 표기법 형태의) 클래스 이름과 동일한 속성 이름을 사용해서 커맨드 객체를 뷰에 전달한다.  

아까 예시 코드에서 해당 JSP를 리턴하는 컨트롤러 메서드가 `RegisterRequest` 타입의 커맨드 객체를 파라미터로 받아 처리하고 있기 때문에 이 JSP 코드는 `registerRequest` 라는 이름을 사용해서 커맨드 객체에 접근할 수 있다.  

<br>

### @ModelAttribute 어노테이션으로 커맨드 객체 속성 이름 변경  

커맨드 객체에 접근할 때 사용할 속성 이름을 변경하고 싶다면 커맨드 객체로 사용할 파라미터에 `@ModelAttribute` 어노테이션을 적용하면 된다.  

```java
@PostMapping("/done")
public String handleDone(@ModelAttribute("formData") RegisterRequest registerRequest) {
    ...
}
```

`@ModelAttribute` 어노테이션은 모델에서 사용할 속성 이름을 값으로 설정한다. 위 설정을 사용하면 JSP 코드에서 "formData"라는 이름으로 커맨드 객체에 접근할 수 있다.  

<br>  

### 커맨드 객체와 스프링 폼 연동 

회원 정보 입력 폼에서 중복된 이메일 주소를 입력하면 다음 과정으로 넘어가지 못하고 동일한 화면이 갱신된다. 그럼 입력한 폼들이 비워져 다른 폼들까지 새로 입력해야하는 번거로움이 생긴다. 따라서 다시 폼을 보여줄 때 커맨드 객체의 값을 폼에 채워주면 이런 불편함을 해소할 수 있다.  

```html
<input type="text" name="email" id="email" value="${registerRequest.email}">
...
``` 

<br>

그러나 스프링 MVC가 제공하는 커스텀 태그를 사용하면 좀 더 간단하게 커맨드 객체의 값을 출력할 수 있다. 스프링은 `<form:form>` 태그와 `<form:input>` 태그를 제공하고 있다. 이 두 태그를 사용하면 커맨드 객체의 값을 폼에 출력할 수 있다.  

```jsp
<%@ page contentType="text/html; charset=utf-8" %>
<!-- 스프링이 제공하는 폼 태그를 사용하기 위한 taglib 디렉티브 설정-->
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html>
<head>
    <title>회원가입</title>
</head>
<body>
<h2>회원 정보 입력</h2>
<form:form action="done" modelAttribute="registerRequest">
    <p>
        <label>이메일:<br>
            <form:input path="email"/>
        </label>
    </p>
    <p>
        <label>이름:<br>
            <form:input path="name"/>
        </label>
    </p>
    <p>
        <label>비밀번호:<br>
            <form:password path="password"/>
        </label>
    </p>
    <p>
        <label>비밀번호 확인:<br>
            <form:password path="confirmPassword"/>
        </label>
    </p>
    <input type="submit" value="가입 완료">
</form:form>
</body>
</html>
```

위와 같이 taglib 디렉티브를 설정해주면 스프링이 제공하는 `<form>` 태그를 사용할 수 있다. `<form:form>` 태그는 HTML의 `<form>` 태그를 생성하는데, `<form:form>` 태그의 속성은 다음과 같다.  

- action : `<form>` 태그의 action 속성과 동일한 값을 사용한다.
- modelAttribute : 커맨드 객체의 속성 이름을 지정한다. 설정하지 않는 경우 "command"를 기본 값으로 사용한다.  

`<form:input>` 태그는 HTML의 `<input>` 태그를 생성한다. path로 지정한 커맨드 객체의 프로퍼티를 `<input>` 의 value 속성값으로 사용한다. `<form:password>` 도 마찬가지로 password 타입의 input을 생성한다.  

`<form:form>` 태그를 사용하려면 매칭할 커맨드 객체가 필요한데, 이는 컨트롤러 메서드에서 제공해줄 수 있다.  

```java
@Controller
@RequestParam("/request")
public class RegisterController {
    ...
    @PostMapping("/enter_profile")
    public String handleEnterProfile(
            @RequestParam(value = "agree", defaultValue = "false") boolean agree,
            /* 추가 */Model model) {
        if (!agree) {
            return "register/tos";
        }
        // 커맨드 객체 넘겨주기
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register/enter_profile";
    }
    ...
}
```

<br>

## 컨트롤러 구현 없는 경로 매핑  

별도 데이터 처리가 필요없는 화면의 경우, 컨트롤러 클래스에서 특별히 처리할 것이 없기 때문에 단순히 JSP 이름만 리턴하도록 구현할 것이다. 단순 연결을 위해 특별한 로직이 없는 컨트롤러 클래스를 만드는 것 보다는 `WebMvcConfigurer` 인터페이스의 `addViewControllers()` 메서드를 사용하면 좋다.  

```java
@Override
public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/main").setViewName("main");
}
```

"~/main" 경로의 요청은 별다른 데이터 처리 없이 바로 "main" 이름을 가진 화면을 보여주게 될 것이다.

<br>
<br>


--- 
해당 포스팅은 [초보 웹 개발자를 위한 스프링5 프로그래밍 입문 - 최범균 저](https://www.aladin.co.kr/shop/wproduct.aspx?ItemId=157472828)를 정리하며 작성하였습니다.  
