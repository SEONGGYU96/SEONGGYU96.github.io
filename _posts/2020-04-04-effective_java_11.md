---
layout: post
title: "Effective Java - 3장 (10)"
subtitle: 'equals는 일반 규약을 지켜 재정의하라'
author: "GuGyu"
header-style: text
tags:
  - Java
  - Book
  - EffectiveJava
  - DesignPattern
  - Study
---
equals 메서드는 재정의하기 쉬워보이지만 사실 그렇지 않다.

따라서 다음 상황 중 하나라도 해당한다면 재정의하지 않고 기본적으로 정의된 상태로 사용하는 것이 가장 좋다.

-   각 인스턴스가 본질적으로 고유하다.
    -   값을 표현하는 게 아니라 동작하는 개체를  표현하는 클래스가 여기에 해당한다. ex) Thread
-   인스턴스의 '논리적 동치성(logical equality)'을 검사할 필요가 없다.
-   상위 클래스에서 재정의한 equals가 하위 클래스에도 딱 들어맞는다.
-   클래스가 private이거나 package-private이고 equals 메서드를 호출할 일이 없다.
    -   그리고 혹시나 equals가 실수로라도 호출되는 걸 막고싶다면 다음 처럼 구현하자  
          
        

```java
@Override public boolean equals(Object o) {
    throw new AssertionError();
}
```

그렇다면 equals를 재정의해야 할 때는 언제일까? 객체 식별성(두 객체가 물리적으로 같은가)이 아니라 논리적 동치성을 확인해야 하는데, 상위 클래스의 equals가 논리적 동치성을 비교하도록 재정의되지 않았을 때다.

주로 값 클래스들이 여기 해당한다. 값 클래스란 Inteager와 String처럼 값을 표현하는 클래스를 말한다.

이런 값 객체를 equals로 비교하는 프로그래머는 객체가 같은지가 아니라 값이 같은지를 알고싶어 할 것이다.

equals가 논리적 동치성을 확인하도록 재정의해두면 그 인스턴스들을 비교할 수도 있고 Map의 키와 Set의 원소로 사용할 수 있다.

값 클래스라 해도, 값이 같은 인스턴스가 둘 이상 만들어지지 않음을 보장하는 인스턴스 통제 클래스라면 equals를 재정의하지 않아도 된다. Enum도 마찬가지다.

equals 메서드를 재정의할 때 따라야하는 일반 규약을 보자

-   반사성(reflexivity) : null이 아닌 모든 참조 값 x에 대해, x.equals(x)는 true다.
-   대칭성(symmetry) : null이 아닌 모든 참조 값 x, y에 대해, x.equals(y)가 true면 y.equals(x)도 true다.
-   추이성(transitivity) : null이 아닌 모든 참조 값 x, y, z에 대해, x.equals(y)가 true이고 y.equals(z)도 true면 x.equals(z)도 true다.
-   일관성(consistency) : null이 아닌 모든 참조 값 x, y에 대해, x.equals(y)를 반복해서 호출하면 항상 true를 반환하거나 항상 false를 반환한다.
-   null-아님 : null이 아닌 모든 참조 값 x에 대해, x.equals(null)은 false다.

이 규약을 어기면 프로그램이 이상하게 동작하거나 종료될 것이고, 원인이 되는 코드를 찾기도 굉장히 어려울 것이다.

그렇다면 Object 명세(위에서 말한 규약)에서 말하는 동치관계란 무엇일까?

쉽게 말해, 집합을 서로 같은 원소들로 이뤄진 부분집합으로 나누는 연산이다. 이 부분집합을 동치류(equivalence calss; 동치 클래스)라 한다. 동치관계를 만족시키기 위한 다섯 요건을 하나씩 살펴보자.

**반사성**은 단순히 객체는 자기 자신과 같아야 한다는 뜻이다.

**대칭성**은 두 객체는 서로에 대한 동치 여부에 똑같이 답해야 한다는 뜻이다.

