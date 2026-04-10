# 🚀 START HERE - Understanding IMS-DC-SDK

> **You mentioned being confused about 5G, networking, and terminal concepts.**
> **This guide will help! Start with the simplified diagrams below.**

---

## 📖 Reading Path for Beginners

### Step 1: 🌟 Understand the Big Picture (5 minutes)
**Read:** `simplified-flow-summary.puml`

**What you'll learn:**
- What is a mini app?
- How does the SDK connect mini apps to 5G hardware?
- What is a "data channel" and why do we need it?
- Real example: Restaurant menu during a phone call

**Key takeaway:** You'll understand how all the pieces fit together without technical jargon.

---

### Step 2: 🎓 Learn the Confusing Terms (10 minutes)
**Read:** `5g-concepts-explained.puml`

**What you'll learn:**
- What is "Terminal"? → It's just your phone!
- What is "IMS Network"? → Modern phone calls over internet
- What is "Modem"? → The 5G chip in your phone
- What is "AIDL"? → How Android talks to the modem
- Highway analogy to understand data channels

**Key takeaway:** All those scary networking terms will make sense.

---

### Step 3: 💻 See Real Code Flow (⭐ NEW - For Developers)
**Read:** `detailed-functions-flow.puml`

**What you'll learn:**
- Actual function names from the code (e.g., `DCManager.createApplicationDataChannels()`)
- Exact file paths and line numbers (e.g., `DCMiniUseCase.kt:52`)
- Real parameters and validation logic
- 13 numbered steps with detailed notes
- Vertical flow (easy to follow)

**Key takeaway:** You'll know exactly which functions are called and where to find them in the code.

**Example Functions You'll See:**
- `JSApi.async(msg, handler)` - Line 48
- `DCJsEventDispatcher.dispatchAsyncMessage()` - Line 36
- `DCMiniUseCase.createAppDataChannel(params)` - Line 52
- `DCManager.createApplicationDataChannels()` - Line 328
- `IImsDataChannelCallback.onApplicationDataChannelResponse()` - Line 217

---

### Step 4: 📁 Know Which Files to Read (⭐ NEW - For Developers)
**Read:** `file-structure-flow.puml`

**What you'll learn:**
- Complete file paths for all components
- Which file handles which responsibility
- Key functions in each file
- How files connect to each other
- Layer organization

**Key takeaway:** When you need to modify something, you'll know exactly which file to open.

---

### Step 5: 🏗️ See the Architecture (Optional - for deeper understanding)
**Read:** `architecture-overview.puml`

**What you'll learn:**
- All SDK components and how they connect
- Managers, dispatchers, use cases, AIDL interfaces
- Multi-layer architecture

**Key takeaway:** Where each piece of code lives and how they interact.

---

### Step 6: 🔄 Follow a Real Transaction (Optional - for developers)
**Read:** `data-channel-creation-sequence.puml`

**What you'll learn:**
- Step-by-step: What happens when user clicks "Create Data Channel"
- 13 phases from JavaScript call to data channel ready
- Timing details and queue processing

**Key takeaway:** Complete understanding of data channel creation flow.

---

### Step 7: ⏱️ Understand Lifecycle (Optional - for developers)
**Read:** `call-lifecycle-flow.puml`

**What you'll learn:**
- What happens when a call starts?
- How are managers initialized?
- What happens when a call ends?
- Resource cleanup process

**Key takeaway:** How the SDK manages resources throughout a call's lifetime.

---

## 🎯 Quick Answers to Common Questions

### Q: What is this project?
**A:** An Android SDK that lets mini apps (HTML5/JavaScript) send data during 5G phone calls. Like sharing a menu or playing a game WHILE talking on the phone.

### Q: Why do we need special SDK for this?
**A:** Regular phone calls only send voice/video. 5G New Calling adds DATA CHANNELS. This SDK makes it easy to use those channels.

### Q: What is the "Terminal" everyone talks about?
**A:** Just your Android phone! In telecom language:
- **Terminal** = Your phone (Android OS + 5G chip)
- **Modem** = The 5G chip inside your phone
- **IMS** = The network system that handles 5G calls

### Q: What is IMS?
**A:** **IMS = IP Multimedia Subsystem**
- Old way: Calls use cellular network (circuit-switched)
- New way: Calls use 5G internet (packet-switched)
- Think of it as: Zoom/Skype, but built into the 5G network by China Telecom

### Q: What is a Data Channel?
**A:** An extra "pipe" for data during a call:
- **Voice Channel** → Audio (what you say)
- **Video Channel** → Camera (what you show)
- **Data Channel** → App data (menu, game moves, AR data, files, etc.) ← **NEW!**

### Q: What is AIDL?
**A:** **AIDL = Android Interface Definition Language**
- The modem chip is separate hardware from Android
- AIDL is the "bridge" that lets Android apps talk to modem
- SDK uses AIDL to tell modem: "Create a data channel!"

### Q: What do I need to know to use this SDK?
**A:** Just two things:
1. **HTML/JavaScript** - To build your mini app (like building a website)
2. **SDK JavaScript API** - To call functions like `createDataChannel()`, `sendData()`

