-- ==========================================================
-- PULSE SHORT-VIDEO SOCIAL MEDIA PLATFORM
-- PRODUCTION SUPABASE POSTGRESQL SCHEMA & RLS POLICIES
-- Migration: 20260907000001_initial_schema.sql
-- ==========================================================

-- Enable essential extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ==========================================================
-- 1. PROFILES TABLE (Syncs with auth.users)
-- ==========================================================
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    username TEXT UNIQUE NOT NULL,
    display_name TEXT NOT NULL,
    email TEXT,
    avatar TEXT,
    bio TEXT DEFAULT '',
    followers_count INT DEFAULT 0,
    following_count INT DEFAULT 0,
    likes_count INT DEFAULT 0,
    verified BOOLEAN DEFAULT false,
    private_account BOOLEAN DEFAULT false,
    is_banned BOOLEAN DEFAULT false,
    is_suspended BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_profiles_username ON public.profiles(username);
CREATE INDEX IF NOT EXISTS idx_profiles_created ON public.profiles(created_at DESC);

-- ==========================================================
-- 2. SOUNDS TABLE
-- ==========================================================
CREATE TABLE IF NOT EXISTS public.sounds (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    artist TEXT NOT NULL,
    cover TEXT,
    audio_url TEXT NOT NULL,
    usage_count INT DEFAULT 0,
    is_featured BOOLEAN DEFAULT false,
    is_disabled BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_sounds_usage ON public.sounds(usage_count DESC);
CREATE INDEX IF NOT EXISTS idx_sounds_featured ON public.sounds(is_featured);

-- ==========================================================
-- 3. VIDEOS TABLE
-- ==========================================================
CREATE TABLE IF NOT EXISTS public.videos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    video_url TEXT NOT NULL,
    thumbnail_url TEXT NOT NULL,
    caption TEXT DEFAULT '',
    description TEXT DEFAULT '',
    hashtags TEXT[] DEFAULT '{}',
    sound_id UUID REFERENCES public.sounds(id) ON DELETE SET NULL,
    visibility TEXT DEFAULT 'public' CHECK (visibility IN ('public', 'followers', 'private')),
    allow_comments BOOLEAN DEFAULT true,
    allow_download BOOLEAN DEFAULT true,
    views_count INT DEFAULT 0,
    likes_count INT DEFAULT 0,
    comments_count INT DEFAULT 0,
    shares_count INT DEFAULT 0,
    saves_count INT DEFAULT 0,
    moderation_status TEXT DEFAULT 'approved' CHECK (moderation_status IN ('approved', 'pending', 'rejected', 'hidden')),
    is_featured BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_videos_user ON public.videos(user_id);
CREATE INDEX IF NOT EXISTS idx_videos_status_created ON public.videos(moderation_status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_videos_ranking ON public.videos(likes_count DESC, views_count DESC, created_at DESC);

-- ==========================================================
-- 4. HASHTAGS & JUNCTION
-- ==========================================================
CREATE TABLE IF NOT EXISTS public.hashtags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT UNIQUE NOT NULL,
    usage_count INT DEFAULT 0,
    is_featured BOOLEAN DEFAULT false,
    is_hidden BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.video_hashtags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    video_id UUID NOT NULL REFERENCES public.videos(id) ON DELETE CASCADE,
    hashtag_id UUID NOT NULL REFERENCES public.hashtags(id) ON DELETE CASCADE,
    UNIQUE(video_id, hashtag_id)
);

CREATE INDEX IF NOT EXISTS idx_hashtags_name ON public.hashtags(name);

-- ==========================================================
-- 5. VIDEO LIKES & VIEWS & BOOKMARKS
-- ==========================================================
CREATE TABLE IF NOT EXISTS public.video_likes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    video_id UUID NOT NULL REFERENCES public.videos(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE(user_id, video_id)
);

CREATE TABLE IF NOT EXISTS public.video_views (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    video_id UUID NOT NULL REFERENCES public.videos(id) ON DELETE CASCADE,
    watch_time_ms INT DEFAULT 0,
    completed BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.bookmarks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    video_id UUID NOT NULL REFERENCES public.videos(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE(user_id, video_id)
);

-- ==========================================================
-- 6. COMMENTS & REPLIES
-- ==========================================================
CREATE TABLE IF NOT EXISTS public.comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    video_id UUID NOT NULL REFERENCES public.videos(id) ON DELETE CASCADE,
    text TEXT NOT NULL,
    likes_count INT DEFAULT 0,
    replies_count INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.comment_likes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    comment_id UUID NOT NULL REFERENCES public.comments(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE(user_id, comment_id)
);

CREATE TABLE IF NOT EXISTS public.comment_replies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    comment_id UUID NOT NULL REFERENCES public.comments(id) ON DELETE CASCADE,
    text TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- ==========================================================
-- 7. FOLLOWS & FOLLOW REQUESTS
-- ==========================================================
CREATE TABLE IF NOT EXISTS public.follows (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    follower_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    following_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE(follower_id, following_id)
);

CREATE TABLE IF NOT EXISTS public.follow_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    requester_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    target_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    status TEXT DEFAULT 'pending' CHECK (status IN ('pending', 'accepted', 'rejected')),
    created_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE(requester_id, target_id)
);

-- ==========================================================
-- 8. BLOCKING & REPORTS
-- ==========================================================
CREATE TABLE IF NOT EXISTS public.blocks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    blocker_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    blocked_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE(blocker_id, blocked_id)
);

