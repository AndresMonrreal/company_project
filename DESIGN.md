<!-- SEED: re-run `$impeccable document` once Angular code exists to capture real tokens and generate the .impeccable/design.json sidecar. -->

---
name: RubberTrace OPERATIONS
description: Manufacturing traceability for rubber profile cutting — shift-floor operational UI
---

# Design System: RubberTrace OPERATIONS

## 1. Overview

**Creative North Star: "The Flight Deck at Hour Zero"**

RubberTrace OPERATIONS is the instrument panel you read the moment the shift lights go on. The interface is architectural first, chromatic second: a near-black structural shell holds the navigation and orientation; a pure-white content field holds the data. Cobalt appears exactly where instruments appear on a real flight deck — at the controls, the active indicators, the things you reach for — and nowhere else. The result is a UI that communicates authority and readiness without shouting.

The system's density is deliberate. Operators scan tables, not prose. Container codes and quantities are the content; the chrome wraps them and gets out of the way. Typography is a two-tier system: a geometric sans for interface language (labels, nav, headings) and a monospaced variant for codes, IDs, and quantities — so `CNT-00481` reads as a code string, not a label. No ambiguity about which text is data and which is navigation.

Motion is functional and fast. A state change confirms an action; a route transition orients. Nothing choreographs, nothing bounces, nothing delays a worker in gloves who needs the screen to respond immediately.

**This system explicitly rejects:** generic SaaS dashboard chrome (cream backgrounds, hero-metric grids, gradient brand bars); consumer app rounding and padding (this is not a phone experience); and enterprise gray (SAP-palette uniformity where every surface looks the same). The reference feeling is Vercel/Linear — crisp, calibrated, confident — applied to a floor-level operational context.

**Key Characteristics:**
- Near-black sidebar is architectural structure, not a brand statement
- Cobalt active states and primary actions: <10% of any given screen
- Monospaced treatment for all codes, IDs, and quantities
- Status badges carry both color AND a text label — never color alone
- Responsive motion only: state transitions, route changes, focus rings

---

## 2. Colors: The Instrument Palette

A restrained palette: near-neutral surfaces with a single cobalt anchor for actions and active state. Amber for status and warnings. The sidebar is near-black — its darkness is structural, not chromatic.

**Color strategy: Restrained.** The cobalt primary accounts for ≤10% of any given screen. The dark sidebar is architectural (a near-black at low chroma, not a saturated brand blue). Its rarity is the point.

### Primary
- **Instrument Cobalt** (`[oklch(~0.55 0.165 258)] — to be resolved during implementation`): Used exclusively for interactive primary actions, active navigation state, and selected/focus indicators. White text on fills. Never used decoratively; never used as a background tint.

### Secondary
- **Shift Amber** (`[oklch(~0.72 0.155 54)] — to be resolved during implementation`): Warning indicators, active shift badge, badge-level status markers that signal operational urgency. White or near-white text on fills. Distinct from cobalt in both hue (54° vs 258°) and lightness.

### Neutral
- **Content White** (`oklch(1.000 0.000 0)` — literal `#ffffff`): The content field background. Pure white, no warmth, no tint. The cobalt and amber read more saturated against pure white than against tinted neutral — which is the correct perceptual direction for an instrument UI.
- **Shell Black** (`[oklch(~0.11 0.010 258)] — to be resolved during implementation`): The sidebar and structural shell. Near-black with a trace of cobalt at low chroma (0.010) — barely perceptible, but cohesive. Not warm-black; not pure-neutral-black.
- **Ink** (`[oklch(~0.14 0.010 258)] — to be resolved during implementation`): Primary body text on white content field. ≥7:1 contrast required. Slight cobalt tint in the same hue family as Shell Black.
- **Muted** (`[oklch(~0.46 0.012 258)] — to be resolved during implementation`): Secondary labels, helper text, table meta. ≥4.5:1 contrast on Content White required. Must not fall into the L 0.45–0.72 + chroma < 0.08 zone that reads as mushroom-gray; keep chroma above 0.010 toward cobalt to stay readable and slightly branded.
- **Surface** (`[oklch(~0.97 0.005 258)] — to be resolved during implementation`): Panel backgrounds, table row alternates, card-like containers in the content field. Pulled toward Shell Black by 3–5% lightness.

