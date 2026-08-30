# 모아모아 프론트엔드

## 폴더 구조 (페이지별로 나눔)

```
src/
├── main.jsx              앱 시작점
├── App.jsx               로그인 상태 관리 + 화면 전환 + 레이아웃
├── pages/                화면 하나당 파일 하나
│   ├── LoginPage.jsx
│   ├── OnboardingPage.jsx
│   ├── HomePage.jsx
│   ├── BenefitsPage.jsx      (F1 채팅으로 넘어가야 하면 ChatPanel 렌더)
│   ├── ChatPanel.jsx
│   ├── BenefitDetailPage.jsx
│   ├── WagePage.jsx
│   ├── ExchangePage.jsx
│   ├── FeesPage.jsx
│   └── CalendarPage.jsx
├── components/           여러 페이지에서 같이 쓰는 것들
│   ├── Sidebar.jsx
│   ├── TopHeader.jsx
│   ├── PageWrap.jsx      전역 스타일/애니메이션
│   └── ui/Primitives.jsx (Btn, Card, Badge, Row, Field)
├── api/
│   ├── client.js         ★ 실제 백엔드 호출 (여기가 API 연결 핵심 파일)
│   └── mock.js           client.js가 실패했을 때 자동으로 쓰는 가짜 데이터
└── lib/                  theme.js(색상), format.js, useCountUp.js, supabase.js
```

기능을 하나 고치고 싶으면 대부분 `pages/` 안의 파일 하나만 열면 됩니다. 예: 임금 진단 화면 수정 → `pages/WagePage.jsx`만 건드리면 돼요.

## API 연결은 이미 되어 있어요

`src/api/client.js`에 명세서(API 명세서 v1.0)의 모든 엔드포인트가 실제 `fetch()`로 구현되어 있어요:

- `GET /api/auth/me`, `POST /api/profile/basic`
- `GET /api/benefits`, `POST /api/chat/message`
- `POST /api/benefits/{id}/explain`, `POST /api/benefits/{id}/calendar`
- `POST /api/wage-check`
- `GET /api/exchange-rate/insight`, `GET /api/fees`
- `GET/POST /api/calendar`, `DELETE /api/calendar/{id}`

**중요**: 실제 호출이 실패하면(백엔드가 아직 없거나, 안 켜져 있거나, 에러가 나면) 자동으로 `api/mock.js`의 가짜 데이터로 대체돼요. 그래서 백엔드 팀원 작업이 아직 안 끝났어도 화면은 항상 정상적으로 보여요. 브라우저 개발자도구 콘솔(F12)을 열어두면 지금 실제 응답을 쓰고 있는지 mock을 쓰고 있는지 경고 메시지로 알 수 있어요.

백엔드 주소가 정해지면 `.env` 파일의 `VITE_API_BASE_URL`만 바꾸면 됩니다. 코드는 안 건드려도 돼요.

## 실행 방법

1. [Node.js](https://nodejs.org) 설치 (LTS 버전)
2. 터미널에서 이 폴더로 이동 후:
   ```
   npm install
   ```
3. `.env.example`을 복사해서 `.env` 파일 만들기 (Supabase 값은 아직 없으면 비워둬도 돼요 — 그러면 구글 로그인 버튼이 자동으로 "가짜 로그인"으로 동작해서 화면 테스트는 계속 가능해요):
   ```
   cp .env.example .env
   ```
4. 개발 서버 실행:
   ```
   npm run dev
   ```
   터미널에 뜨는 `http://localhost:5173` 주소를 브라우저로 열면 돼요.
5. 실제 배포용 빌드 (백엔드 팀원에게 전달할 때):
   ```
   npm run build
   ```
   `dist/` 폴더가 생기는데, 이걸 어떤 서버에 올려도(Vercel, Netlify, 백엔드 서버의 static 폴더 등) 그대로 동작해요.

## GitHub에 올리기

이미 저장소랑 브랜치 보호 규칙(main 승인 1명, dev는 자유)이 있으시니, 보통 이렇게 하시면 돼요:

```
git checkout dev
git pull

# 이 폴더 안의 파일들을 저장소 폴더에 복사해 넣은 다음:
git add .
git commit -m "feat: 프론트엔드 초기 구현 + API 연동"
git push origin dev
```

`main`에 반영하려면 GitHub에서 `dev` → `main` 으로 Pull Request를 열고, 팀원 승인을 받은 뒤 머지하면 돼요.

## 지금 당장 손볼 게 있다면

- `src/pages/WagePage.jsx`의 2026년 최저임금(10,320원) 같은 하드코딩된 값은 실제 정책에 맞게 확인해보세요.
- 로그인 화면 카피, 팀/서비스 이름 등 실제 프로젝트에 맞게 텍스트만 바꾸면 데모 준비 끝이에요.
