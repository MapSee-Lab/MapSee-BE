-- ===================================================================
-- Flyway Migration: V0.3.1
-- Description: member_place 테이블에 북마크 기능 필드 추가
-- Author: MapSee Team
-- Date: 2026-01-19
-- ===================================================================

-- member_place 테이블 확장 (북마크 기능 강화)
DO
$$
    BEGIN
        -- member_place 테이블 존재 확인
        IF EXISTS (SELECT 1
                   FROM information_schema.tables
                   WHERE table_schema = 'public'
                     AND table_name = 'member_place') THEN

            -- folder 컬럼 추가
            IF NOT EXISTS (SELECT 1
                           FROM information_schema.columns
                           WHERE table_schema = 'public'
                             AND table_name = 'member_place'
                             AND column_name = 'folder') THEN
                ALTER TABLE public.member_place
                    ADD COLUMN folder VARCHAR(50) DEFAULT 'default';
                RAISE NOTICE 'Added folder column to member_place';
            ELSE
                RAISE NOTICE 'folder column already exists in member_place';
            END IF;

            -- memo 컬럼 추가
            IF NOT EXISTS (SELECT 1
                           FROM information_schema.columns
                           WHERE table_schema = 'public'
                             AND table_name = 'member_place'
                             AND column_name = 'memo') THEN
                ALTER TABLE public.member_place
                    ADD COLUMN memo TEXT;
                RAISE NOTICE 'Added memo column to member_place';
            ELSE
                RAISE NOTICE 'memo column already exists in member_place';
            END IF;

            -- rating 컬럼 추가
            IF NOT EXISTS (SELECT 1
                           FROM information_schema.columns
                           WHERE table_schema = 'public'
                             AND table_name = 'member_place'
                             AND column_name = 'rating') THEN
                ALTER TABLE public.member_place
                    ADD COLUMN rating INTEGER;

                -- 별점은 1-5 사이의 값만 허용
                ALTER TABLE public.member_place
                    ADD CONSTRAINT chk_member_place_rating CHECK (rating IS NULL OR (rating >= 1 AND rating <= 5));

                RAISE NOTICE 'Added rating column to member_place with check constraint';
            ELSE
                RAISE NOTICE 'rating column already exists in member_place';
            END IF;

            -- visited 컬럼 추가
            IF NOT EXISTS (SELECT 1
                           FROM information_schema.columns
                           WHERE table_schema = 'public'
                             AND table_name = 'member_place'
                             AND column_name = 'visited') THEN
                ALTER TABLE public.member_place
                    ADD COLUMN visited BOOLEAN NOT NULL DEFAULT FALSE;
                RAISE NOTICE 'Added visited column to member_place';
            ELSE
                RAISE NOTICE 'visited column already exists in member_place';
            END IF;

            -- visited_at 컬럼 추가
            IF NOT EXISTS (SELECT 1
                           FROM information_schema.columns
                           WHERE table_schema = 'public'
                             AND table_name = 'member_place'
                             AND column_name = 'visited_at') THEN
                ALTER TABLE public.member_place
                    ADD COLUMN visited_at TIMESTAMP WITH TIME ZONE;
                RAISE NOTICE 'Added visited_at column to member_place';
            ELSE
                RAISE NOTICE 'visited_at column already exists in member_place';
            END IF;

            -- 폴더 기반 검색을 위한 인덱스 추가
            IF NOT EXISTS (SELECT 1
                           FROM pg_indexes
                           WHERE schemaname = 'public'
                             AND tablename = 'member_place'
                             AND indexname = 'idx_member_place_folder') THEN
                CREATE INDEX idx_member_place_folder ON public.member_place (member_id, folder);
                RAISE NOTICE 'Created index idx_member_place_folder';
            ELSE
                RAISE NOTICE 'Index idx_member_place_folder already exists';
            END IF;

            -- 방문 여부 필터링을 위한 인덱스 추가
            IF NOT EXISTS (SELECT 1
                           FROM pg_indexes
                           WHERE schemaname = 'public'
                             AND tablename = 'member_place'
                             AND indexname = 'idx_member_place_visited') THEN
                CREATE INDEX idx_member_place_visited ON public.member_place (member_id, visited);
                RAISE NOTICE 'Created index idx_member_place_visited';
            ELSE
                RAISE NOTICE 'Index idx_member_place_visited already exists';
            END IF;

            -- 별점 기반 정렬을 위한 인덱스 추가
            IF NOT EXISTS (SELECT 1
                           FROM pg_indexes
                           WHERE schemaname = 'public'
                             AND tablename = 'member_place'
                             AND indexname = 'idx_member_place_rating') THEN
                CREATE INDEX idx_member_place_rating ON public.member_place (member_id, rating DESC NULLS LAST);
                RAISE NOTICE 'Created index idx_member_place_rating';
            ELSE
                RAISE NOTICE 'Index idx_member_place_rating already exists';
            END IF;

        ELSE
            RAISE NOTICE 'member_place table does not exist. Skipping migration. JPA will create the table with new columns.';
        END IF;
    END
$$;
