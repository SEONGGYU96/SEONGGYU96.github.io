---
layout: post
title: "안드로이드 오픈소스 라이브러리 만들기"
subtitle: "iOS 느낌의 둥근 다이얼로그"
author: "GuGyu"
header-style: text
tags:
  - Android
  - Library
  - Custom
  - Jitpack
  - Github
  - Builder
---
  
안드로이드를 포함하여, 개발을 하다보면 자주 사용하는 클래스나 모듈이 생기기 마련이다. 나 같은 경우에는 안드로이드 앱을 개발하면서 매번 비슷한 구조의 비슷한 디자인의 다이얼로그를 사용하고 있음을 발견하였다. 우선 내가 빌더 패턴을 사용한 다이얼로그 사용이 굉장히 편하게 느껴졌고, 함께 일하는 디자이너도 다이얼로그는 매번 심플하고 비슷한 디자인을 사용했기 때문이다. 3번째 똑같은 다이얼로그 코드를 작성하면서, 이건 꼭 라이브러리로 만들어두어야겠다는 생각을 했다. 그리고 Github에 찾아보니 은근 내가 사용하는 형태의 오픈소스 다이얼로그가 없었다. 그래서 이왕 만드는 김에 `Jitpack`을 이용한 오픈소스 라이브러리 배포를 하게 되었다.

# 안드로이드 라이브러리 모듈 생성

배포 유무에 관계없이, 우선은 라이브러리라 함은 어떤 프로젝트에서도 가져다 쓸 수 있는 형태로 만들어두어야한다. 이를 `모듈`이라고 하는데, 안드로이드 스튜디오는 `모듈`을 쉽게 만들 수 있도록 기능을 제공한다.  
<br>

<img src="https://user-images.githubusercontent.com/57310034/103052238-fb3a7a00-45db-11eb-85c6-fbc0c0f357c3.png">  

우선은 일반적인 방법으로 빈 프로젝트를 생성했다. 이 빈 프로젝트는 곧 내가 만든 라이브러리의 사용 케이스를 담은 데포 프로젝트이기 때문에 `Demo` 키워드를 붙여주었다. 하지만 업로드 될 원격 저장소는 라이브러리를 담고 소개하기 때문에, 폴더 이름에는 `Lib` 키워드를 붙여주었다.  
<br>  

<img src="https://user-images.githubusercontent.com/57310034/103053574-135ec900-45dd-11eb-83b3-47207537ba92.png"/>

`File -> New -> New Module` 을 눌러 모듈 타입을 선택할 수 있다. 나는 안드로이드 라이브러리를 선택했다.  
<br>

<img src="https://user-images.githubusercontent.com/57310034/103054526-57ea6480-45dd-11eb-923f-9bab36d2e739.png"/>  

모듈의 이름은 `Lib` 키워드를 붙여주었고, 사용할 언어와 최소 SDK 레벨을 지정해주었다.  
<br>

<img width="200" src="https://user-images.githubusercontent.com/57310034/103055754-71d87700-45de-11eb-8339-d1630bcac9de.png"/>

그럼 이렇게 기존에 있던 `app` 프로젝트 패키지 말고도 `RoundDialogLib` 모듈 폴더가 생긴다. 구성은 프로젝트 패키지와 거의 동일하다. 리소스를 추가하여 사용할 수도 있고 테스트도 물론 가능하다. 이 패키지 안에 다이얼로그를 구성하는 클래스를 작성하면 끝이다.

# 다이얼로그의 구성

만들고자하는 다이얼로그를 위해서, 총 3개의 클래스를 만들었다. `DialogFragment`를 상곡하는 `RoundDialog` 클래스, 다이얼로그의 다양한 속성을 쉽게 적용하여 인스턴스를 생성할 수 있는 `RoundDialogBuilder` 클래스, 마지막으로 다이얼로그에 사용될 버튼의 문구와 리스너를 가지고 있는 `RoundDialogButton` 클래스가 그것이다.  
<br>

## `RoundDialog`

```kotlin
internal class RoundDialog(
    private val title: String?, //제목
    @LayoutRes private val contentViewRes: Int?, //내용 레이아웃
    private val contentText: String?, //내용 문구
    private val contentTextBold: Boolean, //내용 문구 스타일
    private val horizontalButtons: List<RoundDialogButton>?, //가로 버튼 리스트
    private val verticalButtons: List<RoundDialogButton>?, //세로 버튼 리스트
    private var fontSize: Float?, //전체 폰트 사이즈
    @ColorRes private val pointColor: Int?, //제목과 버튼에 적용될 포인트 컬러
    @ColorRes private val dividerColor: Int?, //구분선 컬러
    private val enableCancel: Boolean, //다이얼로그 바깥을 눌러 취소가 가능한지 유무
    private val enableDivider: Boolean //구분선의 유무

) : DialogFragment() {
    ...
}
```

