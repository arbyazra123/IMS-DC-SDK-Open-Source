# IMS-DC-SDK PlantUML Diagrams

This directory contains comprehensive PlantUML diagrams documenting the end-to-end flow and architecture of the 5G New Calling Terminal SDK (IMS Data Channel SDK).

## Overview

The **IMS-DC-SDK** is a runtime environment for 5G New Calling Applications developed by China Telecom Research Institute. It adds interactive data channels on top of traditional IMS audio/video calls, enabling mini applications to run during calls and exchange data beyond voice/video.

## Diagrams

### 🌟 START HERE - Simplified Diagrams

#### 1. Simplified Flow Summary (`simplified-flow-summary.puml`) ⭐ **RECOMMENDED FIRST**

**Purpose:** Easy-to-understand diagram showing how mini apps, SDK, and 5G network work together.

**Key Elements:**
- 📱 Mini App Layer (what users see)
- 🔗 SDK Bridge (connects app to phone hardware)
- 📡 Terminal Layer (your phone's 5G chip)
- ☁️ 5G Network (cell towers and IMS infrastructure)
- Step-by-step numbered flow (1-13 steps)
- Real-world example (restaurant menu ordering)

**Best for:** Understanding the big picture without technical jargon.

#### 2. 5G Concepts Explained (`5g-concepts-explained.puml`) ⭐ **READ SECOND**

**Purpose:** Explains confusing 5G/networking terms using simple analogies.

**Key Concepts Explained:**
- What is a "Terminal"? (Your phone's hardware)
- What is "IMS"? (Modern phone call system over internet)
- What is a "Data Channel"? (Extra data pipe during calls)
- What is a "Modem"? (5G radio chip in your phone)
- What is "AIDL"? (Bridge between Android and modem)
- Highway analogy (old calls vs. new 5G calls)

**Best for:** Understanding the 5G networking parts you were confused about.

### 📊 Detailed Technical Diagrams

#### 3. Detailed Functions Flow (`detailed-functions-flow.puml`) ⭐ **NEW - WITH ACTUAL CODE!**

**Purpose:** Complete vertical flow with actual function names, file paths, and line numbers from the codebase.

**Key Elements:**
- 13 numbered steps from user click to DC ready
- Real function signatures from actual code
- Exact file paths for every component
- Line numbers for key functions
- Detailed parameter information
- Code snippets and validation logic
- Vertical layout (easy to follow top-to-bottom)

**Example Functions Shown:**
- `JSApi.async()` (JSApi.kt:48)
- `DCJsEventDispatcher.dispatchAsyncMessage()` (DCJsEventDispatcher.kt:36)
- `DCMiniUseCase.createAppDataChannel()` (DCMiniUseCase.kt:52)
- `DCManager.createApplicationDataChannels()` (DCManager.kt:328)
- `IImsDataChannelCallback.onApplicationDataChannelResponse()` (Line 217)

**Best for:** Developers who need to understand the exact code flow with real function names.

#### 4. File Structure Flow (`file-structure-flow.puml`) ⭐ **NEW - FILE REFERENCE!**

**Purpose:** Shows all key files involved in the data channel creation flow with their paths and responsibilities.

**Key Elements:**
- Complete file paths for every component
- Key functions in each file with line numbers
- Data structures and their purposes
- AIDL interface definitions
- File-to-file relationships
- Layer-by-layer organization

**Files Documented:**
- Mini App: `main.vue`
- Bridge: `JSApi.kt`
- Dispatcher: `DCJsEventDispatcher.kt`, `JsEventDispatcherFactory.kt`
- Use Case: `DCMiniUseCase.kt`
- Manager: `MiniAppManager.kt`, `DCManager.kt`
- Service: `InCallServiceImpl.kt`, `DCServiceManager.kt`
- AIDL: `IImsDataChannel.aidl`, `IImsDataChannelCallback.aidl`

**Best for:** Understanding which files to look at and where to find specific functionality.

#### 5. Architecture Overview (`architecture-overview.puml`)

**Purpose:** High-level view of the entire SDK architecture showing all layers and components.

**Key Elements:**
- Application Layer (Mini App Launcher, Mini Applications)
- SDK Core Layer (Services, Managers, Container, JavaScript API)
- Base Module (Data structures, Port interfaces)
- OEM Extended Capabilities
- Android Framework integration
- Terminal/Modem layer
- Network layer

**Best for:** Understanding the overall system structure and how components are organized.

#### 6. End-to-End Flow (`end-to-end-flow.puml`)

**Purpose:** Component-level diagram showing the complete flow from user interaction to network communication.

**Key Flows:**
- Call lifecycle management
- Mini app loading and execution
- Data channel creation and transmission
- Extended capabilities integration
- Call termination and cleanup

**Best for:** Understanding how data flows through the system and component interactions.

#### 7. Data Channel Creation Sequence (`data-channel-creation-sequence.puml`)

**Purpose:** Detailed step-by-step sequence diagram for creating an IMS data channel.

**Phases:**
1. User Interaction (Button click in mini app)
2. JavaScript to Native Bridge (DSBridge)
3. Event Routing (Dispatcher Factory)
4. Validation & Processing (UseCase)
5. DC Manager Processing
6. Queue Processing (Background coroutine)
7. AIDL Call to Terminal
8. Modem Processing & Network Negotiation
9. Callback from Modem
10. DC Caching & Routing
11. Mini App Notification
12. Response to JavaScript
13. DC Ready for Use

**Best for:** Understanding the complete data channel creation process in detail.

#### 8. Call Lifecycle Flow (`call-lifecycle-flow.puml`)

**Purpose:** Activity diagram showing the complete lifecycle of a call from start to end.

**Key Stages:**
- Service Binding (InCallServiceImpl.onBind)
- Manager Initialization (First call only)
- Call Added Event Processing
- Mini App Loading (Auto-load and user-initiated)
- Active Call Operations (DC creation, data transmission)
- Call Removed Event Processing
- Resource Cleanup
- Service Unbinding

**Best for:** Understanding how the SDK manages resources throughout a call's lifetime.

## How to View These Diagrams

### Option 1: Online PlantUML Editor
1. Visit [PlantUML Online Editor](http://www.plantuml.com/plantuml/uml/)
2. Copy the content of any `.puml` file
3. Paste it into the editor
4. View the rendered diagram

### Option 2: Visual Studio Code
1. Install the "PlantUML" extension by jebbs
2. Open any `.puml` file
3. Press `Alt+D` (Windows/Linux) or `Option+D` (Mac) to preview
4. Or right-click and select "Preview Current Diagram"

### Option 3: IntelliJ IDEA / Android Studio
1. Install the "PlantUML integration" plugin
2. Open any `.puml` file
3. The diagram will render in the editor

### Option 4: Command Line (Generate Images)
```bash
# Install PlantUML
# On macOS:
brew install plantuml

# On Linux:
sudo apt-get install plantuml

# Generate PNG images
plantuml architecture-overview.puml
plantuml end-to-end-flow.puml
plantuml data-channel-creation-sequence.puml
plantuml call-lifecycle-flow.puml

# Generate SVG (scalable)
plantuml -tsvg *.puml
```

## Quick Reference: 5G/Networking Terms Simplified

| Confusing Term | Simple Explanation | Think of It As... |
|----------------|-------------------|------------------|
| **Terminal** | Your phone's hardware (Android + 5G chip) | The entire device |
| **Modem/Chipset** | The 5G radio chip inside your phone | Wi-Fi chip, but for 5G |
| **IMS Network** | Modern phone call system using internet | Zoom, but built into 5G by telecom companies |
| **Data Channel** | Extra data pipe during a call | Third lane on a highway (voice, video, **data**) |
| **AIDL** | Android's way to talk to modem chip | USB cable between app and hardware |
| **ADC** | Application Data Channel | Your mini app's data pipe |
| **BDC** | Bootstrap Data Channel | Control channel for managing apps |
| **3GPP TS.71** | International standard for data channels | Recipe book that all phone makers follow |
| **WebRTC** | Technology for real-time communication | Same tech as Google Meet/Zoom |

## Key Concepts

### Data Channel (DC)
- **BDC (Bootstrap Data Channel):** Initial control channel for app management
- **ADC (Application Data Channel):** User data channel for specific apps
- **Format:** `local_[appId]_[index]_[label]` (e.g., "local_333_1_test")

### Mini App Processes
- Up to 10 concurrent mini apps supported
- Each runs in a separate process (MiniAppActivity0-9)
- HTML5/JavaScript based (Vue.js, React, etc.)
- Communicates with SDK via DSBridge

### JavaScript API Events
- **'dc'** - Data Channel operations (create, close, send, receive)
- **'miniapp'** - App control (start, window, hangup, mute, etc.)
- **'file'** - File operations (select, delete, compress)
- **'screen'** - Screen sharing
- **'ec'** - Extended Capabilities (AI, translation, AR)
- **'system'** - System information

### Extended Capabilities (EC)
- OEM customizations
- Operator-specific features (CT, CM, CU)
- AI video detection
- Real-time translation
- AR/VR integration

## Standards Compliance

The SDK implements:
- **3GPP TS.71:** IMS Data Channel specification
- **3GPP TS.66:** WebRTC-based Data Channel
- **GSMA 5G New Calling** specifications

## Architecture Highlights

### Design Patterns
- **Service-Oriented:** InCallService as entry point
- **Manager Pattern:** Separate managers for different concerns
- **Listener/Observer:** Event-driven architecture
- **Factory Pattern:** Event dispatcher routing
- **Queue-Based:** Ordered DC creation
- **Cache-First:** DC reuse for efficiency

### Key Design Decisions
- **Call-Bound Lifecycle:** All resources tied to active calls
- **Multi-Process:** Isolation and stability for mini apps
- **Queue-Based DC Creation:** Prevents overwhelming modem (3-second delays)
- **DC Caching:** Reuses open channels when apps restart
- **AIDL Interface:** Clean separation from terminal implementation
- **Extensibility:** EC provider system for OEM/operator features

## File Structure

```
document/
├── DIAGRAMS_README.md                      # This file (guide to all diagrams)
├── START-HERE.md                           # Quick start guide
│
├── 🌟 SIMPLIFIED DIAGRAMS (Start Here!)
├── simplified-flow-summary.puml            # Easy overview of SDK → Mini App → 5G
├── 5g-concepts-explained.puml              # 5G/networking terms explained
│
├── 💻 CODE-LEVEL DIAGRAMS (With Actual Functions!)
├── detailed-functions-flow.puml            # Real function names & line numbers ⭐ NEW
├── file-structure-flow.puml                # File paths & responsibilities ⭐ NEW
│
├── 📊 DETAILED TECHNICAL DIAGRAMS
├── architecture-overview.puml              # Complete system architecture
├── end-to-end-flow.puml                    # Component interactions
├── data-channel-creation-sequence.puml     # Detailed DC creation flow
└── call-lifecycle-flow.puml                # Complete call lifecycle
```

**Recommended Reading Order:**

**For Beginners:**
1. `simplified-flow-summary.puml` - Get the big picture
2. `5g-concepts-explained.puml` - Understand 5G terminology

**For Developers:**
3. `detailed-functions-flow.puml` ⭐ **START HERE** - See actual code flow
4. `file-structure-flow.puml` - Know which files to read
5. `architecture-overview.puml` - See all components
6. `data-channel-creation-sequence.puml` - Deep dive into DC creation
7. `call-lifecycle-flow.puml` - Understand lifecycle management

## Further Reading

For more information, refer to:
- `/README.md` - Project overview and setup
- `/README-EN.md` - English documentation
- `/core/` - Core SDK implementation
- `/miniapp/` - Mini app development guide
- `/base/` - Base data structures and interfaces

## Contributing

When updating these diagrams:
1. Ensure they reflect actual code implementation
2. Keep them synchronized with code changes
3. Add comments for complex flows
4. Test rendering in multiple viewers
5. Update this README if adding new diagrams

## License

These diagrams document the IMS-DC-SDK which is licensed under Apache License 2.0.
Copyright (C) China Telecom Research Institute

---

**Last Updated:** 2026-02-11
**SDK Version:** Based on current codebase analysis
