# RFB Protocol Overview

RFB ("Remote Framebuffer") is the protocol underlying VNC. It operates at the framebuffer level, making it applicable to all windowing systems. The remote user endpoint is the **client** (viewer); the windowing system side is the **server**.

This document summarises the protocol as implemented in vnc4j, based on the RFB 3.8 specification (`rfbproto.rst.txt`).

---

## Table of Contents

1. [Protocol Phases](#protocol-phases)
2. [Handshaking Phase](#handshaking-phase)
3. [Security Types](#security-types)
4. [Initialisation Phase](#initialisation-phase)
5. [Normal Operation Phase](#normal-operation-phase)
6. [Client-to-Server Messages](#client-to-server-messages)
7. [Server-to-Client Messages](#server-to-client-messages)
8. [Encodings and Pseudo-Encodings](#encodings-and-pseudo-encodings)
9. [Key Design Principles](#key-design-principles)

---

## Protocol Phases

The RFB protocol has three sequential phases:

```
┌─────────────┐      ┌─────────────┐      ┌─────────────────────┐
│ Handshaking │  →   │ Initialisa- │  →   │  Normal Operation   │
│    Phase    │      │    tion     │      │  (message loop)     │
└─────────────┘      └─────────────┘      └─────────────────────┘
  Version +             ClientInit /          Client sends input,
  Security              ServerInit            requests updates;
  negotiation                                 server sends updates
```

---

## Handshaking Phase

### Version Negotiation

```
Server                                     Client
  │                                          │
  │── ProtocolVersion ("RFB 003.008\n") ──► │
  │                                          │
  │◄── ProtocolVersion ("RFB 003.008\n") ───│
  │                                          │
```

The server sends the highest version it supports; the client responds with the version to use. Supported versions: 3.3, 3.7, 3.8. vnc4j always uses **RFB 3.8**.

### Security Negotiation (RFB 3.7+)

```
Server                                     Client
  │                                          │
  │── SecurityTypes ([1, 2, ...]) ─────────► │
  │                                          │
  │◄── SecurityTypeSelection (chosen type) ──│
  │                                          │
  │  [security-type-specific exchange]       │
  │                                          │
  │── SecurityResult (OK / Failed) ────────► │
  │                                          │
```

If `SecurityResult` is `OK`, the protocol advances to the initialisation phase. If it fails, RFB 3.8 sends a reason string before closing the connection.

vnc4j currently supports **Security Type 1 (None)** only.

### Full Handshake Sequence (None security, RFB 3.8)

```
Server                                     Client
  │                                          │
  │── "RFB 003.008\n" (12 bytes) ──────────►│
  │◄── "RFB 003.008\n" (12 bytes) ──────────│
  │                                          │
  │── SecurityTypes {count=1, types=[1]} ──►│
  │◄── SecurityTypeSelection {type=1} ───────│
  │                                          │
  │── SecurityResult {status=0 (OK)} ──────►│
  │                                          │
```

---

## Security Types

The following security types are defined by the RFB specification. vnc4j implements **None (1)** only.

| Number | Name                              | Notes                                                         |
|--------|-----------------------------------|---------------------------------------------------------------|
| 0      | Invalid                           | Connection failure; followed by reason string                 |
| 1      | **None**                          | No authentication; data unencrypted. **Implemented in vnc4j** |
| 2      | VNC Authentication                | DES challenge–response using a password                       |
| 5      | RSA-AES                           | RSA key exchange + AES-EAX encryption                         |
| 6      | RSA-AES Unencrypted               | RSA handshake only; data unencrypted after                    |
| 13     | RSA-AES Two-step                  | RSA-AES with second key-derivation round                      |
| 16     | Tight Security Type               | Generic extension (tunnelling, auth, capability exchange)      |
| 19     | VeNCrypt                          | Wraps TLS/X509 around other auth subtypes                     |
| 20     | SASL                              | RFC 2222 SASL pluggable authentication                        |
| 22     | xvp Authentication                | VNC Auth extended with username and target system name        |
| 30     | Diffie-Hellman Authentication     | DH key exchange + AES-ECB credential encryption               |
| 113    | MSLogonII Authentication          | DH-based (64-bit, weak) + DES credential encryption           |
| 129    | RSA-AES-256                       | RSA-AES using SHA-256 instead of SHA-1                        |
| 130    | RSA-AES-256 Unencrypted           | RSA-AES-256 handshake only                                    |
| 133    | RSA-AES-256 Two-step              | RSA-AES-256 with second key-derivation round                  |

### VNC Authentication Detail

```
Server                                     Client
  │── 16-byte random challenge ───────────►│
  │◄── 16-byte DES(challenge, password) ───│
  │── SecurityResult ───────────────────── ►│
```

### VeNCrypt Subtypes

VeNCrypt wraps a TLS session around an inner authentication method:

| Code | Name        | Description                                      |
|------|-------------|--------------------------------------------------|
| 256  | Plain        | Plain (username + password, no encryption)       |
| 257  | TLSNone      | TLS with anonymous cert; no credential auth      |
| 258  | TLSVnc       | TLS + VNC challenge–response                     |
| 259  | TLSPlain     | TLS + plain credentials                          |
| 260  | X509None     | X.509 cert; no credential auth                   |
| 261  | X509Vnc      | X.509 cert + VNC challenge–response              |
| 262  | X509Plain    | X.509 cert + plain credentials                   |
| 263  | TLSSASL      | TLS + SASL                                       |
| 264  | X509SASL     | X.509 cert + SASL                                |
| 265  | Ident        | Ident-based username (redirect only)             |
| 266  | TLSIdent     | TLS + Ident                                      |
| 267  | X509Ident    | X.509 cert + Ident                               |

---

## Initialisation Phase

```
Server                                     Client
  │                                          │
  │◄── ClientInit {shared-flag} ─────────── │
  │                                          │
  │── ServerInit {                   ──────►│
  │     framebuffer-width,                   │
  │     framebuffer-height,                  │
  │     pixel-format (PIXEL_FORMAT),         │
  │     name-string                          │
  │   }                                      │
```

**ClientInit** carries a single byte: `shared-flag` (non-zero = share the desktop; zero = exclusive access).

**ServerInit** carries:
- Framebuffer dimensions (width × height)
- Server's natural pixel format (`PIXEL_FORMAT` — bits-per-pixel, depth, byte order, colour masks/shifts)
- Desktop name string (UTF-8 recommended)

---

## Normal Operation Phase

After initialisation, both sides enter the normal message loop. Updates are **demand-driven**: the client sends a `FramebufferUpdateRequest` and the server responds with a `FramebufferUpdate`.

### Typical Update Cycle

```
Server                                     Client
  │                                          │
  │◄── SetEncodings [0,2,5,7,16,...] ───────│  (declare supported encodings)
  │◄── FramebufferUpdateRequest            ──│  (incremental=false, whole screen)
  │                                          │
  │── FramebufferUpdate {rectangles} ──────►│
  │                                          │
  │◄── FramebufferUpdateRequest ─────────── │  (incremental=true)
  │                         ┆               │
  │  (server sends when     ┆               │
  │   framebuffer changes)  ┆               │
  │── FramebufferUpdate {rectangles} ──────►│
  │                                          │
  │◄── KeyEvent / PointerEvent ─────────── │  (user input at any time)
  │◄── ClientCutText ──────────────────────│  (clipboard)
```

### Continuous Updates Extension

When the `ContinuousUpdates` pseudo-encoding is negotiated, the client can enable push-mode updates without polling:

```
Server                                     Client
  │                                          │
  │◄── EnableContinuousUpdates {enable=1} ──│
  │                                          │
  │── FramebufferUpdate ───────────────────►│  (pushed as changes occur)
  │── FramebufferUpdate ───────────────────►│
  │                         ┆               │
  │◄── EnableContinuousUpdates {enable=0} ──│
  │── EndOfContinuousUpdates ──────────────►│
```

---

## Client-to-Server Messages

### Required Messages

| Type | Name                        | Description                                                  |
|------|-----------------------------|--------------------------------------------------------------|
| 0    | `SetPixelFormat`            | Change the pixel format the server uses for framebuffer data |
| 2    | `SetEncodings`              | Declare supported encodings (and pseudo-encodings)           |
| 3    | `FramebufferUpdateRequest`  | Request a framebuffer update for a region                    |
| 4    | `KeyEvent`                  | Key press or release (X11 keysym)                            |
| 5    | `PointerEvent`              | Mouse move or button press/release                           |
| 6    | `ClientCutText`             | Send clipboard text to server (Latin-1 or extended)          |

### Optional/Extension Messages

| Type | Name                        | Description                                                          |
|------|-----------------------------|----------------------------------------------------------------------|
| 150  | `EnableContinuousUpdates`   | Toggle continuous push-mode framebuffer updates                      |
| 248  | `ClientFence`               | Stream synchronisation barrier                                       |
| 250  | `xvp Client Message`        | Request server shutdown / reboot / reset                             |
| 251  | `SetDesktopSize`            | Request framebuffer resize and/or screen layout change               |
| 253  | `gii Client Message`        | General Input Interface (joysticks, extra axes, etc.)                |
| 255  | `QEMU Client Message`       | Extended key events (with keycodes), audio control                   |

### SetPixelFormat Structure

```
U8   message-type = 0
U8×3 padding
--- PIXEL_FORMAT (16 bytes) ---
U8   bits-per-pixel
U8   depth
U8   big-endian-flag
U8   true-colour-flag
U16  red-max
U16  green-max
U16  blue-max
U8   red-shift
U8   green-shift
U8   blue-shift
U8×3 padding
```

### FramebufferUpdateRequest Structure

```
U8   message-type = 3
U8   incremental   (0 = full refresh, 1 = incremental)
U16  x-position
U16  y-position
U16  width
U16  height
```

---

## Server-to-Client Messages

### Required Messages

| Type | Name                    | Description                                                      |
|------|-------------------------|------------------------------------------------------------------|
| 0    | `FramebufferUpdate`     | One or more encoded rectangles of pixel data                     |
| 1    | `SetColourMapEntries`   | Define palette entries when using indexed colour mode            |
| 2    | `Bell`                  | Ring the client's audio bell                                     |
| 3    | `ServerCutText`         | Send clipboard text to client (Latin-1 or extended)              |

### Optional/Extension Messages

| Type | Name                    | Description                                                      |
|------|-------------------------|------------------------------------------------------------------|
| 150  | `EndOfContinuousUpdates`| Acknowledge that continuous updates have been disabled           |
| 248  | `ServerFence`           | Stream synchronisation barrier                                   |
| 250  | `xvp Server Message`    | Inform client of xvp support or operation failure                |
| 253  | `gii Server Message`    | General Input Interface version and device responses             |
| 255  | `QEMU Server Message`   | Audio data stream                                                |

### FramebufferUpdate Structure

```
U8   message-type = 0
U8   padding
U16  number-of-rectangles

-- repeated number-of-rectangles times --
U16  x-position
U16  y-position
U16  width
U16  height
S32  encoding-type
[encoding-specific payload]
```

---

## Encodings and Pseudo-Encodings

A client advertises its supported encodings via `SetEncodings`. The server chooses the best encoding for each rectangle. **Pseudo-encodings** are not actual pixel formats; they signal protocol capabilities or carry metadata.

### Real Encodings

| Number | Name             | Java Class                      | Description                                                                                                      |
|--------|------------------|---------------------------------|------------------------------------------------------------------------------------------------------------------|
| 0      | Raw              | `RfbRectangleRaw`               | Uncompressed pixel array, row by row. Mandatory; all clients must support it.                                    |
| 1      | CopyRect         | `RfbRectangleCopyRect`          | Copy a rectangle from elsewhere in the framebuffer (2-byte src-x, 2-byte src-y). Very efficient for window moves.|
| 2      | RRE              | `RfbRectangleRre`               | Rise-and-Run-Length Encoding. Background colour + list of solid subrectangles.                                   |
| 4      | CoRRE            | *(not in dispatch)*             | Compressed RRE; subrect coordinates limited to U8 (max 255×255 each).                                           |
| 5      | Hextile          | `RfbRectangleHextile`           | Rectangle split into 16×16 tiles, each encoded as raw or RRE-like with sub-nibble coordinates.                   |
| 6      | zlib             | `RfbRectangleZlib`              | Raw encoding data compressed with a single persistent zlib stream.                                               |
| 7      | Tight            | `RfbRectangleTight`             | Multi-stream zlib with Fill, JPEG, or Basic (with optional palette/gradient filters) sub-types.                  |
| 8      | zlibhex          | *(not in dispatch)*             | Hextile with optional per-tile zlib compression.                                                                 |
| 16     | ZRLE             | `RfbRectangleZrle`              | Zlib Run-Length Encoding: 64×64 tiles with palettisation and RLE, all zlib-compressed.                           |
| 21     | JPEG             | `RfbRectangleJpeg`              | Raw JFIF stream. No length prefix; requires an out-of-band delimiter (not self-delimiting in vnc4j).             |
| 50     | Open H.264       | *(not in dispatch)*             | H.264 baseline stream per named context rectangle.                                                               |
| -260   | Tight PNG        | `RfbRectangleTightPng`          | Tight variant using Fill, JPEG, or **PNG** instead of raw zlib BasicCompression.                                 |

#### Tight Sub-Types

The Tight encoding (type 7) reads a `compression-control` byte and dispatches to one of:

| High nibble | Sub-type              | Java Class                   | Description                                          |
|-------------|-----------------------|------------------------------|------------------------------------------------------|
| `0x8`       | FillCompression       | `RfbRectangleTightFill`      | Entire rectangle is one solid `TPIXEL` colour.       |
| `0x9`       | JpegCompression       | `RfbRectangleTightJpeg`      | JFIF data with compact-length prefix.                |
| other       | BasicCompression      | `RfbRectangleTightBasic`     | zlib-compressed data with optional palette/gradient. |

Tight PNG (type -260) additionally has:

| High nibble | Sub-type              | Java Class                    | Description                               |
|-------------|-----------------------|-------------------------------|-------------------------------------------|
| `0x8`       | FillCompression       | `RfbRectangleTightPngFill`   | Same as Tight Fill.                       |
| `0x9`       | JpegCompression       | `RfbRectangleTightPngJpeg`   | Same as Tight JPEG.                       |
| `0xA`       | PngCompression        | `RfbRectangleTightPngPng`    | PNG image data instead of raw zlib.       |

### Pseudo-Encodings

Pseudo-encodings appear in `SetEncodings` and may appear as encoding types in `FramebufferUpdate` rectangles, but they carry metadata rather than raw pixel data.

| Number         | Name                              | Java Class                          | Description                                                                                              |
|----------------|-----------------------------------|-------------------------------------|----------------------------------------------------------------------------------------------------------|
| -23 to -32     | JPEG Quality Level                | —                                   | Hint: -23 = highest quality, -32 = lowest. Enables JpegCompression in Tight.                            |
| -223           | DesktopSize                       | `RfbRectangleDesktopSize`           | Server signals framebuffer resize; width/height in rectangle header, no payload.                         |
| -224           | LastRect                          | `RfbRectangleLastRect`              | Terminates a `FramebufferUpdate` early; client stops parsing further rectangles.                         |
| -239           | Cursor                            | `RfbRectangleCursor`                | Server sends cursor shape (pixel array + bitmask) for local rendering.                                   |
| -240           | X Cursor                          | —                                   | Two-colour cursor (primary/secondary + bitmap + bitmask) for local rendering.                            |
| -247 to -256   | Compression Level                 | —                                   | Hint: -247 = high compression, -256 = low. Tradeoff: CPU vs. bandwidth.                                  |
| -257           | QEMU Pointer Motion Change        | —                                   | Server switches pointer events between absolute coordinates and relative deltas.                          |
| -258           | QEMU Extended Key Event           | —                                   | Client declares support for keycodes in addition to keysyms.                                             |
| -259           | QEMU Audio                        | —                                   | Client declares ability to receive audio stream; server sends QEMU Audio messages.                       |
| -261           | QEMU LED State                    | —                                   | Server sends keyboard lock key state (ScrollLock, NumLock, CapsLock bits).                               |
| -305           | gii                               | —                                   | General Input Interface: joysticks, extra axes, many buttons.                                            |
| -307           | DesktopName                       | —                                   | Server can update the desktop name at runtime.                                                           |
| -308           | ExtendedDesktopSize               | `RfbRectangleExtendedDesktopSize`   | Framebuffer resize with full multi-screen layout; replaces DesktopSize.                                  |
| -309           | xvp                               | —                                   | Enables remote power-management commands (shutdown, reboot, reset).                                      |
| -312           | Fence                             | —                                   | Stream barrier/synchronisation (BlockBefore, BlockAfter, SyncNext flags).                                |
| -313           | ContinuousUpdates                 | —                                   | Enables push-mode updates via `EnableContinuousUpdates` / `EndOfContinuousUpdates`.                      |
| -314           | Cursor With Alpha                 | —                                   | Cursor shape with pre-multiplied RGBA alpha channel.                                                     |
| -316           | ExtendedMouseButtons              | —                                   | Extends `PointerEvent` with an extra byte for up to 15 mouse buttons.                                    |
| -317           | Tight Without Zlib                | —                                   | Declares support for Tight `BasicCompression Without Zlib` sub-type.                                     |
| -412 to -512   | JPEG Fine-Grained Quality Level   | —                                   | 0–100 JPEG quality scale (-512 = 0, -412 = 100).                                                         |
| -763 to -768   | JPEG Subsampling Level            | —                                   | Chrominance subsampling: -768=1X (none), -767=4X, -766=2X, -765=greyscale, -764=8X, -763=16X.           |
| 0x574d5664     | VMware Cursor                     | —                                   | Classic (AND/XOR mask) or alpha cursor shape.                                                            |
| 0x574d5665     | VMware Cursor State               | —                                   | Cursor visibility, absolute/relative mode, warp flag.                                                    |
| 0x574d5666     | VMware Cursor Position            | —                                   | Server forces cursor hot-spot position.                                                                  |
| 0x574d5667     | VMware Key Repeat                 | —                                   | Negotiate whether client or server handles key auto-repeat.                                              |
| 0x574d5668     | VMware LED State                  | —                                   | Keyboard lock key LEDs (ScrollLock, NumLock, CapsLock).                                                  |
| 0x574d5669     | VMware Display Mode Change        | —                                   | Framebuffer resize including pixel format change; must be followed by ExtendedDesktopSize.               |
| 0x574d566a     | VMware Virtual Machine State      | —                                   | VM state flags (fullscreen console, VNC updates disabled).                                               |
| 0xc0a1e5ce     | Extended Clipboard                | —                                   | Extends cut-text messages with rich formats (text, RTF, HTML, DIB), requests, and notifications.         |

---

## Key Design Principles

**Thin client** — The server must always be able to produce pixel data in whatever format the client requests. Clients can be extremely simple; the mandatory encoding is Raw.

**Demand-driven updates** — The server only sends a `FramebufferUpdate` in response to a `FramebufferUpdateRequest` (or when continuous updates are enabled). This adapts automatically to slow clients and networks.

**Stateless client** — If a client disconnects and reconnects to the same server, the user interface state is preserved. A different client can connect and see the same state.

**Extensible via encodings** — New capability is added as new encodings or pseudo-encodings. Servers ignore unknown encoding requests; clients ignore unknown pseudo-encoding rectangles. No version bumps are needed.

**Pixel format negotiation** — After `ServerInit`, the client can send `SetPixelFormat` to request any pixel layout. The server must comply. This allows clients with limited rendering to work with servers that natively use a different colour depth.

**Byte order** — All multi-byte integers (except pixel values and some gii extension fields) are big-endian. Strings are recommended to use UTF-8.