### Named Rules

**The Instrument Rule.** Cobalt appears only where a control or active indicator would appear. A cobalt element that isn't interactive, active, or selected is a mistake.

**The Label Rule.** Status badges carry both color and a text label. `RECEIVED` in blue is readable by someone who is color-blind. Blue alone is not. Never ship a badge that relies on color as its sole communicator.

**The One-Tint Rule.** If the sidebar is dark and the content is white, no third surface tone may exist between them without a clear architectural reason. Card elevation in the content field uses `Surface` tint, not a new color.

---

## 3. Typography: Precision Two-Tier

**UI Font:** Geometric sans — Inter, Geist, or DM Sans (to be finalized at implementation). Priority: Inter for maximum legibility density at small sizes.

**Code/Data Font:** A monospaced variant — JetBrains Mono, Geist Mono, or system mono (to be finalized at implementation). Applied to all container codes, lot IDs, timestamps, and quantities.

**Character:** The pairing is utilitarian, not expressive. The geometric sans reads clinical at small weights but gains authority at medium (500) and bold (600). The monospace variant makes data scannable by visually separating it from interface text — you know immediately whether text is something to read or something to act on.

### Hierarchy

- **Display** (weight 600, `clamp(1.5rem, 3vw, 2rem)`, line-height 1.2): Page titles, modal headers. Letter-spacing ≥ -0.02em (never tighter). `text-wrap: balance`.
- **Headline** (weight 600, `1.125rem / 18px`, line-height 1.3): Section headings, card group titles. Sparse use — only where clear hierarchy is needed.
- **Title** (weight 500, `0.9375rem / 15px`, line-height 1.4): Sidebar nav labels, table column headers, form field labels. Uppercase with `0.04em` letter-spacing for column headers only.
- **Body** (weight 400, `0.875rem / 14px`, line-height 1.5): All prose, description text, error messages. Max 65ch line length in non-table contexts.
- **Label** (weight 500, `0.75rem / 12px`, letter-spacing `0.02em`): Badge text, status chips, metadata. Always paired with color from the Instrument Palette; never used as heading-level text.
- **Code** (monospace, weight 400, `0.8125rem / 13px`, line-height 1.4): Container codes (`CNT-00481`), profile IDs (`P-36`), quantities (`120 pcs`), timestamps (`08:32`). Renders in `Code Font`. Applied automatically via CSS class `ds-code` — not manually in every template.

### Named Rules

**The Two-Tier Rule.** Interface text uses the UI font. Data identifiers use the code font. No exceptions — mixing erases the scannability that makes this UI fast.

**The Label Ceiling Rule.** Uppercase with letter-spacing is permitted only for table column headers (Title role) and badge labels. Not for nav items, not for section headings, not as an eyebrow above every section. One uppercase register earns its place; two is decoration.

---

## 4. Elevation

This system is flat by default. Surfaces are differentiated by color value (`Content White` vs `Surface` tint vs `Shell Black`), not by shadow. The sidebar and content field are at the same z-plane visually; the dark-light contrast is sufficient to establish hierarchy without introducing a shadow stack.

The one exception is overlays (modals, dropdowns, command palette): these use a single diffuse ambient shadow to separate from the content field. No multiple shadow layers; no colored glows.

### Shadow Vocabulary

- **Overlay** (`0 8px 32px oklch(0 0 0 / 0.18)`): Applied only to floating layers that sit above the content field — dialogs, popover menus, command palette. Not applied to cards, panels, or sidebar.

### Named Rules

**The Flat-By-Default Rule.** Surfaces are flat at rest. The single overlay shadow appears only on elements that truly float above the content plane. Cards in the content field: no shadow, use `Surface` tint instead.

---

## 5. Components

No component code yet — pre-implementation seed. Re-run `$impeccable document` once Angular components are built to extract real snippets.

