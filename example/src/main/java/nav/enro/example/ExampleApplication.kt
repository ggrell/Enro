package nav.enro.example

import android.app.Application
import nav.enro.annotations.NavigationComponent
import nav.enro.core.controller.NavigationApplication
import nav.enro.core.controller.NavigationController
import nav.enro.core.controller.navigationController
import nav.enro.core.plugins.EnroLogger
import nav.enro.result.EnroResult

@NavigationComponent
class ExampleApplication : Application(), NavigationApplication {
    override val navigationController = navigationController {
        withPlugin(EnroResult())
        withPlugin(EnroLogger())
    }
}