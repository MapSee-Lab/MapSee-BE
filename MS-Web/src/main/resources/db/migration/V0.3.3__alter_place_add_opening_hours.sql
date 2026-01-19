-- V0.3.3: Place 테이블에 opening_hours 컬럼 추가
-- MapSee AI 서버 연동을 위한 영업시간 필드 추가

-- opening_hours 컬럼 추가 (존재하지 않는 경우에만)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'place'
          AND column_name = 'opening_hours'
    ) THEN
        ALTER TABLE place
        ADD COLUMN opening_hours VARCHAR(500);

        COMMENT ON COLUMN place.opening_hours IS 'AI 서버에서 추출한 영업시간 정보';
    END IF;
END $$;
