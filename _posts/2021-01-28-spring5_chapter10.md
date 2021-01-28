---
layout: post
title: "Spring5 : 스프링 MVC 프레임워크"
subtitle: "Spring5 study (10)"
author: "GuGyu"
header-style: text
tags:
  - Spring
  - Spring5
  - Java
  - Study
  - Server
  - MVC
---

## MVC 패턴의 등장

웹 어플리케이션을 개발함에 있어 [Servlet과 JSP](https://seonggyu96.github.io/2021/01/27/servlet_jsp/)를 많이 사용한다. 이를 통해 사용자가 웹 서버에 Http 요청을 보내면 웹 어플리케이션 서버가 요청을 전달받아 데이터를 처리하여 동적으로 웹 페이지를 만들어 돌려준다. 따라서 사용자는 자신이 요청한 주소와 파라미터에 따라 그때 그때 동적으로 생성된 웹 페이지를 사용할 수 있게 된다.  

서블릿은 Http 요청을 처리하는 행위들을 작성해둔 자바 클래스로, DB 접근 및 각종 연산을 수행할 수 있다. 게다가 HTML 문법을 포함하고 있어서 데이터 처리의 결과를 HTML 문서로 만들 수도 있다. 그러나 서블릿은 `.java` 파일이기 때문에 HTML 문법을 삽입하는 것이 상당히 번거로웠고, 이를 해결하기 위해 `JSP` 라는 기술을 도입하였다.  

JSP는 서블릿과 반대로 `.html` 파일에 자바 코드를 삽입하여 복잡한 웹 페이지 레이아웃을 한결 편하게 작성할 수 있다. 그리고 해당 웹 페이지를 구성하기 위한 동적인 행위를 자바로 작성하여 삽입할 수도 있다. JSP로 요청을 처리하려면 서블릿 클래스의 형태로 다시 변환해야하는 번거로움이 있지만 최초 변환 후에는 변환과 컴파일이 완료된 파일을 재사용하므로 서블릿을 사용하는 것과 다를 바가 없다.  

이렇게 서블릿과 JSP는 요청을 처리하는 동일한 역할을 하고 있으나 서블릿은 데이터 처리 및 비즈니스 로직과의 연동에 더 용이하고, JSP는 웹 페이지의 레이아웃을 구성하기에 더 용이했다. 따라서 이 둘을 함께 사용하여 두 가지 방식의 장점을 모두 활용할 수 있는 MVC 패턴이 나타났다.

<img src="https://user-images.githubusercontent.com/57310034/106102983-926a9280-6183-11eb-9398-327bf28c7378.png"/>  

데이터를 처리하는 것에 용이한 서블릿이 사용자의 요청을 받아 분석하고 처리하는 `Controller` 역할을 맡고, 웹 페이지를 구성하는 것에 용이한 JSP가 Controller로 부터 처리된 데이터를 받아 화면을 구성하는 `View` 역할을 맡았다. Controller는 데이터를 처리하는 비즈니스 로직을 직접 수행하는 것이 아니라, 해당 비즈니스 로직을 포함하고 있는 `Model` 을 찾아 메서드를 실행시킨다. 이렇게 `Model` + `View` + `Controller` 의 구성요소로 나누어 역할을 분담시킨 구조를 MVC 패턴이라고 한다. 

|컴포넌트|담당|기능|
|:-----:|:---:|---|
|Model|ServiceClass,<br>자바 빈|비즈니스 로직을 처리하는 모든 것이 모델에 속한다. 컨트롤러로부터 특정 로직에 대한 처리 요청(게시판 글쓰기, 회원가입 등)이 들어오면 이를 수행하고 수행 결과를 컨트롤러에 반환한다.|
|View|JSP|클라이언트가 보게 될 화면(웹 페이지)를 생성한다. JSP에 작성된 자바 코드는 데이터를 처리하는 것이 아니라 컨트롤러가 처리한 데이터 결과물을 받아 화면을 동적으로 그리는데 사용된다.|
|Controller|Servlet|MVC 패턴에서 모든 흐름제어를 담당핟나. Http 요청이 들어오면 이를 처리하기 위한 모델을 사용해서 처리한다. 처리 결과를 추가로 가공할 수도 있고, 완료 후에는 적합한 View(JSP)를 선택하여 결과를 넘겨주고 웹 페이지를 작성하도록 제어한다.|  

### 장점

- Presentation을 위한 코드와 비즈니스 로직을 위한 코드들을 분리할 수 있기 때문에 코드가 훨씬 간결해진다.
- 기능에 따라 분리되어 있기 때문에 분업과 유지보수가 용이하다.
- 확장성이 높다.

### 단점

- 자바에 대한 높은 이해가 필요하다.
- 설계 단계에서 클래스들이 많아서 구조가 복잡해질 수 있다.

<br>

## 스프링 MVC

MVC 패턴에 따라 웹 어플리케이션 서버를 구축하는 것은 쉬운 일이 아니다. 설계 과정에서 상당히 많은 클래스들이 생성되고 그 클래스들간의 상호작용을 하나하나 설정해줘야한다. 이런 것들이 사실 MVC 패턴의 가장 큰 단점이었다.  

그러나 스프링은 개발자가 MVC 패턴을 구현하는데는 시간을 쓰지 않고 비즈니스 로직을 작성하는데에만 집중할 수 있도록 MVC 프레임워크를 지원한다. 이 MVC 프레임워크 지원이 웹 어플리케이션 서버를 개발할 때 스프링을 선택하는 가장 큰 이유이다.  

## 스프링 MVC의 핵심 구성요소

<img src="https://user-images.githubusercontent.com/57310034/106110204-c480f200-618d-11eb-9f2a-616bf65005d3.png"/>

#### DispatcherServlet

`DispatcherServlet`은 모든 연결을 담당하고 있다. Http 요청이 들어오면 DispatcherServlet은 그 요청을 처리하기위한 컨트롤러 객체를 검색한다. 이때 DispatcherServlet은 직접 컨트롤러를 검색하지 않고 `HandlerMapping`이라는 빈 객체에게 컨트롤러 검색을 요청한다.  

#### HandlerMapping

`HandlerMapping`은 Http 요청 경로를 이용해서 이를 처리할 컨트롤러 빈 객체를 찾아 `DispatcherServlet`에게 전달한다. 예를 들어 요청 url이 `/hello` 라면, 등록된 컨트롤러 빈 중에서 `/hello` 요청 경로를 처리하기로 약속된 컨트롤러를 리턴한다.  

#### HandlerAdapter

DiapatcherServlet은 컨트롤러 객체의 메서드를 직접 실행하지 않는다. 스프링에서 사용되는 컨트롤러는 `@Controller` 어노테이션을 붙인 빈 객체와 `Controller` 를 상속받은 빈 객체, 그리고 `HttpRequestHandler` 인터페이스를 구현한 빈 객체가 있는데, 이들을 동일한 방식으로 처리하기 위해 중간에 사용되는 것이 바로 `HandlerAdapter` 이다.  

DispatcherServlet은 HandlerAdapter에게 컨트롤러의 메서드를 실행하여 데이터를 처리하는 권한을 위임하고 HandlerAdapter가 요청을 처리하면 그 결과를 다시 DispatcherServlet에게 돌려준다. 이때 컨트롤러의 처리 결과를 `ModelAndView`라는 객체로 변환해서 돌려준다.  

#### ViewResolver

DispatcherServlet은 컨트롤러의 요청 처리 결과를 클라이언트에게 보여줄 뷰를 찾기 위해 `ViewResolver` 빈 객체를 사용한다. `ModelAndView`는 컨트롤러가 리턴한 뷰 이름을 담고 있는데, ViewResolver는 이 뷰 이름에 해당하는 `View` 객체를 찾거나 새로 생성해서 리턴한다.  

#### View, JSP

DispatcherServlet은 ViewResolver가 리턴한 View 객체에게 응답 결과 생성을 요청한다. `JSP`를 사용하는 경우, View 객체는 JSP를 실행함으로써 웹 프라우저에 전송할 응답 결과를 생성하고 반환한다.  

<br>

### 컨트롤러와 핸들러  

Http 요청를 실제로 처리하는 것은 컨트롤러이고 `DispatcherServlet`는 Http 요청을 전달받는 **창구** 역할을 한다. 어떤 컨트롤러에게 요청 처리를 맡길지 찾기 위해서 `HandlerMapping` 빈 객체를 이용하는데, 그러보니 이름이 조금 이상하다. 컨트롤러를 매핑해주는 객체라면 `ControllerMapping`이 올바른 이름 아닐까?  

스프링 MVC는 웹 요청을 처리할 수 있는 범용 프레임워크다. 위에서도 잠깐 언급했지만 요청을 처리하기위해서 반드시 `@Controller`를 붙인 클래스만 사용할 수 있는 것은 아니다. 스프링은 `HttpRequestHandler` 타입도 요청을 처리하기 위해 제공하기 때문이다.  

이런 이유로 스프링 MVC는 요청을 실제로 처리하는 객체를 **핸들러**라고 표현하고 있으며, `@Controller` 적용 객체나 `Controller` 인터페이스를 구현한 객체 모두 스프링 MVC에서는 핸들러가 된다. 따라서 요청 경로를 핸들러에게 매핑해주는 객체니까 `HandlerMapping` 이라고 부르는 것이다.  

어떤 타입의 핸들러든, 요청 처리 결과를 `ModelAndView` 타입으로만 반환해주면 `DispacheerServlet`이 다음 동일하게 다음 작업을 진행할 수 있다. 그러나 각 핸들러는 `ModelAndView` 타입의 객체를 리턴하기도 하고 그렇지 않기도 한다. 따라서 핸들러의 처리 결과를 항상 `ModelAndView` 타입으로 돌려주기 위한 중간 변환 과정이 필요하며, `HnalderAdapter`가 이 역할을 맡고 있다.  

핸들러 객체의 타입마다 그에 알맞은 `HanlderMapping`, `HandlerAdapter`가 존재하기 때문에, 사용할 핸들러 종류에 따라 해당 `HandlerMapping`, `HandlerAdapter`를 스프링 빈으로 등록해두어야 한다. 스프링 제공하는 설정 기능을 사용하면 이 과정을 생략할 수도 있는데 나중에 자세히 알아보도록 하자.  

<br>

## DispatchServlet과 스프링 컨테이너

위에서 개발자가 직접 스프링 빈으로 등록해줘야하는 객체들이 있었다. 이 객체들은 스프링 설정 클래스에서 빈으로 등록되는데, `DispatcherServlet`과 스프링 컨테이너는 어떤 관계를 맺고 있길래 DispatcherServlet이 스프링 컨테이너에 등록된 빈 객체들에게 접근할 수 있는 것일까?  

스프링 MVC에 대한 각종 구성 요소와 정보들을 설정할 수 있는 `web.xml` 파일에 스프링 설정 파일에 대한 정보를 작성할 수 있다. 스프링 MVC 프레임워크는 `DispatcherServlet`을 생성할 때, 생성자로 `web.xml`에 작성한 스프링 설정 클래스를 넘겨준다. 따라서 DispatchServlet이 생성되면서 스프링 컨테이너를 같이 생성해 가지고 있기 때문에 스프링 설정 파일 안에 정의된 빈 객체들을 DispatcherServlet이 알고 접근할 수 있는 것이다.  

<img src="https://user-images.githubusercontent.com/57310034/106124260-3c571880-619e-11eb-96f6-38d1b47b8ace.png"/>  

<br>

## @Controller를 위한 HandlerMapping과 HandleerAdpater

DispatcherServlet이 지원하는 핸들러의 종류는 한 가지가 아니고, 각 핸들러마다 그에 알맞은 `HandlerMapping`과 `HandlerAdapter`를 빈으로 등록해야한다고 하였다. 그런데 `@Controller` 어노테이션을 붙인 핸들러를 사용할 때에는 이 두 가지를 개발자가 직접 빈으로 등록해주지 않아도 된다.  

```java
@Configuration
@EnableWebMvc
public class AppContext {
    ...
}
```

위 처럼 스프링 설정 클래스 위에 `@EnalbeWebMvc` 어노테이션을 붙여주면, 스프링 MVC의 구성요소로써 구현하거나 설정해야할 코드들을 알아서 추가해준다. 여기에는 `@Controller` 핸들러 객체를 처리할 수 있는 다음의 두 클래스도 포함되어 있다.  
- `org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping`
- `org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter`

`RequestMappingHandlerAdapter`는 `@Controller` 어노테이션이 적용된 객체의 요청 매핑 어노테이션 (`@GetMapping`, `@PostMapping`) 값을 이용해서 현재 들어온 Http 요청을 처리할 컨트롤러 빈을 찾는다.  

`RequestMappingHandlerAdapter`는 컨트롤러의 메서드를 알맞게 처리하고 그 결과를 `ModelAndView` 객체로 변환해서 `DispatcherServlet`에 리턴한다.  

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

위와 같은 컨트롤러가 있고, `/hello?name=gugyu` url로 요청이 오면 `RequestMappingHandlerAdapter`가 `HelloController`를 찾아낸다. 그리고 `RequestMappingHandlereAdapter`가 요청 처리 권한을 위임받고 `HelloController#hollo()` 메서드를 호출한다. 이때 `Model` 타입의 객체를 생성해서 첫 번째 파라미터로 전달하고, Http 요청 시 url에 작성된 `name` 파라미터도 두 번째 파라미터로 전달한다. `Model`에는 뷰에게 넘겨줄 데이터를 Map 형식으로 가지고 있다. 따라서 파라미터로 전달받은 `Model` 객체에 키와 값의 형태로 데이터를 담아줄 수 있는데, 위 코드에서는 `greeting`이라는 키에 `"안녕하세요, " + name` 라는 값을 할당해 담아주었다. 컨트롤러 메서드의 결과 값이 String 타입이면 해당 문자열을 뷰 이름으로 갖는 `ModelAndView` 객체를 생성해서 `DispatcherServlet`에게 리턴한다. 위 코드에서는 "hello"를 리턴하므로 뷰 이름으로 "hello"를 사용한다.   이때 아까 사용한 `Model` 객체를 함께 포함시킨다. 그럼 `DispatcherServlet`이 `View`에게 응답 생성을 요청할 때 `greeting` 키를 갖는 Map 객체를 View에 전달한다.  

<br>

## WebMvcConfigurer 인터페이스와 설정

`@EnableWebMvc` 어노테이션은 스프링 MVC를 위한 다양한 설정을 생성해준다고 하였다. 그러나 여기에 개발자가 추가적인 설정을 추가해야할 경우가 있을 것이다. 이때는 설정 클래스가 `WebMvcConfigurer` 인터페이스를 상속하도록 하고 원하는 메서드를 구현하면 된다.  

```java
@Configuration
@EnableWebMvc
public class ApplicationContext implements WebMvcConfigurer {

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        registry.jsp("/WEB-INF/view/", ".jsp");
    }
}
```
스프링 설정 클래스가 `WebMvcConfigurer`를 구현하고 있다면, 스프링 설정 클래스가 빈으로 등록될 때는 `WebMvcConfigurer` 타입으로 등록된다. 그럼 `@EnableWebMvc` 어노테이션은 `WebMvcConfigurer`의 메서드들을 호출해서 MVC 설정을 추가하게된다. 따라서 위 코드 처럼 원하는 추가 설정을 할 수 있는 메서드를 골라 오버라이딩하면 된다.  

예를 들어 `ViewResolver`를 추가할 때 현재 프로젝트 디렉토리 구조에 맞도록 `View`의 이름에 대한 접두사와 접미사를 설정하고 싶으면 `configureViewResolver()` 메서드를 오버라이딩할 수 있다. 위 코드처럼 오버라이딩하면 컨트롤러가 리턴한 뷰 이름에 대해서 앞에 "/WEB-INF/view/", 뒤에는 ".jsp"를 붙여, "/WEB-INF/view/hello.jsp" 와 같은 구체적인 경로와 이름으로 뷰를 찾을 수 있게 된다.  

`WebMvcConfigurer`의 메서드들은 `default` 키워드를 사용했기 때문에 반드시 모든 메서드를 구현하지 않아도 된다.  

<br>

## 디폴트 핸들러와 HandlerMapping의 우선순위

DispatcherSerevlet에 대한 매핑 경로를 `web.xml`에 정의할 수 있다.  

```xml
<servlet>
    <servlet-name>dispatcher</servlet-name>
    <servlet-class>
        org.springframework.web.servlet.DispatcherServlet
    </servlet-class>
    ...
</servlet>

<servlet-mapping>
    <servlet-name>dispatcher</servlet-name>
    <url-pattern>/</url-pattern>
</servlet-mapping>
```

위 코드 처럼 `DispatcherServlet`에 대한 매핑 경로를 '/'로 설정해두면 `.jsp`로 끝나는 요청을 제외한 모든 요청을 `DispatcherServlet`이 받아 처리한다. 즉 `/index.html`이나 `/css/bootstrap.css`와 같이 확장자가 `.jsp`가 아닌 모든 <b>정적 리소스</b>에 대한 요청도 `DispatcherServlet`이 처리하게 된다.  

그런데 `@EnableWebMvc` 어노테이션이 등록하는 `HandlerMapping`은 `@Controller` 어노테이션을 적용한 빈 객체가 처리할 수 있는 요청 경로만 대응할 수 있다. 만약 들어온 요청에 대해 대응할 수 있는 `@Controller` 핸들러가 빈으로 등록되어있지 않다면 `DispatcherServlet`은 404 응답을 전송한다. 정적 리소를 대응할 수 있는 컨트롤러를 직접 구현할 수도 있으나 동적인 데이터가 없는 리소스를 굳이 구현하기보다는 이를 디폴트 서블릿이 처리할 수 있도록 넘겨주는 것이 더 편한다.  

```java
@Configuration
@EnableWebMvc
public class ApplicationContext implements WebMvcConfigurer {

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }
    ...
}
```

위 처럼 스프링 설정 클래스에서 `WebMvcConfigurer`의 `configureDefaultServletHandling()` 메서드를 구현해 `configurer.endable()`를 실행하도록 하면, 설정 시 `DefaultServletHttpRequestHandler`와 `SimpleUrlHandlerMapping` 타입의 빈 객체를 등록한다.  

여기서 등록된 `SimpleUrlHandlerMapping`은 기존에 `@EnableWebMvc`가 등록한 `RequestMappingHandlerMapping`보다 우선순위가 낮다. 따라서 대응할 수 있는 `@Controller` 컨트롤러가 있다면 그대로 처리하고, 만약 없다면 `SimpleUrlHandlerMapping`에서 요청을 처리할 핸들러를 검색한다. 그러나 `SimpleUrlHandlerMapping`은 요청 경로가 무엇이든간에 모두 `DefaultServletHttpRequestHandler`를 반환한다. 따라서 `DispatcherServlet`은 `DefaultServletHttpRequestHandler`에게 요청 처리를 위임하게되는데, `DefaultServletHttpRequestHandler`는 위임 받은 요청 처리를 다시 디폴트 서블릿에게 위임한다. 이런 과정을 통해 정적 리소스 요청을 포함해 개발자가 등록하지 않은 요청은 디폴트 서블릿이 처리하도록 넘겨줄 수 있다.  

<br>

## @EnableWebMvc 없이 직접 설정하기 

`@EnableWebMvc` 어노테이션은 필수가 아니다. 해당 어노테이션을 사용하지 않고도 스프링 MVC를 사용할 수 있는데, 대신 `@EnalbeWebMvc`가 등록해주던 빈들을 직접 등록해주어야한다. 물론 모든 빈을 다 등록할 필요는 없고 필요한 빈만 등록하면 된다. 따라서 프로젝트에 따라 불필요한 빈은 빼서 가볍게 설정할 수 있다.


<br>
<br>


--- 
해당 포스팅은 [초보 웹 개발자를 위한 스프링5 프로그래밍 입문 - 최범균 저](https://www.aladin.co.kr/shop/wproduct.aspx?ItemId=157472828)를 정리하며 작성하였습니다.  

참고:  
[https://m.blog.naver.com/acornedu/221128616501](https://m.blog.naver.com/acornedu/221128616501)  

DispatcherServlet & SpringContainer :
[https://goodgid.github.io/Spring-DispatcherServlet/](https://goodgid.github.io/Spring-DispatcherServlet/)

WebMvcConfigurer:
[https://goodgid.github.io/Spring-WebMvcConfigurer/](https://goodgid.github.io/Spring-WebMvcConfigurer/)  