대소문자를 구별하지 않는 문자열을 구현한 다음 클래스를 예로 살펴보자. 이 클래스에서 toString 메서드는 원본 문자열의 대소문자를 그대로 돌려주지만 equals에서는 대소문자를 무시한다.

```java
public final class CaseInsensitiveString {
    private final String s;

    public CaseInsensitiveString(String s) {
        this.s = Objects.requireNonNull(s);
    }

    // 대칭성 위배!
    @Override public boolean equals(Object o) {
        if (o instanceof CaseInsensitiveString)
            return s.equalsIgnoreCase(
                    ((CaseInsensitiveString) o).s);
        if (o instanceof String)  // 한 방향으로만 작동한다!
            return s.equalsIgnoreCase((String) o);
        return false;
    }
    ...
}
```

CaseInsensitiveString의 equals는 순진하게 일반 문자열과도 비교를 시도한다. 다음처럼 CaseInsensitiveString과 일반 String 객체가 하나씩 있다고 해보자.

```java
CaseInsensitiveString cis = new CaseInsensitiveString("Polish");
String s = "polish";
```

예상할 수 있듯 cis.equals(s)는 true를 반환한다.

문제는 CaseInsensitiveString의 equals는 일반 String을 알고 있지만 String의 equals는 CaseInsensitiveStringd의 존재를 모른다는 데 있다.

이 문제를 해결하려면 다음 코드 처럼 equals를 String과도 연동하는 것을  포기해야한다.

```java
@Override public boolean equals(Object o) {
    return o instanceof CaseInsensitiveString &&
        ((CaseInsensitiveString) o).s.equalsIgnoreCase(s);
}
```

추이성은 간단하지만 자칫하면 어기기 쉽다. 상위 클래스에는 없는 새로운 필드를 하위 클래스에 추가하는 상황을 생각해보자. 

equals 비교에 영향을 주는 정보를 추가한 것이다.

```java
public class Point {
    private final int x;
    private final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override public boolean equals(Object o) {
        if (!(o instanceof Point))
            return false;
        Point p = (Point)o;
        return p.x == x && p.y == y;
    }
    ...
}
```

위 클래스를 확장해서 점에 색상을 더해본다

```java
public class ColorPoint extends Point {
    private final Color color;

    public ColorPoint(int x, int y, Color color) {
        super(x, y);
        this.color = color;
    }
    ...
}
```

이런 경우, equals  메서드는 어떻게 해야 할까? 그대로 둔다면 색상 정보는 무시한 채 비교를 수행한다.

그렇다면 다음 처럼, 비교 대상이 또 다른 ColorPoint 이고 위치와 색상이 같을 때만 true를 반환하는 equals를 생각해보자.

```java
//대칭성에 위배
@Override public boolean equals(Object o) {
    if (!(o instanceof ColorPoint))
        return false;
    return super.equals(o) && ((ColorPoint) o).color == color;
}
```

이 메서드는 일반 Point를 ColorPoint에 비교한 결과와 그 둘을 바꿔 비교한 결과가 다를 수 있다.

Point의 equals는 색상을 무시, ColorPoint의 equals는 입력 매개변수의 클래스 종류가 다르다며 매번 false만 반환할 것이다.

그럼 equals가 Point와 비교할 때는 색상을 무시하도록 하면 해결될까?

```java
//추이성 위배
@Override public boolean equals(Object o) {
    if (!(o instanceof Point))
        return false;

    // o가 일반 Point면 색상을 무시하고 비교한다.
    if (!(o instanceof ColorPoint))
        return o.equals(this);

    // o가 ColorPoint면 색상까지 비교한다.
    return super.equals(o) && ((ColorPoint) o).color == color;
}
```

이 방식은 대칭성은 지켜주지만, 추이성을 깨버린다...

```java
ColorPoint p1 = new ColorPoint(1, 2, Color.RED);
Point p2 = new Point(1, 2);
ColorPoint p3 = new ColorPoint(1, 2, Color.BLUE);
```

