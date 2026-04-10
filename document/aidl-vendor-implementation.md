# How AIDL Interfaces Work with Vendor .so Files

## Overview

There are **two separate layers** of AIDL interfaces in this SDK:

1. **SDK Internal AIDL** - Communication between SDK processes (implemented in this project)
2. **Terminal/Modem AIDL** - Communication with phone's modem chip (implemented by vendor)

---

## Layer 1: SDK Internal AIDL (This Project)

### Purpose
Communication between the SDK's parent process and mini app child processes.

### Interfaces

| AIDL File | Implemented By | Location in This Project |
|-----------|----------------|--------------------------|
| `IMiniToParent.aidl` | `MiniAppManager.kt` | `core/src/main/java/.../miniapp/MiniAppManager.kt` |
| `IParentToMini.aidl` | `MiniAppActivity.kt` | `core/src/main/java/.../miniapp/MiniAppActivity.kt` |
| `IMessageCallback.aidl` | Various use cases | `core/src/main/java/.../usecase/` |
| `IDCCallback.aidl` | `MiniAppManager.kt` | `core/src/main/java/.../miniapp/MiniAppManager.kt` |

### How It Works

```
┌───────────────────────────────────────────────────────────────┐
│ PARENT PROCESS (Main SDK)                                     │
│                                                                │
│ ┌────────────────────────────────────────────────────────┐    │
│ │ MiniAppManager.kt (Kotlin)                             │    │
│ │                                                         │    │
│ │ Implements: IMiniToParent (AIDL interface)             │    │
│ │                                                         │    │
│ │ override fun createDC(...) {                           │    │
│ │   // Kotlin implementation                             │    │
│ │   dcManager.createApplicationDataChannels(...)         │    │
│ │ }                                                       │    │
│ │                                                         │    │
│ │ override fun sendMessageToParent(...) {                │    │
│ │   // Handle message from mini app                      │    │
│ │ }                                                       │    │
│ └────────────────────────────────────────────────────────┘    │
└───────────────────────────────────────────────────────────────┘
                           ↕ (Android Binder IPC)
┌───────────────────────────────────────────────────────────────┐
│ CHILD PROCESS (Mini App)                                      │
│                                                                │
│ ┌────────────────────────────────────────────────────────┐    │
│ │ DCMiniUseCase.kt (Kotlin)                              │    │
│ │                                                         │    │
│ │ Calls: IMiniToParent via miniToParentManager           │    │
│ │                                                         │    │
│ │ miniToParentManager.createDC(dcLabels, description)    │    │
│ │                                                         │    │
│ │ // This AIDL call crosses process boundary              │    │
│ │ // Android Binder handles marshalling                  │    │
│ └────────────────────────────────────────────────────────┘    │
└───────────────────────────────────────────────────────────────┘
```

**Key Point:** Both sides (parent and mini app) are **Kotlin code in this project**. No vendor involvement.

---

## Layer 2: Terminal/Modem AIDL (Vendor Implements)

### Purpose
Communication between SDK and the phone's 5G modem chip.

### Interfaces

| AIDL File | Implemented By | Location |
|-----------|----------------|----------|
| `IImsDataChannel.aidl` | **VENDOR** (Qualcomm/MediaTek) | `/vendor/lib64/libimsdc.so` (native C++) |
| `IImsDataChannelServiceController.aidl` | **VENDOR** | `/vendor/lib64/` or system service |
| `IImsDataChannelCallback.aidl` | SDK (DCManager.kt) | This project (receives callbacks) |

### How Native .so Files Implement AIDL

Even though AIDL is defined in Java/Kotlin syntax, vendors implement it in C++ using **Android's HIDL/AIDL C++ bindings**.