Component philosophy direction (to apply at build time):

**Buttons** — sharp radius (4px maximum, not pill, not zero). Primary: Instrument Cobalt fill, white text, weight 500. Hover: 8% darkened cobalt (stay in hue, drop L by ~0.04). Focus ring: 2px offset, Instrument Cobalt. Ghost variant: transparent fill, cobalt border at 1px, cobalt text — no box-shadow paired with border.

**Status Badges** — pill-shaped (full-radius). Color fill from Instrument Palette secondary or action-specific colors (Reception blue, Cut yellow, Scrap orange, Molding Output purple — specific OKLCH values to be locked at implementation). Always include text label. White text on saturated fills.

**Table Rows** — no row border between cells. Horizontal rule under header only (1px `Surface` tint). Row hover: `Surface` tint at 60% opacity over `Content White`. Selected row: left accent bar (2px Instrument Cobalt) + subtle `Surface` fill.

**Inputs** — 1px border in `Surface` (darker tint), no fill (transparent on white). Focus: border shifts to Instrument Cobalt, no additional shadow. Radius: 4px. Placeholder text must hit ≥4.5:1 against background — use `Muted` color, never lighter.

**Sidebar Navigation** — items at Body size, weight 500. Default: Muted color on Shell Black. Hover: white text, Shell Black tint 10% lighter. Active: white text + 2px left accent bar in Instrument Cobalt + subtle background lift (Shell Black 8% lighter). Role label: Label size, uppercase, muted cobalt on Shell Black — clearly subordinate to the logo.

---

## 6. Do's and Don'ts

### Do:
- **Do** use pure white (`oklch(1.000 0.000 0)`, literal `#ffffff`) as the content field background — never cream, sand, or warm-tinted neutrals.
- **Do** render all container codes, lot IDs, timestamps, and quantities in the code font (`ds-code`). These are data strings, not labels.
- **Do** keep status badges bilingual: color AND text label. WCAG AA for color-blind users is non-negotiable.
- **Do** keep primary button shadows off — a 1px cobalt border OR a cobalt fill, never both plus a wide drop shadow.
- **Do** limit cobalt to active states, primary actions, and selected indicators. Scanning for a cobalt element should tell you immediately that something is interactive or selected.
- **Do** use `text-wrap: balance` on h1–h2 headings to prevent awkward breaks.
- **Do** respect `@media (prefers-reduced-motion: reduce)` — transitions fall back to instant or a simple crossfade.
- **Do** ensure body text meets ≥4.5:1 contrast and placeholder text meets the same bar (not lower).

### Don't:
- **Don't** use cream, sand, beige, or warm-neutral backgrounds. The entire warm-neutral band (OKLCH L 0.84–0.97, C < 0.06, hue 40–100) is prohibited regardless of what the token is named.
- **Don't** use a `border-left` greater than 1px as a colored accent stripe on cards, callouts, or table rows. Use a full background tint or a left border that is part of the component structure (sidebar active item), never a decorative stripe.
- **Don't** pair `border: 1px solid X` + `box-shadow` with blur ≥16px on the same element. Pick one.
- **Don't** use `border-radius` larger than 8px on panels, tables, or input fields. Pill radius (full-round) is reserved for badges and chips only.
- **Don't** use gradient text (`background-clip: text` + gradient). Any emphasis is done through weight or size, not chromatic decoration.
- **Don't** use glassmorphism or backdrop-filter blur as a decorative surface treatment.
- **Don't** add an uppercase tracked eyebrow label above every section. One deliberate use in the sidebar role label is the limit.
- **Don't** make the sidebar feel like a SaaS dashboard nav — no icon + label cards, no rounded active pill backgrounds, no gradient fills on the sidebar itself.
- **Don't** design for phones. This is a desktop terminal UI. Touch target padding and large-thumb ergonomics are irrelevant. Density over comfort.
- **Don't** use the dull SAP/enterprise gray palette — uniform gray surfaces with no visual identity. The Shell Black sidebar and Instrument Cobalt accents are the identity.