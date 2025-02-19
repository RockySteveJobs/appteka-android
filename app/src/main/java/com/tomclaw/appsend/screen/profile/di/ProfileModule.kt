package com.tomclaw.appsend.screen.profile.di

import android.content.Context
import android.os.Bundle
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleAdapterPresenter
import com.avito.konveyor.blueprint.ItemBlueprint
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.di.DATE_FORMATTER
import com.tomclaw.appsend.di.TIME_FORMATTER
import com.tomclaw.appsend.screen.profile.ProfileConverter
import com.tomclaw.appsend.screen.profile.ProfileConverterImpl
import com.tomclaw.appsend.screen.profile.ProfileInteractor
import com.tomclaw.appsend.screen.profile.ProfileInteractorImpl
import com.tomclaw.appsend.screen.profile.ProfilePresenter
import com.tomclaw.appsend.screen.profile.ProfilePresenterImpl
import com.tomclaw.appsend.screen.profile.adapter.header.HeaderItemBlueprint
import com.tomclaw.appsend.screen.profile.adapter.header.HeaderItemPresenter
import com.tomclaw.appsend.screen.profile.adapter.header.HeaderResourceProvider
import com.tomclaw.appsend.screen.profile.adapter.header.HeaderResourceProviderImpl
import com.tomclaw.appsend.screen.profile.adapter.uploads.UploadsItemBlueprint
import com.tomclaw.appsend.screen.profile.adapter.uploads.UploadsItemPresenter
import com.tomclaw.appsend.util.PerFragment
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import java.text.DateFormat
import java.util.Locale
import javax.inject.Named

@Module
class ProfileModule(
    private val userId: Int,
    private val context: Context,
    private val state: Bundle?
) {

    @Provides
    @PerFragment
    internal fun providePresenter(
        interactor: ProfileInteractor,
        converter: ProfileConverter,
        adapterPresenter: Lazy<AdapterPresenter>,
        schedulers: SchedulersFactory
    ): ProfilePresenter = ProfilePresenterImpl(
        userId,
        interactor,
        converter,
        adapterPresenter,
        schedulers,
        state
    )

    @Provides
    @PerFragment
    internal fun provideInteractor(
        api: StoreApi,
        schedulers: SchedulersFactory
    ): ProfileInteractor = ProfileInteractorImpl(api, schedulers)

    @Provides
    @PerFragment
    internal fun provideConverter(): ProfileConverter = ProfileConverterImpl()

    @Provides
    @PerFragment
    internal fun provideAdapterPresenter(binder: ItemBinder): AdapterPresenter {
        return SimpleAdapterPresenter(binder, binder)
    }

    @Provides
    @PerFragment
    internal fun provideItemBinder(
        blueprintSet: Set<@JvmSuppressWildcards ItemBlueprint<*, *>>
    ): ItemBinder {
        return ItemBinder.Builder().apply {
            blueprintSet.forEach { registerItem(it) }
        }.build()
    }

    @Provides
    @IntoSet
    @PerFragment
    internal fun provideHeaderItemBlueprint(
        presenter: HeaderItemPresenter
    ): ItemBlueprint<*, *> = HeaderItemBlueprint(presenter)

    @Provides
    @PerFragment
    internal fun provideHeaderResourceProvider(
        context: Context,
        @Named(TIME_FORMATTER) timeFormatter: DateFormat,
        @Named(DATE_FORMATTER) dateFormatter: DateFormat,
    ): HeaderResourceProvider = HeaderResourceProviderImpl(
        context,
        timeFormatter,
        dateFormatter
    )

    @Provides
    @PerFragment
    internal fun provideHeaderItemPresenter(
        presenter: ProfilePresenter,
        resourceProvider: HeaderResourceProvider,
        locale: Locale,
    ) = HeaderItemPresenter(presenter, resourceProvider, locale)

    @Provides
    @IntoSet
    @PerFragment
    internal fun provideUploadsItemBlueprint(
        presenter: UploadsItemPresenter
    ): ItemBlueprint<*, *> = UploadsItemBlueprint(presenter)

    @Provides
    @PerFragment
    internal fun provideUploadsItemPresenter(
        presenter: ProfilePresenter,
    ) = UploadsItemPresenter(presenter)

}
