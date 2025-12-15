# Bookstore API Server (Spring Boot)

**DB·API 설계를 기반으로 구현한 실전형 Bookstore API 서버**입니다.  
과제 요구사항(30+ endpoints / Swagger / Postman / JWT 인증·인가(RBAC) / 페이지네이션·검색·정렬 / 에러 응답 통일 / MySQL+FK+Index / 시드데이터 200+ / 레이트리밋 / 헬스체크 / 자동화 테스트 20+)을 충족하도록 구성했습니다.

---

## 1) 프로젝트 개요

### 문제정의
온라인 서점 서비스에서 사용자(User)가 도서를 탐색/구매하고, 판매자(Seller)가 도서를 등록/주문을 처리하며, 관리자(Admin)가 운영 관리(유저 정지/통계/정산 등)를 수행할 수 있는 API 서버를 구현합니다.

### 주요 기능
- **Auth/User**: 회원가입/로그인/로그아웃/토큰 재발급/내 정보 조회·수정/회원 탈퇴(soft delete)
- **Book/Category/Author**: 도서·카테고리·작가 CRUD 및 검색/정렬/페이지네이션
- **Review/Comment**: 리뷰·댓글 CRUD + 좋아요(Like) + Top 리뷰 조회(캐시)
- **Cart/Wishlist**: 장바구니/위시리스트 담기·조회·삭제
- **Order/Library**: 주문 생성/내 주문 조회/주문 상세/내 서재(구매 목록)
- **Seller**: 판매자 정보/내 판매자 설정/판매자 도서 등록/판매자 주문 처리
- **Settlement/Stats (Admin 포함)**: 정산 생성·조회, 관리자 통계/운영 API

> 총 엔드포인트는 30개 이상(실제 구현은 더 많음)이며, 관리자 전용 엔드포인트는 3개 이상 포함합니다.

---

## 2) Tech Stack
- **Spring Boot 4.x**, Java 21
- Spring Web, Spring Validation
- **Spring Security + JWT(access) + Refresh Token(해시 저장/회전)**
- **MySQL + Flyway**(마이그레이션/시드)
- Spring Data JPA (Open-In-View OFF)
- Swagger/OpenAPI (springdoc)
- JUnit5 테스트 (20+)

---

## 3) 실행 방법

### 3-1. 사전 준비
- Java 21
- MySQL 8.x

### 3-2. 환경변수 설정
`.env.example`을 복사해서 `.env` 생성 후 값을 채웁니다.

```bash
cp .env.example .env
````

### 3-3. MySQL DB 생성

```sql
CREATE DATABASE bookstore CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3-4. 서버 실행 (Local)

```bash
./gradlew clean bootRun
```

또는 jar 실행:

```bash
./gradlew clean build
PORT=8080 java -jar build/libs/bookstore-0.0.1.jar
```

### 3-5. 마이그레이션/시드

* Flyway가 애플리케이션 시작 시 자동 실행됩니다.
* 시드 데이터는 Flyway 마이그레이션에 포함되어 **여러 테이블에 총 200건 이상(실제 400+ rows 수준)**이 입력됩니다.

### 3-6. 테스트 실행

```bash
./gradlew test
```

---

## 4) 환경변수(.env) 설명

`.env.example` 기준:

| 변수                         | 설명                  | 예시                                          |
| -------------------------- | ------------------- | ------------------------------------------- |
| `DB_URL`                   | MySQL JDBC URL      | `jdbc:mysql://localhost:3306/bookstore?...` |
| `DB_USERNAME`              | DB 계정               | `root`                                      |
| `DB_PASSWORD`              | DB 비밀번호             | (비공개)                                       |
| `JWT_ISSUER`               | JWT 발급자             | `bookstore`                                 |
| `JWT_ACCESS_SECRET`        | Access 토큰 서명키(32자+) | `CHANGE_ME...`                              |
| `JWT_ACCESS_EXP_MINUTES`   | Access 만료(분)        | `15`                                        |
| `JWT_REFRESH_PEPPER`       | Refresh 해시 pepper   | `CHANGE_ME...`                              |
| `JWT_REFRESH_EXP_DAYS`     | Refresh 만료(일)       | `14`                                        |
| `APP_CORS_ALLOWED_ORIGINS` | 허용 Origin(콤마 구분)    | `http://localhost:3000`                     |

> `.env`는 **절대 GitHub에 커밋하지 않습니다.** (`.env.example`만 커밋)

---

## 5) 배포 주소 (JCloud)

아래는 예시입니다. 본인 JCloud 주소/포트로 교체하세요.

* Base URL: `http://<JCloud-IP-or-Domain>:<PORT>`
* API Root: `/api`
* Swagger UI: `/swagger-ui/index.html`
* OpenAPI JSON: `/v3/api-docs`
* Health: `/health`

---

## 6) 인증 플로우(JWT)

### 6-1. 로그인/회원가입