CREATE TABLE IF NOT EXISTS public.reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reporter_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    reported_user_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    target_type TEXT NOT NULL CHECK (target_type IN ('video', 'user', 'comment', 'message')),
    target_id UUID NOT NULL,
    reason TEXT NOT NULL CHECK (reason IN ('Spam', 'Harassment', 'Hate', 'Violence', 'Sexual content', 'Dangerous content', 'Copyright', 'Scam', 'Other')),
    status TEXT DEFAULT 'pending' CHECK (status IN ('pending', 'reviewing', 'resolved', 'rejected')),
    moderation_note TEXT DEFAULT '',
    resolved_by UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- ==========================================================
-- 9. VERIFICATION REQUESTS
-- ==========================================================
CREATE TABLE IF NOT EXISTS public.verification_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    reason TEXT NOT NULL,
    supporting_info TEXT DEFAULT '',
    status TEXT DEFAULT 'pending' CHECK (status IN ('pending', 'approved', 'rejected')),
    verified_by UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    verified_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- ==========================================================
-- 10. NOTIFICATIONS
-- ==========================================================
CREATE TABLE IF NOT EXISTS public.notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    actor_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    type TEXT NOT NULL CHECK (type IN ('follow', 'follow_request', 'follow_accepted', 'like', 'comment', 'reply', 'mention', 'share', 'admin')),
    title TEXT NOT NULL,
    message TEXT NOT NULL,
    target_type TEXT,
    target_id UUID,
    is_read BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_notifications_user ON public.notifications(user_id, created_at DESC);

-- ==========================================================
-- 11. MESSAGING & CONVERSATIONS
-- ==========================================================
CREATE TABLE IF NOT EXISTS public.conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.conversation_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES public.conversations(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    joined_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE(conversation_id, user_id)
);

