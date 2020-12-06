---
layout: post
title: "Effective Java - 2장 (5)"
subtitle: '객체의 생성과 파괴'
author: "GuGyu"
header-style: text
tags:
  - Java
  - Book
  - EffectiveJava
  - DesignPattern
  - Study
---
규칙5 - 자원을 직접 명시하지 말고 의존 객체 주입을 사용하라.

많은 클래스가 하나 이상의 자원에 의존한다. 이런 클래스는 종종 정적 유틸리티 클래스로 구현되곤 한다.

다음은 맞춤법 검사 클래스가 의존하는 사전 자원을 나타낸 코드다.

```java
public class SpellChecker {
    private static final Lexicon dictionary = ...;
    
    private SpellChecker() {} //객체 생성 바지
    
    public static boolean isValid(String word) { ... }
    public static List<String> suggestions(String typo) { ... }
}
```

이 방식은 그리 훌륭해 보이지 않는다.

실전에서는 특수 어휘용 사전을 두기도 하고, 테스트용 사전도 필요할 수 있다. 사전 하나로 모든 쓰임에 대응하기에는 어렵다.

여러 사전을 사용할 수 있도록 만들어보자.

**사용하는 자원에 따라 동작이 달라지는 클래스에는 정적 유틸리티 클래스나 싱글톤 방식이 적합하지 않다.**

따라서 클래스가 여러 자원 인스턴스를 지원해야하고, 클라이언트가 원하는 자원을 사용해야 한다. 이를 만족하기 위해서는**인스턴스를 생성할 때 생성자에 필요한 자원을 넘겨주면 된다.**

다음 코드처럼 말이다.

```java
public class SpellChecker {
    private final Lexicon dictionary;
    
    public SpellChecker(Lexicon dictionary) {
        this.dictionary = Objects.requireNonNull(dictionary);
    }
    
    public boolean isValid(String word) { ... }
    public List<String> suggestions(String typo) { ... }
}
```

의존 객체 주입 패턴은 아주 단순하여 수많은 프로그래머가 이 방식에 이름이 있다는 사실도 모른 채 사용해왔다.

예에서는 하나의 자원만 사용하지만 몇 개든 의존 관계가 어떻든 상관없이 잘 작동한다.

또한 불변을 보장하여 여러 클라이언트가 의존 객체들을 안심하고 공유할 수 있기도 하다.

의존 객체 주입은 생성자, 정적 팩토리, 빌더 모두에 똑같이 응용할 수 있다.

의존 객체 주입이 유연성과 테스트 용이성을 개선해주긴 하지만, 의존성이 수 천개나 되는 프로젝트에서는 코드를 어지럽게 만들기도 한다. 이럴 때는 대거(Dagger), 주스(Guice), 스프링(Spring) 같은 의존 객체 주입 프레임워크를 사용하면 큰 도움이 된다.

정리하자면,

클래스가 내부적으로 하나 이상의 자원에 의존하고, 그 자원이 클래스 동작에 영향을 준다면 싱글톤과 정적 유틸리티 클래스는 사용하지 않는 것이 좋다. 이 자원들을 클래스가 직접 만들게 해서도 안 된다. 대신 필요한 자원을 (혹은 그 자원을 만들어주는 팩토리를) 생성자에 (혹은 정적 팩토리나 빌더에) 넘겨주자. 의존 객체 주입이라 하는 이 기법은 클래스의 유연성, 재사용성, 테스트 용이성을 기막히게 개선해준다.