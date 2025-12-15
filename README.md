# Bookstore API Server (Spring Boot)

과제 스크립트(30+ endpoints / JWT-RBAC / 페이지네이션·검색·정렬 / 에러 응답 통일 / Swagger / MySQL+Flyway / 레이트리밋 / 헬스체크)를 기준으로 구현한 API 서버입니다.

## Tech Stack
- Spring Boot 4.x, Spring Security, Spring Data JPA
- MySQL + Flyway
- JWT (access) + Refresh Token(해시 저장)
- Swagger(OpenAPI) 자동 문서 (springdoc)

## Run (Local)
### 1) 환경변수 설정
`.env.example`을 복사해서 `.env`를 만들고 값을 채워주세요.

```bash
cp .env.example .env
```

### 2) MySQL 준비
`bookstore` DB를 생성하고, `.env`의 계정이 접속 가능하도록 설정합니다.

### 3) 실행
```bash
./gradlew clean bootRun
```

- Health: `GET /health`
- Swagger: `/swagger-ui/index.html`
- API Root: `/api`

## Environment Variables
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `JWT_ACCESS_SECRET` (최소 32자 이상)
- `JWT_REFRESH_PEPPER`
- `APP_CORS_ALLOWED_ORIGINS` (콤마 구분)

## Error Response Format
모든 오류는 아래 공통 포맷으로 반환됩니다.

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

## Notes
- `.env`는 커밋 금지 (.gitignore 처리됨)
- 요청 요약 로그: method/path/status/latency_ms 기록
- 레이트리밋: 인증 없는 요청에 IP당 60req/분
