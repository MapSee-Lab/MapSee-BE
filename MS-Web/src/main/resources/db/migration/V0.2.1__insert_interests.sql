-- ============================================
-- Tripgether Interest Data Migration
-- Version: 0.2.1
-- Description: 14개 대분류, 123개 소분류 관심사 초기화
-- - 테이블이 없으면 아무 작업도 하지 않음 (JPA가 자동 생성)
-- ============================================

DO
$$
    BEGIN
        -------------------------------------------------------------------
        -- 0. interest 테이블 존재 확인
        -------------------------------------------------------------------
        IF EXISTS (SELECT 1
                   FROM information_schema.tables
                   WHERE table_schema = 'public'
                     AND table_name = 'interest') THEN
            -----------------------------------------------------------------
            -- 1. UNIQUE 제약조건 추가 (ON CONFLICT를 위해 필수)
            -----------------------------------------------------------------
            IF NOT EXISTS (SELECT 1
                           FROM pg_constraint
                           WHERE conname = 'interest_category_name_unique') THEN
                ALTER TABLE public.interest
                    ADD CONSTRAINT interest_category_name_unique UNIQUE (category, name);
            END IF;

            -----------------------------------------------------------------
            -- 2. Check constraint 수정 (실제 사용하는 카테고리로 변경)
            -- 기존 제약조건 삭제 후 재생성
            -----------------------------------------------------------------
            ALTER TABLE public.interest DROP CONSTRAINT IF EXISTS interest_category_check;
            ALTER TABLE public.interest
                ADD CONSTRAINT interest_category_check CHECK (
                    category IN (
                        'FOOD', 'CAFE_DESSERT', 'LOCAL_MARKET', 'NATURE_OUTDOOR',
                        'URBAN_PHOTOSPOTS', 'CULTURE_ART', 'HISTORY_ARCHITECTURE',
                        'EXPERIENCE_CLASS', 'SHOPPING_FASHION', 'NIGHTLIFE',
                        'WELLNESS', 'FAMILY_KIDS', 'KPOP_CULTURE', 'DRIVE_SUBURBS'
                    )
                );

            -----------------------------------------------------------------
            -- 3. 관심사 데이터 INSERT
            -----------------------------------------------------------------

            -- 1. 맛집/푸드 (14개)
            INSERT INTO public.interest (id, category, name, created_at, updated_at)
            VALUES
                (gen_random_uuid(), 'FOOD', '한식', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'FOOD', '일식', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'FOOD', '중식', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'FOOD', '양식', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'FOOD', '분식/간식', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'FOOD', '고기구이', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'FOOD', '해산물', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'FOOD', '비건/플렉시', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'FOOD', '브런치', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'FOOD', '포장마차/야식', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'FOOD', '파인다이닝/오마카세', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'FOOD', '지역별 로컬맛집', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'FOOD', '세계음식', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'FOOD', '전통주점/민속주점', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (category, name) DO NOTHING;

            -- 2. 카페/디저트 (7개)
            INSERT INTO public.interest (id, category, name, created_at, updated_at)
            VALUES
                (gen_random_uuid(), 'CAFE_DESSERT', '스페셜티 카페', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'CAFE_DESSERT', '베이커리/빵집', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'CAFE_DESSERT', '디저트 바', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'CAFE_DESSERT', '뷰카페(루프탑/오션뷰)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'CAFE_DESSERT', '레트로 카페', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'CAFE_DESSERT', '감성 카페', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'CAFE_DESSERT', '전통찻집/다원', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (category, name) DO NOTHING;

            -- 3. 로컬시장/골목 (7개)
            INSERT INTO public.interest (id, category, name, created_at, updated_at)
            VALUES
                (gen_random_uuid(), 'LOCAL_MARKET', '재래시장', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'LOCAL_MARKET', '수산시장', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'LOCAL_MARKET', '야시장/도깨비시장', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'LOCAL_MARKET', '벼룩/플리마켓/공예마켓', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'LOCAL_MARKET', '노포거리', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'LOCAL_MARKET', '공방거리', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'LOCAL_MARKET', '대학교 상권/대학가', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (category, name) DO NOTHING;

            -- 4. 자연/아웃도어 (10개)
            INSERT INTO public.interest (id, category, name, created_at, updated_at)
            VALUES
                (gen_random_uuid(), 'NATURE_OUTDOOR', '해변/수상 액티비티', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'NATURE_OUTDOOR', '산/트레킹', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'NATURE_OUTDOOR', '국립공원', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'NATURE_OUTDOOR', '공원/유원지', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'NATURE_OUTDOOR', '계절 명소 (벚꽃, 단풍 등)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'NATURE_OUTDOOR', '캠핑/백패킹', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'NATURE_OUTDOOR', '일출/일몰 스팟', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'NATURE_OUTDOOR', '계곡/폭포', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'NATURE_OUTDOOR', '수목원/식물원', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'NATURE_OUTDOOR', '자전거길/하이킹코스', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (category, name) DO NOTHING;

            -- 5. 도시산책/포토스팟 (8개)
            INSERT INTO public.interest (id, category, name, created_at, updated_at)
            VALUES
                (gen_random_uuid(), 'URBAN_PHOTOSPOTS', '골목 산책로', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'URBAN_PHOTOSPOTS', '벽화마을', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'URBAN_PHOTOSPOTS', '하천/보행로', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'URBAN_PHOTOSPOTS', '야경 스팟/전망대', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'URBAN_PHOTOSPOTS', '레트로 거리', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'URBAN_PHOTOSPOTS', '창고/산업뷰', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'URBAN_PHOTOSPOTS', '대학교 캠퍼스', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'URBAN_PHOTOSPOTS', '건축물 투어', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (category, name) DO NOTHING;

            -- 6. 문화/예술 (10개)
            INSERT INTO public.interest (id, category, name, created_at, updated_at)
            VALUES
                (gen_random_uuid(), 'CULTURE_ART', '미술관/갤러리', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'CULTURE_ART', '공연/연극/뮤지컬', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'CULTURE_ART', '독립서점', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'CULTURE_ART', '전시회/박람회', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'CULTURE_ART', '페스티벌', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'CULTURE_ART', '복합문화공간', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'CULTURE_ART', '스트리트 아트', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'CULTURE_ART', '클래식 공연', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'CULTURE_ART', '독립영화관', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'CULTURE_ART', '라이브 공연장', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (category, name) DO NOTHING;

            -- 7. 역사/건축/종교 (9개)
            INSERT INTO public.interest (id, category, name, created_at, updated_at)
            VALUES
                (gen_random_uuid(), 'HISTORY_ARCHITECTURE', '궁궐/고택', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'HISTORY_ARCHITECTURE', '성곽/산성', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'HISTORY_ARCHITECTURE', '근대건축', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'HISTORY_ARCHITECTURE', '한옥', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'HISTORY_ARCHITECTURE', '유적지/역사박물관', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'HISTORY_ARCHITECTURE', '사찰', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'HISTORY_ARCHITECTURE', '성당', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'HISTORY_ARCHITECTURE', '교회', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'HISTORY_ARCHITECTURE', '템플스테이', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (category, name) DO NOTHING;

            -- 8. 체험/클래스 (9개)
            INSERT INTO public.interest (id, category, name, created_at, updated_at)
            VALUES
                (gen_random_uuid(), 'EXPERIENCE_CLASS', '도예/목공', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'EXPERIENCE_CLASS', '향수/비누', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'EXPERIENCE_CLASS', '쿠킹 클래스', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'EXPERIENCE_CLASS', '플라워/가드닝', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'EXPERIENCE_CLASS', '사진/영상 원데이', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'EXPERIENCE_CLASS', '한복 대여', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'EXPERIENCE_CLASS', '농장 체험', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'EXPERIENCE_CLASS', '스포츠 원데이 클래스', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'EXPERIENCE_CLASS', '실내 액티비티/게임', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (category, name) DO NOTHING;

            -- 9. 쇼핑/패션 (10개)
            INSERT INTO public.interest (id, category, name, created_at, updated_at)
            VALUES
                (gen_random_uuid(), 'SHOPPING_FASHION', '편집숍', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'SHOPPING_FASHION', '로컬 브랜드', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'SHOPPING_FASHION', '빈티지', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'SHOPPING_FASHION', '아울렛/몰', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'SHOPPING_FASHION', '홈리빙/인테리어숍', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'SHOPPING_FASHION', '뷰티/드럭스토어', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'SHOPPING_FASHION', '백화점/면세점', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'SHOPPING_FASHION', '캐릭터/팬시샵', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'SHOPPING_FASHION', '플래그십 스토어', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'SHOPPING_FASHION', '팝업스토어/브랜드 행사', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (category, name) DO NOTHING;

            -- 10. 나이트라이프/음주 (10개)
            INSERT INTO public.interest (id, category, name, created_at, updated_at)
            VALUES
                (gen_random_uuid(), 'NIGHTLIFE', '수제맥주', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'NIGHTLIFE', '와인바', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'NIGHTLIFE', '칵테일바', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'NIGHTLIFE', '이자카야/사케바', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'NIGHTLIFE', '루프탑 바', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'NIGHTLIFE', '재즈클럽', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'NIGHTLIFE', '힙합/EDM 클럽', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'NIGHTLIFE', '위스키 바', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'NIGHTLIFE', 'LP바', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'NIGHTLIFE', '라운지 바', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (category, name) DO NOTHING;

            -- 11. 웰니스/휴식 (6개)
            INSERT INTO public.interest (id, category, name, created_at, updated_at)
            VALUES
                (gen_random_uuid(), 'WELLNESS', '스파/마사지', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'WELLNESS', '찜질방/사우나', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'WELLNESS', '온천', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'WELLNESS', '요가/명상', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'WELLNESS', '북카페/라운지', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'WELLNESS', '삼림욕/휴양림', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (category, name) DO NOTHING;

            -- 12. 가족/아이동반 (9개)
            INSERT INTO public.interest (id, category, name, created_at, updated_at)
            VALUES
                (gen_random_uuid(), 'FAMILY_KIDS', '키즈카페', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'FAMILY_KIDS', '키즈존 식당', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'FAMILY_KIDS', '과학관/체험관', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'FAMILY_KIDS', '아쿠아리움', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'FAMILY_KIDS', '동물농장/체험', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'FAMILY_KIDS', '테마파크/놀이공원', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'FAMILY_KIDS', '어린이 도서관', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'FAMILY_KIDS', '직업체험관', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'FAMILY_KIDS', '어린이 공연/전시', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (category, name) DO NOTHING;

            -- 13. K-POP·K-컬처 (9개)
            INSERT INTO public.interest (id, category, name, created_at, updated_at)
            VALUES
                (gen_random_uuid(), 'KPOP_CULTURE', '엔터사 사옥', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'KPOP_CULTURE', '음악방송/사전녹화', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'KPOP_CULTURE', '굿즈샵', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'KPOP_CULTURE', 'K-pop 댄스 원데이', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'KPOP_CULTURE', '팬카페/포토존', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'KPOP_CULTURE', '드라마/영화 촬영지', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'KPOP_CULTURE', 'K-뷰티', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'KPOP_CULTURE', '미디어·스타 맛집', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'KPOP_CULTURE', '아이돌 생일/이벤트 카페', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (category, name) DO NOTHING;

            -- 14. 드라이브/근교 (5개)
            INSERT INTO public.interest (id, category, name, created_at, updated_at)
            VALUES
                (gen_random_uuid(), 'DRIVE_SUBURBS', '해안도로', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'DRIVE_SUBURBS', '야경/전망대', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'DRIVE_SUBURBS', '오토캠핑/차박', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'DRIVE_SUBURBS', '야간 드라이브', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (gen_random_uuid(), 'DRIVE_SUBURBS', '자동차 극장', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (category, name) DO NOTHING;

        ELSE
            RAISE NOTICE 'Table "public.interest" does not exist. Skipping migration. JPA will create the table automatically.';
        END IF;

    END
$$;
