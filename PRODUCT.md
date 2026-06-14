# Product

## Register

product

## Users

Factory floor workers operating across four roles: **OPERADOR** (production line, registers cuts and receptions during their shift), **SUPERVISOR** (approves receptions, monitors shift output, reads reports), **ADMIN** (manages catalogs, users, and full history), **CONSULTA** (read-only reports and history).

Context of use: industrial environment — loud, demanding, shift-paced. Users move between screens quickly. Gloves, ambient noise, and time pressure are real. The UI must be readable in a scan, not a study.

Job to be done: track every rubber profile unit through its lifecycle — Reception → Inventory → Cutting → Scrap → Molding Output — with zero ambiguity about quantity, container, and status at every step.

## Product Purpose

RubberTrace OPERATIONS is a manufacturing traceability system for rubber profile cutting. It exists so that every production movement — reception, cut, scrap, and molding output — is logged, attributed to a shift, and instantly visible to supervisors and admins.

Success means a supervisor can open the app at any moment and know exactly what happened this shift, who did it, and whether the quantities add up.

## Brand Personality

**Clear · Fast · Confident**

The interface does not make users think. It makes them act. Information is dense but organized — never cluttered. The visual language borrows from precision tools: tight type, restrained palette, status at a glance. It feels like software built by people who understand the floor.

Reference feeling: Vercel / Linear — crisp dark-light hybrid, tight typography, confident whitespace. Not decorative. Not playful. Not enterprise-gray.

## Anti-references

- **Generic SaaS dashboard**: no cream or sand backgrounds, no hero-metric card grids, no purple-gradient brand bars, no eyebrow-above-every-section scaffolding.
- **Consumer mobile app**: not rounded, not card-per-item, not tap-target-padded. Workers use desktop terminals, not phone screens.
- **Enterprise gray**: not the dull SAP / legacy ERP palette. Gray everything, no visual identity — this must feel like a tool people actually want to use.

## Design Principles

1. **Clarity above decoration** — every element earns its place. No chrome that doesn't carry information.
2. **Scan at a glance** — operators in a noisy environment read status and quantities first. Information hierarchy is structural, not a nice-to-have.
3. **Confidence through precision** — tight typography, controlled spacing. Rounded and bubbly signals consumer toy; sharp and measured signals professional instrument.
4. **Speed over ceremony** — no animation that delays task completion. Motion is reserved for orientation (route transitions, state changes), never for show.
5. **Data is the interface** — the numbers, statuses, and container codes are the design. Chrome wraps them; it never competes with them.

## Accessibility & Inclusion

WCAG 2.1 AA minimum throughout:
- Body text ≥ 4.5:1 contrast against background
- Large text (≥ 18px regular or ≥ 14px bold) ≥ 3:1
- All interactive elements keyboard-navigable
- `@media (prefers-reduced-motion: reduce)` respected — no gating of content visibility on animation completion
- Status badges must not rely on color alone — include a text label