`RoundDialog`는 위와 같이 굉장히 많은 생성자 파라미터를 가지고 있다. 그도 당연한 것이, 최대한 다양한 프로젝트, 다양한 상황에서 사용할 수 있게 다양한 모습으로 커스텀 할 수 있도록 만들었기 때문이다. 따라서 이 모든 속성들이 필수는 아니다.  
그리고 이 다양한 속성들을 사용해서 내가 원하는 형태와 기능을 하는 다이얼로그를 만들기 위해서는 `DialogFragment` 나 `Dialog` 클래스를 확장해야한다.  
<br>

### `DialogFragment` vs `Dialog` 

커스텀 다이얼로그를 만들기 위한 방법을 찾아보면, `Dialog`를 확장하거나 `DialogFramgent`를 확장하는 방법이 나온다. 참고로 안드로이드 표준 라이브러리로 제공되는 `AlertDialog`는 `Dialog`를 확장한 클래스이다. 그렇다면 `Dialog`와 `DialogFragment` 의 차이는 뭘까?  
<br>

<img src="https://user-images.githubusercontent.com/57310034/103115195-fee60380-46a4-11eb-8a04-f2fa10f78bb1.png"/>  

`Dialog`의 경우, 최상위 객체인 `Object` 를 확장하며 `DialogInterface`, `Window.Callback`, `KeyEvent.Callback`, `View.OnCreateContextMenuListener` 인터페이스를 구현하고 있다. 무려 API 1 레벨부터 사용된 뿌리깊은 클래스다. 그렇다면 `DialogFragment`는 어떻게 구현되어있을까?  
<br>

<img src="https://user-images.githubusercontent.com/57310034/103115497-18d41600-46a6-11eb-9b7f-d5ae72f64951.png"/>  

`Fragment`를 상속하고 `DialogInterface`의 `OnCancelListener`와 `OnDismissListener`만을 구현하고 있는 것을 보고있다. 그럼 `DialogFramgment`는 `Dialog`와는 근본부터가 다른, 완전히 별개의 클래스인걸까? 물론 그렇지 않다.  

`DialogFramgnemt` 는 `Fragment`를 상속하고 있긴하지만, 내부적으로 `Dialog` 의 인스턴스를 생성하여 프래그먼트의 생명 주기에 따라 적절하게 보여준다. `Fragment`에 대한 개념이 안드로이드에 정착하면서 자체적인 UI를 가지고 있는 `Dialog` 또한 `Fragment` 로 다뤄 재사용성을 높히려고 한 것 같다.  

결과적으로 두 클래스 모두 커스텀 다이얼로그를 위해 확장할 수 있으며 프래그먼트냐 아니냐 이 차이만 존재한다. 나는 `Fragment`처럼 사용하는 것이 조금 더 익숙하기 때문에 `DialogFragment` 를 확장하여 커스텀 다이얼로그를 만들었다.  
<br>

### 다이얼로그 크기 조절

```kotlin
private const val DIALOG_WITH_RATIO = 0.7
...

val size = Point()
if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
    context!!.display
} else {
    (context!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
}?.getRealSize(size)

deviceSizeX = size.x

val params = dialog?.window?.attributes
params?.width = (deviceSizeX!! * DIALOG_WITH_RATIO).toInt()
dialog?.window?.attributes = params as WindowManager.LayoutParams
```

위와 같이 일반적인 방법으로 현재 프래그먼트가 속한 화면의 가로 길이를 구하고, 다이얼로그는 여기에 0.7배 만큼의 가로 길이를 갖도록 했다. 위에서 `DialogFragment`는 `Dialog` 인스턴스를 사용한다고 했다. 따라서 자바의 경우 `getDialog()`, 코틀린의 경우 그냥 `dialog` 로 해당 인스턴스에 접근하여 원하는대로 속성을 변경해줄 수 있다.  
<br>

```kotlin
private fun checkIsIllegalAttribute() {
    if (contentText != null && contentViewRes != null) {
        throw IllegalArgumentException("Content text and content view cannot be coexist")
    }
}
```

