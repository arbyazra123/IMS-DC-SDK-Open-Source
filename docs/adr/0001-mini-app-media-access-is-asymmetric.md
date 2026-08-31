---
status: accepted
---

# Mini-app media access is asymmetric: local mic in, call video/audio out of reach

While building the `Avatar` and `Translate` Extended Capability mock modules and their
demo mini-apps, we needed to know what a mini-app's WebView can actually see of the call.

The SDK's `InCallServiceImpl` owns the real call video and the received (far-end) audio
entirely — neither is ever exposed to the Data Channel or the mini-app JS layer, and there
is no capability request that bridges them in. A mini-app **can** capture the device's own
microphone via `getUserMedia({audio:true})`, but only after clearing two platform gates:
`CTWebChromeClient.onPermissionRequest` requires **both** `MINIAPP_CAMERA` and
`MINIAPP_RECORD_AUDIO` to be granted even for an audio-only request, and the mini-app must
have already called `setSystemApiLicense` for `"getUserMedia"` — skipping either leaves the
permission prompt unresolved forever (no grant, no deny, no rejection).

Consequence: any EC capability that reacts to "what the caller is saying/showing" can only
be event-driven by the far end's native provider (pushed in via an EC callback), never
mini-app-detected; only the local user's own mic input is directly reachable from JS. The
`Avatar` mini-app is therefore a control surface only (it can't render the swapped video
itself), and `Translate` can locally detect "I just spoke" via mic volume but must rely on
a native-simulated push for "the other side just spoke."
