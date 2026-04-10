# IMS-DC-SDK Complete Project Guide

> **5G New Calling Terminal SDK - Comprehensive Documentation**
> Developed by China Telecom Research Institute

---

## 📚 Table of Contents

1. [Quick Start](#quick-start)
2. [What is This Project?](#what-is-this-project)
3. [Key Concepts Explained](#key-concepts-explained)
4. [Architecture Overview](#architecture-overview)
5. [How It Works - End-to-End Flow](#how-it-works---end-to-end-flow)
6. [Data Channel Creation](#data-channel-creation)
7. [Call Lifecycle Management](#call-lifecycle-management)
8. [File Structure & Code Organization](#file-structure--code-organization)
9. [Detailed Function Flow](#detailed-function-flow)
10. [Development Guide](#development-guide)
11. [Additional Resources](#additional-resources)

---

## Quick Start

### For Non-Technical Users
**Read:** [Simplified Flow Summary](#simplified-flow-summary) (5 minutes)
**Understand:** What this SDK does and why it matters

### For Mini App Developers (HTML/JS)
**Read:** [Key Concepts](#key-concepts-explained) → [Data Channel Creation](#data-channel-creation)
**Time:** 30 minutes
**Output:** Understand how to build apps that work during phone calls

### For Android SDK Developers
**Read:** All sections in order
**Time:** 2-3 hours
**Output:** Complete understanding of SDK architecture and implementation

---

## What is This Project?

### The Big Picture

**5G New Calling** adds a **data channel** (IMS Data Channel) on top of traditional IMS audio and video channels, enabling interactive information exchange during phone calls by integrating AR, AI, and other technologies.

```
Traditional Phone Call:
📞 [Voice] -----> 👂 [Hear conversation]
📹 [Video] -----> 👀 [See each other]

5G New Calling:
📞 [Voice] -----> 👂 [Hear conversation]
📹 [Video] -----> 👀 [See each other]
📊 [Data]  -----> 💻 [Share menus, files, AR, games] ⭐ NEW!
```

### Real-World Example

**Scenario:** You're calling a restaurant to order food.

**Before (Traditional Call):**
- 📞 You: "What's on the menu?"
- 👨‍🍳 Restaurant: "We have pizza, pasta, salad..." *(describes everything verbally)*

**After (5G New Calling with IMS-DC-SDK):**
- 📞 You: "What's on the menu?"
- 👨‍🍳 Restaurant: "Let me send you the menu!" *(sends interactive menu via data channel)*
- 📱 Your screen shows: Full menu with pictures, prices, and "Order" buttons
- You tap items to order while still talking on the phone!

### What This SDK Does

The **IMS-DC-SDK** (5G New Calling Terminal SDK):

1. **Provides Runtime Environment** for mini apps (HTML5/JavaScript applications)
2. **Manages Data Channel Lifecycle** tied to phone call state
3. **Bridges Communication** between mini apps and 5G hardware (modem)
4. **Ensures Security** with isolated storage and permission management
5. **Supports Multiple Apps** running simultaneously during a call

---

## Key Concepts Explained

### Visual Guide to 5G Concepts

![5G Concepts Explained](./5G%20Concepts%20Explained.svg)

### Essential Terminology

| Term | Simple Explanation | Technical Definition |
|------|-------------------|---------------------|
| **Terminal** | Your Android smartphone | The physical device with Android OS + 5G modem chip |
| **IMS Network** | Modern phone call system over internet | IP Multimedia Subsystem - handles VoLTE/5G calls using packet-switched network |
| **Modem** | 5G chip inside your phone | Hardware component that handles cellular communication (Qualcomm, MediaTek, etc.) |
| **Data Channel** | Extra data pipe during calls | IMS-based channel for transmitting application data alongside voice/video |
| **AIDL** | Bridge between Android and modem | Android Interface Definition Language - enables inter-process communication |
| **Mini App** | HTML5 app running during calls | Web-based application (HTML/CSS/JS) managed by the SDK |
| **TS.71** | Industry standard for data channels | Technical specification defining AIDL interfaces between SDK and terminal |
| **WebRTC** | Web real-time communication | Framework used for establishing peer-to-peer data channels |

### The Highway Analogy

Think of a phone call as a highway system:

```
🛣️ Voice Lane    - Carries your spoken words
🛣️ Video Lane    - Carries video stream (if video call)
🛣️ Data Lane     - Carries app data (menus, files, games) ⭐ NEW!
```

All lanes run simultaneously on the same "5G highway" (IMS network).

---

## Architecture Overview

### System Architecture Diagram

![IMS-DC-SDK Architecture Overview](./IMS-DC-SDK%20Architecture%20Overview.svg)

### Key Components

#### 1️⃣ **InCallService Layer**
- **What:** Inherits Android's `InCallService` to monitor call state
- **Why:** Ensures SDK only runs during active calls
- **Location:** `core/src/main/java/com/ct/ertclib/dc/core/service/InCallServiceImpl.kt`

#### 2️⃣ **TS.71 AIDL Interfaces**
- **What:** Standard interfaces between SDK and terminal hardware
- **Why:** Enables SDK to control data channels without knowing hardware details
- **Location:** `core/src/main/aidl/com/newcalllib/datachannel/V1_0`
- **Functions:**
  - Get mini app list from network
  - Download mini app packages
  - Create/destroy data channels
  - Send/receive data

#### 3️⃣ **Extended Capability Interfaces (IEC)**
- **What:** Plugin system for operator/manufacturer-specific features
- **Why:** Allows China Telecom (or other operators) to add unique features while maintaining unified SDK
- **Example:** Custom AR features, special payment APIs, operator-specific services
- **Location:**
  - Interface: `base/src/main/java/com/ct/ertclib/dc/base/port/ec/IEC.kt`
  - Manager: `core/src/main/java/com/ct/ertclib/dc/core/manager/common/ExpandingCapacityManager.kt`
  - Sample: `oemec/src/main/java/com/ct/oemec/OemEC.kt`

#### 4️⃣ **JS API Layer (DSBridge)**
- **What:** JavaScript interfaces exposed to mini apps
- **Why:** Mini app developers call simple JS functions instead of complex Android/AIDL code
- **Framework:** [DSBridge](https://github.com/wendux/DSBridge-Android)
- **Location:** `core/src/main/java/com/ct/ertclib/dc/core/miniapp/bridge/JSApi.kt`
- **Example APIs:**
  ```javascript
  // Mini app code
  await bridge.createDataChannel({ channelId: 'menu-channel' })
  await bridge.sendData({ data: menuSelection })
  ```

### Architecture Layers

```
┌─────────────────────────────────────────────────────┐
│  Mini Apps (HTML5/JavaScript)                       │
│  - Restaurant menu, AR viewer, File share, etc.     │
└──────────────────┬──────────────────────────────────┘
                   │ JS API (DSBridge)
┌──────────────────▼──────────────────────────────────┐
│  SDK Core (Kotlin/Java)                             │
│  ├─ InCallService (Call monitoring)                 │
│  ├─ DCManager (Data channel management)             │
│  ├─ MiniAppManager (App lifecycle)                  │
│  ├─ ExpandingCapacityManager (Extensions)           │
│  └─ Security & Permission Manager                   │
└──────────────────┬──────────────────────────────────┘
                   │ AIDL (TS.71 Interfaces)
┌──────────────────▼──────────────────────────────────┐
│  Terminal/Hardware Layer                            │
│  ├─ Android Telephony Framework                     │
│  └─ 5G Modem (Qualcomm/MediaTek)                    │
└──────────────────┬──────────────────────────────────┘
                   │ 5G Protocol
┌──────────────────▼──────────────────────────────────┐
│  IMS Network (China Telecom 5G Infrastructure)      │
└─────────────────────────────────────────────────────┘
```

---

## How It Works - End-to-End Flow

### Complete System Flow

![IMS-DC-SDK End-to-End Flow](./IMS-DC-SDK%20End-to-End%20Flow.svg)

### Simplified Flow Summary

![Simplified IMS-DC-SDK Flow Summary](./Simplified%20IMS-DC-SDK%20Flow%20Summary.svg)

### Step-by-Step Process

#### Phase 1: Call Establishment (Steps 1-3)
```
1. 📞 User makes/receives call
   → InCallService detects call active
   → SDK initializes

2. 🔍 SDK queries available mini apps
   → AIDL call to terminal
   → Terminal queries IMS network
   → Returns app list (e.g., "Restaurant Menu", "File Share")

3. 📥 SDK downloads mini app packages
   → AIDL downloads .zip from network
   → Verifies signature & permissions
   → Extracts to isolated storage
```

#### Phase 2: Mini App Launch (Steps 4-6)
```
4. 👆 User opens mini app from list
   → SDK creates isolated WebView process
   → Loads index.html from package
   → Injects JSBridge for API access

5. 🔗 Mini app requests data channel
   → JavaScript calls: bridge.createDataChannel()
   → SDK validates permissions
   → Prepares channel parameters

6. 📡 SDK requests channel from modem
   → AIDL call to terminal
   → Modem negotiates with network
   → WebRTC signaling establishes peer connection
```

#### Phase 3: Data Exchange (Steps 7-9)
```
7. ✅ Channel creation success
   → Callback to SDK with channel ID
   → SDK notifies mini app via JS callback
   → Mini app UI updates to "Connected"

8. 📤 Mini app sends data
   → JavaScript: bridge.sendData({ type: 'menu-order', item: 'pizza' })
   → SDK marshals data to AIDL
   → Modem sends via IMS data channel
   → Remote party receives

9. 📥 Receiving data
   → Modem receives IMS data
   → AIDL callback to SDK
   → SDK dispatches to correct mini app
   → JavaScript callback fires with data
```

#### Phase 4: Call End (Steps 10-11)
```
10. 📞 Call ends
    → InCallService detects disconnect
    → SDK triggers cleanup

11. 🧹 Resource cleanup
    → Close all data channels (AIDL)
    → Terminate mini app processes
    → Clear temporary files
    → SDK enters idle state
```

---

## Data Channel Creation

### Data Channel Creation Sequence

![Data Channel Creation Sequence](./Data%20Channel%20Creation%20Sequence.svg)

### Detailed Creation Phases

#### Phase 1-4: Request Initiation
```
Mini App (JS)
  ↓ createDataChannel({ channelId: 'app-channel' })
JSApi.async()
  ↓ Validate parameters
DCJsEventDispatcher
  ↓ Route to correct handler
DCMiniUseCase
  ↓ Business logic validation
```

#### Phase 5-8: Channel Negotiation
```
DCManager
  ↓ createApplicationDataChannels(params)
AIDL Interface
  ↓ IImsDataChannel.createAppDataChannel()
Terminal Layer
  ↓ Modem negotiation with network
IMS Network
  ↓ Establishes WebRTC connection
```

#### Phase 9-13: Success Callback
```
IMS Network
  ↓ Channel ready, returns channel ID
Terminal Layer
  ↓ AIDL callback
DCManager
  ↓ onApplicationDataChannelResponse()
DCMiniUseCase
  ↓ Update internal state
DCJsEventDispatcher
  ↓ Prepare JS response
Mini App (JS)
  ↓ Success callback fires
```

### Key Parameters

```kotlin
// Channel creation request
data class CreateChannelParams(
    val channelId: String,           // Unique identifier
    val label: String,                // Human-readable name
    val ordered: Boolean = true,      // Guarantee message order
    val maxRetransmits: Int? = null,  // Reliability setting
    val protocol: String = "sctp"     // Transport protocol
)
```

---

## Call Lifecycle Management

### Call Lifecycle Flow

![Call Lifecycle Flow](./Call%20Lifecycle%20Flow.svg)

### Lifecycle States

#### 1️⃣ **IDLE** (No Active Call)
```
State: SDK not running
Resources: None allocated
Trigger: No calls active
```

#### 2️⃣ **INITIALIZING** (Call Detected)
```
State: Call detected, SDK starting
Actions:
  - InCallService.onCallAdded() fires
  - Initialize managers (DCManager, MiniAppManager, etc.)
  - Set up AIDL connections
  - Prepare isolated storage directories
Transition: → READY
```

#### 3️⃣ **READY** (Available for Mini Apps)
```
State: SDK running, waiting for user interaction
Actions:
  - Display mini app launcher (floating ball or dialer integration)
  - Listen for user input
  - Monitor call state changes
Resources: Managers active, AIDL connected
```

#### 4️⃣ **MINI_APP_RUNNING** (App Active)
```
State: One or more mini apps running
Actions:
  - WebView processes active
  - Data channels may be open
  - Processing JS API calls
  - Handling data transmission
Resources: WebView, data channels, event loops
```

#### 5️⃣ **CLEANUP** (Call Ending)
```
State: Call disconnected, cleaning up
Actions:
  - InCallService.onCallRemoved() fires
  - Close all data channels
  - Terminate mini app processes
  - Clear cache (if not configured to persist)
  - Release AIDL connections
  - Dispose managers
Transition: → IDLE
```

### State Transition Diagram

```
        ┌─────────────┐
        │    IDLE     │
        └──────┬──────┘
               │ Call Detected
        ┌──────▼──────┐
        │ INITIALIZING│
        └──────┬──────┘
               │ Managers Ready
        ┌──────▼──────┐
        │    READY    │◄────┐
        └──────┬──────┘     │
               │ User Opens Mini App
        ┌──────▼──────┐     │
        │  MINI_APP   │     │ App Closed
        │   RUNNING   │─────┘ (Call Active)
        └──────┬──────┘
               │ Call Ends
        ┌──────▼──────┐
        │   CLEANUP   │
        └──────┬──────┘
               │
        ┌──────▼──────┐
        │    IDLE     │
        └─────────────┘
```

---

## File Structure & Code Organization

### File Structure Overview

![File Structure and Flow](./File%20Structure%20and%20Flow.svg)

### Project Structure

```
IMS-DC-SDK-Open-Source/
│
├── app/                          # Mini app list display UI
│   └── MainActivity.kt           # Entry point for app launcher
│
├── base/                         # Core interfaces & data structures
│   ├── data/                     # Common data models
│   └── port/                     # Interface definitions
│       ├── ec/IEC.kt            # Extended capability interface
│       └── ...
│
├── build-logic/                  # Gradle build configuration
│
├── core/                         # 🔥 Main SDK logic
│   ├── aidl/                     # AIDL interface definitions
│   │   └── com/newcalllib/datachannel/V1_0/
│   │       ├── IImsDataChannel.aidl
│   │       ├── IImsDataChannelCallback.aidl
│   │       └── ...
│   │
│   └── core/
│       ├── common/               # Utilities
│       ├── constants/            # Configuration constants
│       ├── data/                 # Data models
│       ├── dispatcher/           # Event routing system
│       │   ├── DCJsEventDispatcher.kt  # Routes JS API calls
│       │   └── MiniServiceEventDispatcher.kt
│       │
│       ├── factory/              # Factory pattern implementations
│       │
│       ├── manager/              # Core business logic
│       │   ├── call/
│       │   │   └── DCManager.kt              # Data channel operations
│       │   ├── common/
│       │   │   └── ExpandingCapacityManager.kt  # Extension manager
│       │   └── miniapp/
│       │       └── MiniAppManager.kt         # Mini app lifecycle
│       │
│       ├── miniapp/              # Mini app runtime
│       │   ├── bridge/
│       │   │   └── JSApi.kt     # 🌟 JavaScript API exposure
│       │   ├── ui/
│       │   │   └── MiniAppWebView.kt  # WebView container
│       │   └── ...
│       │
│       ├── service/              # Android services
│       │   └── InCallServiceImpl.kt  # 🌟 Call state monitor
│       │
│       ├── usecase/              # Business logic handlers
│       │   ├── miniapp/
│       │   │   └── DCMiniUseCase.kt  # 🌟 Data channel use cases
│       │   └── ...
│       │
│       └── utils/                # Helper functions
│
├── libs/                         # Third-party libraries
│
├── miniapp/                      # Mini app development tools
│   ├── webrtcDC/                 # TS.66 WebRTC implementation
│   │   └── webrtcDC.js          # JavaScript library for mini apps
│   │
│   └── demo/                     # Example mini apps
│       └── IMS_DC_Mini_app_demo_source_code/
│           ├── index.html       # Mini app entry point
│           ├── properties.json  # App metadata
│           └── src/             # Vue.js source code
│
├── oemec/                        # OEM extended capabilities example
│   └── OemEC.kt                 # Sample EC implementation
│
├── script/                       # Build scripts
│
├── testing/                      # Local simulation tools
│
└── document/                     # 📚 Documentation
    ├── PROJECT_GUIDE.md         # This file
    ├── START-HERE.md            # Quick start guide
    ├── *.svg                    # Architecture diagrams
    └── *.docx                   # API specifications
```

### Key Files for Developers

#### Core Entry Points
| File | Purpose | Line of Interest |
|------|---------|-----------------|
| `InCallServiceImpl.kt` | Call state monitoring | `onCallAdded()`, `onCallRemoved()` |
| `JSApi.kt` | JS API definitions | All `@JavascriptInterface` methods |
| `DCManager.kt` | Data channel operations | `createApplicationDataChannels()` at line 328 |
| `MiniAppManager.kt` | Mini app lifecycle | `launchMiniApp()`, `closeMiniApp()` |

#### AIDL Interfaces
| File | Purpose |
|------|---------|
| `IImsDataChannel.aidl` | Terminal interface for DC operations |
| `IImsDataChannelCallback.aidl` | Callbacks from terminal to SDK |

#### Use Cases (Business Logic)
| File | Responsibility |
|------|---------------|
| `DCMiniUseCase.kt` | Handles data channel requests from mini apps |
| `MiniServiceEventUseCase.kt` | Processes mini app lifecycle events |

---

## Detailed Function Flow

### Function-Level Data Channel Creation

![Detailed Functions Flow](./Detailed%20Functions%20Flow%20-%20Data%20Channel%20Creation.svg)

### Code Flow with Line Numbers

#### Step 1-3: JavaScript Call → SDK Entry
```javascript
// Mini app code
bridge.async("createDataChannel", { channelId: "my-channel" }, function(response) {
    console.log("Channel created:", response)
})
```

```kotlin
// JSApi.kt:48
@JavascriptInterface
fun async(msg: String, handler: CompletionHandler<String>) {
    // Parse JSON message
    val request = JSONObject(msg)

    // Route to dispatcher
    dispatcher.dispatchAsyncMessage(request, handler)
}
```

#### Step 4-6: Event Dispatching
```kotlin
// DCJsEventDispatcher.kt:36
fun dispatchAsyncMessage(request: JSONObject, handler: CompletionHandler<String>) {
    when (request.getString("method")) {
        "createDataChannel" -> {
            val params = request.getJSONObject("params")
            dcMiniUseCase.createAppDataChannel(params, handler)
        }
        // ... other methods
    }
}
```

#### Step 7-9: Use Case Logic
```kotlin
// DCMiniUseCase.kt:52
suspend fun createAppDataChannel(
    params: JSONObject,
    handler: CompletionHandler<String>
) {
    // 1. Validate parameters
    val channelId = params.getString("channelId")
    require(channelId.isNotEmpty()) { "Channel ID required" }

    // 2. Check permissions
    if (!permissionManager.hasDataChannelPermission(currentMiniApp)) {
        handler.complete(createErrorResponse("Permission denied"))
        return
    }

    // 3. Forward to manager
    dcManager.createApplicationDataChannels(
        channelId = channelId,
        label = params.optString("label", channelId),
        ordered = params.optBoolean("ordered", true),
        callback = { result ->
            handler.complete(result.toJSON())
        }
    )
}
```

#### Step 10-12: Manager → AIDL Call
```kotlin
// DCManager.kt:328
fun createApplicationDataChannels(
    channelId: String,
    label: String,
    ordered: Boolean,
    callback: (ChannelResult) -> Unit
) {
    // Store callback for later
    pendingCallbacks[channelId] = callback

    // Prepare AIDL parameters
    val params = Bundle().apply {
        putString("channel_id", channelId)
        putString("label", label)
        putBoolean("ordered", ordered)
    }

    // Call terminal via AIDL
    try {
        imsDataChannelService?.createAppDataChannel(params, callbackBinder)
    } catch (e: RemoteException) {
        callback(ChannelResult.Error("AIDL call failed"))
        pendingCallbacks.remove(channelId)
    }
}
```

#### Step 13: AIDL Callback Processing
```kotlin
// DCManager.kt:217 (callback handler)
private val callbackBinder = object : IImsDataChannelCallback.Stub() {
    override fun onApplicationDataChannelResponse(
        channelId: String,
        result: Int,
        message: String
    ) {
        // Retrieve stored callback
        val callback = pendingCallbacks.remove(channelId)

        // Process result
        when (result) {
            RESULT_SUCCESS -> {
                callback?.invoke(ChannelResult.Success(channelId))
            }
            else -> {
                callback?.invoke(ChannelResult.Error(message))
            }
        }
    }
}
```

### Key Function Reference

| Component | File | Function | Line | Purpose |
|-----------|------|----------|------|---------|
| JS Entry | JSApi.kt | `async()` | 48 | Receives JS API calls |
| Dispatcher | DCJsEventDispatcher.kt | `dispatchAsyncMessage()` | 36 | Routes to correct handler |
| Use Case | DCMiniUseCase.kt | `createAppDataChannel()` | 52 | Business logic & validation |
| Manager | DCManager.kt | `createApplicationDataChannels()` | 328 | AIDL communication |
| Callback | DCManager.kt | `onApplicationDataChannelResponse()` | 217 | Handles AIDL response |

---

## Development Guide

### SDK Development

#### Prerequisites
- **JDK:** Version 17
- **Gradle:** Version 8.1
- **Android SDK:** compileSdk 34, minSdk 26
- **IDE:** Android Studio (recommended)

#### Building the SDK

```bash
# Clone repository
git clone <repository-url>
cd IMS-DC-SDK-Open-Source

# Build all variants
./gradlew assembleRelease

# Output APKs:
# - app/build/outputs/apk/normal/release/    # Floating ball entry
# - app/build/outputs/apk/dialer/release/    # Dialer integration
# - app/build/outputs/apk/local/release/     # Local debugging
```

#### SDK Variants

| Variant | Entry Point | Use Case |
|---------|------------|----------|
| **Normal** | Floating ball during call | General distribution |
| **Dialer** | Native dialer button | OEM integration |
| **Local** | Home screen app icon | Development & testing |

### Mini App Development

#### 1. Set Up Development Environment

```bash
# Use provided demo as template
cd miniapp/demo/IMS_DC_Mini_app_demo_source_code

# Install dependencies
npm install

# Start development server
npm run dev
```

#### 2. Required Files

Every mini app package must contain:

##### `index.html` (Entry Point)
```html
<!DOCTYPE html>
<html>
<head>
    <title>My Mini App</title>
    <script src="webrtcDC.js"></script>
</head>
<body>
    <div id="app"></div>
    <script src="main.js"></script>
</body>
</html>
```

##### `properties.json` (Metadata)
```json
{
    "app_id": "com.example.myapp",
    "app_name": "My Mini App",
    "version": "1.0.0",
    "description": "Example mini app",
    "permissions": [
        "DATA_CHANNEL",
        "FILE_ACCESS"
    ],
    "icon": "icon.png",
    "entry": "index.html"
}
```

#### 3. Using JS APIs

```javascript
// Initialize bridge
const bridge = window.dsBridge

// Create data channel
bridge.async('createDataChannel', {
    channelId: 'my-channel',
    label: 'My Channel',
    ordered: true
}, function(response) {
    if (response.success) {
        console.log('Channel created:', response.channelId)
    }
})

// Send data
bridge.async('sendData', {
    channelId: 'my-channel',
    data: JSON.stringify({ type: 'message', content: 'Hello!' })
}, function(response) {
    console.log('Data sent')
})

// Receive data (callback)
window.onDataReceived = function(channelId, data) {
    console.log('Received on', channelId, ':', data)
}
```

#### 4. Package Mini App

```bash
# Build production version
npm run build

# Package structure:
# dist/
# ├── index.html
# ├── properties.json
# ├── assets/
# │   ├── js/
# │   ├── css/
# │   └── images/
# └── icon.png

# Create zip package
cd dist
zip -r ../MyMiniApp.zip *
```

#### 5. Local Testing

```bash
# Install Local variant SDK to device
adb install app-local-release.apk

# Push mini app package to device
adb push MyMiniApp.zip /sdcard/

# Launch SDK
adb shell am start -n com.ct.ertclib.dc/.MainActivity

# Configure in app:
# Settings → Local Debugging → Load Package → /sdcard/MyMiniApp.zip
```

### Terminal Adaptation

Terminal manufacturers must implement AIDL interfaces according to the **5G New Calling SDK Terminal Adaptation Specification** (see `document/` folder).

#### Key Adaptation Points

1. **Implement TS.71 AIDL Interfaces**
   - `IImsDataChannel.aidl` - Data channel operations
   - Vendor must provide implementation in system service

2. **Integrate with Modem/RIL**
   - Connect AIDL calls to modem's IMS DC capabilities
   - Handle WebRTC signaling with network

3. **System Integration**
   - Register SDK as default InCallService
   - Provide dialer integration points (for Dialer variant)

4. **Testing & Certification**
   - Verify all AIDL methods work correctly
   - Test with reference mini apps
   - Performance benchmarking

---

## Additional Resources

### Documentation Files

| Document | Description | Location |
|----------|-------------|----------|
| **START-HERE.md** | Quick start guide for beginners | `document/START-HERE.md` |
| **DIAGRAMS_README.md** | How to view/edit PlantUML diagrams | `document/DIAGRAMS_README.md` |
| **aidl-vendor-implementation.md** | Guide for terminal vendors | `document/aidl-vendor-implementation.md` |
| **JS API Specification** | Complete API reference (Word doc) | `document/5G New Calling IMS Data Channel JS API.docx` |
| **Terminal Adaptation Spec** | OEM integration guide (Word doc) | `document/5G New Calling SDK Terminal Adaptation Specification.docx` |

### Code Examples

| Example | Description | Location |
|---------|-------------|----------|
| **Demo Mini App** | Vue.js example app with all features | `miniapp/demo/IMS_DC_Mini_app_demo_source_code/` |
| **WebRTC DC Library** | TS.66 compliant JavaScript library | `miniapp/webrtcDC/` |
| **OEM Extension** | Sample extended capability implementation | `oemec/src/main/java/com/ct/oemec/OemEC.kt` |

### Architecture Diagrams (SVG)

All diagrams are available in the `document/` folder as `.svg` files for easy viewing in browsers or documentation.

| Diagram | File |
|---------|------|
| 5G Concepts Explained | `5G Concepts Explained.svg` |
| Architecture Overview | `IMS-DC-SDK Architecture Overview.svg` |
| End-to-End Flow | `IMS-DC-SDK End-to-End Flow.svg` |
| Simplified Flow Summary | `Simplified IMS-DC-SDK Flow Summary.svg` |
| Data Channel Creation | `Data Channel Creation Sequence.svg` |
| Call Lifecycle | `Call Lifecycle Flow.svg` |
| File Structure | `File Structure and Flow.svg` |
| Detailed Functions | `Detailed Functions Flow - Data Channel Creation.svg` |

### PlantUML Source Files

All diagrams can be regenerated from `.puml` source files using:
- Online: http://www.plantuml.com/plantuml/uml/
- VS Code: PlantUML extension
- IntelliJ/Android Studio: PlantUML integration plugin

---

## Standards & Specifications

### Implemented Standards

- **3GPP TS 26.114** - IMS Multimedia Telephony
- **GSMA TS.66** - WebRTC-based Data Channel API
- **GSMA TS.71** - AIDL Interface for IMS Data Channel
- **WebRTC** - Peer-to-peer data channels

### Compliance

This SDK is designed to comply with international telecommunications standards while supporting China Telecom's specific requirements for 5G New Calling services.

---

## FAQ

### For Business/Product

**Q: What's the business value of this SDK?**
A: Enables new revenue streams through in-call services: food ordering, customer service enhancements, AR experiences, real-time document sharing, etc.

**Q: Who are the target users?**
A: Telecom operators, device manufacturers (OEMs), and mini app developers building services for 5G networks.

**Q: What devices support this?**
A: Android devices with 5G modems that implement the TS.71 AIDL interfaces (requires OEM adaptation).

### For Developers

**Q: What technologies do I need to know?**
**A:**
- **For SDK development:** Kotlin, Android, AIDL, Jetpack Compose
- **For mini app development:** HTML5, CSS3, JavaScript (ES6+), WebRTC basics

**Q: Can I test without a 5G network?**
A: Yes! Use the "Local" variant which simulates the network environment for offline testing.

**Q: How do I debug mini apps?**
A: Use Chrome DevTools with WebView debugging:
```bash
# Enable WebView debugging in SDK
adb shell setprop debug.webview.devtools true

# Open chrome://inspect in Chrome browser
# Your mini app will appear in the list
```

**Q: What's the difference between TS.66 and TS.71?**
A:
- **TS.66** - JavaScript API standard for mini apps (WebRTC-based)
- **TS.71** - AIDL interface standard between SDK and terminal

### For Terminal Manufacturers

**Q: What adaptation work is required?**
A: Implement AIDL interfaces, integrate with modem/RIL, system service registration. See Terminal Adaptation Specification.

**Q: How long does adaptation take?**
A: Typically 2-4 weeks for experienced Android platform teams, including testing.

**Q: Is certification required?**
A: Yes, China Telecom provides certification testing for adapted terminals.

---

## Contact & Support

### Development Team
- **Email:** xuq17@chinatelecom.cn, pengc23@chinatelecom.cn
- **Organization:** China Telecom Research Institute

### Contributing
This is an open-source project under the Apache 2.0 License. Contributions are welcome!

### Issue Reporting
For bugs or feature requests, please contact the development team via email.

---

## License

```
Copyright (C) China Telecom Research Institute

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

---

## Appendix: Quick Reference

### Common JS API Methods

| Method | Parameters | Purpose |
|--------|-----------|---------|
| `createDataChannel` | `{ channelId, label, ordered }` | Create new data channel |
| `sendData` | `{ channelId, data }` | Send data on channel |
| `closeDataChannel` | `{ channelId }` | Close data channel |
| `getChannelList` | `{}` | Get all open channels |
| `requestPermission` | `{ permission }` | Request runtime permission |

### AIDL Interface Methods

| Method | Purpose | Callback |
|--------|---------|----------|
| `createAppDataChannel()` | Create channel | `onApplicationDataChannelResponse()` |
| `sendAppData()` | Send data | `onSendDataResponse()` |
| `closeAppDataChannel()` | Close channel | `onApplicationDataChannelClosed()` |
| `receiveAppData()` | N/A (callback only) | `onReceiveData()` |

### File Path Quick Reference

```
📂 Key SDK Files
├── InCallServiceImpl.kt         → core/src/main/java/com/ct/ertclib/dc/core/service/
├── JSApi.kt                      → core/src/main/java/com/ct/ertclib/dc/core/miniapp/bridge/
├── DCManager.kt                  → core/src/main/java/com/ct/ertclib/dc/core/manager/call/
├── DCMiniUseCase.kt             → core/src/main/java/com/ct/ertclib/dc/core/usecase/miniapp/
└── IImsDataChannel.aidl         → core/src/main/aidl/com/newcalllib/datachannel/V1_0/

📂 Mini App Development
├── Demo source code              → miniapp/demo/IMS_DC_Mini_app_demo_source_code/
├── WebRTC DC library             → miniapp/webrtcDC/
└── Example packages              → miniapp/demo/*.zip

📂 Documentation
├── This guide                    → document/PROJECT_GUIDE.md
├── Quick start                   → document/START-HERE.md
├── SVG diagrams                  → document/*.svg
└── PlantUML sources              → document/*.puml
```

---

**Last Updated:** 2026-02-13
**Document Version:** 1.0
**SDK Version:** See README.md

