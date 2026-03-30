# Design System

Source: `src/assets/main.css`

This is a pure CSS design system — no external CSS framework (no Tailwind, no Bootstrap, no component library). Every token, component class, and utility is defined in a single file and applied directly to HTML elements and Vue component templates.

---

## Design Philosophy

The primary user is a restaurant worker operating under time pressure, often with wet hands, on a mounted tablet or desktop screen in a kitchen environment. Every design decision follows from this constraint.

**Principles:**

- Clarity over cleverness — no decorative elements that don't carry information
- Large touch targets — minimum 44px interactive height across all buttons and interactive rows
- Instant status recognition — color communicates state before the user reads a word
- Fast scanning and legibility — high-contrast text, generous line-height, system font stack
- Flat design — no gradients on interactive elements; shadows are used sparingly and only at semantic scale steps (xs, sm, md, lg)
- WCAG AA minimum contrast — status colors have paired foreground/background tokens, never used standalone

---

## Layout Modes

The app shell supports two mutually exclusive rendering modes, toggled by `useLayoutStore.isTabletMode`.

### Desktop (default)

A fixed dark left sidebar sits alongside a light content area.

```
┌──────────────────────────────────────────┐
│  Sidebar (240px)  │  Content area         │
│  dark #0F172A     │  bg #F1F5F9           │
│                   │  padding: 2rem 2.5rem │
└──────────────────────────────────────────┘
```

- Sidebar width: `--sidebar-w: 240px`, collapses to `64px`
- Content area uses `margin-left: var(--sidebar-w)` and transitions on collapse via `--t-slow`
- When collapsed, text labels and the brand name fade to `opacity: 0` (still in DOM, pointer-events disabled)

### Tablet Simulator

A developer tool that wraps the entire app in an iPad-shaped frame, useful for previewing the tablet UI on a desktop monitor.

- Dark outer background: `#0D1320`
- Device frame: `834px wide`, `94vh` height, `28px` border-radius, double-ring box shadow
- Content scrolls inside the frame; bottom navigation replaces the sidebar
- Bottom tab bar: `--tabbar-h: 76px`, fixed to the bottom of the device frame
- Toggled by the floating `.tablet-toggle-btn` in the bottom-right corner of the viewport

---

## Color Tokens

All tokens are defined as CSS custom properties on `:root`.

### Brand

| Variable | Value | Usage |
|---|---|---|
| `--c-primary` | `#16A34A` | Primary actions, active states, green food-safety theme |
| `--c-primary-dark` | `#15803D` | Button hover state |
| `--c-primary-light` | `#F0FDF4` | Active sub-nav background |
| `--c-primary-mid` | `#DCFCE7` | Active sub-nav border |

### Status Palette

Each status color has five paired tokens: base, background, text, and border. This ensures color is never the only indicator — a danger badge always shows both a red background and dark-red text.

| Base variable | Value | bg | text | border |
|---|---|---|---|---|
| `--c-success` | `#16A34A` | `--c-success-bg: #F0FDF4` | `--c-success-text: #15803D` | `--c-success-border: #BBF7D0` |
| `--c-warning` | `#D97706` | `--c-warning-bg: #FFFBEB` | `--c-warning-text: #92400E` | `--c-warning-border: #FDE68A` |
| `--c-danger` | `#DC2626` | `--c-danger-bg: #FEF2F2` | `--c-danger-text: #991B1B` | `--c-danger-border: #FECACA` |
| `--c-info` | `#2563EB` | `--c-info-bg: #EFF6FF` | `--c-info-text: #1E40AF` | `--c-info-border: #BFDBFE` |

### Surfaces

| Variable | Value | Usage |
|---|---|---|
| `--c-bg` | `#F1F5F9` | Page background |
| `--c-surface` | `#FFFFFF` | Cards, modals, inputs |
| `--c-surface-2` | `#F8FAFC` | Hover states, subtle alternates |
| `--c-surface-3` | `#F1F5F9` | Progress bar track, ghost button hover |

### Text

