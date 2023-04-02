package ru.netology.supervision

import kotlinx.coroutines.*
import okhttp3.internal.wait
import kotlin.coroutines.EmptyCoroutineContext

// Section: Cancellation
// Question1: Отработает ли в этом коде строка <--? Поясните, почему да или нет.
// Answer: Не отработает, потому что в родительской job была вызвана функция ее отмены: job.cancelAndJoin(), что,
// в свою очередь, запустило отмену дочерних корутин

//fun main() = runBlocking {
//    val job = CoroutineScope(EmptyCoroutineContext).launch {
//        launch {
//            delay(500)
//            println("ok") // <--
//        }
//        launch {
//            delay(500)
//            println("ok")
//        }
//    }
//    delay(100)
//    job.cancelAndJoin()
//}


// Question2: Отработает ли в этом коде строка <--. Поясните, почему да или нет.
// Answer: Не отработает, потому что мы отменили корутину, сохраненную в job "child" прежде, чем истекла задержка 500мс перед
// запуском функции println("ok")

//fun main() = runBlocking {
//    val job = CoroutineScope(EmptyCoroutineContext).launch {
//        val child = launch {
//            delay(500)
//            println("ok") // <--
//        }
//        launch {
//            delay(500)
//            println("ok2")
//        }
//        delay(100)
//        child.cancel()
//    }
//    delay(100)
//    job.join()
//}


// Section: Exception Handling
// Question1: Отработает ли в этом коде строка <--. Поясните, почему да или нет.
// Answer: Не отработает, так как исключение, не будучи обработанным внутри launch-блока, не будет перехвачено catch-блоком являющегося парным
// для охватывающего корутину try-блока так как данный прием перехвата сбоев внтури корутины является неверным.

//fun main() {
//    with(CoroutineScope(EmptyCoroutineContext)) {
//        try {
//            launch {
//                throw Exception("something bad happened")
//            }
//        } catch (e: Exception) {
//            e.printStackTrace() // <--
//        }
//    }
//    Thread.sleep(1000)
//}


// Question2: Отработает ли в этом коде строка <--. Поясните, почему да или нет.
// Answer: Отработает, так перехват исключения осуществлен внутри try-catch блока, находящегося внутри launch-блока родительской корутины
// Блок coroutineScope перехватывает ошибку и передает ее в catch-блок для обработки.

//fun main() {
//    CoroutineScope(EmptyCoroutineContext).launch {
//        try {
//            coroutineScope {
//                throw Exception("something bad happened")
//            }
//        } catch (e: Exception) {
//            e.printStackTrace() // <--
//        }
//    }
//    Thread.sleep(1000)
//}


// Question3: Отработает ли в этом коде строка <--. Поясните, почему да или нет.
// Answer: Отработает, так перехват исключения осуществлен внутри try-catch блока, находящегося внутри launch-блока родительской корутины
// Блок supervisorScope перехватывает ошибку и передает ее в catch-блок для обработки, так как у него не предусмотрено собственного
// обработчика для сценария, когда ошибка пришла не изнутри корутины, находящейся под его наблюдением.

//fun main() {
//    CoroutineScope(EmptyCoroutineContext).launch {
//        try {
//            supervisorScope {
//                throw Exception("something bad happened")
//            }
//        } catch (e: Exception) {
//            e.printStackTrace() // <--
//        }
//    }
//    Thread.sleep(1000)
//}


// Question4: Отработает ли в этом коде строка <--. Поясните, почему да или нет.
// Answer: Не отработает, так как у coroutineScope не предусмотрено механизма защиты дочерних корутин от сбоя в соседних корутинах
// Исключение будет передано coroutineScope в вышестоящий catch-блок для обработки, при этом соседние корутины прекращают свою работу.


//fun main() {
//    CoroutineScope(EmptyCoroutineContext).launch {
//        try {
//            coroutineScope {
//                launch {
//                    delay(500)
//                    throw Exception("something bad happened2") // <--
//                }
//                launch {
//                    throw Exception("something bad happened1")
//                }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            println("!")
//        }
//    }
//    Thread.sleep(1000)
//}


// Question5: Отработает ли в этом коде строка <--. Поясните, почему да или нет.
// Answer: Отработает, так как у supervisorScope предусмотрен механизм защиты дочерних корутин от сбоя в соседних корутинах.
// При сбое в одной из корутин, соседние корутины продолжают свою работу.
// Исключение, возникшее в любой из корутин будет передано supervisorScope в вышестоящий catch-блок для обработки.


//fun main() {
//    CoroutineScope(EmptyCoroutineContext).launch {
//        try {
//            supervisorScope {
//                launch {
//                    delay(500)
//                    throw Exception("something bad happened") // <--
//                }
//                launch {
//                    throw Exception("something bad happened")
//                }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace() // <--
//        }
//    }
//    Thread.sleep(1000)
//}


// Question6: Отработает ли в этом коде строка <--. Поясните, почему да или нет.
// Answer: Не отработает, так как был вызван сбой во внутренней корутине, повлекший за собой отмену дочерних корутин


//fun main() {
//    CoroutineScope(EmptyCoroutineContext).launch {
//        CoroutineScope(EmptyCoroutineContext).launch {
//            launch {
//                delay(1000)
//                println("ok") // <--
//            }
//            launch {
//                delay(500)
//                println("ok")
//            }
//             throw Exception("something bad happened")
//        }
//    }
//    Thread.sleep(1000)
//}


// Question7: Отработает ли в этом коде строка <--. Поясните, почему да или нет.
// Answer: Не отработает, так как был вызван сбой во внутренней корутине, повлекший за собой отмену дочерних корутин
// Добавление  SupervisorJob() погоды не сделает так как у него предусмотрен свой обработчик только для дочерних корутин, а исключение
// произошло в родительской по отгношению к 2-м дочерним, соответственно оно не будет им обработано, будет обработано выше.

//fun main() {
//    CoroutineScope(EmptyCoroutineContext).launch {
//        CoroutineScope(EmptyCoroutineContext + SupervisorJob()).launch {
//            launch {
//                delay(1000)
//                println("ok") // <--
//            }
//            launch {
//                delay(500)
//                println("ok")
//            }
//            throw Exception("something bad happened")
//        }
//    }
//    Thread.sleep(1000)
//}
