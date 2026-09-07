-- ==========================================================
-- PULSE SHORT-VIDEO PLATFORM: SEED DATA
-- File: supabase/seed/seed.sql
-- ==========================================================

-- 1. Default Sounds
INSERT INTO public.sounds (id, title, artist, cover, audio_url, usage_count, is_featured)
VALUES 
    ('00000000-0000-0000-0000-000000000001', 'Neon Cyber Symphony', 'Pulse Audio Labs', 'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=200', 'https://actions.google.com/sounds/v1/science_fiction/synth_pulse.ogg', 2410, true),
    ('00000000-0000-0000-0000-000000000002', 'Sunset Echoes (Original Mix)', 'Mira Wave', 'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=200', 'https://actions.google.com/sounds/v1/ambiences/outdoor_ambience.ogg', 1890, true),
    ('00000000-0000-0000-0000-000000000003', 'Future Trap Beats 2026', 'DJ Kairon', 'https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=200', 'https://actions.google.com/sounds/v1/science_fiction/sci_fi_pulse.ogg', 940, false),
    ('00000000-0000-0000-0000-000000000004', 'Chill Lo-Fi Rain Loop', 'Cozy Coffee', 'https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=200', 'https://actions.google.com/sounds/v1/weather/rain_heavy.ogg', 3250, false)
ON CONFLICT (id) DO NOTHING;

-- 2. Default Trending Hashtags
INSERT INTO public.hashtags (name, usage_count, is_featured)
VALUES 
    ('pulsecreator', 54200, true),
    ('shortvideo', 42100, true),
    ('dancechallenge', 38900, true),
    ('techtrends', 27400, false),
    ('traveltok', 19800, false),
    ('foodie', 15400, false)
ON CONFLICT (name) DO NOTHING;

-- 3. Super Admin Role Provisioning for yourtasin3@gmail.com
-- Automatically links when user signs up with yourtasin3@gmail.com
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM auth.users WHERE email = 'yourtasin3@gmail.com') THEN
        INSERT INTO public.admin_users (user_id, email, role, is_active)
        SELECT id, email, 'super_admin', true
        FROM auth.users
        WHERE email = 'yourtasin3@gmail.com'
        ON CONFLICT (user_id) DO UPDATE SET role = 'super_admin', is_active = true;

        UPDATE public.profiles SET verified = true
        WHERE email = 'yourtasin3@gmail.com';
    END IF;
END $$;
