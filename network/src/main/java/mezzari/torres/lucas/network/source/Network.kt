package mezzari.torres.lucas.network.source

import mezzari.torres.lucas.network.annotation.Route
import mezzari.torres.lucas.network.source.auth.BaseAuth
import mezzari.torres.lucas.network.source.auth.NoAuth
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import kotlin.reflect.KClass

/**
 * @author Lucas T. Mezzari
 * @since 25/07/2019
 *
 * Generic class for network use with retrofit
 **/
object Network {

    //List of modules in retrofit level
    private var retrofitLevelModules: List<RetrofitLevelModule> = ArrayList()
    //List of modules in retrofit client level
    private var okHttpClientLevelModule: List<OkHttpClientLevelModule> = ArrayList()
    //An instance that will deal with the authentication of the network
    internal var auth: BaseAuth = NoAuth()

    //This property will map and guard all retrofit instances
    //Every base url will have a single retrofit instance
    private val retrofitInstances: HashMap<String, Retrofit> = HashMap()

    /**
     * This method should be called in the onCreate of the Application
     * It will receive the array of modules to initialize the network properties
     */
    fun initialize(
        retrofitLevelModules: List<RetrofitLevelModule> = ArrayList(),
        okHttpClientLevelModule: List<OkHttpClientLevelModule> = ArrayList(),
        auth: BaseAuth = NoAuth()
    ) {
        this.retrofitLevelModules = retrofitLevelModules
        this.okHttpClientLevelModule = okHttpClientLevelModule
        this.auth = auth
    }

    /**
     * This method build a API to call the needed services
     * It will bring the class already casted
     */
    inline fun <reified T> build(url: String = ""): T {
        return build(T::class, url)
    }

    /**
     * This method build a API to call the needed services
     * It will bring the class already casted
     */
    fun <T> build(dClass: KClass<*>, url: String = ""): T {
        //Set the baseUrl with the value from the parameter
        var baseUrl = url
        //If it is empty, get it from the annotation
        if (baseUrl.isEmpty()) {
            //Get the Service Route annotation
            val serviceRoute = dClass.annotations.find { it.annotationClass == Route::class } as? Route
                ?: throw RuntimeException("The class should be annotated with ServiceRoute")
            //Change the base url
            baseUrl = serviceRoute.url
        }

        //Check if a retrofit instance for the given base url already exists
        if (!retrofitInstances.containsKey(baseUrl)) {
            //If there is no retrofit instance, creates one and set in the map
            retrofitInstances[baseUrl] = createRetrofit(baseUrl)
        }

        //Creates the API and cast to the expected object
        //Shouldn't return null
        return retrofitInstances[baseUrl]!!.create(dClass.java) as T
    }

    /**
     * Create a retrofit object with the configuration
     * When the default configuration is built, it will loops through the retrofit modules to configure what is needed
     */
    private fun createRetrofit(baseUrl: String): Retrofit {
        //Creates a retrofit builder
        val retrofitBuilder = Retrofit.Builder()
            //Set the base URL
            .baseUrl(baseUrl)
            //Creates and Set the client
            .client(createHttpClient())

        //Loops through the list of retrofit level modules
        retrofitLevelModules.forEach {
            //Call the interface method
            it.onRetrofitBuilderCreated(retrofitBuilder)
        }

        //Returns the built retrofit
        return retrofitBuilder.build()
    }

    /**
     * Creates the http client
     * It will loop through the client modules to configure interceptors and other items
     */
    private fun createHttpClient(): OkHttpClient {
        //Create the client builder
        val clientBuilder = OkHttpClient.Builder()

        //Loops through the client level modules
        okHttpClientLevelModule.forEach {
            //Call the interface methods
            it.onClientBuilderCreated(clientBuilder)
        }

        //Return the built client
        return clientBuilder.build()
    }

    /**
     * Interface for retrofit level modules
     */
    interface RetrofitLevelModule {
        /**
         * This method is called in the createRetrofit
         * Should be used to add attributes to the retrofit
         */
        fun onRetrofitBuilderCreated(retrofitBuilder: Retrofit.Builder)
    }

    /**
     * Interface for client level modules
     */
    interface OkHttpClientLevelModule {
        /**
         * This method is called in the createHttpclient method
         * Should be used to add attributes to the client
         */
        fun onClientBuilderCreated(okHttpClientBuilder: OkHttpClient.Builder)
    }
}