# Pulse Platform - Supabase & Backend Setup Guide

## 1. Supabase Project Setup
1. Create a project at [supabase.com](https://supabase.com).
2. Navigate to **SQL Editor**.
3. Copy and run the contents of `/supabase/schema.sql`.
4. Copy and run the contents of `/supabase/storage_and_setup.sql`.

## 2. Initial Super Admin Setup (yourtasin3@gmail.com)
1. Sign up the user account with email `yourtasin3@gmail.com` in Supabase Auth (or through the Pulse app User signup screen).
2. Go to **SQL Editor** and run:
   ```sql
   CALL public.grant_initial_super_admin('yourtasin3@gmail.com');
   ```
3. Or manually insert:
   ```sql
   INSERT INTO public.admin_users (user_id, email, role, is_active)
   SELECT id, email, 'super_admin', true
   FROM auth.users
   WHERE email = 'yourtasin3@gmail.com'
   ON CONFLICT (user_id) DO UPDATE SET role = 'super_admin', is_active = true;
   ```
4. Now `yourtasin3@gmail.com` can log into the Pulse Admin Portal with their credentials!

## 3. Environment Variables
Add the following to `.env`:
```env
SUPABASE_URL=https://your-project-id.supabase.co
SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```
*(Pulse also supports in-app dynamic configuration from the Admin portal or User Settings so you can test seamlessly with or without setting the build-time `.env`!)*

## 4. Live Streaming Architecture Specification
Supabase is a database, storage, and authentication platform and does not natively transcode or broadcast RTMP/WebRTC live video streams. 
To implement production live streaming:
1. **Streaming Provider**: Use **Cloudflare Stream**, **Mux**, or **Livepeer**.
2. **Workflow**:
   - Creator requests stream: App calls Supabase Edge Function `create-live-stream`.
   - Edge Function calls Mux/Cloudflare API with secret credentials to create a Broadcast Live Stream and returns RTMP Stream Key + HLS Playback URL.
   - Creator broadcasts video from device using RTMP SDK (e.g., `rtmp-rtsp-stream-client-java`).
   - Viewers play the HLS stream URL (`.m3u8`) in Pulse using standard video player.
   - Realtime chat & viewer count: Uses Supabase Realtime channel `stream:{id}:chat`.
3. An integration interface is provided in Pulse under `com.example.data.LiveStreamService`.