* `POST /api/auth/register` 또는 `POST /api/auth/login`
* 응답에 `accessToken`, `refreshToken` 포함

### 6-2. API 호출

* 요청 헤더에 access token 포함

```http
Authorization: Bearer <accessToken>
```

### 6-3. 토큰 재발급

* `POST /api/auth/refresh` 로 refresh token을 보내 새 access/refresh 발급
* Refresh token은 서버에 **해시로 저장**되며, 재발급 시 **회전(rotate)** 됩니다.

### 6-4. 로그아웃

* `POST /api/auth/logout` 로 refresh token 폐기

---

## 7) 역할/권한표 (RBAC)

| 구분            | 권한                                                  |
| ------------- | --------------------------------------------------- |
| `ROLE_USER`   | 도서/작가/카테고리 조회, 리뷰/댓글 작성, 장바구니/위시리스트, 주문/내 서재        |
| `ROLE_SELLER` | 판매자 도서 등록/수정/삭제(본인), 판매자 주문 처리, 판매자 정산 조회           |
| `ROLE_ADMIN`  | 사용자 관리(조회/비활성화), 도서/작가/카테고리 관리자 CRUD, 주문/정산/통계 조회 등 |

> 관리자 전용 엔드포인트는 `/api/admin/**` 경로로 제공됩니다.

---

## 8) 예제 계정 (Seed)

시드 데이터에 아래 계정이 포함됩니다.

* `user1@example.com / P@ssw0rd!` (USER)
* `seller1@example.com / P@ssw0rd!` (SELLER)
* `admin@example.com / P@ssw0rd!` (ADMIN)

---

## 9) DB 연결 정보(테스트용)

* DBMS: MySQL
* DB: `bookstore`
* Host/Port/User/Password는 `.env` 기준으로 설정

접속 예시:

```bash
mysql -h <DB_HOST> -P 3306 -u <DB_USERNAME> -p
```

> 과제 제출용 “DB 아이디/비밀번호 및 접속 명령어”는 별도의 텍스트/워드 파일로 제출합니다(README에 비밀번호 직접 기재 금지).

---

## 10) 엔드포인트 요약표

> 아래는 대표 요약이며, 상세 스펙/예시는 Swagger에서 확인합니다.

| Method | Path                                | 설명                 | 권한                   |            |       |
| ------ | ----------------------------------- | ------------------ | -------------------- | ---------- | ----- |
| GET    | `/health`                           | 헬스체크(무인증)          | Public               |            |       |
| POST   | `/api/auth/register`                | 회원가입 + 토큰 발급       | Public               |            |       |
| POST   | `/api/auth/login`                   | 로그인 + 토큰 발급        | Public               |            |       |
| POST   | `/api/auth/refresh`                 | 토큰 재발급             | Public               |            |       |
| POST   | `/api/auth/logout`                  | 로그아웃               | Public               |            |       |
| GET    | `/api/auth/me`                      | 내 정보 조회            | User/Seller/Admin    |            |       |
| PATCH  | `/api/auth/me`                      | 내 정보 수정            | User/Seller/Admin    |            |       |
| PATCH  | `/api/auth/me/delete`               | 회원 탈퇴(soft delete) | User/Seller/Admin    |            |       |
| GET    | `/api/books`                        | 도서 목록(검색/정렬/페이지)   | Public               |            |       |
| GET    | `/api/books/{bookId}`               | 도서 상세              | Public               |            |       |
| GET    | `/api/categories`                   | 카테고리 목록            | Public               |            |       |
| GET    | `/api/authors`                      | 작가 목록              | Public               |            |       |
| GET    | `/api/sellers`                      | 판매자 목록             | Public               |            |       |
| GET    | `/api/books/{bookId}/reviews`       | 도서 리뷰 목록(페이지/정렬)   | Public/Optional Auth |            |       |
| POST   | `/api/books/{bookId}/reviews`       | 리뷰 작성              | Auth                 |            |       |
| PATCH  | `/api/reviews/{reviewId}`           | 리뷰 수정              | Auth                 |            |       |
| DELETE | `/api/reviews/{reviewId}`           | 리뷰 삭제(본인/관리자)      | Auth                 |            |       |
| POST   | `/api/reviews/{reviewId}/like`      | 리뷰 좋아요             | Auth                 |            |       |
| GET    | `/api/reviews/{reviewId}/comments`  | 댓글 목록(페이지)         | Public/Optional Auth |            |       |
| POST   | `/api/reviews/{reviewId}/comments`  | 댓글 작성              | Auth                 |            |       |
| PATCH  | `/api/comments/{commentId}`         | 댓글 수정              | Auth                 |            |       |
| DELETE | `/api/comments/{commentId}`         | 댓글 삭제(본인/관리자)      | Auth                 |            |       |
| POST   | `/api/comments/{commentId}/like`    | 댓글 좋아요             | Auth                 |            |       |
| GET    | `/api/users/me/cart`                | 내 장바구니 조회          | Auth                 |            |       |
| POST   | `/api/users/me/cart/items`          | 장바구니 담기            | Auth                 |            |       |
| DELETE | `/api/users/me/cart/items/{id}`     | 장바구니 삭제            | Auth                 |            |       |
| GET    | `/api/users/me/wishlist`            | 위시리스트 조회           | Auth                 |            |       |
| POST   | `/api/users/me/wishlist/items`      | 위시리스트 담기           | Auth                 |            |       |
| DELETE | `/api/users/me/wishlist/items/{id}` | 위시리스트 삭제           | Auth                 |            |       |
| POST   | `/api/users/me/orders`              | 주문 생성              | Auth                 |            |       |
| GET    | `/api/users/me/orders`              | 내 주문 목록(페이지)       | Auth                 |            |       |
| GET    | `/api/users/me/orders/{id}`         | 내 주문 상세            | Auth                 |            |       |
| GET    | `/api/users/me/library`             | 내 서재(구매 목록)        | Auth                 |            |       |
| POST   | `/api/seller/books`                 | 판매자 도서 등록          | Seller               |            |       |
| PATCH  | `/api/seller/books/{id}`            | 판매자 도서 수정          | Seller               |            |       |
| DELETE | `/api/seller/books/{id}`            | 판매자 도서 삭제          | Seller               |            |       |
| GET    | `/api/seller/orders`                | 판매자 주문 목록          | Seller               |            |       |
| PATCH  | `/api/seller/orders/{id}/status`    | 주문 상태 변경           | Seller               |            |       |
| GET    | `/api/admin/users`                  | 사용자 목록             | Admin                |            |       |
| PATCH  | `/api/admin/users/{id}/deactivate`  | 사용자 비활성화           | Admin                |            |       |
| GET    | `/api/admin/stats/*`                | 통계 조회              | Admin                |            |       |
| CRUD   | `/api/admin/books                   | authors            | categories`          | 관리자 리소스 관리 | Admin |

