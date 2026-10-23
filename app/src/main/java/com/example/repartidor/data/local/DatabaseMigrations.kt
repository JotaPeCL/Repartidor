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

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS devolucion_sustitucion (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                uuid TEXT NOT NULL,
                devolucionId INTEGER NOT NULL,
                devolucionUuid TEXT NOT NULL,
                devolucionDetalleId INTEGER NOT NULL,
                productoVariacionId INTEGER NOT NULL,
                nombreProducto TEXT NOT NULL,
                cantidad REAL NOT NULL,
                precioUnitario REAL NOT NULL,
                createdAt TEXT NOT NULL,
                sincronizado INTEGER NOT NULL
            )
        """.trimIndent())
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS cancelacion_devolucion (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                uuid TEXT NOT NULL,
                devolucionUuid TEXT NOT NULL,
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
            CREATE TABLE IF NOT EXISTS cancelacion_devolucion_detalle (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                uuid TEXT NOT NULL,
                cancelacionDevolucionId INTEGER NOT NULL,
                cancelacionDevolucionUuid TEXT NOT NULL,
                productoVariacionId INTEGER NOT NULL,
                nombreProducto TEXT NOT NULL,
                cantidad REAL NOT NULL,
                precioUnitario REAL NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS cancelacion_devolucion_sustitucion (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                uuid TEXT NOT NULL,
                cancelacionDevolucionId INTEGER NOT NULL,
                cancelacionDevolucionUuid TEXT NOT NULL,
                productoVariacionId INTEGER NOT NULL,
                nombreProducto TEXT NOT NULL,
                cantidad REAL NOT NULL,
                precioUnitario REAL NOT NULL
            )
        """.trimIndent())
    }
}

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS rotaciones (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                uuid TEXT NOT NULL,
                clienteId INTEGER,
                usuarioId INTEGER NOT NULL,
                miniBodegaId INTEGER NOT NULL,
                fecha TEXT NOT NULL,
                descripcion TEXT NOT NULL,
                sincronizado INTEGER NOT NULL,
                createdAt TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS rotacion_detalle (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                uuid TEXT NOT NULL,
                rotacionId INTEGER NOT NULL,
                rotacionUuid TEXT NOT NULL,
                productoVariacionId INTEGER NOT NULL,
                nombreProducto TEXT NOT NULL,
                cantidad REAL NOT NULL,
                precioUnitario REAL NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS rotacion_sustitucion (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                uuid TEXT NOT NULL,
                rotacionId INTEGER NOT NULL,
                rotacionUuid TEXT NOT NULL,
                rotacionDetalleId INTEGER NOT NULL,
                productoVariacionId INTEGER NOT NULL,
                nombreProducto TEXT NOT NULL,
                cantidad REAL NOT NULL,
                precioUnitario REAL NOT NULL,
                createdAt TEXT NOT NULL,
                sincronizado INTEGER NOT NULL
            )
        """.trimIndent())
    }
}
val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL("""
            ALTER TABLE rotaciones
            ADD COLUMN diferenciaDinero REAL NOT NULL DEFAULT 0.0
        """.trimIndent())

        db.execSQL("""
            ALTER TABLE rotaciones
            ADD COLUMN tipoDiferencia TEXT NOT NULL DEFAULT 'SIN_DIFERENCIA'
        """.trimIndent())

        db.execSQL("""
            ALTER TABLE rotaciones
            ADD COLUMN formaLiquidacion TEXT NOT NULL DEFAULT 'EFECTIVO'
        """.trimIndent())
    }
}
val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS cancelacion_rotacion (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                uuid TEXT NOT NULL,
                rotacionUuid TEXT NOT NULL,
                usuarioId INTEGER NOT NULL,
                miniBodegaId INTEGER NOT NULL,
                fecha TEXT NOT NULL,
                descripcion TEXT NOT NULL,
                diferenciaDinero REAL NOT NULL,
                tipoDiferencia TEXT NOT NULL,
                formaLiquidacion TEXT NOT NULL,
                motivo TEXT NOT NULL,
                sincronizado INTEGER NOT NULL,
                createdAt TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS cancelacion_rotacion_detalle (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                uuid TEXT NOT NULL,
                cancelacionRotacionId INTEGER NOT NULL,
                cancelacionRotacionUuid TEXT NOT NULL,
                productoVariacionId INTEGER NOT NULL,
                nombreProducto TEXT NOT NULL,
                cantidad REAL NOT NULL,
                precioUnitario REAL NOT NULL,
                tipoMovimiento TEXT NOT NULL
            )
        """.trimIndent())
    }
}