---
layout: post
title: "Effective Java - 1장 들어가기"
subtitle: 'effective java'
author: "GuGyu"
header-style: text
tags:
  - Java
  - Book
  - EffectiveJava
  - DesignPattern
  - Study
---
나는 자바로 안드로이드 프로그래밍을 자주 하는 편이다.

이런 저런 예제를 참고하고, 수정해보면서 자바를 익혔다.

그런데 예제마다 코딩 방식이 각양각색이었다.

마음에 드는 방식을 아무거나 골라 흉내내면서 코딩을 하였는데 영 기분이 찝찝한 상태였다.

그러다가 Effective Java 라는 책을 접하게 되었다.

자바라는 언어를 사용함에 있어서 효과적이고 관례적인 방법(용법)을 서술한 책이었다.

여기서 다루는 방법, 규칙들은 100% 옳을 수는 없겠지만, 거의 모든 경우에 적용되는 최고의 모범 사례라고 한다.

이 규칙들을 생각 없이 맹종하지는 말아야겠지만 어겨야 할 때는 합당한 이유가 있어야 할 것이다.

원래 규칙은 알고 깨는 것이 정석이다.

이 책은 총 90개의 규칙을 다루고 있다.

각 규칙들은 업계 최고의 베테랑 프로그래머들이 유익하다고 인정하는 관례라고 한다.

대부분의 규칙은 아주 핵심적인 기본 원칙 몇 개에서 파생된다.

**명료성**(clarity)과 **단순성**(simplicity)이다.

컴포넌트를 예로 들어보자.

1\. 컴포넌트는 사용자를 놀라게 하는 동작을 절대 해서는 안 된다. 정해져있거나 예측 가능한 동작만을 해야한다는 것이다.

( 컴포넌트 - 개별 메서드부터 여러 패키지로 이뤄진 복잡한 프레임워크까지 재사용 가능한 모든 소프트웨어 요소 )

2.  컴포넌트는 가능한 작게, 그렇지만 너무 작아서는 안 된다. ( 적당히가 제일 어려운 법..)

3\. 코드는 복사되는 게 아니라 재사용 되어야 한다.

4\. 컴포넌트 사이의 의존성은 최소로 유지해야 한다.

5\. 오류는 만들어지자마자 가능한 빨리 (되도록 컴파일 타임에) 잡아야 한다.

이렇듯 성능에 집중하기보다 프로그램을 명확하고 정확하고 유용하고 견고하고 유연하고... 관리하기 쉽게 짜는 데 집중하자.

\* 참고

**자바가 지원하는 타입(자료형)**

인터페이스(interface), 클래스(class), 배열(array), 기본 타입(primitive)

기본 타입을 제외하고는 참조 타입(reference type) 이라고 한다.

즉, 클래스의 인스턴스와 배열은 객체(object)인 반면, 기본 타입 값은 그렇지 않다.

**클래스의 멤버**

필드(field), 메서드(method), 멤버 클래스, 멤버 인터페이스

**메서드 시그니처**

메서드 이름 + 입력 매개변수의 타입들 (반환값의 타입은 시그니처에 포함되지 않는다)

**API (Application Programming Interface)**

프로그래머가 클래스, 인터페이스, 패키지를 통해 접근할 수 있는 모든 클래스, 인터페이스, 생성자, 멤버, 직렬화된 형태(serialized form) 등

공개 API는 그 API를 정의한 패키지의 밖에서 접근할 수 있는 요소로 이루어진다.

즉, 모든 클라이언트가 접근할 수 있고, API 작성자가 지원하기로 약속한 API 요소들이다.

**모듈 시스템**

자바 라이브러리에 이 모듈 개념을 적용하면 공개 API는 '해당 라이브러리의 모듈 선언(module declaration)에서 공개하겠다고 한' 패키지들의 공개 API 만으로 이루어진다.

즉 공개 패키지를 선택할 수 있다.