```
┌───────────────────────────────────────────────────────────────┐
│ SDK (This Project - Kotlin)                                   │
│                                                                │
│ ┌────────────────────────────────────────────────────────┐    │
│ │ DCManager.kt                                           │    │
│ │                                                         │    │
│ │ val dc: IImsDataChannel = ... // from vendor           │    │
│ │ dc.send(data, size, callback)                          │    │
│ │                                                         │    │
│ │ // This calls vendor's native implementation           │    │
│ └────────────────────────────────────────────────────────┘    │
└───────────────────────────────────────────────────────────────┘
                           ↕ (Android Binder IPC)
                           ↕ (Java → Native translation)
┌───────────────────────────────────────────────────────────────┐
│ VENDOR SERVICE (Native C++)                                   │
│ Package: /vendor/lib64/libimsdc.so                            │
│                                                                │
│ ┌────────────────────────────────────────────────────────┐    │
│ │ ImsDataChannelImpl.cpp (C++)                           │    │
│ │                                                         │    │
│ │ class ImsDataChannelImpl : public IImsDataChannel {    │    │
│ │   Status send(const vector<uint8_t>& data,            │    │
│ │               int32_t size,                            │    │
│ │               const sp<IDCSendDataCallback>& callback) │    │
│ │               override {                               │    │
│ │                                                         │    │
│ │     // 1. Call Hardware Abstraction Layer (HAL)       │    │
│ │     mModemHal->sendData(data, size);                  │    │
│ │                                                         │    │
│ │     // 2. Modem chip processes it                     │    │
│ │     // 3. Callback with result                        │    │
│ │     callback->onSendDataResult(STATUS_OK);            │    │
│ │     return Status::ok();                              │    │
│ │   }                                                    │    │
│ │                                                         │    │
│ │   Status close() override {                           │    │
│ │     mModemHal->closeDataChannel();                    │    │
│ │     return Status::ok();                              │    │
│ │   }                                                    │    │
│ │ };                                                     │    │
│ └────────────────────────────────────────────────────────┘    │
└───────────────────────────────────────────────────────────────┘
                           ↕
┌───────────────────────────────────────────────────────────────┐
│ HARDWARE ABSTRACTION LAYER (HAL)                              │
│ /vendor/lib64/hw/android.hardware.radio@1.6-impl-qti.so      │
│                                                                │
│ Talks directly to modem chip via hardware-specific APIs       │
└───────────────────────────────────────────────────────────────┘
                           ↕
┌───────────────────────────────────────────────────────────────┐
│ MODEM FIRMWARE                                                │
│ Runs on 5G modem chip (Qualcomm X65, MediaTek M80, etc.)     │
│                                                                │
│ Handles actual 5G radio transmission                          │
└───────────────────────────────────────────────────────────────┘
```

---

## Detailed: How .so Files Implement AIDL in C++

### Step 1: AIDL Definition (Java/Kotlin Style)

```java
// File: IImsDataChannel.aidl
package com.newcalllib.datachannel.V1_0;

interface IImsDataChannel {
    void send(in byte[] data, int size, IDCSendDataCallback callback);
    void close();
    long bufferedAmount();
    String getDcLabel();
}
```

### Step 2: Android Generates C++ Header

When vendors build their ROM, Android SDK tools generate C++ bindings:

```cpp
// Auto-generated: IImsDataChannel.h
namespace com::newcalllib::datachannel::V1_0 {

class IImsDataChannel : public IInterface {
public:
    virtual Status send(
        const std::vector<uint8_t>& data,
        int32_t size,
        const sp<IDCSendDataCallback>& callback) = 0;

    virtual Status close() = 0;

    virtual Status bufferedAmount(int64_t* _aidl_return) = 0;

    virtual Status getDcLabel(std::string* _aidl_return) = 0;
};

}  // namespace
```

### Step 3: Vendor Implements in C++

```cpp
// Vendor implementation: ImsDataChannelImpl.cpp
#include "IImsDataChannel.h"
#include "ModemHardwareInterface.h"

namespace vendor::qti::hardware::radio::imsdc {

class ImsDataChannelImpl : public com::newcalllib::datachannel::V1_0::IImsDataChannel {
private:
    sp<IModemHardware> mModemHal;  // Hardware interface
    std::string mDcLabel;
    int32_t mStreamId;

public:
    ImsDataChannelImpl(const std::string& label, int32_t streamId)
        : mDcLabel(label), mStreamId(streamId) {
        // Initialize modem hardware interface
        mModemHal = IModemHardware::getService();
    }

    Status send(
        const std::vector<uint8_t>& data,
        int32_t size,
        const sp<IDCSendDataCallback>& callback) override {

        ALOGD("ImsDataChannelImpl::send - label=%s, size=%d",
              mDcLabel.c_str(), size);

        // 1. Validate parameters
        if (data.empty() || size <= 0) {
            callback->onSendDataResult(DC_SEND_DATA_FAILED);
            return Status::fromExceptionCode(Status::EX_ILLEGAL_ARGUMENT);
        }

        // 2. Convert data format for modem
        ModemDataPacket packet;
        packet.streamId = mStreamId;
        packet.data = data;
        packet.size = size;

        // 3. Send to modem hardware via HAL
        int result = mModemHal->transmitData(packet);

        // 4. Callback with result
        if (result == 0) {
            callback->onSendDataResult(DC_SEND_DATA_OK);
        } else {
            callback->onSendDataResult(DC_SEND_DATA_FAILED);
        }

        return Status::ok();
    }

    Status close() override {
        ALOGD("ImsDataChannelImpl::close - label=%s", mDcLabel.c_str());

        // Tell modem to close this data channel
        mModemHal->closeDataChannel(mStreamId);

        return Status::ok();
    }

    Status bufferedAmount(int64_t* _aidl_return) override {
        // Query modem for buffered data amount
        *_aidl_return = mModemHal->getBufferedAmount(mStreamId);
        return Status::ok();
    }

    Status getDcLabel(std::string* _aidl_return) override {
        *_aidl_return = mDcLabel;
        return Status::ok();
    }
};

}  // namespace
```

