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
The EC module that provides live speech translation between the two call participants. `start`/`stop` control the simulated arrival of the *other* party's translated speech (native-sourced, never mini-app-detectable); `voice` is fired by the mini-app itself when its own mic detects the local caller speaking (see `docs/adr/0001-mini-app-media-access-is-asymmetric.md`).

**Mini-app**:
A lightweight, sandboxed HTML/CSS/JS package (`index.html` + `properties.json` at its zip root) that a caller runs during a call inside the SDK's WebView runtime, communicating with native code only through the JS bridge (DSBridge) and EC requests — never by embedding capability logic itself.
_Avoid_: App, mini program, applet
