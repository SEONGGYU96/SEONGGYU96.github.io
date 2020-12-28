---
layout: post
title: "Spring5 : 스프링 시작하기"
subtitle: "Spring5 study (2)"
author: "GuGyu"
header-style: text
tags:
  - Spring
  - Spring5
  - Java
  - Study
  - Server
---
  
# 스프링 프로젝트 시작하기

스프링 프로젝트를 시작하기에 앞서 필요한 디렉토리를 만들어야한다. 생성할 디렉토리 구조는 다음과 같다.  
```
root  
ㄴ src  
    ㄴ main  
        ㄴ java  
```

## Maven 프로젝트 생성

`root` 디렉토리 안에 다음과 같이 `pom.xml` 파일을 작성한다.
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
		 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		 xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
		http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<groupId>com.gugyu</groupId>
	<artifactId>spring5-chapter02</artifactId> <!-- 프로젝트 식별자 지정-->
	<version>0.0.1-SNAPSHOT</version>

	<dependencies>
		<dependency>
			<!-- 5.0.2.RELEASE 버전의 spring-context 모듈을 사용한다고 설정-->
			<groupId>org.springframework</groupId>
			<artifactId>spring-context</artifactId>
			<version>5.0.2.RELEASE</version>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<!--1.8 버전을 기준으로 자바 소스를 컴파일하고 결과 클래스 생성. 소스 코드를 읽을 땐 UTF-8-->
				<artifactId>maven-compiler-plugin</artifactId>
				<version>3.8.1</version>
				<configuration>
					<source>1.8</source>
					<target>1.8</target>
					<encoding>utf-8</encoding>
				</configuration>
			</plugin>
		</plugins>
	</build>

</project>
```

`pom.xml`은 Maven 프로젝트의 핵심 파일이고 프로젝트에 대한 설정 정보를 관리하는 파일로서 프로젝트에서 필요로 하는 의존 모듈이나 플러그인 등에 대한 설정을 담는다.

### Maven 의존 설정

위에서 생성한 `pom.xml`에는 `dependency`와 `plugin`, 두 가지 정보를 설정했다. 이 중 `dependency`는 다음과 같다.

```xml
<dependency>
    <!-- 5.0.2.RELEASE 버전의 spring-context 모듈을 사용한다고 설정-->
	<groupId>org.springframework</groupId>
	<artifactId>spring-context</artifactId>
    <version>5.0.2.RELEASE</version>
</dependency>
```

Maven은 한 개의 모듈을 `artifact`라는 단위로 관리한다. 위 설정은 `spring-context`라는 식별자를 가진 `5.0.2.RELEASE` 버전의 `artifact`에 대한 `dependency`를 추가한 것이다. 각 `artifact`의 완전한 이름은 `아티팩트이름-버전.jar` 이므로 결과적으로 Maven 프로젝트의 소스 코드를 컴파일하고 실행할 때, 사용할 클래스 경로에 `spring-context-5.0.2.RELEASE.jar` 파일을 추가한다는 것을 의미한다. 하지만 이렇게 `dependency`를 추가하여도 `spring-context-5.0.2.RELEASE.jar`파일은 내 파일 시스템 어디에도 존재하지 않는다. 언제 어디에 생성되는 걸까?  

Maven은 코드를 컴파일하거나 실행할 때 다음 과정을 거쳐 `artifact` 파일을 구해온다.
1. Maven Local Repository 에서 `[groupId]\[artifactId]\[version]` 디렉토리에 `아티팩트이름-버전.jar` 형식의 이름이 있는지 검사한다.
2. 파일이 존재하면 이 파일을 사용한다.
3. 파일어 없으면 Maven Remote Repository 로 부터 해당 파일을 다운로드하여 Local Repository 에 복사한 뒤, 그 파일을 사용한다.  

Maven은 기본적으로 `~\.m2\repository` 디렉토리를 Local Repository 로 사용한다. 실제 아티팩트 파일은 위에서 언급한 경로에 존재하는데, `spring-context`의 경우 파일의 위치는 `~\.m2\repository\org\springframework\spring-context\5.0.2.RELEASE` 가 된다.  

하지만 위 디렉토리를 지금 찾아봐도 해당 파일은 찾을 수가 없다. Maven은 이 파일이 "필요할 때" 다운로드한다. 이를 확인하기 위해서 터미널에서 `pom.xml`을 작성한 루트 디렉토리로 이동한 다음 `mvn compile` 명령어를 실행해보자. 그럼 프로젝트에 필요한 수많은 파일을 다운로드하는 것을 확인할 수 있다.

```
...
Downloading from central: https://repo.maven.apache.org/maven2/org/springframework/spring-context/5.0.2.RELEASE/spring-context-5.0.2.RELEASE.jar
...
```

그 중 위와 같은 문구를 찾을 수 있는데 `https://repo.maven.apache.org/maven2/org` 라는 repository에서 `spring-context-5.0.2.RELEASE.jar` 을 다운로드하는 것을 알 수 있다. 저 repository 가 `Maven Central Repository` 이며 이곳에서 필요한 파일을 다운로드한 뒤에 local repository에 복사한다. 이제 해당 경로를 살펴보면 파일이 존재하는 것을 확인할 수 있다.  

