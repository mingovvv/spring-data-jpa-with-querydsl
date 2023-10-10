# Spring Data JPA

---

#### 스프링 데이터 JPA
![Desktop View](/images/1.png)

```java
public interface ItemRepository extends JpaRepository<Item, Long> {

}
```
 - `JpaRepository<Entity, 식별자>`를 상속받는 인터페이스를 생성
 - 스프링 데이터 JPA는 애플리케이션 로딩 시점에 개발자가 생성한 인터페이스 안에다 대신 구현클래스를 injection
   - 실제로 ItemRepository를 `getClass()` 해보면 프록시 객체가 주입되어있음
   - 인터페이스는 `@Repository` 애노테이션 생략 가능

#### 주요 메서드
 - `save(S)` : 새로운 엔티티는 저장하고 이미 있는 엔티티는 병합
 - `delete(T)` : 엔티티 하나를 삭제. 내부에서 EntityManager.remove() 호출
 - `findById(ID)` : 엔티티 하나를 조회. 내부에서 EntityManager.find() 호출
 - `getOne(ID)` : 엔티티를 프록시로 조회. 내부에서 EntityManager.getReference() 호출 / 조회된 프록시 객체에 실제 접근할 때 쿼리가 나감
 - `findAll(…)` : 모든 엔티티를 조회한다. 정렬( Sort )이나 페이징( Pageable ) 조건을 파라미터로 제공할 수 있음

#### Query Method(쿼리 메서드)
1. 메서드 이름으로 쿼리 생성
  - `List<Member> findByUsernameAndAgeGreaterThan(String username, int age);`
  - https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.query-creation
  - 조회 : find…By ,read…By ,query…By get…By
    - https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods.query-creation
  - https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.limit-query-result

2. JPA Named Query
  ```java
  @NamedQuery(
        name = "Member.findByUsername",
        query = "select m from Member m where m.username = :username"
  )
  public class Member {
    ...
  }
  ```
  ```java
  // JPA를 직접 사용해서 네임드 쿼리 호출하는 방법
  public List<Member> findByUsername(String username) {
        ...
        List<Member> resultList = em.createNamedQuery("Member.findByUsername", Member.class)
        .setParameter("username", username)
        .getResultList();
  }
  
  // 스프링데이터 JPA를 사용해서 네임드 쿼리를 호출하는 방법
  public interface MemberRepository extends JpaRepository<Member, Long> {

      List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

      @Query(name = "Member.findByUsername")
      List<Member> findNamedQuery(@Param("username") String username);

  }
  ``` 
  - Entity 클래스에 적용
  - `@NamedQuery()`
    - name : 식별자(관례상 엔티티 + 메서드명으로 사용)
    - query : 대상 식별자가 불리워졌을 때, 수행될 쿼리
  - `스프링데이터 JPA를 사용해서 네임드 쿼리를 호출할 때`
    - @Param("username") 를 사용하는 이유는 네임드 쿼리를 통해 명시적으로 JPQL을 작성하면서 파라미터를 넘겼기 때문
    - @Query는 메서드 이름이 네임드 쿼리의 식별자와 동일한 경우 생략할 수 있음
      - 스프링데이터 JPA는 우선 1. 네임드쿼리 식별자를 찾고 2. 메서드 이름 쿼리 생성을 함
  - 애플리케이션 로딩 시점에 네임드 쿼리를 파싱해서 문제가 있을 경우 컴파일 에러발생(가장 큰 장점)

3. Repository 메서드에 쿼리 정의
```java
public interface MemberRepository extends JpaRepository<Member, Long> {

    /**
     * 메서드 이름 방식
     */
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    /**
     * 네임드 쿼리 방식
     */
    @Query(name = "Member.findByUsername")
    List<Member> findNamedQuery(@Param("username") String username);

    /**
     * Repository 메서드에 쿼리 정의
     */
    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

}
```
 - `@Query` 를 사용해서 repository 메서드에 직접 쿼리를 정의
 - 애플리케이션 로딩 시점에 네임드 쿼리를 파싱해서 문제가 있을 경우 컴파일 에러발생


