package fr.cpe.pokemon_geo.model.user_pokemon

import fr.cpe.pokemon_geo.model.pokemon.Pokemon

class UserPokemon(
    order : Int,
    name : String,
    imageName : String,
    type1 : String,
    type2 : String?,
    private val healPoint : Int,
    private val healPointLoss : Int?,
    private val attack : Int
) : Pokemon(
    order = order,
    name = name,
    isUnknownPokemon = false,
    imageName = imageName,
    type1 = type1,
    type2 = type2
) {
    fun getHealPoint(): Int {
        return healPoint
    }

    fun getHealPointLoss(): Int {
        return healPointLoss ?: 0
    }

    fun getAttack(): Int {
        return attack
    }
}