이후 `onCreateView`의 가장 처음에는 파라미터부터 검사해주었다. 내가 만드는 다이얼로그는 다이얼로그의 내용, 즉 `Content`가 일반적인 텍스트의 형태로 나타날 수도 있고 직접 만든 레이아웃의 형태로 나타날 수 있다. 하지만 이 두 가지가 동시에 적용될 순 없기 때문에 두 가지 파라미터가 모두 입력되면 예외를 던지도록 구성하였다.  

```kotlin
val rootView = FrameLayout(inflater.context)
```  

그 다음은 전체 다이얼로그의 최상위 뷰를 생성해주었다. 참고로 여기서는 xml 의 사용 없이 모두 동적으로 뷰를 생성해나가며 다이얼로그를 구성할 것이다.  
<br>

```kotlin
val cardView = CardView(rootView.context).apply {
    layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    radius = RADIUS_OF_DIALOG_DP.toPixel().toFloat()
    }
    val linearLayout = LinearLayout(rootView.context).apply { layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    orientation = LinearLayout.VERTICAL
}

initTitle(linearLayout)

initContent(linearLayout)

initButtons(linearLayout)

cardView.addView(linearLayout)

rootView.addView(cardView)
```
그 다음으로는 모서리를 둥글게 만들어줄 `CardView`를 생성하고 그 안에 각종 구성요소들을 쉽게 쌓을 수 있도록 `LinearLayout`도 만들어주었다. 이 `LinearLayout` 안에 제목, 내용, 버튼들이 차곡차곡 쌓여 만들어지게 된다.  
<br>

```kotlin
private fun initTitle(parent: ViewGroup) {
    if (title == null) {
        return
    }
    val titleTextView = getTitleTextView(parent.context)
    parent.addView(titleTextView)
}
```  

제목을 적용하는 것은 간단하다. `null`이 아니라면 해당 내용을 포함하는 `TextView`를 만들어 `LinearLayout`에 붙여주면 된다. `getTitleTextView()` 메서드는 폰트 사이즈, 굵기 등 속성들을 적용하여 `TextView`를 리턴해주는 메서드이다.
<br>

```kotlin
private fun initContent(parent: ViewGroup) {
    if (contentViewRes == null && contentText == null) {
        return
    }
    addDivider(parent, true)
    contentView = if (contentViewRes == null) {
        getContentTextView(parent.context).apply { text = contentText }
    } else {
        inflate(parent.context, contentViewRes, null)
    }
    parent.addView(contentView)
}
```  

내용을 적용할 때에는 적용하고자 하는 내용이 레이아웃인지, 텍스트인지 구분해야한다. 텍스트라면   `TextView`를 만들고 붙이고 레이아웃이라면 `inflate` 해서 붙여준다. 그리고 이를 붙이기 전에 `addDivider()`를 통해 구분선을 먼저 붙여주어야한다. 제목과 내용의 영역을 구분해줄 얇은 선 말이다.    
<br>

```kotlin
private fun addDivider(container: ViewGroup, isHorizontal: Boolean) {
    if (!enableDivider) {
        return
    }
    val divider = View(context)

    divider.layoutParams = if (isHorizontal) {
        LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1.toPixel())
    } else {
        LinearLayout.LayoutParams(1.toPixel(), LinearLayout.LayoutParams.MATCH_PARENT)
    }

    if (dividerColor == null) {
        divider.setBackgroundColor(Color.parseColor(DIVIDER_COLOR))
    } else {
        divider.setBackgroundResource(dividerColor)
    }
    container.addView(divider)
}
```  

구분선을 만드는 과정은 위와 같다. 가로인지 세로인지 판단한 후에, 굵기 `1dp` 의 얇은 `View`를 하나 만들어 컬러를 적용한 후에 붙여주는 것이다. 구분선의 유무, 컬러 등은 모두 사용자가 직접 설정가능한 것이다.  
<br>

```kotlin
private fun initButtons(parent: ViewGroup) {
    if (horizontalButtons == null && verticalButtons == null) {
        return
    }

    val buttonLayout = LinearLayout(parent.context).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        orientation = LinearLayout.VERTICAL
    }

    verticalButtons?.let {
        addDivider(buttonLayout, true)
        makeButtons(it, buttonLayout, false)
    }

    horizontalButtons?.let {
        addDivider(buttonLayout, true)
        //가로 버튼을 담을 하나의 LinearLayout을 생성
        val verticalButtonLayout = LinearLayout(parent.context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            orientation = LinearLayout.HORIZONTAL
        }

        //생성한 LinearLayout에 버튼들을 추가
        makeButtons(it, verticalButtonLayout, true)

        //버튼이 추가된 LinearLayout을 다이얼로그에 추가
        buttonLayout.addView(verticalButtonLayout)
    }

    parent.addView(buttonLayout)
}
```  