| Variable | Value | Usage |
|---|---|---|
| `--c-text` | `#0F172A` | Primary body text |
| `--c-text-2` | `#475569` | Secondary / label text |
| `--c-text-3` | `#94A3B8` | Muted / placeholder / disabled |

### Borders

| Variable | Value |
|---|---|
| `--c-border` | `#E2E8F0` |
| `--c-border-2` | `#CBD5E1` |

### Sidebar (dark theme)

| Variable | Value |
|---|---|
| `--c-sidebar-bg` | `#0F172A` |
| `--c-sidebar-text` | `#94A3B8` |
| `--c-sidebar-hover` | `rgba(255,255,255,0.06)` |
| `--c-sidebar-active` | `rgba(22, 163, 74, 0.15)` |

### Section Tab Accent Colors

Each route has a unique accent color used in the bottom tab bar (active indicator and icon tint) and optionally as a theme accent on that section's content. This makes sections instantly recognizable.

| Variable | Value | Section |
|---|---|---|
| `--tab-oversikt` | `#6366F1` | Overview (indigo) |
| `--tab-fryser` | `#3B82F6` | Freezers (blue) |
| `--tab-kjoeleskap` | `#0EA5E9` | Fridges (cyan) |
| `--tab-generelt` | `#16A34A` | General checklists (green) |
| `--tab-avvik` | `#EF4444` | Deviations (red) |
| `--tab-graf` | `#8B5CF6` | Temperature charts (purple) |
| `--tab-opplaering` | `#F59E0B` | Training (amber) |
| `--tab-innstillinger` | `#64748B` | Settings (slate) |

### Shadows

Shadows are defined at semantic scale steps. Used sparingly — only on surfaces that float above the page plane.

| Variable | Value |
|---|---|
| `--shadow-xs` | `0 1px 2px rgba(0,0,0,0.04)` |
| `--shadow-sm` | `0 1px 3px rgba(0,0,0,0.08), 0 1px 2px rgba(0,0,0,0.04)` |
| `--shadow` | `0 4px 6px -1px rgba(0,0,0,0.07), 0 2px 4px -1px rgba(0,0,0,0.04)` |
| `--shadow-md` | `0 10px 15px -3px rgba(0,0,0,0.08), 0 4px 6px -2px rgba(0,0,0,0.03)` |
| `--shadow-lg` | `0 20px 25px -5px rgba(0,0,0,0.08), 0 10px 10px -5px rgba(0,0,0,0.03)` |

### Spacing / Radius

| Variable | Value |
|---|---|
| `--r-xs` | `4px` |
| `--r-sm` | `6px` |
| `--r` | `10px` |
| `--r-lg` | `14px` |
| `--r-xl` | `20px` |
| `--r-2xl` | `28px` |
| `--r-full` | `9999px` |

### Layout Dimensions

| Variable | Value |
|---|---|
| `--sidebar-w` | `240px` |
| `--tabbar-h` | `76px` |

### Transitions

| Variable | Value | Usage |
|---|---|---|
| `--t` | `0.15s ease` | Standard hover/focus transitions |
| `--t-slow` | `0.3s ease` | Sidebar collapse, modal fade |

---

## Typography

**Font stack:** `'Inter', system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif`

Inter is loaded as a web font; system-ui provides fallback. `-webkit-font-smoothing: antialiased` is applied globally.

| Element / Class | Size | Weight | Notes |
|---|---|---|---|
| `h1` | `1.375rem` | `700` | `letter-spacing: -0.02em`, `line-height: 1.25` |
| `h2` | `1.125rem` | `600` | `letter-spacing: -0.01em`, `line-height: 1.35` |
| `h3` | `0.9375rem` | `600` | |
| `h4` | `0.875rem` | `600` | |
| `.page-title` | `1.375rem` | `700` | Same as h1, used in page header regions |
| `.page-subtitle` | `0.875rem` | — | Color `--c-text-2` |
| `.section-title` | `0.9375rem` | `600` | `margin-bottom: 0.75rem` |
| body / default | `0.875rem` | `400` | `line-height: 1.5` |

---

## Component Classes

### `.card`

A white surface container with a light border and rounded corners. The foundational grouping element.