### 의존 전이 (Transitive Dependencies)

`mvn compile`을 실행하면 `spring-context-5.0.2.RELEASE.jar` 파일 외에도 다양한 아티팩트 파일을 다운로드하는 것을 확인할 수 있다. 여기에는 컴파일을 수행하는데 필요한 maven 컴파일러 플러그인과 같이 maven과 관련된 파일이 포함된다. 추가로 `dependency`에서 설정한 아티팩트가 다시 의존하는 파일도 포함된다.  

예를 들어 `spring-context-5.0.2.RELEASE.jar` 파일을 다운로드 하기 전에 `spring-context-5.0.2.RELEASE.pom` 파일을 다운로드한다. 해당 파일 안에는 `aspectjwearver`, `srping-aop`, `spring-beans` 등 다양한 아티팩트에 대한 의존이 명시되어있다. 즉 `spring-context`를 사용하려면 위와 같은 다른 아티팩트도 추가로 필요한 것이다. 따라서 maven은 `srping-context`에 대한 의존이 설정이 있으면 `spring-context`가 의존하는 다른 아티팩트도 함께 다운로드한다. 의존한 아티팩트가 또 다시 의존하고 있는 다른 아티팩트가 있다면 그 아티팩트도 함께 다운로드 한다. 이를 `의존 전의 (Transitive Dependencies)` 라고 한다.  

### Maven 기본 디렉토리 구조

앞서 프로젝트 root 디렉토리를 기준으로 `src\main\java` 디렉토리를 생성했다. 이는 Maven에 정의되어있는 기본 디렉토리 구조로서 자바 소스 코드가 위치하게 된다. `XML`이나 `properties` 파일과 같이 자바 소스 이외의 다른 리소스 파일이 필요하다면 `src\main\resources` 디렉토리에 해당 파일들을 위치시키면 된다.  

웹 어플리케이션을 개발할 때에는 `src\main\webapp` 디렉토리를 웹 어플리케이션 기준 디렉토리로 사용하며, 이 디렉토리를 기준으로 `JSP` 코드나 `WEB-INF\web.xml` 파일 등을 작성해서 넣는다. 이를 간략하게 나타내자면 다음과 같다.

```
root
ㄴ pom.xml
ㄴ src
    ㄴ main
        ㄴ java
        ㄴ resources
        ㄴ webapp
            ㄴ WEB-INF
            ㄴ web.xml
```

### Maven 프로젝트 임포트

나는 `IntelliJ IDEA`를 사용하여 아까 만든 root 디렉토리를 open 했다.  
<br>

<img width="600" src="https://user-images.githubusercontent.com/57310034/103169053-f4319700-487b-11eb-8c9f-52ea6ae758ec.png"/>  

`External Libraries` 를 보면 다운로드 된 `.jar` 아티팩트들을 확인할 수 있다.  

--- 
<br>

## Gradle 프로젝트 생성

Gradle 프로젝트를 생성하는 방법은 Maven과 유사하며 디렉토리 구조도 동일하다. 다만 `pom.xml` 대신에 `build.gradle` 파일을 사용한다.  
<br>

