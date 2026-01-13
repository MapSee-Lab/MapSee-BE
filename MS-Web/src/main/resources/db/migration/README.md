# Flyway 마이그레이션 가이드

## 📋 개요

이 프로젝트는 **Flyway**를 사용하여 데이터베이스 스키마 마이그레이션을 관리합니다.

**중요**: **JPA DDL 전략을 `update`로 사용**하므로 Flyway 마이그레이션 후 JPA DDL이 실행됩니다.

## 🔄 실행 순서

1. **Flyway 마이그레이션 실행** (`db/migration/` 폴더의 SQL 파일들)
2. **JPA DDL 실행** (`spring.jpa.hibernate.ddl-auto=update`)

## ⚠️ 중요한 주의사항

### 테스트 환경에서의 DB Drop 시나리오

테스트 단계에서는 DB를 완전히 drop하고 다시 시작하는 경우가 있습니다.

**문제**: 마이그레이션 코드에서 존재하지 않는 테이블을 참조하면 에러 발생

**해결**: **모든 마이그레이션 코드는 테이블/컬럼 존재 여부를 확인한 후 작업을 수행해야 합니다.**

## 📝 마이그레이션 파일 작성 규칙

### 1. 파일 네이밍 규칙

```
V{version}__{description}.sql
```

**버전 규칙**: `version.yml` 파일의 `version` 값을 사용합니다.

**예시:**
- `version.yml`에 `version: "0.2.5"`가 있으면 → `V0.2.5__description.sql`
- `version.yml`에 `version: "0.2.6"`가 있으면 → `V0.2.6__description.sql`

**파일명 규칙:**
- 버전 번호는 점(.)으로 구분된 숫자
- 설명은 언더스코어(`_`)로 구분된 소문자
- 파일 확장자는 `.sql`

### 2. 필수 안전장치 패턴

모든 마이그레이션 코드는 다음 패턴을 따라야 합니다:

```sql
DO
$$
    BEGIN
        -- 테이블 존재 확인
        IF EXISTS (SELECT 1
                   FROM information_schema.tables
                   WHERE table_schema = 'public'
                     AND table_name = '테이블명') THEN
            -- 작업 수행
        ELSE
            RAISE NOTICE 'Table "public.테이블명" does not exist. Skipping migration.';
        END IF;
    END
$$;
```

**컬럼 작업 시:**
```sql
-- 컬럼 추가
IF NOT EXISTS (SELECT 1
               FROM information_schema.columns
               WHERE table_schema = 'public'
                 AND table_name = '테이블명'
                 AND column_name = '컬럼명') THEN
    ALTER TABLE public.테이블명 ADD COLUMN 컬럼명 타입;
END IF;

-- 컬럼 변경
IF EXISTS (SELECT 1
           FROM information_schema.columns
           WHERE table_schema = 'public'
             AND table_name = '테이블명'
             AND column_name = '기존컬럼명') THEN
    ALTER TABLE public.테이블명 RENAME COLUMN 기존컬럼명 TO 새컬럼명;
END IF;
```

**중복 실행 방지:**
```sql
IF NOT EXISTS (SELECT 1
               FROM information_schema.tables
               WHERE table_schema = 'public'
                 AND table_name = '새테이블명') THEN
    -- 테이블 생성/변경 작업
ELSE
    RAISE NOTICE 'Table "public.새테이블명" already exists. Skipping migration.';
END IF;
```

## 📚 예시 코드

### 예시: 테이블명 변경

```sql
DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.tables
                   WHERE table_schema = 'public'
                     AND table_name = 'place_external') THEN
            IF NOT EXISTS (SELECT 1
                           FROM information_schema.tables
                           WHERE table_schema = 'public'
                             AND table_name = 'place_platform_reference') THEN
                ALTER TABLE public.place_external
                    RENAME TO place_platform_reference;
            ELSE
                RAISE NOTICE 'Table "public.place_platform_reference" already exists. Skipping migration.';
            END IF;
        ELSE
            RAISE NOTICE 'Table "public.place_external" does not exist. Skipping migration. JPA will create the table automatically.';
        END IF;
    END
$$;
```

