package mx.tec.charla.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mx.tec.charla.BuildConfig
import mx.tec.charla.domain.ANFITRION
import mx.tec.charla.domain.Destino

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sala")

/** Sala activa: por omisión, la propia; si se acepta una invitación, la sala ajena. */
@Singleton
class SalaStore @Inject constructor(@ApplicationContext private val context: Context) {

    private val servidorKey = stringPreferencesKey("servidor")
    private val tokenKey = stringPreferencesKey("token")
    private val yoKey = stringPreferencesKey("yo")

    val destino: Flow<Destino> = context.dataStore.data.map { prefs ->
        val servidor = prefs[servidorKey]
        val token = prefs[tokenKey]
        val yo = prefs[yoKey]
        if (servidor != null && token != null && yo != null) {
            Destino(servidor, token, yo, esPropio = false)
        } else {
            propio
        }
    }

    suspend fun entrar(servidor: String, token: String, yo: String) {
        context.dataStore.edit {
            it[servidorKey] = servidor
            it[tokenKey] = token
            it[yoKey] = yo
        }
    }

    /** Olvida la sala ajena y vuelve a la propia. */
    suspend fun reiniciar() {
        context.dataStore.edit { it.clear() }
    }

    companion object {
        val propio = Destino(BuildConfig.SERVIDOR_PROPIO, BuildConfig.CLAVE_ANFITRION, ANFITRION, esPropio = true)
    }
}