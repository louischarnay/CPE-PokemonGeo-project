package fr.cpe.pokemon_geo.ui.screen.pokedex

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import fr.cpe.pokemon_geo.R
import fr.cpe.pokemon_geo.model.POKEMON_TYPE
import fr.cpe.pokemon_geo.model.Pokemon
import fr.cpe.pokemon_geo.ui.screen.pokemon_card.PokemonCard
import timber.log.Timber

@Composable
fun PokedexItem(pokemon: Pokemon) {
    val (showPokemonDetails, setShowPokemonDetails) = remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 15.dp, vertical = 2.dp)
            .clickable {
                Timber.d("Pokemon clicked: ${pokemon.name}")
                setShowPokemonDetails(true)
            }
    ) {
        PokemonImage(pokemon)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(pokemon.name)
            Row {
                PokemonType(pokemon.getType1())
                PokemonType(pokemon.getType2())
            }
        }
        Text("#${pokemon.order}")
    }
    if(showPokemonDetails) PokemonDetails(pokemon = pokemon , onClose = { setShowPokemonDetails(false) })
}

@Composable
fun PokemonImage(pokemon: Pokemon) {
    Image(
        painter = painterResource(id = pokemon.frontResource),
        contentDescription = null,
        modifier = Modifier
            .padding(5.dp)
            .size(60.dp)
    )
}

@Composable
fun PokemonType(type: POKEMON_TYPE?) {
    if (type == null) return
    Row {
        Image(
            painter = painterResource(id = R.drawable.feu),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Text(type.name)
    }
}

@Composable
fun PokemonDetails(pokemon: Pokemon, onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = {
            Button(onClick = onClose) {
                Text(text = "Close")
            }
        },
        text = {
            PokemonCard(pokemon)
        }
    )
}