package mezzari.torres.lucas.network

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import mezzari.torres.lucas.network.source.Network
import mezzari.torres.lucas.network.source.module.client.CookiesModule
import mezzari.torres.lucas.network.source.module.client.LogModule
import mezzari.torres.lucas.network.source.module.retrofit.GsonConverterModule
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.net.HttpURLConnection
import java.util.concurrent.CountDownLatch

/**
 * @author Lucas T. Mezzari
 * @since 27/02/2020
 */
class RetrofitCallbackTest {

    //<editor-fold desc="Properties">
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private lateinit var mockedServer: MockWebServer

    private var response: MockResponse = MockResponse().apply {
        setResponseCode(404)
    }

    private val dispatcher = object: Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            return response
        }
    }

    private lateinit var service: CallbackService
    //</editor-fold>

    //<editor-fold desc="Test Setup">
    @Before
    fun setup() {
        mockedServer = MockWebServer()

        mockedServer.dispatcher = dispatcher

        mockedServer.start()

        Network.initialize(
            retrofitLevelModules = arrayListOf(GsonConverterModule()),
            okHttpClientLevelModule = arrayListOf(CookiesModule(), LogModule()),
            baseUrl = mockedServer.url("/").toString()
        )

        service = CallbackService()
    }

    @After
    fun tearDown() {
        mockedServer.shutdown()
    }
    //</editor-fold>

    //<editor-fold desc="Test GET Names">
    @Test
    fun `test valid get names`() {
        val signal = CountDownLatch(1)
        var result: Response<List<String>>? = null

        response = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody("[\"Clarrisa\", \"Natália\", \"João\"]")

        service.getNames(object: Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                signal.countDown()
                result = response
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                signal.countDown()
                result = null
            }
        })
        signal.await()

        val isResponseValid = (result?.isSuccessful ?: false)
                && result?.body() != null
                && result!!.body()!!.isNotEmpty()

        assertThat(isResponseValid, equalTo(true))
    }

    @Test
    fun `test invalid get names`() {
        val signal = CountDownLatch(1)
        var result: Response<List<String>>? = null

        response = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)

        service.getNames(object: Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                signal.countDown()
                result = response
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                signal.countDown()
                result = null
            }
        })
        signal.await()

        val isResponseValid = (result?.isSuccessful ?: false)
                && result?.body() != null

        assertThat(isResponseValid, equalTo(false))
    }

    @Test
    fun `test failed get names`() {
        val signal = CountDownLatch(1)
        var result: Response<List<String>>? = null

        response = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody("{}")

        service.getNames(object: Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                signal.countDown()
                result = response
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                signal.countDown()
                result = null
            }
        })
        signal.await()

        val isResponseValid = (result?.isSuccessful ?: false)
                && result?.body() != null

        assertThat(isResponseValid, equalTo(false))
    }
    //</editor-fold>

    //<editor-fold desc="Test POST Name">
    @Test
    fun `test valid post name`() {
        val signal = CountDownLatch(1)
        var result: Response<Void>? = null

        response = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody("{\"success\": true}")

        service.postName(NameWrapper("Lucas"), object : Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                signal.countDown()
                result = null
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                signal.countDown()
                result = response
            }
        })
        signal.await()

        assertThat(result?.isSuccessful, equalTo(true))
    }

    @Test
    fun `test invalid post name`() {
        val signal = CountDownLatch(1)
        var result: Response<Void>? = null

        response = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)

        service.postName(NameWrapper("Lucas"), object : Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                signal.countDown()
                result = null
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                signal.countDown()
                result = response
            }
        })
        signal.await()

        assertThat(result?.isSuccessful, equalTo(false))
    }
    //</editor-fold>

    //<editor-fold desc="Setup Classes">
    data class NameWrapper(
        val name: String
    )

    interface ICallbackAPI {
        @GET("names")
        fun getName(): Call<List<String>>

        @POST("names")
        fun postName(
            @Body nameWrapper: NameWrapper
        ): Call<Void>
    }

    class CallbackService {
        private val api: ICallbackAPI by lazy {
            Network.build<ICallbackAPI>()
        }

        fun getNames(callback: Callback<List<String>>) {
            api.getName().enqueue(callback)
        }

        fun postName(nameWrapper: NameWrapper, callback: Callback<Void>) {
            api.postName(nameWrapper).enqueue(callback)
        }
    }
    //</editor-fold>
}