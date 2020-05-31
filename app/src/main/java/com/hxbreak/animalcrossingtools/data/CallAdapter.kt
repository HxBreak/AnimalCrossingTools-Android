package com.hxbreak.animalcrossingtools.data

import android.util.Log
import androidx.lifecycle.LiveData
import com.hxbreak.animalcrossingtools.data.Result.Success
import okhttp3.Request
import okio.Timeout
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
        val type = CallAdapter.Factory.getParameterUpperBound(
            0,
            observableType
        )

        return if (LiveData::class.java == returnType.rawType && Result::class.java == observableType.rawType) {
            LiveDataCallAdapter<Any>(type)
        } else {
            null
        }
    }
}


class CoroutinesCallAdapterFactory : CallAdapter.Factory() {
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
        val type = CallAdapter.Factory.getParameterUpperBound(
            0,
            observableType
        )
        return if (returnType.rawType == Call::class.java && observableType.rawType == Result::class.java) {
            ResultCallAdapter<Any>(type)
        } else {
            null
        }
    }
}

/**
 * Result = Response
 * Make Call<Response> to Result<Response>
 */
class ResultCallAdapter<S : Any>(
    private val responseType: Type
) : CallAdapter<S, Call<Result<S>>> {
    override fun adapt(call: Call<S>): Call<Result<S>> {
        return ResultCall(call)
    }

    override fun responseType(): Type = responseType

    internal class ResultCall<S : Any>(
        private val delegate: Call<S>
    ) : Call<Result<S>> {
        override fun enqueue(callback: Callback<Result<S>>) {
            delegate.enqueue(object : Callback<S> {
                override fun onFailure(call: Call<S>, t: Throwable) {
                    callback.onResponse(
                        this@ResultCall,
                        Response.success(Result.Error(Exception(t)))
                    )
                }

                override fun onResponse(call: Call<S>, response: Response<S>) {
                    callback.onResponse(
                        this@ResultCall,
                        Response.success(Result.Success(response.body()!!))
                    )
                }
            })
        }

        override fun isExecuted(): Boolean = delegate.isExecuted

        override fun timeout(): Timeout = delegate.timeout()

        override fun clone(): Call<Result<S>> = ResultCall(delegate)

        override fun isCanceled(): Boolean = delegate.isCanceled

        override fun cancel() = delegate.cancel()

        override fun execute(): Response<Result<S>> {
            throw UnsupportedOperationException()
        }

        override fun request(): Request = delegate.request()
    }
}

