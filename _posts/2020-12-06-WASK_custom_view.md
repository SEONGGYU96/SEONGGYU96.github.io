---
layout: post
title: "안드로이드 앱 현지화를 위한 커스텀 텍스트뷰 만들기"
subtitle: '단수/복수, 영어/한국어'
author: "GuGyu"
header-style: text
tags:
  - Android
  - WASK
  - Project
  - naccoro
  - English
  - CustomView
  - Locale
  - Language
  - TextView
---
# 개요

이전에 안드로이드 앱의 다국어 지원에 관한 포스팅을 작성한 적이 있다.  
[안드로이드 앱, 다국어 지원하기](https://seonggyu96.github.io/2020/12/05/WASK_translate_to_english/)  
<br>
나는 여기서, day/days 와 같이 보여주고자 하는 데이터에 따라 뒤에 붙는 문자열의 형태를 변경해주기 위해 커스텀뷰를 사용했다고 작성하였다.  
그때 구현한 커스텀뷰를 만드는 방법을 기록해두고자 한다.  

# 요구사항 파악

1. 한국어로 설정되었을 때는 `N일, N일 마다, N일 뒤` 등으로 변수 뒤에 문자열이 붙어야한다.
2. 영어로 설정되었을 때는 `Every N days, in N days` 등으로 변수 앞뒤로 문자열이 붙어야 한다.  
    - N이 1인 경우는 아예 생략하고 뒤에 붙는 문자열을 단수형(day)로 변경한다.  

# 구조 설계

요구사항에 따르면, 구현하고자 하는 부분은 아래의 구조를 띄게 된다.

> <b>`prefix` + `period` + `suffix`</b>  
`prefix` = "" | "Every" | "in"  
`period` = `int`형의 변수  
`suffix` = "day" | "days" | "일 마다" | "일 뒤" | "일 째 사용중"

여기서 우리가 직접 지정해주는 데이터는 `period` 뿐이고, 앞뒤로 조건에 맞는 문자열이 병합되는 구조이기 때문에 `AppCompatTextView`를 상속하는 커스텀뷰를 만들기로 했다.  
`prefix`와 `suffix`를 결정하는 조건은 다음과 같다.
1. 영어인가?
    - 교체 주기(Replacement Reminder)를 보여주는 문장인가?
    - 나중에 교체하기(Snooze Reminder)를 보여주는 문장인가?
    - 현재까지 마스크를 며칠 사용했는지 보여주는 문장인가?

2. 영어가 아닌가? (현재 한국어가 `default` 언어로 설정되어 있기 때문에 영어 외에는 한국어)
    - 교체 주기(Replacement Reminder)를 보여주는 문장인가?
    - 나중에 교체하기(Snooze Reminder)를 보여주는 문장인가?
    - 현재까지 마스크를 며칠 사용했는지 보여주는 문장인가?  

이렇게 나열하고 보면 영어인지 아닌지는 `if`, 그 아래 조건은 `switch`로 나누면 될 것 같다.  

```java
public class PeriodPresenter extends androidx.appcompat.widget.AppCompatTextView {
    private static final int REPLACEMENT_CYCLE = 0;
    private static final int SNOOZE_REMINDER = 1;
    private static final int CURRENT_USING = 2;

    private boolean isEnglishType;
    private int format;
    ...
}
```  
위와 같이 현재 설정된 언어가 영어인지 나타내는 `boolean` 타입의 `isEnglishType`과, 어떤 형태를 가져야하는지 나타내는 `int` 타입의 `format`을 맴버변수로 선언해주었다.  

여기서 `isEnglishType`은 한 어플리케이션 단위에서 모두 동일한 값을 가지기 때문에 이 클래스 내부에서 결정하여도 무방하다.  
그러나 `format`의 경우, 실행 중에 동적으로 변화하거나, 특정 시점 이후 초기화가 되어야하는 값이 아니기 때문에 `xml` 작성과 동시에 결정해줄 수 있도록 구현하였다.

``` 
public void setPeriod(int period) {
    ...
}
```
이후 해당 뷰를 사용하는 로직에서는 그저 `.setPeriod(int period)` 만 호출하면 그에 선행된 조건에 맞는 문자열의 형태로 표현해주는 `public` 타입의 메서드를 하나 열어주었다.

# 구현

## 커스텀 속성 추가하기

우선 `format`을 `xml` 레벨에서 결정할 수 있도록 커스텀 속성을 추가해주자.
<img width=600 float="left" src="https://user-images.githubusercontent.com/57310034/101274318-6adcf680-37e0-11eb-87dd-e14309a404ca.png"/>  
`res` > `values` 폴더에 `attrs.xml` 이라는 리소스 파일을 생성한다.  

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <declare-styleable name="PeriodPresenter">
        <attr name="type" format="enum">
            <enum name="replacement_cycle" value="0"/>
            <enum name="snooze_reminder" value="1"/>
            <enum name="current_using" value="2"/>
        </attr>
    </declare-styleable>
</resources>
```
나는 위와 같이 작성하였다.  
`declare-styleable`의 이름은 해당 속성을 사용하고자 하는 뷰 클래스의 이름을 적어주면 된다. 공식문서에 따르면 클래스의 이름과 맞추는 것은 필수는 아니지만 관례라고 한다.  
그 다음, 태그 내에 원하는 속성을 `attr` 태그를 통해 쭉 작성하면 되는데, 나는 `enum` 형태의 `type` 이라는 속성을 선언하였다.  
`enum` 형태는 내부에 `enum` 태그를 사용하여 나열한 값들 중 하나를 선택할 수 있도록 해준다.  
그리고 각각의 값은 함께 적어둔 `value`의 값과 매칭되어 전달된다.  
`attr` 태그에서 사용할 수 있는 `format` 속성의 값은 다음과 같다.  


| 값 | 사용 예 | 
|:--------|:--------|
| enum | `android:spinnerMode` | 
| boolean | `android:focusable` | 
| color | `android:colorBackground` | 
| dimension | `android:layout_height` | 
| flags | - (찾지 못함) | 
| float | `android:alpha` | 
| fraction | `android:inset` | 
| integer | `android:fontWeight` | 
| reference | `android:textAppearance` | 
| string | `android:fontFamily` |  

<br>

--- 
```xml
<com.naccoro.wask.customview.PeriodPresenter
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:type="snooze_reminder"
    ...
/>
```
그럼 이처럼 `type` 속성을 `xml`레벨에서 작성할 수 있게 된다.  
하지만 여기서 주의할 점은 네임스페이스가 `android`와 다르다는 것이다.  
직접 지정한 속성의 경우  
`http://schemas.android.com/apk/res/android` 에 속하는 것이 아니라  
`http://schemas.android.com/apk/res/[your package name]`에 속한다.  
따라서 `xmlns` 지시어를 사용하여 상위 뷰그룹에
```xml
<ConstraintLayout xmlns:custom="http://schemas.android.com/apk/res/com.naccoro.wask.customview.PeriodPresenter">
    <com.naccoro.wask.customview.PeriodPresenter
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    custom:type="snooze_reminder"
    .../>

    ...
</ConstraintLayout>
```
이렇게 선언해준 뒤에 `custom:type="snooze_reminder"` 로 속성을 부여할 수도 있다.  
`custom` 대신에 원하는 별칭을 사용해도 된다.  
하지만 `ConstraintLayout` 을 사용하는 경우, 
`xmlns:app="http://schemas.android.com/apk/res-auto"` 이 선언되어 있는데, 이놈을 사용하여도 무방하다.

## 커스텀 속성 가져오기

`AppCompatTextView`를 상속하는 클래스를 작성하기 위해서는 우선 3개 이상의 생성자가 필요하다.  
```java
public class PeriodPresenter extends androidx.appcompat.widget.AppCompatTextView {

    public PeriodPresenter(Context context) {
        super(context);
    }

    public PeriodPresenter(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PeriodPresenter(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    ...
}
``` 
뷰를 생성하는 것이기 때문에 뷰가 표현될 `Context`는 반드시 필요하고, `xml`에서 함께 작성된 속성들을 참조하는 있는 `AttributeSet`과 뷰의 스타일 속성을 참조하는 `defStyleAttr`을 선택적으로 필요로 한다.  
우리는 여기서 `AttributeSet` 을 사용할 것이다.  
<br>

`AttributeSet`에는 `xml` 태그에 작성된 모든 속성을 가지고 있고 직접 값을 읽을 수는 있지만 권장하지 않는다.  
공식 문서에 따르면 속성 값 내의 리소스 참조가 결정되지 않으며 스타일이 적용되지 않는다고 한다.  
따라서 `obtainStyledAttributes()` 메서드의 파라미터로 넘겨주어서 스타일이 지정된 값의 `TypedArray` 배열을 가져와 사용해야한다.

```java
if (attrs != null) {
    //TypedArray 형태로 xml에서 작성한 속성들 읽기
    TypedArray typedArray = getContext().getTheme()
        .obtainStyledAttributes(attrs, R.styleable.PeriodPresenter, 0, 0);

    //찾는 속성이 있는지 우선 검사
    if (typedArray.hasValue(R.styleable.PeriodPresenter_format)) {
        //있다면 해당 속성의 키를 int 형태로 가져온다.
        format = typedArray.getInt(R.styleable.PeriodPresenter_format, 0);
    } else {
        //속성이 없을 경우 기본값 지정
        format = REPLACEMENT_CYCLE;
    }
    //리소스 반환
    typedArray.recycle();
}
```
여기서 주의할 점은 `TypedArray`는 공유 리소스이기 때문에 다 사용한 후 `recycle()` 메서드로 반드시 반환해주어야한다.  

## 언어 설정 값 가져오기
이전 포스팅에 작성한 것 처럼 간단하게 디바이스의 언어 설정을 가져와 맴버변수를 초기화해준다.
```java
private void setLocaleDateType() {
        //설정된 국가 가져오기
        Locale locale = getResources().getConfiguration().locale;
        //국가로부터 언어 가져오기
        String language = locale.getLanguage();

        isEnglishType = language.equals("en");
    }
```

## 문장 병합

### Prefix

```java
private String getPrefix() {
    if (isEnglishType) { //영어인 경우
        switch (format) {
            case REPLACEMENT_CYCLE: //교체 주기 표현
                return getContext().getString(R.string.period_prefix_cycle); // "Every "
            case SNOOZE_REMINDER: //교체 미루기 표현
                return getContext().getString(R.string.period_prefix_snooze); //"in "
            case CURRENT_USING: //현재 사용 일자 표현
                return ""; //접두사 필요 없음
            default: //범위 외 값 예외처리
                throw new IllegalArgumentException("format value must be in 0 ~ 2");
        }
    } else { //영어 외(한국어)인 경우
        return ""; //어떤 경우에도 접두사 필요 없음
    }
}
```
영어인 경우에 각 상황에 따른 접두사를 `switch` 문으로 붙여준다.  
한국어의 경우에는 어떤 경우에도 `period` 앞에 별도의 문자열이 붙지 않으므로 빈 문자열을 리턴한다.  

### Period

```java
private String getPeriod(int period) {
    if (format == CURRENT_USING) { //현재 사용 일자의 경우 period를 나타내는 별도의 TextView가 있으므로 나타내지 않음
        return "";
    }
    if (isEnglishType) { //영어의 경우만 1일 일 때 period를 생략함
        return period == 1 ? "" : period + "";
    } else {
        return period + "";
    }
}
```
현재 사용 일자는 `period`를 나타내는 별도의 `TextView`가 있었기에.. (폰트 크기나 색이 다르고, 다국어 계획이 없을 때 구현되었음) 이 부분을 크게 건들이지 않기 위해서 여기서는 아예 나타내지 않도록 하였다.  
그리고 영어의 경우에는 `Every day`, `in day` 와 같이 1일일 때에는 숫자를 생략하기 때문에 이렇게 분기해주었다.  

### Suffix
```java
private String getSuffix(int period) {
    if (isEnglishType) { //영어일 경우 period에 맞춰 복수/단수를 구분함
        String suffix = period == 1 ? getContext().getString(R.string.period_suffix_singular) //" day"
            : getContext().getString(R.string.period_suffix_plural); //" days"
        if (format == CURRENT_USING) { //현재 사용 일자를 나타내는 경우에는 뒤에 추가로 문자열을 더 붙여줌 ("N days used")
            suffix += getContext().getString(R.string.period_suffix_used); //" used"
        }
        return suffix;
    } else { //한국어일 경우
        switch (format) { 
            case REPLACEMENT_CYCLE : //교체 주기 표현
                return getContext().getString(R.string.period_suffix_cycle); //"일 마다"
            case SNOOZE_REMINDER : //교체 미루기 표현
                return getContext().getString(R.string.period_suffix_snooze); //"일 뒤"
            case CURRENT_USING : //현재 사용 일자 표현
                return getContext().getString(R.string.period_suffix_current_using); //"일 째 사용중"
            default: //범위 외 값 예외처리
                throw new IllegalArgumentException("format value must be in 0 ~ 2");
        }
    }
}
```
`period` 뒤에 붙을 문자열은 영어일 경우 `period` 값에 따라 단수/복수를 구분하여 붙여주고, 한국어일 경우에는 직관적인 표현을 구분하여 붙여주었다.  

## 커스텀뷰를 사용할 `public` 메서드 뚫어두기

이제 위에서 만든 문자열을 순서대로 조합하여 줄 차례이다.  

```java
public void setPeriod(int period) {
    StringBuilder builder = new StringBuilder()
        .append(getPrefix())
        .append(getPeriod(period))
        .append(getSuffix(period));

    setText(builder.toString());
}
```  
`TextView`를 상속하였기 때문에 조합한 문자열을 스스로 `setText()` 해주면 된다.  
아예 `setText()`를 오버라이딩 해버릴까도 생각했지만, 마스크 교체 기록이 없는 경우 마스크 교체 기록이 없다는 일반적인 문자열을 보여주기도 해야하기 때문에 관두었다.  

## 사용

```java 
public void setSnoozeValue(int snoozeValue) {
    ...
    snoozeAlertLabel.setPeriod(snoozeValue);
}
```
이제 액티비티에서 초기값을 세팅하거나 값이 변경되어 업데이트 해줄 때, 이렇게 `.setPeriod()` 메서드만 호출해주면 알아서 표현된다.  
기존에는 `.setText(snoozeValue + "일")` 이런 식으로 작성이 되어 있었다.   물론 다국어를 위해서 여기서 매번 디바이스 언어 설정 값을 비교한 후에 `.setText("in " + snoozeValue + " days")` 이런식으로 어떻게든 해볼 수는 있었으나 이런 문자열은 앱 내에 3개나 있으므로 상당히 비효율적인 방법이었다.  
나는 그래서 이런 작업을 책임질 커스텀뷰를 만들었고 기존 로직은 크게 수정하지 않을 수 있었다.  
<br>

# 결과
## 영어
### 설정창
<img width="640" src="https://user-images.githubusercontent.com/57310034/101276447-3ffa9e80-37f0-11eb-89cc-d6aa74402401.png">

### 다이얼로그
<img width="640" src="https://user-images.githubusercontent.com/57310034/101276468-6a4c5c00-37f0-11eb-9088-66be9de6fbff.png">

### 메인화면
<img width="640" src="https://user-images.githubusercontent.com/57310034/101276495-8fd96580-37f0-11eb-9605-243e6faedfff.png"/>

## 한국어

### 설정창
<img width="300" src="https://user-images.githubusercontent.com/57310034/101270382-dbbde780-37bb-11eb-9b83-7161093248d6.png">

### 다이얼로그
<p>
<img width="640" src="https://user-images.githubusercontent.com/57310034/101276520-cdd68980-37f0-11eb-9780-0009292cf2ce.png"/>


끝

# 참조

https://developer.android.com/training/custom-views/create-view?hl=en#java  
