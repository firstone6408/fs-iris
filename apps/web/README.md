# apps/web

Web UI for FS-Iris — built with Next.js 15 (App Router), React 19, TypeScript, and Tailwind CSS 4.

---

## Stack

| | |
|---|---|
| Framework | Next.js 15 (App Router) |
| Language | TypeScript 5 |
| Styling | Tailwind CSS 4 |
| Database | Prisma 6 + MySQL |
| Validation | Zod |
| Package manager | pnpm |

---

## Setup

```bash
pnpm install
```

Copy the environment file and fill in the values:

```bash
cp .env.example .env
```

```env
DATABASE_URL="mysql://user:password@localhost:3306/iris"
NEXT_PUBLIC_BASE_URL="http://localhost:3000"
```

Run database migrations:

```bash
pnpm prisma-migrate:dev
```

---

## Development

```bash
pnpm dev
```

Open [http://localhost:3000](http://localhost:3000)

---

## Scripts

| Script | Description |
|---|---|
| `pnpm dev` | Start development server |
| `pnpm build` | Build for production |
| `pnpm start` | Start production server |
| `pnpm lint` | Run ESLint |
| `pnpm prisma-migrate:dev` | Run migrations (development) |
| `pnpm prisma-migrate:deploy` | Run migrations (production) |
| `pnpm prisma-generate:schema` | Regenerate Prisma client |
