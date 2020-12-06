---
layout: post
title: "Effective Java - 2장 (7)"
subtitle: '다 쓴 객체 참조를 해제하라'
author: "GuGyu"
header-style: text
tags:
  - Java
  - Book
  - EffectiveJava
  - DesignPattern
  - Study
---
자바처럼 가비지 컬렉터를 갖춘 언어는 다 쓴 객체를 알아서 회수하기 때문에 메모리 관리에 있어서 훨씬 편리하다.

하지만 메모리 관리에 전혀 신경을 쓰지 않아도 되는 것은 아니다.

스택을 간단히 구현한 다음 코드를 보자.

```java
public class Stack {
    private Object[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    public Stack() {
        elements = new Object[DEFAULT_INITIAL_CAPACITY];
    }

    public void push(Object e) {
        ensureCapacity();
        elements[size++] = e;
    }

    public Object pop() {
        if (size == 0)
            throw new EmptyStackException();
        return elements[--size];
    }

    /**
     * 원소를 위한 공간을 적어도 하나 이상 확보한다.
     * 배열 크기를 늘려야 할 때마다 대략 두 배씩 늘린다.
     */
    private void ensureCapacity() {
        if (elements.length == size)
            elements = Arrays.copyOf(elements, 2 * size + 1);
}
```

특별한 문제는 없어보이지만 메모리 누수가 발생하는 부분이 있다.

이 코드에서는 스택이 커졌다가 줄어들었을 때, 스택에서 꺼내진 객체들을 가비지 컬렉터가 회수하지 않는다. 이 스택이 그 객체들의 다 쓴 참조를 여전히 가지고 있기 때문이다.

앞의 코드에서는 elements 배열의 '활성 영역' 밖의 참조들이 모두 여기에 해당한다. 활성 영역은 인덱스가 size 보다 작은 원소들로 구성된다.

가비지 컬렉션 언어에서는 의도치 않게 객체를 살려두는 메모리 누수를 찾기가 아주 까다롭다. 객체 참조 하나를 살려두면 그 객체가 참조하는 모든 객체(그리고 또 그 객체들이 참조하는 모든 객체...)를 회수해가지 못한다.

해법은 간단하다. 해당 참조를 다 썼을 때 null 처리하면 된다.

다음은 pop 메서드를 제대로 구현한 모습이다.

```java
public Object pop() {
    if (size == 0)
	throw new EmptyStackException();
    Object result = elements[--size];
    elements[size] = null; // 다 쓴 참조 해제
    return result;
}

```

하지만 모든 객체를 다 쓰자마자 일일이 null 처리를 할 필요는 없다. 객체 참조를 null 처리 하는 일은 예외적인 경우여야 한다.

다 쓴 참조를 해제하는 가장 좋은 방법은 그 참조를 담은 변수를 유효 범위 밖으로 밀어내는 것이다.

프로그래머가 변수의 범위를 최소가 되게 정의했다면 이 일은 자연스럽게 이뤄진다.

스택과 같이 자기 메모리를 직접 관리하는 상황에야 말로 null 처리를 해줘야 할 때다. 앞에서 스택은 elements 배열(객체자체가 아니라 객체 참조를 담는)로 저장소 풀을 만들어 원소를 관리한다. 배열의 활성 영역에 속산 원소들이 사용되고 비활성 영역은 쓰이지 않는데 가비지 컬렉터는 이 사실을 알 길이 없다. 비활성 영역의 객체가 더이상 쓸모가 없다는 건 프로그래머만 아는 사실이다.

그러므로 프로그래머는 비활성 영역이 되는 순간 null 해서 해당 객체를 더는 쓰지 않을 것임을 가비지 컬렉터에 알려야 한다.

일반적으로, **자기 메모리를 직접 관리하는 클래스라면 프로그래머는 항시 메모리 누수에 주의해야 한다.**

메모리 누수에 영향을 미치는 두 번째 요소는 캐시다. 객체 참조를 캐시에 넣어두고 그 객체를 다 쓴 뒤에도 한참을 까먹고 놔두는 일이 많다. 이럴 때는 WeakHashMap을 사용해 캐시를 만드는 것을 권장한다.

메모리 누수의 세 번째 요소는 리스너 혹은 콜백이다. 클라이언트가 콜백을 등록만 하고 명확히 해지하지 않는다면, 콜백은 계속 쌓여갈 것이다. 이럴 때 콜백을 약한 참조(weak reference)로 저장하면 가비지 컬렉터가 즉시 수거해간다.

정리하자면,

메모리 누수는 겉으로 잘 드러나지 않아 시스템에 수년간 잠복하는 사례도 있다. 이런 누수는 철저한 코드 리뷰나 힙 프로파일러 같은 디버깅 도구를 동원해야만 발견되기도 한다. 그래서 이런 종류의 문제는 예방법을 익혀두는 것이 매우 중요하다.