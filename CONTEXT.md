# IMS Data Channel SDK

The 5G New Calling Terminal SDK runtime for IMS Data Channel Applications ("mini-apps"): it manages the call-bound Data Channel lifecycle, hosts mini-apps in an isolated JS runtime, and brokers calls out to terminal/operator-provided Extended Capabilities.

## Language

**Extended Capability (EC)**:
A private, non-standard capability (e.g. screen share, AI avatar, translation) that a terminal manufacturer or operator plugs into the SDK behind the `IEC` interface, invoked by mini-apps or native call UI via `module`/`func`/`data` requests routed through `ExpandingCapacityManager`.
_Avoid_: Extension, plugin, add-on

**EC Provider**:
The party supplying an `IEC` implementation for a given namespace (`OEM`, `CT`, `CM`, `CU`). Real providers may themselves proxy the request off-device (e.g. through the modem to a cloud AI service) — the SDK only defines and routes the contract, it does not host the capability's real logic.
_Avoid_: Vendor, backend

**AIVideo module**:
The EC module that verifies whether a video feed is AI-generated (`detect` → `{isAI}}`) — a trust/integrity signal about the *other* party's video.
_Avoid_: Avatar, deepfake detection

**Avatar module**:
The EC module that lets a caller replace their own live video with a synthetic character (`avatarList`/`setAvatar`/`setAvatarEnable`/`avatarFrameCallback`) — an active self-presentation capability, distinct from `AIVideo`'s passive detection role. The mini-app side is a control surface only; it cannot render the swap itself (see `docs/adr/0001-mini-app-media-access-is-asymmetric.md`).
_Avoid_: AIVideo, face swap

**Translate module**:
The EC module that turns the local caller's own detected speech into original+translated text (`voice` → `translateResultCallback`). It only ever produces results for the *local* participant — getting a translated caption to the other participant is not an EC concern at all, it happens over the ADC (see `docs/adr/0001-mini-app-media-access-is-asymmetric.md`). Two `IExpandingCapacity` implementations answer it identically on the wire: `oemec`'s `TestECManager` (mock, canned phrases) and `translate-tflite`'s `TranslateTfliteManager` (reference, real on-device TFLite ASR+MT — ships no model files, see that module's README).

**Application Data Channel (ADC)**:
The peer-to-peer channel (`createAppDataChannel`/`sendData`/`messageNotify`, wrapped by `webrtcDC.js`'s `RTCDataChannel`) connecting the *same* mini-app running on the caller's and callee's devices, riding the network's IMS Data Channel. Distinct from EC: EC is local (device ↔ its own capability provider), ADC is peer-to-peer (mini-app instance ↔ the other side's mini-app instance). A mini-app that wants the other side's mini-app to react to something (e.g. today's translated caption) must send it over the ADC itself — no EC push can substitute for that.
_Avoid_: DC, Data Channel (too broad — also covers the Bootstrap Data Channel), peer channel

**Mini-app**:
A lightweight, sandboxed HTML/CSS/JS package (`index.html` + `properties.json` at its zip root) that a caller runs during a call inside the SDK's WebView runtime, communicating with native code only through the JS bridge (DSBridge), EC requests (local capabilities), and the ADC (peer mini-app instance) — never by embedding capability logic itself.
_Avoid_: App, mini program, applet
