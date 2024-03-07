package fr.cpe.pokemon_geo.ui.screen.map

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.cpe.pokemon_geo.usecase.GetInterestPointUseCase
import fr.cpe.pokemon_geo.usecase.GetLocationUseCase
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
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
        collectCurrentLocation()
        collectInterestPoints()
    }

    private fun collectCurrentLocation() {
        viewModelScope.launch {
            getLocationUseCase.invoke().collect { location ->
                _currentLocation.value = location
            }
        }
    }

    private fun collectInterestPoints() {
        viewModelScope.launch {
            getInterestPointUseCase.invoke()?.collect { interestPoints ->
                //TODO: Display interest points on the map
            }
        }
    }
}
