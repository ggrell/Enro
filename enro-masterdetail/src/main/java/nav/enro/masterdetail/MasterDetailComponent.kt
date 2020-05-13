package nav.enro.masterdetail

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import nav.enro.core.NavigationKey
import nav.enro.core.addOpenInstruction
import nav.enro.core.context.fragment
import nav.enro.core.context.parentActivity
import nav.enro.core.controller.NavigationController
import nav.enro.core.controller.navigationController
import nav.enro.core.executors.override.createOverride
import nav.enro.core.forward
import nav.enro.core.navigationHandle
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class MasterDetailController

class MasterDetailProperty(
    private val lifecycleOwner: LifecycleOwner,
    private val owningType: KClass<out FragmentActivity>,
    @IdRes private val masterContainer: Int,
    private val masterKey: KClass<out NavigationKey>,
    @IdRes private val detailContainer: Int,
    private val detailKey: KClass<out NavigationKey>,
    private val initialMasterKey: () -> NavigationKey
) : ReadOnlyProperty<Any, MasterDetailController> {

    private lateinit var masterDetailController: MasterDetailController
    private lateinit var navigationController: NavigationController

    private val masterOverride by lazy {
        val masterType = navigationController.navigatorForKeyType(masterKey)!!.contextType as KClass<out Fragment>
        createOverride(
            owningType,
            masterType,
            launch = {
                val fragment = it.fromContext.childFragmentManager.fragmentFactory.instantiate(
                    masterType.java.classLoader!!,
                    masterType.java.name
                ).addOpenInstruction(it.instruction)

                it.fromContext.childFragmentManager.beginTransaction()
                    .replace(masterContainer, fragment)
                    .setPrimaryNavigationFragment(fragment)
                    .commitNow()
            },
            close = {
                it.parentActivity.finish()
            }
        )
    }

    private val detailOverride by lazy {
        val detailType = navigationController.navigatorForKeyType(detailKey)!!.contextType as KClass<out Fragment>
        createOverride(
            owningType,
            detailType,
            launch = {
                val fragment =  it.fromContext.childFragmentManager.fragmentFactory.instantiate(
                    detailType.java.classLoader!!,
                    detailType.java.name
                ).addOpenInstruction(it.instruction)

                it.fromContext.childFragmentManager.beginTransaction()
                    .replace(detailContainer, fragment)
                    .setPrimaryNavigationFragment(fragment)
                    .commitNow()
            },
            close = { context ->
                context.fragment.parentFragmentManager.beginTransaction()
                    .remove(context.fragment)
                    .setPrimaryNavigationFragment(context.parentActivity.supportFragmentManager.findFragmentById(masterContainer))
                    .commitNow()
            }
        )
    }

    init {
        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if(event == Lifecycle.Event.ON_CREATE) {
                    navigationController = when(lifecycleOwner) {
                        is FragmentActivity -> lifecycleOwner.application.navigationController
                        is Fragment -> lifecycleOwner.requireActivity().application.navigationController
                        else -> TODO()
                    }
                    navigationController.addOverride(masterOverride)
                    navigationController.addOverride(detailOverride)

                    (lifecycleOwner as FragmentActivity).navigationHandle<Nothing>().value.forward(initialMasterKey())
                }

                if(event == Lifecycle.Event.ON_DESTROY){
                    navigationController.removeOverride(masterOverride)
                    navigationController.removeOverride(detailOverride)
                }
            }
        })
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): MasterDetailController {
        return masterDetailController
    }
}