---
layout: post
title: "Effective Java - 2장 (1)"
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
객체의 생성과 파괴에 대해 다뤄보자.

객체를 만들어야 할 때와 만들지 말아야 할 때를 구분하는 법,

올바른 객체 생성 방법과 불필요한 생성을 피하는 방법,

제때 파괴됨을 보장하고 파괴 전에 수행해야 할 정리 작업을 관리하는 요령을 익혀야 한다.

**규칙 1 - 생성자 대신 정적 팩토리 메서드를 고려하라.**

인스턴스를 얻는 전통적인 수단은 public 생성자이다.

하지만 그와 별도로 정적 팩토리 메서드(static factory method)를 사용할 수도 있다.

정적 팩토리 메서드는 단순히 그 클래스의 인스턴스를 반환하는 정적 메서드이다.

대표적인 예로 valueOf 메서드가 있다.

```java
BigInteger answer = BigInteger.valueOf(42L); //BigInteger 42를 리턴한다
```

**정적 팩토리 메서드가 public 생성자 보다 좋은 점**

**첫 번째, 이름을 가질 수 있다.**

생성자에 넘기는 매개변수와 생성자 자체만으로는 반환될 객체의 특성을 제대로 설명하지 못한다.

반면에 정적 팩토리 메서드는 이름만 잘 지으면 반환될 객체의 특성을 쉽게 묘사할 수 있다.

ex) BigInteger(int, int, Random) vs BigInteger.probablePrim(int, int, Random) 중 어느 쪽이 '값이 소수인 BigInteger 를 반환한다' 라는 의미를 가질까?

이름을 가진다는 것은 하나의 시그니처로도 여러가지 형태의 인스턴스를 반환할 수 있수도 있다는 것이다.

한 클래스에 시그니처가 같은 생성자가 여러 개 필요할 것 같으면 각각의 차이를 잘 드러내는 이름을 가진 정적 팩토리 메서드를 사용하자.

**두 번째, 호출될 때마다 인스턴스를 새로 생성하지는 않아도 된다.**

해당 방법을 사용하면 인스턴스를 미리 만들어 놓거나, 새로 생성한 인스턴스를 캐싱하여 재활용하는 식으로 불필요한 객체 생성을 피할 수 있다.

다음은 BigInteger.valueOf 의 예이다.

```java
public static BigInteger valueOf(long val) {
    // 미리 만들어둔 객체를 리턴한다
    if (val == 0)
        return ZERO;
    if (val > 0 && val <= MAX_CONSTANT)
        return posConst[(int) val];
    else if (val < 0 && val >= -MAX_CONSTANT)
        return negConst[(int) -val];

    // 새로운 객체를 만들어 리턴한다
    return new BigInteger(val);
}
```

매개변수로 전달받은 val이 어떤 값을 가지고 있는지에 따라서 미리 만들어둔 값을 반환할 수 있다.

이렇든 예상가능한 매개변수를 전달 받을 때에 미리 반환할 인스턴스나 값을 만들어 놓는다면 new 연산자를 통한 불필요한 객체 생성을 피할 수 있다.

따라서 같은 객체가 자주 요청되는 상황이라면 성능을 상당히 끌어올려준다.

**세 번째, 반환 타입의 하위 타입 객체를 반환할 수 있는 능력이 있다.**

즉 반환할 객체의 클래스를 자유롭게 선택할 수 있다.

API를 만들 때 이 방법을 응용하면 구현 클래스를 공개하지 않고도 그 객체를 반환할 수 있어 API를 작게 유지할 수 있다. (?)

**네 번째, 입력 매개변수에 따라 매번 다른 클래스의 객체를 반환할 수 있다.**

반환 타입의 하위 타입이기만 하면 어떤 클래스의 객체를 반환해도 된다.

클라이언트는 팩토리가 건네주는 객체가 어느 클래스의 인스턴스인지 알 수도 없고 알 필요도 없다. 그저 하위 클래스이기만 하면 되는 것이다.

