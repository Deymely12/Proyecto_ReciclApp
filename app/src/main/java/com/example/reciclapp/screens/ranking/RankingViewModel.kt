package com.example.reciclapp.screens.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reciclapp.model.RankedUser
import com.example.reciclapp.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.collections.filter
import kotlin.text.contains
import kotlin.text.isBlank

class RankingViewModel(
    private val repository: RankingRepository =
        RankingRepository(FirebaseFirestore.getInstance())
): ViewModel(){
    private var fullRankingList: List<RankedUser> = emptyList()

    private val _ranking = MutableStateFlow<List<RankedUser>>(emptyList())
    val ranking: StateFlow<List<RankedUser>> = _ranking

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    init {
        observeRanking()
    }

    private fun observeRanking() {
        viewModelScope.launch {
            repository.getRankingFlow().collect { list ->
                fullRankingList = list.mapIndexed { index, user ->
                    RankedUser(
                        user = user,
                        position = index + 1
                    )
                }
                filterRanking(_searchQuery.value)
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        filterRanking(query)
    }

    private fun filterRanking(query: String) {
        _ranking.value = if (query.isBlank()) {
            fullRankingList
        } else {
            fullRankingList.filter {
                it.user.firstname.contains(query, ignoreCase = true)
            }
        }
    }
}