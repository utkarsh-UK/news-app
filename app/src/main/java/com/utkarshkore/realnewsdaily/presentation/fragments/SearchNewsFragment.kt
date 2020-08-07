package com.utkarshkore.realnewsdaily.presentation.fragments

import android.os.Bundle
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.utkarshkore.realnewsdaily.R
import com.utkarshkore.realnewsdaily.adapter.NewsAdapter
import com.utkarshkore.realnewsdaily.databinding.FragmentSearchNewsBinding
import com.utkarshkore.realnewsdaily.db.ArticleDatabase
import com.utkarshkore.realnewsdaily.presentation.HomeActivity
import com.utkarshkore.realnewsdaily.presentation.NewsViewModel
import com.utkarshkore.realnewsdaily.presentation.NewsViewModelProviderFactory
import com.utkarshkore.realnewsdaily.repository.NewsRepository
import com.utkarshkore.realnewsdaily.utils.Resource
import com.utkarshkore.realnewsdaily.utils.onRightDrawableClicked
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by Utkarsh Kore on 8/2/2020, Aug, 2020
 * UK Solutions Pvt. Ltd.
 * utkarshkore@gmail.com
 * 8693886401
 **/
class SearchNewsFragment : Fragment(R.layout.fragment_search_news) {
    private lateinit var viewModel: NewsViewModel
    private lateinit var binding: FragmentSearchNewsBinding
    private lateinit var newsAdapter: NewsAdapter

    private val TAG = "SearchNewsFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_news, container, false)
        viewModel = (activity as HomeActivity).viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpRecyclerView()

        newsAdapter.setOnItemClickListener { article, imageView, titleView ->
            val bundle = Bundle().apply {
                putSerializable("article", article)
            }
            findNavController().navigate(
                R.id.action_searchNewsFragment_to_articleDetailsFragment,
                bundle
            )
        }

        binding.textSearchNews.onRightDrawableClicked {
            if (it.text.toString().isNotEmpty()) viewModel.getSearchedArticles(it.text.toString())
        }

        viewModel.searchedArticles.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    binding.searchedNewsRecyclerView.visibility = View.VISIBLE
                    binding.shimmerViewContainer.visibility = View.GONE
                    response.data?.let {
                        newsAdapter.differ.submitList(it.articles)
                        val totalPages = it.totalResults / 20 + 2
                        isLastPage = viewModel.searchedPage == totalPages
                        binding.searchedNewsCount.text =
                            getString(R.string.search_result_count, it.totalResults)
                    }
                }

                is Resource.Error -> {
                    binding.shimmerViewContainer.visibility = View.GONE
                    response.message?.let {
                        Log.e(TAG, it)
                    }
                }

                is Resource.Loading -> {
                    if (newsAdapter.differ.currentList.isNotEmpty()) {
                        binding.searchedNewsRecyclerView.visibility = View.GONE
                    }
                    binding.shimmerViewContainer.visibility = View.VISIBLE
                }
            }
        })
    }

    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = binding.searchedNewsRecyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val totalVisibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isLastItem = firstVisibleItemPosition + totalVisibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= 19
            val shouldPaginate =
                isNotLoadingAndNotLastPage && isLastItem && isNotAtBeginning && isTotalMoreThanVisible
                        && isScrolling
            if (shouldPaginate){
                viewModel.getSearchedArticles(binding.textSearchNews.text.toString())
                isScrolling = false
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }
    }

    private fun setUpRecyclerView() {
        newsAdapter = NewsAdapter()
        binding.searchedNewsRecyclerView.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@SearchNewsFragment.scrollListener)
        }
    }
}