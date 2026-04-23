Purpose: Document how to run `user-interface-service` locally for development.
Last updated: 2026-04-22

## Option A: Docker Compose

From repository root:

```bash
docker compose up -d
```

UI will be available at `http://localhost:4200`.

## Option B: Angular dev server

From `user-interface-service/`:

```bash
npm install
npm start
```

This runs `ng serve` (see `package.json` scripts) and serves on `http://localhost:4200`.

