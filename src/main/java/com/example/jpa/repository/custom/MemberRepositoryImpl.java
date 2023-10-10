package com.example.jpa.repository.custom;

import com.example.jpa.entity.Member;
import jakarta.persistence.EntityManager;

import java.util.List;

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
