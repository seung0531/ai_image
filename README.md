AI Image Generator

AI 기반 이미지 생성 기능을 중심으로 제작한 풀스택 웹 애플리케이션입니다.

사용자가 회원가입 및 로그인을 통해 서비스를 이용하고, 프롬프트와 이미지 옵션을 입력하여 이미지를 생성할 수 있습니다.
생성된 이미지는 개인 이미지 기록으로 관리할 수 있으며, 공개 설정을 통해 다른 사용자와 공유하고 좋아요를 받을 수 있도록 구현했습니다.

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
  ├─ Controller
  ├─ Service
  ├─ Repository
  └─ Security
  ↓
MongoDB
  ↓
AI 이미지 생성 서버
