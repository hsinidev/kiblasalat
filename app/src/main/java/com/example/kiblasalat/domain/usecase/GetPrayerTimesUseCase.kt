package com.example.kiblasalat.domain.usecase

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import com.example.kiblasalat.domain.model.PrayerTimeItem
import com.example.kiblasalat.domain.model.SalatTimes
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

class GetPrayerTimesUseCase @Inject constructor() {

    operator fun invoke(
        latitude: Double,
        longitude: Double,
        calculationMethod: String,
        asrMadhab: String,
        date: Date = Date(),
        locationName: String = "Current Location",
        offsets: Map<String, Int> = emptyMap()
    ): SalatTimes {
        val coordinates = Coordinates(latitude, longitude)
        val calendar = Calendar.getInstance().apply { time = date }
        val dateComponents = DateComponents(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        val method = when (calculationMethod) {
            "MUSLIM_WORLD_LEAGUE" -> CalculationMethod.MUSLIM_WORLD_LEAGUE
            "ISNA" -> CalculationMethod.NORTH_AMERICA
            "EGYPT" -> CalculationMethod.EGYPTIAN
            "UMM_AL_QURA" -> CalculationMethod.UMM_AL_QURA
            "KARACHI" -> CalculationMethod.KARACHI
            "GULF" -> CalculationMethod.DUBAI
            "DUBAI" -> CalculationMethod.DUBAI
            "KUWAIT" -> CalculationMethod.KUWAIT
            "SINGAPORE" -> CalculationMethod.SINGAPORE
            "QATAR" -> CalculationMethod.QATAR
            "TURKEY" -> CalculationMethod.MUSLIM_WORLD_LEAGUE
            "MOROCCO" -> CalculationMethod.OTHER
            else -> CalculationMethod.MUSLIM_WORLD_LEAGUE
        }

        val params = method.parameters.apply {
            madhab = if (asrMadhab == "HANAFI") Madhab.HANAFI else Madhab.SHAFI
            if (calculationMethod == "MOROCCO") {
                fajrAngle = 19.0
                ishaAngle = 17.0
            }
        }

        val adhanPrayers = PrayerTimes(coordinates, dateComponents, params)

        fun Date.addMinutes(minutes: Int): Date {
            val cal = Calendar.getInstance()
            cal.time = this
            cal.add(Calendar.MINUTE, minutes)
            return cal.time
        }

        val fajr = adhanPrayers.fajr.addMinutes(offsets["Fajr"] ?: 0)
        val sunrise = adhanPrayers.sunrise.addMinutes(offsets["Sunrise"] ?: 0)
        val dhuhr = adhanPrayers.dhuhr.addMinutes(offsets["Dhuhr"] ?: 0)
        val asr = adhanPrayers.asr.addMinutes(offsets["Asr"] ?: 0)
        val maghrib = adhanPrayers.maghrib.addMinutes(offsets["Maghrib"] ?: 0)
        val isha = adhanPrayers.isha.addMinutes(offsets["Isha"] ?: 0)

        val prayersList = listOf(
            PrayerTimeItem("Fajr", fajr),
            PrayerTimeItem("Sunrise", sunrise),
            PrayerTimeItem("Dhuhr", dhuhr),
            PrayerTimeItem("Asr", asr),
            PrayerTimeItem("Maghrib", maghrib),
            PrayerTimeItem("Isha", isha)
        )

        val now = Date()
        var nextPrayerItem: PrayerTimeItem? = null
        
        if (now.before(fajr)) {
            nextPrayerItem = prayersList[0]
        } else if (now.before(sunrise)) {
            nextPrayerItem = prayersList[1]
        } else if (now.before(dhuhr)) {
            nextPrayerItem = prayersList[2]
        } else if (now.before(asr)) {
            nextPrayerItem = prayersList[3]
        } else if (now.before(maghrib)) {
            nextPrayerItem = prayersList[4]
        } else if (now.before(isha)) {
            nextPrayerItem = prayersList[5]
        }

        val finalNextPrayerName: String
        val finalNextPrayerTime: Date
        val countdownMillis: Long

        if (nextPrayerItem != null) {
            finalNextPrayerName = nextPrayerItem.name
            finalNextPrayerTime = nextPrayerItem.time
            countdownMillis = finalNextPrayerTime.time - now.time
        } else {
            finalNextPrayerName = "Fajr"
            val tomorrowCal = Calendar.getInstance().apply {
                time = date
                add(Calendar.DAY_OF_YEAR, 1)
            }
            val tomorrowComponents = DateComponents(
                tomorrowCal.get(Calendar.YEAR),
                tomorrowCal.get(Calendar.MONTH) + 1,
                tomorrowCal.get(Calendar.DAY_OF_MONTH)
            )
            val tomorrowAdhan = PrayerTimes(coordinates, tomorrowComponents, params)
            finalNextPrayerTime = tomorrowAdhan.fajr
            countdownMillis = finalNextPrayerTime.time - now.time
        }

        val mappedPrayers = prayersList.map { item ->
            item.copy(isNext = item.name == finalNextPrayerName && nextPrayerItem != null)
        }

        return SalatTimes(
            locationName = locationName,
            date = date,
            prayers = mappedPrayers,
            nextPrayerName = finalNextPrayerName,
            nextPrayerTime = finalNextPrayerTime,
            countdownMillis = if (countdownMillis > 0) countdownMillis else 0L
        )
    }
}
