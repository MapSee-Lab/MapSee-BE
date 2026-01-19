-- ===================================================================
-- Flyway Migration: V0.3.2
-- Description: Interest 관련 테이블 제거 (MapSee에서 불필요)
-- Author: MapSee Team
-- Date: 2026-01-19
-- ===================================================================

-- 1. member_interest 중간 테이블 제거
DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.tables
                   WHERE table_schema = 'public'
                     AND table_name = 'member_interest') THEN
            DROP TABLE public.member_interest CASCADE;
            RAISE NOTICE 'Dropped member_interest table';
        ELSE
            RAISE NOTICE 'member_interest table does not exist. Skipping.';
        END IF;
    END
$$;

-- 2. interest 테이블 제거
DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.tables
                   WHERE table_schema = 'public'
                     AND table_name = 'interest') THEN
            DROP TABLE public.interest CASCADE;
            RAISE NOTICE 'Dropped interest table';
        ELSE
            RAISE NOTICE 'interest table does not exist. Skipping.';
        END IF;
    END
$$;

-- 3. member 테이블에서 interest 관련 컬럼 제거 (있다면)
DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.tables
                   WHERE table_schema = 'public'
                     AND table_name = 'member') THEN

            -- onboarding_interest_selected 컬럼 제거 (있다면)
            IF EXISTS (SELECT 1
                       FROM information_schema.columns
                       WHERE table_schema = 'public'
                         AND table_name = 'member'
                         AND column_name = 'onboarding_interest_selected') THEN
                ALTER TABLE public.member
                    DROP COLUMN onboarding_interest_selected;
                RAISE NOTICE 'Dropped onboarding_interest_selected column from member';
            ELSE
                RAISE NOTICE 'onboarding_interest_selected column does not exist in member';
            END IF;

        ELSE
            RAISE NOTICE 'member table does not exist. Skipping interest column cleanup.';
        END IF;
    END
$$;
