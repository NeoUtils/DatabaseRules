package com.neo.fbrules.main.di

import com.neo.fbrules.main.domain.useCase.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
sealed class DomainModule {

    @Binds
    abstract fun bindGetRules(
        getRulesImpl: GetRulesImpl
    ): GetRules

    @Binds
   abstract fun bindSetRules(
        setRulesImpl: SetRulesImpl
    ): SetRules

}
