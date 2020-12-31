---
layout: post
title: "Spring5 : 스프링 DI"
subtitle: "Spring5 study (3)"
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
  
# 의존이란?

`DI`는 `Dependency Injection`의 약자로, 말 그대로 의존성을 주입하는 것이다. 여기서 "의존"은 객체 간의 의존을 의미한다. 다음 코드를 살펴보자.  
```java
public class MemberRegisterService {
    private MemberDao memberDao = new MemberDao();

    public void regist(RegisterRequest request) {
        Member member = memberDao.getMemberByEmail(request.getEmail());
        if (member != null) {
            //같은 이메일을 가진 회원이 존재하면 예외 발생
            throw new DuplicateMemberException("duplicate email " + request.getEmail());
        }
        //DB 삽입
        Member new Member = new Member(request.getEmail(), request.getPassword(), request.getName());
        memberDao.insert(newMember);
    }
}
```

`MemberRegisterService`는 회원가입 관련 기능을 포함하는 클래스이다. 따라서 DB 처리를 위해 `MemeberDao` 클래스의 메서드를 사용한다. 이렇게 <b>한 클래스가 다른 클래스의 메서드를 실행할 때 "의존"한다</b>고 표현한다. 조금 더 자세히 말하면 한 클래스의 변경이 다른 클래스에게 영향을 준다면, 이 두 클래스는 "의존" 관계에 있다. 예를 들면 `MemberDao` 클래스의 insert() 메서드의 파라미터를 하나 추가하면 `MemberRegisterService`의 `insert()` 메서드에서도 파라미터를 하나 추가해주어야한다.  

의존 하는 대상이 있으면 그 대상을 구하는 방법이 필요하다. 위 코드처럼 의존 대상의 객체를 `new` 키워드로 직접 생성하는 것이 가장 쉬운 방법이다. 이 경우, `MemberRegisterService` 클래스를 생성하는 순간, `MemberDao` 객체도 함께 생성된다. 아마 대부분의 입문 개발자들은 이와 같이 객체를 생성할 것이다. 그리고 이번 주제에서 다루는 `DI` 또한 의존 대상 객체를 구하는 방법 중 하나이다.  
<br>

# DI를 통한 의존 처리

`DI`는 의존하는 객체를 직접 생성하는 대신 <b>의존 객체를 대상 클래스에 전달</b>해준다. 앞서 예시로 사용한 의존 객체 직접 생성 방식을 `DI`로 바꾸면 다음과 같다.  
```java
public class MemberRegisterService {
    private MemeberDao memberDao;

    public MemeberRegisterService(MemberDao memberDao) {
        this.memberDao = memberDao;
    }
    ...
}
```

이제 `MemberDao`를 클래스 내에서 직접 생성하는 것이 아니라 생성자를 통해서 의존하고 있는 `MemberDao` 객체를 **주입(Injection)** 받기 때문에 이 코드는 **DI패턴**을 따른다고 할 수 있다. 왜 굳이 이렇게 하는 것일까? 이유는 바로 **유연함**이다.  

# DI와 의존 객체 변경의 유연함

DI 패턴으로 코드를 작성하든 아니든 어차피 동일한 객체를 사용하고 있고, 객체 생성과 동시에 의존 대상 객체가 결국 맴버 변수로 자리잡게 되는 것은 똑같은데 무슨 유연함을 준다는 것일까? 오히려 `MemberRegisterService` 클래스를 생성할 때 생성자 파라미터가 하나 추가되어 코드만 길어진 것 같다. 하지만 다음 예시를 천천히 살펴보자.  
<br>

```java
public class MemberRegisterService {
    private MemeberDao memberDao = new MemberDao();
    ...
}

public class ChangePasswordService {
    private MemberDao memberDao = new MemberDao();
    ...
}
```

