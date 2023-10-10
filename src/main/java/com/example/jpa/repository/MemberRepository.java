package com.example.jpa.repository;

import com.example.jpa.dto.MemberDto;
import com.example.jpa.entity.Member;
import com.example.jpa.repository.custom.MemberCustomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberCustomRepository {

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
     * 메서드 직접 쿼리 방식
     */
    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

    /**
     * 값 타입 조회
     */
    @Query("select m.username from Member m")
    List<String> findMember();

    /**
     * DTO 타입 조회
     */
    @Query("select new com.example.jpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();

    /**
     * 컬렉션 파라미터 바인딩
     */
    @Query("select m from Member m where m.username in :names")
    List<Member> finByNames(@Param("names") List<String> names);

    /**
     * 메서드 이름 방식 - List 반환타입
     */
    List<Member> findListByUsername(String username);

    /**
     * 메서드 이름 방식 - Entity 반환타입
     */
    Member findMemberByUsername(String username);

    /**
     * 메서드 이름 방식 - Optional 반환타입
     */
    Optional<Member> findOptionalByUsername(String username);

    /**
     * count 쿼리 사용
     */
//    Page<Member> find1ByAge(int age, Pageable pageable);
    /**
     * count 쿼리 사용 안함
     */
//    Slice<Member> find2ByAge(int age, Pageable pageable);
    /**
     * count 쿼리 사용 안함
     */
//    List<Member> find3ByAge(int age, Pageable pageable);
    /**
     * count 쿼리 사용
     */
//    List<Member> find4ByUsername(String name, Sort sort);

    /**
     * bulk update
     */
    @Modifying
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);

    /**
     * fetch join
     *   - 연관된 테이블까지 싹 다 영속성 컨텍스트에 로딩시킴
     */
    @Query("select m from Member m left join fetch m.team")
    List<Member> findMemberFetchJoin();

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





    /**
     * test1
     */
    @Query("select m from Member m join m.team t")
    List<Member> join();

    /**
     * test2
     */
    @Query("select m, t from Member m join fetch m.team t")
    List<Member> fetchJoin();
}
