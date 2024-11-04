import com.ritika.voy.api.Utility
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object Retrofit {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // Connection timeout
        .readTimeout(30, TimeUnit.SECONDS) // Read timeout
        .writeTimeout(30, TimeUnit.SECONDS) // Write timeout
        .build()

    val api: Retrofit = Retrofit.Builder()
        .baseUrl(Utility.BASE_URL)
        .client(client) // Set the custom OkHttpClient
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}