위와 같이 암호를 변경하는 기능을 제공하는 `ChangePasswordService` 클래스도 `MemberDao` 객체를 직접 생성한다고 하자. 그런데 `MemberDao`에서 빠른 데이터 조회를 위해 이를 상속하여 캐시 기능을 포함하는 `CachedMemberDao`를 만들었다. 그렇다면 `MemberDao`를 직접 생성하고 있던 위의 두 클래스는 캐시 기능을 사용하기 위해서 `private Memberdao memberDao = new CachedMemberDao;`로 하나하나 변경해주어야한다. 이 경우에서는 두 개에 그쳤지만 실 프로젝트에서는 더 많은 클래스들이 의존하고 있을 수 있다. 아찔하지 않은가?  

이 경우에 DI를 사용하면 이런 수정 사항에 대해 유연하게 대처할 수 있다.  
<br>

```java
MemberDao memberDao = new MemberDao();
MemberRegisterService registerService = new MemberRegisterService(memberDao);
ChangePasswordService passwaordService = new ChangePasswordService(memberDao);
```  

위 처럼 클래스를 생성하고 의존성을 주입해주면, 다음과 같이 실제 객체를 생성하는 단 한 부분만 수정해주면 된다.  
<br>

```java
MemberDao memberDao = new CachedMemberDao(); //이 부분만 수정
MemberRegisterService registerService = new MemberRegisterService(memberDao);
ChangePasswordService passwaordService = new ChangePasswordService(memberDao);
```

앞서 의존 객체를 직접 생성했던 방식에 비해 변경할 코드가 한 곳으로 집중되는 것을 알 수 있다.  
<br>

# 객체 어셈블러

DI패턴으로 작성된 코드에서는 객체 생성에 사용할 클래스를 변경하기 위해 객체를 주입하는 코드 한 곳만 변경하면 된다고 했다. 그렇다면 실제를 생성하고 주입하는 코드는 어디에 위치해있을까? 단순하게 생각해보면 `main()` 메서드에 작성하면 될 것 같다.  
```java
public class Main {
    ...
    MemberDao memberDao = new CachedMemberDao(); //이 부분만 수정
    MemberRegisterService registerService = new MemberRegisterService(memberDao);
    ChangePasswordService passwaordService = new ChangePasswordService(memberDao);

    ChangePasswordService passwordService = asembler.getPasswordService();
    passwordService.changePassword("link5658@gmail.com", "1234", "newpassword");
    ...
}
```
<br>

이 방법도 나쁘진 않지만 좀 더 나은 방법은 객체를 생성하고 의존 객체를 주입해주는 클래스를 **따로** 작성하는 것이다. 의존 객체를 주입한다는 것은 서로 다른 두 객체를 조립한다고 생각할 수 있는데, 이런 의미에서 이 클래스를 **어샘블러**라고도 표현한다.  
<br>

```java
public class Assembler {
    private MemberDao memberDao;
    private MemberRegisterService registerService;
    private ChangePasswordService passwordService;

    public Assembler() {
        memberDao = new MemberDao();
        //의존 주입
        registerService = new MemberRegisterService(memberDao);
        passwordService = new ChangePasswordService(memberDao);
    }

    public MemberDao getMemberDao() {
        return memberDao;
    }

    public MemberRegisterService getRegisterService() {
        return registerService;
    }

    public ChangePasswordService getPasswordService() {
        return passwordService;
    }
}
```

위 어샘블러는 생성자에서 의존 객체를 생성하고 이를 각 클래스마다 주입한다. 이제 이 어샘블러를 다음과 같이 사용하면 된다.
<br>

```java
public class Main {
    ...
    Assembler assembler = new Assembler();

    private static void main(String[] args) throw IOException {
        ChangePasswordService passwordService = asembler.getPasswordService();
        passwordService.changePassword("link5658@gmail.com", "1234", "newpassword");
        ...
    }
    ...
}
```

