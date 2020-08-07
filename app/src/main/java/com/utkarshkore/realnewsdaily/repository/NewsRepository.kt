package com.utkarshkore.realnewsdaily.repository

import com.utkarshkore.realnewsdaily.api.RetrofitInstance
import com.utkarshkore.realnewsdaily.db.ArticleDatabase
import com.utkarshkore.realnewsdaily.models.NewsResponse
import retrofit2.Response

/**
 * Created by Utkarsh Kore on 8/3/2020, Aug, 2020
 * UK Solutions Pvt. Ltd.
 * utkarshkore@gmail.com
 * 8693886401
 **/
class NewsRepository(private val db: ArticleDatabase) {

    suspend fun getTopHeadlines(countryCode: String, pageNumber: Int) =
        RetrofitInstance.api.getTopHeadlines(countryCode, pageNumber)

    suspend fun searchForArticles(searchQuery: String, pageNumber: Int) =
        RetrofitInstance.api.searchForArticles(searchQuery, pageNumber)
}