```gradle
apply plugin: 'java' //자바 플러그인 적용

//자바 1.8 버전으로 컴파일
sourceCompatibility = 1.8
targetCompatibility = 1.8
//UTF-8 인코딩
compileJava.options.encoding = "UTF-8"

//dependency 모듈을 MavenCentralRepository 에서 다운로드 하도록 함
repositories {
    mavenCentral()
}

//spring-context 모듈에 대한 dependency 설정
dependencies {
    compile 'org.springframework:spring-context:5.0.2.RELEASE'
}

//소스 코드를 공유할 때 gradle 설치 없이 gradle 명령어를 실행할 수 있는 wrapper 설정
wrapper {
    gradleVersion = '6.7.1'
}
```
그런 뒤, 위와 같이 root 디렉토리에 `build.gradle` 파일을 생성한다. 그 다음, 동일한 위치에서 `gradle wrapper` 명령어를 실행해서 `wrapper` 파일을 생성하자. 그럼 `gradlew.bat`, `gradlew` 파일과 `gradle` 디렉토리가 생성된다. 이 두 파일은 윈도우와 리눅스에서 사용할 수 있는 실팽파일로 `gradle` 명령어 대신 사용할 수 있는 `wrapper` 파일이다. 이 파일을 사용하면 gradle 설치 없이 명령어를 실행할 수 있다. 소스 코드를 공유할 때 이 두 파일과 디렉토리를 공유하면 gradle을 설치하지 않은 개발자도 명령어를 실행할 수 있게 되는 것이다.  

`$ /.gradlew compileJava` 명령어를 실행하면 자바 코드를 컴파일하고 필요한 모듈을 다운로드 받는다.  

### Gradle 프로젝트 임포트 

<img width="600" src="https://user-images.githubusercontent.com/57310034/103183913-b32b9800-48f8-11eb-8e55-cb2143918fff.png"/>
Maven과 동일하게 root 디렉토리를 IntelliJ로 import 해주었다. 의존 모듈들을 확인할 수 있다.  
<br>

---

## 예제 작성

예제 코드를 작성하기에 앞서 `root\src\main\java` 디렉토리에 하위 패키지를 생성한다. 나는 `AndroidFramework`에서 하던 것 처럼 `root\src\main\java\com\gugyu\chapter02` 를 추가해주었다. 거기에 `Greeter.java` 파일을 생성해보자.  
<br>

```java
package com.gugyu.chapter02;

public class Greeter {
    private String format;

    public void setFormat(String format) {
        this.format = format;
    }
    
    public String greet(String guest) {
        return String.format(format, guest);
    }
}
```
단순히 문자열 포맷을 이용해서 새로운 문자열을 생성하는 `greet()` 메서드를 작성해주었다. 다음으로는 `AppContext.java` 파일을 추가해주자.  
<br>

```java
package com.gugyu.chapter02;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppContext {
    
    @Bean
    public Greeter greeter() {
        Greeter greeter = new Greeter();
        greeter.setFormat("%s, Hello spring!");
        return greeter;
    }
}
```
`@Configuration` 어노테이션은 해당 클래스를 스프링 설정 클래스로 지정한다.  

스프링은 객체를 생성하고 초기화하는 기능을 제공하는데, 스프링이 생성하는 객체를 `빈(Bean)` 객체라고 한다. 위 코드에서 `@Bean` 어노테이션을 붙임으로써 해당 메서드가 생성한 객체를 스프링이 관리하는 빈 객체로 등록한다. `@Bean` 어노테이션을 붙인 메서드의 이름은 빈 객체를 구분할 때 사용하게 된다.  

이제 마지막으로 `Main.java` 파일을 추가하자.  
<br>

```java
package com.gugyu.chapter02;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppContext.class);
        Greeter greeter = context.getBean("greeter", Greeter.class);
        String message = greeter.greet("Spring5");
        System.out.println(message);
        context.close();
    }
}
```
`AnnotationConfigApplicationContext` 클래스는 자바 설정에서 정보를 읽어와 빈 객체를 생성하고 관리한다. `AnnotationConfigApplicationContext` 객체를 생성할 때는 앞서 작성한 `AppContext` 클래스를 생성자 파라미러토 전달한다. 그럼 `AppContext`에서 정의한 `@Bean` 설정 정보를 읽어와 `Greeter` 객체를 생성하고 초기화한다.  

