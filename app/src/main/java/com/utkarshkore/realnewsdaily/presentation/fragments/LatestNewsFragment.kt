package com.utkarshkore.realnewsdaily.presentation.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.utkarshkore.realnewsdaily.R
import com.utkarshkore.realnewsdaily.adapter.NewsAdapter
import com.utkarshkore.realnewsdaily.databinding.FragmentLatestNewsBinding
import com.utkarshkore.realnewsdaily.presentation.HomeActivity
import com.utkarshkore.realnewsdaily.presentation.NewsViewModel
import com.utkarshkore.realnewsdaily.utils.Resource
import kotlinx.android.synthetic.main.fragment_latest_news.*

/**
 * Created by Utkarsh Kore on 8/2/2020, Aug, 2020
 * UK Solutions Pvt. Ltd.
 * utkarshkore@gmail.com
 * 8693886401
 **/
class LatestNewsFragment : Fragment(R.layout.fragment_latest_news) {
    private lateinit var viewModel: NewsViewModel
    private lateinit var binding: FragmentLatestNewsBinding
    private lateinit var newsAdapter: NewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_latest_news, container, false)
        return binding.apply {
            viewModel = (activity as HomeActivity).viewModel
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpRecyclerView()
        getSelectedCountry()

        newsAdapter.setOnItemClickListener { article, imageView, titleView ->
            val bundle = Bundle().apply {
                putSerializable("article", article)
            }
            findNavController().navigate(R.id.action_latestNewsFragment_to_articleDetailsFragment, bundle)
        }

        viewModel.latestArticles.observe(viewLifecycleOwner, Observer { response ->

            when (response) {
                is Resource.Success -> {
                    binding.shimmerViewContainer.visibility = View.GONE
                    response.data?.let {
                        newsAdapter.differ.submitList(it.articles)
                        val totalPages = it.totalResults / 20 + 2
                        isLastPage = viewModel.latestPage == totalPages
                        binding.latestNewsRecyclerView.visibility = View.VISIBLE
                    }
                }
                is Resource.Error -> {
                    binding.shimmerViewContainer.visibility = View.GONE
                }
                is Resource.Loading -> {
                    if (newsAdapter.differ.currentList.isNotEmpty())
                        binding.latestNewsRecyclerView.visibility = View.GONE

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

            val layoutManager = binding.latestNewsRecyclerView.layoutManager as LinearLayoutManager
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
                getSelectedCountry()
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
        binding.latestNewsRecyclerView.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@LatestNewsFragment.scrollListener)
        }
    }

    private fun getSelectedCountry() {
        binding.countriesChipGroup.setOnCheckedChangeListener { _, checkedId ->
            var countryCode = ""

            when (checkedId) {
                binding.chipIndia.id -> countryCode = "in"
                binding.chipUs.id -> countryCode = "us"
                binding.chipAus.id -> countryCode = "au"
            }

            viewModel.getLatestArticles(countryCode)
        }
    }
}