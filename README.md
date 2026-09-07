# Pulse — Short-Video Social Media Platform

A production-ready short-video social media platform featuring an **Immersive UI**, high-performance vertical video feed, interactive social engagements, and a dedicated **Admin Control Center**, all unified on a single **Supabase** backend with Row Level Security (RLS).

---

## 1. Project Overview

**Pulse** is a short-video social network built natively with modern Android Jetpack Compose and powered by Supabase (PostgreSQL, Supabase Auth, Supabase Storage, and Realtime). The platform comprises two unified operational layers:

1. **User Application**: A vertical video discovery feed, creator tools (camera recording, filters, sound library, video trimmer), comments engine, direct messaging, hashtag explore, profile customization, and social interaction graph.
2. **Admin Control Center**: A role-based administration portal providing live platform metrics, user moderation (banning/suspension), video content review & takedowns, verification badge approvals, sound/hashtag management, broadcast notifications, and tamper-evident audit logs.

---

## 2. Features

### User App
- **Infinite Vertical Video Feed**: Native vertical pager with video player controls, double-tap like animations, bookmarking, and creator follow toggles.
- **Sound System**: Rotating vinyl disk animation, audio tracks with creator attribution, usage count, and track selection.
- **Interactive Comments**: Bottom sheet comment drawer with likes, timestamped threads, and real-time count badges.
- **Creator Studio (Create Screen)**:
  - Multi-aspect recording simulation & camera integration.
  - Video trimmer and speed presets (0.5x, 1x, 2x, 3x).
  - Visual filter preview (Cyberpunk, Retro, Monochrome, Warm Neon).
  - Hashtag pill insertion and caption composer.
- **Discover & Search**:
  - Live search for creators, hashtags, and sounds.
  - Trending hashtag discovery chips with usage counts.
  - Featured creator spotlight cards with quick-follow buttons.
- **Direct Messaging & Activity Inbox**:
  - Real-time conversation threads with 1-on-1 messaging.
  - Notifications hub for likes, new followers, mentions, and system broadcasts.
- **Profile Hub**:
  - Creator statistics (Followers, Following, Total Likes).
  - Tabbed video showcases (Published videos, Liked videos, Bookmarks).
  - In-app profile editor with avatar and bio management.

### Admin Control Center
- **RBAC Security**: Granular role hierarchy (`super_admin`, `admin`, `moderator`, `support`).
- **Real-Time Analytics Dashboard**: Live metrics for Total Users, Active Videos, Content Reports, and Pending Verifications.
- **Video Moderation**: Review pending uploads, flag violations, approve or remove content with moderation audit logs.
- **User Account Management**: Ban/unban accounts, suspend access, toggle manual verification badges.
- **Verification Portal**: Review identity applications with external portfolio links and issue blue badges.
- **Content Moderation & Reports**: Resolve community complaints across categories (Spam, Harassment, Hate, Copyright).
- **Audio & Hashtags Management**: Publish and feature official sound tracks and trending hashtags.
- **System Broadcasts**: Push urgent system-wide announcements to all user inboxes.
- **Audit Logging**: Immutable action logs recording every administrative action with timestamp, admin ID, and metadata.

---

## 3. Architecture

```
                       ┌──────────────────────────────┐
                       │       PULSE PLATFORM         │
                       └──────────────┬───────────────┘
                                      │
              ┌───────────────────────┴──────────────────────┐
              ▼                                              ▼
   ┌──────────────────────┐                       ┌──────────────────────┐
   │       USER APP       │                       │     ADMIN PANEL      │
   │  - Feed / Creator    │                       │  - Dashboard/Audit   │
   │  - Discover / Inbox  │                       │  - User/Video Ops    │
   │  - Profile / Chat    │                       │  - Verification/Tags │
   └──────────┬───────────┘                       └──────────┬───────────┘
              │                                              │
              │         Unified Supabase API & Auth          │
              └───────────────────────┬──────────────────────┘
                                      ▼
             ┌────────────────────────────────────────────────┐
             │               SUPABASE BACKEND                 │
             │  - PostgreSQL 15 with Row Level Security (RLS) │
             │  - Supabase Auth (JWT & Email)                 │
             │  - Storage Buckets (Videos, Avatars, Sounds)   │
             │  - Edge Functions (Content Moderation)         │
             └────────────────────────────────────────────────┘
```

---

## 4. User App

The User App runs on Android 8.0+ (API 26+) with:
- **Framework**: Kotlin & Jetpack Compose (Material 3)
- **State Management**: Reactive MVVM via Kotlin Coroutines & `StateFlow`
- **Networking**: Supabase Kotlin Client / REST API
- **UI Components**: `VerticalPager`, `ModalBottomSheetLayout`, Custom Canvas Glassmorphism, Dynamic Gradients

---

## 5. Admin Panel

The Admin Panel is integrated securely within the application suite and gated behind strict server-side authorization:
- Accessible via the in-app Admin Portal route (`admin_portal`).
- **Initial Super Admin**: `yourtasin3@gmail.com`
- Unauthenticated or unauthorized users are blocked at the RLS database layer.
- All actions generate entries in `public.admin_audit_logs`.

---

## 6. Supabase Setup

