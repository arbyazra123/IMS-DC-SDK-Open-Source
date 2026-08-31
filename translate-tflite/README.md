# translate-tflite

A reference (non-mock) `IExpandingCapacity` provider for the `Translate` EC module,
using on-device TensorFlow Lite instead of `oemec`'s `TestECManager` mock. It answers
the same wire protocol as the mock — a mini-app built against `TestECService` works
against this unmodified — but actually runs inference instead of returning canned text.

## What this module does NOT include

**No `.tflite` model files, and no vocab files.** Per this SDK's design (see
`docs/adr/0001-mini-app-media-access-is-asymmetric.md` and `CONTEXT.md`), ML models are
never shipped inside the SDK — they're an OEM/operator concern, updated independently of
the SDK build. This module is the *plumbing* around wherever your models come from
(bundled by your app, downloaded on first run, etc.) — not the models themselves.

## Model file contract

At runtime this module looks for exactly these paths, and treats a missing file as
"capability unavailable" (it reports that back through `translateErrorCallback` rather
than crashing or fabricating a result):

```
<context.getExternalFilesDir(null)>/translate_tflite_models/
├── asr.tflite            # speech-to-text model
├── asr_vocab.txt         # one output token per line, index == line number
├── translate.tflite      # text-to-text translation model
└── translate_vocab.txt   # one output token per line, index == line number
```

## Assumptions this reference makes about your models

These are almost certainly **not** exactly what a real ASR/MT model wants — they exist so
`TranslateTfliteManager` has *something* concrete to run, demonstrating the integration
shape (service binding → EC protocol → interpreter lifecycle → error handling) rather than
being correct for any specific published model:

- **ASR** (`runAsr` in `TranslateTfliteManager.kt`): a single float32 input tensor of raw
  16kHz mono PCM samples (no mel-spectrogram/feature extraction), and a single output
  tensor of per-timestep logits over `asr_vocab.txt`, decoded greedily with CTC-style
  blank/repeat collapsing. A real model (Whisper, Conformer, ...) typically wants log-mel
  features and a real beam-search + WordPiece/SentencePiece decoder instead.
- **Translate** (`runTranslate`): a single int32 input tensor of whitespace-tokenized
  vocab ids, and a single output logits tensor over `translate_vocab.txt`, decoded
  greedily. A real MT model usually wants SentencePiece tokenization and a proper seq2seq
  decode loop (start/end tokens), not one forward pass.

If your models don't match these shapes, replace `runAsr`/`runTranslate` — everything
else (model loading/caching in `ModelRepository`, PCM decoding in `AudioCodec`, the EC
request/response plumbing in `TranslateTfliteManager`) is reusable regardless of what your
models actually expect.

## Wiring it in

`OemEC.init()` (in the `oemec` module) binds to whatever service is named by the
`expand_capacity_service_package_name` / `_cls` / `_action` string resources, defaulting
to `com.ct.oemec.test.TestECService`. Point `_cls` at
`com.ct.oemec.translatetflite.TranslateTfliteService` instead to use this module for
`Translate` — note it only implements that one module, so if your app also needs the
mock's `Avatar`/`AIVideo`/`NewCallSDK` behavior, either extend this manager to cover them
too, or keep both services and bind per-module in your own `IEC` implementation.
