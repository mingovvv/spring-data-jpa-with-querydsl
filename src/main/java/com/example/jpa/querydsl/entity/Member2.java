package com.example.jpa.querydsl.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "username", "age"})
public class Member2 {

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String username;

    private int age;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team2 team;

    public Member2(String username) {
        this(username, 0);
    }

    public Member2(String username, int age) {
        this(username, age, null);
    }

    public Member2(String username, int age, Team2 team) {
        this.username = username;
        this.age = age;
        if (team != null) {
            changeTeam(team);
        }
    }

    public void changeTeam(Team2 team) {
        this.team = team;
        team.getMembers().add(this);
    }

}
