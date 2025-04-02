@file:Suppress("UNCHECKED_CAST")

package com.example.hilton.domain

import java.lang.ref.WeakReference
import java.util.ServiceLoader
import kotlin.reflect.KClass

interface StrongType
object UseCase {
    sealed interface Reference {
        fun key(): KClass<*>?
        data class Strong(val value: Any) : Reference {
            override fun key(): KClass<*> {
                return value::class
            }

        }

        data class Weak(val value: WeakReference<Any>) : Reference {
            override fun key(): KClass<*>? {
                val v = value.get()
                return if (v != null) {
                    v::class
                } else {
                    null
                }
            }
        }
    }

    private val map = mutableMapOf<KClass<*>, List<Reference>>()
    private var count = 0

    private fun <T : Any> getReferencesFromCache(kClass: KClass<T>): List<Reference>? {
        synchronized(this) {
            if (count++ >= 20) {
                clear()
            }
        }
        return map[kClass]
    }

    private fun <T> List<Reference>.getByFilter(predicate: T.() -> Boolean): T? {
        return firstOrNull {
            when (it) {
                is Reference.Strong -> {
                    (it.value as T).predicate()
                }

                is Reference.Weak -> {
                    (it.value.get() as? T)?.predicate() ?: false
                }
            }
        }?.run {
            when (this) {
                is Reference.Strong -> value as T
                is Reference.Weak -> value.get() as? T
            }
        }
    }

    fun <T : Any> getReferencesFromCacheByFilter(kClass: KClass<T>, predicate: T.() -> Boolean): T? {
        return getReferencesFromCache(kClass)?.getByFilter(predicate)
    }

    fun <T : Any> getReferencesFromCacheByDefault(kClass: KClass<T>): T? {
        return getReferencesFromCache(kClass)?.firstOrNull {
            it.key() != null
        }?.run {
            when (this) {
                is Reference.Strong -> value as T
                is Reference.Weak -> value.get() as? T
            }
        }
    }

    private fun <T : Any> loadAllReferences(kClass: KClass<T>): List<Reference> {
        return ServiceLoader.load(kClass.java, kClass.java.classLoader).iterator().asSequence().map {
            if (it is StrongType) {
                Reference.Strong(value = it)
            } else {
                Reference.Weak(WeakReference(it))
            }
        }.toList()
    }

    private fun List<Reference>.filterExist(): List<KClass<*>> {
        return mapNotNull { it.key() }
    }

    fun <T : Any> saveAndGet(kClass: KClass<T>, predicate: T.() -> Boolean): T? {
        return saveAllReferences(kClass).getByFilter(predicate)
    }

    fun <T : Any> saveAndGet(kClass: KClass<T>): T? {
        return saveAllReferences(kClass).first {
            it.key() != null
        }.run {
            when (this) {
                is Reference.Strong -> value as T
                is Reference.Weak -> value.get() as? T
            }
        }
    }

    private fun <T : Any> saveAllReferences(kClass: KClass<T>): List<Reference> {
        val list = loadAllReferences(kClass)
        if (list.isEmpty()) {
            error("there is no ${kClass.java.name} was implemented in META-INFO/service")
        }
        val exist = map.contains(kClass)
        if (exist) {
            val referenceValue = (map[kClass] ?: listOf()).filter {
                it.key() != null
            }.toMutableList()
            val referenceKeys = referenceValue.filterExist()
            if (referenceKeys.isEmpty()) {
                map[kClass] = list
            } else {
                list.forEach {
                    if (it.key() != null && it.key() !in referenceKeys) {
                        referenceValue.add(it)
                    }
                }
                map[kClass] = referenceValue
            }
        } else {
            map[kClass] = list
        }
        return map[kClass] ?: listOf()
    }


    inline fun <reified T : Any> getBy(noinline predicate: T.() -> Boolean): T {
        return synchronized(T::class) {
            getReferencesFromCacheByFilter(T::class, predicate) ?: saveAndGet(T::class, predicate) ?: error("no found")
        }
    }

    inline fun <reified T : Any> get(): T {
        return synchronized(T::class) {
            getReferencesFromCacheByDefault(T::class) ?: saveAndGet(T::class) ?: error("no found")
        }
    }

    fun clear() {
        val keys = mutableListOf<KClass<*>>()
        map.forEach { (t, u) ->
            val v = u.filter { it.key() != null }
            if (u.size != v.size && v.isNotEmpty()) {
                map[t] = v
            }
            if (v.isEmpty()) {
                keys.add(t)
            }
        }
        keys.forEach {
            map.remove(it)
        }
    }
}
