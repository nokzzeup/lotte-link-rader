# 🔴 Lotte-Link Radar

> 롯데 자이언츠 홈경기 예매 취소표 실시간 모니터링 데스크톱 앱

![Java](https://img.shields.io/badge/Java-17-orange?logo=java)
![Swing](https://img.shields.io/badge/UI-Java%20Swing-blue)
![License](https://img.shields.io/badge/license-MIT-green)

---

## 📌 소개

롯데 자이언츠 팬을 위한 홈경기 예매 취소표 실시간 모니터링 프로그램입니다.  
매진된 경기의 취소표 발생을 실시간으로 감지하고, 찜한 경기를 관리하며 예매 페이지로 바로 이동할 수 있습니다.

---

## ✨ 주요 기능

### 🎫 경기 일정 & 잔여석 모니터링
- 홈경기 잔여석 실시간 조회 (15초 자동 갱신)
- 취소표 발생 시 배지 `+n석` 강조 + 상단 티커 알림
- 매진 / 현장예매 / 예매중 배지 자동 전환
- 찜한 경기 핑크색 강조 표시

### ♥ 내 찜 목록
- 월별 캘린더 뷰로 경기 일정 확인
- 경기 클릭 → 찜하기 / 예매하기 바로 이동
- 이벤트 경기 관리 (관리자 기능)
- 예매중 / 이벤트 / 찜 태그 자동 표시
- 월별 홈·원정·이벤트·찜 통계

### 📢 공지사항
- 롯데 자이언츠 공식 공지 자동 크롤링
- 이벤트 / 예매 / 공지 / 기타 필터
- 이미지 포함 공지 그대로 표시
- 관리자 공지 직접 추가/수정/삭제
- 상단 티커 공지 자동 순환

### 🏆 팀 순위
- KBO 공식 홈페이지 실시간 순위 크롤링
- 롯데 행 강조 표시
- 전날 기준 날짜 표시

### 📊 상단 대시보드
- 오늘 경기 자동 표시
- 롯데 현재 순위 실시간 반영
- 다음 이벤트 D-day 자동 계산

---

## 🛠 기술 스택

| 항목 | 내용 |
|------|------|
| Language | Java 17 |
| UI | Java Swing |
| IDE | IntelliJ IDEA |
| 데이터 수집 | HttpURLConnection, HTML 파싱 (정규식) |
| 데이터 저장 | 로컬 파일 (txt) |

---

## 🚀 실행 방법

### 요구 사항
- JDK 17 이상
- 롯데 자이언츠 예매 사이트 계정 (JSESSIONID 필요)

### 실행

```bash
git clone https://github.com/nokzzeup/lotte-link-rader.git
cd lotte-link-rader
```

IntelliJ IDEA에서 프로젝트 열고 `Main.java` 실행

### JSESSIONID 입력 방법
1. 브라우저에서 [롯데 자이언츠 예매 사이트](https://ticket.giantsclub.com) 로그인
2. F12 → Application → Cookies → `JSESSIONID` 복사
3. 앱 실행 후 입력창에 붙여넣기

---

## 📁 프로젝트 구조

```
src/
├── Main.java               # 진입점
├── LoginFrame.java         # JSESSIONID 입력 화면
├── LoadingFrame.java       # 초기 데이터 로딩
├── MainFrame.java          # 메인 UI (탭 구조)
├── GameSchedule.java       # 경기 일정 데이터
├── SeatEngine.java         # 잔여석 HTTP 요청 & 파싱
├── SeatInfo.java           # 좌석 데이터 클래스
├── SeatDataCache.java      # 잔여석 캐시
├── WishManager.java        # 찜 목록 관리
├── WishCalendarPanel.java  # 찜 목록 캘린더 UI
├── Notice.java             # 공지 데이터 클래스
├── NoticeManager.java      # 공지 크롤링 & 관리
├── NoticeListPanel.java    # 공지사항 UI
├── KboService.java         # KBO 순위 크롤링
└── KboRankPanel.java       # 팀 순위 UI
```

---

## 📸 스크린샷

| 경기 일정 | 내 찜 목록 |
|-----------|-----------|
| 경기 일정 화면 | 찜 목록 캘린더 화면 |

| 공지사항 | 팀 순위 |
|----------|---------|
| 공지사항 화면 | KBO 팀 순위 화면 |

---

## ⚠️ 주의사항

- JSESSIONID는 로그인 세션 유지 시간에 따라 만료될 수 있습니다.
- 과도한 요청으로 인한 IP 차단을 방지하기 위해 조회 주기를 60초 이상으로 설정했습니다.
- 본 프로그램은 학습 목적으로 제작되었습니다.

---

## 👥 개발

동의대학교 컴퓨터공학과 팀 프로젝트 (2026)
