---
layout: post
title: "Effective Java - 3장 (11)"
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
equals를 재정의한 클래스 모두에서 hashCode도 재정의해야 한다.

그렇지 않으면 hashCode 일반 규약을 어기게 되어 해당 클래스의 인스턴스를 HashMap이나 HashSet 같은 컬렉션의 원소로 사용할 때 문제를 일으킬 것이다.

다음은 Object 명세에서 발췌한 규약이다.

1\. equals 비교에서 사용되는 정보가 변경되지 않았다면, 애플리케이션이 실행되는 동안 그 객체의 hashCode 메서드는 몇 번을 호출해도 일관되게 항상 같은 값을 반환해야 한다. 단, 애플리케이션을 다시 실행한다면 이 값이 달라져도 상관없다.

2\. equals(Object)가 두 객체를 같다고 판단했다면, 두 객체의 hashCode는 똑같은 값을 반환하여야 한다.

3\. equals(Object)가 두 객체를 다르게 판단했더라도, 두 객체의 hashCode가 서로 다른 값을 반환할 필요는 없다. 단, 다른 객체에 대해서는 다른 값을 반환해야 해시테이블의 성능이 좋아진다.

hashCode 재정의를 잘못했을 때 크게 문제가 되는 조항은 2번이다. 즉, 논리적으로 같은 객체는 같은 해시코드를 반환해야 한다.

equals는 물리적으로 다른 두 객체를 논리적으로 같다고 할 수 있지만 기본 hashCode는 이 둘이 전혀 다르다고 판단하여, 규약과 달리 서로 다른 값을 반환하게 된다.

예를 들어 PhoneNumber 클래스의 인스턴스를 HashMap의 원소로 사용한다고 하자

```java
Map<PhoneNumber, String> m = new HashMap<>();
m.put(new PhoneNumber(707, 867, 5309), "제니");
```

이 코드 다음엥 m.get(new PhoneNumber(707, 867, 5309))를 실행하면 "제니"가 나와야하지만 실제로는 null을 반환한다.

HashMap에 넣을 때 사용한 인스턴스과 꺼낼 때 사용한 인스턴스가 논리적으로 같지만 두 인스턴스가 다른 애시코드를 반환하여 엉뚱한 해시 버킷에 가서 인스턴스를 찾으려 한 것이다.

이 문제는 PhoneNumber에 적절한 hashCode 메서드만 작성해주면 해결된다.

좋은 해시 함수는 서로 다른 인스턴스에 다른 해시코드를 반환한다. 이것이 바로 세 번째 규약이 교구하는 속성이다.

이상적인 해시 함수는 주어진 서로 다른 인스턴스들을 32비트 정수 범위에 균일하게 분배해야 한다.

다음은 좋은 hashCode를 작성하는 간단한 요령이다.

1.  int 변수 result를 선언한 후 값 c로 초기화한다. 이때 c는 해당 객체의 첫 번째 핵심 필드를 2.1 방식으로 계산한 해시코드다.
2.  해당 객체의 나머지 핵심 필드 f 각각에 대해 다음 작업을 수행힌다.
    1.  해당 필드의 해시코드 c를 계산한다.
        1.  기본 타입 필드라면, _Type._hashCode(f)를 수행한다. 여기서 _Type_은 해당 기본 타입의 박싱 클래스다.
        2.  참조 타입 필드면서 이 클래스의 equals 메서드가 이 필드의 equals를 재귀적으로 호출해 비교한다면, 이 필드의 hashCode를 재귀적으로 호출한다. 계산이 더 복잡해질 것 같으면, 이 필드의 표준형(canonical representation)을 만들어 그 표준형의 hashCode를 호출한다. 필드의 값이 null이면 0을 사용한다.(다른 상수도 괜찮지만 전통적으로 0을 사용한다.)
        3.  필드가 배열이라면, 핵심 원소 각각을 별도 필드처럼 다룬다. 이상의 규칙을 재귀적으로 적용해 각 핵심 원소의 해시코드를 계산한 다음, 단계 2.b 방식으로 갱신한다. 배열에 핵심 원소가 하나도 없다면 단순히 상수(0을 추천한다)를 사용한다. 모든 원소가 핵심 원소라면 Arrays.hashCode를 사용한다.
    2.  단계 2.1에서 계산한 해시코드 c로 result를 갱신한다. 코드로는 다음과 같다.  
        result = 31 \* result + c;
