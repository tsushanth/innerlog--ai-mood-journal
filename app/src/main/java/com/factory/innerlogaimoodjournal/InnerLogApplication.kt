package com.factory.innerlogaimoodjournal

import android.app.Application
import com.factory.innerlogaimoodjournal.billing.BillingManager
import com.factory.innerlogaimoodjournal.billing.PremiumManager
import com.factory.innerlogaimoodjournal.data.local.AppDatabase
import com.factory.innerlogaimoodjournal.data.repository.GoalRepository
import com.factory.innerlogaimoodjournal.data.repository.HabitRepository
import com.factory.innerlogaimoodjournal.data.repository.JournalRepository
import com.factory.innerlogaimoodjournal.data.repository.MoodRepository
import com.factory.innerlogaimoodjournal.security.LockManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class InnerLogApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    val journalRepository: JournalRepository by lazy { JournalRepository(database.journalDao()) }
    val habitRepository: HabitRepository by lazy { HabitRepository(database.habitDao()) }
    val goalRepository: GoalRepository by lazy { GoalRepository(database.goalDao()) }
    val moodRepository: MoodRepository by lazy { MoodRepository(database.moodDao()) }
    val lockManager: LockManager by lazy { LockManager(this) }

    val premiumManager: PremiumManager by lazy { PremiumManager(this) }
    val billingManager: BillingManager by lazy { BillingManager(this) }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            billingManager.purchases.collect { purchases ->
                premiumManager.updateFromPurchases(purchases)
            }
        }
        billingManager.startConnection()
    }
}
