package com.example.jpa.repository.custom;

import com.example.jpa.entity.Member;

import java.util.List;

public interface MemberCustomRepository {

    List<Member> findMemberCustrom();

}
