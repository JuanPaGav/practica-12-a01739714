package mx.tec.charla.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

/** Operaciones HTTP de una sala: obtener invitación, solicitar acceso y consultar estado. */
interface SalaApi {
    @GET
    suspend fun invitacion(@Url url: String): Invitacion

    @POST
    suspend fun solicitar(@Url url: String, @Body cuerpo: NuevaSolicitud): EstadoSolicitud

    @GET
    suspend fun consultar(@Url url: String): EstadoSolicitud
}

@Serializable
data class Invitacion(val url: String)

@Serializable
data class NuevaSolicitud(val nickname: String)

/** `token` solo llega cuando el anfitrión aprueba la solicitud. */
@Serializable
data class EstadoSolicitud(val id: String, val nickname: String, val estado: String, val token: String? = null)