이제 자칫 복잡해질 수 있는 메인 메서드가 한결 깔끔해졌다. 객체를 생성하고 의존 객체를 주입하는 기능은 어샘블러가 전담하기 때문이다. 덕분에 이제 `MemberDao` 클래스가 아니라 이를 상속받은 `CachedMemberDao` 클래스를 사용해야한다면 `Assembler`에서 객체를 초기화하는 코드만 변경하면 된다. 어떤가? "유연하다" 라는 의미가 쫌 와닿는가?  
<br>

# 스프링의 DI 설정

이제 의존이 무엇인지와, DI를 이용해 의존 객체를 주입하는 방법에 대해 알아봤다. 특별할 것 없는 디자인 패턴 같은데 여기서 이렇게 설명하고 있는 까닭은 무엇일까? 바로 <b>스프링이 DI를 지원하는 어샘블러</b>의 역할을 할 수 있기 때문이다.  

스프링은 위에서 다룬 `Assembler` 클래스의 생성자처럼 필요한 객체를 생성하고 생성한 객체에 의존을 주입하는 기능을 자체적으로 지원하고 `Assembler#getRegisterService()` 메서드처럼 객체를 제공하는 기능 또한 정의하고 있다. 이렇게 직접 작성한 클래스와의 차이라면 `Assembler` 클래스는 특정 타입의 클래스를 다루고 있는 반면, 스프링은 다양한 타입의 클래스를 디루는 <b>범용 어셈블러</b>라는 점이다. 아직은 살짝 와닿지 않으니 스프링 예제 코드를 보며 이해해보자.
<br>

```java
@Configuration
public class AppContext {

    @Bean
    public MemberDao memberDao() {
        return new MemberDao();
    }

    @Bean
    public MemberRegisterService memberRegisterService() {
        return new MemberRegisterService(memberDao());
    }

    @Bean
    public ChangePasswordService changePasswordService() {
        return new ChangePasswordService(memberDao());
    }
}
```

이전 포스팅에서와 마찬가지로 스프링 설정 클래스인 `AppContext`를 만들고 여기서 사용될 빈(Bean) 객체를 정의해주었다. 이로써 컨테이너를 **정의**한 것이고, 설정 클래스를 이용해서 컨테이너를 **생성**해야한다.  
<br>

```java
public class Main {
    ...
    private static ApplicationContext context = null;

    private static void main(String[] args) throw IOException {
        context = new AnnotationConfigApplicationContext(AppContext.class);

        ChangePasswordService passwordService = context.getBean("changePasswordService", ChangePasswordService.class);
        passwordService.changePassword("link5658@gmail.com", "1234", "newpassword");
        ...
    }
    ...
}
```

이렇게 컨테이너를 생성하고 `memberRegisterService`라는 이름의 빈 객체를 할당받을 수 있다. 이때, 컨테이너를 정의할 때 작성한 `MemberRegisterService()`메서드에 따라 해당 빈 객체는 내부적으로 `memberDao` 빈 객체를 주입받은 객체가 된다. 이 메인 클래스가 위에서 작성한 메인 클래스와 다른 점은 `Assembler` 클래스 대신 스프링 컨테이너인 `ApplicationContext`를 사용한 것 뿐이다. 이렇게 스프링은 컨테이너 내에서 의존성을 주입하는 기능을 자체적으로 제공한다.  
<br>

# DI : 세터 메서드 방식

DI 패턴을 구현하는 방식도 여러가지가 있다. 지금껏 작성한 클래스를 보면 생성자를 통해서 의존 객체를 주입받았다. 하지만 이 외에 세터 메서드를 이용해서 객체를 주입받기도 한다.  
<br>

```java
public class ChangePasswordService {
    private MemberDao memberDao;
    ...

    //생성자가 아닌 세터 메서드로 의존 주입
    public void setMemberDao(MemberDao memberDao) {
        this.memberDao = memberDao;
    }
}
```

```java
@Configuration
public class AppContext {
    ...
    @Bean
    public ChangePasswordService changePasswordService() {
        ChangePasswordService passwordService = new ChangePasswordService();
        //세터 메서드로 의존 주입
        passwordService.setMemberDao(memberDao());
        return passwordService;
    }
}
```

