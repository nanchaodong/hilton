package com.example.hilton.domain

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.Test
import org.junit.jupiter.api.assertThrows

interface NonUseCase
interface TestUseCase {
    fun get(): Int
}

interface AnotherTestUseCase {
    fun get(): Int
}

class TestOne : TestUseCase, StrongType {
    override fun get(): Int = 1
}

class TestTwo : TestUseCase {
    override fun get(): Int = 2
}

class AnotherTestOne : AnotherTestUseCase {
    override fun get(): Int = 1
}

class AnotherTestTwo : AnotherTestUseCase {
    override fun get(): Int = 2
}

class UseCaseTest {
    private val lock = Any()

    @Test
    fun `test get`() {
        synchronized(lock) {
            assertTrue {
                UseCase.get<TestUseCase>().get() == 1
            }
        }
    }
    @Test
    fun `test get by strong`() {
        synchronized(lock) {
            assertTrue {
                UseCase.getBy<TestUseCase>{ this is StrongType}.get() == 1
            }
        }
    }

    @Test
    fun `test get more than 20`() {
        synchronized(lock) {
            repeat(30) {
                UseCase.get<TestUseCase>().get()
            }
            System.gc()
            Runtime.getRuntime().gc()
            Thread.sleep(3000)
            UseCase.clear()
            assertTrue {
                UseCase.get<TestUseCase>().get() == 1
            }
        }
    }

    @Test
    fun `test get another more than 20`() {
        synchronized(lock) {
            repeat(30) {
                UseCase.get<AnotherTestUseCase>().get()
            }
            System.gc()
            Runtime.getRuntime().gc()
            Thread.sleep(3000)
            UseCase.clear()
            assertTrue {
                UseCase.get<AnotherTestUseCase>().get() == 1
            }
        }
    }

    @Test
    fun `test get by`() {
        synchronized(lock) {
            assertTrue {
                UseCase.getBy<TestUseCase> {
                    this !is StrongType
                }.get() == 2
            }
        }
    }

    @Test
    fun `test get by more than 20`() {
        synchronized(lock) {
            repeat(30) {
                UseCase.getBy<TestUseCase> {
                    this !is StrongType
                }.get()
            }
            System.gc()
            Runtime.getRuntime().gc()
            Thread.sleep(3000)
            UseCase.clear()
            assertTrue {
                UseCase.getBy<TestUseCase> {
                    this !is StrongType
                }.get() == 2
            }
        }
    }

    @Test
    fun `test get non`() {
        synchronized(lock) {
            assertThrows<Exception> {
                UseCase.get<NonUseCase>()
            }
        }
    }
}