package com.hxbreak.animalcrossingtools.data

import androidx.lifecycle.LiveData
import com.hxbreak.animalcrossingtools.data.Result.Success
import retrofit2.*
import java.lang.Exception
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class LiveDataCallAdapter<R>(private val responseType: Type) :
    CallAdapter<R, LiveData<Result<R>>> {
    override fun adapt(call: Call<R>): LiveData<Result<R>> {
        return object : LiveData<Result<R>>() {
            private var isSuccess = false

            override fun onActive() {
                super.onActive()
                if (!isSuccess) enqueue()
            }

            override fun onInactive() {
                super.onInactive()
                dequeue()
            }

            private fun dequeue() {
                if (call.isExecuted) call.cancel()
            }

            private fun enqueue() {
                call.enqueue(object : Callback<R> {
                    override fun onFailure(call: Call<R>, t: Throwable) {
                        postValue(Result.Error(Exception(t)))
                    }

                    override fun onResponse(call: Call<R>, response: Response<R>) {
                        postValue(Success(response.body()!!))
                        isSuccess = true
                    }
                })
            }
        }
    }

    override fun responseType(): Type = responseType
}

class LiveDataCallAdapterFactory : CallAdapter.Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        val observableType =
            CallAdapter.Factory.getParameterUpperBound(
                0,
                returnType as ParameterizedType
            ) as? ParameterizedType
                ?: throw IllegalArgumentException("resource must be parameterized")
        return LiveDataCallAdapter<Any>(
            CallAdapter.Factory.getParameterUpperBound(
                0,
                observableType
            )
        )
    }
}