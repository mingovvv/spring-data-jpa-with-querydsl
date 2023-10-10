# Querydsl

---

#### Querydsl
 - Querydsl은 JPQL의 빌더 역할
 - Querydsl로 작성한 코드는 JPQL이 됨

#### settings
```java
// gradle.build (springboot 3.x 이상)
implementation 'com.querydsl:querydsl-jpa:5.0.0:jakarta'
annotationProcessor 'com.querydsl:querydsl-apt:5.0.0:jakarta'
annotationProcessor "jakarta.annotation:jakarta.annotation-api"
annotationProcessor "jakarta.persistence:jakarta.persistence-api"
```

#### Q타입 생성
![Desktop View](/images/2.png)
 - `gradle` → `task` → `other` → `compileJava` 시, `Q + Entity`명의 Q타입 class 생성됨

#### 기본 Q-Type 활용

```java
//별칭 직접 지정
QMember qMember = new QMember("m");
//기본 인스턴스 사용
QMember qMember = QMember.member;
```

#### 실행 테스트
 - `EntityManager`로 `JPAQueryFactory` 생성
 - 메서드 체이닝으로 작성되기 때문에 쿼리 실수가 있었을 때, 컴파일 시점에 에러로 잡을 수 있음
    ```java
    @Test
    void startQuerydsl() {
        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
        QMember2 m = QMember2.member2;

        Member2 findMember = jpaQueryFactory
                    .select(m)
                    .from(m)
                    .where(m.username.eq("member1"))
                    .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }
    ```

#### 기초문법   
```java
// where절의 and는 생략하고 ,로 대체할 수 있음
where(member.username.eq("member1").and(member.age.eq(10)))
where(member.username.eq("member1"), member.age.eq(10)) // 이 방식의 경우 null은 무시한다.
```

#### 결과조회
```java
//List
List<Member> fetch = queryFactory
     .selectFrom(member)
     .fetch();

//단 건
Member findMember1 = queryFactory
     .selectFrom(member)
     .fetchOne();

//처음 한 건 조회
Member findMember2 = queryFactory
     .selectFrom(member)
     .fetchFirst();

//페이징에서 사용
QueryResults<Member> results = queryFactory
     .selectFrom(member)
     .fetchResults();

//count 쿼리로 변경
long count = queryFactory
     .selectFrom(member)
     .fetchCount();
```
 - `fetch()` : 리스트 조회, 데이터 없으면 빈 리스트 반환
 - `fetchOne()` : 단 건 조회
    - 결과가 없으면 : null
    - 결과가 둘 이상이면 : com.querydsl.core.NonUniqueResultException
 - `fetchFirst()` : limit(1).fetchOne()
 - `fetchResults()` : 페이징 정보 포함, total count 쿼리 추가 실행
 - `fetchCount()` : count 쿼리로 변경해서 count 수 조회

#### 정렬
```java
/**
 * 회원 정렬 순서
 * 1. 회원 나이 내림차순(desc)
 * 2. 회원 이름 올림차순(asc)
 * 단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
 */
@Test
public void sort(){
    
    em.persist(new Member2(null,100));
    em.persist(new Member2("member5",100));
    em.persist(new Member2("member6",100));
    
    List<Member2> result=jpaQueryFactory
        .selectFrom(member2)
        .where(member2.age.eq(100))
        .orderBy(member2.age.desc(),member2.username.asc().nullsLast())
        .fetch();
    
    Member2 member5=result.get(0);
    Member2 member6=result.get(1);
    Member2 memberNull=result.get(2);
    
    assertThat(member5.getUsername()).isEqualTo("member5");
    assertThat(member6.getUsername()).isEqualTo("member6");
    assertThat(memberNull.getUsername()).isNull();
    
}
```

#### 페이징
```java
@Test
public void paging1() {
    List<Member2> result = jpaQueryFactory
            .selectFrom(member2)
            .orderBy(member2.username.desc())
            .offset(1) //0부터 시작(zero index)
            .limit(2) //최대 2건 조회
            .fetch();
    assertThat(result.size()).isEqualTo(2);
}

@Test
public void paging2() {
    QueryResults<Member2> queryResults = jpaQueryFactory
            .selectFrom(member2)
            .orderBy(member2.username.desc())
            .offset(1)
            .limit(2)
            .fetchResults();
    assertThat(queryResults.getTotal()).isEqualTo(4);
    assertThat(queryResults.getLimit()).isEqualTo(2);
    assertThat(queryResults.getOffset()).isEqualTo(1);
    assertThat(queryResults.getResults().size()).isEqualTo(2);
}
```

#### 집합
```java
/**
 * JPQL
 * select
 * COUNT(m), //회원수
 * SUM(m.age), //나이 합
 * AVG(m.age), //평균 나이
 * MAX(m.age), //최대 나이
 * MIN(m.age) //최소 나이
 * from Member m
 */
@Test
public void aggregation() throws Exception {
    List<Tuple> result = jpaQueryFactory
            .select(member2.count(),
                    member2.age.sum(),
                    member2.age.avg(),
                    member2.age.max(),
                    member2.age.min())
            .from(member2)
            .fetch();
    Tuple tuple = result.get(0);
    assertThat(tuple.get(member2.count())).isEqualTo(4);
    assertThat(tuple.get(member2.age.sum())).isEqualTo(100);
    assertThat(tuple.get(member2.age.avg())).isEqualTo(25);
    assertThat(tuple.get(member2.age.max())).isEqualTo(40);
    assertThat(tuple.get(member2.age.min())).isEqualTo(10);
}

/**
 * 팀의 이름과 각 팀의 평균 연령을 구해라.
 */
@Test
public void group() throws Exception {
    
    List<Tuple> result = jpaQueryFactory
        .select(team2.name, member2.age.avg())
        .from(member2)
        .join(member2.team, team2)
        .groupBy(team2.name)
        .fetch();

    Tuple teamA = result.get(0);
    Tuple teamB = result.get(1);
    
    assertThat(teamA.get(team2.name)).isEqualTo("teamA");
    assertThat(teamA.get(member2.age.avg())).isEqualTo(15);
    assertThat(teamB.get(team2.name)).isEqualTo("teamB");
    assertThat(teamB.get(member2.age.avg())).isEqualTo(35);
}
```

#### 조인
 - 조인의 기본 문법은 첫 번째 파라미터에 조인 대상을 지정하고, 두 번째 파라미터에 별칭(alias)으로 사용할 Q 타입을 지정

