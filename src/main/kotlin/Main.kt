import java.time.Instant
import java.time.zone.ZoneRulesException
import java.time.zone.ZoneRulesProvider
import java.util.*

/**
 * This program prints a CSV of all timezones and their offsets from UTC.
 * It also prints the previous and next DST change for each timezone.
 */
fun main(args: Array<String>) {
    // Get all timezones
    val timezones = TimeZone.getAvailableIDs()

    // Print the header
    println("Timezone,Offset from UTC (min),Has DST,DST Offset from UTC (min),Previous offset from UTC, Previous DST Change,Current offset from UTC, Next DST Change, Next offset from UTC, Second Next DST Change, Second Next offset from UTC")

    // For each timezone, print the timezone ID and the offset from UTC
    timezones.forEach { timezone ->

        val rules = try {
            ZoneRulesProvider.getRules(timezone, false)
        } catch (e: ZoneRulesException) {
            null
        }
        val tz = TimeZone.getTimeZone(timezone)
        val offsetFromUtc = offsetToMinutes(tz.rawOffset)
        val hasDst = tz.useDaylightTime()
        val dstOffset = offsetFromUtc + offsetToMinutes(tz.dstSavings)
        val previousDstChange: String;
        val nextDstChange: String;
        val secondNextDstChange: String;
        var (previousOffset, currentOffset, nextOffset, secondNextOffset) = arrayOf(
            offsetFromUtc,
            offsetFromUtc,
            offsetFromUtc,
            offsetFromUtc
        )
        if (hasDst && rules != null) {
            val previousTransition = rules.previousTransition(Instant.now())
            val nextTransition = rules.nextTransition(Instant.now())
            val secondNextTransition = rules.nextTransition(nextTransition.instant)
            previousOffset = previousTransition.offsetBefore.totalSeconds / 60
            currentOffset = previousTransition.offsetAfter.totalSeconds / 60
            nextOffset = nextTransition.offsetAfter.totalSeconds / 60
            secondNextOffset = secondNextTransition.offsetAfter.totalSeconds / 60
            previousDstChange = rules.previousTransition(Instant.now())
                .instant.toString()
            nextDstChange = rules.nextTransition(Instant.now())
                .instant.toString()
            secondNextDstChange = secondNextTransition.instant.toString()

        } else {
            previousDstChange = ""
            nextDstChange = ""
            secondNextDstChange = ""
        }

        // Print the variables as CSV
        println("$timezone,$offsetFromUtc,$hasDst,$dstOffset,$previousOffset,$previousDstChange,$currentOffset,$nextDstChange,$nextOffset,$secondNextDstChange,$secondNextOffset")
    }
}

fun offsetToMinutes(offset: Int) = offset / 1000 / 60