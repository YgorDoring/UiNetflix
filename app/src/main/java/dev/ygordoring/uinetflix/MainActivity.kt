package dev.ygordoring.uinetflix

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.ygordoring.uinetflix.model.Category
import dev.ygordoring.uinetflix.util.CategoryTask

class MainActivity : AppCompatActivity(), CategoryTask.Callback {

    private lateinit var progress: ProgressBar
    private lateinit var adapter: CategoryAdapter
    private val categories = mutableListOf<Category>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val rvMain: RecyclerView = findViewById(R.id.rv_main)
        adapter = CategoryAdapter(categories) { id ->
            val intent = Intent(this@MainActivity, MovieActivity::class.java)
            intent.putExtra("id", id)
            startActivity(intent)
        }
        rvMain.layoutManager = LinearLayoutManager(this)
        rvMain.adapter = adapter

        CategoryTask(this).execute("https://api.tiagoaguiar.co/netflixapp/home?apiKey=2e460dcc-a5bb-4d16-acb7-649d605ec190")
    }

    override fun onPreExecute() {
        progress = findViewById(R.id.progress_main)
        progress.visibility = View.VISIBLE
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResult(categories: List<Category>) {
        // Colocar o callback/listener do CategoryTask
        this.categories.clear()
        this.categories.addAll(categories)
        adapter.notifyDataSetChanged()
        progress.visibility = View.GONE
    }

    override fun onFailure(message: String) {
        Toast.makeText(this, message,Toast.LENGTH_SHORT).show()
        progress.visibility = View.VISIBLE
        Toast.makeText(this, R.string.message_error,Toast.LENGTH_LONG).show()
    }
}