---

## 11) 목록 조회 공통 규격 (페이지네이션/검색/정렬)

* `page`: 기본 1 (1-base)
* `size` 또는 `limit`: 기본 20, 최대 100
* `sort`: `field,ASC|DESC` 형식 (예: `price_cents,DESC`)
* 검색/필터 예시(도서):

  * `GET /api/books?q=java&category_id=1&sort=price_cents,DESC&page=1&limit=20`

응답 메타는 `meta`에 포함됩니다(페이지/limit/total/hasNext 등).

---

## 12) 에러 처리 규격

모든 에러는 아래 JSON 포맷으로 통일됩니다.

```json
{
  "timestamp": "2025-03-05T12:34:56Z",
  "path": "/api/books/1",
  "status": 401,
  "code": "TOKEN_EXPIRED",
  "message": "토큰이 만료되었습니다.",
  "details": null
}
```

대표 에러 코드(10+):

* `BAD_REQUEST`, `VALIDATION_FAILED`, `INVALID_QUERY_PARAM`
* `UNAUTHORIZED`, `FORBIDDEN`, `NOT_FOUND`, `CONFLICT`
* `TOO_MANY_REQUESTS`, `INTERNAL_ERROR`, `DATABASE_ERROR`
* `TOKEN_INVALID`, `TOKEN_EXPIRED`, `TOKEN_REVOKED`

---

## 13) 보안/성능 고려사항

* 비밀번호 해시: BCrypt
* CORS: `APP_CORS_ALLOWED_ORIGINS` 기반 허용
* 레이트리밋: 인증 없는 요청에 대해 IP당 60req/분(간단 인메모리)
* MySQL FK/Index 적용(조인/검색 필드 기준)
* Open-In-View 비활성화로 예기치 않은 Lazy 로딩 방지
* Top 리뷰 조회 캐시 적용(예: `topReviews`)

---

## 14) Swagger / Postman

* Swagger UI: `/swagger-ui/index.html`
* OpenAPI JSON: `/v3/api-docs`

Postman:

* `postman/bookstore.postman_collection.json`
* 환경 변수 기반(`baseUrl`, `accessToken`, `refreshToken` 등)
* Pre-request/Test 스크립트 포함(토큰 저장·주입, 응답 검증 등)

---

## 15) 한계와 개선 계획

* 운영 환경에서는 레이트리밋/캐시를 Redis 등 외부 스토어로 분리 필요
* 통합 테스트(MockMvc) 비중을 더 늘려 인증/인가 시나리오를 더 촘촘히 커버 가능
* CI(GitHub Actions)로 테스트/빌드 자동화 가능
* Docker Compose로 DB+App 원클릭 실행 개선 가능

---

## 16) 프로젝트 구조(권장)

```text
repo-root
├─ README.md
├─ .gitignore
├─ .env.example
├─ docs/
├─ postman/
├─ src/
└─ tests/
```

```