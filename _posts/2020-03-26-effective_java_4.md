---
layout: post
title: "Effective Java - 2장 (3)"
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
## 규칙3 - private 생성자나 열거 타입으로 싱글톤임을 보증하라

**싱글톤(singleton)** 이란 인스턴스를 오직 하나만 생성할 수 있는 클래스를 말한다.

하지만 클래스를 싱글톤으로 만들면 이를 사용하는 클라이언트를 테스트하기가 어려워질 수 있다.

타입을 인터페이스로 정의한 다음 그 인터페이스를 구현해서 만든 싱글톤이 아니라면, 싱글톤 인스턴스를 가짜(mock) 구현으로 대체할 수 없기 때문이다. (?)

싱글톤을 만드는 방식은 보통 줄 중 하나이나 모두 생상자를 private으로 감춰두고, 유일한 인스턴스에 접근할 수 있는 수단으로  
public static 멤버를 하나 마련해둔다.

우선 **public static 멤버가 final 필드인 방식**을 살펴보자.

```java
public class Elvis {
    public static final Elvis INSTANCE = new Elvis();
    private Elvis() { ... }
    
    public void leaveTheBuilding() { ... }
}
```

private 생성자는 public static final 필드인 Elvis.INSTANCE 를 초기화할 때 딱 한 번만 호출된다.

public 이나 protected 생성자가 없으므로 클래스가 초기화될 때 만들어진 인스턴스가 전체 시스템에서 하나뿐임이 보장된다.

이렇게 싱글톤을 구현하면 해당 클래스가 싱글톤임이 API에 명백히 드러나고 public static 필드가 final이니 절대로 다른 객체를 참조할 수 없다.

두 번째 방법으로는 **정적 팩토리 메서드를 public static 멤버로 제공**하는 것이다.

```java
public class Elvis {
    private static final Elvis INSTANCE = new Elvis();
    private Elvis() { }
    public static Elvis getInstance() { return INSTANCE; }

    public void leaveTheBuilding() { ... }
```

Elvis.getInstancesms 항상 같은 객체의 참조를 반환한다.

이와 같은 방법은 API를 바꾸지 않고도 싱글톤이 아니게 변경할 수 있다는 장점이 있다. 유일한 인스턴스를 반환하던 팩토리 메서드가 호출하는 스레드별로 다른 인스턴스를 넘겨주게 할 수 있다. 

또한 원한다면 정적 팩토리를 제네릭 싱글톤 팩토리로 만들 수 있다. (후에 규칙 30에서 설명)

혹은 정적 팩토리의 메서드 참조를 공급자(supplier)로 사용할 수 있다. 예를 들어 Elvis::getInstance 를 Supplier<Elvis>로 사용하는 식이다. 

싱글톤을 만드는 마지막 방법은 원소가 하나인 열거 타입을 선언하는 것이다.

```java
public enum Elvis {
    INSTANCE;
    
    public void leaveTheBuilding() { ... }
}
```

public 필드 방식과 비슷하지만 더 간결하고 추가 노력 없이 직렬화가 가능하다.

대부분의 상황에서는 해당 방법이 가장 좋은 방법이지만 만들려는 싱글톤이 Enum 외의 클래스를 상속해야 한다면 이 방법을 사용할 수 없다.