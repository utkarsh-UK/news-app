package com.utkarshkore.realnewsdaily.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.utkarshkore.realnewsdaily.repository.NewsRepository

/**
 * Created by Utkarsh Kore on 8/3/2020, Aug, 2020
 * UK Solutions Pvt. Ltd.
 * utkarshkore@gmail.com
 * 8693886401
 **/
class NewsViewModelProviderFactory(private val newsRepository: NewsRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return NewsViewModel(newsRepository) as T
    }
}