`ChangePasswordService` 클래스에 세터 메서드 방식으로 의존 객체를 주입하면 위와 같이 작성할 수 있을 것이다.  

그렇다면 생성자 방식과 세터 메서드 방식의 장단점은 무엇일까? 장점부터 알아보자면 다음과 같다.
- 생성자 방식 : 빈 객체를 생성하는 시점에 모든 의존 객체가 주입된다.
- 세터 메서드 방식 : 세터 메서드 이름을 통해 어떤 의존 객체가 주입되는지 알아보기 쉽다.

각 방식의 장점은 곧 다른 방식의 단점이 된다. 예를 들어 생성자 파라미터 개수가 많을 경우, 각 인자가 어떤 의존 객체를 설정하는지 알아내려면 생성자 코드를 직접 확인해야한다. 하지만 세터 메서드 방식은 메서드 이름만으로도 어떤 의존 객체를 설정하는지 쉽게 유추할 수 있다. 또, 생성자 방식은 빈 객체를 생성하는 시점에 필요한 모든 의존 객체를 주입받기 때문에 객체를 사용할 때 완전한 상태로 안전하게 사용할 수 있다. 하지만 세터 메서드 방식은 세터 메서드를 사용하지 않아도 빈 객체가 생성됨으로 프로그래머의 실수로 `NullPointerException`을 야기하는 위험이 존재한다.  

# @Configuration 클래스(설정 클래스)의 @Bean 설정과 싱글턴

앞서 작성한 `AppContext` 클래스를 다시 보자.  
<br>

```java
@Configuration
public class AppContext {

    @Bean
    public MemberDao memberDao() {
        return new MemberDao();
    }

    @Bean
    public MemberRegisterService memberRegisterService() {
        return new MemberRegisterService(memberDao());
    }

    @Bean
    public ChangePasswordService changePasswordService() {
        return new ChangePasswordService(memberDao());
    }
}
```

`memberRegisterService()` 메서드와 `changePasswordService()` 메서드는 `memberDao()` 메서드를 실행하고 있다. 그리고 `memberDao()` 메서드는 매번 새로운 `MemberDao` 객체를 생성해서 리턴한다. 그렇다면 `memberRegisterService()` 에서 생성한 `MemberRegisterService` 객체와 `changePasswordService()` 메서드에서 생성한 `ChangePasswordService` 객체는 서로 다른 `MemberDao` 객체를 사용하고 있는 것일까? 이는 어찌보면 당연히 다른 객체인 것 같다.  

하지만 앞선 포스팅에서 설명한 것 처럼, 스프링 컨테이너가 생성한 빈은 싱글톤 객체이다. 스프링 컨테이너는 `@Bean`이 붙은 메서드에 대해 한 개의 객체만 생성한다. 이는 `@Configuration` 클래스 내 다른 어떤 메서드에서도 `memberDao()`를 몇 번 호출하든 항상 같은 객체를 리턴한다는 것을 의미한다. 이게 어떻게 가능한 일일까?  

스프링은 `@Configuration` 클래스를 그대로 사용하지 않는다. 대신 이 클래스를 상속한 새로운 설정 클래스를 만들어서 사용한다. 스프링이 런타임에 생성한 새로운 설정 클래스는 다음과 유사한 방식으로 동작한다.  
<br>

```java
public class AppContextExtension extends AppContext {
    private Map<String, Object> beans = ...;

    @Override
    public MemberDao memberDao() {
        if (!beans.containsKey("memberDao")) {
            beans.put("memberDao", super.memberDao());
        }
        return (MemberDao) beans.get("memberDao");
    }
    ...
}
```

