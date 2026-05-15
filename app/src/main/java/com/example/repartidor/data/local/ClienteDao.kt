package com.example.repartidor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.repartidor.data.model.ClienteEntity

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
    @Query("SELECT * FROM cliente WHERE CAST(id AS TEXT) = :query OR nombre LIKE '%' || :query || '%'")
    suspend fun buscarPorIdONombre(query: String): List<ClienteEntity>

    @Query("UPDATE cliente SET saldoAdeudo = :nuevoSaldo WHERE id = :clienteId")
    suspend fun actualizarSaldo(clienteId: Int, nuevoSaldo: Double)
}