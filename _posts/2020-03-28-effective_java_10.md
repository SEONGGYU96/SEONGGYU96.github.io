---
layout: post
title: "Effective Java - 2장 (9)"
subtitle: 'try-finally 보다는 try-with-resources를 사용하라'
author: "GuGyu"
header-style: text
tags:
  - Java
  - Book
  - EffectiveJava
  - DesignPattern
  - Study
---
자바 라이브러리에는 close 메서드를 호출해 직접 닫아줘야 하는 자원이 많다.

이는 클라이언트가 놓치기 쉬워서 예측할 수 없는 성능 문제로 이어지기도 한다. 전통적으로 자원이 제대로 닫힘을 보장하는 수단으로 try-finally가 쓰였다. 예의가 발생하거나 메서드에서 반환되는 경우를  포함해서 말이다.

```java
static String firstLineOfFile(String path) throws IOExeption {
    BufferedReader br = new BufferedReader(new FileReader(path));
    try {
        return br.readLine();
    } finally {
        br.close();
    }
}
```

나쁘지 않지만 자원을 하나 더 사용한다면 어떨까?

```java
static void copy(String src, String dst) throws IOException {
    InputStream in = new FileInputStream(src);
    try {
        OutputStream out = new FileOutputStream(dst);
        try {
            byte[] buf = new byte[BUFFER_SIZE];
            int n;
            while ((n = in.read(buf)) >= 0)
                out.write(buf, 0, n);
        } finally {
            out.close();
        }
    } finally {
        in.close();
    }
}

```

위는 흔하게 발생하는 실수이다. 자원이 둘 이상이면 try-finally 코드는 너무 지저분해진다.

예외는 try블록과 finally 블록 모두에서 발생할 수 있는데, 예컨데 기기에 물리적인 문제가 생긴다면 firstLineOfFile 메서드 안의 readLine 메서드가 예외를 던지고, 같은 이유로 close 메서드도 실패할 것이다. 이런 상황이라면 두 번째 예외가 첫 번째 예외를 완전히 집어삼켜 버린다. 그러면 스택 추적 내역에 첫 번째 예외에 관한 정보는 남지 않게 되어, 실제 시스템에서의 디버깅을 몹시 어렵게 한다.

이러한 문제는 try-with-resources 로 모두 해결할 수 있다. 이 구조를 사용하려면 해당 자원이 AutoCloseable 인터페이스를 구현해야 한다. 단순히 void를 반환하는 close 메서드 하나만 덩그러니 정의한 인터페이스다.

다음 위 두 코드들을 try-with-resources로 변형한 예이다

```java
static Sring firstLineOfFile(String path) throws IOException {
    try (BufferedReader br = new BufferedReader(new FileReader(path))) {
        return br.LeadLine();
    }
}
    
```
```java
private static final int BUFFER_SIZE = 8 * 1024;

static void copy(String src, String dst) throws IOException {
    try (InputStream   in = new FileInputStream(src);
         OutputStream out = new FileOutputStream(dst)) {
        byte[] buf = new byte[BUFFER_SIZE];
        int n;
        while ((n = in.read(buf)) >= 0)
            out.write(buf, 0, n);
    }
}
```

try-with-resources 버전이 짧고 읽기 수월할 뿐 아니라 문제를 진단하기도 훨씬 좋다.

위 firstLineOfFile 메서드를 생각해보자. readLine과 (코드에서는 나타나지 않는) close 호출 양쪽에서 예외가 발생하면 close 에서 발생한 예외는 숨겨지고 readLine에서 발생한 예외가 기록된다. 이처럼 실전에서는 프로그래머에게 보여줄 예외 하나만 보존되고 여러 개의 다른 예외가 숨겨질 수도 있다. 

try-with-resources 에서도 catch 절을 쓸 수 있다. 덕분에 try 문을 더 중첩하지 않고도 다수의 예외를 처리할 수 있다.

다음 예처럼 말이다.

```java
static String firstLineOfFile(String path, String defaultVal) {
    try (BufferedReader br = new BufferedReader(new FileReader(path))) {
        return br.readLine()
    } catch (IOExceptrion e) {
        return defaultVal;
    }
}
```

정리하자면, 

꼭 회수해야 하는 자원을 다룰 때는 try-finally 말고, try-with-resources를 사용하자. 예외는 없다. 코드는 더 짧아지고 분명해지고, 만들어지는 예외 정보도 훨씬 유용하다. try-finally로 작성하면 실용적이지 못할 만큼 코드가 지저분해지는경우라도 try-with-resources로는 정확하고 쉽게 자원을 회소할 수 있다.