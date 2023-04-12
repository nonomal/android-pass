package proton.android.pass.data.impl.usecases

import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.PaymentManager
import me.proton.core.payment.domain.usecase.GetCurrentSubscription
import me.proton.core.plan.domain.entity.Plan
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class GetUserPlanImpl @Inject constructor(
    private val paymentManager: PaymentManager,
    private val getSubscription: GetCurrentSubscription
) : GetUserPlan {

    @Suppress("ReturnCount")
    override suspend fun invoke(userId: UserId): String {
        val plan = getPlan(userId)
        return plan?.title ?: FREE_PLAN_NAME
    }

    private suspend fun getPlan(userId: UserId): Plan? {

        val hasSubscription = paymentManager.isSubscriptionAvailable(userId)
        if (!hasSubscription) {
            return null
        }

        // If user does not have a subscription it means they are in the free plan
        val subscription = getSubscription.invoke(userId)
        if (subscription == null) {
            PassLogger.w(TAG, "PaymentManager has returned hasSubscription but is null")
            return null
        }
        return subscription.plans.firstOrNull { it.type == ACTIVE_PLAN_TYPE }
    }

    companion object {
        private const val FREE_PLAN_NAME = "Proton Free"
        private const val TAG = "GetUserPlanImpl"
        private const val ACTIVE_PLAN_TYPE = 1
    }
}