```css
background: var(--c-surface);        /* #fff */
border: 1px solid var(--c-border);
border-radius: var(--r-lg);          /* 14px */
padding: 1.25rem;
box-shadow: var(--shadow-xs);
```

Adjacent `.card + .card` elements have `margin-top: 0.75rem` applied automatically.

`.card-header` is a flex row (`justify-content: space-between`) for placing a title and an action button at opposite ends.

---

### `.btn` and variants

Base class for all buttons. All buttons are `inline-flex`, vertically centered, and use `font-family: inherit`. Disabled state applies `opacity: 0.5` and `cursor: not-allowed`.

| Class | Background | Text | Border | Hover |
|---|---|---|---|---|
| `.btn-primary` | `--c-primary` | `#fff` | none | `--c-primary-dark` |
| `.btn-secondary` | `--c-surface` | `--c-text` | `1.5px --c-border` | `--c-surface-2` bg, `--c-border-2` border |
| `.btn-danger` | `--c-danger-bg` | `--c-danger-text` | `1.5px --c-danger-border` | `#FEE2E2` |
| `.btn-ghost` | `transparent` | `--c-text-2` | none | `--c-surface-3` |

Size modifiers:

| Class | Padding | Font size | Min-height |
|---|---|---|---|
| `.btn-sm` | `0.375rem 0.75rem` | `0.8125rem` | — |
| default | `0.5625rem 1rem` | `0.875rem` | ~36px |
| `.btn-lg` | `0.875rem 1.5rem` | `1rem` | `52px` |

All variants satisfy the 44px touch target minimum at default size; `.btn-lg` adds extra padding for primary CTAs on the tablet view.

---

### `.badge` and variants

Pill-shaped inline label for status, categories, and counts.

```css
padding: 0.225rem 0.6rem;
border-radius: var(--r-full);       /* pill */
font-size: 0.75rem;
font-weight: 600;
```

| Class | Background | Text color |
|---|---|---|
| `.badge-success` | `--c-success-bg` | `--c-success-text` |
| `.badge-warning` | `--c-warning-bg` | `--c-warning-text` |
| `.badge-danger` | `--c-danger-bg` | `--c-danger-text` |
| `.badge-info` | `--c-info-bg` | `--c-info-text` |
| `.badge-neutral` | `--c-surface-3` | `--c-text-2` |
| `.badge-purple` | `#EDE9FE` | `#5B21B6` |

---

### `.status-row`

A horizontally-laid-out list row used for temperature readings, unit summaries, and any item with a name on the left and a status badge on the right.

```css
display: flex;
align-items: center;
justify-content: space-between;
padding: 0.75rem 1rem;
border: 1px solid var(--c-border);
border-radius: var(--r);
margin-bottom: 0.5rem;
```

State variants:
- `.status-row.is-alert` — danger-tinted background and border
- `.status-row.is-warning` — warning-tinted background and border

---

### `.checklist-item`

A single checkbox row within a checklist, designed for tap interaction.

```css
display: flex;
align-items: center;
gap: 0.625rem;
padding: 0.375rem 0;
cursor: pointer;
```

- `.checklist-item:hover` adds a subtle background and shifts `padding-left` for visual feedback
- `.checklist-item.is-done` applies `color: --c-text-3` and `text-decoration: line-through`

The `input[type="checkbox"]` inside uses `accent-color: var(--c-primary)` so the checkmark inherits the brand green.

---

### `.alert-banner`

Full-width inline alert strip, used above content areas to surface active temperature alarms or compliance warnings.

```css
display: flex;
align-items: flex-start;
gap: 0.75rem;
padding: 0.875rem 1rem;
border-radius: var(--r);
margin-bottom: 0.625rem;
```

| Variant | Background | Text | Border |
|---|---|---|---|
| `.alert-banner.danger` | `--c-danger-bg` | `--c-danger-text` | `--c-danger-border` |
| `.alert-banner.warning` | `--c-warning-bg` | `--c-warning-text` | `--c-warning-border` |
| `.alert-banner.info` | `--c-info-bg` | `--c-info-text` | `--c-info-border` |

---

