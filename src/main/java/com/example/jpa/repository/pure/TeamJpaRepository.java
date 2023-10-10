package com.example.jpa.repository.pure;

import com.example.jpa.entity.Team;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TeamJpaRepository {

    @PersistenceContext
    private EntityManager em;

    public Team save(Team member) {
        em.persist(member);
        return member;
    }

    public Team find(Long id) {
        return em.find(Team.class, id);
    }

    public void delete(Team member) {
        em.remove(member);
    }

    public List<Team> findAll() {
        // JPQL
        return em.createQuery("select t from Team t", Team.class).getResultList();
    }

    public Optional<Team> findById(Long id) {
        return Optional.ofNullable(em.find(Team.class, id));
    }

    public long count() {
        return em.createQuery("select count(t) from Team t", Long.class).getSingleResult();
    }


}
