---
layout: post
title: "Servlet(서블릿)과 JSP"
subtitle: "자바를 이용한 웹 어플리케이션 통신"
author: "GuGyu"
header-style: text
tags:
  - Java
  - Servlet
  - JSP
  - Network
  - Server
---

## Servelt 이란?

서블릿은 자바를 이용해 웹 어플리케이션을 개발할 때 사용할 수 있는 자바 프로그램이다. 웹 서버가 동적인 데이터 처리를 웹 어플리케이션 서버에게 요청을 하면 이를 처리해 반환하는 역할을 담당한다. 따라서 서블릿은 자바로 구현된 [CGI](https://seonggyu96.github.io/2021/01/27/common_gateway_interface/)라고도 한다. 각각의 서블릿 클래스들은 미리 정의된 요청과 매핑되어 이를 처리한다.

<img src="https://user-images.githubusercontent.com/57310034/105978803-50374780-60d6-11eb-9d81-5dca833c1a46.png"/>

<br>

### Servlet 의 특징 

- 서블렛은 요청에 대해 `HTML`의 형태로 응답한다. 따라서 데이터 처리 과정에 사용되는 `.java` 클래스 파일 안에 `HTML` 요소가 문자열의 형태로 포함되어 있다.
- 요청에 대한 처리를 위해 매번 새로운 프로세스를 생성하던 CGI 와 달리, 스레드를 생성하여 웹 어플리케이션 서버의 메모리를 훨씬 적게 사용하며 요청을 처리한다.

<br>

### Servlet 의 구현

서블릿을 구현하기 위해서 프로그래머는 `HttpServelt` 클래스를 상속하여야 한다. 해당 클래스에는 클라이언트의 요청에 대해 해당 서블릿을 실행하는 조건들이 포함되어있다.

<img src="https://user-images.githubusercontent.com/57310034/105981428-68f52c80-60d9-11eb-9765-531a3c5bf701.png"/>  

#### Servlet 인터페이스

`Sevlet` 인터페이스는 서블릿 프로그램을 개발할 때 반드시 구현해야하는 메서드들을 선언하고 있다. 이 메서드들은 서블릿 실행의 생명주기와 연관되어있다.  

#### GenericServlet 클래스

`Servlet` 인터페이스를 상속하며 서버단의 어플리케이션으로서 필요한 기능을 구현한 추상 클래스이다.  

#### HttpServlet 클래스

`GenericServlet` 클래스를 상속하여 `service()` 메서드를 재정의함으로써 HTTP 프로토콜(GET, POST)에 알맞은 동작을 수행하도록 구현한 클래스이다. 즉 HTTP 프로토콜을 기반으로 웹 서버로부터 전달받은 요청을 처리하는 클래스이다.  

<br>

### Servlet 의 콜백 메서드와 생명주기  

위에서 살펴본 메서드들 중에 생명주기에 따른 콜백 메서드가 존재하는데 이는 다음과 같다.  

|메서드 명|메서드 실행 시점|실행 횟수|기능 구현|
|:---|:---|:---|:---|
|init()|최초로 서블릿 요청이 들어왔을 때|1|초기화 작업|
|service()|요청이 있을 때마다 실행|n|실제 서블릿이 요청에 대해 처리해야하는 작업|
|destroy()|서블릿 객체가 메모리에서 삭제될 때|1|자원 해제 작업|  

서블릿은 최초 요청 시 인스턴스가 생성되어 메모리에 적재되고 `init()` 메서드가 실행되어 초기화 작업을 수행한다. 이후에 동일한 서블릿에 대한 요청이 들어오면 해당 인스턴스를 계속 재사용한다. 즉 싱글턴으로 동작하는 것이다. 요청을 수행할 때에는 `service()` 메서드가 매번 실행되어 요청을 처리하고 서버를 중시시켜 웹 어플리케이션 서비스를 중지하게 되면 서블릿 객체가 메모리에서 삭제되고 이때 `destroy()` 메서드가 호출되어 자원을 해제하는 작업을 수행한다. 이 생명주기 메서드들은 프로그래머가 작성하여야 한다.  

<br>

### Servlet 을 사용한 클라이언트와의 파이프라인

위에서 살펴본 서블릿의 역할과 구현 및 구조를 기억하면서 클라이언트 - 웹 서버 - 웹 어플리케이션 서버 구조에서 서블릿이 어떻게 동작하는지 살펴보자.

<img src="https://user-images.githubusercontent.com/57310034/105987785-1ec47900-60e2-11eb-8832-28292f380cc9.png"/>


1. 사용자가 URL을 통한 HTTP Request를 보내면, 웹 서버는 정적 데이터 요청인지 동적 데이터 요청인지 판단한다. 정적 데이터 요청이라면 웹 서버에 저장된 데이터를 반환한다.
2. 사용자가 동적 데이터 요청을 보냈다면 웹 서버는 해당 요청을 웹 어플리케이션 서버의 서블릿 컨테이너(`SevletContainer`)에 포워딩한다.
3. 서블릿 컨테이너는 해당 요청에 대한 `HttpServeltRequest` 객체와 `HttpServletResponse` 객체를 생성한다.
4. 이후 서블릿 컨테이너는 해당 요청을 어떤 서블릿 클래스에서 이 요청을 처리해야할지 찾는다.
5. 이전에 실행된 적 없는 서블릿이라면 해당 서블릿의 인스턴스를 생성하고 `init()` 메서드를 통해 초기화하여 메모리에 로드 시킨다. 실행된 적이 있다면 새로 인스턴스를 생성하지 않고 메모리에 로드 된 인스턴스를 사용한다.
6. 요청을 처리하기 위해 스레드 풀에서 스레드드를 하나 가져온다.
7. 가져온 스레드에서 매핑된 서블릿의 `service()` 메서드를 호출한다. 해당 메서드는 받은 요청에 따라 `doGet()`, `doPost()` 메서드를 호출하며, 이때 앞서 컨테이너가 생성한 `HttpServletRequest`, `HttpServletResponse` 인스터스를 서블릿 컨테이너에 의해 인수로 전달받는다.
8. `doGet()`, `doPost()` 메서드는 동적으로 웹 페이지를 생성한 후 `HttpServletResponse` 객체에 결과물을 싣어 보낸다.
8. 응답이 끝나면 서블릿 컨테이너는 `HttpServletRequest`, `HttpServletResponse` 객체를 소멸시킨다.  

<br>

### Servlet Container

위 파이프라인에서는 서블릿 컨테이너라는 요소가 등장한다. 서블릿 컨테이너는 웹 어플리케이선 서버 내에서 실행되어 요청에 따른 서블릿을 실행시키고 생명주기를 관리하는 역할을 맡는다. 즉, 서블릿 클래스들이 요청을 처리하기 위한 행동들을 정의한 것들이라면 서블릿 컨테이너는 그 행동들을 적재적소에 수행하도록 실행시켜주는 요소이다. 이런 서블릿 컨테이너의 역할을하는 대표적인 예로 **톰캣(Tomcat)** 이 있다.  

#### 서블릿 매핑 방법 

서블릿 컨테이너는 다양한 서블릿 클래스 중에서 요청을 처리할 서블릿을 어떻게 매핑해주는 것일까?  

서블릿 매핑 방법에는 몇 가지가 있는데 가장 많이 사용되는 것은 <b>배포서술자(Deployment Descriptor)</b>를 사용하거나 어노테이션을 사용하는 방법이다.  

배포서술자의 역할을 하는 파일은 `web.xml`으로, 웹 어플리케이션을 구성하는 웹 컴포넌트에 대한 구성 및 배치정보 등을 제공한다.

```xml
<web-app ... >

  <!-- <servlet> 항목에 컨테이너가 관리하는 서블릿들을 정의한다.-->
  <servlet>
    <!-- <servlet-name> 항목은 해당 파일 내에서만 사용될 이름을 정의-->
    <servlet-name>internal name</servlet-name>
    <!-- <servlet-class> 항목에는 완전한 서블릿 클래스 명을 입력-->
    <servlet-class>foo.bar.MyServlet<servlet-class>
  </servlet>

  <!-- HTTP 요청을 받으면 해당 항목에서 담당하는 서블릿을 찾음-->
  <servlet-mapping>
    <servlet-name>Internal name</servlet-name>
    <!--<url-pattern> 항목은 HTTP 요청을 받은 URL을 나타냄-->
    <url-pattern>/myservlet</url-pattern>
  </servlet-mapping>
  ...
</wep-app>
```

그러나 서블릿 3.0 부터는 어노테이션을 지원하여 배포서술자를 작성하지 않고도 서블릿 매핑이 가능하게 되었다. 해당하는 서블릿 클래스에 어노테이션을 달아주기만 하면 된다.

```java
@WebServlet("/myservlet")
public class MyServlet extends HttpServlet {
  ...
}
```

#### 서블릿 컨테이너의 역할

- 웹 서버와의 통신
서블릿 컨테이너는 웹 서버와의 통신을 가능하도록한다. 일반적으로 웹 서버와 웹 어플리케이션 서버는 소켓을 통해 통신을 하는데, 이 부분을 톰켓이 API로 제공해주기 때문에 개발자가 직접 구현할 필요가 없다.  

- 서블릿 생명주기 관리
서블릿의 생성과 소멸을 관리한다. 첫 호출 시 서블릿 클래스를 메모리에 로딩하고 초기화 메서드를 호출해주며, 요청을 처리하기 위해 실행 메서드를 실행시킨다. 또한 서블릿을 메모리에서 제거할 경우 종료 메서드를 실행해 자원을 해제할 수 있도록 한다.

- 멀티 스레드 지원 및 관리
요청이 올 때 마다 자바 스레드에서 이를 처리시킨다. 서블릿 컨테이너는 스레드 풀을 구현하고 있어서 스레드를 적절히 재사용하며 운영해주기 때문에 속도가 빠르고 안정적이다.

- 선언적인 보안 관리
보안에 관련된 내용을 서블릿 또는 자바 클래스에 직접 삽입하지 않고 별도의 XML 파일에 기록해두면 서블릿 컨테이너가 올바르게 처리해준다.  



<br>


## JSP (Java Server Page)

서블릿은 자바 클래스 파일로 HTML 웹 페이지를 생성하다보니 코드 내에서 HTML 코드를 모두 print 해줘야한다.

```java
writer.println("<html>");
writer.println("<head>");
writer.println("</head>");
writer.println("<body>");
writer.println("<h1>helloWorld~</h1>");
writer.println("name : " + request.getParameter("name") + "<br/>");
writer.println("id : " + request.getParameter("id") + "<br/>");
writer.println("pw : " + request.getParameter("pw" + "<br/>"));
writer.println("major : " + request.getParameter("major") + "<br/>");
writer.println("protocol : " + request.getParameter("protocol") + "<br/>");
writer.println("</body>");
writer.println("</html>");
writer.close();
```

위 코드를 보면 자바 클래스 파일 안에 HTML 코드를 삽입한다기 보다는 스트림 객체를 생성해서 하나하나 출력해주고 있다. 이런 방법으로는 복잡한 레이아웃을 구현하는데 상당한 어려움이 있다.  

이런 문제를 해결하고자 JSP가 탄생했는데, JSP는 서블릿과 반대로 HTML 코드 형식을 하되, 중간에 자바 코드를 삽입할 수 있도록 하였다.

```html
<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="EUC-KR"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="EUC-KR">
<title>Hello world!</title>
</head>
<body>

<%-- 자바 코드 삽입 --%>
<% 
	for(int i = 0; i < 10; i++) {
		out.println(i);
	}
%>

</body>
</html>
```

이렇게 구성하면 HTML 기반으로 레이아웃을 작성한 뒤, 필요한 부분만 자바 코드를 삽입할 수 있어 레이아웃을 구성하는데 서블릿에 비해 훨씬 편리하다.  

### JSP 의 동작 구조

프로그래머가 JSP 파일을 작성해 웹 어플리케이션 서버에 올려두면 서블릿과 동일한 과정으로 JSP가 호출된다. 웹 서버에서 보내준 요청에 따라 매핑된 JSP를 사용하여 웹 페이지를 동적으로 생성한다.

JSP는 먼저 서블릿 파일(.java)로 변환된다. 서블릿 클래스를 사용할 때 프로그래머가 작성하는 그 서블릿 클래스 파일의 형태로 변환되는 것이다. 이렇게 변환된 서블릿 파일을 다시 컴파일하여 실행하는 과정을 거친다. 실행 결과는 자바 언저가 모두 사라진 HTML 코드가 된다. 위에서 작성한 JSP 예시 코드는 결과적으로 포문으로 인해 0부터 9까지 출력된 정적인 HTML 코드를 내놓게 된다.  

<img src="https://user-images.githubusercontent.com/57310034/105994893-93e87c00-60eb-11eb-884b-043e2b68d61b.png"/>  

그냥 서블릿을 바로 사용하는 것에 비해 처음 구동 시 변환 과정이 한 번 더 있으므로 조금 느리지만, .class 파일이 한 번 생성되면 두 번째 요청부터는 변환 과정 및 컴파일 과정이 없기 때문에 서블릿과 거의 동일하게 작동한다.  

위 JSP 코드를 사용하여 최종적으로 사용자에게 도달한 웹 페이지는 다음과 같다.  

```html
<!DOCTYPE html>
<html>
  <head>
    <meta charset="EUC-KR">
    <title>Hello world!</title>
  </head>
  <body>
0
1
2
3
4
5
6
7
8
9
  </body>
</html>
``` 

<br>

## Servlet과 JSP의 사용

지금까지 서블릿과 JSP에 대해 간략하게 알아보았다. 위 내용만 보면 서블릿이나 JSP나 코드를 처음 작성하는 방식만 다르고 결국엔 동일한 역할을 하고 있음을 알 수 있다. 초기 자바를 이용한 웹 개발은 서블릿을 이용했으나 갈수록 레이아웃이 복잡해지고 구현하기 어려워지면서 JSP가 유행하게 되었다. 그러나 복잡한 데이터 처리는 여전히 서블릿이 훨씬 용이하기 때문에 결국 서블릿과 JSP를 함께 사용하는 형태로 진화하였다.  

JSP는 JSP의 장점을 최대한 활용하기 위해 웹 어플리케이션 구조에서 사용자에게 결과를 보여주는 presentation 역할을 담당하고, 서블릿은 서블릿의 장점을 최대한 활용하기 위해 사용자의 요청을 받아 분석하고 비스니스 로직을 수행하는 객체들과 통신하여 데이터를 처리하는 역할을 담당하게 되었다.  

이렇게 Presentation 과 Business Logic 부분을 분리하여 웹 어플리케이션을 개발하는 MVC 패턴이 유행하게 되었다.

<br>
<br>


--- 
참고  
[https://mangkyu.tistory.com/14](https://mangkyu.tistory.com/14)  
[https://galid1.tistory.com/487](https://galid1.tistory.com/487)  
[https://codevang.tistory.com/191](https://codevang.tistory.com/191)  
[https://m.blog.naver.com/acornedu/221128616501](https://m.blog.naver.com/acornedu/221128616501)