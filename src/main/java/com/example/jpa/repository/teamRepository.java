package com.example.jpa.repository;

import com.example.jpa.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface teamRepository extends JpaRepository<Team, Long> {
}
