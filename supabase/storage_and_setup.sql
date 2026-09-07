-- ==========================================================
-- PULSE SHORT-VIDEO PLATFORM: STORAGE BUCKETS & POLICIES
-- ==========================================================

-- 1. Create Storage Buckets
INSERT INTO storage.buckets (id, name, public) 
VALUES 
    ('avatars', 'avatars', true),
    ('videos', 'videos', true),
    ('thumbnails', 'thumbnails', true),
    ('chat-media', 'chat-media', false),
    ('sounds', 'sounds', true)
ON CONFLICT (id) DO NOTHING;

-- 2. Storage Policies for Avatars
CREATE POLICY "Public Read Avatars" ON storage.objects
    FOR SELECT USING (bucket_id = 'avatars');

CREATE POLICY "Users Upload Own Avatar" ON storage.objects
    FOR INSERT WITH CHECK (
        bucket_id = 'avatars' 
        AND auth.role() = 'authenticated'
    );

-- 3. Storage Policies for Videos
CREATE POLICY "Public Read Videos" ON storage.objects
    FOR SELECT USING (bucket_id = 'videos');

CREATE POLICY "Users Upload Videos" ON storage.objects
    FOR INSERT WITH CHECK (
        bucket_id = 'videos' 
        AND auth.role() = 'authenticated'
    );

CREATE POLICY "Users Delete Own Videos" ON storage.objects
    FOR DELETE USING (
        bucket_id = 'videos' 
        AND (auth.uid() = owner OR public.get_admin_role(auth.uid()) IN ('super_admin', 'admin'))
    );

-- 4. Storage Policies for Thumbnails
CREATE POLICY "Public Read Thumbnails" ON storage.objects
    FOR SELECT USING (bucket_id = 'thumbnails');

CREATE POLICY "Users Upload Thumbnails" ON storage.objects
    FOR INSERT WITH CHECK (
        bucket_id = 'thumbnails' 
        AND auth.role() = 'authenticated'
    );

-- 5. Storage Policies for Chat Media (Private)
CREATE POLICY "Members Read Chat Media" ON storage.objects
    FOR SELECT USING (
        bucket_id = 'chat-media'
        AND auth.role() = 'authenticated'
    );

CREATE POLICY "Members Upload Chat Media" ON storage.objects
    FOR INSERT WITH CHECK (
        bucket_id = 'chat-media'
        AND auth.role() = 'authenticated'
    );

-- 6. Storage Policies for Sounds
CREATE POLICY "Public Read Sounds" ON storage.objects
    FOR SELECT USING (bucket_id = 'sounds');

CREATE POLICY "Admins or Creators Upload Sounds" ON storage.objects
    FOR INSERT WITH CHECK (
        bucket_id = 'sounds'
        AND auth.role() = 'authenticated'
    );

-- ==========================================================
-- SUPER ADMIN PROVISIONING SCRIPT FOR yourtasin3@gmail.com
-- ==========================================================
-- Run this SQL in the Supabase SQL Editor once yourtasin3@gmail.com signs up:
CREATE OR REPLACE PROCEDURE public.grant_initial_super_admin(target_email TEXT DEFAULT 'yourtasin3@gmail.com')
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    target_user_id UUID;
BEGIN
    SELECT id INTO target_user_id FROM auth.users WHERE email = target_email LIMIT 1;
    
    IF target_user_id IS NULL THEN
        RAISE EXCEPTION 'User % not found in auth.users! Please sign up first via email.', target_email;
    END IF;

    INSERT INTO public.admin_users (user_id, email, role, is_active)
    VALUES (target_user_id, target_email, 'super_admin', true)
    ON CONFLICT (user_id) 
    DO UPDATE SET role = 'super_admin', is_active = true, updated_at = now();

    -- Also update profile to verified
    UPDATE public.profiles SET verified = true WHERE id = target_user_id;

    RAISE NOTICE 'Successfully granted super_admin role to % (ID: %)', target_email, target_user_id;
END;
$$;
