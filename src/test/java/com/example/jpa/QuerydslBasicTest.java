package com.example.jpa;

import com.example.jpa.querydsl.entity.Member2;
import com.example.jpa.querydsl.entity.QMember2;
import com.example.jpa.querydsl.entity.QTeam2;
import com.example.jpa.querydsl.entity.Team2;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.jpa.entity.QMember.member;
import static com.example.jpa.entity.QTeam.team;
import static com.example.jpa.querydsl.entity.QMember2.member2;
import static com.example.jpa.querydsl.entity.QTeam2.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);


    @BeforeEach
    public void before() {
        Team2 teamA = new Team2("teamA");
        Team2 teamB = new Team2("teamB");
        em.persist(teamA);
        em.persist(teamB);
        Member2 member1 = new Member2("member1", 10, teamA);
        Member2 member2 = new Member2("member2", 20, teamA);
        Member2 member3 = new Member2("member3", 30, teamB);
        Member2 member4 = new Member2("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    void startJPQL() {
        //member1을 찾아라.
        String qlString = "select m from Member2 m where m.username = :username";

        Member2 findMember = em.createQuery(qlString, Member2.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    void startQuerydsl() {
        QMember2 m = member2;

        Member2 findMember = jpaQueryFactory
                    .select(m)
                    .from(m)
                    .where(m.username.eq("member1"))
                    .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void search() {
        Member2 findMember = jpaQueryFactory
                .selectFrom(member2)
                .where(member2.username.eq("member1").and(member2.age.eq(10)))
                .fetchOne();
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 올림차순(asc)
     * 단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
     */
    @Test
    public void sort() {
        em.persist(new Member2(null, 100));
        em.persist(new Member2("member5", 100));
        em.persist(new Member2("member6", 100));
        List<Member2> result = jpaQueryFactory
                .selectFrom(member2)
                .where(member2.age.eq(100))
                .orderBy(member2.age.desc(), member2.username.asc().nullsLast())
                .fetch();
        Member2 member5 = result.get(0);
        Member2 member6 = result.get(1);
        Member2 memberNull = result.get(2);
        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }

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

    /**
     * 팀 A에 소속된 모든 회원
     */
    @Test
    public void join() throws Exception {
        QMember2 member = QMember2.member2;
        QTeam2 team = QTeam2.team2;

        List<Member2> result = jpaQueryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");
    }
}