버튼의 경우 조금 복잡하다. 버튼들을 담을 새로운 세로 방향의 `LinearLayout`을 만든다. 이후 안에 세로 버튼과 가로 버튼들을 순서대로 넣어준다. 세로 버튼의 경우 구분선을 넣고 버튼을 넣어 쌓아주기만 하면 되면 가로버튼의 경우, 또 하나의 `LinearLayout`이 필요하다. 가로 방향으로 말이다. 그 안에 가로 버튼들을 구분선과 함께 차곡차곡 쌓아두고 가장 처음 만든 `LinearLayout`에 다시 넣어준다.  
<br>

```kotlin
private fun makeButtons(buttons: List<RoundDialogButton>, parent: ViewGroup, isHorizontal: Boolean) {
    for (roundDialogButton in buttons) {
        val button = getButtonTextView(parent.context).apply {
            layoutParams = if (isHorizontal) {
                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            } else {
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            }

            foreground = getRippleBackground(parent.context)

            //버튼에 문자열 적용
            text = roundDialogButton.text

            //클릭 리스너 적용
            setOnClickListener {
                roundDialogButton.listener?.let { it(contentView) }
                if (roundDialogButton.canDismiss) {
                    dismiss()
                }
            }
        }
    
        //첫 번째 버튼이 아니라면
        if (roundDialogButton != buttons[0]) {
            //구분선 삽입
            addDivider(parent, !isHorizontal)
        }

        //버튼 추가
        parent.addView(button)
    }
}
```
버튼 리스트에서 정보를 뽑아 뷰로 만들어주는 메서드이다. 편의상 버튼은 `TextView`로 대체했고 버튼 방향에 따라 `width`, `height`를 적용한다. 가로 버튼이라면 버튼들의 가로 길이가 모두 동일하고 그 합이 다이얼로그의 가로 길이와 동일해야하기 때문에 `weight` 값을 준다. 이후 버튼에 들어갈 문구나 터치 효과를 적용한 후에 부모 뷰에 붙여준다.  
<br>

```kotlin
dialog?.window?.run {
    setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    requestFeature(Window.FEATURE_NO_TITLE)
}
```
참, 다이얼로그의 배경색을 투명하게 바꾸는 것 또한 잊지 말아야한다. 그렇지 않으면 `CardView`로 둥글게 깎아도 다이얼로그 자체 배경색이 남아있기 때문에 각져보이기 때문이다.  
<br>

## `RoundDialogBuilder`

```kotlin
class RoundDialogBuilder {
    private var textSize: Float? = null

    private var title: String? = null
    private var contentText: String? = null

    private var enableCancel: Boolean = true
    private var contentIsBold: Boolean = false
    private var enableDivider: Boolean = true

    @ColorRes private var pointColor: Int? = null
    @LayoutRes private var contentRes: Int? = null
    @ColorRes private var divideColor: Int? = null

    private var verticalButtons: List<RoundDialogButton>? = null
    private var horizontalButtons: List<RoundDialogButton>? = null
    ...
}
```  
위에서 언급한 것 처럼, 이 다이얼로그에는 사용자가 직접 설정할 수 있는 속성이 굉장히 많다. 사용하든 사용하지 않든 생성자에 모두 작성하여 다이얼로그를 생성하는 것은 상당히 번거로울 것이다. 그래서 원하는 속성만을 쉽게 적용하여 `RoundDialog` 인스턴스를 얻을 수 있는, 빌더 패턴으로 만들어진 `RoundDialogBuilder` 를 제공한다. 각 속성들을 맴버 변수로 갖고 있다.

