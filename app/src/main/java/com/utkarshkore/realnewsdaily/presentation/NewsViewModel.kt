package com.utkarshkore.realnewsdaily.presentation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utkarshkore.realnewsdaily.models.Article
import com.utkarshkore.realnewsdaily.models.NewsResponse
import com.utkarshkore.realnewsdaily.repository.NewsRepository
import com.utkarshkore.realnewsdaily.utils.Resource
import kotlinx.android.synthetic.main.news_item.view.*
import kotlinx.coroutines.launch
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Utkarsh Kore on 8/3/2020, Aug, 2020
 * UK Solutions Pvt. Ltd.
 * utkarshkore@gmail.com
 * 8693886401
 **/
class NewsViewModel(private val newsRepository: NewsRepository) : ViewModel() {

    val topHeadlines: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    val latestArticles: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    val searchedArticles: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    val topArticle: MutableLiveData<Article> = MutableLiveData()

    var headlinesPage = 1
    var latestPage = 1
    var searchedPage = 1

    var topArticlesResponse: NewsResponse? = null
    var searchedArticlesResponse: NewsResponse? = null
    var latestArticlesResponse: NewsResponse? = null

    init {
        getTopHeadlines("in")
        getLatestArticles("in")
    }

    fun getTopHeadlines(countryCode: String, shouldInformTopArticle: Boolean = true) =
        viewModelScope.launch {
            topHeadlines.postValue(Resource.Loading())
            val response = newsRepository.getTopHeadlines(countryCode, headlinesPage)
            topHeadlines.postValue(handleTopHeadlinesResponse(response, shouldInformTopArticle))
        }

    fun getLatestArticles(countryCode: String) = viewModelScope.launch {
        latestArticles.postValue(Resource.Loading())
        val response = newsRepository.getTopHeadlines(countryCode, latestPage)
        latestArticles.postValue(handleLatestArticlesResponse(response))
    }

    fun getSearchedArticles(searchQuery: String) = viewModelScope.launch {
        searchedArticles.postValue(Resource.Loading())
        val response = newsRepository.searchForArticles(searchQuery, searchedPage)
        searchedArticles.postValue(handleSearchArticlesResponse(response))
    }

    private fun handleTopHeadlinesResponse(
        response: Response<NewsResponse>,
        shouldInformTopArticle: Boolean = true
    ): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let {
                headlinesPage++
                var oldArticles: MutableList<Article>? = null
                if (shouldInformTopArticle) {
                    if (topArticlesResponse == null) {
                        val article = it.articles.first()
                        val todaysDate = Date()
                        val removedT = article.publishedAt.replace("T", " ")
                        val removedZ = removedT.replace("Z", "")
                        val parsedDate = SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(removedZ)
                        val publishedDate = SimpleDateFormat("EEE dd MMM ").format(parsedDate)
                        val diff: Long = todaysDate.time - parsedDate.time
                        val hours = (diff % (24 * 3600)) / 3600

                        article.publishedAt = if (hours < 25) "$hours h ago" else publishedDate
                        topArticle.postValue(article)
                    }
                }
                if (topArticlesResponse == null) topArticlesResponse = it
                else {
                    oldArticles = topArticlesResponse?.articles
                    val newArticles = it.articles
                    oldArticles?.addAll(newArticles)
                }

                return Resource.Success(topArticlesResponse ?: it)
            }
        }

        return Resource.Error(response.message())
    }

    private fun handleLatestArticlesResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let {
                latestPage++
                if (latestArticlesResponse == null) latestArticlesResponse = it
                else {
                    val oldArticles = latestArticlesResponse?.articles
                    val newArticles = it.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(latestArticlesResponse ?: it)
            }
        }

        return Resource.Error(response.message())
    }

    private fun handleSearchArticlesResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let {
                headlinesPage++
                if (searchedArticlesResponse == null) searchedArticlesResponse = it
                else {
                    val oldArticles = searchedArticlesResponse?.articles
                    val newArticles = it.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(searchedArticlesResponse ?: it)
            }
        }

        return Resource.Error(response.message())
    }
}