### Step 4: Vendor Registers Service

```cpp
// Vendor's service registration (usually in init.rc or service manager)
// File: vendor/qcom/proprietary/imsdc/service.cpp

#include <binder/IServiceManager.h>
#include "ImsDataChannelServiceController.h"

int main() {
    // Create service controller
    sp<ImsDataChannelServiceController> service =
        new ImsDataChannelServiceController();

    // Register with Android's service manager
    defaultServiceManager()->addService(
        String16("vendor.qti.hardware.radio.imsdc"),
        service
    );

    ProcessState::self()->startThreadPool();
    IPCThreadState::self()->joinThreadPool();

    return 0;
}
```

---

## What Vendor Must Implement

### Required by Vendor (in .so files)

✅ **IImsDataChannel interface**
- Methods: `send()`, `close()`, `bufferedAmount()`, `getDcLabel()`, `getStreamId()`, `getState()`
- Location: `/vendor/lib64/libimsdc.so` or similar

✅ **IImsDataChannelServiceController interface**
- Methods: `createApplicationDataChannel()`, `registerCallback()`, `unregisterCallback()`
- Location: System service or vendor HAL

✅ **Call SDK's callback interfaces**
- `IImsDataChannelCallback.onApplicationDataChannelResponse()`
- When DC state changes, vendor must call back into SDK

### NOT Required by Vendor

❌ **IMiniToParent** - Internal to SDK
❌ **IParentToMini** - Internal to SDK
❌ **IMessageCallback** - Internal to SDK
❌ **IDCCallback** - Internal to SDK

These are handled entirely within the SDK's own processes.

---

## Real Example: Qualcomm Implementation

On a Qualcomm device, you might find:

```
/vendor/lib64/
├── vendor.qti.hardware.radio@1.6.so          # Radio HAL
├── vendor.qti.hardware.radio.ims@1.0.so      # IMS HAL
├── libimscamera_jni.so                       # IMS camera
├── libimsmedia_jni.so                        # IMS media
└── libimsdc.so                               # IMS Data Channel (implements IImsDataChannel)
```

The SDK's `DCServiceManager.kt` binds to a service like:
```kotlin
val intent = Intent()
intent.component = ComponentName(
    "com.qualcomm.qti.telephony",
    "com.qualcomm.qti.telephony.ImsDataChannelService"
)
context.bindService(intent, ...)
```

This service is backed by the native .so files.

---

## Summary

| Component | Language | Implemented By | Purpose |
|-----------|----------|----------------|---------|
| `IMiniToParent` | AIDL → Kotlin | This SDK | SDK internal communication |
| `IParentToMini` | AIDL → Kotlin | This SDK | SDK internal communication |
| `IImsDataChannel` | AIDL → C++ | **VENDOR** | Talk to modem chip |
| `IImsDataChannelServiceController` | AIDL → C++ | **VENDOR** | Control DC service |
| `IImsDataChannelCallback` | AIDL → Kotlin | This SDK | Receive modem callbacks |

**Key Insight:** Only the `IImsDataChannel*` interfaces need vendor implementation in .so files. The rest (`IMiniToParent`, `IParentToMini`, etc.) are purely SDK-internal!

The .so files you found in `/vendor/lib64/` implement the **modem-related AIDL interfaces**, not the SDK-internal ones. 🎯
