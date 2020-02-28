package mezzari.torres.lucas.network

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import mezzari.torres.lucas.network.source.Network
import mezzari.torres.lucas.network.source.auth.BaseAuth
import mezzari.torres.lucas.network.source.module.client.CookiesModule
import mezzari.torres.lucas.network.source.module.client.LogModule
import mezzari.torres.lucas.network.source.module.retrofit.GsonConverterModule
import mezzari.torres.lucas.network.source.promise.BaseNetworkPromise
import mezzari.torres.lucas.network.source.promise.NetworkPromise
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
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
class NetworkPromiseTest {

    //<editor-fold desc="Properties">
    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private lateinit var mockedServer: MockWebServer

    private var response: MockResponse = MockResponse().apply {
        setResponseCode(HttpURLConnection.HTTP_NOT_FOUND)
    }

    private val dispatcher = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            return response
        }
    }

    private var shouldInvalidateAuthentication = false

    private var onUnauthorized: () -> Unit = {

    }

    private var onResponseIntercepted: () -> Boolean = {
        false
    }

    private var onFailureIntercepted: () -> Boolean = {
        false
    }

    private var interceptor: Network.ResponseInterceptor = object : Network.ResponseInterceptor {
        override fun <T> onFailure(
            call: Call<T>,
            t: Throwable,
            promise: BaseNetworkPromise<T>
        ): Boolean {
            return onFailureIntercepted()
        }

        override fun <T> onResponse(
            call: Call<T>,
            response: Response<T>,
            promise: BaseNetworkPromise<T>
        ): Boolean {
            return onResponseIntercepted()
        }

    }

    private val authenticator = object : BaseAuth() {
        override fun <T> doAuthentication(call: Call<T>, callback: Callback<T>) {
            response = MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
                .addHeader("Set-Cookie", "Code=1234")

            count++

            service.getAuth().then {
                this@NetworkPromiseTest.response = if (shouldInvalidateAuthentication) {
                    MockResponse().setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED)
                } else {
                    MockResponse()
                        .setResponseCode(HttpURLConnection.HTTP_OK)
                }

                call.clone().enqueue(callback)
            }.catch {

            }
        }

        override fun onAuthenticationFailed() {
            shouldInvalidateAuthentication = false
            onUnauthorized()
        }
    }

    private lateinit var service: PromiseService
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
            responseInterceptors = arrayListOf(interceptor, interceptor),
            auth = authenticator,
            baseUrl = mockedServer.url("/").toString()
        )

        service = PromiseService()
    }

    @After
    fun tearDown() {
        mockedServer.shutdown()
    }
    //</editor-fold>

    //<editor-fold desc="Test GET names">
    @Test
    fun `test valid get names`() {
        val signal = CountDownLatch(1)
        var result: Response<List<String>>? = null

        response = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody("[\"Clarrisa\", \"Natália\", \"João\"]")

        service.getNames().then {
            signal.countDown()
            result = response
        }.catch {
            signal.countDown()
            result = null
        }

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
            .setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)

        service.getNames().then {
            signal.countDown()
            result = response
        }.catch {
            signal.countDown()
            result = null
        }

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

        service.getNames().then {
            signal.countDown()
            result = response
        }.catch {
            signal.countDown()
            result = null
        }

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

        service.postName(NameWrapper("Lucas")).then {
            signal.countDown()
            result = response
        }.catch {
            signal.countDown()
            result = null
        }

        signal.await()

        assertThat(result?.isSuccessful, equalTo(true))
    }

    @Test
    fun `test invalid post name`() {
        val signal = CountDownLatch(1)
        var result: Response<Void>? = null

        response = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)

        service.postName(NameWrapper("Lucas")).then {
            signal.countDown()
            result = response
        }.catch {
            signal.countDown()
            result = response
        }

        signal.await()

        assertThat(result?.isSuccessful, equalTo(false))
    }
    //</editor-fold>

    //<editor-fold desc="Test Auth">
    @Test
    fun `test valid post name auth`() {
        val signal = CountDownLatch(1)
        var result: Response<Void>? = null

        response = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED)

        service.postName(NameWrapper("Lucas")).then {
            signal.countDown()
            result = response
        }.catch {
            signal.countDown()
            result = null
        }

        signal.await()

        assertThat(result?.isSuccessful, equalTo(true))
    }

    @Test
    fun `test invalid post name auth`() {
        val signal = CountDownLatch(1)
        var wasUnauthorized = false

        onUnauthorized = {
            wasUnauthorized = true
            signal.countDown()
        }

        response = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED)

        shouldInvalidateAuthentication = true

        service.postName(NameWrapper("Lucas")).then {
            signal.countDown()
            wasUnauthorized = false
        }.catch {
            signal.countDown()
            wasUnauthorized = false
        }

        signal.await()

        assertThat(wasUnauthorized, equalTo(true))
    }
    //</editor-fold>

    //<editor-fold desc="Test Response Interceptors">
    @Test
    fun `test all response interceptors`() {
        val signal = CountDownLatch(1)
        var responseCount = 0

        onResponseIntercepted = {
            responseCount++
            false
        }

        response = MockResponse().setResponseCode(HttpURLConnection.HTTP_CREATED)

        service.postName(NameWrapper("João")).then {
            signal.countDown()
            responseCount++
        }.catch {
            signal.countDown()
            responseCount++
        }

        signal.await()

        assertThat(responseCount, equalTo(3))
    }

    @Test
    fun `test single response interceptors`() {
        val signal = CountDownLatch(1)
        var responseCount = 0

        onResponseIntercepted = {
            signal.countDown()
            responseCount++
            true
        }

        response = MockResponse().setResponseCode(HttpURLConnection.HTTP_CREATED)

        service.postName(NameWrapper("João")).then {
            signal.countDown()
            responseCount++
        }.catch {
            signal.countDown()
            responseCount++
        }

        signal.await()

        assertThat(responseCount, equalTo(1))
    }
    //</editor-fold>

    //<editor-fold desc="Test Failure Interceptors">
    @Test
    fun `test all failure interceptors`() {
        val signal = CountDownLatch(1)
        var responseCount = 0

        onFailureIntercepted = {
            responseCount++
            false
        }

        response = MockResponse().setResponseCode(HttpURLConnection.HTTP_OK).setBody("{}")

        service.getNames().then {
            signal.countDown()
            responseCount++
        }.catch {
            signal.countDown()
            responseCount++
        }

        signal.await()

        assertThat(responseCount, equalTo(3))
    }

    @Test
    fun `test single failure interceptor`() {
        val signal = CountDownLatch(1)
        var responseCount = 0

        onFailureIntercepted = {
            signal.countDown()
            responseCount++
            true
        }

        response = MockResponse().setResponseCode(HttpURLConnection.HTTP_OK).setBody("{}")

        service.getNames().then {
            signal.countDown()
            responseCount++
        }.catch {
            signal.countDown()
            responseCount++
        }

        signal.await()

        assertThat(responseCount, equalTo(1))
    }
    //</editor-fold>

    //<editor-fold desc="Test Properties">
    @Test
    fun `test response call`() {
        val signal = CountDownLatch(1)

        var requestCall: Call<Void>? = null
        response = MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)

        service.postName(NameWrapper(("Pedro"))).then {
            signal.countDown()
            requestCall = call
        }.catch {
            signal.countDown()
            requestCall = call
        }

        signal.await()

        assertThat(requestCall, notNullValue())
    }

    @Test
    fun `test exception call`() {
        val signal = CountDownLatch(1)

        var errorMessage: String? = null
        response = MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)

        service.postName(NameWrapper(("Pedro"))).then {
            signal.countDown()
        }.catch { error ->
            signal.countDown()
            errorMessage = error
        }

        signal.await()

        assertThat(errorMessage, equalTo("Client Error"))
    }

    @Test
    fun `test throwable call`() {
        val signal = CountDownLatch(1)

        var errorMessage: String? = null
        response = MockResponse()
            .setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody("{}")

        service.getNames().then {
            signal.countDown()
        }.catch {
            errorMessage = throwable?.message
            signal.countDown()
        }

        signal.await()

        assertThat(
            errorMessage,
            equalTo("Expected BEGIN_ARRAY but was BEGIN_OBJECT at line 1 column 2 path \$")
        )
    }
    //</editor-fold>

    //<editor-fold desc="Setup Classes">
    data class NameWrapper(
        val name: String
    )

    interface IPromiseAPI {
        @GET("names")
        fun getNames(): Call<List<String>>

        @POST("names")
        fun postName(
            @Body nameWrapper: NameWrapper
        ): Call<Void>

        @GET("auth")
        fun getAuth(): Call<Void>
    }

    class PromiseService {
        private val api: IPromiseAPI by lazy {
            Network.build<IPromiseAPI>()
        }

        fun getNames(): NetworkPromise<List<String>> {
            return NetworkPromise {
                api.getNames().enqueue(this)
            }
        }

        fun postName(nameWrapper: NameWrapper): NetworkPromise<Void> {
            return NetworkPromise {
                api.postName(nameWrapper).enqueue(this)
            }
        }

        fun getAuth(): NetworkPromise<Void> {
            return NetworkPromise {
                api.getAuth().enqueue(this)
            }
        }
    }
    //</editor-fold>
}