실제 코드는 훨씬 복잡하지만 대략 위와 같은 방식으로 `@Bean` 객체를 싱글턴으로 유지하고 있다. `memberDao()` 메서드가 호출될 때 마다 매번 새로운 객체를 생성하는 것이 아니라 최초 호출 시 이를 어딘가에 보관해두고 이후에는 동일한 객체를 꺼내와 리턴한다. 따라서 `memberRegisterService()` 메서드와 `changePasswordService()` 메서드에서 주입받은 `MemberDao` 객체는 동일한 객체임이 보장된다.  
<br>

# 두 개 이상의 설정 클래스

스프링을 이용해서 어플리케이션을 개발하다보면 적게는 수십 개에서 많게는 수백여 개 이상의 빈 객체를 정의하게 된다. 정의하는 빈의 개수가 증가하면 한 개의 클래스에 모두 작성하는 것보다 영역별로 설정 클래스를 나누면 관리하기 편해진다. 이는 당연히 그럴듯한 이야기지만, 나눠진 클래스들이 서로가 정의하고 있는 빈 객체를 필요로 하면 어떻게 해야할까? 하나의 설정 클래스에서 다른 설정 클래스를 생성하여 의존해야할까? 이는 유연하지 못한 방식이니 설정 클래스에 설정 클래스 객체를 주입해주어야할까? 그렇다면 이 의존 주입은 어디서 이루어져야할까? 상당히 복잡해진다. 하지만 다행히도 스프링은 어노테이션 하나로 이를 단순히 해결한다.
<br>

```java
@Configuration
public class AppContext1 {
    @Bean
    public MemberDao memberDao() {
        return new MemberDao();
    }
}
```  
```java
@Configuration
public class AppContext2 {
    @Autowired
    private MemberDao memberDao;
    
    @Bean
    public MemberRegisterService memberRegisterService() {
        return new MemberRegisterService(memberDao);
    }

    @Bean
    public ChangePasswordService changePasswordService() {
        return new ChangePasswordService(memberDao);
    }
}
```

`@Autowired` 어노테이션은 스프링의 자동 주입 기능을 위한 것이다. 스프링 설정 클래스의 필드에 `@Autowired` 어노테이션을 붙이면 해당 타입의 빈을 찾아, 알아서 필드에 할당해준다. 단순하지 않은가?  

하지만 또 의문점이 하나 생긴다. 설정 클래스가 두 개 이상이면 스프링 컨테이너를 생성하는 코드는 어떻게 작성해야할까?  
<br>

```java
context = new AnnotationConfigApplicationContext(AppContext1.class, AppContext2.class);
```

`AnnotationConfigApplicationContext`클래스의 생성자는 가변 인자를 갖기 때문에 설정 클래스 목록을 콤마로 구분해서 전달하면 된다. 이 또한 단순하게 해결되었다.
<br>

# 어노테이션

## @Autowired

앞서 등장한 `@Autowired` 어노테이션에 대해 조금만 더 알아보자. `@Autowired` 어노테이션은 스프링 빈에 의존하는 다른 빈을 자동으로 주입하고 싶을 때 사용한다. 이는 설정 클래스에서만 한정된 기능이 아니다.  
<br>

```java
public class MemberRegisterService {
    
    @Autowired
    private MemberDao memberDao;

    public void regist(RegisterRequest request) {
        Member member = memberDao.getMemberByEmail(request.getEmail());
        ...
    }
    ...
}
```

`@Autowired` 어노테이션은 빈 객체에 주입되어야 할 빈 객체를 자동으로 찾아 넣어주는 녀석이다. 따라서 위와 깉이 빈 객체 내 의존 필드에 어노테이션을 붙여주면 설정 클래스의 `@Bean` 메서드에서 지금까지 설명한 생성자 방식, 세터 메서드 방식 모두 작성하지 않아도 자동으로 `MemberDao` 객체가 주입된다.  
<br>

```java
@Configuration
public class AppContext2 {
    @Autowired
    private MemberDao memberDao;
    ...
}
```
그렇다면 처음에 살펴본 설정 클래스 내에서 `@Autowired`도 동일하게 동작하는 것일까? 사실 스프링 컨테이너는 `AppContext2` 객체도 빈으로 등록한다. 그리고 `@Autowired` 어노테이션이 붙은 필드에 해당 타입의 빈 객체를 주입하는 것이다.  
<br>

