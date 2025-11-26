# IPiece - Backend 💻

> **블록체인 기반 캐릭터 IP 증권형 토큰(STO) 거래 플랫폼**

IPiece는 캐릭터 IP를 증권형 토큰(STO)으로 발행하여, 개인 투자자들이 소액으로 유망 캐릭터 IP에 투자하고 배당 수익을 얻을 수 있는 STO 플랫폼입니다.

<br>

---

## 📚 목차

- [1. 프로젝트 소개](#1-프로젝트-소개)
- [2. 팀원 및 역할](#2-팀원-및-역할)
- [3. 기술 스택 및 선정 이유](#3-기술-스택-및-선정-이유)
- [4. ERD 및 프로젝트 구조](#4-ERD-및-프로젝트-구조)
- [5. 페이지별 주요 기능](#5-페이지별-주요-기능)
- [6. 설치 및 실행](#6-설치-및-실행)
- [7. 트러블슈팅](#7-트러블슈팅)
- [8. 관련 링크](#8-관련-링크)
  
---

## 1. 프로젝트 소개

<br>

###  배경 및 목적

기존 캐릭터 IP 투자 시장은 높은 진입 장벽으로 인해 대형 투자자와 기관에게만 기회가 열려 있었습니다. IPiece는 블록체인 기술을 활용해 **캐릭터 IP를 증권형 토큰으로 조각(Piece)내어**, 누구나 소액으로 간편하게 투자할 수 있는 민주화된 투자 환경을 제공하는 것을 목표로 합니다.

<br>


### 🗓️ 프로젝트 기간

- **총 개발 기간:** 2025.10.21 ~ 2025.12.5 (7주)

<br>

### 🎯 주요 목표
- Primary Offering(공모) 기능 구현
- Secondary Trading(2차 거래) 엔진 구축
- 사용자 보유 자산/수익률 시스템
- Redis 성능 최적화 및 캐싱 전략 적용
- On-premise Blockchain(Besu) 연동
- 고가용성·확장성을 고려한 백엔드 아키텍처 설계
<br>

---

## 2. 팀원 및 역할

<br>


<div align="left">

<table width="100%">
  <tr>
    <td align="center" width="20%">
      <img src="https://github.com/kohtaewoo.png" width="120" height="120" style="border-radius:50%;"/><br/>
      <b>고태우</b><br/>
      <sub>PM / 블록체인/ Infra</sub><br/>
      <a href="https://github.com/kohtaewoo">@kohtaewoo</a>
    </td>
    <td align="center" width="20%">
      <img src="https://github.com/LeeJoEun-01.png" width="120" height="120" style="border-radius:50%;"/><br/>
      <b>이조은</b><br/>
      <sub>PL / FE / BE</sub><br/>
      <a href="https://github.com/LeeJoEun-01">@LeeJoEun-01</a>
    </td>
    <td align="center" width="20%">
      <img src="https://github.com/kkangsol.png" width="120" height="120" style="border-radius:50%;"/><br/>
      <b>강한솔</b><br/>
      <sub>FE / BE</sub><br/>
      <a href="https://github.com/kkangsol">@kkangsol</a>
    </td>
    <td align="center" width="20%">
      <img src="https://github.com/GIHYUN-LEE.png" width="120" height="120" style="border-radius:50%;"/><br/>
      <b>이기현</b><br/>
      <sub>Infra / BE</sub><br/>
      <a href="https://github.com/GIHYUN-LEE">@GIHYUN-LEE</a>
    </td>
    <td align="center" width="20%">
      <img src="https://github.com/Gill010147.png" width="120" height="120" style="border-radius:50%;"/><br/>
      <b>황병길</b><br/>
      <sub>블록체인 / Infra</sub><br/>
      <a href="https://github.com/Gill010147">@Gill010147</a>
    </td>
  </tr>
</table>

</div>

<br>

---


## 3. 기술 스택 및 선정 이유



### ⚙️ **Backend**

| 기술 | 선정 이유 |
| :--- | :--- |
| <img src="https://img.shields.io/badge/Java_17-007396?style=flat&logo=openjdk&logoColor=white"/> | 금융권에서 널리 사용하는 안정적인 LTS 버전으로, 성능·보안·호환성이 우수합니다. |
| <img src="https://img.shields.io/badge/Spring_Boot_3-6DB33F?style=flat&logo=SpringBoot&logoColor=white"/> | Web, Security, JPA 등 생태계가 완성되어 있어 STO 백엔드 구축에 최적화되어 있습니다. |
| <img src="https://img.shields.io/badge/Spring_Security-6DB33F?style=flat&logo=SpringSecurity&logoColor=white"/> | 인증·인가 로직을 안정적으로 처리하며, 확장 가능한 보안 구조를 제공합니다. |
| <img src="https://img.shields.io/badge/JWT-000000?style=flat&logo=jsonwebtokens&logoColor=white"/> | 토큰 기반 인증으로 무상태(State-less) 서버 구조를 만들고, 모바일·웹·서버 간 확장성을 높입니다. |
| <img src="https://img.shields.io/badge/Spring_Data_JPA-59666C?style=flat&logo=Hibernate&logoColor=white"/> | 도메인 중심으로 개발 가능하며 반복적인 SQL 작성 없이 생산성 높은 개발이 가능합니다. |




### 🗄 **Database & Storage**

| 기술 | 선정 이유 |
| :--- | :--- |
| <img src="https://img.shields.io/badge/PostgreSQL-4169E1?style=flat&logo=PostgreSQL&logoColor=white"/> | 트랜잭션 안정성과 JSON·파티셔닝 등 다양한 기능을 제공하여 공모·거래 데이터 처리에 최적입니다. |
| <img src="https://img.shields.io/badge/Redis-DC382D?style=flat&logo=Redis&logoColor=white"/> | 인기순위·조회수·세션·토큰블랙리스트 등 초고속 처리 필요한 데이터 구조에 적합합니다. |
| <img src="https://img.shields.io/badge/Amazon_S3-569A31?style=flat&logo=AmazonS3&logoColor=white"/> | 이미지·문서 등의 저장에 안전하며, CDN과 연계해 빠른 응답 속도를 제공합니다. |
| <img src="https://img.shields.io/badge/DBeaver-372923?style=flat&logo=DBeaver&logoColor=white"/> | PostgreSQL 스키마 관리, 쿼리 테스트에 유용한 DB GUI 도구입니다. |




### 🔗 **Blockchain**

| 기술 | 선정 이유 |
| :--- | :--- |
| <img src="https://img.shields.io/badge/Hyperledger_Besu-2F3134?style=flat&logo=LinuxFoundation&logoColor=white"/> | Private Blockchain 기반으로 STO 서비스에서 필요한 투명성과 보안성을 동시에 충족합니다. |
| <img src="https://img.shields.io/badge/Web3j-E34F26?style=flat&logo=Ethereum&logoColor=white"/> | Java 기반 블록체인 연동 라이브러리로, Spring Boot 환경과 자연스럽게 통합됩니다. |




### 🧪 **Test & API Tools**

| 기술 | 선정 이유 |
| :--- | :--- |
| <img src="https://img.shields.io/badge/Postman-FF6C37?style=flat&logo=postman&logoColor=white"/> | API 테스트 및 시나리오 검증에 사용해 실제 동작을 빠르게 검증할 수 있습니다. |
| <img src="https://img.shields.io/badge/Swagger-85EA2D?style=flat&logo=Swagger&logoColor=black"/> | API 문서를 자동 생성하며, 테스트 가능한 인터페이스를 제공해 협업 효율이 증가합니다. |



### 🛠 **Development Environment**

| 기술 | 선정 이유 |
| :--- | :--- |
| <img src="https://img.shields.io/badge/IntelliJ_IDEA-000000?style=flat&logo=IntelliJIDEA&logoColor=white"/> | 가장 강력한 Spring Boot 개발 환경을 제공하며 생산성을 극대화합니다. |
| <img src="https://img.shields.io/badge/GitKraken-179287?style=flat&logo=GitKraken&logoColor=white"/> | 시각적인 Git 브랜치/커밋 관리로 협업과 형상관리 효율을 높여줍니다. |
| <img src="https://img.shields.io/badge/GitHub-181717?style=flat&logo=github&logoColor=white"/> | 코드 리뷰·이슈 관리·PR 기반 협업 등 전체 개발 프로세스의 중심 역할을 합니다. |




### 🤝 **Collaboration Tools**

| 기술 | 선정 이유 |
| :--- | :--- |
| <img src="https://img.shields.io/badge/Slack-4A154B?style=flat&logo=slack&logoColor=white"/> | 팀 커뮤니케이션, 알림, 개발 로그 공유에 사용했습니다. |
| <img src="https://img.shields.io/badge/Notion-000000?style=flat&logo=notion&logoColor=white"/> | 회의록, 일정관리, API 명세, 문서화를 한 곳에서 관리해 협업 효율이 높아졌습니다. |



---


## 4. ERD 및 프로젝트 구조


### 🕸️ ERD

<img width="3730" height="1442" alt="Copy of IPiece ERD" src="https://github.com/user-attachments/assets/eb7eac0e-e88e-482b-be02-167d57db1f6c" />


### 📁 프로젝트 구조
본 프로젝트는 `Domain-Oriented Feature-Based Structure`(도메인 중심 기능 구조)를 채택했습니다.  
기능 영역(admin, user, market, offering 등)별로 패키지를 구성하여 유지보수성과 확장성을 극대화하고,  
DDD(Domain-Driven Design)의 모듈 설계 방식과도 일관성을 맞추었습니다.


```
IPiece-server/
┣ 📂 src/
┃ ┣ 📂 main/
┃ ┃ ┣ 📂 java/com/masterpiece/IPiece/
┃ ┃ ┃ ┣ 🧩 admin # 관리자(공모 승인, 배당관리, 거래활성화)
┃ ┃ ┃ ┃ ┣ 📁 blockchain
┃ ┃ ┃ ┃ ┣ 📁 dividend
┃ ┃ ┃ ┃ ┗ 📁 offeringandtrade
┃ ┃ ┃
┃ ┃ ┃ ┣ 🔗 blockchain # KRWT, TokenFactory, 트랜잭션, 지갑
┃ ┃ ┃ ┃ ┣ 📁 api
┃ ┃ ┃ ┃ ┣ 📁 controller
┃ ┃ ┃ ┃ ┣ 📁 dto
┃ ┃ ┃ ┃ ┣ 📁 application
┃ ┃ ┃ ┃ ┣ 📁 domain
┃ ┃ ┃ ┃ ┗ 📁 infra
┃ ┃ ┃
┃ ┃ ┃ ┣ ⚙️ common # 공통 엔티티, 예외, Response, JWT 유틸
┃ ┃ ┃ ┃ ┣ 📁 domain
┃ ┃ ┃ ┃ ┣ 📁 infra
┃ ┃ ┃ ┃ ┣ 📁 exception
┃ ┃ ┃ ┃ ┣ 📁 http
┃ ┃ ┃ ┃ ┣ 📁 id
┃ ┃ ┃ ┃ ┣ 📁 util
┃ ┃ ┃ ┃ ┗ 📁 web
┃ ┃ ┃
┃ ┃ ┃ ┣ 🛡️ config # 전체 Spring 설정
┃ ┃ ┃ ┃ ┣ 🔐 SecurityConfig
┃ ┃ ┃ ┃ ┣ 🔐 JwtAuthenticationFilter
┃ ┃ ┃ ┃ ┣ 📘 SwaggerConfig
┃ ┃ ┃ ┃ ┗ 🔌 WebSocketConfig
┃ ┃ ┃
┃ ┃ ┃ ┣ 💰 dividends # 배당(등록, 실행, 온체인 처리)
┃ ┃ ┃ ┃ ┗ 📁 api / application / domain / infra
┃ ┃ ┃
┃ ┃ ┃ ┣ ⭐ favorite # 즐겨찾기
┃ ┃ ┃ ┃ ┗ 📁 api / application / domain / infra
┃ ┃ ┃
┃ ┃ ┃ ┣ 🩺 health # Health Check
┃ ┃ ┃ ┃ ┗ HealthCheckController
┃ ┃ ┃
┃ ┃ ┃ ┣ 🔌 integration # 외부 연동
┃ ┃ ┃ ┃ ┣ 📁 besu # RPC / EVM 연동
┃ ┃ ┃ ┃ ┣ 📁 sms # 인증번호(LG/Solapi)
┃ ┃ ┃ ┃ ┗ 📁 storage # S3 업로드
┃ ┃ ┃
┃ ┃ ┃ ┣ 📈 investment # 투자 API (Stateless)
┃ ┃ ┃ ┃ ┗ 📁 api / application / domain / infra
┃ ┃ ┃
┃ ┃ ┃ ┣ 🏠 main # 메인 페이지 데이터
┃ ┃ ┃ ┃ ┗ 📁 api / application / domain / infra
┃ ┃ ┃
┃ ┃ ┃ ┣ 📉 market (2차 거래) # 매칭엔진, 차트, 오더북
┃ ┃ ┃ ┃ ┣ 📁 api
┃ ┃ ┃ ┃ ┣ 📁 application
┃ ┃ ┃ ┃ ┣ 📁 domain
┃ ┃ ┃ ┃ ┗ 📁 infra
┃ ┃ ┃
┃ ┃ ┃ ┣ 🧾 mypage # 자산/계좌/거래내역/포트폴리오
┃ ┃ ┃ ┃ ┗ 📁 api / application / domain / infra
┃ ┃ ┃
┃ ┃ ┃ ┣ 📮 offering (1차 공모) # 청약, 검증, 진행률 업데이트
┃ ┃ ┃ ┃ ┗ 📁 api / application / domain / infra
┃ ┃ ┃
┃ ┃ ┃ ┗ 👤 user # 회원가입/인증/JWT/블랙리스트
┃ ┃ ┃ ┗ 📁 api / application / domain / infra
┃ ┃
┃ ┗ 📂 test/ # 단위/통합 테스트
┃ ┗ (컨트롤러, 서비스 테스트)
┗ 📄 build.gradle

```



---

## 5. 페이지별 주요 기능

<br>

아래는 실제 서비스 플로우 기준으로 구성한 **페이지별 상세 기능 설명 + 사용 API 목록**입니다.  
모든 화면은 Swagger 기반 REST API를 기반으로 동작합니다.

<br>

## 1. **메인 페이지 (`/`)**

| 제목 | 화면 캡처 |
| ---- | -------- |
| 메인 페이지 | <img src="https://github.com/user-attachments/assets/2f7bd517-8a1a-4299-8938-876af72fa53f" width="800"/>
 |

### 🔗 사용 API
- `GET /v1/main/home` — 메인 페이지 데이터


### 🔍 상세 기능
- 공모, 거래 중인 캐릭터 IP 4개씩 노출  
- 로그인 여부에 따라 Header 구성  
- 공모·거래 상세 페이지로 이동

<br>


## 2. **로그인 · 회원가입 (`/auth/*`)**

### 2-1. 로그인 페이지 (`/auth/login`)

| 제목 | 화면 캡처 |
| ---- | -------- |
| 로그인 페이지 | <img width="800" alt="image" src="https://github.com/user-attachments/assets/80efb376-8def-4fd8-be12-13cbf0ec1c97" /> |

### 🔗 사용 API
- `POST /v1/auth/token/login`
- `POST /v1/auth/token/logout`
- `POST /v1/auth/token/refresh`

<br>


### 2-2. 본인인증 페이지 (`/auth/verification`)

| 제목 | 화면 캡처 |
| ---- | -------- |
| 본인인증 페이지 | <img width="800" alt="image" src="https://github.com/user-attachments/assets/ac84a4b1-33e0-49c0-8d5c-4f9ca5f32032" /> |
| 본인인증 SMS | <img width="800" alt="image" src="https://github.com/user-attachments/assets/d22f86b4-e610-427c-907f-9fb7daf4983c" /> |

### 🔗 사용 API
- `POST /v1/auth/otp/start?phone=`
- `POST /v1/auth/otp/verify?phone=&code=&birth=`

<br>

### 2-3. 회원가입 페이지 (`/auth/signup`)

| 제목 | 화면 캡처 |
| ---- | -------- |
| 회원가입 페이지 | <img src="https://github.com/user-attachments/assets/e134a766-ba24-496f-af85-ef4136b85734" width="800"/> |



### 🔗 사용 API
- `GET /v1/signup/duplicate-check?id=`
- `POST /v1/signup/info` *(multipart/form-data)*  

<br>

## 3. **공모(1차 시장) 페이지 (`/offering`)**

| 제목 | 화면 캡처 |
| ---- | -------- |
| 공모 리스트 | <img src="https://github.com/user-attachments/assets/33ac435a-8a1f-4aff-b312-e5bb0e006e50" width="800"/> |
| 공모 상세 페이지 | <img src="https://github.com/user-attachments/assets/4ee52017-a78a-4b27-88ae-dc6c8593660a" width="800"/> |
| 공모 참여 | <img src="https://github.com/user-attachments/assets/7042098e-9457-4851-b066-2fe7d52b06b3" width="800"/> |


### 🔗 사용 API
- `GET /v1/offerings?cursor=` — 공모 리스트
- `GET /v1/offerings/{product_id}/detail` — 공모 상세
- `GET /v1/offerings/{product_id}/purchase/validate?quantity=` — 청약 가능 여부
- `POST /v1/offerings/{product_id}/purchase` — 공모 참여

### 🔍 상세 기능
- 무한스크롤 기반 리스트  
- 공모 가격/기간/진행률 표시  
- 잔액·기간·공모 가능 여부 검증  
- Progress Rate 실시간 업데이트  
- 공모 종료 시 상태 자동 전환

<br>

## 4. **2차 거래 페이지 (`/trading/[id]`)**

| 제목 | 화면 캡처 |
| ---- | -------- |
| 거래 리스트 | <img width="800" alt="image" src="https://github.com/user-attachments/assets/a2087702-6150-4992-8b70-e7a4686e02a7" /> |
| 거래 상세 – 차트/호가 | <img width="800" alt="image" src="https://github.com/user-attachments/assets/3f0cd2be-653b-415c-b6e4-e3101ab348e6" /> |
| 거래 상세 – 공시/소개 | <img width="800" alt="image" src="https://github.com/user-attachments/assets/9fa6a76d-b2a7-4f58-bb0c-0df1d9b94f39" /> |

### 🔗 사용 API
- `GET /v1/market/{product_id}/details`
- `GET /v1/market/{product_id}/chart?interval=`
- `GET /v1/market/{product_id}/orders`
- `POST /v1/market/{product_id}/buy`
- `POST /v1/market/{product_id}/sell`
- `GET /v1/market/{product_id}/orders/pending`

### 🔍 상세 기능
- 실시간 차트 (라인/캔들)
- 실시간 호가창 (매수/매도)
- 지정가 매수/매도 주문
- Idempotency-Key 기반 **중복 주문 방지**
- 공시/프로젝트 소개 탭 제공
- 체결/미체결 내역 조회

<br>


## 5. **마이페이지 (`/mypage`)**

| 제목 | 화면 캡처 |
| ---- | -------- |
| MY HOME – 자산 있음 | <img width="800" alt="image" src="https://github.com/user-attachments/assets/b81e6e40-0b8b-4478-938f-476a3ce4bac8" /> |
| MY HOME – 자산 없음 | <img width="800" alt="image" src="https://github.com/user-attachments/assets/1748bf81-4a68-4387-83e5-a9e8efc4dd7c" /> |
| 내 계좌 화면 | <img width="800" alt="image" src="https://github.com/user-attachments/assets/6a425bc0-ed7d-4de6-b4ff-9448f3463c24" /> |






### 5-1. ⭐ MY HOME (`/mypage/myhome`)

### 🔗 사용 API
- `GET /v1/mypage/myhome`

### 🔍 상세 기능
- 총 자산 / 보유 자산 / 평가손익  
- 포트폴리오 파이 차트  
- 공모참여 내역 + 보유 종목 통합 조회  
- 실시간 가격 반영
- 계좌 생성성


### 5-2. ⭐ 내 계좌 (`/mypage/account`)

### 🔗 사용 API
- `POST /v1/mypage/account` — 가상계좌 생성
- `GET /v1/mypage/account?date_from=&date_to=` — 입출금 조회

### 🔍 상세 기능
- KRW 잔액/출금 가능 금액 표시  
- 입금/출금/보류금(pending) 조회  
- 거래 시 자동 저널 기록 반영  



### 5-3. ⭐ 거래 내역 (`/mypage/account/journals`)

### 🔗 사용 API
- `GET /v1/mypage/account/journals`

### 🔍 상세 기능
- 매수/매도/입금/출금/배당 전체 기록 조회  
- 날짜 범위 필터 제공

  
<br>

## 6. **관심 목록 (`/mypage/interest`)**

| 제목 | 화면 캡처 |
| ---- | -------- |
| 관심 화면 | <img width="800" alt="image" src="https://github.com/user-attachments/assets/05640eb0-940b-4cda-8e35-2ad8347c52d3" /> |

### 🔗 사용 API
- `POST /v1/products/{product_id}/favorite`
- `DELETE /v1/products/{product_id}/favorite`
- `POST /v1/favorites/status`
- `GET /v1/mypage/favorites`

### 🔍 상세 기능
- 즐겨찾기한 캐릭터 리스트 조회  
- 실시간 가격 반영  
- 클릭 시 거래 상세 페이지 이동  

<br>

## 7. **관리자(Admin) 페이지 (`/admin/*`)**

| 제목 | 화면 캡처 |
| ---- | -------- |
| Admin 상품 조회 | <img width="800" alt="image" src="https://github.com/user-attachments/assets/b7f26fad-e6ef-454e-8043-e404d831b594" /> |
| Admin 상품 생성 | <img width="800" alt="image" src="https://github.com/user-attachments/assets/cea9c8eb-89bc-49df-b036-1c3d3867c85c" /> |
| Admin 공모 -> 2차거래 | <img width="800" alt="image" src="https://github.com/user-attachments/assets/af9755c1-2cbf-45eb-8ac7-792d4411bb47" /> |
| Admin 배당 관리 | <img width="800" alt="image" src="https://github.com/user-attachments/assets/589e869c-42c4-4a3c-8446-7650b06cd818" /> |
| 컨트랙트/트랜잭션 모니터링 | *(이미지 예정)* |



<br>

### 7-1. ⭐ 상품 관리

### 🔗 사용 API
- `POST /v1/admin/products` — 상품 생성 (이미지 포함)
- `POST /v1/admin/products/{productId}/enable-offering` — 공모 오픈 승인

### 🔍 상세 기능
- 상품 기본 정보 + 이미지 업로드  
- 공모 일정·가격·수량 설정  
- 공모 OPEN 승인 처리  
- 공모 종료 후 자동 TRADE 전환 지원  



### 7-2. ⭐ 배당 관리

### 🔗 사용 API
- `POST /v1/admin/dividends` — 배당 등록/업데이트
- `GET /v1/admin/dividends` — 배당 리스트
- `GET /v1/admin/dividends/{dividendId}/payouts` — 지급 내역

### 🔍 상세 기능
- 배당 등록/수정  
- 배당 실행 → 온체인 트랜잭션 처리  
- 지급 성공/실패 내역 조회  
- 배당 대상자 수·총액 자동 계산  



### 7-3. ⭐ 블록체인 / 컨트랙트 / 트랜잭션 관리

### 🔗 사용 API
- `GET /v1/admin/blockchain/contracts` — 전체 컨트랙트 정보
- `GET /v1/admin/blockchain/transactions` — 온체인 트랜잭션 로그
- `GET /v1/admin/blockchain/tokens` — 발행된 모든 토큰 리스트
- `POST /v1/blockchain/wallet/krwt/mint` — KRWT 민트
- `POST /v1/blockchain/wallet/krwt/burn` — KRWT 소각

### 🔍 상세 기능
- KRWT 민팅/소각  
- TokenFactory 기반 프로젝트 토큰 생성  
- 트랜잭션 상태 모니터링 (PENDING/FAILED)  
- Gas/Block 등 상세 메타데이터 조회  
- Holder 변동 추이 확인  


---



## 6. 설치 및 실행


### 1) 프로젝트 클론
```bash
git clone https://github.com/your-org/IPiece-backend.git
cd IPiece-backend
```

### 2) 환경 변수 설정
env 파일에 DB, Redis, Blockchain RPC, AWS 자격증명 등을 입력합니다.
```bash
cp .env.example .env
```


### 3) 로컬 실행

```bash
./mvnw spring-boot:run

```


### 4) Docker Compose 실행

```bash
docker-compose up -d

```

---


## 7. 트러블슈팅





---


## 8. 관련 링크
* 👩‍💻 [Frontend Repository](https://github.com/Woori-FISA-Go/IPiece-web)
* ⛓️ [Blockchain Repository](https://github.com/Woori-FISA-Go/IPiece-blockchain)

