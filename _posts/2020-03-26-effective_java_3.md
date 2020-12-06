---
layout: post
title: "Effective Java - 2장 (2)"
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
**규칙2 - 생성자에 매개변수가 많다면 빌더를 고려하라.**

정적 팩토리와 생성자는 모두 선택적 매개변수가 많을 때 적절히 대응하기 어렵다.

선택적인 매개변수가 20개가 넘어가는 경우에는 매개변수를 1개 받는 생성자, 2개 받는 생성자, ... 끝도 없다.

보통 이런 생성자는 사용자가 원하지 않는 매개변수까지 포함하고 있는 경우가 많아서 어쩔 수 없이 0이라도 넣어주는 문제가 발생한다.

**즉, 이와 같은 점층적 생성자 패턴도 사용할 수는 있지만, 매개변수 개수가 많아지면 클라이언트 코드를 작성하거나 읽기가 어렵다.**

각 코드의 의미가 무엇인지도 헷갈리고 매개 변수를 몇 개나 썼는지 세어보면서 사용할 것이다.

이의 대안으로, **자바빈즈 패턴(JavaBeans pattern)** 을 알아보자.

자바빈즈 패턴은 매개변수가 없는 생성자로 객체를 만든 후, 세터 메서드들을 호출해 원하는 매개변수의 값을 설정하는 방식이다.

다음 예를 보자.

```java
NutritionFacts cocaCola = new NutritionFacts();
cocaCola.setServingSize(240);
cocaCola.setServings(8);
cocaCola.setCalories(100);
cocaCola.setSodium(35);
cocaCola.setCarbogydrate(27);
```

 코드가 길어지긴 했지만 인스턴스를 만들기 쉽고, 더 읽기 쉽다.

하지만 자바빈즈는 객체를 하나 만들기 위해서 메서드를 여러 개 호출해야하고, 객체가 완전히 완성되기 전까지는 일관성이 무너진 상태에 놓이게 된다.

일관성이 깨지면 버그를 심은 코드와 그 버그 때문에 런타임에 문제를 겪는 코드가 물리적으로 멀리 떨어져 있을 것이므로 디버깅도 만만치 않다.

따라서 자바빈즈 패턴에서는 클래스를 불변으로 만들 수 없으며 스레드 안정성을 얻으려면 추가 작업을 해주어야만 한다.

그럼 어떤 패턴을 사용하는 것이 좋을까?

점층적 생성자 패턴의 안정성과 자바빈즈 패턴의 가독성을 겸비한 **빌더 패턴(Builder pattern)** 이 있다.

클라이언트는 필요한 객체를 직접 만드는 대신, 필수 매개변수만으로 생성자(혹은 정적 팩토리 메서드)를 호출해 **빌더 객체**를 얻는다.

그런 다음, 빌더 객체가 제공하는 일종의 세터 메서드들로 원하는 선택 매개변수들을 설정할 수 있다.

마지막으로 매개변수가 없는 **build** 메서드를 호출해 사용자가 필요로 하는 객체를 얻을 수 있다.

빌더는 생성할 클래스 안에 정적 멤버 클래스로 만들어두는 게 보통이다.

다음 코드를 보자

```java
public class NutritionFacts {
    private final int servingSize;
    private final int servings;
    private final int calories;
    private final int fat;
    private final int sodium;
    private final int carbohydrate;

    public static class Builder {
        // 필수 매개변수
        private final int servingSize;
        private final int servings;

        // 선택 매개변수 - 기본값으로 초기화한다.
        private int calories      = 0;
        private int fat           = 0;
        private int sodium        = 0;
        private int carbohydrate  = 0;

        public Builder(int servingSize, int servings) {
            this.servingSize = servingSize;
            this.servings    = servings;
        }

        public Builder calories(int val)
        { calories = val;      return this; }
        public Builder fat(int val)
        { fat = val;           return this; }
        public Builder sodium(int val)
        { sodium = val;        return this; }
        public Builder carbohydrate(int val)
        { carbohydrate = val;  return this; }

        public NutritionFacts build() {
            return new NutritionFacts(this);
        }
    }

    private NutritionFacts(Builder builder) {
        servingSize  = builder.servingSize;
        servings     = builder.servings;
        calories     = builder.calories;
        fat          = builder.fat;
        sodium       = builder.sodium;
        carbohydrate = builder.carbohydrate;
    }
}
```

NutritionFacts 클래스는 불변이며, 모든 매개변수의 기본값들을 한 곳에 모아뒀다.

빌더의 세터 메서드들은 빌더 자신을 반환하기 때문에 연쇄적으로 호출할 수 있다. 이런 방식을 메서드 호출이 흐르듯 연결된다는 뜻으로

**플루언트 API(fluent API) **혹은 **메서드 체이닝(method chaining)**이라고 한다.

다음은 이 클래스를 사용하는 클라이언트 코드의 모습이다.

```java
NutritionFacts cocaCola = new NutritionFacts.Builder(240, 8)
        .calories(100).sodium(35).carbohydrate(27).build();

```

이 클라이언트 코드는 쓰기 쉽고, 무엇보다도 읽기 쉽다.

잘못된 매개변수를 검사하는 유효성 검사 코드를 삽입할 수도 있다.

그러기 위해서는 빌더의 생성자와 메서드에서 입력 매개변수를 검사하고 build 메서드가 호출하는 생성자에서 여러 매개변수에 걸친 불변식(invariant)을 검사하자.