```java
public static Animal getFromSound(String sound) {
	if (sound == "멍멍")
		return new Dog();
	else if (sound == "야옹" )
		return new Cat();
}
class Dog extend Animal;
class Cat extend Animal;
        
```

매개변수의 규칙에 따라서 반환하고자 하는 객체를 다르게 할 수 있다.

그저 반환하고자 하는 객체들이 같은 부모를 같거나 같은 인터페이스를 구현하기만 하면 된다.

따라서 클라이언트는 건네주는 객체를 사용하기만 하면 된다.

버전에 따라 반환되는 객체가 변경되는 일이 있더라도 클라이언트에게는 영향을 주지 않을 수 있다.

생성자를 이용해 객체를 반환받았다면, 반환 되는 객체가 다르다는 것은 즉 구현 클래스 자체가 바뀌었다는 것이기 때문에 클라이언트는 코드를 수정해야만 할 것이다.

**다섯 번째, 정적 팩토리 메서드를 작성하는 시점에는 반환할 객체의 클래스가 존재하지 않아도 된다.**

인터페이스나 클래스가 만들어 지는 시점에서 하위 타입의 클래스가 존재 하지 않아도 나중에 만들 클래스가 기존의 인터페이스나 클래스를 상속 받는 상황이라면 언제든지 의존성을 주입 받아서 사용이 가능하다.

반환값이 인터페이스여도 되며,정적 팩터리 메서드의 변경 없이 구현체를 바꿔 끼울 수 있다. ( ? )

**정적 팩토리 메서드의 단점**

**첫 번째, 정적 팩토리 메서드만 제공하면 하위 클래스를 만들 수 없다.**

상속을 하려면 public 이나 protected 생성자가 필요하기 때문에 추가적으로 생성자를 필요로 한다.

**두 번째, 프로그래머가 메소드를 찾기 어렵다.**

생성자처럼 API 설명에 명확히 드러나지 않으니 사용자는 정적 팩토리 메서드 방식 클래스를 인스턴스할 방법을 직접 알아내야 한다.

따라서 API 문서를 잘 써놓고 메서드 이름도 알려진 규칙에 따라 짓는 식으로 문제를 완화해주어야 한다.

다음은 정적 팩토리 메서드에 흔히 사용되는 명명 방식들이다.

-   form : 매개변수를 하나 받아서 해당 타입의 인스턴스를 반환하는 형변환 메서드

```java
Date d = Date.from(instant);
```

-   of : 여러 매개변수를 받아 적합한 타입의 인스턴스를 반환하는 집계 메서드

```java
Set<Rank> faceCards = EnumSet.of(JACK, QUEEN, KING);
```

-   instance 혹은 getInstance : 매개변수를 받는다면 매개변수로 명시한 인스턴스를 반환하지만, 같은 인스턴스임을 보장하지는 않는다.

```java
StackWalker luke = StackWalker.getInstance(options);
```

-   create 혹은 newInstance : instance, getInstance 와 같지만, 매번 새로운 인스턴스를 생성함을 보장한다.

```java
Object newArray = Array.newInstance(classObject, arrayLen);
```

-   getType : getInstance와 같으나, 생성할 클래스가 아닌 다른 클래스에 팩토리 메서드를 정의할 때 쓴다. 여기서 "Type"은 팩토리 메서드가 반환할 객체의 타입이다.

```java
FileStore fs = Files.getFileStore(path);
```

-   newType : newInstance와 같으나 위와 마찬가지로 다른 클래스에 정의할 때 쓴다.

```java
BufferedReader br = Files.newBufferedReader(path);
```

-   type : getType과 newType의 간결한 버전

```java
List<Complaint> litany = Collections.list(legacyLitany);
```

정리하자면,

정적 팩토리 메서드와 public 생성자는 각자의 쓰임새가 있으니 상대적인 장단점을 이해하고 사용하는 것이 좋다. 그렇다고 하더라도 정적 팩토리를 사용하는 게 유리한 경우가 더 많으므로 무작정 public 생성자를 제공하던 습관이 있다면 고치자.