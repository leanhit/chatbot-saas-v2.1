# Chatbot SaaS — Frontend

Vue 3 + Pinia + Vue Router frontend for the multi-tenant chatbot SaaS platform.

## Stack

- **Vue 3** (Composition API) + **Vue Router 4**
- **Pinia** for state management (`src/stores/`)
- **TailwindCSS** for styling
- Axios-based API layer (`src/api/`, `src/services/`)

## Project Structure

```
src/
├── api/            # API client modules
├── components/     # Reusable UI components
├── composables/    # Composition functions
├── locales/        # i18n
├── router/
│   ├── index.js    # Router instance + navigation guard
│   └── routes/     # Route modules by domain
│       ├── core.js       # auth, dashboard, notifications
│       ├── tenant.js     # tenant settings, members, agents, audit
│       ├── penny.js      # Penny bot management
│       ├── messaging.js  # chat + customer data
│       ├── routing.js    # queue, supervisor, escalation
│       ├── payment.js    # deposit, history (OWNER only)
│       ├── admin.js      # /admin/* (requiresSystemAdmin)
│       ├── analytics.js  # funnel, reports, journey
│       ├── training.js   # training library, certifications
│       └── customer.js   # profiles, feedback, VIP queue
├── stores/         # Pinia stores (authStore, tenant stores)
├── views/          # Page components
└── main.js
```

## Route Guards

Navigation guard in `src/router/index.js` enforces:

- `requiresAuth` — JWT token required
- `requiresOwner` — tenant OWNER role (payment routes)
- `requiresSystemAdmin` — system ADMIN/SYSTEM_ADMIN role (`/admin/*` routes)
- `skipTenantCheck` — routes that don't need an active tenant

## Development

```bash
npm install
npm run serve    # dev server
npm run build    # production build
```

## Notes

- This project was originally scaffolded from the Windzo admin template; the README has been updated to reflect the actual codebase.
- Backend: Spring Boot 3.4 / Java 21 — see `../backend`.