3.  result를 반환한다.

파생 필드는 해시코드 계산에서 제외해도 된다. 즉, 다른 필드로부터 계산해낼 수 있는 필드는 모두 무시해도 된다.

또한 equals 비교에 사용되지 않은 필드는 '반드시' 제외해야 한다.

단계 2.2의 곱셈 31 \* result는 필드를 곱하는 순서에 따라 result 값이 달라지게 한다.

곱할 숫자를 31로 정한 이유는 31이 홀수이면서 소수이기 때문이다. 31을 이용하면 이 곱셈을 시프트 연산과 뺄셈으로 대체해 최적화할 수 있다. (31 \* i 는 (1 << 5) - i 와 같다.)

이 요령을 PhoneNumber 클래스에 적용해보자.

```java
전형적인 hashCode 메서드
@Override 
public int hashCode() {
    int result = Short.hashCode(areaCode);
    result = 31 * result + Short.hashCode(prefix);
    result = 31 * result + Short.hashCode(lineNum);
    return result;
}

```

이 메서드는 PhoneNumber 인스턴스의 핵심 필드 3개만을 사용해 간단한 계산만 수행한다.

 그 과정에 비결정적 요소는 전혀 없으므로 동치인 PhoneNumber 인스턴스들은 같은 해시코드를 가질 것이 확실하다.

이 제작 요령들은 최첨단은 아니지만 충분히 훌륭하다. 단, 해시 충돌이 더욱 적은 방법을 꼭 써야 한다면 구아바의 com.google.common.hash.Hashing을 참고할 수 있겠다.

Object 클래스가 제공하는 hash 메서드를 활용하여 앞서의 요령대로 구현한 코드와 비슷한 수준의 hashCode 함수를 단 한 줄로 작성할 수 있다. 단점은 속도가 더 느리다. 그러니 hash 메서드는 성능에 민감하지 않은 상황에서만 사용하자.

다음 방식은 PhoneNumber의 hashCode를 이 방식으로 구현한 예다.

```java
@Override
public int hashCode() {
    return Objects.hash(lineNum, prefix, areaCode);
}
```

클래스가 불변이고 해시코드를 계산하는 비용이 크다면, 매번 새로 계산하기 보다는 캐싱하는 방식을 고려해야 한다.

이 타입의 객체가 주로 해시의 키로 사용될 것 같다면 인스턴스가 만들어질 때 해시코드를 계산해둬야 한다.

해시의 키로 사용되지 않는 경우라면 hashCode가 처음 불릴 때 계산하는 지연 초기화(lazy initialization) 전략은 어떨까? 필드를 지연 초기화하려면 그 클래스를 스레드 안전하게 만들도록 신경써야 한다. hashCode 필드의 초깃값은 흔히 생성되는 객체의 해시코드와는 달라야 함에 유념하면서 다음 예시를 참고하자.

```java
해시코드를 지연 초기화하는 hashCode 메서드 - 스레드 안정성까지 고려해야 한다.
private int hashCode; // 자동으로 0으로 초기화된다.

@Override
public int hashCode() {
    int result = hashCode;
    if (result == 0) {
        result = Short.hashCode(areaCode);
        result = 31 * result + Short.hashCode(prefix);
        result = 31 * result + Short.hashCode(lineNum);
        hashCode = result;
    }
    return result;
}
```

성능을 높인답시고 해시코드를 계산할 때 핵심 필드를 생략해서는 안된다. 속도야 빨라지겠지만, 해시 품질이 나빠져 해시테이블의 성능을 심각하게 떨어뜨릴 수도 있다.

또한 hashCode가 반환하는 값의 생성규칙을 API 사용자에게 자세히 공표하지 말자. 그래야 클라이언트가 이 값에 의지하지 않게 되고, 추후에 계산 방식을 바꿀 수도 있다.

정리하자면,

equals를 재정의할 때는 hashCode도 반드시 재정의해야 한다. 그렇지 않으면 프로그램이 제대로 동작하지 않을 것이다. 재정의한 hashCode는 Object의 API문서에 기술된 일반 규약을 따라야 하며, 서로 다른 인스턴스라면 되도록 해시코드도 서로 다르게 구현해야 한다. 이렇게 구현하기가 어렵지는 않지만 조금 따분한 일이긴 하다. 하지만 AutoValue 프레임워크를 사용하면 멋진 equals와 hashCode를 자동으로 만들어준다.