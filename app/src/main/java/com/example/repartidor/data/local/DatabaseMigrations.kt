package com.example.repartidor.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS cancelacion_venta (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                uuid TEXT NOT NULL,
                ventaUuid TEXT NOT NULL,
                usuarioId INTEGER NOT NULL,
                miniBodegaId INTEGER NOT NULL,
                fecha TEXT NOT NULL,
                total REAL NOT NULL,
                motivo TEXT NOT NULL,
                sincronizado INTEGER NOT NULL,
                createdAt TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS cancelacion_venta_detalle (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                uuid TEXT NOT NULL,
                cancelacionVentaId INTEGER NOT NULL,
                cancelacionVentaUuid TEXT NOT NULL,
                productoVariacionId INTEGER NOT NULL,
                nombreProducto TEXT NOT NULL,
                cantidad REAL NOT NULL,
                precioUnitario REAL NOT NULL
            )
        """.trimIndent())
    }
}