---
layout: post
title: "Effective Java - 2장 (8)"
subtitle: 'finalizer와 cleaner 사용을 피하라'
author: "GuGyu"
header-style: text
tags:
  - Java
  - Book
  - EffectiveJava
  - DesignPattern
  - Study
---
자바는 두 가지 객체 소멸자를 제공한다.

finalizer, cleaner

**finalizer**는 **예측할 수 없고, 상황에 따라 위험할 수 있어 일반적으로 불필요**하다.

오동작, 낮은 성능, 이식성 문제 등 기본적으로 쓰지 말아야 한다. 그래서 자바 9에서는 finalizer를 deprecated API로 지정하고 cleaner를 대안으로 소개했다.

**cleaner**는 finalizer보다는 덜 위험하지만, **여전히 예측할 수 없고, 느리고, 일반적으로 불필요**하다.

자바의 finalizer와 cleaner는 C++의 파괴자(destructor)와는 다른 개념이다. 파괴자는 특정 객체와 관련된 자원을 회수하는 보편적인 방법이지만 자바에서는 접근할 수 없게 된 객체를 회수하는 역할을 가비지 컬렉터가 담당하고, 프로그래머에게는 아무런 작업도 요구하지 않는다.

C++의 파괴자는 비메모리 자원을 회수한다는 용도로도 쓰이지만 자바에서는 try-with-resources와 같이 try-finally가 해당 기능을 수행한다.

finalizer와 cleaner는 즉시 수행된다는 보장이 없기 때문에 제때 실행되어야 하는 작업은 절대 할 수 없다.

시스템이 동시에 열 수 있는 파일 개수에 한계가 있는데 시스템이 finalizer나 cleaner 실행을 게을리 해서 파일을 계속 열어 둔다면 새로운 파일을 열지 못해 프로그램이 실패할 수 있다.

현업에서도 finalizer를 달아둔 클래스에서 그 인스턴스의 자원 회수가 제멋대로 지연되기도 한다. finalizer 스레드가 다른 애플리케이션 스레드보다 우선 순위가 낮아서 수천개의 그래픽스 객체가 대기열에서 회수되기만 기다릴 수도 있는 것이다. 

cleaner는 자신을 수행할 스레드를 제어할 수는 있지만 여전히 백그라운드에서 가비지 컬렉터의 통제하에 있으니 즉각 수행되리나는 보장은 없다.

따라서 프로그램 생애주기와 상관없는, 상태를 영구적으로 수정하는 작업에서는 절대 finalizer나 cleaner에 의존해서는 안된다. 예를 들어 데이터베이스 같은 공유 자원의 영구 락 해제를 finalizersk cleaner에 맡겨 놓으면 분산 시스템 전체가 서서히 멈출 것이다.

정리하자면,

cleaner(자바 8까지는 finalizer)는 안전망 역할이나 중요하지 않은 네이티브 자원 회수용으로만 사용하자. 물론 이런 경우라도 불확실성과 성능 저하에 주의해야한다.