#### 값, DTO 조회하는 방법
```java
/**
 * 값 타입 조회
 */
@Query("select m.username from Member m")
List<String> findMemberUserName();

/**
 * DTO 타입 조회
 */
@Query("select new com.example.jpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
List<MemberDto> findMemberDto();
```

#### 파라미터 바인딩
```java
/**
 * 컬렉션 파라미터 바인딩
 */
@Query("select m from Member m where m.username in :names")
List<Member> finByNames(@Param("names") List<String> names);
```

#### 반환타입
https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repository-query-return-types
```java
/**
 * 메서드 이름 방식 - List 반환타입
 *   - 조회가 안되었을 경우, 빈 컬레션 반환
 */
List<Member> findListByUsername(String username);

/**
 * 메서드 이름 방식 - Entity 반환타입
 *   - 조회가 안되었을 경우, Null(순수 JPA의 경우 exception 발생)
 */
Member findMemberByUsername(String username);

/**
 * 메서드 이름 방식 - Optional 반환타입
 */
Optional<Member> findOptionalByUsername(String username);
```

#### 페이징과 정렬
```java
/**
 * count 쿼리 사용
 */
Page<Member> find1ByAge(int age, Pageable pageable);
/**
 * count 쿼리 사용 안함
 */
Slice<Member> find2ByAge(int age, Pageable pageable);
/**
 * count 쿼리 사용 안함
 */
List<Member> find3ByAge(int age, Pageable pageable);
```

```java
@Test
public void pagingByDataJpa() throws Exception {

    //given
    memberRepository.save(new Member("member1", 10));
    memberRepository.save(new Member("member2", 10));
    memberRepository.save(new Member("member3", 10));
    memberRepository.save(new Member("member4", 10));
    memberRepository.save(new Member("member5", 10));

    //when
    PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));
    Page<Member> page = memberRepository.find1ByAge(10, pageRequest);

    //then
    List<Member> content = page.getContent(); //조회된 데이터
    Assertions.assertThat(content.size()).isEqualTo(3); //조회된 데이터 수
    Assertions.assertThat(page.getTotalElements()).isEqualTo(5); //전체 데이터 수
    Assertions.assertThat(page.getNumber()).isEqualTo(0); //페이지 번호
    Assertions.assertThat(page.getTotalPages()).isEqualTo(2); //전체 페이지 번호
    Assertions.assertThat(page.isFirst()).isTrue(); //첫번째 항목인가?
    Assertions.assertThat(page.hasNext()).isTrue(); //다음 페이지가 있는가?
}
```

```java
// 카운터 쿼리 분리 가능
@Query(value = "select m from Member m",
 countQuery = "select count(m.username) from Member m")
Page<Member> findMemberAllCountBy(Pageable pageable);
```

#### 벌크성 수정 쿼리
```java
/**
 * bulk update
 */
@Modifying
@Query("update Member m set m.age = m.age + 1 where m.age >= :age")
int bulkAgePlus(@Param("age") int age);
```
 - 벌크 연산은 영속성 컨텍스트를 무시하고 실행하기 때문에, 영속성 컨텍스트에 있는 엔티티의 상태와 DB에 엔티티 상태가 달라질 수 있음
 - 벌크성 쿼리를 실행하고 나서 영속성 컨텍스트 초기화: `@Modifying(clearAutomatically = true)`  (이 옵션의 기본값은 false )

#### EntityGraph
```java
// 지연로딩 여부 체크 방법

//Hibernate 기능으로 확인
Hibernate.isInitialized(member.getTeam());
        
//JPA 표준 방법으로 확인
PersistenceUnitUtil util =em.getEntityManagerFactory().getPersistenceUnitUtil();
util.isLoaded(member.getTeam());
```
```java
/**
 * fetch join
 *   - 연관된 테이블까지 싹 다 영속성 컨텍스트에 로딩시킴
 */
@Query("select m from Member m left join fetch m.team")
List<Member> findMemberFetchJoin();
```
```java
//공통 메서드 오버라이드
@Override
@EntityGraph(attributePaths = {"team"})
List<Member> findAll();

//JPQL + 엔티티 그래프
@EntityGraph(attributePaths = {"team"})
@Query("select m from Member m")
List<Member> findMemberEntityGraph();

//메서드 이름으로 쿼리에서 특히 편리하다.
@EntityGraph(attributePaths = {"team"})
List<Member> findByUsername(String username);
```

