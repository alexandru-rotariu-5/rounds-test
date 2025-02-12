package ai.rounds.speedmeter.repo

import ai.rounds.speedmeter.db.access.SessionAccess
import ai.rounds.speedmeter.models.Session
import android.content.Context


/**
 * Class dedicated to the speed tracking
 */
object TrackerRepo {
    /**
     * List of all recorded distances during session.
     */
    private val distanceRecords = mutableListOf<Float>()

    /**
     * List of all recorded speeds during session.
     */
    private val speedRecords = mutableListOf<Float>()

    /**
     * Current tracking session
     */
    private var session: Session? = null

    /**
     * Database access for tracking session
     */
    private var sessionAccess: SessionAccess? = null

    /**
     * Creates a new session
     *
     * @param context Context used to instantiate the database access
     */
    @JvmStatic
    fun initializeSession(context: Context?) {
        if (sessionAccess == null) {
            // Instantiating the database access
            sessionAccess = SessionAccess(context)
        }

        if (session != null) {
            // If a session has been started but not ended, we finalize it.
            finalizeSession()
        }

        // Creating new session
        session = Session(System.currentTimeMillis())
    }

    /**
     * Determines whether a session has already been started or not
     *
     * @return True if a session is in progress
     */
    @JvmStatic
    val isInitialized: Boolean
        get() = session != null

    /**
     * Adds a speed to the recorded speeds for the session
     *
     * @param speed Speed to add to records
     */
    @JvmStatic
    fun addSpeed(speed: Float) {
        speedRecords.add(speed)
    }

    /**
     * Calculates the average speed of the session in progress
     *
     * @return The average speed for the current session
     */
    private val averageSpeed: Float
        get() = speedRecords.average().toFloat()

    /**
     * Adds a distance to the recorded distances for the session
     *
     * @param distance Distance to add to records
     */
    @JvmStatic
    fun addDistance(distance: Float) {
        distanceRecords.add(distance)
    }

    /**
     * Calculates the total distance traveled during the session
     *
     * @return The total distance traveled
     */
    private val totalDistance: Float
        get() = distanceRecords.sum()

    /**
     * Gets the id of the tracking session in progress
     *
     * @return The id of the current tracking session
     */
    @JvmStatic
    val currentSessionId: String?
        get() = session?.id

    /**
     * Ends the tracking session in progress and saves it to the database
     */
    @JvmStatic
    fun finalizeSession() {
        // Finalizing the session in progress
        val endTime = System.currentTimeMillis()
        session?.apply {
            this.endTime = endTime
            this.averageSpeed = TrackerRepo.averageSpeed
            this.distance = totalDistance
            // Writing the session to the database
            sessionAccess?.let {
                it.openToWrite()
                it.saveTrackingSession(session)
                it.close()
            }
        }

        // Resetting the session fields
        session = null
        speedRecords.clear()
        distanceRecords.clear()
    }
}
