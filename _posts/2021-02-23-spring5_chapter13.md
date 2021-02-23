---
layout: post
title: "Spring5 : MVC 프로그래밍 (4)"
subtitle: "세션, 인터셉터, 쿠키"
author: "GuGyu"
header-style: text
tags:
  - Spring
  - Spring5
  - Java
  - Study
  - Server
  - HttpSession
  - HandlerInterceptor
---

[Spring5: MVC 프로그래밍 (3)](https://seonggyu96.github.io/2021/02/16/spring5_chapter12/)  

<br>

## 로그인 상태 유지 

HTTP 프로토콜은 클라이언트와 서버와의 관계를 유지하지 않는 `Stateless` 기반의 프로토콜이다. 하지만 대부분의 웹 서비스에서는 로그인을 통해 연결 상태 및 특정 데이터(장바구니 등)을 유지해야한다. 

로그인 상태를 유지하는 방법은 크게 다음과 같다.  

- HttpSession
- 쿠키  

### HttpSession  

컨트롤러에서 `HttpSession`을 사용하려면 다음의 두 방법 중 한 가지를 사용하면 된다.  

- 요청 매핑 어노테이션 적용 메서드에 `HttpSession` 파라미터를 추가한다.  
- 요청 매핑 어노테이션 적용 메서드에 `HttpServletRequest` 파라미터를 추가하고 `HttpSession`을 구한다.  

```java
//#1 - 항상 HttpSession을 생성함
@PostMapping
public String form(LoginCommand loginCommand, HttpSession session) {
    ...// session을 사용하는 코드
}

//#2 - 필요한 시점에만 HttpSession을 생성할 수 있음
@PostMapping
public String submit(
    LoginCommand loginCommand, HttpServletRequest request) {
    HttpSession session = request.getSession();
    ...// session을 사용하는 코드
}
```

첫 번째 방법을 사용하여 로그인에 성공하면 인증 정보를 저장하도록 하는 예제를 살펴보자.  

```java
//인증 정보 객체
public class AuthInfo {
    private Long id;
    private String email;
    private String name;
    ...
}

//인증 관련 서비스
public class AuthService {
    ...
    public AuthInfo authenticate(String email, String password) {
        Member member = memberDao.selectByEmail(email);
        if (member == null) {
            throw new WrongIdPasswordException();
        }
        if (!member.matchPassword(password)) {
            throw new WrongIdPasswordException();
        }
        return new AuthInfo(member.getId()), member.getEmail(), emeber.getName());
    }
}

//로그인 컨트롤러
@Controller
@RequeestMapping("/login")
public class LoginController {
    @AutoWired
    private AuthService authService;
    ...
    @PostMapping
    public String submit(LoginCommand loginCommand, HttpSession session) {
        try {
            AuthInfo authInfo = authService.authenticate(
                loginCommand.getEmail(),
                loginCommand.getPassword()
            );
            //로그인에 성공하면 "authInfo" 속성에 인증 정보 객체를 저장
            session.setAttribute("authInfo", authInfo);

            return "login/loginSuccess";
        } catch ...
    }
}
```

그리고 로그아웃을 위한 컨트롤러에서는 `HttpSession`을 제거하면 된다.  

```java
@Controller
public class LogoutController {

    @RequestMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/main";
    }
}
```  

`HttpSession` 객체를 jsp 에서 활용하는 예제는 다음과 같다.  

```jsp
<%@ taglib prefix="c" url="http://java.sun.com/jsp/jstl/core" %>
...
<body>
    <!-- 로그인을 하지 않은 경우 -->
    <c:if test="${empty authInfo}">
    <p> 환영합니다.</p>
    ...
    </c:if>

    <!-- 로그인에 성공한 경우 -->
    <c:if test="${! empty authInfo}">
    <p> ${authInfo.name}님, 환영합니다.</p>
    ...
    </c:if>
</body>
...
```  

사용자의 로그인 정보가 보괸 및 유지되고 있는 `HttpSession` 객체는 jsp 뿐만 아니라 컨트롤러에서도 사용가능하다. 이는 활용할 수 있는 예는 비밀번호 수정이다.  

```java
@Controller
@RequestMapping("/edit/changePassword")
public class ChangePasswordController {
    ...
    @PostMapping
    public String submit(
        @ModelAttribute("command") ChangePasswordCommand command,
        HttpSession session
    ) {
        //현재 로그인 정보 가져오기
        AuthInfo authInfo = (AuthInfo) session.getAttribute("authInfo");
        try {
            changePasswordService.changePassword(
                //현재 로그인 정보의 이메일 가져오기
                authInfo.getEmail(),
                command.getCurrentPassword(),
                command.getNewPassword()
            );
            return "edit/changePassword";
        } catch ...
    }
}
```

위 처럼 `HttpSession` 객체에서 현재 로그인 된 계정의 이메일을 가져오기 때문에 사용자는 비밀번호를 변경할 때 이메일을 입력하지 않아도 된다.  

<br>

## 인터셉터  

로그인을 하지 않은 상태에서 비밀번호 변경 페이지 등의 주소를 직접 입력하여 접근할 수 있으면 안된다. 그보다는 로그인화면으로 이동시키는 것이 좋다.  

이를 위해 `HttpSession`에 "authInfo" 객체가 존재하는지 검사하고 존재하지 않으면 로그인 경로로 리다이렉트하도록 할 수 있다.  

그러나 웹 서비스에서 로그인을 해야만 이용할 수 있는 기능은 매우 많다. 해당하는 모든 컨트롤러에서 "authInfo" 객체 존재 여부를 검사하는 것은 중복 코드를 발생시킨다.  

이렇게 다수의 컨트롤러에 대해 동일한 기능을 적용해야할 때 사용할 수 있는 것이 `HandlerInterceptor`이다.  

<br>

### HandlerInterceptor 구현

`HandlerInterceptor` 인터페이스를 사용하면 다음의 세 시점에 공통 기능을 넣을 수 있다.  

- 컨트롤러(핸들러) 실행 전
- 컨트롤러(핸들러) 실행 후, 아직 뷰를 실행하기 전
- 뷰를 실행한 이후  

위 세 시점을 처리하기 위한 인터페이스 메서드는 다음과 같다.  

```java
boolean preHandle(
    HttpServletRequest request,
    HttpServletResponse response,
    Object Handler
) throws Exception;  

void postHandle(
    HttpServletRequest request,
    HttpServletResponse response,
    Object handler,
    ModelAndView modelAndView
) throws Exception;

void afterCompletion(
    HttpServletRequest request,
    HttpServletResponse response,
    Object handler,
    Exception exception
) throws Exception;  
```  

#### preHandle()  

`preHandle()` 메서드는 컨트롤러(핸들러) 객체를 실행하기 전에 필요한 기능을 구현할 때 사용한다. 이 메서드를 사용하면 로그인을 하지 않은 경우에 컨트롤러를 실행하지 않거나 컨트롤러를 실행하기 전에 필요한 정보를 생성하는 것이 가능하다. handler 파라미터는 웹 요청을 처리할 컨트롤러 객체이다.  

리턴 타입은 `boolean`인데, `preHandle()` 메서드가 `false`를 리턴하면 컨트롤러(또는 다음 `HandlerInterceptor`)를 실행하지 않는다.  

#### postHandler()

`postHandler()` 메서드는 컨트롤러가 "정상적으로" 실행된 이후에 추가 기능을 구현할 때 사용한다. 만약 컨트롤러가 예외를 발생시키면 해당 메서드는 실행되지 않는다.  

#### afterCompletion()  

`afterCompletion()` 메서드는 뷰가 클라이언트에 응답을 전송한 뒤에 실행된다. 컨트롤러 실행 과정에서 예외가 발생하면 네 번째 파라미터로 전달이 된다. 예외가 발생하지 않으면 null이 전달된다.  

컨트롤러 실행 이후에 예상치 못하게 발생한 예외를 로그로 남긴다거나 실행 시간을 기록하는 등의 후처리를 하기에 적합한 메서드이다.  

<br>

`HandlerInterceptor`와 컨트롤러의 실행 흐름을 대략적인 그림으로 보면 다음과 같다.  

<img src="https://user-images.githubusercontent.com/57310034/108811321-65799600-75f0-11eb-9eb6-a427e12aac09.png"/>

`HandlerIntercepter` 인터페이스의 각 메서드는 아무 기능도 구현하지 않은 자바8의 디폴트 메서드이다. 따라서 위 메서드들을 모두 구현할 필요는 없고 필요한 메서드만 재정의하면 된다.  

처음으로 돌아가, 비밀번호 변경 기능에 접근할 때 `HandlerIntercepter`를 사용하면 로그인 여부에 따라 로그인 폼으로 보내거나 컨트롤러를 실행하도록 구현할 수 있다.  

```java
public class AuthCheckInterceptor implements HandlerIntercepter {

    @Override
    public boolean preHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler
    ) throws Exception {
        //세션이 존재하지 않으면 null을 반환하도록 함
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object authInfo = session.getAttribute("authInfo");
            if (authInfo != null) {
                //세션이 존재하고 authInfo 객체가 있다면 컨트롤러 메서드 실행
                return true;
            }
        }
        //세션이나 authInfo 객체가 존재하지 않으면 로그인 화면으로 리다이렉트
        response.sendRedirect(request.getContextPath() + "/login");
        //컨트롤러 메서드를 실행하지 않음  
        return false;
    }
}
```

참고로 `request.getContextPath()`는 현재 컨텍스트 경로를 리턴한다. 예를 들어 웹 어플리케이션 경로가 `http://localhost:8080/spring` 이면 컨텍스트 경로는 `/spring`이 된다. 따라서 위 코드는 `/spring/login`으로 리다이렉트하라는 응답을 전송한다.  

<br>

### HandlerInterceptor 설정

`HandlerInterceptor`를 구현했으면 어디에 적용할지 설정을 해야 동작한다. 이는 설정 클래스에서 설정할 수 있다.  

```java
@Configuration
@EnableWebMVC
public class MvcConfig implements WebMvcConfigurer {
    ...

    //HandlerInterceptor를 구현한 클래스를 빈으로 등록
    @Bean
    public AuthCheckInterceptor authCheckInterceptor() {
        return new AuthCheckInterceptor();
    }

    //HandlerIntercepter를 설정
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authCheckInterceptor())
            //인터셉터를 적용할 경로를 설정
            .addPathPattern("/edit/**");
    }
}
```

인터셉터를 적용할 경로는 Ant 경로 패턴을 사용한다. 두 개 이상의 경로 패턴을 지정하려면 각 경로 패턴을 콤마로 구분해서 지정한다. 위 코드에서는 `/edit/` 경로의 하위에 존재하는 모든 컨트롤러에 인터셉터가 적용된다.  

만약 `addPathPatterns()` 메서드에 지정한 경로 패턴 중 일부를 제외하고 싶다면 `excludePathPatterns()` 메서드를 사용하면 된다.  

#### Ant 경로 패턴  

Ant 경로 패턴은 "*, **, ?" 세 가지 특수문자를 이용해서 경로를 표현한다.  

- *: 0개 또는 그 이상의 글자  
- ?: 1개의 글자
- **: 0개 또는 그 이상의 폴더 경로  


<br>  

## 쿠키  

쿠키는 사용자의 편이를 위해 아이디를 기억해두었다가 다음에 로그인할 때 아이디를 자동으로 넣어주는 등의 기능에 사용된다.  

쿠키는 서버가 아닌 클라이언트에 저장되는 데이터이기 때문에 서버의 재부팅 여부와 관계없이 유지될 수 있다. 따라서 서버는 클라이언트에 저장된 쿠키를 받아와 사용하는데, 이 과정은 다음과 같다.  

1. 클라이언트가 서버에 HTTP Request를 보냄
2. 서버는 쿠키를 설정하고 응답객체 헤더에 쿠키를 담아 보냄
3. 이후 클라이언트가 HTTP Request를 보낼 때 마다 가지고 있는 쿠키를 헤더에 함께 보냄

쿠키는 4kb 정도의 아주 작은 데이터이다. 쿠키에 담을 수 있는 데이터는 여러가지가 있지만 여기서는 Name과 Value 두 가지만 알아보도록 하자.  

<br>

스프링 MVC에서 쿠키를 사용하는 방법 중 하나는 `@CookieValue` 어노테이션을 사용하는 것이다. 이 어노테이션은 요청 매핑 어노테이션 적용 메서드의 `Cookie` 타입 파라미터에 적용한다. 그럼 스프링이 쿠키를 해당 파라미터로 전달해준다.  

```java
@Controllere
@RequestMapping("/login")
public class LoginController {
    ...
    @GetMapping
    public String form(
        LoginCommand loginCommand,
        //"REMEMBER" 이름의 쿠키를 전달 받음
        @CookieValue(value = "REMEMBER", required = false)
        Cookie rCookie
    ) {
        if (rCookie != null) {
            //쿠키에 보관된 값들로 커맨드 객체를 채움
            loginCommand.setEmail(rCookie.getValue());
            loginCommand.setRememberEmail(true);
        }
        return "login/loginForm";
    }
}
```

`@CookieValue` 어노테이션의 `required` 속성의 기본 값은 `true`이며, 해당하는 쿠키가 없을 경우 예외를 발생한다. `false`로 설정하면 쿠키가 없어도 null을 전달할 뿐 예외는 발생하지 않는다.  

그렇다면 이 쿠키를 생성하는 곳은 어딜까? 바로 로그인을 처리하는 컨트롤러 메서드이다. 쿠키를 생성하려면 `HttpSevletResponse` 객체가 필요하므로 해당 메서드의 파라미터에 추가한다.  

```java
@Controllere
@RequestMapping("/login")
public class LoginController {
    ...
    @PostMapping
    public String sumbit(
        LoginCommand loginCommand,
        HttpSession session,
        HttpServletResponse response
    ) {
        try {
            ...

            Cooke rememberCookie =
                new Cookie("REMEMBER", loginCommand.getEmail());
            rememberCookie.setPath("/");
            //이메일 기억하기 체크 여부
            if (loginCommand.isRememberEmail()) {
                //30일 동안 유지되는 쿠키를 생성
                rememberCookie.setMaxAge(60 * 60 * 24 * 30);
            } else {
                //바로 소멸되는 쿠키를 생성
                rememberCookie.setMaxAge(0);
            }
            response.addCookie(rememberCookie);

            return "login/loginSuccess"
        } catch ...
    }
```

<br>
<br>


--- 
해당 포스팅은 [초보 웹 개발자를 위한 스프링5 프로그래밍 입문 - 최범균 저](https://www.aladin.co.kr/shop/wproduct.aspx?ItemId=157472828)를 정리하며 작성하였습니다.  

<br>

참고 :  
[세션과 쿠키](https://lazymankook.tistory.com/35)