#### JPA Hint & Lock
```java
@QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
Member findReadOnlyByUsername(String username);
```
 - JPA 힌트 (DB 힌트와 다름)
 - JPA 표준은 아니고 hibernate 기술
 - 변경감지를 위한 스냅샷을 떠두는 것을 힌트를 줌으로써 막아줌

#### 사용자 정의 repository 구현
어디서 사용할까?
 - JPA 직접 사용( EntityManager )
 - 스프링 JDBC Template 사용
 - MyBatis 사용
 - 데이터베이스 커넥션 직접 사용 등등
 - Querydsl 사용

```java
// 1. 인터페이스 생성
public interface MemberCustomRepository {
    List<Member> findMemberCustrom();
}

// 2. 원하는 방식으로 인터페이스 구현
public class MemberRepositoryImpl implements MemberCustomRepository {

    private final EntityManager em;

    public MemberRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public List<Member> findMemberCustrom() {
        return  em.createQuery("select m from Member m")
                .getResultList();

    }

}

// 3. 생성한 인터페이스를 스프링데이터 JPA 인터페이스로 구현
public interface MemberRepository extends JpaRepository<Member, Long>, MemberCustomRepository {
    ...
}
```
 - 규칙 : `2`의 생성된 인터페이스 구현 클래스의 이름은 JPA 인터페이스 이름 + `Impl`로 마춰야 함
 - 스프링데이터 JPA가 자동으로 인식하고 스프링 빈 등록
 - 규칙을 변경하려면?
   - `@EnableJpaRepositories(basePackages = "study.datajpa.repository", repositoryImplementationPostfix = "Impl")`


#### Auditing
 - 등록일 / 수정일 / 등록자 / 수정자 관리

```java
// 순수 JPA
@MappedSuperclass
@Getter
public class JpaBaseEntity {
    
    @Column(updatable = false) // 변경막기
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    
    @PrePersist // persist 하기 전에 영속화
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdDate = now;
        updatedDate = now;
    }
    
    @PreUpdate // 업데이트 하기 전에 영속화
    public void preUpdate() {
        updatedDate = LocalDateTime.now();
    }
    
}
```

```java
// 스프링데이터 JPA
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@Getter
public class BaseEntity {
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdDate;
    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;
    @LastModifiedBy
    private String lastModifiedBy;
}
```
```java
@EnableJpaAuditing // auditing 기능 활성화
@SpringBootApplication
public class SpringDataJpaWithQuerydslApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringDataJpaWithQuerydslApplication.class, args);
    }

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of(UUID.randomUUID().toString()); // session 값 등을 꺼내 넣으면 됨
    }
}
```
 - `@EnableJpaAuditing` : base package 위치
 - `@EntityListeners(AuditingEntityListener.class)` : auditing class

#### Web - 도메인 클래스 컨버터
```java
@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberRepository memberRepository;
    
    // 도메인 컨버터 적용 X
    @GetMapping("/members/{id}")
    public String findMember(@PathVariable("id") Long id) {
        Member member = memberRepository.findById(id).get();
        return member.getUsername();
    }

    // 도메인 컨버터 적용 O
    @GetMapping("/members/{id}")
    public String findMember(@PathVariable("id") Member member) {
        return member.getUsername();
    }
    
}
```
 - HTTP 요청은 회원 id 를 받지만 도메인 클래스 컨버터가 중간에 동작해서 회원 엔티티 객체를 반환
 - 도메인 클래스 컨버터도 리파지토리를 사용해서 엔티티를 찾음
 - 도메인 클래스 컨버터로 엔티티를 파라미터로 받으면, 이 엔티티는 단순 조회용으로만 사용해야 한다. (트랜잭션이 없는 범위에서 엔티티를 조회했으므로, 엔티티를 변경해도 DB에 반영되지 않는다.)
