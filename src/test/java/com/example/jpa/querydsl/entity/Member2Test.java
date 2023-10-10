package com.example.jpa.querydsl.entity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
//@Commit
class Member2Test {

    @PersistenceContext
    EntityManager em;

    @Test
    public void testEntity() {
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

        //초기화
        em.flush();
        em.clear();

        //확인
        List<Member2> members = em.createQuery("select m from Member2 m", Member2.class)
                .getResultList();

        for (Member2 member : members) {
            System.out.println("member=" + member);
            System.out.println("-> member.team=" + member.getTeam());
        }

    }

}