package com.example.pruebaclasificador.data

object ClassMapping {
    val mapping = mapOf(
        // 1. Orgánico
        "BIODEGRADABLE" to "Orgánico",
        "potato" to "Orgánico",
        "Damaged potato" to "Orgánico",
        "Defected potato" to "Orgánico",
        "Diseased/fungal potato" to "Orgánico",
        "Sprouted potato" to "Orgánico",
        "carrot" to "Orgánico",
        "half carrot" to "Orgánico",
        "slice carrot" to "Orgánico",
        "onion" to "Orgánico",
        "egg" to "Orgánico",

        // 2. Vidrio
        "GLASS" to "Vidrio",

        // 3. Plásticos y envases metálicos
        "PLASTIC" to "Plásticos y envases metálicos",
        "plastic" to "Plásticos y envases metálicos",
        "bottle" to "Plásticos y envases metálicos",
        "petbottle" to "Plásticos y envases metálicos",
        "METAL" to "Plásticos y envases metálicos",
        "can" to "Plásticos y envases metálicos",
        "mask" to "Plásticos y envases metálicos",

        // 4. Papel y cartón
        "PAPER" to "Papel y cartón",
        "CARDBOARD" to "Papel y cartón",

        // 5. Otros residuos
        "Battery:Hazardous" to "Otros residuos",
        "coincell" to "Otros residuos",
        "drycell" to "Otros residuos",
        "Bud" to "Otros residuos",
        "stone" to "Otros residuos",
        "ilac" to "Otros residuos",
        "Panadol - 500mg" to "Otros residuos",
        "Valium - 10mg" to "Otros residuos",
        "Adalat - 30mg" to "Otros residuos",
        "Xanax - 2mg" to "Otros residuos"
    )
}