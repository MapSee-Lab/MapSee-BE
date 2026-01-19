-- ===================================================================
-- Flyway Migration: V0.3.0
-- Description: 키워드 시스템 추가 (keywords, place_keywords 테이블 생성)
-- Author: MapSee Team
-- Date: 2026-01-19
-- ===================================================================

-- 1. keywords 테이블 생성
DO
$$
    BEGIN
        -- keywords 테이블이 이미 존재하는지 확인
        IF NOT EXISTS (SELECT 1
                       FROM information_schema.tables
                       WHERE table_schema = 'public'
                         AND table_name = 'keywords') THEN

            -- keywords 테이블 생성
            CREATE TABLE public.keywords
            (
                id          UUID                     NOT NULL,
                keyword     VARCHAR(100)             NOT NULL,
                count       INTEGER                  NOT NULL DEFAULT 1,
                trend_score NUMERIC(10, 2)           NOT NULL DEFAULT 0,
                created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
                updated_at  TIMESTAMP WITH TIME ZONE NOT NULL,
                created_by  VARCHAR(255),
                updated_by  VARCHAR(255),

                CONSTRAINT pk_keywords PRIMARY KEY (id),
                CONSTRAINT uk_keywords_keyword UNIQUE (keyword)
            );

            -- 키워드 검색 성능 향상을 위한 인덱스
            CREATE INDEX idx_keywords_keyword ON public.keywords (keyword);

            -- 트렌드 점수 기반 정렬을 위한 인덱스
            CREATE INDEX idx_keywords_trend_score ON public.keywords (trend_score DESC);

            -- 사용 횟수 기반 정렬을 위한 인덱스
            CREATE INDEX idx_keywords_count ON public.keywords (count DESC);

            RAISE NOTICE 'Created keywords table with indexes';
        ELSE
            RAISE NOTICE 'keywords table already exists. Skipping creation.';
        END IF;
    END
$$;

-- 2. place_keywords 중간 테이블 생성
DO
$$
    BEGIN
        -- place_keywords 테이블이 이미 존재하는지 확인
        IF NOT EXISTS (SELECT 1
                       FROM information_schema.tables
                       WHERE table_schema = 'public'
                         AND table_name = 'place_keywords') THEN

            -- place 테이블 존재 확인 (부모 테이블)
            IF EXISTS (SELECT 1
                       FROM information_schema.tables
                       WHERE table_schema = 'public'
                         AND table_name = 'place') THEN

                -- keywords 테이블 존재 확인 (부모 테이블)
                IF EXISTS (SELECT 1
                           FROM information_schema.tables
                           WHERE table_schema = 'public'
                             AND table_name = 'keywords') THEN

                    -- place_keywords 테이블 생성
                    CREATE TABLE public.place_keywords
                    (
                        place_id   UUID NOT NULL,
                        keyword_id UUID NOT NULL,

                        CONSTRAINT pk_place_keywords PRIMARY KEY (place_id, keyword_id),
                        CONSTRAINT fk_place_keywords_place FOREIGN KEY (place_id)
                            REFERENCES public.place (id) ON DELETE CASCADE,
                        CONSTRAINT fk_place_keywords_keyword FOREIGN KEY (keyword_id)
                            REFERENCES public.keywords (id) ON DELETE CASCADE
                    );

                    -- 키워드로 장소 검색 성능 향상을 위한 인덱스
                    CREATE INDEX idx_place_keywords_keyword_id ON public.place_keywords (keyword_id);

                    -- 장소로 키워드 검색 성능 향상을 위한 인덱스
                    CREATE INDEX idx_place_keywords_place_id ON public.place_keywords (place_id);

                    RAISE NOTICE 'Created place_keywords table with foreign keys and indexes';
                ELSE
                    RAISE NOTICE 'keywords table does not exist. Skipping place_keywords creation. JPA will create it.';
                END IF;
            ELSE
                RAISE NOTICE 'place table does not exist. Skipping place_keywords creation. JPA will create it.';
            END IF;
        ELSE
            RAISE NOTICE 'place_keywords table already exists. Skipping creation.';
        END IF;
    END
$$;