```java
ApplicationContext context = new AnnotationConfigApplicationContext(AppContext1.class, AppContext2.class);
AppContext2 appContext2 = context.getBean(AppContext.class);
System.out.println(appContext2 != null); //true 출력
```

위 코드를 실행해보면 스프링 컨테이너가 `@Configuration` 어노테이션을 붙인 설정 클래스를 스프링 빈으로 등록해두었다는 것을 확인할 수 있다.  
<br>

## @Import

아까 두 개 이상의 설정 클래스를 사용할 경우, `AnnotationConfigApplicationContext` 클래스 생성자의 인수가 가변인수이기 때문에 설정 클래스를 모두 써서 사용할 수 있다고 하였다. 그런데 이 방법 말고도 두 개 이상의 설정 클래스를 사용하는 또 다른 방법이 있는데 바로 `@Import` 어노테이션을 사용하는 것이다. `@Import` 어노테이션은 함께 사용할 설정 클래스를 지정한다.  
<br>

```java
@Configuration
@Import(AppContext2.class)
public class AppContext1 {

    @Bean
    public MemberDao memberDao() {
        return new MemberDao();
    }
}
```

위 코드 처럼 `AppContext1`에다가 `@import` 어노테이션을 붙여주면 여기서 지정한 `AppContext2` 설정 클래스도 함께 사용한다는 의미가 된다. 따라서 스프링 컨테이너를 생성할 때 `AppContext2`를 생성자 파라미터에 작성해주지 않아도 된다.  
<br>

```java
context = new AnnotationConfigApplicationContext(AppContext1.class);
```

이렇게만 해줘도 `AppContext1, AppContext2`를 모두 파라미터에 작성한 것 처럼 두 설정 클래스를 사용할 수 있다.  

그리고 만약 더 많은 설정 클래스를 함께 사용하고 싶다면 다음과 같이 배열을 이용해서 두 개 이상의 설정 클래스를 묶어줄 수 있다.  
<br>

```java
@Import( {AppContext2.class, AppContext3.class, ...} )
```

참고로 `@Import`를 사용해서 포함된 클래스가 또다시 `@Import`를 사용할 수 있다. 이렇게 설정 클래스들이 계층 구조를 가지면서 추가되면 스프링 컨테이너를 생성할 때는 여전히 아무런 수정 없이 최상위 설정 클래스 한 개만 사용하면 된다.
<br>

# 주입 대상 객체의 형태

이렇게 설명을 따라오다 보면 주입할 객체는 모두 스프링 빈으로 설정해야할 것만 같다. 하지만 꼭 빈 객체일 필요는 없다. 객체를 빈으로 등록할 때와 등록하지 않을 때의 차이는 스프링 컨테이너가 해당 객체를 관리하는지 여부이다. 따라서 빈으로 등록하지 않으면 스프링 컨테이너로부터 `getBean()` 메서드를 사용하여 객체를 구할 수 없고 자동 주입, 라이프사이클 관리 등 다양한 기능을 사용할 수 없게된다. `@Autowired`도 같은 이유로 사용할 수 없다. 만약 스프링 컨테이너가 제공하는 다양한 관리 기능이 필요없고 `getBean()` 메서드로 객체를 구할 필요도 없는 객체라면 빈 객체로 꼭 등록할 필요는 없다.  

하지만 최근에는 의존 자동 주입 기능을 프로젝트 전반에 걸쳐 사용하는 추세이기 때문에 의존 주입 대상은 스프링 빈으로 등록하는 것이 보통이다.  
<br>
<br>


--- 
해당 포스팅은 [초보 웹 개발자를 위한 스프링5 프로그래밍 입문 - 최범균 저](https://www.aladin.co.kr/shop/wproduct.aspx?ItemId=157472828)를 정리하며 작성하였습니다.



