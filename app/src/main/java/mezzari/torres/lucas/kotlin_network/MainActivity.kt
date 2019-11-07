package mezzari.torres.lucas.kotlin_network

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.activity_main.*
import mezzari.torres.lucas.network.annotation.Route
import mezzari.torres.lucas.network.source.Network
import mezzari.torres.lucas.network.source.module.client.LogModule
import mezzari.torres.lucas.network.source.module.retrofit.GsonConverterModule
import mezzari.torres.lucas.network.source.promise.BaseNetworkPromise
import mezzari.torres.lucas.network.source.promise.NetworkPromise
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import java.net.UnknownHostException
import java.util.*

class MainActivity : AppCompatActivity() {

    private val service: ViacepService  = ViacepService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Network.initialize(
            retrofitLevelModules = Collections.singletonList(GsonConverterModule()),
            okHttpClientLevelModule = Collections.singletonList(LogModule()),
            responseInterceptors = listOf(ConnectionFailed())
        )

        btnSend.setOnClickListener {
            val cep = etCep.text.toString()
            if (cep.isNotEmpty()) {
                service.getAddress(cep).then { address ->
                    address ?: return@then
                    Toast.makeText(this@MainActivity, address.street, Toast.LENGTH_LONG).show()
                }.catch { error ->
                    error ?: return@catch
                    Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    @Route("https://viacep.com.br/ws/")
    interface IViacepAPI {
        @GET("{cep}/json")
        fun getAddress(
            @Path("cep") cep: String
        ): Call<Address>
    }

    class ViacepService {
        private val api: IViacepAPI by lazy {
            Network.build<IViacepAPI>()
        }

        fun getAddress(cep: String): NetworkPromise<Address> {
            return NetworkPromise {
                api.getAddress(cep).enqueue(this)
            }
        }
    }

    class Address (
        @SerializedName("cep")
        val cep: String,
        @SerializedName("logradouro")
        val street: String,
        @SerializedName("complemento")
        val complement: String,
        @SerializedName("bairro")
        val neighborhood: String,
        @SerializedName("localidade")
        val locality: String,
        @SerializedName("uf")
        val state: String,
        @SerializedName("unidade")
        val unity: String,
        @SerializedName("ibge")
        val ibge: String,
        @SerializedName("gia")
        val gia: String
    )

    class ConnectionFailed: Network.ResponseInterceptor {
        override fun <T> onFailure(
            call: Call<T>,
            t: Throwable,
            promise: BaseNetworkPromise<T>
        ): Boolean {
            if (t is UnknownHostException) {
                promise.failureCallback?.invoke(promise, "You are offline")
                return true
            }
            return false
        }

        override fun <T> onResponse(
            call: Call<T>,
            response: Response<T>,
            promise: BaseNetworkPromise<T>
        ): Boolean {
            return false
        }
    }
}
