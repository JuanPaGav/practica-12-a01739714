package mx.tec.charla.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import mx.tec.charla.data.remote.SalaApi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object RedModule {

    /**
     * Un solo cliente para el WebSocket y Retrofit.
     *
     * `pingInterval`: cada 20 s OkHttp manda un ping por el WebSocket. Si no
     * vuelve el pong, da la conexión por muerta y avisa con `onFailure`.
     */
    @Provides
    @Singleton
    fun cliente(): OkHttpClient = OkHttpClient.Builder()
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun salaApi(cliente: OkHttpClient): SalaApi = Retrofit.Builder()
        // Obligatoria para Retrofit; las llamadas usan su URL completa mediante @Url.
        .baseUrl("http://localhost/")
        .client(cliente)
        .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(SalaApi::class.java)
}