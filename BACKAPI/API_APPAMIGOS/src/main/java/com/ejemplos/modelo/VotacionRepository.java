package com.ejemplos.modelo;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VotacionRepository extends JpaRepository<Nota, Long> {

}
