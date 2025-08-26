package com.tsaha.nucleus.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus

/**
 * Base ViewModel class that provides common functionality for all ViewModels
 */
abstract class BaseViewModel : ViewModel() {

    /**
     * Exception handler for coroutines launched in ViewModels
     */
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        handleError(throwable)
    }

    /**
     * Protected scope with error handling for ViewModel operations
     */
    protected val safeViewModelScope: CoroutineScope = viewModelScope + exceptionHandler

    /**
     * Handle errors that occur during ViewModel operations
     * Override this method to implement custom error handling
     */
    protected open fun handleError(throwable: Throwable) {
        // Default error handling - can be overridden in subclasses
        throwable.printStackTrace()
    }
}