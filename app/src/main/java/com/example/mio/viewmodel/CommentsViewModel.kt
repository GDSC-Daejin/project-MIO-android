package com.example.mio.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mio.model.CommentData

class CommentsViewModel : ViewModel() {
    // LiveData for all comments (flattened list for RecyclerView handling)
    private val _allComments = MutableLiveData<List<CommentData>>()
    val allComments: LiveData<List<CommentData>> get() = _allComments

    // LiveData for parent comments
    private val _parentComments = MutableLiveData<List<CommentData>>()
    val parentComments: LiveData<List<CommentData>> get() = _parentComments

    // LiveData for child comments associated with each parent comment
    private val _childCommentsMap = MutableLiveData<Map<Int, List<CommentData>>>()
    val childCommentsMap: LiveData<Map<Int, List<CommentData>>> get() = _childCommentsMap

    // LiveData for loading state
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    // LiveData for error state
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // Set all comments externally
    fun setAllComments(newComments: List<CommentData>) {
        _allComments.value = newComments
    }

    fun setParentComments(newComments: List<CommentData>) {
        _parentComments.value = newComments
    }

    fun setChildComments(newComments: Map<Int, List<CommentData>>) {
        _childCommentsMap.value = newComments
    }

    fun setLoading(isLoading: Boolean) {
        _loading.value = isLoading
    }

    // Update error state externally
    fun setError(errorMessage: String) {
        _error.value = errorMessage
    }

    fun addComment(comment: CommentData) {
        val updatedList = _allComments.value.orEmpty().toMutableList()
        updatedList.add(comment)
        _allComments.value = updatedList
    }


    fun addParentComment(comment: CommentData) {
        val currentParentComments = _parentComments.value?.toMutableList() ?: mutableListOf()
        currentParentComments.add(comment)
        _parentComments.value = currentParentComments
    }

    fun addChildComment(childComment: CommentData, parentId: Int) {
        val currentChildCommentsMap = _childCommentsMap.value?.toMutableMap() ?: mutableMapOf()
        val childCommentsForParent = currentChildCommentsMap[parentId]?.toMutableList() ?: mutableListOf()

        childCommentsForParent.add(childComment)
        currentChildCommentsMap[parentId] = childCommentsForParent
        _childCommentsMap.value = currentChildCommentsMap
    }

    private fun updateAllComments() {

        val parentComments = _parentComments.value.orEmpty()
        val childCommentsMap = _childCommentsMap.value.orEmpty()

        val allComments = mutableListOf<CommentData>()

        for (parentComment in parentComments) {
            allComments.add(parentComment)
            val childComments = childCommentsMap[parentComment.commentId].orEmpty()
            allComments.addAll(childComments)
        }

        _allComments.value = allComments
    }

    // Remove a comment by marking it as "deleted"
    fun removeComment(commentId: Int) {
        // Update parent comments
        val updatedParentCommentsList = _parentComments.value.orEmpty().map {
            if (it.commentId == commentId) {
                it.copy(content = "삭제된 댓글입니다.")
            } else {
                it
            }
        }

        _parentComments.value = updatedParentCommentsList

        // Update child comments map
        val updatedChildCommentsMap = _childCommentsMap.value.orEmpty().mapValues { (_, childComments) ->
            childComments.map {
                if (it.commentId == commentId) {
                    it.copy(content = "삭제된 댓글입니다.")
                } else {
                    it
                }
            }
        }

        _childCommentsMap.value = updatedChildCommentsMap

        // Update all comments
        updateAllComments()
    }

    // Update a comment
    fun updateComment(updatedComment: CommentData) {
        // Check if the comment is marked as deleted
        if (updatedComment.content == "삭제된 댓글입니다.") {
            return
        }

        // Update parent comments
        val parentComments = _parentComments.value.orEmpty().toMutableList()
        val parentIndex = parentComments.indexOfFirst { it.commentId == updatedComment.commentId }

        if (parentIndex != -1) {
            // Update parent comment
            val existingParentComment = parentComments[parentIndex]
            val updatedParentComment = updatedComment.copy(childComments = existingParentComment.childComments)
            parentComments[parentIndex] = updatedParentComment
            _parentComments.value = parentComments
        } else {
            // Update child comments map
            val childCommentsMap = _childCommentsMap.value.orEmpty().toMutableMap()
            val parentId = childCommentsMap.entries.find { (_, childComments) ->
                childComments.any { it.commentId == updatedComment.commentId }
            }?.key

            if (parentId != null) {
                val mutableChildComments = childCommentsMap[parentId]?.toMutableList()

                if (mutableChildComments != null) {
                    val childIndex = mutableChildComments.indexOfFirst { it.commentId == updatedComment.commentId }
                    if (childIndex != -1) {
                        mutableChildComments[childIndex] = updatedComment
                        childCommentsMap[parentId] = mutableChildComments
                        _childCommentsMap.value = childCommentsMap
                    }
                }
            }
        }

        // Update all comments
        updateAllComments()
    }
}