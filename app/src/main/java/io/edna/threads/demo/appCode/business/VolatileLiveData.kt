package io.edna.threads.demo.appCode.business

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicInteger

open class VolatileLiveData<T> : MutableLiveData<T>() {
    private val lastValueSeq = AtomicInteger(0)
    private val wrappers = HashMap<Observer<in T>, Observer<T>>()

    @MainThread
    override fun setValue(value: T) {
        lastValueSeq.incrementAndGet()
        super.setValue(value)
    }

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        val observerWrapper = ObserverWrapper(lastValueSeq, observer)
        wrappers[observer] = observerWrapper
        super.observe(owner, observerWrapper)
    }

    @MainThread
    override fun observeForever(observer: Observer<in T>) {
        val observerWrapper = ObserverWrapper(lastValueSeq, observer)
        wrappers[observer] = observerWrapper
        super.observeForever(observerWrapper)
    }

    @MainThread
    override fun removeObserver(observer: Observer<in T>) {
        val observerWrapper = wrappers[observer]
        observerWrapper?.let {
            wrappers.remove(observerWrapper)
            super.removeObserver(observerWrapper)
        }
    }
}

private class ObserverWrapper<T>(private var currentSeq: AtomicInteger, private val observer: Observer<in T>) : Observer<T> {
    private val initialSeq = currentSeq.get()
    private var _observer: Observer<in T> = Observer {
        if (currentSeq.get() != initialSeq) {
            _observer = observer
            _observer.onChanged(it)
        }
    }

    override fun onChanged(value: T) {
        _observer.onChanged(value)
    }
}
