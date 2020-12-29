---
layout: post
title: "Android BLE AutoConnect와 활용"
subtitle: "저전력 블루투스 기기 자동 연결"
author: "GuGyu"
header-style: text
tags:
  - Android
  - Bluetooth
  - BluetoothLowEnerge
  - BLE
  - GATT
  - AutoConnect
  - DirectConnect
---
  
무선 이어폰을 사용하면 한 번 등록한 기기의 경우 전원을 켜기만 해도 스마트폰과 바로 연결이 된다. 안드로이드 OS는 블루투스가 켜져있는 내내 블루투스 기기들을 검색하고 있는 것일까?

# AutoConnect

```kotlin
var bluetoothGatt: BluetoothGatt? = null
...

bluetoothGatt = device.connectGatt(this, false, gattCallback)
```

[공식문서](https://developer.android.com/guide/topics/connectivity/bluetooth-le?hl=ko#connect)를 살펴보면, 위와 같은 스니펫을 볼 수 있다. `GATT Server`에 연결할 때 사용하는 `connectGatt()` 메서드의 두 번째 파라미터로 "이용 가능한 즉시 블루투스 기기에 자동 연결할지 나타내는 부울"을 넘겨줌으로써 주제의 기능을 사용할 수 있는 것이다. 이를 `true`로 설정하면 `AutoConnect`, `false`로 설정하면 `DirectConnect`로 연결된다.
<br>

# DirectConnect 와 AutoConnect

`DirectConnect`는 `AutoConnect`와 반대되는 개념이다. `DirectConnect`는 말 그대로 직접 연결하는 것이다. 블루투스 설정 창에서 감지된 기기를 터치해 **직접** 연결을 시도하는 것이 바로 그것이다. 안드로이드 OS는 `DirectConnect`를 위한 연결 시도를 30초로 제한하고 있다. 30초 이내에 연결을 성공하지 못하면 실패한 것으로 간주하고 더이상 연결을 시도하지 않는다. 또한 한 번에 하나의 기기만 연결을 시도할 수 있다. 이에 비해 `AutoConnect`는 한 번 연결된 적 있는 디바이스와는 사용자의 상호작용과 관계없이 자동으로 연결한다. 이때 연결을 위한 시도에는 별다른 시간적 제한이 없다. 동시에 여러 개의 기기와 연결을 시도할 수도 있다. 따라서 연결해 사용하고 있던 블루투스 기기가 멀어지거나 전원이 꺼지는 등의 이유로 더 이상 연결을 지속할 수 없게 되면 안드로이드 OS는 블루투스 기능을 끄지 않는다면 해당 블루투스 기기와 다시 연결하기 위한 시도를 지속한다.  

하지만 사용자가 더이상 연결을 원하지 않을 경우도 분명 있다. 이때는 `disconnect()`나 `close()`를 통해 직접 연결을 해제할 수 있다. 이 경우, 블루투스 기기의 신호가 여전히 유효하더라도 연결을 끊고 안드로이드 OS는 더이상 해당 기기와 연결을 자동으로 실행하지 않는다. 즉 바꾸어 말하면 `AutoConnect`는 `disconnect()`나 `close()`를 실행하지 않는 이상 계속 해당 기기와의 연결을 자동으로 시도한다는 것이다.  

그렇다면 이 두 방법은 연결 시도의 시간 차이와, 한 번에 여러 대의 기기와 연결이 가능한지 여부에만 차이가 있을까? 당연히 그렇지 않다. `DirectConnect`가 연결 시도 시간에 제한을 가진 대신, `AutoConnect`보다 더 높은 우선 순위를 갖고 있으며 더욱 짧은 주기로 연결 가능한 `advertisement`들을 검색한다. 즉 `AutoConnect`는 지속적으로 재연결 시도를 하고 있지만 실질적으로 `advertisement`를 검색하는 주기는 `DirectConnect`보다 길다는 것이다. 따라서 `DriectConnect`를 시도하는 동안, `DirectConnect Queue`가 비어있지 않으면 `AutoConnect` 시도는 잠시 중단되고, `DirectConnect`를 보다 **집중적**으로 시도한다는 것이다.  

하지만 이 둘의 차이는 사실 Android 10 부터는 꽤 옅어졌다. `DirectConnect Queue`가 삭제되었고, 이를 처리하는 동안 `AutoConnect`가 멈추지도 않는다. 대신, `DirectConnect`에 `whitelist`를 도입해 `AutoConnect`와 비슷하게 동작하도록 했다. 따라서 사실상 이 둘의 큰 성능 차이는 없고 개념적인 구분만 남아있는 상태이다.

현 시점에서 결론을 간단하게 정리하자면 `connectGatt()` 메서드의 두 번째 파라미터, `autuConnect = true`로 두고 디바이스와 연결을 하게 되면 디바이스의 `address`를 `whitelist`에 추가한다. OS는 `whitelist`에 있지만 연결되어 있지 않은 디바이스와는 시간 제한 없이 지속적으로 연결을 시도하기 때문에 해당 기기와의 연결은 의도적으로 끊지 않는 이상 계속 재연결 될 것이다. 하지만 `autoConnect = false`로 두고 디바이스와 연결을 하면 30초의 연결 제한 시간이 있고 연결이 끊어진다면 재연결 시도는 자동으로 이루어지지 않는다.  
<br>  

# Whitelist

사실 `autoConnect = true`로 기기와 연결(페어링)을 성공했다고 해서 무조건 `whitelist`에 주소가 등록되는 것은 아니다. 다음과 같은 조건 중 하나 이상을 만족해야한다.
- 블루투스 기기와 `bind` 되어야 한다.
- 블루투스 기기의 주소가 `public` 해야한다.
- 블루투스 기기의 주소가 `random static address`여야 한다.  

즉 `Random resolvable address`를 갖거나 `Random non-Resovable address`를 갖는 블루투스 기기와 `bind` 조차 하지 못했다면 `autoConnect = true`로 연결을 했다 하더라도 `whitelist`에 추가되지 못하고 `autoConnect = false`인 것 처럼 동작하게 된다.  
<br>

# AutoConnect 활용

`autoConnect = true`를 활용하는 경우에는 웨어러블 디바이스나 블루투스 키보드/마우스 등 백그라운드에서 지속적인 작업을 하는 경우가 많다. 그러나 `BLE Gatt Connect/transmission`은 이를 실행 중인 앱이 종료되지 않도록 유지시켜주지 못한다. 다시 말하면, 앱에서 연결하고 데이터를 주고받고 있을 때, 앱을 백그라운드로 보내고 다른 작업을 하게 되면 언제든 OS에 의해 `kill` 당할 위험이 있다는 것이다. 따라서 `ForegroundService` 를 함께 사용해야한다.  

실제로 나 같은 경우에는, 앱이 백그라운드로 나갔을 때 `onPause()` 에서 `startForegroundService()`를 이용해 서비스를 실행하였다. 이후 작업 관리자에서 앱을 종료해 `onDestroy()`가 호출된 후에도 블루투스 기기가 계속 연결되어 있는 것을 확인할 수 있었다. 이후 블루투스 기기의 전원을 껐다가 한참 뒤에 다시 켜봤더니 곧바로 기기와 연결이 되었고, 앱을 실행시키자 기기와 연결된 상태로 액티비티에 진행하는 것을 확인하였다.  
<br>
<br>


--- 
참고  
autoConnect/directConenct :  
[https://stackoverflow.com/questions/40156699/which-correct-flag-of-autoconnect-in-connectgatt-of-ble](https://stackoverflow.com/questions/40156699/which-correct-flag-of-autoconnect-in-connectgatt-of-ble)  

bluetooth/whitelist :  
[https://devzone.nordicsemi.com/nordic/nordic-blog/b/blog/posts/what-to-keep-in-mind-when-developing-your-ble-andr](https://devzone.nordicsemi.com/nordic/nordic-blog/b/blog/posts/what-to-keep-in-mind-when-developing-your-ble-andr)  

bind/paring :  
[https://piratecomm.wordpress.com/2014/01/19/ble-pairing-vs-bonding/](https://piratecomm.wordpress.com/2014/01/19/ble-pairing-vs-bonding/)