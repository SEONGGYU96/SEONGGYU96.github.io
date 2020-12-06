---
layout: post
title: "Effective Java - 2장 (4)"
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
규칙4 - 인스턴스화를 막으려거든 private 생성자를 사용하라

이따금 정적 메서드와 정적 필드만을 담은 클래스를 만들고 싶을 때가 분명 있다.

final 클래스와 관련한 메서드들을 모아놓고 싶을 때도 있다.

이러한 클래스들은 인스턴스로 만들어 쓰려고 설계한 것이 아니다. 하지만 생성자를 명시하지 않으면 자동으로 public 기본 생성자가 만들어지기 때문에 사용자는 자동 생성된 것인지 구분도 할 수 없다.

추상클래스로 만드는 것으로도 인스턴스화를 막을 수 없다. 하위 클래스를 만들어 인스턴스화 하면 그만이다.

따라서 클래스의 **인스턴스화를 막기 위해서는 private 생성자를 추가하면 된다.**

명시적 생성자가 private 이니 클래스 바깥에서는 접근할 수 없다. 상속 또한 불가능하게 하는 효과가 있다.