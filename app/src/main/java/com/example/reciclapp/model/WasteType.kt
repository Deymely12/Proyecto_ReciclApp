package com.example.reciclapp.model

enum class WasteType(
    val displayName: String,
    val binDocId: String
) {
    ORGANICO("Orgánico", "organicos"),
    VIDRIO("Vidrio", "vidrio"),
    PLASTICOS_ENVASES("Plásticos y envases metálicos", "plastico"),
    PAPEL_CARTON("Papel y cartón", "papel_carton"),
    OTROS_RESIDUOS("Otros residuos", "no_reciclables");

    companion object {
        fun fromDisplayName(name: String): WasteType? =
            values().firstOrNull { it.displayName == name }
    }
}