```kotlin
fun setTitle(title: String): RoundDialogBuilder {
    this.title = title
    return this
}

//버튼의 문구만 입력한 경우
fun addHorizontalButton(text: String): RoundDialogBuilder {
    return addHorizontalButton(text, true)
}

//버튼의 문구와 리스너를 입력한 경우
fun addHorizontalButton(text: String, listener: ((content: View?) -> Unit)?): RoundDialogBuilder {
    return addHorizontalButton(text, true, listener)
}

//버튼의 문구와 터치 시 다이얼로그를 닫을지 명시한 경우
fun addHorizontalButton(text: String, canDismiss: Boolean): RoundDialogBuilder {
    return addHorizontalButton(text, canDismiss, null)
}

//버튼의 모든 속성을 작성한 경우
fun addHorizontalButton(text: String, canDismiss: Boolean, listener: ((content: View?) -> Unit)?) : RoundDialogBuilder {
    if (horizontalButtons == null) {
        horizontalButtons = mutableListOf()
    }
    (horizontalButtons as MutableList<RoundDialogButton>).add(RoundDialogButton(text, canDismiss, listener))
    return this
}

fun enableDivider(enableDivider: Boolean) : RoundDialogBuilder {
    this.enableDivider = enableDivider
    return this
}
...
```
그리고 이런식으로 메서드를 작성해준다. 위 메서드들은 사용자가 직접 사용할 것들이기 때문에 최대한 간결하게 사용할 수 있도록 작성하였다. 예를 들어 위 `addHorizontalButton`의 경우 무려 4가지의 형태가 존재한다. 명시하고싶은 속성 말고는 초기값으로 설정할 수 있도록 다양한 형태를 제공하는 것이다.  
또한 모든 메서드는 자기 자신, `RoundDialogBuilder`를 리턴한다.  

```kotlin
RoundDialogBuilder()
    .setTitle("Normal")
    .setContentText("This is normal Dialog")
    .addVerticalButton("OK") {
        Toast.makeText(application, "You said OK!", Toast.LENGTH_SHORT).show()
    }
    .addVerticalButton("Cancel")
    .build()
    .show(supportFragmentManager, "normal")
}
```
그럼 위 처럼 하나의 메서드를 사용하여 속성을 적용한 다음, 바로 연달아 다른 속성을 적용할 수 있게 된다. 이렇게 `메서드 체이닝` 기법을 활용하여 사용자들이 원하는 속성만 가져다 쌓고, 마지막으로 `build()` 메서드를 호출함으로써 결과물을 얻게되는 것이 `빌더 패턴`이다.  
<br>

```kotlin
fun build(): DialogFragment {
    if (title == null && verticalButtons == null && horizontalButtons == null && contentRes == null && contentText == null) {
        throw IllegalAccessException("At least one attribute should be applied. (Title or Content or Buttons")
    }
    return RoundDialog(title,
        contentRes,
        contentText,
        contentIsBold,
        horizontalButtons,
        verticalButtons,
        textSize,
        pointColor,
        divideColor,
        enableCancel,
        enableDivider)
}
```  
실제로 `build()` 메서들을 살펴보면, 모든 속성들을 사용해 `RroundDialog` 인스턴스를 생성하고 리턴하는 것이 전부이다. 이 속성들은 사용자에 의해 변경된 것도 있을 것이고 초기값 그대로 남아있는 경우도 있을 것이다.  

# 다이얼로그 사용

위에서 잠깐 보여주었지만 이렇게 완성된 다이얼로그는 아주 간편하게 사용이 가능하다.  

<img width="300" src="https://user-images.githubusercontent.com/57310034/101352778-fd12f680-38d5-11eb-9a17-52387dec52ba.gif"/>  

```kotlin
RoundDialogBuilder()
    .setTitle("Custom content!")
    .setContentView(R.layout.layout_edittext)
    .addHorizontalButton("Done") {
        Toast.makeText(application, "Your name is " + it?.findViewById<EditText>(R.id.editText)?.text, Toast.LENGTH_SHORT).show()
    }
    .addHorizontalButton("Nope")
    .build()
    .show(supportFragmentManager, "edittext")
```
위처럼 단 몇 줄로 다이얼로그를 띄울 수 있고, 미리 레이아웃을 준비한다면 이렇게 특별한 형태의 다이얼로그를 만드는 것도 간단하다. `addHorizontalButton()`의 리스너는 `고차함수`이기 때문에 버튼을 눌렀을 때의 함수를 위와 같이 작성해줄 수 있다. 이 때 매개변수 `it`은 `inflate` 된 `contentView`를 가리키기 때문에 위와 같이 해당 뷰 안에 있는 요소에 접근하여 값을 가져오는 것도 가능한 것이다.  
<br>

