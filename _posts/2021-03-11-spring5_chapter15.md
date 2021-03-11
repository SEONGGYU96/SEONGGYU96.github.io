---
layout: post
title: "Spring5 : 간단한 웹 어플리케이션의 구조"
subtitle: ""
author: "GuGyu"
header-style: text
tags:
  - Spring
  - Spring5
  - Java
  - Study
  - Server
---

## 간단한 웹 어플리케이션의 구성 요소

간단한 웹 어플리케이션을 개발할 때 사용하는 전형적인 구조는 다음 요소를 포함한다.  

- 프론트 서블릿
- 컨트롤러 + 뷰
- 서비스
- DAO

프론트 서블릿은 웹 브라우저의 모든 요청을 받는 창구 역할을 한다. 프론트 서블릿은 요청을 분석해서 알맞은 컨트롤러에 전달한다. 스프링 MVC에서는 DispatcherServlet이 프론트 서블릿의 역할을 수행한다.  

컨트롤러는 실제 웹 브라우저의 요청을 처리한다. `@Controller` 어노테이션을 붙인 클래스들이 여기에 포함된다. 컨트롤러는 클라이언트(브라우저)의 요청을 처리하기 위해 알맞은 기능을 실행하고 그 결과를 뷰에 전달한다. 컨트롤러의 주요 역할은 다음과 같다.  

- 클라이언트가 요구한 기능을 실행
- 응답 결과를 생성하는데 필요한 모델 생성
- 응답 결과를 생성할 뷰 선택

컨트롤러는 어플리케이션이 제공하는 기능과 사용자 요청을 연결하는 매개체로서 기능 제공을 위한 로직을 직접 수행하지는 않는다. 대신 해당 로직을 제공하는 서비스에 그 처리를 위임한다.  

```java
@AutoWired
ChangePasswordService changePasswordService;

@PostMapping
public String submit(
    @ModelAttribute("command") ChangePaswordCommand passwordCommand) {
        ...
        try {
            changePasswordService.changePassword(
                authInfo.getEmail(),
                passwordCommand.getCurrentPassword(),
                passwordCommand.getNewPassword()
            );
            return "edit/changePassword";
        } catch ...
    }
)
```  

서비스는 기능의 로직을 구현한다. 사용자에게 비밀번호 변경 기능을 제공하려면 수정 폼을 제공하고, 로그인 여부를 확인하고, 실제로 비밀번호를 변경해야 한다. 이 중에서 핵심 로직은 비밀번호를 변경하는 것이다. 폼을 보여주는 로직이나 로그인 여부를 확인하는 로직은 핵심이 아니다.   

서비스는 DB 연동이 필요하면 DAO를 사용한다. DAO는 <b>Data Access Object</b>의 약자로, DB와 웹 어플리케이션 간에 데이터를 이동시켜 주는 역할을 맡는다. 어플리케이션은 DAO를 통해서 DB에 데이터를 추가하거나 DB에서 데이터를 읽어온다.  

목록이나 상세 화면과 같이 데이터를 조회하는 기능만 있고 부가적인 로직이 없는 경우에는 컨트롤러에서 직접 DAO를 사용하기도 한다.

<br>

### 서비스의 구현  

서비스는 핵심이 되는 기능의 로직을 제공한다고 했다. 예를 들어 비밀번호 변경 기능은 다음 로직을 서비스에서 수행한다.  

- DB에서 비밀번호를 변경할 회원의 데이터를 구한다.
- 존재하지 않으면 예외를 발생시킨다.
- 회원 데이터의 비밀번호를 변경한다.
- 변경 내역을 DB에 반영한다.  

서비스 로직들은 한 번의 과정으로 끝나기보다는 위 처럼 몇 단계의 과정을 거치곤 한다. 중간 과정에서 실패가 나면 이전까지 했던 것을 취소해야하고, 모든 과정을 성공적으로 진행했을 때 완료해야 한다. 이런 이유로 서비스 메서드를 트랜잭션 범위에서 실행한다.  

```java
@Transactional
public void changePassword(String email, String oldPassword, String newPassword) {
    Member member = memberDao.selectByEmail(email);
    if (member == null) {
        throw new MemberNotFoundException();
    }
    member.changePassword(oldPassword, newPassword);
    memeberDao.update(member);
}
```