The SDK handles all the complex 5G/networking/modem stuff for you!

---

## 🛠️ How to View the Diagrams

### Option 1: Online (Easiest)
1. Go to: http://www.plantuml.com/plantuml/uml/
2. Open any `.puml` file
3. Copy all the text
4. Paste into the online editor
5. See the diagram!

### Option 2: VS Code (Best for developers)
1. Install "PlantUML" extension by jebbs
2. Open any `.puml` file
3. Press `Alt+D` (Windows/Linux) or `Option+D` (Mac)
4. Diagram appears!

### Option 3: IntelliJ/Android Studio
1. Install "PlantUML integration" plugin
2. Open any `.puml` file
3. Diagram renders automatically

---

## 📚 Diagram Summary Table

| Diagram | Complexity | Time to Read | Best For |
|---------|-----------|--------------|----------|
| `simplified-flow-summary.puml` | ⭐ Easy | 5 min | Understanding the big picture |
| `5g-concepts-explained.puml` | ⭐ Easy | 10 min | Learning 5G/networking terms |
| `detailed-functions-flow.puml` ⭐ NEW | ⭐⭐⭐ Advanced | 25 min | Real function names & code flow |
| `file-structure-flow.puml` ⭐ NEW | ⭐⭐ Medium | 15 min | Finding files & understanding structure |
| `architecture-overview.puml` | ⭐⭐ Medium | 15 min | Seeing all components |
| `end-to-end-flow.puml` | ⭐⭐ Medium | 15 min | Component interactions |
| `data-channel-creation-sequence.puml` | ⭐⭐⭐ Advanced | 20 min | Deep dive into DC creation |
| `call-lifecycle-flow.puml` | ⭐⭐⭐ Advanced | 20 min | Lifecycle management |

---

## 🎓 Learning Path by Role

### 👨‍💼 Product Manager / Non-Technical
**Read only:**
1. `simplified-flow-summary.puml` - Understand what this SDK does
2. `5g-concepts-explained.puml` - Learn the terminology

**Time needed:** 15 minutes

---

### 👨‍🎨 Mini App Developer (HTML/JS)
**Read:**
1. `simplified-flow-summary.puml` - Big picture
2. `5g-concepts-explained.puml` - Learn terminology
3. `data-channel-creation-sequence.puml` - How to create channels

**Time needed:** 35 minutes

**You can ignore:** Architecture diagrams (SDK handles it for you)

---

### 👨‍💻 SDK Developer / Android Engineer
**Read all diagrams in order:**
1. `simplified-flow-summary.puml` (5 min) - Big picture
2. `5g-concepts-explained.puml` (10 min) - Terminology
3. `detailed-functions-flow.puml` ⭐ (25 min) - **Real code flow**
4. `file-structure-flow.puml` ⭐ (15 min) - **File structure**
5. `architecture-overview.puml` (15 min) - Architecture
6. `end-to-end-flow.puml` (15 min) - Component interactions
7. `data-channel-creation-sequence.puml` (20 min) - DC creation details
8. `call-lifecycle-flow.puml` (20 min) - Lifecycle

**Time needed:** 2 hours

**Then:** Read the actual code files identified in `file-structure-flow.puml`
- Start with: `/core/src/main/java/com/ct/ertclib/dc/core/usecase/miniapp/DCMiniUseCase.kt`
- Then: `/core/src/main/java/com/ct/ertclib/dc/core/manager/call/DCManager.kt`

---

## 🔗 Related Documentation

- `/README.md` - Main project README (Chinese)
- `/README-EN.md` - English project README
- `/core/` - SDK core implementation code
- `/miniapp/demo/` - Example mini app (Vue.js)
- `/document/DIAGRAMS_README.md` - Detailed diagram guide

---

## 💡 Still Confused?

**Common confusion points and solutions:**

| If you're confused about... | Read this diagram... | Look for this section... |
|----------------------------|---------------------|-------------------------|
| What this project does | `simplified-flow-summary.puml` | "THE BIG PICTURE" |
| Terminal, Modem, IMS | `5g-concepts-explained.puml` | "KEY TERMS DECODED" |
| How mini app talks to SDK | `simplified-flow-summary.puml` | The numbered flow (1-13) |
| SDK architecture | `architecture-overview.puml` | All the packages |
| Creating a data channel | `data-channel-creation-sequence.puml` | Phase 1-13 |
| Call lifecycle | `call-lifecycle-flow.puml` | Start to end flow |

---

## ✅ Success Checklist

After reading the diagrams, you should understand:

- [ ] What a mini app is (HTML5 app that runs during calls)
- [ ] What a data channel is (extra data pipe during calls)
- [ ] What the SDK does (connects mini apps to 5G hardware)
- [ ] What Terminal means (your Android phone)
- [ ] What IMS means (modern phone call system over internet)
- [ ] What the Modem is (5G chip in your phone)
- [ ] How to create a data channel (call JavaScript API)
- [ ] How to send data (use sendData() function)

If you can check all boxes above, you're ready to use this SDK! 🎉

---

**Need more help?** Check the example mini app in `/miniapp/demo/IMS_DC_Mini_app_demo_source_code/`
