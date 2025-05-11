# 장소 검색 서비스

## 프로젝트 설명
### 검색어에 대한 결과를 외부 API에서 조회하고, 검색어 순위를 집계하여 제공하는 서비스.
### [요구사항]
* 장소 검색 API(KAKAO, NAVER)를 통해 키워드 관련 장소를 검색한다.
* 두 API 검색 결과에 동일 업체로 판단되면 카카오 장소 검색 API의 결과를 상위로 정렬한다.
* 둘 중 하나만 존재하는 경우, 카카오 결과를 우선 배치 후 네이버 결과를 배치한다.
* 사용자들이 많이 검색한 순서대로 최대 10개의 검색 키워드 목록을 제공한다.

## 기술 스택
|영역|기술|
|--|--|
|Language|Java17|
|Framework|Spring boot 3.2.5|
|Build Tool|Gradle 8.2|
|Database|DB H2|
|Persistence|MyBatis|
|Cache|Redis (RedisTemplate)|
|Container|Docker|
|HTTP Client|WebClient|

## 아키텍처


## DB Table
* keyword_count : 키워드별 검색 횟수를 저장하기 위한 테이블

|필드명|타입|설명|
|--|--|--|
|keyword|VARCHAR(255)|검색어 (PRIMARY KEY)|
|count|BIGINT|검색 횟수|

## 라이브러리
* Lombok : 반복코드 제거 및 가독성 향상을 위해 사용.
* WebClient : 외부 API 호출을 위해 사용.
  * 향후 트래픽 증가나 성능 개선을 위해 비동기식 통신을 하게될 상황을 고려하였을때 확장성이 좋다고 판단하여 WebClient를 선택.
* RedisTemplate : 장소 검색 결과 데이터 캐싱 처리를 위해 사용.
  * H2(in-memory)DB를 사용한 프로젝트이기 때문에, Scale-out시 데이터의 일관성을 보장하기 위해 사용(확장성 고려).
* KafkaTemplate : 대용량 트래픽을 대비하기위해 사용.


## 실행 방법
#### Docker 설치 - Redis, Kafka 실행을 위해 Docker 설치 부탁드립니다.
* https://www.docker.com/products/docker-desktop/
  
```bash
# 1. docker 실행

# 2. 빌드
./gradlew clean build 실행

# 3. 실행
./gradlew bootRun 실행
```

## 구현 내용
* ### API 설명
  * #### 1) 장소 검색
    * ##### URI 정보
        |메서드|URI|
        |-|-|
        |GET|/v1/place|
      
    * ##### 요청 - 쿼리 파라미터
        |이름|타입|설명|
        |-|-|-|
        |keyword|String|검색어|
        
    * ##### 응답
        |이름|타입|설명|
        |-|-|-|
        |title|String|업체명|
        |address|String|주소|
        |roadAddress|String|도로명 주소|
        |longitude|String|경도|
        |latitude|String|위도|
        |categoryName|String|카테고리명|
        |phone|String|전화번호|
      
    * ##### 시나리오 및 설명
      > 1. 검색 키워드 공백 제거 (공백 여부 관계없이 검색 결과가 같은것을 확인했기 때문에 검색어 순위 집계를 위해 공백 제거)
      > 2. 키워드 검색 횟수 INSERT or UPDATE
      >> 1. 대용량 트래픽을 고려하여 Kafka로 keyword를 전달하여 발행 (topic : keyword-count)
      >> 2. Consumer가 발행된 메시지를 구독하여 검색어 카운트 증가
      > 3. 검색한 키워드를 key로 사용해 Redis에 검색 결과 데이터가 있는지 조회
      > 4. 있을 경우, Redis에서 조회해온 리스트 결과 리스트에 넣음
      > 5. 없을 경우,
      >> 1. WebClient를 통해 Kakao, Naver 장소 검색 API 호출하여 리스트 조회하고, 응답에 내려줄 데이터에 맞게 셋팅
      >> 2. 업체명의 HTML 태그, 공백을 제거하고 소문자로 바꾸어 동일 업체인지 체크
      >> 3. 동일업체, 카카오에만 존재, 네이버에만 존재하는 리스트를 각각 추출하고 결과 리스트에 순서에 맞게 추가하고 10개까지 잘라냄
      >> 4. 검색어를 key로 하여 최종 결과 리스트를 Redis에 적재 (TTL 3분으로 설정)
      > 6. 결과 리스트 반환

  * #### 2) 검색 키워드 목록
    * ##### URI 정보
        |메서드|URI|
        |-|-|
        |GET|/v1/keyword|
      
    * ##### 요청 - 쿼리 파라미터
        |이름|타입|설명|
        |-|-|-|
        |size|Integer|검색어 순위 개수 (default: 10개)|
        
    * ##### 응답
        |이름|타입|설명|
        |-|-|-|
        |keyword|String|검색어|
        |count|BIGINT|검색 횟수|

    * ##### 시나리오 및 설명
      > 1. Redis에 검색 순위 데이터가 있는지 조회
      > 2. 있을 경우, Redis에서 조회해 온 리스트 반환
      > 3. 없을 경우, DB에서 키워드 카운트 높은 순으로 요청한 size 만큼 조회하여 리스트 반환

* ###
## 개선할 점
  #### 1.Redis Set 사용 및 배치 추가
    * 대용량 트래픽 상황에서 장소 검색 API 요청이 증가할 경우, 검색어의 검색 횟수 증가하는 로직을 Kafka를 통해 전송하는 방식으로 전환.
    * 기대 효과 : DB 검색 요청 API