`getBean()` 메서드는 `AnnotaionConfigApplicationContext`가 생성한 빈 객체를 검색할 때 사용된다. 첫 번째 파라미터는 빈 객체의 이름(`@Bean` 어노테이션이 붙은 메서드의 이름)이며, 두 번째 파라미터는 검색할 빈 객체의 타입이다. 위 코드에서는 `AppContext.greeter()` 메서드가 생성한 `Greeter` 객체를 리턴하게 된다.  
<br>

```
...
> Task :Main.main()
Spring5, Hello spring!
...
```
프로젝트를 실행하면 콘솔에서 위와 같이 출력되는 것을 확인할 수 있다 !  
<br>

# 스프링은 객체 컨테이너

스프링의 핵심 기능은 객체를 생성하고 초기화하는 것이다. 이와 관련된 기능은 `ApplicationContext` 라는 인터페이스에 정의되어 있고 `AnnotationConfigApplicationContext` 클래스는 이 인터페이스를 알맞게 구현한 클래스 중 하나다. `XML` 파일이나 `Groovy` 설정 코드를 이용해서 객체 생성/초기화를 수행하는 클래스도 존재한다.  
<br>

<img src="https://user-images.githubusercontent.com/57310034/103185183-94c89b00-48fe-11eb-85d0-bb06607ad781.png"/>  

`AnnotationConfigApplicationContext` 클래스의 계층도 **일부**를 위와 같이 나타낼 수 있다.  
- `BeanFactory` : 객체 생성과 검색에 대한 기능을 정의 (ex. `getBean()`), 싱글턴/프로토타입 여부 확인
- `ApplicationContext` : 메시지, 프로필/환경 변수 등을 처리하는 기능을 정의
- `AnnotationConfigApplicationContext` : 자바 어노테이션을 이용하여 클래스로부터 객체 설정 정보를 가져온다.
- `GenericXmlApplicationContext` : XML로부터 객체 설정 정보를 가져온다.
- `GenericGroovyApplicationContext` : `groovy` 코드를 이용해 설정 정보를 가져온다.  

어떤 구현 클래스를 사용하든 각 구현 클래스는 설정 정보로부터 빈(bean) 객체를 생성하고 그 객체를 **내부**에 보관한다. 그리고 `getBean()` 메서드를 실행하면 해당하는 빈 객체를 제공한다. 이처럼 `ApplicationContext(또는 BeanFactory)`는 빈 객체의 생성, 초기화, 보관, 제거 등을 관리하고 있어서 이를 `컨테이너(Container)`라고도 부른다.  

## 싱글턴(Singleton) 객체

싱글턴 객체에 대해 알아보기 위해 `Main.java` 코드를 일부 수정한다.  
<br>

```java
package com.gugyu.chapter02;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppContext.class);
        Greeter greeter1 = context.getBean("greeter", Greeter.class);
        Greeter greeter2 = context.getBean("greeter", Greeter.class);
        System.out.println("(greeter1 == greeger2) == " + (greeter1 == greeter2));
        context.close();
    }
}
```
위와 같이 `getBean()` 메서드를 통해서 동일한 이름의 빈 객체를 구해서 `greeter1`, `greeter2`에 각각 할당해 비교해보았다.  
<br>

```
...
> Task :Main.main()
(greeter1 == greeger2) == true
...
```
즉 `getBean("greeger", Greeter.class)` 메서드는 동일한 객체를 리턴하고 있었다. 별도 설정을 하지 않을 경우 스프링은 한 개의 빈 객체만을 생성하며, 이때 빈 객체는 "싱글턴(singleton) 범위를 갖는다"고 표현한다. 기본적으로 스프링은 `@Bean` 어노테이션에 대해 한 개의 빈 객체를 생성한다. 따라서 각각 다른 객체로 사용하고 싶다면 `@Bean` 어노테이션에 해당하는 메서드의 이름을 바꿔 또 한번 작성해주어야 한다.  
<br>

--- 
해당 포스팅은 [초보 웹 개발자를 위한 스프링5 프로그래밍 입문 - 최범균 저](https://www.aladin.co.kr/shop/wproduct.aspx?ItemId=157472828)를 정리하며 작성하였습니다.