위와 같이 인스턴스를 생성하고 equals 메서드를 사용해보면 p1.equals(p2)와 p2.equals(p3)는 true를 반환하는데, p1.equals(p3)가 false를 반환한다. 추이성에 명백히 위배되는 것이다.

p1과 p2, p2와 p3비교에서는 색상을 무시했지만, p1과 p3 비교에서는 색상까지 고려했기 때문이다.

또한, 이 방식은 무한 재귀에 빠질 위험도 있다.

그럼 해결방법은 무엇일까? 사실 이현상은 모든 객체 지향 언어의 동치 관계에서 나타나는 근본적인 문제이다.

구체 클랫를 확장해 새로운 값을 추가하면서 equals 규약을 만족시킬 방법은 존재하지 않는다. 객체 지향적 추상화의 이점을 포기하지 않는 한은 말이다.

그렇다면 euqals 안의 instanceof 검사를 getClass 검사로 바꾸어 규약도 지키고 값도 추가하면서 구체 클래스를 상속할 수 있을까?

```java
//잘못된 코드 - 리스코프 치환 원칙 위배! (59쪽)
@Override
public boolean equals(Object o) {
    if (o == null || o.getClass() != getClass())
        return false;
    Point p = (Point) o;
    return p.x == x && p.y == y;
}

```

이번 equals는 같은 구현 클래스의 객체와 비교할 때만 true를 반환한다.

괜찮아 보이지만 실제로 활용할 수는 없다. Point의 하위 클래스는 정의상 여전히 Point이므로 어디서든 Point로써 활용될 수 있어야 한다. 그런데 이 방식에서는 그렇지 못하다. 다음 코드를 보자.

```java
private static final Set<Point> unitCircle = set.of(
    new Point( 1, 0), new Point(0,  1),
    new Point(-1, 0), new Point(0, -1);
    
public static boolean onUniteCircle(point p) {
    return unitCircle.contains(p);
}
```

이 기능을 구현하는 가장 빠른 방법은 아니겠지만, 어쨋든 동작은 한다. 이제 값을 추가하지 않는 방식으로 Point를 확장하겠다. 만들어진 인스턴스의 개수를 생성자에서 세보도록 하자.

```java
public class CounterPoint extends Point {
    private static final AtomicInteger counter = new AtomicInteger();

    public CounterPoint(int x, int y) {
        super(x, y);
        counter.incrementAndGet();
    }
    public static int numberCreated() { return counter.get(); }
}
```

리스코프 치완 원칙에 따르면 어떤 타입에 있어 중요한 속성이라면 그 하위 타입에서도 마찬가지로 중요하다. 따라서 그 타입의 모든 메서드가 하위 타입에서도 똑같이 잘 작동해야한다.

그런데 CounterPoint의 인스턴스를 onUnitCircle 메서드에 넘기면 어떻게 될까?

Point 클래스의 equals를 getClass를 사용해 작성했다면 onUnitCircle은 false를 반환할 것이다. CounterPoint 인스턴스의 x, y값과는 무관하게 말이다. 

set을 포함한 대부분의 컬렉션은 원소를 포함하고 있는지 확인하는 과정에서 equals 메서드를 이용하는데, CounterPoint의 인스턴스는 어떤 Point와도 같을 수 없기 때문이다.

반면, instanceof 기반으로 구현했다면 제대로 작동할 것이다.

구체 클래스의 하위 클래스에서 값을 추가할 방법은 없지만 괜찮은 우회 방법이 있다.

Point를 상속하는 대신 Point를 ColorPoint의 private 필드로 두고, ColorPoint와 같은 위치의 일반 Point를 반환하는 뷰(view) 메서드를 public으로 추가하는 식이다.

```java
public class ColorPoint {
    private final Point point;
    private final Color color;

    public ColorPoint(int x, int y, Color color) {
        point = new Point(x, y);
        this.color = Objects.requireNonNull(color);
    }

    /**
     * 이 ColorPoint의 Point 뷰를 반환한다.
     */
    public Point asPoint() {
        return point;
    }

    @Override public boolean equals(Object o) {
        if (!(o instanceof ColorPoint))
            return false;
        ColorPoint cp = (ColorPoint) o;
        return cp.point.equals(point) && cp.color.equals(color);
    }
    ...
}
```

