package ph.perfumedex

data class Scent(val id: String, val name: String, val house: String, val notes: String, val story: String, val color: Long)
data class Wear(val id: String, val scent: String, val occasion: String, val note: String, val format: String, val date: String, val shared: Boolean)
val occasions = listOf("Hot commute", "Office", "Date night", "Rainy day")
val catalog = listOf(
    Scent("001", "Sampaguita Mist", "Isla Studio", "Sampaguita · Green tea · White musk", "A soft white floral with the cool lift of freshly steeped tea.", 0xFFDCE6C8),
    Scent("002", "Calamansi Sunday", "Bahay Olfactory", "Calamansi · Basil · Cedar", "Bright citrus peel and garden herbs, settling into airy woods.", 0xFFF2DEA0),
    Scent("003", "After Amihan", "Isla Studio", "Sea salt · Pandan · Vetiver", "A coastal breeze over green leaves and dry, earthy roots.", 0xFFC4DFDE),
    Scent("004", "Ube at Dusk", "Likha Lab", "Ube · Vanilla · Sandalwood", "A gentle purple gourmand with creamy woods and a quiet sweetness.", 0xFFDED1EA),
    Scent("005", "Monsoon Garden", "Likha Lab", "Ginger · Rain accord · Moss", "Damp garden paths, fresh ginger, and the hush after afternoon rain.", 0xFFCCD9CD),
    Scent("006", "Ylang Moon", "Bahay Olfactory", "Ylang-ylang · Cacao · Amber", "Golden tropical petals with a warm, bittersweet evening finish.", 0xFFEBC9AE)
)
object VouchRules {
    fun key(scent: String, occasion: String) = "$scent|$occasion"
    fun eligible(wears: List<Wear>, scent: String) = wears.any { it.scent == scent }
}
