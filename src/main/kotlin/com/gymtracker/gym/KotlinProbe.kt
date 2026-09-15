package com.gymtracker.gym

import com.gymtracker.gym.workoutSessions.model.WorkoutSession

/**
 * TYMCZASOWY plik-sonda dla Etapu 1. Do skasowania po zielonym buildzie.
 *
 * Sprawdza trzy rzeczy naraz:
 *  1. kotlinc w ogole wstaje i kompiluje src/main/kotlin,
 *  2. Kotlin widzi klase z Javy (interop w kierunku Kotlin -> Java),
 *  3. Kotlin widzi getter WYGENEROWANY PRZEZ LOMBOKA (`getUserId()`), czyli plugin
 *     kompilatora `lombok` faktycznie dziala. Bez niego: "unresolved reference: userId".
 */
internal fun probeLombokVisibility(session: WorkoutSession): Long? = session.userId