서비스 클래스는 기능별로 작성하는 것이 좋다. 예를 들어 `MemberService`라는 회원 관련 서비스 클래스를 만들어 회원가입 기능과 비밀번호 변경 기능, 비밀번호 찾기 등 다양한 관련 메서드들을 작성해둘 수 있다. 그럼 나중에 회원 기능에 문제가 생겼을 때, 해당 서비스 클래스에서만 원인을 찾을 수 있다.  

서비스 클래스의 메서드는 기능을 실행하는데 필요한 값을 파라미터로 전달받는다. 이때 필요한 데이터를 전달받기 위해 별도 타입을 만들면 스프링 MVC의 커맨드 객체로 해당 타입을 사용할 수 있어 편하다.  

```java
@PostMapping("/register/step3")
public String handleStep3(RegisterRequest registerRequest) {
    ...
    memberRegisterSerivce.regist(registerRequest);
    ...
}
```  

커맨드 클래스를 작성한 이유는 스프링 MVC가 제공하는 폼 값 바인딩과 검증, 스프링 폼 태그와의 연동 기능을 사용하기 위함이다.  

서비스 메서드는 기능을 실행한 후에 결과를 알려줘야한다. 결과는 크게 두 가지 방식으로 알려준다.  

- 리턴 값을 이용한 정상 결과
- 예외를 이용한 비정상 결과  

```java
public class AuthService {
    ...
    public AuthInfo authenticate(String email, String password) {
        Member member = memberDao.selectByEmail(email);
        //이메일과 일치하는 계정이 없으면 예외 발생
        if (member == null) {
            throw new WrongIdPasswordException();
        }
        //비밀번호를 잘못 입력했다면 예외 발생
        if (!member.matchPassword(password)) {
            throw new WrongIdPssswordException();
        }
        //정상 확인되었다면 인증 정보를 리턴
        return new AuthInfo(member.getId(), member.getEmail(), member.getName());
    }
}
```

위 처럼 서비스 메서드를 작성했을 때, 예외가 발생하면 인증에 실패했다는 것을 알 수 있다. 컨트롤러 메서드는 try-catch 문으로 예외가 발생한 경우 이를 인증 실패로 간주하고 별도의 뷰를 리턴할 수 있게 된다.  

<br>  

## 패키지 구성  

그럼 각 구성 요소의 패키지는 어떻게 구분해 줘야할까? 패키지 구성에는 사실 정답이 없다. 패키지를 구성할 때 중요한 것은 팀 구성원 모두가 동일한 규칙에 따라 일관되게 패키지를 구성해야 한다는 것이다. 개발자에 따라 패키지를 구성하는 방식이 서로 다르면 코드를 유지보수할 때 불필요하게 시간을 낭비하게 된다. 예를 들면 당연히 존재할 거라고 생각한 패키지가 아닌 예상 밖의 패키지에 위치한 클래스를 찾느라 시간을 허비할 수 있다.  

## 웹 어플리케이션이 복잡해지면  

컨트롤러-서비스-DAO 구조는 간단한 웹 어플리케이션을 개발하기에는 무리가 없다. 문제는 어플리케이션의 기능이 많아지고 로직이 추가되기 시작할 때 발생한다. 중요 로직이 DAO, 서비스에 걸쳐 흩어지기도 하고 중복된 쿼리나 코드가 늘어나기도 한다.  

이런 문제를 완화하는 방법 중 하나는 도메인 주도 설계를 적용하는 것이다. 도메인 주도 설계는 컨트롤러-서비스-DAO 구조 대신에 UI-서비스-도메인-인프라의 네 영역으로 어플리케이션을 구성한다. 여기서 UI는 컨트롤러 영역에 대응하고 인프라는 DAO 영역에 대응한다. 중요한 점은 주요한 도메인 모델과 업무 로직이 서비스 영역이 아닌 도메인 영역에 위치한다는 것이다. 또한 도메인 영역은 정해진 패턴에 따라 모델을 구현한다. 이를 통해 업무가 복잡해져도 일정 수준의 복잡도로 코드를 유지할 수 있도록 해 준다.  

<br>
--- 
해당 포스팅은 [초보 웹 개발자를 위한 스프링5 프로그래밍 입문 - 최범균 저](https://www.aladin.co.kr/shop/wproduct.aspx?ItemId=157472828)를 정리하며 작성하였습니다.  

<br>

참고 :  
