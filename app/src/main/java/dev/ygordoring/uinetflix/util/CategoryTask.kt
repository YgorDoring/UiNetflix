package dev.ygordoring.uinetflix.util

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ProgressBar
import dev.ygordoring.uinetflix.R
import dev.ygordoring.uinetflix.model.Category
import dev.ygordoring.uinetflix.model.Movie
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.lang.Exception
import java.net.URL
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection

class CategoryTask(private val callback: Callback) {

    private val handler = Handler(Looper.getMainLooper())

    interface Callback {
        fun onPreExecute()
        fun onResult(categories: List<Category>)
        fun onFailure(message: String)
    }

    fun execute(url: String) {
        callback.onPreExecute()
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            var urlConnection: HttpsURLConnection? = null
            var stream: InputStream? = null

            try {
                val requestURL = URL(url)
                urlConnection = requestURL.openConnection() as HttpsURLConnection
                urlConnection.readTimeout = 2000
                urlConnection.connectTimeout = 2000
                val statusCode = urlConnection.responseCode
                if (statusCode > 400) {
                    throw IOException("Communication error")
                }

                stream = urlConnection.inputStream
                val jsonAsString = stream.bufferedReader().use { it.readText() }
                val categories = toCategories(jsonAsString)

                handler.post {
                    // Handler renderiza dentro da UI-thread
                    callback.onResult(categories)
                }


            } catch (e: Exception) {
                val message = e.message ?: "erro desconhecido"
                Log.e("teste", message, e)
                handler.post { callback.onFailure(message) }

            } finally {
                urlConnection?.disconnect()
                stream?.close()
            }
        }
    }

    private fun toCategories(jsonAsString: String) : List<Category> {
        val categories = mutableListOf<Category>()
        val jsonRoot = JSONObject(jsonAsString)
        val jsonCategories = jsonRoot.getJSONArray("category")
        for (i in 0 until jsonCategories.length()) {
            val jsonCategory = jsonCategories.getJSONObject(i)
            val title = jsonCategory.getString("title")
            val jsonMovies = jsonCategory.getJSONArray("movie")
            val movies = mutableListOf<Movie>()
            for (j in 0 until jsonMovies.length()) {
                val jsonMovie = jsonMovies.getJSONObject(j)
                val id = jsonMovie.getInt("id")
                val coverUrl = jsonMovie.getString("cover_url")
                movies.add(Movie(id, coverUrl))
            }
            categories.add(Category(title, movies))
        }
        return categories
    }

}