# translate-speechkit

A real, working `Translate` EC provider built from platform pieces instead of a raw
TensorFlow Lite model: **`android.speech.SpeechRecognizer`** for speech-to-text and
**ML Kit Translate** (`com.google.mlkit:translate`) for machine translation. Unlike
`translate-tflite`, this one actually produces real recognized/translated text out of
the box — no model files to source or convert yourself.

## Requirements

- **Google Play Services** on the device. On a real phone this is normal; on an
  **emulator**, the AVD must use a "Google Play" system image (not a bare "Google APIs"
  or AOSP image) or `SpeechRecognizer`/ML Kit will simply be unavailable.
- **Internet access the first time** a given language pair is used — ML Kit downloads
  that pair's translation model once, then works fully offline. Speech recognition
  itself may also fall back to a network-backed recognizer on some devices/Android
  versions if no on-device recognizer is installed.
- Currently only **Indonesian ↔ English** are wired up (`LANGUAGE_TO_RECOGNIZER_LOCALE`
  / `LANGUAGE_TO_MLKIT` in `SpeechKitTranslateManager`) — add an entry to both maps to
  support another language.

## The important architectural difference from the mock/translate-tflite

The mock and `translate-tflite` both expect the mini-app to capture audio itself
(`getUserMedia` in the WebView) and send it as PCM bytes in the `voice` EC request.
**`SpeechRecognizer` doesn't work that way** — it owns the microphone directly and
continuously for as long as it's listening; it has no API to accept a pre-recorded
buffer. So here:

- `start` begins a continuous native listening session (each recognized utterance is
  translated and pushed as soon as it's ready; `SpeechRecognizer`'s single-shot session
  is restarted automatically in `onResults`/`onError` to keep listening until `stop`).
- `voice` (and whatever audio the mini-app sent with it) is **ignored** — it's accepted
  for wire-compatibility with the other two providers, but does nothing here.

**Known risk this creates**: the Translation mini-app's own `getUserMedia` call (used
for its mic-level dot indicator) and this provider's native `SpeechRecognizer` session
both want the microphone at the same time. Whether Android allows that concurrently is
inconsistent across devices/versions. If `SpeechRecognizer` reports `ERROR_AUDIO`
repeatedly, `SpeechKitTranslateManager` gives up after 3 consecutive failures and
reports it via `translateErrorCallback` with a message naming the mini-app's own mic
capture as the likely cause, rather than retrying forever. If you hit this, the fix is
on the mini-app side: stop calling `getUserMedia` (skip `startMicListening()`) while
this provider is active, and rely purely on native `start`/`stop`.

## Wiring it in

Point `OemEC`'s `expand_capacity_service_cls` string resource at
`com.ct.oemec.speechkittranslate.SpeechKitTranslateService` instead of
`TestECService`/`TranslateTfliteService`. Like `translate-tflite`, this only implements
`Translate` — `Avatar`/`AIVideo`/`NewCallSDK` have no provider while it's active.