```kotlin
internal class RoundDialog
class RoundDialogBuilder
internal data class RoundDialogButton
```
그리고 중요한 것은, 사용자에게 보여질 클래스 외에는 `internal` 접근 지정자를 명시해주어야 한다. `RoundDialog`를 직접 사용하는 방법에 대해서는 문서에 전혀 작성하지 않았기 때문에 함부로 사용하였다간 오류를 야기할 수 있다. `RoundDialogBuilder`를 통해 `RoundDialog`를 통해 해당 인스턴스를 얻을 수 있도록 안내하는 것이 바람직하다. 그리고 `RoundDialogButton` 데이터 클래스 또한 다이얼로그를 생성하면서 내부적으로만 사용하는 데이터클래스이므로 굳이 사용자에게 보여져 혼란을 줄 필요가 없다. 따라서 `RoundDialogBuilder`를 제외하고는 `internal` 접근 지정자를 명시하여 같은 모듈 내에서만 접근이 가능하도록 해주었다.

# 배포

<img src="https://user-images.githubusercontent.com/57310034/103145407-67081880-477d-11eb-9ca2-30be49116802.png"/>  
<img src="https://user-images.githubusercontent.com/57310034/103145409-68d1dc00-477d-11eb-81df-0885f14086ca.png"/>  

```gradle

dependencies {
    ...
    classpath "com.github.dcendents:android-maven-gradle-plugin:2.1"
}

allprojects {
    repositories {
        ...
        mavenCentral()
    }
}
```
이제 다 만든 모듈을 배포하면 끝이다. 프로젝트 수준의 `build.gradle`에서 위와 같이 종속성을 추가해준다. `dependency`에는 `maven-gradle-plugin`을, `repositories`에는 `mavenCentral()`을 말이다. 현 시점에서 `maven-gradle-plugin`의 최신 버전은 `2.1`인데 이는 [android-maven-gradle-plugin](https://github.com/dcendents/android-maven-gradle-plugin) 여기서 확인할 수 있다.

```
apply plugin: 'com.github.dcendents.android-maven'
group='com.github.자신의 깃허브 id'
```
그 다음, 새로 만든 모듈 (나의 경우엔 `RoundDialogLib` 모듈) 수준의 `build.gradle`에 위 두 줄을 추가해준다. 그리고 빌드 후에 원격 저장소에 `push` 해준다.  
<br>

<img src="https://user-images.githubusercontent.com/57310034/103145358-6d49c500-477c-11eb-9e1c-cc12da7b3d5f.png"/>

그리고 릴리즈 해준다.  
<br>

<img src="https://user-images.githubusercontent.com/57310034/103145377-d6313d00-477c-11eb-92ab-e0e9321f04d9.png"/>

[Jipack.io](https://jitpack.io/)에 들어가서 원격 저장소의 주소를 붙여넣는다. 프로젝트를 찾으면 자동으로 빌드를 시작하고 완료되면 위 처럼 `Log`에 로딩이 사라지고 문서 모양이 생긴다. 그럼 놀랍게도 끝이다 !  
<br> 

<img src="https://user-images.githubusercontent.com/57310034/103145430-acc4e100-477d-11eb-903a-592f1562cec5.png"/>

아래에 보면 위와 같이 사용법이 나와있다. 오픈소스를 사용할 때 많이 봤던 익숙한 사용법이다. 앞으로 이렇게 `dependency`만 추가해주면 내가 만든 다이얼로그를 어떤 안드로이드 프로젝트에서도 누구나 사용할 수 있다.  
<br>

# 마치며

드디어 미루고 미뤘던 다이얼로그 라이브러리를 만들어보았다. 배포하고 보니 개선할 수 있는 부분도 많이 보이고 추가하고 싶은 기능도 많이 떠올랐다. 그래서 앞으로도 꾸준히 버전 업데이트를 해보고자 한다. 그래서 많은 사람들이 사용하고 먼저 이슈를 남겨주는 경험도 해보고싶다. 하지만 홍보를 하기에는 아직 부족한 것 같아 여기에만 살짝 링크를 남겨놓고 글을 마친다.

Github : **[RoundDialogLib](https://github.com/SEONGGYU96/RoundDialogLib)**

---
참고  
[https://findanyanswer.com/what-is-the-difference-between-dialog-and-dialogfragment](https://findanyanswer.com/what-is-the-difference-between-dialog-and-dialogfragment)  

[https://mobikul.com/using-dialogfragment-instead-of-basic-dialog/](https://mobikul.com/using-dialogfragment-instead-of-basic-dialog/)