### `.progress-bar` and `.progress-fill`

Thin horizontal progress indicator (5px height) used in checklist completion summaries.

```css
.progress-bar {
  height: 5px;
  background: var(--c-surface-3);
  border-radius: var(--r-full);
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: var(--r-full);
  transition: width 0.4s ease;
}
```

Fill state modifier classes:

| Class | Color |
|---|---|
| `.progress-fill.full` | `--c-success` |
| `.progress-fill.partial` | `--c-warning` |
| `.progress-fill.empty` | `--c-surface-3` (invisible) |

---

### `.form-group`, `.form-grid-2`, `.form-grid-3`

Form layout primitives.

- `.form-group` — a flex column with `gap: 0.375rem`, for a single label + input pair
- `.form-grid-2` — two-column grid (`1fr 1fr`), `gap: 0.75rem`
- `.form-grid-3` — three-column grid (`1fr 1fr 1fr`), `gap: 0.75rem`

All `input`, `select`, and `textarea` elements have globally-scoped styles:
- `border: 1.5px solid --c-border`, `border-radius: --r-sm`
- Focus ring: `border-color: --c-primary`, `box-shadow: 0 0 0 3px rgba(22,163,74,0.14)`
- `width: 100%` so they fill their grid cell

`label` elements are globally styled at `0.75rem / 500` weight in `--c-text-2`.

---

### `.modal`, `.modal-header`, `.modal-body`, `.modal-footer`

Modal panel displayed inside `.modal-backdrop`.

`.modal-backdrop` — fixed fullscreen overlay with `rgba(15,23,42,0.55)` tint and `backdrop-filter: blur(2px)`, `z-index: 9000`.

`.modal`:
```css
background: var(--c-surface);
border-radius: var(--r-xl);         /* 20px */
padding: 1.75rem;
max-width: 480px;
box-shadow: var(--shadow-lg);
animation: modal-in 0.2s ease;
```

`.modal-header` — flex row `justify-content: space-between` for title and close button; `margin-bottom: 1.25rem`.

`.modal-footer` — flex row `justify-content: flex-end`, `gap: 0.625rem`, `border-top: 1px solid --c-border`, `padding-top: 1.25rem`, `margin-top: 1.5rem`.

There is no `.modal-body` class — body content sits directly inside `.modal` between header and footer.

---

### `.mod-badge.ik-mat` and `.mod-badge.ik-alkohol`

Module affiliation badges. Used to indicate which regulatory module a checklist or document belongs to.

```css
.mod-badge {
  padding: 0.25rem 0.75rem;
  border-radius: var(--r-full);
  font-size: 0.75rem;
  font-weight: 600;
}

.mod-badge.ik-mat     { background: --c-info-bg;  color: --c-info-text; }
.mod-badge.ik-alkohol { background: #EDE9FE;      color: #5B21B6; }
```

---

### `.section-title`

A heading used to label groups of content within a card or page section.

```css
font-size: 0.9375rem;
font-weight: 600;
color: var(--c-text);
margin-bottom: 0.75rem;
```

---

### `.divider`

A 1px horizontal rule for separating regions within a card.

```css
height: 1px;
background: var(--c-border);
margin: 1rem 0;
```

---

### `.avatar`

A circular container for user initials, typically `36px × 36px`.

```css
width: 36px;
height: 36px;
border-radius: 50%;
display: flex;
align-items: center;
justify-content: center;
font-weight: 700;
font-size: 0.75rem;
flex-shrink: 0;
```

Background and text color are set inline per user (defined in `SettingsUser.colorBg` / `SettingsUser.colorText`).

---

## Utility Classes

### Layout

| Class | CSS |
|---|---|
| `.flex` | `display: flex` |
| `.flex-col` | `display: flex; flex-direction: column` |
| `.flex-1` | `flex: 1` |
| `.items-center` | `align-items: center` |
| `.items-start` | `align-items: flex-start` |
| `.justify-between` | `justify-content: space-between` |
| `.justify-end` | `justify-content: flex-end` |
| `.gap-2` | `gap: 0.5rem` |
| `.gap-3` | `gap: 0.75rem` |
| `.gap-4` | `gap: 1rem` |
| `.min-w-0` | `min-width: 0` — required on flex children with `.truncate` |