\* 불변식 (invariant)

객체는 변경될 수 있어도 프로그램이 실행되는 동안, 혹은 정해진 기간 동안 반드시 만족해야 하는 조건.

ex) 리스트의 크기는 반드시 0 이상이어야 한다. 한 순간이라도 음수 값이 된다면 불변식이 깨진 것이다.

빌더 패턴은 계층적으로 설계된 클래스와 함께 쓰기에 좋다.

각 계층의 클래스에 관련 빌더를 맴버로 정의하면 된다.

다음 코드를 보자.

```java
public abstract class Pizza {
    public enum Topping { HAM, MUSHROOM, ONION, PEPPER, SAUSAGE }
    final Set<Topping> toppings;

    abstract static class Builder<T extends Builder<T>> {
        EnumSet<Topping> toppings = EnumSet.noneOf(Topping.class);
        public T addTopping(Topping topping) {
            toppings.add(Objects.requireNonNull(topping));
            return self();
        }

        abstract Pizza build();

        // 하위 클래스는 이 메서드를 재정의(overriding)하여
        // "this"를 반환하도록 해야 한다.
        protected abstract T self();
    }
    
    Pizza(Builder<?> builder) {
        toppings = builder.toppings.clone(); // 규칙 50 참조
    }
}
```

Pizza.Builder 클래스는 재귀적 타입 한정을 이용하는 제네릭 타입이다.

여기에 추상 메서드인 self를 더해 하위 클래스에서는 형변환을 하지 않고도 메서드 체이닝을 할 수 있다.

self 타입이 없는 자바를 위한 이 우회 방법을 시뮬레이트한 셀프 타입(simulated self-type) 관용구라 한다.

Pizza의 하위 클래스들을 보자. 하나는 일반적인 뉴욕 피자고, 다른 하나는 칼초네 피자다. 뉴욕 피자는 크기 매개변수를 필수로 받고, 칼초네 피자는 소스를 안에 넣을지 선택하는 매개변수를 필수로 받는다.

```java
public class NyPizza extends Pizza {
    public enum Size { SMALL, MEDIUM, LARGE }
    private final Size size;

    public static class Builder extends Pizza.Builder<Builder> {
        private final Size size;

        public Builder(Size size) {
            this.size = Objects.requireNonNull(size);
        }

        @Override public NyPizza build() {
            return new NyPizza(this);
        }

        @Override protected Builder self() { return this; }
    }

    private NyPizza(Builder builder) {
        super(builder);
        size = builder.size;
    }

    @Override public String toString() {
        return toppings + "로 토핑한 뉴욕 피자";
    }
}
```

```java
public class Calzone extends Pizza {
    private final boolean sauceInside;

    public static class Builder extends Pizza.Builder<Builder> {
        private boolean sauceInside = false; // 기본값

        public Builder sauceInside() {
            sauceInside = true;
            return this;
        }

        @Override public Calzone build() {
            return new Calzone(this);
        }

        @Override protected Builder self() { return this; }
    }

    private Calzone(Builder builder) {
        super(builder);
        sauceInside = builder.sauceInside;
    }

    @Override public String toString() {
        return String.format("%s로 토핑한 칼초네 피자 (소스는 %s에)",
                toppings, sauceInside ? "안" : "바깥");
    }
}
```

각 하위 클래스의 빌더가 정의한 builder 메서드는 해당하는 구체 하위 클래스 객체를 반환하도록 선언한다.

하위 클래스의 메서드가 상위 클래스의 메서드가 정의한 반환 타입이 아닌, 그 하위 타입을 반환하는 기능을 **공변 반환 타이핑(covariant return typing) **이라고 한다. 이 기능을 이용하여 클라이언트가 형변환에 신경 쓰지 않고도 빌더를 사용할 수 있다.

이러한 '계층적 빌더'를 사용하는 클라이언트의 코드도 앞선 영양정보 빌더 코드와 같이 사용할 수 있다.

다음 코드를 보자.

```java
NyPizza pizza = new NyPizza.Builder(SMALL)
                .addTopping(SAUSAGE).addTopping(ONION).build();
        Calzone calzone = new Calzone.Builder()
                .addTopping(HAM).sauceInside().build();
```

생성자로는 누릴 수 없는 사소한 이점으로, 가변인수(varargs) 매개변수를 여러 개 사용할 수 있다. 각각을 적절한 메서드로 나눠 선언하면 된다. 혹은 메서드를 여러 번 호출하도록 하고 각 호출 때 넘겨진 매개변수들을 하나의 필드로 모을 수도 있겠다. (여기서는 addTopping)

하지만 빌더 패턴에 장점만 있는 것은 아니다.

객체를 만들려면 빌더부터 만들어야한다. 빌더 생성 비용이 크지는 않지만 성능에 민감한 상황에서는 문제가 될 수 있다.

또한 코드가 장황해져서 매개변수가 4개 이상은 되어야 값어치를 한다.

물론 시간이 지날 수록 매개변수는 많아지는법이니 애초에 빌더로 시작하는 편이 나을 때가 많다.

정리하자면,

**생성자나 정적 팩토리가 처리해야 할 매개변수가 많다면 빌더 패턴을 선택하는 것이 더 낫다.**

매개변수 중 다수가 필수가 아니거나 같은 타입이면 더욱 더 그렇다.