package mx.tec.charla.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mx.tec.charla.data.local.SalaStore
import mx.tec.charla.data.qr.GeneradorQr
import mx.tec.charla.data.remote.ChatConexion
import mx.tec.charla.data.remote.EventoServidor
import mx.tec.charla.data.remote.Orden
import mx.tec.charla.data.remote.SalaApi
import mx.tec.charla.domain.Solicitud
import retrofit2.HttpException

@HiltViewModel
class SalaViewModel @Inject constructor(
    private val store: SalaStore,
    private val conexion: ChatConexion,
    private val api: SalaApi
) : ViewModel() {

    private val _ui = MutableStateFlow(SalaUiState())
    val ui: StateFlow<SalaUiState> = _ui.asStateFlow()

    private var visible = false

    init {
        viewModelScope.launch {
            // Cambiar de destino vacía la vista; la bienvenida traerá el historial de esa sala.
            store.destino.distinctUntilChanged().collect { destino ->
                _ui.update { SalaUiState(destino = destino, conexion = it.conexion) }
                if (visible) conexion.conectar(destino.servidor, destino.token)
            }
        }
        viewModelScope.launch {
            conexion.estado.collect { estado -> _ui.update { it.copy(conexion = estado) } }
        }
        viewModelScope.launch {
            conexion.eventos.collect(::recibir)
        }
    }

    fun alAparecer() {
        visible = true
        _ui.value.destino?.let { conexion.conectar(it.servidor, it.token) }
    }

    fun alDesaparecer() {
        visible = false
        conexion.desconectar()
    }

    private fun recibir(evento: EventoServidor) {
        when (evento) {
            is EventoServidor.Bienvenida -> _ui.update {
                it.copy(mensajes = evento.mensajes, solicitudes = evento.solicitudes)
            }
            is EventoServidor.Nuevo -> _ui.update { it.copy(mensajes = it.mensajes + evento.aMensaje()) }
            is EventoServidor.NuevaSolicitud -> _ui.update { it.copy(solicitudes = it.solicitudes + evento.aSolicitud()) }
        }
    }

    fun enviar(texto: String): Boolean = texto.isNotBlank() && conexion.enviar(Orden.Enviar(texto.trim()))

    fun aprobar(solicitud: Solicitud) = responder(solicitud, Orden.Aprobar(solicitud.id))

    fun rechazar(solicitud: Solicitud) = responder(solicitud, Orden.Rechazar(solicitud.id))

    private fun responder(solicitud: Solicitud, orden: Orden) {
        if (conexion.enviar(orden)) _ui.update { it.copy(solicitudes = it.solicitudes - solicitud) }
    }

    /** Pregunta a tu servidor por el enlace público y lo convierte en QR. */
    fun invitar() {
        _ui.update { it.copy(invitacion = InvitacionUi.Cargando) }
        viewModelScope.launch {
            val invitacion = try {
                val url = api.invitacion("${SalaStore.propio.servidor}/invitacion").url
                InvitacionUi.Lista(url, withContext(Dispatchers.Default) { GeneradorQr.bitmap(url) })
            } catch (e: IOException) {
                InvitacionUi.Error(mensajeDe(e))
            } catch (e: HttpException) {
                InvitacionUi.Error(mensajeDe(e))
            }
            _ui.update { it.copy(invitacion = invitacion) }
        }
    }

    fun cerrarInvitacion() = _ui.update { it.copy(invitacion = InvitacionUi.Cerrada) }

    fun reiniciar() {
        viewModelScope.launch { store.reiniciar() }
    }

    override fun onCleared() = conexion.desconectar()
}