### Spacing

| Class | Value |
|---|---|
| `.mb-1` | `margin-bottom: 0.25rem` |
| `.mb-2` | `margin-bottom: 0.5rem` |
| `.mb-3` | `margin-bottom: 0.75rem` |
| `.mb-4` | `margin-bottom: 1rem` |
| `.mb-6` | `margin-bottom: 1.5rem` |
| `.mt-1` | `margin-top: 0.25rem` |
| `.mt-2` | `margin-top: 0.5rem` |
| `.mt-3` | `margin-top: 0.75rem` |
| `.mt-4` | `margin-top: 1rem` |

### Text

| Class | CSS |
|---|---|
| `.text-sm` | `font-size: 0.875rem` |
| `.text-xs` | `font-size: 0.75rem` |
| `.text-muted` | `color: var(--c-text-3)` |
| `.text-success` | `color: var(--c-success)` |
| `.text-warning` | `color: var(--c-warning)` |
| `.text-danger` | `color: var(--c-danger)` |
| `.text-info` | `color: var(--c-info)` |
| `.font-medium` | `font-weight: 500` |
| `.font-semibold` | `font-weight: 600` |
| `.font-bold` | `font-weight: 700` |
| `.truncate` | `overflow: hidden; text-overflow: ellipsis; white-space: nowrap` |

### Other

| Class | CSS |
|---|---|
| `.hidden` | — (not defined globally; handled per-component with `v-show` / `v-if`) |

---

## Transitions and Animations

### Sidebar collapse/expand

Driven by `--t-slow: 0.3s ease`. The `.sidebar` element transitions `width`; text labels, the brand name, group labels, and user info all transition `opacity` at the same duration. This creates a simultaneous shrink-and-fade effect.

### Modal entrance

```css
@keyframes modal-in {
  from { opacity: 0; transform: scale(0.96) translateY(8px); }
  to   { opacity: 1; transform: scale(1) translateY(0); }
}
```

Applied to `.modal` automatically on mount. Duration: `0.2s ease`.

### Vue route / view transitions

Two named transitions are available for use with `<Transition>` or `<RouterView>`:

- **`.fade-*`** — opacity fade, `0.15s ease`
- **`.slide-*`** — opacity + `translateX(12px)` slide in from the right, leave slides left, `0.2s ease`

### Standard hover states

All interactive elements (`.btn`, `.sidebar-item`, `.tab-bar-item`, `.checklist-item`, `.sub-nav-item`, `.sidebar-toggle`) transition via `--t: 0.15s ease`. This keeps micro-interactions consistent and snappy.

### Progress bar fill

`.progress-fill` transitions `width` at `0.4s ease`, giving a smooth fill animation when completion percentages update.

---

## Accessibility

- **Focus rings**: All `input`, `select`, and `textarea` elements display a green focus ring (`box-shadow: 0 0 0 3px rgba(22,163,74,0.14)`) on `:focus`. Interactive elements in Vue components should use `:focus-visible` where appropriate.
- **Semantic HTML**: Pages use `<main>`, `<nav>`, `<section>`, `<form>`, `<label>`, and `<button>` elements. The sidebar uses `<nav>` and individual navigation links use anchor elements or buttons.
- **Icon-only buttons**: Require an explicit `aria-label` attribute (e.g., the sidebar collapse toggle, modal close button).
- **Active navigation**: The active sidebar item and active tab bar item both use `aria-current="page"`.
- **Color is not the sole status indicator**: Every badge applies both a background color and a contrasting text color. Alert banners include an icon alongside the colored background. Checklist completion uses both strikethrough text and muted color together.
- **Keyboard navigation**: All interactive elements are reachable via Tab. Modals trap focus while open (implemented in component logic, not CSS).
- **Touch targets**: `.btn` default height is approximately 36px; `.btn-lg` minimum height is `52px`. `.status-row` and `.checklist-item` padding ensures at least 44px tap height in practice. Tab bar items are full-height of the `76px` tab bar.
