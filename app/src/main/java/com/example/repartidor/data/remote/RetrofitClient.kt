package com.example.repartidor.data.remote

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8000/api/"
        //"http://192.168.68.63:8000/api/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: Api = retrofit.create(Api::class.java)

    interface Api {

        @GET("roles")
        suspend fun getRoles(
            @Query("updated_after") updatedAfter: String? = null
        ): Response<List<RolDto>>

        @GET("usuarios")
        suspend fun getUsuarios(
            @Query("updated_after") updatedAfter: String? = null
        ): Response<List<UsuarioDto>>

        @GET("vehiculos")
        suspend fun getVehiculos(
            @Query("updated_after") updatedAfter: String? = null
        ): Response<List<VehiculoDto>>

        @GET("rutas")
        suspend fun getRutas(
            @Query("updated_after") updatedAfter: String? = null
        ): Response<List<RutaDto>>

        @GET("clientes")
        suspend fun getClientes(
            @Query("updated_after") updatedAfter: String? = null
        ): Response<List<ClienteDto>>

        @GET("cliente-dias")
        suspend fun getClienteDiasVisita(
            @Query("updated_after") updatedAfter: String? = null
        ): Response<List<ClienteDiasVisitaDto>>

        @GET("categorias")
        suspend fun getCategorias(
            @Query("updated_after") updatedAfter: String? = null
        ): Response<List<CategoriaProductoDTO>>

        @GET("presentaciones")
        suspend fun getPresentaciones(
            @Query("updated_after") updatedAfter: String? = null
        ): Response<List<PresentacionProductoTerminadoDTO>>

        @GET("productos")
        suspend fun getProductos(
            @Query("updated_after") updatedAfter: String? = null
        ): Response<List<ProductoTerminadoDTO>>

        @GET("variaciones")
        suspend fun getVariaciones(
            @Query("updated_after") updatedAfter: String? = null
        ): Response<List<ProductoVariacionDTO>>

        @GET("mini-bodegas")
        suspend fun getMiniBodegas(
            @Query("updated_after") updatedAfter: String? = null
        ): Response<List<MiniBodegaDto>>

        @GET("mini-bodega-detalles")
        suspend fun getMiniBodegaDetalles(
            @Query("updated_after") updatedAfter: String? = null
        ): Response<List<MiniBodegaDetalleDto>>

        @GET("pedidos-reabastecimiento")
        suspend fun getPedidosReabastecimiento(
            @Query("updated_after") updatedAfter: String? = null
        ): Response<List<PedidoReabastecimientoDto>>

        @GET("pedido-reabastecimiento-detalles")
        suspend fun getPedidosReabastecimientoDetalle(
            @Query("updated_after") updatedAfter: String? = null
        ): Response<List<PedidoReabastecimientoDetalleDto>>

        @POST("reabastecimiento/crear/")
        suspend fun crearReabastecimiento(
            @Body request: PedidoReabastecimientoRequest
        ): Response<Unit>

        @POST("mini-bodega/cerrar/")
        suspend fun cerrarMiniBodega(
            @Body request: CerrarMiniBodegaRequest
        ): Response<Unit>

    }


}