### Step 1: Create a Supabase Project
1. Go to [https://supabase.com](https://supabase.com) and create a new project.
2. Note your **Project URL** (`https://<project-id>.supabase.co`) and **Public Anon Key** (`ey...`) from **Settings > API**.

### Step 2: Run Database Migrations
Navigate to the **SQL Editor** in your Supabase dashboard and execute the migration files in order:
1. `supabase/migrations/20260907000001_initial_schema.sql` (Creates all tables, constraints, indexes, triggers, and RLS policies).
2. `supabase/migrations/20260907000002_storage_and_buckets.sql` (Provisions storage buckets and access policies).
3. `supabase/seed/seed.sql` (Optional: Seeds default hashtags, sounds, and settings).

Alternatively, if using the Supabase CLI:
```bash
supabase login
supabase link --project-ref <your-project-ref>
supabase db push
```

---

## 7. Database Migration Setup

Migrations are version-controlled under `supabase/migrations/`:
- `20260907000001_initial_schema.sql`: Core schema for `profiles`, `videos`, `comments`, `likes`, `follows`, `messages`, `admin_users`, `admin_audit_logs`, and `app_settings`.
- `20260907000002_storage_and_buckets.sql`: Storage buckets (`avatars`, `videos`, `thumbnails`, `sounds`, `chat-media`).

---

## 8. Storage Setup

The following storage buckets are created automatically by migration `002`:
- `videos`: Public read, authenticated write (Max 100MB).
- `thumbnails`: Public read, authenticated write.
- `avatars`: Public read, authenticated write.
- `sounds`: Public read, admin/creator write.
- `chat-media`: Private read/write restricted to conversation participants.

---

## 9. Authentication Setup

Supabase Auth handles user identity:
1. In the Supabase Dashboard, go to **Authentication > Providers** and ensure **Email** is enabled.
2. Under **URL Configuration**, set your redirect URI or keep default.
3. Every new user signup triggers `public.handle_new_user()`, which automatically generates a synchronized record in `public.profiles`.

---

## 10. Admin Setup & First-Time Super Admin Access

The system assigns `yourtasin3@gmail.com` as the initial Super Admin:

1. Sign up or create the account with email `yourtasin3@gmail.com` in your Supabase project (via app signup or Supabase Auth dashboard).
2. Execute the provisioning procedure in the **Supabase SQL Editor**:
   ```sql
   CALL public.grant_initial_super_admin('yourtasin3@gmail.com');
   ```
3. This assigns the `super_admin` role in `public.admin_users` and marks the profile as verified.
4. Log in through the Admin Portal inside the application.

> **Security Note**: Admin passwords are never committed to git. Access is validated through Supabase Auth tokens combined with PostgreSQL RLS `get_admin_role(auth.uid())`.

---

## 11. Environment Variables

Create a local `.env` file from `.env.example`:

```bash
cp .env.example .env
```

Set your configuration:

```env
# Supabase Production Backend Configuration
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your-anon-key-here

# Optional AI Features
# GEMINI_API_KEY=your-gemini-api-key
```

> **IMPORTANT**:
> - Never commit `.env` or any file containing private keys.
> - Never expose `service_role` keys in client-side code.

---

## 12. Local Development

### Prerequisites
- Android Studio Iguana / Jellyfish or higher (or command-line Gradle)
- JDK 17+
- Node.js 18+ (for Supabase CLI / linting scripts)

### Running the App
```bash
# Build the debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest
```

---

## 13. Build & CI

Build scripts in `package.json`:
- `npm run dev`: Assembles debug build.
- `npm run build`: Assembles release & debug APKs.
- `npm run test`: Executes unit tests.
- `npm run lint`: Validates code formatting and linting.

---

## 14. GitHub Setup Instructions

Follow these exact steps to push this project to your GitHub repository:

```bash
# 1. Initialize git if not already initialized
git init

# 2. Stage all files
git add .

# 3. Create initial production commit
git commit -m "Initial production release: Pulse Short-Video Platform"

# 4. Set main branch
git branch -M main

# 5. Link your GitHub repository (replace with your actual repository URL)
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPOSITORY_NAME.git

# 6. Push to GitHub
git push -u origin main
```

---

## 15. Deployment

### Mobile App Deployment (Google Play / Direct APK)
1. **GitHub Actions**: Every push to `main` triggers `.github/workflows/ci.yml` to compile the app and publish artifacts.
2. Download the compiled APK from the GitHub Actions run summary.
3. For Play Store release, configure your signing keystore via GitHub Secrets (`RELEASE_KEYSTORE_BASE64`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`).

### Supabase Edge Functions Deployment
```bash
# Deploy moderation edge function
supabase functions deploy moderate-video --project-ref <your-project-ref>
```

---

## 16. Troubleshooting

| Issue | Cause | Solution |
|---|---|---|
| `Video upload fails` | Storage bucket missing or size limit exceeded | Verify `videos` bucket exists and RLS policy allows authenticated upload. |
| `Admin access denied` | User email not linked in `admin_users` | Execute `CALL public.grant_initial_super_admin('yourtasin3@gmail.com');` in SQL Editor. |
| `Likes/Follows count not updating` | PostgreSQL triggers disabled | Ensure migrations `001` executed completely with triggers enabled. |
| `Build fails on CI` | Missing secrets | Add `SUPABASE_URL` and `SUPABASE_ANON_KEY` to **GitHub Repository Secrets**. |

---

## 17. Security Notes
- **Row Level Security (RLS)**: Strictly enforced across all 14 database tables.
- **Zero Hardcoded Secrets**: All backend endpoints and keys are injected via environment variables and BuildConfig.
- **Zero-Trust Admin Authorization**: Role validation is performed at the database level (`get_admin_role()`), preventing client-side spoofing.
- **Sanitized Media**: Video and audio uploads are restricted to supported MIME types with size limits enforced by Supabase Storage.
