import java.time.Instant
import java.time.zone.ZoneRulesException
import java.time.zone.ZoneRulesProvider
import java.util.*

/**
 * This program prints a CSV of all timezones and their offsets from UTC.
 * It also prints the previous and next DST change for each timezone.
 */
fun main(args: Array<String>) {
    // Get start date and end date from the arguments. The dates are in format YYYY-MM-DD
    val startDate = if (args.size > 0) args[0] else "2019-01-01"
    val endDate = if (args.size > 1) args[1] else "2020-01-01"

    // If not arguments are provided, print help and exit
    if (args.isEmpty()) {
        println("Usage: java -jar dst.jar <start date> <end date>")
        println("Example: java -jar dst.jar 2019-01-01 2020-01-01")
        return
    }

    var startDateAsInstant = Instant.parse("${startDate}T00:00:00Z")
    var endDateAsInstant = Instant.parse("${endDate}T00:00:00Z")

    // Get all timezones
    val timezones = TimeZone.getAvailableIDs()

    println("Timezone,Offset from UTC (min), Has DST, DST Offset from UTC (min), Offset before transition date, Transition date, Offset after transition date")

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

        if (!hasDst || rules == null) {
            // Print the variables as CSV
            println("$timezone,$offsetFromUtc,$hasDst,$dstOffset,,,,,")
            return@forEach
        }

        // for each date between start and end date, identify when the dst transition happens and print the offset before and after
        var date = startDateAsInstant
        while (date.isBefore(endDateAsInstant)) {
            val transition = rules.nextTransition(date)
            val offsetBefore = transition.offsetBefore.totalSeconds / 60
            val offsetAfter = transition.offsetAfter.totalSeconds / 60
            val transitionDate = transition.instant.toString()
            println("$timezone,$offsetFromUtc,$hasDst,$dstOffset,$offsetBefore,$transitionDate,$offsetAfter")
            date = transition.instant
        }
    }
}

fun offsetToMinutes(offset: Int) = offset / 1000 / 60