CREATE TABLE IF NOT EXISTS public.messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES public.conversations(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    text TEXT DEFAULT '',
    media_url TEXT,
    media_type TEXT DEFAULT 'text' CHECK (media_type IN ('text', 'image', 'video', 'profile_share')),
    is_read BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_messages_conv ON public.messages(conversation_id, created_at ASC);

-- ==========================================================
-- 12. ADMIN USERS & AUDIT LOGS & SETTINGS
-- ==========================================================
CREATE TABLE IF NOT EXISTS public.admin_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT NOT NULL,
    role TEXT NOT NULL CHECK (role IN ('super_admin', 'admin', 'moderator', 'support')),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.admin_audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    admin_id UUID NOT NULL REFERENCES auth.users(id),
    action TEXT NOT NULL,
    target_type TEXT NOT NULL,
    target_id TEXT,
    metadata JSONB DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_created ON public.admin_audit_logs(created_at DESC);

CREATE TABLE IF NOT EXISTS public.app_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    key TEXT UNIQUE NOT NULL,
    value JSONB NOT NULL,
    description TEXT,
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- Seed default application settings
INSERT INTO public.app_settings (key, value, description)
VALUES 
('app_name', '"Pulse"', 'Platform application brand name'),
('maintenance_mode', 'false', 'Enable maintenance mode'),
('registration_enabled', 'true', 'Enable new user registrations'),
('video_upload_enabled', 'true', 'Enable user video uploads'),
('max_video_duration', '180', 'Maximum video duration in seconds (3 mins)'),
('max_upload_size_mb', '100', 'Maximum file upload size in MB'),
('comments_enabled', 'true', 'Enable comments platform-wide'),
('messaging_enabled', 'true', 'Enable private messaging platform-wide'),
('downloads_enabled', 'true', 'Allow public video downloads')
ON CONFLICT (key) DO NOTHING;

-- ==========================================================
-- 13. ROW LEVEL SECURITY (RLS)
-- ==========================================================
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.videos ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.video_likes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.video_views ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.comments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.comment_likes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.comment_replies ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.follows ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.follow_requests ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.bookmarks ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.conversations ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.conversation_members ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.hashtags ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.video_hashtags ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.sounds ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.reports ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.blocks ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.verification_requests ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.admin_users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.admin_audit_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.app_settings ENABLE ROW LEVEL SECURITY;

-- Helper function to check if current authenticated user has an active admin role
CREATE OR REPLACE FUNCTION public.get_admin_role(user_uuid UUID)
RETURNS TEXT AS $$
    SELECT role FROM public.admin_users 
    WHERE user_id = user_uuid AND is_active = true 
    LIMIT 1;
$$ LANGUAGE sql SECURITY DEFINER;

-- Profiles policies
CREATE POLICY "Public profiles are readable by everyone"
    ON public.profiles FOR SELECT
    USING (is_banned = false OR auth.uid() = id OR public.get_admin_role(auth.uid()) IS NOT NULL);

CREATE POLICY "Users can update own profile"
    ON public.profiles FOR UPDATE
    USING (auth.uid() = id OR public.get_admin_role(auth.uid()) IN ('super_admin', 'admin'));

-- Videos policies
CREATE POLICY "Active approved videos viewable by users"
    ON public.videos FOR SELECT
    USING (
        (moderation_status = 'approved' AND (visibility = 'public' OR user_id = auth.uid()))
        OR (visibility = 'followers' AND EXISTS (SELECT 1 FROM public.follows WHERE follower_id = auth.uid() AND following_id = videos.user_id))
        OR public.get_admin_role(auth.uid()) IS NOT NULL
    );

CREATE POLICY "Users can insert own videos"
    ON public.videos FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own videos or admins moderate"
    ON public.videos FOR UPDATE
    USING (auth.uid() = user_id OR public.get_admin_role(auth.uid()) IN ('super_admin', 'admin', 'moderator'));

CREATE POLICY "Users can delete own videos or admins delete"
    ON public.videos FOR DELETE
    USING (auth.uid() = user_id OR public.get_admin_role(auth.uid()) IN ('super_admin', 'admin'));

-- Comments policies
CREATE POLICY "Comments readable by everyone"
    ON public.comments FOR SELECT
    USING (true);

CREATE POLICY "Users can insert comments"
    ON public.comments FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can delete own comments or admins"
    ON public.comments FOR DELETE
    USING (auth.uid() = user_id OR public.get_admin_role(auth.uid()) IN ('super_admin', 'admin', 'moderator'));

-- Messaging policies
CREATE POLICY "Members can view conversations"
    ON public.conversations FOR SELECT
    USING (EXISTS (SELECT 1 FROM public.conversation_members WHERE conversation_id = conversations.id AND user_id = auth.uid()));

CREATE POLICY "Members can view messages"
    ON public.messages FOR SELECT
    USING (EXISTS (SELECT 1 FROM public.conversation_members WHERE conversation_id = messages.conversation_id AND user_id = auth.uid()));

CREATE POLICY "Members can send messages"
    ON public.messages FOR INSERT
    WITH CHECK (auth.uid() = sender_id AND EXISTS (SELECT 1 FROM public.conversation_members WHERE conversation_id = messages.conversation_id AND user_id = auth.uid()));

-- Admin & Audit logs policies
CREATE POLICY "Admins can view admin_users"
    ON public.admin_users FOR SELECT
    USING (public.get_admin_role(auth.uid()) IS NOT NULL);

CREATE POLICY "Super admins can manage admin_users"
    ON public.admin_users FOR ALL
    USING (public.get_admin_role(auth.uid()) = 'super_admin');

CREATE POLICY "Admins can view and insert audit logs"
    ON public.admin_audit_logs FOR ALL
    USING (public.get_admin_role(auth.uid()) IS NOT NULL);

CREATE POLICY "Public read app settings, Super Admin write"
    ON public.app_settings FOR SELECT USING (true);

CREATE POLICY "Super Admin write app settings"
    ON public.app_settings FOR UPDATE
    USING (public.get_admin_role(auth.uid()) = 'super_admin');

-- Reports policies
CREATE POLICY "Users can create reports"
    ON public.reports FOR INSERT
    WITH CHECK (auth.uid() = reporter_id);

CREATE POLICY "Admins can view and manage reports"
    ON public.reports FOR ALL
    USING (public.get_admin_role(auth.uid()) IS NOT NULL);

-- ==========================================================
-- 14. AUTOMATIC TRIGGERS & COUNTERS
-- ==========================================================
-- Video Likes counter trigger
CREATE OR REPLACE FUNCTION update_video_likes_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE public.videos SET likes_count = likes_count + 1 WHERE id = NEW.video_id;
        UPDATE public.profiles SET likes_count = likes_count + 1 WHERE id = (SELECT user_id FROM public.videos WHERE id = NEW.video_id);
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE public.videos SET likes_count = GREATEST(likes_count - 1, 0) WHERE id = OLD.video_id;
        UPDATE public.profiles SET likes_count = GREATEST(likes_count - 1, 0) WHERE id = (SELECT user_id FROM public.videos WHERE id = OLD.video_id);
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_video_likes_count
AFTER INSERT OR DELETE ON public.video_likes
FOR EACH ROW EXECUTE FUNCTION update_video_likes_count();

-- Follows counter trigger
CREATE OR REPLACE FUNCTION update_follows_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE public.profiles SET following_count = following_count + 1 WHERE id = NEW.follower_id;
        UPDATE public.profiles SET followers_count = followers_count + 1 WHERE id = NEW.following_id;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE public.profiles SET following_count = GREATEST(following_count - 1, 0) WHERE id = OLD.follower_id;
        UPDATE public.profiles SET followers_count = GREATEST(followers_count - 1, 0) WHERE id = OLD.following_id;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER trg_follows_count
AFTER INSERT OR DELETE ON public.follows
FOR EACH ROW EXECUTE FUNCTION update_follows_count();

-- Auto create profile on auth.users signup
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
DECLARE
    clean_username TEXT;
BEGIN
    clean_username := split_part(NEW.email, '@', 1);
    IF EXISTS (SELECT 1 FROM public.profiles WHERE username = clean_username) THEN
        clean_username := clean_username || '_' || substr(md5(random()::text), 1, 4);
    END IF;

    INSERT INTO public.profiles (id, username, display_name, email, avatar)
    VALUES (
        NEW.id,
        clean_username,
        coalesce(NEW.raw_user_meta_data->>'full_name', clean_username),
        NEW.email,
        coalesce(NEW.raw_user_meta_data->>'avatar_url', 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150')
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE TRIGGER trg_on_auth_user_created
AFTER INSERT ON auth.users
FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();
