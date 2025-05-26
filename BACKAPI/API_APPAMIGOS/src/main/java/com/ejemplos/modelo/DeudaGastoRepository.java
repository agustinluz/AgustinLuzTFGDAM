package com.ejemplos.modelo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DeudaGastoRepository extends JpaRepository<DeudaGasto, Long> {
    
    // Obtener deudas de un gasto específico
    List<DeudaGasto> findByGastoId(Long gastoId);
    
    // Obtener deudas de un usuario específico (lo que debe)
    List<DeudaGasto> findByDeudorId(Long deudorId);
    
    // Obtener créditos de un usuario específico (lo que le deben)
    List<DeudaGasto> findByAcreedorId(Long acreedorId);
    
    // Obtener deudas pendientes entre dos usuarios
    @Query("SELECT d FROM DeudaGasto d WHERE d.deudor.id = :deudorId AND d.acreedor.id = :acreedorId AND d.saldado = false")
    List<DeudaGasto> findDeudasPendientes(@Param("deudorId") Long deudorId, @Param("acreedorId") Long acreedorId);
    
    // Obtener una deuda específica entre dos usuarios para un gasto
    @Query("SELECT d FROM DeudaGasto d WHERE d.gasto.id = :gastoId AND d.deudor.id = :deudorId AND d.acreedor.id = :acreedorId")
    Optional<DeudaGasto> findByGastoAndDeudorAndAcreedor(
        @Param("gastoId") Long gastoId, 
        @Param("deudorId") Long deudorId, 
        @Param("acreedorId") Long acreedorId
    );
    
    // Obtener todas las deudas de un grupo
    @Query("SELECT d FROM DeudaGasto d WHERE d.gasto.grupo.id = :grupoId")
    List<DeudaGasto> findByGrupoId(@Param("grupoId") Long grupoId);
    
    // Obtener deudas pendientes de un grupo
    @Query("SELECT d FROM DeudaGasto d WHERE d.gasto.grupo.id = :grupoId AND d.saldado = false")
    List<DeudaGasto> findDeudasPendientesByGrupoId(@Param("grupoId") Long grupoId);
}