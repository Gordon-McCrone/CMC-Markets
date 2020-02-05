package com.cmcmarkets

import com.google.gson.Gson

interface NetConProtocol {

    fun Success(dict: Map<*,*>)
    fun Fail (dict: Map<*,*>)
}