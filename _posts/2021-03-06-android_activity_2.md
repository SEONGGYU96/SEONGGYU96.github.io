---
layout: post
title: "안드로이드 액티비티 (Activity) - 2"
subtitle: "태스크"
author: "GuGyu"
header-style: text
tags:
  - Android
  - AndroidFramework
  - Study
  - Activity
  - Task
  - BackStack
---

[안드로이드 액티비티 (Activity) - 1](https://seonggyu96.github.io/2021/03/02/android_activity/)

## 태스크  

태스크는 간단하게 액티비티 작업 묶음 단위라고 보면 된다. 태스크의 예를 들어보자면 사진 리스트를 보고(PictureListActivity), 사진 상세를 살펴보고(PictureDetailActivity), 사진을 올리려고 카메라를 실행시킨다(별도 앱의 CameraActivity). 이러면 3개의 액티비티가 하나의 태스크가 되는데 여기서는 2개의 앱이 하나의 태스크가 된 경우이기도 하다. 즉 앱과 태스크는 일대일 대응이 아니라는 것을 염두에 두자. 여러 개의 앱이 하나의 태스크가 될 수도 있고, 하나의 앱에서도 태스크를 여러개 가지고 있을 수도 있다.  

### 백 스택  

액티비티는 백 스택(back stack)이라 불리는 스택에 차례대로 쌓인다. 태스크와 백 스택은 용어를 혼용해서 사용하기도 하는데 태스크는 액티비티의 모임이고 백 스택은 그 모임이 저장된 방식을 의미하는 것으로 이해하면 쉽다.  

이름에 스택이 포함된다고 해서 반드시 LIFO 방식을 고수하며 순서를 바꿀 수 없는 것은 아니다. 예를 들어 `Intent.FLAG_ACTIVITY_REORDER_TO_FRONT` 플래그를 사용하면 순서를 조정할 수 있다.  

프로그램의 흐름은 단순히 액티비티를 실행했다가 백 키로 돌아기만 하는 경우는 잘 없다. 상황에 따라 다양한 경로로 각각의 액티비티에 접근하기 때문에 내비게이션(화면 흐름)이 꼬이는 경우가 많다. 이를 방지하기 위해서는 태스크의 동작 방식을 이해하고 활용해야한다.  

<br>

## 태스크 상태  

태스크에는 화면에 포커스되어 있는 포그라운드 상태와, 화면에 보이지 않는 백그라운드 상태가 있다. 포그라운드에 있는 것은 홈 키를 통해서 언제든 백그라운드로 이동할 수 있고, 백그라운드에 있는 것도 언제든 포그라운드로 이동할 수 있다.  

태스크를 포그라운드나 백그라운드 상태로 바꾸는 메서드도 있는데 이 메서드에 대해서도 알아보자.

<br>

### 포그라운드에서 백그라운드로의 태스크 이동  

`Activity#moveTaskToBack(boolean nonRoot)` 메서드를 사용하면 포그라운드에서 백그라운드로 상태를 변경할 수 있다.  `nonRoot` 파라미터에 `true`가 들어가면 어느 위치에서건 백그라운드로 이동할 수 있고, `false`인 경우에는 태스크 루트일 때만 백그라운드 이동이 가능하다.  

이 메서드를 사용하는 예를 살펴보자면 카카오톡 등의 프라이버시를 포함하는 앱을 실행할 때 사용된다. 앱을 실행하기 전에 비밀번호를 입력하는 화면을 띄우는데, 이 화면은 기존 액티비티 위에 실행된 구조이기 때문에 백 키를 눌러 기존 화면으로 쉽게 돌아가버릴 수 있다. 그렇다고 백 스택을 모두 비워 기존 화면들을 없애버릴 수도 없기 때문에 `onBackPressed()` 메서드를 오버라이드해서 `moveTaskToBack(true)`를 호출하면 된다.  

<br>

### 백그라운드에서 포그라운드로의 태스크 이동  

반대로 백그라운드에서 포그라운드로 상태를 변경하는 메서드도 있다. 백그라운드에서는 액티비티가 보이지 않기 때문에 `Activity`에 포함된 메서드로는 안된다. 이때는 `ActivityManager#moveTaskToFront(int taskId, int flags)` 메서드를 사용하면 된다. 이 메서드는 허니콤부터 사용 가능하고, `android.permission.REORDER_TASKS` 퍼미션이 필요하다.  

```java
//ActivityManager 가져오기
ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

//해당 앱과 관련있는 태스크들을 읽어오기
List<ActivityManager.AppTask> runningTaskInfos = activityManager.getAppTasks();
        
for (ActivityManager.AppTask recentTaskInfo : runningTaskInfos) {
  //해당 앱과 패키지 이름이 동일하면
    if (recentTaskInfo.getTaskInfo().baseIntent.getComponent().getPackageName().equals(getPackageName())) {
        int taskId;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            taskId = recentTaskInfo.getTaskInfo().taskId;
        } else {
            taskId = recentTaskInfo.getTaskInfo().id;
        }
        //실행 중인 태스크만 포그라운드로 가져오기
        if (taskId > -1) {
            activityManager.moveTaskToFront(taskId, 0);
        }
    }
}
```

<br>

## dumpsys 명령어  

어느 화면에서 `startActivity()`를 실행해서 브라우저를 열었다면, 이 둘은 한 태스크 같아 보이지만 그렇지 않다. 브라우저는 singleTask launchMode로 되어 있어서 별도의 태스크로 실행된다. 이처럼 태스크를 확인할 때에는 눈으로 직접 화면을 전환하는 것으로 판단하기는 어렵다.  

이에 대한 대안으로 adb shell에서 `dumpsys` 명령어를 활용할 수 있다. `adb shell dumpsys activity activities`를 실행하거나, adb shell 내에서 `dumpsys activity activities`를 실행하면 된다. 마지막 옵션인 `activities`는 `a`로 줄여 쓸 수도 있다.  

그러나 대게 dumpsys 명령어의 출력 결과는 많기 때문에 바로바로 확인하기가 어렵다. 따라서 `adb shell dumpsys activity a > tasks.txt`오 같이 사용하면 편하다. 그리고 여기서 해당 앱 관련 내용만 보고 싶으면 `dumpsys activity a | grep com.example.android.androidx` 와 같이 특정 패키지와 관련한 라인만 볼 수 있다.  

```
    * TaskRecord{ba382b7 #43917 A=com.gugyu.taskexample U=0 StackId=1405 sz=3}
      affinity=com.gugyu.taskexample
      intent={act=android.intent.action.MAIN cat=[android.intent.category.LAUNCHER] flg=0x10000000 cmp=com.gugyu.taskexample/.FirstActivity}
      mActivityComponent=com.gugyu.taskexample/.FirstActivity
      Activities=[ActivityRecord{45443a2 u0 com.gugyu.taskexample/.FirstActivity t43917}, ActivityRecord{2e1d45d u0 com.gugyu.taskexample/.SecondActivity t43917}, ActivityRecord{466a3fc u0 com.gugyu.taskexample/.ThirdActivity t43917}]
      mRootProcess=ProcessRecord{a19044c 8025:com.gugyu.taskexample/u0a1367}
      * Hist #2: ActivityRecord{466a3fc u0 com.gugyu.taskexample/.ThirdActivity t43917}
          packageName=com.gugyu.taskexample processName=com.gugyu.taskexample
          launchedFromUid=11367 launchedFromPackage=com.gugyu.taskexample userId=0
          app=ProcessRecord{a19044c 8025:com.gugyu.taskexample/u0a1367}
          Intent { cmp=com.gugyu.taskexample/.ThirdActivity }
          frontOfTask=false task=TaskRecord{ba382b7 #43917 A=com.gugyu.taskexample U=0 StackId=1405 sz=3}
          taskAffinity=com.gugyu.taskexample
          mActivityComponent=com.gugyu.taskexample/.ThirdActivity
          baseDir=/data/app/com.gugyu.taskexample-mkvq2AE1kZKvcuA5RD0xuA==/base.apk
          dataDir=/data/user/0/com.gugyu.taskexample
      * Hist #1: ActivityRecord{2e1d45d u0 com.gugyu.taskexample/.SecondActivity t43917}
          packageName=com.gugyu.taskexample processName=com.gugyu.taskexample
          launchedFromUid=11367 launchedFromPackage=com.gugyu.taskexample userId=0
          app=ProcessRecord{a19044c 8025:com.gugyu.taskexample/u0a1367}
          Intent { cmp=com.gugyu.taskexample/.SecondActivity }
          frontOfTask=false task=TaskRecord{ba382b7 #43917 A=com.gugyu.taskexample U=0 StackId=1405 sz=3}
          taskAffinity=com.gugyu.taskexample
          mActivityComponent=com.gugyu.taskexample/.SecondActivity
          baseDir=/data/app/com.gugyu.taskexample-mkvq2AE1kZKvcuA5RD0xuA==/base.apk
          dataDir=/data/user/0/com.gugyu.taskexample
      * Hist #0: ActivityRecord{45443a2 u0 com.gugyu.taskexample/.FirstActivity t43917}
          packageName=com.gugyu.taskexample processName=com.gugyu.taskexample
          app=ProcessRecord{a19044c 8025:com.gugyu.taskexample/u0a1367}
          Intent { act=android.intent.action.MAIN cat=[android.intent.category.LAUNCHER] flg=0x10000000 cmp=com.gugyu.taskexample/.FirstActivity }
          frontOfTask=true task=TaskRecord{ba382b7 #43917 A=com.gugyu.taskexample U=0 StackId=1405 sz=3}
          taskAffinity=com.gugyu.taskexample
          mActivityComponent=com.gugyu.taskexample/.FirstActivity
          baseDir=/data/app/com.gugyu.taskexample-mkvq2AE1kZKvcuA5RD0xuA==/base.apk
          dataDir=/data/user/0/com.gugyu.taskexample
      TaskRecord{ba382b7 #43917 A=com.gugyu.taskexample U=0 StackId=1405 sz=3}
        Run #2: ActivityRecord{466a3fc u0 com.gugyu.taskexample/.ThirdActivity t43917}
        Run #1: ActivityRecord{2e1d45d u0 com.gugyu.taskexample/.SecondActivity t43917}
        Run #0: ActivityRecord{45443a2 u0 com.gugyu.taskexample/.FirstActivity t43917}
    mResumedActivity: ActivityRecord{466a3fc u0 com.gugyu.taskexample/.ThirdActivity t43917}
    mLastPausedActivity: ActivityRecord{2e1d45d u0 com.gugyu.taskexample/.SecondActivity t43917}
 ResumedActivity:ActivityRecord{466a3fc u0 com.gugyu.taskexample/.ThirdActivity t43917}
  ResumedActivity: ActivityRecord{466a3fc u0 com.gugyu.taskexample/.ThirdActivity t43917}
```

태스크는 최근에 사용한 액티비티 기준으로 먼저 위쪽에 나타난다.  

TaskRecord 섹션은 하나의 태스크를 이루고 태스크의 다양한 정보를 볼 수 있다. numActivities로 스택의 액티비티 개수를 알 수 있고, 그 안에 Hist 섹션의 ActivityRecord를 통해 스택의 액티비티 정보를 알 수 있다. ProcessRecord에는 프로세스명(패키지명) 앞뒤로 프로세스의 PID와 USER ID도 보여준다.  

위 내용에는 나오지 않았지만 TaskRecord에는 'app=null'이면서 'state=DESTROYED'인 것도 볼 수 있는데, 이는 프로세스가 종료된 것을 뜻한다. 말 그대로 히스토리이다.

### 포커스된 액티비티 찾기

`dumpsys` 명령어는 현재 포커스된 액티비티가 어떤 것인지 확인하는 데에도 유용하다.  

`adb shell dumpsys activity a | grep mFocusedActivity` 를 터미널에 입력해보자.  

<br>

## taskAffinity 속성  

위에서 `dumpsys` 명령어를 통해서 태스크 목록을 살펴보았다. 액티비티가 시작되면 특정 TaskRecord의 특정 ActivityRecord에 소속된다. 여기서 어디에 소속될지 결정하는 기준 중 한 가지가 바로 taskAffinity 문자열 속성이다. taskAffinity 속성은 단어 뜻으로 해석해보면 액티비티가 '관련된' 태스크에 들어갈 때 참고하는 값이라고 볼 수 있다.  

taskAffinity와 비슷한 속성으로 TaskRecord의 affinity가 있다. 이 둘은 비슷하지만 약간의 차이점을 가지고 있다.  

- taskAffinity : ActivityRecord에 속해있으며 AndroidManifest.xml의 액티비티 설정에 들어가는 값이다. `android:taskAffinity`로 설정할 수 있는데 기본값은 앱의 패키지명이다.  
- affinity : TaskRecord에 속해있으며 태스크를 시작한 액티비티의 taskAffinity 속성이다.

### 사용 시기  

액티비티를 시작하면 태스크에 들어가는 기준으로 taskAffinity 속성은 언제 사용하게 될까? 바로 AndroidManifest.xml의 액티비티 설정에서 `android:launchMode`에 `singleTask`를 지정하거나, 액티비티를 시작하는 `Intent`에 `FLAG_ACTIVITY_NEW_TASK` 플래그를 전달하는 경우에 사용된다. 이 두 가지 경우에 액티비티가 시작되면서 TaskRecord의 affinity가 액티비티의 taskActivity와 동일한 것을 찾아 그 태스크에 액티비티가 속하게 된다.

`singleTask` 속성 값이나 `FLAG_ACTIVITY_NEW_TASK` 플래그는 이름만 봤을 때 새로운 태스크를 생성하여 액티비티를 실행하는 것으로 이해하기 쉽다. 하지만 새로운 태스크를 생성하는 것은 기본 옵션이 아니라 부가 옵션으로 봐야한다. 이 경우, 시작하려는 액티비티의 taskAffinity에 따라서 결과가 달라진다. TaskRecord의 affinity가 실행하려는 taskAffinity와 동일한 게 있다면, 그 태스크에 액티비티가 포함되고, 그렇지 않다면 새로운 태스크가 시작된다. (새로 시작된 태스크의 affinity는 실행된 액티비티의 taskAffinity가 될 것이다.) 따라서 조금 더 명확하게 플래그 이름을 수정하자면 `FLAG_BELONG_OR_NEW_TASK`로 수정할 수 있을 것이다.  

### 액티비티 외의 컴포넌트에서의 액티비티 시작  

Activity에서 `startActivity()`를 실행하는 게 일반적이지만, BroadcastReceiver나 Service에서 `startActivity()`를 실행하기도 한다. 드물지만 Application에서 startActivity()를 실행하는 경우도 있다. Activity에서 `startActivity()`를 실행할 때 특별한 옵션이 없다면 피호출자는 호출자와 동일한 태스크에 올라가면 된다. 하지만 다른 컴포넌트에서 `startActivity()`를 실행하면 어느 태스크에 올라가야할까?  

Activity 외 다른 컴포넌트에서 `startActivity()`를 실행하면 다음과 같은 에러와 함께 크래시가 발생한다. 

> android.util.AndroidRuntimeException: Calling startActivity() from outside of an Activity  context requires the FLAG_ACTIVITY_NEW_TASK flag. Is this really what you want?  

따라서 에러 메시지에 있는대로 `FLAG_ACTIVITY_NEW_TASK` 플래그를 포함해야 한다.  

```java
Intent intent = new Intent(context, MainActivity.class);
intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
context.startActivity(intent);
```

그럼 앞서 설명한대로, `MainActivity`의 taskAffinity와 동일한 affinity를 가진 태스크가 있다면 그 태스크 위에 올라가고, 그런 태스크가 없다면 새로운 태스크를 생성하여 실행되고 새로 생성한 태스크의 baseActivity가 된다.  

### 속성 지정  

앞에서 언급했듯이 taskAffinity는 AndroidManifest.xml의 액티비티 선언에 `android:taskAffinity`로 지정할 수 있고, 속성이 없다면 디폴트 값은 패키지명이다. 결국 해당 속성을 선언하지 않은 것 끼리는 `FLAG_ACTIVITY_NEW_TASK` 속성을 쓰더라도 같은 태스크에 있게 된다. taskAffinity 속성을 지정할 때는 `android:taskAffinity`에 ':alarm'과 같이 콜론(:) 뒤에 구분자를 적는 것이 권장된다. 그러나 taskAffinity는 보통은 쓰지 않는 속성이다. 해당 속성을 별도로 지정하는 경우는 어떤 게 있을까? 

다른 화면들과 독립적으로 보여지는 알람 화면을 예로 들어보겠다. 알람 앱에 알람 리스트 화면(AlarmClock), 알람 설정 화면(AlarmSettings), 알람 화면(AlarmAlert)과 같이 3개의 액티비티가 있다. 만일 AlarmAlert에 `android:taskAffinity` 속성이 따로 없다면 어떻게 될까? 일정 시간이 되어 알림이 뜨는(AlarmAlert) 그 순간에 알람 앱의 태스크가 포그라운드나 백그라운드에 이미 있을 수도 있다. 포그라운드에 이미 있었다면 AlarmAlert 화면이 그 위에 추가되어서 포커스될 것이고, 백그라운드에 있다면 태스크가 포그라운드되면서 그 위에 AlarmAlert 화면이 뜰 것이다. 백 키를 누르면 방금 전까지 보이지 않았던 AlarmSettings 화면이 뜬다. 백그라운드에 있던 화면들까지 모두 딸려와서 흐름이 어색해지는 것이다.  

이런 케이스를 막기 위해 독립적인 화면인 AlarmAlert의 `android:taskAffinity` 속성을 별도로 지정하고, AlarmAlert를 호출할 때 `FLAG_ACTIVITY_NEW_TASK` 플래그를 포함해보자. 그럼 새로운 태스크로 알람 화면이 뜨게 되고, 백 스택에 다른 화면이 딸려오는 일이 없어진다. 이 경우에는 하나의 앱에서 2개의 태스크를 사용한 것이고 최근 사용 앱 목록을 보면 2개가 있는 것을 확인할 수 있다.  

만약 알람 태스크가 최근 앱 목록에 별도로 보이는 것을 방지하고 싶다면 AndroidManifest.xml의 AlarmAlert 선언에 `android:excludeFromRecents` 속성을 `true`로 하면 된다.  

<br>

## 태스크 속성 부여  

액티비티에 태스크 속성을 전혀 부여하지 않고서는 원하는 내비게이션을 만들기 어렵다. 동일한 액티비티라도 새로운 인스턴스로 계속 쌓이기 때문이다.  

속성을 부여하는 방법에는 2가지가 있다. 하나는 피호출자에서 스스로를 설정하는 방법(`android:launchMode`), 다른 하나는 호출자에서 피호출자를 설정하는 방법이다(Intent flag). 이 두 가지 방법을 조합할 수도 있다.  

<br>

### android:launchMode  

launchMode에는 standard, singleTop, singleTask, singleInstance가 있다. standard와 singleTop은 여러 인스턴스가 존재할 수 있고, singleTask와 singleInstance는 1개의 인스턴스만 존재한다. 태스크와 관련해서 가장 혼동되는 부분이 이 부분이다.  

#### standard

기본 값으로, 태스크의 topActivity에 매번 새로운 액티비티 인스터스를 생성해서 Intent를 전달한다. Activity의 `onCreate()` 메서드에서부터 `getIntent()` 메서드를 사용해서 전달된 값을 읽어들인다.  
#### singleTop 

호출하고자 하는 액티비티가 이미 `topActivity`에 있다면 새로 생성하지 않고, `onNewIntent()` 메서드로 Intent를 기존 인스턴스로 전달한다. topActivity에 없을 때는 standard와 동일하게 새로 생성한다. 즉 동일한 태스크에 여러 인스턴스가 존재할 수 있다.  

#### singleTask

태스크에 인스턴스는 1개 뿐이다. taskAffinity 값을 참고해서 들어가게 되는 태스크가 존재하고, 여기에 동일한 액티비티의 인스턴스가 이미 있다면 새로 생성하지 않고 `onNewIntent()` 메서드로 Intent를 기존 인스턴스에 전달한다. 태스크에 동일한 인스턴스가 없다면 새로 생성해 쌓는다. taskAffinity 값에 맞는 태스크가 없다면 새로운 태스크를 만들고 새로운 태스크의 baseActivity가 된다.  

ActivityB만 singleTask로 설정하고 ActivityA -> ActivityB -> ActivityC 순서로 호출하면 결과적으로 하나의 태스크에 모든 액티비티가 쌓일 것이다. 동일한 taskAffinity의 태스크가 존재하기 때문이다.  

그렇다면 ActivityA -> ActivityB -> ActivityC -> ActivityB 순서로 호출하면 어떻게 될까? ActivityC 위에 ActivityB가 올라가지 않고 ActivityC가 스택에서 제거되면서 ActivityB의 onNewIntent()가 호출된다. 결과적으로 태스크에는 [ActivityA, ActivityB]로 남는다.  

그럼 이번에는 ActivityB가 singleTask인 채로, taskAffinity도 변경해보자. ActivityA -> ActivityB -> ActivityC 순서로 호출하면 [ActivityA], [ActivityB, ActivityC]로 2개의 태스크가 남는다. 여기서도 ActivityA -> ActivityB -> ActivityC -> ActivityB 순서로 호출하면 ActivityC가 스택에서 제거되고 [ActivityA], [ActivityB]와 같이 2개의 태스크만 남는다.  

ActivityB만 taskAffinity를 지정했기때문에 ActivityC는 ActivityA와 동일한 taskAffinity일 것이다. 그런데 왜 ActivityC는 ActivityB의 태스크에 쌓이게 되었을까? ActivityC는 standard이기 때문에 호출자의 태스크에 쌓이는 것으로 정상적인 반응이다. ActivityC를 ActivityA의 태스크에 쌓기 위해서는 ActivityC도 singleTask로 지정하거나 플래그를 사용하면 된다.  

#### singleInstance

singleTask와 마찬가지로 태스크에 해당 액티비티 인스턴스가 1개뿐이며 태스크의 유일한 액티비티다. 즉 태스크에 포함된 액티비티는 이것 하나 밖에 없고 이 액티비티는 인스턴스를 하나만 가지고 사용한다는 의미이다. singleInstance로 지정된 액티비티에서 다른 액티비티를 시작하면 다른 태스크에 들어가게 되어, 새로운 태스크를 만드는 효과가 있다.  

ActivityB의 launchMode가 singleInstance일 때 ActivityA -> ActivityB -> ActivityC 순서로 호출하면 어떻게 될까?  

ActivityB는 당연히 별도의 태스크가 된다. 그런데 ActivityA와 ActivityC는 taskAffinity가 동일하기 때문에 동일한 태스크로 다시 묶인다. 즉 결과로 [ActivityB], [ActivityA, ActivityC]와 같이 2개의 태스크가 된다. ActivityC에서 백 키를 누르면 ActivityB가 아니라 ActivityA로 이동한다. 여기서 다시 백 키를 눌러야만 ActivityB를 볼 수 있다.  

singleTask와 다르게, ActivityB는 자신의 태스크에 자신의 인스턴스 하나만 존재할 수 있기 때문에 ActivityC를 호출하여도 같은 태스크에 쌓을 수 없는 것이다. 따라서 ActivityC는 포함될 태스크를 찾지 못하고 taskAffinity가 같은 ActivityA의 태스크에 쌓이게 된다.  

동일하게 ActivityA -> ActivityB -> DialogActivity 순서대로 호출을 하면 더 재밌는 일이 일어난다. ActivityB에서 DialogActivity를 띄웠지만 배경에는 ActivityA가 보이는 것을 알 수 있다.  

그러나 최종적으로 태스크가 2개가 되었음에도 최근 사용 앱 목록을 보면 singleInstance로 되어 있는 액티비티가 따로 보이지 않는다. 최근 사용 앱 목록도 taskAffinity가 기준이기 때문이다. 따라서 ActivityB의 taskAffinity를 다른 값으로 바꿔주면 결과적으로 태스크가 분리되는 것은 동일하나, 최근 사용 앱 목록에 2개가 따로 뜨게 된다.  

<br>

### Intent Flag  

Intent에는 `setFlags(int flags)` 메서드와 `addFlags(int flags)` 메서드가 있다. 여기에 전달되는 값은 `Intent` 클래스의 int 상수인 `FLAG_ACTIVITY_XXX` 값이고 비트 OR 연산(`|`)으로 여러 개를 전달할 수 있다.  

Intent 플래그에 전달하는 값은 피호출자의 launchMode보다 우선해서 적용된다. 따라서 모순되는 옵션일 경우 Intent 플래그 값이 launchMode를 오버라이드 하여 결과적으로 Intent 플래그 값에 따라 액티비티가 실행된다.  

`setFlag()` 메서드를 사용할 때에는 가능한 최소한의 플래그만 전달하는 것이 좋다. 최소한의 플래그로 의도를 명확히 하여야 내비게이션이 변경되어도 대응이 쉽다.  

#### FLAG_ACTIVITY_SINGLE_TOP

singleTop launchMode와 동일한 효과를 갖는다.  

#### FLAG_ACTIVITY_CLEAR_TOP

launchMode에 동일한 효과를 갖는 건 없다. 스택에서 피호출자보다 위에 있는 액티비티를 모두 종료시킨다. 스택에 [ActivityA, ActivityB, ActivityC]가 있다면 ActivityC에서 ActivityB를 시작할 때 이 플래그를 사용하면 ActivityC는 사라지고 [ActivityA, ActivityB]만 스택에 남는다. ActivityB가 standard 였다면 ActivityB도 제거한 다음 새로운 인스턴스를 생성하여 다시 `onCreate()` 부터 시작한다.  

따라서 이 플래그는 `FLAG_ACTIVITY_SINGLE_TOP` 플래그와 같이 쓰이는 경우가 많은데, 이때 피호출자는 스택의 가장 위에 남아있으므로 인스턴스를 재생성하지 않고 `onNewIntent()`로 새로운 Intent를 전달한다.  

#### FLAG_ACTIVITY_CLEAR_TASK

허니콤부터 사용이 가능하다. 피호출자가 시작되기 전에 관련된 스택이 모두 제거되고, 피호출자는 빈 태스크의 baseActivity가 된다. 이 플래그는 `FLAG_ACTIVITY_NEW_TASK`와 함께 사용되어야 한다. 앱을 사용하면서 태스크에 여러 액티비티를 쌓아놓았다가, 로그아웃하고 다른 아이디로 로그인한다면 이 플래그를 사용해서 태스크를 정리하고 메인 액티비티를 새로 시작하는 게 적절하다.  

#### FLAG_ACTIVITY_REORDER_TO_FRONT

스택에 동일한 액티비티가 이미 있으면 그 액티비티를 스택의 맨 위로 올린다. 해당 액티비티가 스택의 맨 위에 1개만 있어야 하는 경우에 쓸 수 있다. 하지만 주의해야할 점이 2가지 있다.  

1. `FLAG_ACTIVITY_CLEAR_TOP` 플래그와 함께 사용하면 옵션이 무시된다.  
2. 호출자가 액티비티일 때만 정상적으로 재배치(reorder)가 동작한다.  

<br>

## `<activity-alias>`  

AndroidManifest.xml 에는 activity-alias 엘리먼트가 있어서 액티비티의 별명을 **지정**할 수 있다. 이 별명의 용도는 뭘까?  

### 제거된 액티비티 대체  

activity-alias는 기존에 있던 액티비티가 소스에서 제거될 때 사용할 수 있다. 예를 들어 SplashActivity가 맨 처음 뜨는 화면이었는데 이를 없애고 바로 MainActivity를 보여주기로 수정을 했다. 그런데 [숏컷(shortcut)](https://developer.android.com/guide/topics/ui/shortcuts)과 같이 SplashActivity에 대한 링크가 기존 버전을 설치한 단말에 남아있는 경우가 있다. 이때 기존 숏컷이 MainActivity를 바라보게 해야하는데 이때 사용하는 것이 activity-alias이다.  

```xml
<activity-alias
    android:name=".SplashActivity"
    android:targetActivity=".MainActivity" />
```

`android:name` 속성에 반드시 존재하는 클래스명을 넣을 필요는 없다. 숏컷 외에도 `PendingIntent.getActivity()` 메서드로 알람에 등록되어 링크가 남는 경우도 있다. 대체하는 화면이 존재한다면 activity-alias로 기존 액티비티 이름을 남겨두는 것을 고려할 수 있다.  

### FLAG_ACTIVITY_CLEAR_TOP 의 한계 해결  

`FLAG_ACTIVITY_CLEAR_TOP` 플래그는 한계가 있다. 태스크에 ActivityA가 여러 개 있는 상태에서 ActivityA를 `FLAG_ACTIVITY_CLEAR_TOP` 플래그를 사용하여 호출하면 어떻게 될까? 본래 의도가 맨 아래에 있는 ActivityA만 남기는 것이라면 원하는대로 동작하지 않을 것이다. 맨 위에 있는 ActivityA를 기준으로 clear top이 되어서 Activity는 여전히 여러 개 남게 된다.  

이런 한계를 해결할 수 있는 방법이 activity-alias이다. ActivityA에 별명을 지어주고 첫 번째 ActivityA가 시작될 때는 별명으로 시작하면 된다. 그러면 activity-alias 이름으로 `startActivity`를 실행하면서 `FLAG_ACTIVITY_CLEAR_TOP` 플래그가 전달되면 원하는 결과를 얻을 수 있다.  

```xml
<activity-alias
    android:name=".FirstActivityA"
    android:targetActivity=".ActivityA" />
```

activity-alias에 지정한 이름은 실제 클래스가 아니므로 아래와 같이 `Component` 클래스에 별명을 전달해서 액티비티를 시작한다.  

```java
Intent intent = new Intent().setComponent(new Component(this, "com.gugyu.FirstActivityA"));
intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACITIVITY_SINGLE_TOP);
startActivity(intent);
```


<br>
<br>


--- 
해당 포스팅은 [안드로이드 프로그래밍 Next Step - 노재춘 저](http://www.yes24.com/Product/Goods/41085242) 을 바탕으로 내용을 보충하여 작성되었습니다.
