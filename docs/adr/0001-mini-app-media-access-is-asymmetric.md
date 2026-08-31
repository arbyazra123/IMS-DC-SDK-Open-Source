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

Consequence: only the local user's own mic input is directly reachable from a mini-app's JS —
there is no EC request that hands you the far end's raw speech or video. The `Avatar` mini-app
is therefore a control surface only (it can't render the swapped video itself; that has to
happen natively before the video is ever transmitted, so the callee sees it for free with no
extra data path). `Translate` can locally detect "I just spoke" via mic volume and get a local
EC result for it, but getting that result in front of the *other* participant is not an EC
concern at all — it requires the ADC (Application Data Channel): the identical mini-app runs
on both devices (`shouldStartRemoteApp: true`), and each instance sends its own local
translation result to the other's instance over the ADC. There is no capability provider that
can simulate "the other side is speaking" on the EC path — that data can only ever come from
the other side's own mini-app instance, over the peer channel.
