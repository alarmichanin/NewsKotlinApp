package com.example.thenewsapp.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.thenewsapp.models.Article
import com.example.thenewsapp.models.NewsResponse
import com.example.thenewsapp.models.Profile
import com.example.thenewsapp.repository.NewsRepository
import com.example.thenewsapp.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Response
import java.io.IOException

class NewsViewModel(app: Application, val newsRepository: NewsRepository): AndroidViewModel(app) {

    val headlines: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var headlinesPage = 1
    var headlinesResponse: NewsResponse? = null

    val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNewsPage = 1
    var searchNewsResponse: NewsResponse? = null
    var isSearchByCategory: Boolean = false
    var newSearchQuery: String? = null
    var oldSearchQuery: String? = null
    var userHasCategories: Boolean = false

    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var categoryHeadlinesPage = mutableMapOf<String, Int>()

    init {
        getHeadlines("us")
    }

    fun getHeadlines(countryCode: String) = viewModelScope.launch {
        if(firebaseAuth.currentUser != null){
            val userId = firebaseAuth.currentUser?.uid
            if (userId != null) {
                val categories = getUserCategories(userId)
                categories.forEach { category ->
                    categoryHeadlinesPage[category] = 1
                }
                headlinesInternet(countryCode, categories)
            }
        }else{
            headlinesInternet(countryCode)
        }
    }

    suspend fun getUserCategories(userId: String): List<String> {
        return try {
            val userRef = firebaseDatabase.getReference("users").child(userId)
            val dataSnapshot = userRef.get().await()
            val profile = dataSnapshot.getValue(Profile::class.java)
            profile?.categories ?: listOf()
        } catch (e: Exception) {
            // Log error and return an empty list or handle it as needed
            listOf()
        }
    }

    fun getHeadlinesByCategory(countryCode: String, categoryName: String) = viewModelScope.launch {
        searchNewsByCategory(countryCode, categoryName)
    }

    fun searchNews(searchQuery: String) = viewModelScope.launch {
        searchNewsInternet(searchQuery)
    }

    private fun handleHeadlinesResponse(response: Response<NewsResponse>): Resource<NewsResponse>{
        if (response.isSuccessful){
            response.body()?.let { resultResponse ->
                val filteredArticles = resultResponse.articles.filter { it.urlToImage != null && it.urlToImage != "" }

                filteredArticles.forEach { article ->
                    if (article.source.id == null) {
                        article.source.id = "Unknown"
                    }
                    if (article.author == null) {
                        article.author = "Unknown"
                    }
                }

                headlinesPage++
                if ((userHasCategories && headlinesPage == 1) || headlinesResponse == null){
                    headlinesResponse = resultResponse.copy(articles = filteredArticles.toMutableList())
                } else {
                    val oldArticles = headlinesResponse?.articles
                    oldArticles?.addAll(filteredArticles)
                }

                return Resource.Success(headlinesResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleSearchNewsResponse(response: Response<NewsResponse>) : Resource<NewsResponse> {
        if(response.isSuccessful) {
            response.body()?.let { resultResponse ->
                // Filter out articles without images
                val filteredArticles = resultResponse.articles.filter { it.urlToImage != null && it.urlToImage != "" }

                // Set source.id to empty string if it's missing
                filteredArticles.forEach { article ->
                    if (article.source.id == null) {
                        article.source.id = "Unknown"
                    }
                    if (article.author == null) {
                        article.author = "Unknown"
                    }
                }

                if(isSearchByCategory || searchNewsResponse == null || newSearchQuery != oldSearchQuery) {
                    searchNewsPage = 1
                    oldSearchQuery = newSearchQuery
                    searchNewsResponse = resultResponse.copy(articles = filteredArticles.toMutableList())
                } else {
                    searchNewsPage++
                    val oldArticles = searchNewsResponse?.articles
                    oldArticles?.addAll(filteredArticles)
                }
                return Resource.Success(searchNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    fun addToFavourites(article: Article) = viewModelScope.launch {
        newsRepository.upsert(article)
    }

    fun getFavouriteNews() = newsRepository.getFavouriteNews()

    fun deleteArticle(article: Article) = viewModelScope.launch {
        newsRepository.deleteArticle(article)
    }

    fun internetConnection(context: Context): Boolean {
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).apply {
            return getNetworkCapabilities(activeNetwork)?.run {
                when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            } ?: false
        }
    }

    private suspend fun headlinesInternet(countryCode: String,categories: List<String> = listOf()){
        headlines.postValue(Resource.Loading())
        try {
            if(internetConnection(this.getApplication())) {
                if (categories.isNotEmpty()) {
                    userHasCategories = true
                    // Fetch news for each category
                    val allArticles = mutableListOf<Article>()
                    var totalResults = 0
                    categories.forEach { category ->
                        val page = categoryHeadlinesPage[category] ?: 1
                        val response = newsRepository.getHeadlinesWithCategories(countryCode, page, category)
                        val resource = handleHeadlinesResponse(response)
                        if (resource is Resource.Success) {
                            resource.data?.let { allArticles.addAll(it.articles) }
                            totalResults += resource.data!!.totalResults
                        }
                    }
                    val combinedResponse = NewsResponse(allArticles, "ok", totalResults)
                    headlines.postValue(Resource.Success(combinedResponse))
                } else {
                    if (userHasCategories) {
                        headlinesResponse = null
                        userHasCategories = false
                    }

                    val response = newsRepository.getHeadlines(countryCode, headlinesPage)
                    headlines.postValue(handleHeadlinesResponse(response))
                }
            } else {
                headlines.postValue(Resource.Error("No internet connection"))
            }
        } catch(t: Throwable) {
            when(t) {
                is IOException -> headlines.postValue(Resource.Error("Unable to connect"))
                else -> headlines.postValue(Resource.Error("No signal"))
            }
        }
    }

    private suspend fun searchNewsInternet(searchQuery: String) {
        isSearchByCategory = false
        newSearchQuery = searchQuery
        searchNews.postValue(Resource.Loading())
        try {
            if(internetConnection(this.getApplication())) {
                val response = newsRepository.searchNews(searchQuery, searchNewsPage)
                searchNews.postValue(handleSearchNewsResponse(response))
            } else {
                searchNews.postValue(Resource.Error("No internet connection"))
            }
        } catch(t: Throwable) {
            when(t) {
                is IOException -> searchNews.postValue(Resource.Error("Unable to connect"))
                else -> searchNews.postValue(Resource.Error("No signal"))
            }
        }
    }

    private suspend fun searchNewsByCategory(countryCode: String, categoryName: String) {
        isSearchByCategory = true
        searchNews.postValue(Resource.Loading())
        try {
            if(internetConnection(this.getApplication())) {
                val response = newsRepository.getHeadlinesWithCategories(countryCode, searchNewsPage, categoryName)
                searchNews.postValue(handleSearchNewsResponse(response))
            } else {
                searchNews.postValue(Resource.Error("No internet connection"))
            }
        } catch(t: Throwable) {
            when(t) {
                is IOException -> searchNews.postValue(Resource.Error("Unable to connect"))
                else -> searchNews.postValue(Resource.Error("No signal"))
            }
        }
    }
}