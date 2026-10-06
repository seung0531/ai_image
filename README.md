AI Image Generator

AI 기반 이미지 생성 기능을 중심으로 제작한 풀스택 웹 애플리케이션입니다.

사용자가 회원가입 및 로그인을 통해 서비스를 이용하고, 프롬프트와 이미지 옵션을 입력하여 이미지를 생성할 수 있습니다.
생성된 이미지는 개인 이미지 기록으로 관리할 수 있으며, 공개 설정을 통해 다른 사용자와 공유하고 좋아요를 받을 수 있도록 구현하였습니다.

개발 기간

2026.08 ~ 2026.10

사용 기술

- Frontend: React, Vite, React Router, JavaScript, Fetch API, CSS
- Backend: Java, Spring Boot, Spring Security, Spring Data MongoDB, JWT, Maven
- Database: MongoDB
- AI: ComfyUI, FLUX Schnell
- Development Tools: IntelliJ IDEA, VS Code, Git, GitHub, Postman

프로젝트 아키텍처

사용자
  ↓
React + Vite
  ↓
Spring Boot
  ├── Controller
  ├── Service
  ├── Repository ──→ MongoDB
  └── Security
        │
        └──────────→ AI 이미지 생성 서버

주요 기능

1. 회원가입 / 로그인
- 회원가입
- 로그인 / 로그아웃
- JWT 기반 인증
- HttpOnly Cookie를 이용한 인증 정보 관리
- 인증이 필요한 API 접근 제어
- CSRF 방어

2. AI 이미지 생성
- 프롬프트, Negative 프롬프트, 이미지 크기 설정을 통해 AI 이미지를 생성할 수 있습니다. 이미지 생성 진행 상태, 생성에 걸린 시간을 표시하고 생성된 이미지를 확인 및 다운로드 가능합니다.

3. 이미지 기록 관리
- 사용자가 생성한 이미지를 프롬프트 검색, 즐겨찾기, 다운로드 가능하고 이미지 상세 페이지를 확인할 수 있고 기존 프롬프트를 재사용
할 수 있습니다.

4. 공개 갤러리
- 생성한 이미지를 공개 설정을 하여 다른 사용자와 공유할 수 있습니다. 공개된 이미지는 최신순, 인기순 정렬이 가능하고 이미지의 상세 페이지를 확인할 수 있습니다.

5. 좋아요 기능
- 공개된 이미지에 좋아요를 남길 수 있습니다.

기술적으로 해결한 문제

- 문제1 JWT 인증과 CSRF 처리

문제: 로그인 이후 API 요청에서 인증과 CSRF 검증을 함께 처리해야 했습니다.

해결: Spring Security에서 JWT 기반 인증을 구성하고, 프론트엔드에서는 CSRF 토큰을 요청하여 상태 변경 API에 전달하도록 구현하였습니다.

- 문제2 AI 이미지 생성 요청 처리

문제: AI 이미지 생성은 일반적인 CRUD 요청보다 처리 시간이 길어 사용자가 요청이 진행 중인지 알기 어려웠습니다.

해결: 이미지 생성 요청 동안 Loading UI와 경과 시간을 표시하고, 생성 완료 후 결과 이미지를 화면에 표시하도록 구현하였습니다.

- 문제3 공개 이미지와 개인 이미지 분리

문제: 사용자의 이미지 기록과 공개 갤러리 이미지를 구분해야 했습니다.

해결:  `publicImage` 값을 기준으로 API 조회 범위를 분리하고, 공개 이미지에 대해서만 갤러리 접근 및 상세 조회가 가능하도록 구현하였습니다.

- 문제4 이미지 검색 및 페이징

문제: 이미지가 많아질 경우 모든 데이터를 한 번에 조회하면 비효율적이었습니다.

해결: 서버에서 검색 조건과 페이지 정보를 처리하고, 프론트엔드에서는 페이지 단위로 데이터를 조회하도록 구현하였습니다.

프로젝트 성과

- React + Vite / Spring Boot 기반 풀스택 웹 애플리케이션 구현
- JWT + Spring Security + CSRF를 적용한 인증 및 API 보안 구현
- AI 이미지 생성부터 저장, 검색, 즐겨찾기, 공개 갤러리까지 이미지 관리 기능 구현
- 좋아요 및 페이징을 적용한 공개 이미지 갤러리 구현
- Controller / Service / Repository 계층 분리를 통한 백엔드 구조 설계
- GlobalExceptionHandler를 통한 일관된 예외 응답 처리
- Postman을 활용한 주요 API 기능 및 인증/권한 동작 검증
- Git/GitHub를 활용한 프로젝트 버전 관리 및 협업 환경 구축