## ✅ 체크리스트

- [ ] `version.yml`의 버전을 확인하여 파일명에 사용했는가?
- [ ] 파일명이 `V{version}__{description}.sql` 형식을 따르는가?
- [ ] 테이블 작업 전에 테이블 존재 여부를 확인하는가?
- [ ] 컬럼 작업 전에 컬럼 존재 여부를 확인하는가?
- [ ] **Foreign Key 제약조건이 있는 경우, 부모 테이블 존재 여부를 반드시 확인하는가?**
- [ ] 중복 실행을 방지하는 로직이 있는가?
- [ ] 테이블이 없을 때 `RAISE NOTICE`로 안전하게 종료하는가?

## 🚨 주의사항

**절대 하지 말아야 할 것:**

❌ 테이블 존재 확인 없이 작업 수행
```sql
ALTER TABLE public.member ADD COLUMN new_field VARCHAR(100);
```

✅ 테이블 존재 확인 후 작업 수행
```sql
IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'member') THEN
    ALTER TABLE public.member ADD COLUMN new_field VARCHAR(100);
END IF;
```

### ⚠️ **Foreign Key 제약조건 작성 시 필수 확인사항**

**중요**: Foreign Key를 참조하는 부모 테이블이 존재하는지 반드시 확인해야 합니다!

❌ **잘못된 예**: 부모 테이블(member) 존재 확인 없이 FK 생성
```sql
CREATE TABLE public.fcm_token (
    id UUID NOT NULL,
    member_id UUID NOT NULL,
    CONSTRAINT fk_fcm_token_member FOREIGN KEY (member_id)
        REFERENCES public.member (id) ON DELETE CASCADE
);
-- member 테이블이 없으면 에러 발생!
```

✅ **올바른 예**: 부모 테이블 존재 확인 후 FK 생성
```sql
DO $$
BEGIN
    -- 1. 부모 테이블(member) 존재 확인
    IF EXISTS (SELECT 1 FROM information_schema.tables
               WHERE table_schema = 'public' AND table_name = 'member') THEN

        -- 2. 자식 테이블(fcm_token) 존재 확인
        IF NOT EXISTS (SELECT 1 FROM information_schema.tables
                       WHERE table_schema = 'public' AND table_name = 'fcm_token') THEN

            -- 3. 테이블 생성 (FK 포함)
            CREATE TABLE public.fcm_token (
                id UUID NOT NULL,
                member_id UUID NOT NULL,
                CONSTRAINT pk_fcm_token PRIMARY KEY (id),
                CONSTRAINT fk_fcm_token_member FOREIGN KEY (member_id)
                    REFERENCES public.member (id) ON DELETE CASCADE
            );
            RAISE NOTICE 'Created fcm_token table with FK to member';
        ELSE
            RAISE NOTICE 'fcm_token table already exists';
        END IF;
    ELSE
        RAISE NOTICE 'parent table "member" does not exist. Skipping fcm_token creation. JPA will create both tables.';
    END IF;
END $$;
```

**핵심 원칙**:
1. **부모 테이블 먼저 확인**: FK로 참조되는 테이블이 존재하는지 확인
2. **자식 테이블 확인**: 생성할 테이블이 이미 있는지 확인
3. **안전한 종료**: 부모 테이블이 없으면 아무 작업도 하지 않고 JPA에게 맡김

**JPA DDL과의 관계:**
- **Flyway 마이그레이션**: 기존 데이터베이스 스키마 변경 작업
- **JPA DDL**: 엔티티 기반으로 테이블 자동 생성/수정

**중요**: 테이블이 없을 때는 Flyway가 아무 작업도 하지 않고, JPA가 자동으로 테이블을 생성합니다.

## 📖 참고 자료

- [Flyway 공식 문서](https://flywaydb.org/documentation/)
- [PostgreSQL Information Schema](https://www.postgresql.org/docs/current/information-schema.html)
- 기존 마이그레이션 파일 참고