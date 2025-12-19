## grabPT-backend

# 서버 아키텍처 다이어그램
<img width="1319" height="689" alt="Image" src="https://github.com/user-attachments/assets/3d90601c-b10e-446d-a9ae-037cd6876785" />

# grabPT

**grabPT**는 트레이너와 일반 회원을 연결하는 PT 매칭 플랫폼입니다.  
회원은 운동 목적, 일정, 위치, 예산 등을 담은 **요청서**를 작성하고, 트레이너(프로)는 해당 요청서에 맞춘 **제안서**를 제출하여 1:1 맞춤 PT 서비스를 제공합니다.

### 1. 회원가입 & 로그인
- 일반 회원 / 트레이너 프로필 구분
- 이메일·비밀번호 회원가입
- Google / Kakao / Naver 소셜 로그인 (OAuth2)
- AWS S3 기반 프로필 이미지 업로드

### 2. 요청서 & 제안서
- **회원**: 운동 목적, 일정, 위치 등 상세 조건 입력
- **트레이너**: 가격, 수업 횟수, 센터 위치, 경력 입력
- 요청서별 제안서 목록 페이지네이션 조회

### 3. 매칭 및 필터링
- JWT 기반 인증
- 트레이너: 활동 지역과 일치하는 요청서 조회
- 회원: 제출된 제안서 목록 확인

### 4. 파일 & 데이터 관리
- AWS S3 이미지 저장
- JPA 엔티티 관리
- Querydsl 기반 복잡한 조회 구현

### 5. 인프라 & 배포
- **Backend**: Spring Boot
- **Database**: MySQL 8.0
- **Infra**: AWS VPC, EC2, S3, IAM, RDS, Code Deploy, Nginx Reverse Proxy, Nginx SSH
- **Frontend**: Vercel 배포

### 디렉토리 구조
```
main         ← 실제 운영(배포)되는 코드
├─ develop   ← 기능 개발이 끝나고 병합되는 중간 브랜치
│   ├─ feat/*     ← 새로운 기능 개발
│   ├─ fix/*      ← 버그 수정
│   ├─ refactor/* ← 리팩토링
│   └─ test/*     ← 테스트 코드 추가 등
├─ release/* ← 배포 전 테스트 준비용 브랜치
└─ hotfix/*  ← 배포 중 긴급 수정
```

### 팀 규칙 제안
- **main**: 절대 직접 작업 금지, 배포용 코드만
- **develop**: PR로만 병합, 각 기능 브랜치의 병합 대상
- **기능 브랜치 이름**:
    - feat: 기능 개발
    - fix: 버그 수정
    - refactor: 리펙토링
    - test: 테스트 코드 추가
- **PR 대상**:
    - 기능 브랜치 → develop
    - release/hotfix → main

### Git 브랜치 전략
```
- main: 운영/배포용 브랜치
- develop: 개발용 통합 브랜치
- 기능 브랜치: feat/*, fix/*, refactor/*, test/*
- 릴리즈 브랜치: release/*
- 긴급 수정 브랜치: hotfix/*

PR 규칙:
- 모든 브랜치는 PR을 통해 병합
- 기능 완료 후 develop로 PR
- 배포 전 release로 분기, 테스트 후 main 병합
```
- main은 절대 손대지 말고, 모든 기능은 develop에서 브랜치를 따서 작업하고, 작업 완료 후 develop으로 PR 보내주세요

---

## 프로젝트 구조
### 계층형 구조

```
com.projectname
├─ controller
├─ service
├─ repository
├─ domain
├─ dto
├─ ...
├─ apiPayload
├─ validation
└─ config
```
## 템플릿

### Issue 탬플릿

```
---
name: Feature Request
about: 새로운 기능 구현을 위한 이슈 등록
title: "[Feat] 기능 제목"
labels: enhancement
assignees: ''

---

## 기능 설명
- 어떤 기능인지 구체적으로 적어주세요.

## 작업 내용
- [ ] 작업 1
- [ ] 작업 2
- [ ] 작업 3

## 💬 기타 참고 사항
- 연관된 이슈나 문서가 있다면 링크 걸어주세요.
```

### PR 탬플릿

```
## PR 제목
- [Feat] 회원가입 API 구현
- [Fix] 로그인 예외 처리 수정

## 변경 사항
- 어떤 기능을 개발/수정했는지 간단히 요약
- 예: 회원가입 API 구현, 유효성 검사 추가 등

## 작업 내용 체크리스트
- [ ] 기능 동작 확인
- [ ] 테스트 코드 작성
- [ ] 코드 리뷰 반영

## 관련 이슈
- 관련 이슈 번호: #12

## 기타 공유 사항
- 테스트 시 유의사항이나 논의할 포인트 등
```

---

## ERD 설계
- https://dbdiagram.io/d/Copy-of-Copy-of-Untitled-Diagram-68665d7ff413ba35081ba57a
