# I-Sujana — Design System (read for any UI stage)

**Direction:** Enterprise **"Trust & Authority"** — minimal, Swiss, functional, data-dense
but scannable. Target WCAG AA+ (AAA where feasible). This is an operational logistics tool,
not a consumer toy. **Avoid AI-slop:** no purple/pink gradients, no glassmorphism for its own
sake, no playful styling, no decorative motion, no emoji icons.

Implemented once in `app/.../core/theme` (Stage 0) as Material 3 tokens; all screens consume
tokens — **never raw hex in feature code.**

---

## Color tokens → Material 3 mapping

Source palette (slate/navy neutrals + emerald accent):

| Token | Light | Dark | M3 role |
|-------|-------|------|---------|
| Primary | `#0F172A` | `#E2E8F0` | `primary` (brand/structure; dark uses light slate for contrast) |
| Accent / CTA | `#16A34A` | `#22C55E` | `secondary`/`tertiary` or a custom `accent` — emerald, used for primary actions & positive status |
| Background | `#F8FAFC` | `#020617` | `background` |
| Surface | `#FFFFFF` | `#0F172A` | `surface` |
| Surface variant | `#F1F5F9` | `#1E293B` | `surfaceVariant` (cards, table rows) |
| On background/surface | `#0F172A` | `#F8FAFC` | `onBackground`/`onSurface` |
| Muted text | `#475569` | `#94A3B8` | `onSurfaceVariant` |
| Border / divider | `#E2E8F0` | `#334155` | `outline`/`outlineVariant` |
| Error/destructive | `#DC2626` | `#EF4444` | `error` |

Provide both light and dark `ColorScheme`s. Dark mode uses desaturated/lighter tonal
variants (not inverted). Test contrast independently per mode (body text ≥ 4.5:1).

### Status semantics (functional color — always pair with icon + label)
- **Green** (`#16A34A`/`#22C55E`) — OK / Completed / Active / positive sustainability.
- **Amber** (`#D97706`/`#F59E0B`) — Pending / Assigned / at-risk / awaiting action.
- **Red** (`#DC2626`/`#EF4444`) — Overdue / Cancelled / Error.
- **Slate/Blue-gray** — Neutral / Draft / informational.

Define these as a small `StatusColors` extension on the theme so request/assignment statuses
render consistently everywhere (chips, list rows, map markers).

---

## Typography — Inter

One family, **Inter** (bundle as a downloadable/asset font; fallback system sans). Map to
Material 3 type roles. Use **tabular figures** for any numbers in tables, metrics, counts,
timers, and money so columns never shift.

| Role | Size / weight (guide) | Use |
|------|----------------------|-----|
| displaySmall | 36 / 600 | rare hero numbers on dashboards |
| headlineSmall | 24 / 600 | screen titles |
| titleLarge | 22 / 600 | section headers |
| titleMedium | 16 / 600 | card titles, list primary |
| bodyLarge | 16 / 400 | primary body (min 16sp for readability) |
| bodyMedium | 14 / 400 | secondary text |
| labelLarge | 14 / 500 | buttons |
| labelMedium | 12 / 500 | chips, captions, table headers |

Weights: headings 600–700, body 400, labels/medium 500. Respect Dynamic Type — don't
truncate where wrapping works.

---

## Spacing, shape, elevation

- **Spacing scale (4/8dp):** 4, 8, 12, 16, 24, 32, 48. Expose as `Spacing` tokens. Default
  screen padding 16dp; section gaps 24dp.
- **Corner radius:** small 8dp (chips/inputs), medium 12dp (cards), large 16dp (sheets/dialogs).
- **Elevation:** a single consistent scale — surface 0, cards 1dp, app bars 2dp, sheets/dialogs
  3dp. No random shadow values.
- **Density:** comfortable but information-dense for admin/dispatch tables; rider/contributor
  screens slightly roomier (their primary actions are big and obvious).

---

## Component conventions

- **Buttons:** one **primary CTA per screen** (filled, accent). Secondary = tonal/outlined.
  Destructive = error color, visually separated. Show spinner + disable on async.
- **Cards:** `surfaceVariant`, 12dp radius, 1dp elevation, 16dp inner padding.
- **Status chips:** small rounded chip using StatusColors + an icon + text label.
- **Data tables / lists:** tabular figures, clear column headers (labelMedium), sortable where
  useful, zebra via `surfaceVariant`, sticky header on long tables.
- **Forms:** visible labels (never placeholder-only), helper text below, inline validation on
  blur, error text + error color below the field, correct keyboard types, password show/hide.
- **Empty / loading / error states:** every data screen handles all four. Loading > ~300ms →
  skeleton/shimmer, not a bare spinner. Empty → message + primary action. Error → message +
  retry.
- **Maps:** neutral Google Maps style; custom markers using StatusColors; route polyline in
  accent; bottom sheet for trip/assignment details.
- **Icons:** one Lucide-style outline set, consistent stroke width and sizing tokens
  (sm 16, md 24, lg 32). Filled vs outline not mixed at the same hierarchy level.

---

## Motion (restrained)

- Micro-interactions 150–300ms; transitions ≤ 400ms; ease-out enter / ease-in exit.
- Motion conveys cause/effect only (navigation direction, sheet from source). No decorative
  loops. Respect reduced-motion. Press feedback (ripple/state layer) within ~100ms.

---

## Pre-delivery UI checklist (per UI stage)

- [ ] No raw hex in feature code; only theme tokens.
- [ ] Light + dark both verified; text contrast ≥ 4.5:1 each.
- [ ] Status conveyed by icon+label, not color alone.
- [ ] Touch targets ≥ 48dp; safe-area insets respected.
- [ ] Loading/empty/error/content all handled.
- [ ] Tabular figures on all data/number columns.
- [ ] No emoji icons; single icon family; consistent sizing.
- [ ] Reduced-motion + Dynamic Type don't break layout.
