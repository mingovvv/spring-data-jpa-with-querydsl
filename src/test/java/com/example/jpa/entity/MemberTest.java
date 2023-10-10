package com.example.jpa.entity;

import com.example.jpa.dto.MemberDto;
import com.example.jpa.repository.MemberRepository;
import com.example.jpa.repository.pure.MemberJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(false)
class MemberTest {

    @PersistenceContext
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    void testEntity() {

        Team t1 = new Team("플랫폼");
        Team t2 = new Team("프론트엔드");
        Team t3 = new Team("영업");

        em.persist(t1);
        em.persist(t2);
        em.persist(t3);

        Member m1 = new Member("mingo", t1);
        Member m2 = new Member("devyu", t2);
        Member m3 = new Member("puregyu", t3);

        em.persist(m1);
        em.persist(m2);
        em.persist(m3);

        em.flush();
        em.clear();

        Member findMember = em.find(Member.class, m2.getId());
        findMember.changeTeam(t1);


    }

    @Test
    void test() {

        Team t1 = new Team("플랫폼");
        Team t2 = new Team("프론트엔드");
        Team t3 = new Team("영업");

        em.persist(t1);
        em.persist(t2);
        em.persist(t3);

        Member m1 = new Member("mingo", t1);
        Member m2 = new Member("devyu", t2);
        Member m3 = new Member("puregyu", t3);

        em.persist(m1);
        em.persist(m2);
        em.persist(m3);

        em.flush();
        em.clear();

        /**
         * select
         *         m1_0.member_id,
         *         m1_0.age,
         *         m1_0.team_id,
         *         m1_0.username,
         *         t1_0.team_id,
         *         t1_0.name
         *     from
         *         member m1_0
         *     join
         *         team t1_0
         *             on t1_0.team_id=m1_0.team_id
         */
        // select m from Member m join m.team t
//        List<Member> members = memberRepository.join();
//        members.forEach(s -> System.out.println(s.getTeam().getName()));

//        List<Member> members = em.createQuery("select m from Member m join m.team", Member.class).getResultList();
//        members.forEach(s -> System.out.println(s.getTeam().getId()));

        List<MemberDto> memberDto = memberRepository.findMemberDto();
        memberDto.forEach(m -> System.out.println("m = " + m));

    }

    @Test
    public void paging() throws Exception {
        //given
        memberJpaRepository.save(new Member("member1", 10));
        memberJpaRepository.save(new Member("member2", 10));
        memberJpaRepository.save(new Member("member3", 10));
        memberJpaRepository.save(new Member("member4", 10));
        memberJpaRepository.save(new Member("member5", 10));
        int age = 10;
        int offset = 0;
        int limit = 3;
        //when
        List<Member> members = memberJpaRepository.findByPage(age, offset, limit);
        long totalCount = memberJpaRepository.totalCount(age);
        //페이지 계산 공식 적용...
        // totalPage = totalCount / size ...
        // 마지막 페이지 ...
        // 최초 페이지 ..
        //then
        assertThat(members.size()).isEqualTo(3);
        assertThat(totalCount).isEqualTo(5);
    }

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
//        Page<Member> page = memberRepository.find1ByAge(10, pageRequest);

        //then
//        List<Member> content = page.getContent(); //조회된 데이터
//        assertThat(content.size()).isEqualTo(3); //조회된 데이터 수
//        assertThat(page.getTotalElements()).isEqualTo(5); //전체 데이터 수
//        assertThat(page.getNumber()).isEqualTo(0); //페이지 번호
//        assertThat(page.getTotalPages()).isEqualTo(2); //전체 페이지 번호
//        assertThat(page.isFirst()).isTrue(); //첫번째 항목인가?
//        assertThat(page.hasNext()).isTrue(); //다음 페이지가 있는가?
    }

    @Test
    public void bulkUpdate() throws Exception {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 21));
        memberRepository.save(new Member("member5", 40));
        //when
        int resultCount = memberRepository.bulkAgePlus(20);
        //then
        assertThat(resultCount).isEqualTo(3);
    }

}