package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.repartidor.data.model.entity.ClienteEntity

@Dao
interface ClienteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(clientes: List<ClienteEntity>)

    @Query("SELECT id FROM cliente")
    suspend fun getAllIds(): List<Int>

    @Query("DELETE FROM cliente WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Int>)

    @Query("SELECT * FROM cliente")
    suspend fun getAll(): List<ClienteEntity>

    @Query("SELECT * FROM cliente WHERE id = :id LIMIT 1")
    suspend fun obtenerClientePorId(id: Int): ClienteEntity?

    @Query("SELECT * FROM cliente WHERE id = :id LIMIT 1")
    suspend fun getClienteById(id: Int): ClienteEntity?

    // ── NUEVA CONSULTA ─────────────────────────────────────────────
    @Query(
        """
    SELECT DISTINCT cliente.*
    FROM cliente
    INNER JOIN cliente_dias_visita
        ON cliente_dias_visita.clienteId = cliente.id
    WHERE cliente.rutaId IN (
        SELECT id 
        FROM ruta 
        WHERE usuarioId = :userId
    )
    AND cliente_dias_visita.diaSemana = :diaSemana
    AND (
        CAST(cliente.id AS TEXT) = :query
        OR cliente.nombre LIKE '%' || :query || '%'
        OR cliente.nombreNegocio LIKE '%' || :query || '%'
    )
    ORDER BY cliente.nombre ASC
    """
    )
    suspend fun buscarPorIdNombreYDia(
        query: String,
        userId: Int,
        diaSemana: String
    ): List<ClienteEntity>

    @Query(
        """
    SELECT * FROM cliente 
    WHERE rutaId IN (
        SELECT id FROM ruta WHERE usuarioId = :userId
    )
    AND (
        CAST(cliente.id AS TEXT) = :query 
        OR cliente.nombre LIKE '%' || :query || '%'
    )
"""
    )
    suspend fun buscarPorIdONombre(
        query: String,
        userId: Int
    ): List<ClienteEntity>

    @Query("UPDATE cliente SET saldoAdeudo = :nuevoSaldo WHERE id = :clienteId")
    suspend fun actualizarSaldo(clienteId: Int, nuevoSaldo: Double)

    @Query("SELECT * FROM cliente WHERE id = :clienteId")
    suspend fun getClienteById2(clienteId: Int): ClienteEntity

    @Update
    suspend fun updateCliente(cliente: ClienteEntity)
}