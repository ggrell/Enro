package nav.enro.core

import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import nav.enro.core.internal.handle.NavigationHandleViewModel

interface NavigationHandle<T : NavigationKey> {
    val key: T
    fun execute(navigationInstruction: NavigationInstruction)
    fun setOnCloseRequested(onCloseRequested: () -> Unit)
}

fun NavigationHandle<*>.forward(key: NavigationKey) =
    execute(NavigationInstruction.Open(NavigationDirection.FORWARD, key))

fun NavigationHandle<*>.replace(key: NavigationKey) =
    execute(NavigationInstruction.Open(NavigationDirection.REPLACE, key))

fun NavigationHandle<*>.replaceRoot(key: NavigationKey) =
    execute(NavigationInstruction.Open(NavigationDirection.REPLACE_ROOT, key))

fun NavigationHandle<*>.close() =
    execute(NavigationInstruction.Close)

fun <T : NavigationKey> FragmentActivity.navigationHandle(): Lazy<NavigationHandle<T>> = lazy {
    viewModels<NavigationHandleViewModel<T>>().value as NavigationHandle<T>
}

fun <T : NavigationKey> Fragment.navigationHandle(): Lazy<NavigationHandle<T>> = lazy {
    viewModels<NavigationHandleViewModel<T>>().value as NavigationHandle<T>
}