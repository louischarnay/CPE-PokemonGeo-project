package fr.cpe.pokemon_geo.ui.screen.map

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cpe.pokemon_geo.usecase.GetInterestPointUseCase
import fr.cpe.pokemon_geo.usecase.GetLocationUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class OsmdroidMapViewModel @Inject constructor(
    private val getLocationUseCase: GetLocationUseCase,
    private val getInterestPointUseCase: GetInterestPointUseCase
) : ViewModel() {

    val lyon = GeoPoint(45.7578137, 4.8320114)

    private val _currentLocation: MutableState<GeoPoint?> = mutableStateOf(null)
    val currentLocation: State<GeoPoint?> = _currentLocation

    init {
        fetchCurrentLocationPeriodically()
    }

    private fun fetchCurrentLocationPeriodically() {
        viewModelScope.launch {
            while (true) {
                getCurrentLocation()
                getInterestPoints()
                delay(5_000)
            }
        }
    }

    private suspend fun getCurrentLocation() {
        getLocationUseCase.invoke().collect { location ->
            _currentLocation.value = location
        }
    }

    private suspend fun getInterestPoints() {
        getInterestPointUseCase.run()?.collect { interestPoints ->
            //TODO: Display interest points on the map
        }
    }
}
