package dev.ygordoring.uinetflix.util

import android.os.Handler
import android.os.Looper
import android.util.Log
import dev.ygordoring.uinetflix.model.Movie
import dev.ygordoring.uinetflix.model.MovieDetail
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.lang.Exception
import java.net.URL
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection

class MovieTask(private val callback: Callback) {

    private val handler = Handler(Looper.getMainLooper())
    private val executor = Executors.newSingleThreadExecutor()
    private var buffer: BufferedInputStream? = null
    private var jsonAsString = toString()

    interface Callback {
        fun onPreExecute()
        fun onResult(movieDetail: MovieDetail)
        fun onFailure(message: String)
    }

    fun execute(url: String) {
        callback.onPreExecute()
        executor.execute {
            var urlConnection: HttpsURLConnection? = null
            var stream: InputStream? = null

            try {
                val requestURL = URL(url)
                urlConnection = requestURL.openConnection() as HttpsURLConnection
                urlConnection.readTimeout = 2000
                urlConnection.connectTimeout = 2000
                val statusCode = urlConnection.responseCode

                if (statusCode == 400) {
                    stream = urlConnection.errorStream
                    buffer = BufferedInputStream(stream)
                    jsonAsString = buffer.toString()

                    val json = JSONObject(jsonAsString)
                    val message = json.getString("message")
                    throw IOException(message)

                } else if (statusCode > 400) {
                    throw IOException("Communication error")
                }

                stream = urlConnection.inputStream
                buffer = BufferedInputStream(stream)

                val jsonAsString = stream.bufferedReader().use { it.readText() }
                val movieDetail = toMovieDetail(jsonAsString)

                handler.post {
                    // Handler renderiza dentro da UI-thread
                    callback.onResult(movieDetail)
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
    private fun toMovieDetail(jsonAsString: String): MovieDetail {
        val json = JSONObject(jsonAsString)
        val id = json.getInt("id")
        val title = json.getString("title")
        val desc = json.getString("desc")
        val cast = json.getString("cast")
        val coverUrl = json.getString("cover_url")
        val jsonMovies = json.getJSONArray("movie")

        val similar = mutableListOf<Movie>()
        for (i in 0 until jsonMovies.length()) {
            val jsonMovie = jsonMovies.getJSONObject(i)
            val similarId = jsonMovie.getInt("id")
            val similarCoverUrl = jsonMovie.getString("cover_url")
            val m = Movie(similarId, similarCoverUrl)
            similar.add(m)
        }
        val movie = Movie(id, coverUrl, title, desc, cast)
        return MovieDetail(movie, similar)
    }
}