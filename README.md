## Perso.ai FAQ Chatbot

OpenAI Embedding + Qdrant Vector DB 기반으로
FAQ 질문을 의미적으로 이해하고 가장 적절한 답변을 찾아주는 RAG 기반 챗봇입니다.<br><br>


### 사용 기술 스택
### Backend
- **Java 17**
- **Spring Boot 3**
  - **Spring Web** (REST API 개발)
  - **Spring WebFlux** (WebClient로 OpenAI API 호출)
  - **Spring MVC**
- **Apache POI** (엑셀 데이터 파싱)
- **Lombok**
- **Gradle**
  
### Frontend
- **React + Vite**
- **JavaScript**
- **Tailwind CSS** (UI 스타일)
- **Fetch API** (백엔드 API 통신)

### Vector & Embedding
- **OpenAI Embedding API** (`text-embedding-3-small`)
- **Qdrant Cloud** (의미 기반 유사도 검색)

### Infra
- **Railway** (백엔드 서버 배포)
- **Qdrant Cloud** (벡터 DB 호스팅)
- **Vercel** (프론트엔드 배포)<br><br>

### 벡터 DB 및 임베딩 방식

챗봇 프로젝트는 사용자가 입력한 질문을 **벡터(embedding)** 로 변환하고,  
벡터 데이터베이스에서 **가장 의미적으로 가까운 질문-답변을 탐색**하는  
**벡터 기반 RAG** 구조로 동작합니다. <br>


### 1️⃣ 엑셀 데이터 로딩

- Apache POI로 `faq.xlsx` 파일을 파싱하여  
  **질문(Question)**, **답변(Answer)** 쌍 추출
- “Q.”, “A.” 등의 접두어 제거 후 텍스트 정규화


### 2️⃣ OpenAI Embedding 변환

- 질문 텍스트를 OpenAI의 `text-embedding-3-small` 모델로 변환
- **1536차원**의 벡터로 변환됨


### 3️⃣ Qdrant에 벡터 + payload 저장

- 벡터: 임베딩
- payload: 실제 질문/답변 텍스트

### 4️⃣ 의미 기반 검색(Search)

- 사용자 질문 → 임베딩 변환 → Qdrant에 Top-K 검색 요청
  - Qdrant가 유사도 점수와 함께 후보 FAQ들을 반환 <br>

**Qdrant에서 Top-K(5개) 후보를 가져오지만  
최종 답변은 유사도가 가장 높은 Top-1 결과를 기준으로 선택합니다.**
<br><br>

 
### 정확도 향상 전략

### 1. Top-K Retrieval
- 단순히 Top-1 결과만 사용하는 것이 아닌  
  **Top-K(5개)** 후보를 받아 Score 기반으로 최종 답변을 결정  
- 잘못 매칭된 결과를 필터링하여 정확도 향상


### 2. 유사도 Threshold 적용
- `SCORE_THRESHOLD = 0.65`  
- 유사도 점수가 너무 낮으면 오답 가능성이 높아
  **“적절한 답변을 찾지 못했습니다.”** 메시지 반환  


### 3. 정규화된 데이터 사용
- 질문 텍스트 전처리 수행하여 검색 정확도 향상
  - `Q.` / `A.` 접두어 제거  
  - 공백 제거(trim)  


### 4. OpenAI 임베딩 모델 적용
- **`text-embedding-3-small`** 모델 사용  
- 1536차원 벡터로 정교한 의미 매칭 가능


### 5. Qdrant HNSW 기반 고속 검색
- Qdrant는 기본적으로 **HNSW 인덱스를 자동 적용**하여 벡터를 저장
- Qdrant의 **HNSW(계층적 그래프) 알고리즘**을 통해  
  대량 데이터에서도 빠르게 Top-K 유사도 검색 수행  