일관성은 두 객체가 같다면 영원히 같아야 한다는 뜻이다. 가변 객체는 비교 시점에 따라 서로 다를 수도 있지만 불변 객체로 만들기로 했다면 equals가 한 번 같다고 한 객체와는 영원히 같다고 답하고, 다르다고 한 객체와는 영원히 다르다고 답하도록 만들어야한다.

클래스가 불변이든 가변이든 equals의 판단에 신뢰할 수 없는 자원이 끼어들게 해서는 안된다. 이 조건을 어기면 일관성 조건을 맞ㄴ족시키기가 어려워진다.

마지막으로 모든 객체는 null과 같지 않아야 한다. 수많은 클래스가 다음 코드 처럼 입력이 null인지를 확인해 자신을 보호한다.

```java
@Overide
public boolean equals(object o) {
    if (o == null)
        return false;
    ...
}
```

하지만 equals의 동치성을 검사하는 과정에서 이와 같은 null 검사가 묵시적으로 수행된다.

```java
@Override
public boolean equals(Object o) {
    if (!(o instanceof MyType))
        return false;
    Mytype mt = (Mytype) o;
    ...
}
```

위 방법이 훨씬 낫다.

지금까지의 내용을 종합하여 euqals 메서드 구현 방법을 단계별로 정리해보자

1\. **\== 연산자를 사용해 입력이 자기 자신의 참조인지 확인한다.**

자기 자신이면 true를 반환하는데 이는 단순 성능 최적화용이다. 비교 연산이 복잡해지면 값어치를 할 것이다.

2**. instanceof 연산자로 입력이 올바른 타입인지 확인한다.**

3\. **입력을 올바른 타입으로 형변환한다.**

4\. **입력 객체와 자기 자신의 대응되는 '핵심' 필드들이 모두 일치하는지 하나씩 검사한다.**

하나라도 다르면 false이다.

다음은 이상의 비법에 따라 작성해본 PhoneNumber 클래스용 equals 메서드다.

```java
//전형적인 equals 메서드의 예
public final class PhoneNumber {
    private final short areaCode, prefix, lineNum;

    public PhoneNumber(int areaCode, int prefix, int lineNum) {
        this.areaCode = rangeCheck(areaCode, 999, "지역코드");
        this.prefix   = rangeCheck(prefix,   999, "프리픽스");
        this.lineNum  = rangeCheck(lineNum, 9999, "가입자 번호");
    }

    private static short rangeCheck(int val, int max, String arg) {
        if (val < 0 || val > max)
            throw new IllegalArgumentException(arg + ": " + val);
        return (short) val;
    }

    @Override public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof PhoneNumber))
            return false;
        PhoneNumber pn = (PhoneNumber)o;
        return pn.lineNum == lineNum && pn.prefix == prefix
                && pn.areaCode == areaCode;
    }
    ...
}
```

마지막 주의사항으로는

1\. euqals를 재정의할 땐 hashCode도 반드시 재정의하자

2\. 너무 복잡하게 해결하려 들지말자. 필드들의 동치성만 검사해도 어렵지 않게 규약을 지킬 수 있다.

3\. Object 이외의 타입을 매개변수로 받지말자

참고로 이 작업들을 대신 해주는 오픈소스 프레임워크가 있다.

바로 구글이 만든 AutoValue 이다.

클래스에 어노테이션 하나만 추가하면 AutoValue가 이 메서드들을 알아서 작성해준다.

정리하자면,

꼭 필요한 경우가 아니라면 equals를 재정의하지 말자. 많은 경우에 Object의 equals가 우리가 원하는 비교를 정확하게 수행해준다. 재정의해야 할 때는 그 클래스의 핵심 필드를 모두 빠짐없이, 다섯 가지 규약을 확실히 지켜가며 비교해야 한다.