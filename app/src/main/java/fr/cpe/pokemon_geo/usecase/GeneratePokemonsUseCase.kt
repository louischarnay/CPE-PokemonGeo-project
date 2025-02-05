package fr.cpe.pokemon_geo.usecase

import fr.cpe.pokemon_geo.database.PokemonGeoRepository
import fr.cpe.pokemon_geo.database.generated_pokemon.GeneratedPokemonEntity
import fr.cpe.pokemon_geo.model.pokemon.Pokemon
import fr.cpe.pokemon_geo.utils.ONE_MINUTE_IN_MILLIS
import fr.cpe.pokemon_geo.utils.ONE_SECOND_IN_MILLIS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import timber.log.Timber
import javax.inject.Inject

class GeneratePokemonsUseCase @Inject constructor(
    private val repository: PokemonGeoRepository,
    private val getLocationUseCase: GetLocationUseCase
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var isRunning = true

    companion object {
        private const val UPDATE_DELAY = 5 * ONE_SECOND_IN_MILLIS
        private const val MAX_PER_AREA = 6
        private const val BASE_GENERATION_CHANCE = 0.5
        private const val AREA_RADIUS_IN_METERS = 120.0
        private const val GENERATION_COORDINATE_MULTIPLIER = 0.001 // 111 meters
        private const val MAX_DISTANCE_COORDINATE_BETWEEN_POKEMON = 0.0002 // 22 meters
        private const val DISAPPEARANCE_DELAY = 5 * ONE_MINUTE_IN_MILLIS
    }

    fun start() {
        isRunning = true
    }

    fun stop() {
        isRunning = false
    }

    fun invoke(pokemons: List<Pokemon>): Flow<List<GeneratedPokemonEntity>> = callbackFlow {
        coroutineScope.launch {
            while (true) {
                if (!isRunning) break

                val location = getLocationUseCase.getCurrentLocation()

                val generatedPokemons = repository.getAllGeneratedPokemon()
                handlePokemonRemoval(generatedPokemons, location)

                if (location != null) {
                    handlePokemonGeneration(pokemons, generatedPokemons, location)
                }

                trySend(repository.getAllGeneratedPokemon())

                delay(UPDATE_DELAY)
            }
        }

        awaitClose {
            coroutineScope.coroutineContext.cancel()
        }
    }

    private suspend fun handlePokemonGeneration(
        pokemons: List<Pokemon>,
        generatedPokemons: List<GeneratedPokemonEntity>,
        userLocation: GeoPoint
    ) {
        val generatedPokemonsCount = generatedPokemons.size
        if (generatedPokemonsCount < MAX_PER_AREA) {

            val random = Math.random()
            if (random < (BASE_GENERATION_CHANCE / (generatedPokemonsCount * 2 + 1))) {
                val pokemon = pokemons.random()

                val pokemonLocation = generatePokemonLocation(generatedPokemons, userLocation)
                val generatedPokemon = GeneratedPokemonEntity(
                    pokemonOrder = pokemon.getOrder(),
                    level = 1,
                    hpMax = 100,
                    attack = 5,
                    latitude = pokemonLocation.latitude,
                    longitude = pokemonLocation.longitude,
                )

                repository.insertGeneratedPokemon(generatedPokemon)
                Timber.d("Pokemon generated: ${pokemon.getOrder()}-${pokemon.getName()}")
            }
        }
    }

    private fun generatePokemonLocation(generatedPokemons: List<GeneratedPokemonEntity>, userLocation: GeoPoint): GeoPoint {
        var location: GeoPoint

        do {
            val newLatitude = userLocation.latitude + (Math.random() * 2 - 1) * GENERATION_COORDINATE_MULTIPLIER
            val newLongitude = userLocation.longitude + (Math.random() * 2 - 1) * GENERATION_COORDINATE_MULTIPLIER
            location = GeoPoint(newLatitude, newLongitude)
        } while (isTooCloseToExistingPokemons(generatedPokemons, location))

        return location
    }

    private fun isTooCloseToExistingPokemons(
        generatedPokemons: List<GeneratedPokemonEntity>,
        newLocation : GeoPoint,
    ): Boolean {
        generatedPokemons.forEach { pokemon ->
            val pokemonLocation = GeoPoint(pokemon.latitude, pokemon.longitude)
            val distance = newLocation.distanceToAsDouble(pokemonLocation)

            if (distance < MAX_DISTANCE_COORDINATE_BETWEEN_POKEMON) {
                return true
            }
        }
        return false
    }

    private suspend fun handlePokemonRemoval(
        generatedPokemons: List<GeneratedPokemonEntity>,
        location: GeoPoint?
    ) {
        generatedPokemons.forEach { pokemon ->
            val pokemonLocation = GeoPoint(pokemon.latitude, pokemon.longitude)
            val distance = location?.distanceToAsDouble(pokemonLocation)

            if (distance !== null && distance > AREA_RADIUS_IN_METERS) {
                repository.removeGeneratedPokemon(pokemon.id ?: 0)
                Timber.d("Pokemon removed(distance): ${pokemon.pokemonOrder}")
            } else {
                val currentTime = System.currentTimeMillis()
                if (currentTime - pokemon.createdAt > DISAPPEARANCE_DELAY) {
                    repository.removeGeneratedPokemon(pokemon.id ?: 0)
                    Timber.d("Pokemon removed(timer): ${pokemon.pokemonOrder}")
                }
            }
        }
    }

}