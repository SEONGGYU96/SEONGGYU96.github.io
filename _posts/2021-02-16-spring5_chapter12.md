---
layout: post
title: "Spring5 : MVC 프로그래밍 (3)"
subtitle: "메시지, 커맨드 객체 검증"
author: "GuGyu"
header-style: text
tags:
  - Spring
  - Spring5
  - Java
  - Study
  - Server
  - MVC
  - Validator
---

[Spring5: MVC 프로그래밍 (2)](https://seonggyu96.github.io/2021/02/03/spring5_chapter11/)  

지금까지 스프링 MVC의 기본적인 컨트롤러 구현 방법을 살펴봤다. 이번에는 메시지를 출력하는 방법과 커맨드 객체의 값을 검증하는 방법에 대해서 알아보고자 한다.

<br>

## 메시지 출력  

사용자 화면에 보일 문자열은 JSP에 직접 코딩한다. 예를 들어 로그인 폼을 보여줄 때 '아이디', '비밀번호' 등의 문자열을 다음과 같이 뷰 코드에 직접 삽입한다.  

```jsp
<label>이메일</label>
<input type="text" name="email"/>
```

'이메일'과 같은 문자열은 로그인 폼, 회원 가입 폼, 회원 정보 수정 폼에서 반복해서 사용된다. 따라서 이렇게 문자열을 직접 하드코딩하면 동일 문자열을 변경할 때 문제가 있다. 예를 들어 폼에서 사용할 '이메일'을 '이메일 주소'로 변경하기로 했다면 각 폼을 출력하는 JSP를 모두 변갱해야한다. 뿐만 아니라 다국에 지원을 위해 사용자의 언어 설정에 따라 문자열을 표시해야할 때에도 큰 문제가 발생한다.  

따라서 뷰 코드에서 사용할 문자열을 언어별로 파일에 보관하고 뷰 코드는 언어에 따라 알맞은 파일에서 문자열을 읽어와 출력하는 방법이 바람직하다. 스프링은 자체적으로 이 기능을 제공하고 있기 때문에 다음과 같은 절차를 따르면 어렵지 않게 해당 기능을 구현할 수 있다.  

- 문자열을 담은 메시지 파일을 작성한다.
- 메시지 파일에서 값을 읽어오는 `MessageSource` 빈을 설정한다.
- JSP 코드에서 `<spring:message>` 태그를 사용해서 메시지를 출력한다.  

메시지 파일은 자바의 프로퍼티 파일 형식으로 작성한다. 메시지 파일을 보관하기 위해 `src/main/resource`에 message 폴더를 생성하고 이 폴어데 `label.properties` 파일을 생성한다.  

```properties
member.register=회원가입

term=약관
term.agree=약관동의
next.button=다음 단계

member.info=회원정보
email=이메일
name=이름
password=비밀번호
password.comfirm=비밀번호 확인
register.button=가입 완료
...
```

프로퍼티 파일을 작성했다면 `MessageSource` 타입의 빈을 추가해주어야한다.  

```java
@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {
    ...

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        //message 패키지에 있는 label.properties 파일을 읽도록 설정
        messageSource.setBasenames("message.label");
        messageSource.setDefaultEncoding("UTF-8");
        returen messageSource;
    }
}
```

`setBasenames("message.label")` 을 사용했기 때문에 아까 작성한 프로퍼티 파일에 대응될 것이다. 이 메서드의 인자는 가변 인자이므로 사용할 메시지 프로퍼티 목록을 전달할 수도 있다.  

여기서 주의할 점은 빈의 아이디를 반드시 `messageSource`로 지정해야한다는 것이다. 다른 이름을 사용할 경우 정상적으로 동작하지 않는다.  

이렇게 등록된 메시지 빈을 사용하려면 다음과 같은 과정을 거치면 된다.  

- <spring:message> 커스텀 태그를 사용하기 위한 태그 라이브러리 설정 추가
- <spring:message> 태그를 이용해서 메시지 출력

```jsp
<!-- 태그 라이브러리 설정 추가-->
<%@ page contentType="text/html; charset=utf-8" %>
<!DOCTYPE html>
<html>
    <head>
        <title><spring:message code="member.register" /><title>
    </head>
    <body>
        <h2><spring:message code="term" /><h2>
        <form>
        <label>
            <input type="checkbox" name="agree" value="true">
            <spring:message code="term.agree" />
        </label>
        <input type="submit" value="<spring:message code="next.button" />"/>
        </form>
    </body>
<html>
```  

`<spring:message>` 태그는 `MessageSource`로부터 `code` 속성과 일치하는 값을 가진 프로퍼티 값을 출력한다.  

<br>

### MessageSource

스프링은 Locale(지역)에 상관없이 일관된 방법으로 메시지를 관리할 수 있는 `MessageSource` 인터페이스를 정의하고 있다. 특정 Locale에 해당하는 메시지가 필요한 코드는 `MessageSource#getMessage()` 메서드를 통해 가져와 사용하는 방식이다.  

```java
public interface MessageSource {
    String getMessage(String code, Object[] args, String defaultMessage, Locale locale);

    String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException;
    ...
}
```

`getMessage()` 메서드의 code 파라미터는 메시지를 구분하기 위한 코드이고 locale 파라미터는 지역을 구분하기 위한 Locale이다. 같은 코드라 하더라도 지역에 따라 다른 메시지를 제공할 수 있도록 설계되었다.  

`MessageSource`의 구현체로는 자바의 프로퍼티 파일로부터 메시지를 읽어오는 `ResourceBundleMessageSource` 클래스를 사용한다. 이 클래스는 메시지 코드와 일치하는 이름을 가진 프로퍼티의 값을 메시지로 제공한다.  

`ResourceBundleMessageSource` 클래스는 자바의 리소스번들(ResourceBundle)을 사용하기 때문에 해당 프로퍼티 파일이 클래스 패스에 위치해야한다. 앞선 예시 코드에서도 클래스 패스에 포함되는 src/main/resources에 프로퍼티 파일을 위치시켰다.  

`<spring:message>` 태그는 스프링 설정에 등록된 `messageSource` 빈을 이용해서 `getMessage()`를 호출하고 필요한 메시지를 구한다. 이때 `<spring:message>` 태그의 `code` 속성의 값을 파라미터로 사용한다.  

<br>

### `<spring:message>` 태그  

`label.properties`에는 다음과 같은 형태의 프로퍼티도 작성할 수 있다.  

```properties
register.done=<strong>{0}님 ({1})</string>, 회원 가입을 완료하였습니다.
```

이 프로퍼티는 값 부분에 {0}, {1}을 포함한다. 이는 인덱스 기반 변수 중 0번과 1번 인덱스의 값으로 대치되는 부분을 표시한 것이다. `MessageSource#getMessage()` 메서드는 인덱스 기반 변수를 전달하기 위해 다음과 같이 `Object` 배열 타입의 파라미터를 사용한다.  

```java
String getMessage(String code, Object[] args, String defaultMessage, Locale locale);

String getMessage(String code, Object[] args, Locale locale);
```  

위 메서드를 사용해서 `MessageSource` 빈을 직접 실행한다면 다음과 같이 `Object` 배열을 생성해서 인덱스 기반 변수값을 전달할 수 있다.  

```java
Object[] args = new Object[1];
args[0] = "...";
args[1] = "...";
messageSource.getMessage("register.done", args, Locale.KOREA);
```

`<spring:message>` 태그를 사용할 때에는 `arguments` 속성을 사용해서 인덱스 기반 변수값을 전하는데, 두 개 이상의 값을 전달해야할 경우 다음 방법 중 하나를 사용한다.  

- 콤마로 구분한 문자열
- 객체 배열
- `<spring:argument>` 태그 사용

다음은 콤마로 구분한 예이다.

```jsp
<spring:message code"register.done" arguments="${registerRequest.name},${registerRequest.email}" />
```

<br>

다음은 `<spring:argument>` 태그를 사용한 예이다.  

```jsp
<spring:message code="register.done">
    <spring:argument value="${registerRequest.name}" />
    <spring:argument value="${registerRequest.email}" />
</spring:message>
```

<br>

## 커맨드 객체의 값 검증과 에러 메시지 처리  

회원가입 폼에서 올바르지 않은 이메일 주소를 입력해도 가입처리가 되면 곤란하기 때문에 입력한 값에 대한 검증 처리가 필요하다. 또한 중복된 이메일을 입력해서 다시 폼을 보여줄 때 왜 가입에 실패했는지 알려주지 않으면 사용자는 혼란을 겪게 될 것이다. 이렇듯 폼 값 검증과 에러 메시지 처리는 어플리케이션을 개발함에 있어 필수이다.  

스프링은 이 두 가지 문제를 처리하기 위해 다음 방법을 제공하고 있다.  

- 커맨드 객체를 검증하고 결과를 에러 코드로 저장
- JSP에서 에러 코드로부터 메시지를 출력  

<br>

### 커맨드 객체 검증과 에러 코드 지정하기   

스프링 MVC에서 커맨드 객체의 값이 올바른지 검사하려면 다음의 두 인터페이스를 사용한다.  

- `org.springframework.validation.Validator`
- `org.springframework.validation.Errors`

객체를 검증할 때 사용하는 `Validator` 인터페이스는 다음과 같다.  

```java
public interface Validator {
    boolean supports(Class<?> clazz);
    void validate(Object target, Errors errors);
}
```

`supports()` 메서드는 Validator가 검증할 수 있는 타입인지 검사한다. 그리고 `validate()` 메서드에서는 일반적으로 다음과 같이 구현한다.  

- 검사 대상 객체의 특정 프로퍼티나 상태가 올바른지 검사  
- 올바르지 않다면 `Errors`의 `rejectValue()`메서드를 이용해서 에러 코드 저장

```java
public class RegisterRequestValidator implements Validator {
    private static final String emailRegExp = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + 
    "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private Pattern pattern;

    public RegisterRequestValidator() {
        pattern = Pattern.compile(emailRegExp);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        //파라미터로 들어온 clazz 객체가 RegisterRequest 클래스로 타입 변환이 가능한지 확인 
        return RegisterRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        RegisterRequest regReq = (RegisterRequest) target;
        if (regReq.getEmail() == null || regReq.getEmail().trim().isEmpty()) {
            errors.rejectValue("email", "required");
        } else {
            //이메일이 형식에 맞는지 확인
            Matcher matcher = pattern.matcher(regReq.getEmail());
            if (!matcher.matches()) {
                errors.rejectValue("email", "bad");
            }
        }
        //이름이 비었는지 확인
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "required");
        //비밀번호가 비어있는지 확인
        ValidationUtils.rejectIfEmpty(errors, "password", "required");
        ValidationUtils.rejectIfEmpty(errors, "confirmPassword", "required");
        
        if (!regReq.getPassword().isEmpty()) {
            //두 번 입력한 비밀번호가 서로 같은지 확인
            if (!regReq.isPasswordEqualToConfirmPassword()) {
                errors.rejectValue("confirmPassword", "nomatch");
            }
        }
    }
}
```

이렇게 `Validate` 인터페이스를 구현해두면 스프링 MVC가 자동으로 검증 기능을 수행한다. 

`rejectValue()` 메서드는 첫 번째 파라미터로 프로퍼티의 이름을 전달받고, 두 번째 파라미터로 에러 코드를 전달받는다. JSP 코드에서는 여기서 지정한 에러 코드를 이용해서 에러 메시지를 출력한다.  

`ValidationUtils` 클래스는 객체의 값 검증 코드를 간결하게 작성할 수 있도록 도와준다.

```java
String name = regReq.getName();
if (name == null || name.trin().isEmpty()) {
    errors.rejectValue("name", "required");
}
//위 코드를 아래로 함축할 수 있다.
ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "required");
```

그런데 위 코드에서 `ValidateionUtils`는 검사 대상 객체인 `target`을 전달하지 않았는데 어떻게 검증하는 것일까? 스프링 MVC에서 `Validator`를 사용하는 코드는 요청 매핑 어노테이션 적용 메서드에 `Errors` 타입 파라미터를 전달받고, 이 `Errors` 객체를 `Validator`의 `validate()` 메서드에 두 번째 파라미터로 전달한다. 

```java
@Controller
public class RegisterController {
    ...
    @PostMapping("/register/step3")
    public String handleStep3(RegisterRequest regReq, Errors errors) {
        new RegisterRequestValidator().validate(regReq, errors);
        //에러가 있다면 이전 단계로 돌아감
        if (errors.hasErrors()) {
            return "register/step2";
        }
        try {
            memberRegisterService.regist(regReq);
            return "register/step3";
        } catch (DuplicateMemberException exception) {
            //이메일이 중복되면 Error 발생
            errors.rejectValue("email", "duplicate");
            return "register/step2";
        }
    }
}
```

위 코드 처럼 요청 매핑 어노테이션 적용 메서드의 커맨드 객체 파라미터 뒤에 `Errors` 타입 파라미터가 위치하면, 스프링 MVC는 `handleStep3()` 메서드를 호출할 때 커맨드 객체와 연결된 `Errors` 객체를 생성해서 파라미터로 전달한다. 이 `Errors` 객체는 커맨드 객체의 특정 프로퍼티 값을 구할 수 있는 `getFieldValue()` 메서드를 제공한다. 따라서 `ValidationUtile.rejectIfEmptyOrWhitespace()` 메서드는 커맨드 객체를 전달받지 않아도 `Errors` 객체를 이용해서 지정한 값을 구할 수 있다.  

커맨드 객체의 특정 프로퍼티에 문제가 있는 경우 `Errors#rejectValue()` 메서드를 호출한다. 그러나 커맨드 객체 그 자체에 문제가 있는 경우 `reject()` 메서드를 사용한다. 예를 들어 로그인 아이디와 비밀번호를 잘못 입력한 경우 아이디와 비밀번호가 불일치한다는 메시지를 보여줘야한다. 이 경우 특정 프로퍼티에 에러를 추가하기 보다는 커맨드 객체 자체에 에러를 추가하는 것이 더 옳아보인다.  

`reject()` 메서드는 개별 프로퍼티가 아닌 객체 자체에 에러 코드를 추가하므로 글로벌 에러라고 부른다.  

요청 매핑 어노테이션을 붙인 메서드에 `Error` 타입의 파라미터를 추가할 때 주의할 점은 `Errors` 타입 파라미터는 반드시 커맨드 객체를 위한 파라미터 다음에 위치해야한다는 것이다. 그렇지 않으면 요청 처리를 올바르게 하지 못하고 예외가 발생한다.  

<br>

### 커맨드 객체의 에러 메시지 출력하기  

`Errors`에 에러 코드를 추가하면 JSP는 스프링이 제공하는 `<form:errors>` 태그를 사용해서 에러에 해당하는 메시지를 출력할 수 있다.  

```jsp
...
<form:form action="step3" commandName="registerRequest">
<p>
    <label><spring:message code="email" /> : </br>
    <form:input path="email"/>
    <form:errors path="email"/>
    </label>
</p>
...
</form:form>
```

위 처럼 코드를 작성하면 "email" 프로퍼티에 에러 코드가 존재할 때 해당하는 메시지를 출력한다. 에러 코드가 두 개 이상 존재하면 각 에러 코드에 해당하는 메시지가 출력된다.  

에러 코드에 해당하는 메시지 코드를 찾을 때에는 다음 규칙을 따른다.  

1. 에러코드 + "." + 커맨드객체이름 + "." + 필드명
2. 에러코드 + "." + 필드명
3. 에러코드 + "." + 필드타입
4. 에러코드  

프로퍼티 타입이 List이거나 목록인 경우 다음 순서를 사용해서 메시지 코드를 생성한다.  

1. 에러코드 + "." + 커맨드객체이름 + "." + 필드명[인덱스].중첩필드명
1. 에러코드 + "." + 커맨드객체이름 + "." + 필드명.중첩필드명
1. 에러코드 + "." + 필드명[인덱스].중첩필드명
1. 에러코드 + "." + 필드명.중첩필드명
1. 에러코드 + "." + 중첩필드명
1. 에러코드 + "." + 필드타입
7. 에러코드  

위 순서대로 메시지 코드를 검색하고 먼저 검색되는 메시지 코드를 사용한다.  

글로벌 에러 코드는 다음 순서대로 메시지 코드를 검색한다.  

1. 에러코드 + "." + 커맨드객체이름
2. 에러코드  

메시지를 찾을 때에는 앞서 설명한 `MessageSource`를 사용하므로 에러 코드에 해당하는 메시지를 프로퍼티 파일에 추가해주어야한다.  

```properties
...
required=필수항목입니다.
bad.email=이메일이 올바르지 않습니다.
duplicate.email=중복된 이메일입니다.
nomatch.confirmPassword=비밀번호와 확인이 일치하지 않습니다.  
```

<br>

## 글로벌 범위 Validator와 컨트롤러 범위 Validator

스프링 MVC는 모든 컨트롤러에 적용할 수 있는 글로벌 Validator와 단일 컨트롤러에 적용할 수 있는 Validator를 설정하는 방법을 제공한다. 이를 사용하면 `@Valid` 어노테이션을 사용해서 커맨드 객체에 검증 기능을 적용할 수 있다.  

### 글로벌 범위 Validtor 설정  

글로벌 범위라 Validator는 모든 컨트롤러에 적용할 수 있다. 이를 위해서는 다음 두 가지를 설정해야한다.  

- 설정 클래스에서 `WebMvcConfigurer`의 `getValidator()` 메서드가 `Validator` 구현 객체를 리턴하도록 구현  
- 글로벌 범위 `Validator`가 검증할 커맨드 객체에 `@Valid` 어노테이션 적용  

```java
@Configuration
@EableWebMvc
public class MvcConfig implements WebMvcConfigurer {
    ...
    @Override
    public Validator getValidator() {
        return new RegisterRequestValidator();
    }
}
```

스프링 MVC는 `getValidator()` 메서드가 리턴한 객체를 글로벌 범위 `Validator`로 사용한다. 그럼 해당 Validator가 검증하는 커맨드 객체를 다루는 컨트롤러에 `@Valid` 어노테이션만 붙여주면 된다.  

참고로 `@Valid` 어노테이션은 `Bean Validation API`에 포함되어 있기 때문에 다음과 같은 의존을 추가해주어야 한다.  

```gradle
dependencies {
    ...
    implementation "javax.validation:validation-api:2.0.1.Final"
}
```

```java
@Controller
public class RegisterController {
    ...
    @PostMapping("/register/step3")
    public String handleSteb3(@Valid RegisterRequest regReq, Errors errors) {
        //Validator 인스턴스를 생성해 직접 검증할 필요가 없음
        //new RegisterRequestValidator().validate(regReq, errors);
        if (errors.hasErrors()) {
            return "register/step3";
        }
        ...
    }
    ...
}
```

이렇게 `RegisterRequest` 파라미터 앞에 `@Valid` 어노테이션을 붙임으로써 검증이 자동으로 이루어지고, 메서드 내에서는 곧바로 `Errors` 객체를 이용하여 검증 에러를 확인하면 된다. 만약 `Errors` 타입 파라미터가 없으면 검증 실패 시 400 에러를 응답하니 주의하자.  

<br>

### @InitBinder 어노테이션을 이용한 컨트롤러 범위 Validator

`@InitBinder` 어노테이션을 이용하면 컨트롤러 범위 `Validator`를 설정할 수 있다.  

```java
@Controller
public class RegisterController {
    ...
    @PostMapping("/register/step3")
    public String handleSteb3(@Valid RegisterRequest regReq, Errors errors) {
        //Validator 인스턴스를 생성해 직접 검증할 필요가 없음
        //new RegisterRequestValidator().validate(regReq, errors);
        if (errors.hasErrors()) {
            return "register/step3";
        }
        ...
    }
    ...
    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        //해당 컨트롤러에서 사용할 Validator 설정
        binder.setValidator(new RegisterRequestValidator());
    }
}
```  

글로벌 범위의 Validator를 사용할 때와 동일하게 `@Valid` 어노테이션을 사용하였으나 여기서 사용될 검증 객체는 `@initBinder`메서드에서 설정한 Validator이다. 참고로 `@InitBinder`가 붙은 메서드는 컨트롤러의 요청 처리 메서드를 실행하기 전에 매번 실행된다.  

하나의 커맨드 객체에 여러 개의 Validator를 사용해 검증할 수 있다. `@InitBinder` 에서 `setValidator()`를 통해 Validator를 설정한 경우, 검증에 사용할 Validator 목록을 비우고 해당 Validator를 추가한다. 따라서 글로벌 범위 Validator는 검증에 사용되지 않는다. 하지만 `addValidator()`를 사용하면 기존 Validator 목록에 해당 Validator를 더하게 되므로, 글로벌 범위의 Validator를 사용해 먼저 검증한 후, 컨트롤러 범위의 Validator를 사용하게 된다.  

<br>

## Bean Validation을 이용한 값 검증 처리  

`@Valid` 어노테이션은 `Bean Validation` 스펙에 정의되어 있는데, 이 스펙에 포함된 다양한 어노테이션을 활용하면 `Validator` 클래스를 별도로 작성하지 않아도 커맨드 객체의 값 검증을 처리할 수 있다.  

- Bean Validation과 관련된 의존을 설정에 추가한다.
- 커맨드 객체에 `@NotNull`, `@Digits` 등의 어노테이션을 이용해서 검증규칙을 설정한다.  

추가할 의존은 Bean Validation API를 정의한 모듈과 이 API를 구현한 프로바이더이다. 프로바이더로는 `Hibernate Validator`를 사용할 것이다.  

```gradle
dependencies {
    ...
    implementation "javax.validation:validation-api:2.0.1.Final"
    implementation "org.hibernate:hibernate-validator:6.0.7.Final"
}
```

그럼 이제 커맨드 객체 내에서 간편하게 검증 규칙을 설정할 수 있다.  

```java
public class RegisterRequest {
    @NotBlank
    @Email
    private String email;
    @Size(min = 6)
    private String password;
    @NotEmpty
    private String confirmPassword;
    @NotEmpty
    private String name;
}
```

그 다음은 Bean Validation 어노테이션을 적용한 커맨드 객체를 검증할 수 있는 `OptionalValidatorFactoryBean` 클래스를 빈으로 등록해야한다. 그러나 `@EnalbeWebMvc` 어노테이션을 사용하면 이를 글로벌 범위 Validator로 알아서 등록하므로 추가로 설정할 것은 없다.  

하지만 만약 `getValidator()`를 오버라이딩하여 직접 글로벌 범위 Validator를 등록했다면 해당 설정을 삭제해야한다. 이 경우에는 `OptionalValidatorFactoryBean`을 사용하지 않기 때문이다.  

그럼 이전 방법과 똑같이 검증할 커맨드 객체에 `@Valid` 어노테이션만 붙여주면 검증을 적용한다.  

그러나 이렇게 검증을 시도하고 에러가 발생하면, 에러 코드는 이전 방법들과 상이하다. 

- 어노테이션이름.커맨드객체모델명.프로퍼티명
- 어노테이션이름.프로퍼티명
- 어노테이션이름  

```properties
NotBlank=필수 항복입니다. 공백 문자는 허용하지 않습니다.
NotEmpty=필수 항목입니다.
Size.password=암호 길이는 6자 이상이어야 합니다.
Email=올바른 이메일 주소를 입력해야 합니다.
...
```

위 처럼 어노테이션 이름을 사용한 규칙대로 메시지를 정의해주어야한다. 정의된 메시지가 없을 경우, Bean Validation 프로바이더가 제공하는 기본 메시지를 표시한다.  


<br>
<br>


--- 
해당 포스팅은 [초보 웹 개발자를 위한 스프링5 프로그래밍 입문 - 최범균 저](https://www.aladin.co.kr/shop/wproduct.aspx?ItemId=157472828)를 정리하며 작성하였습니다.  
