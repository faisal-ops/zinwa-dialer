package com.zinwa.dialer

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.telecom.Call
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton bridge between [com.zinwa.dialer.service.MyInCallService]
 * (which owns the live [Call] objects from Telecom) and [InCallActivity].
 *
 * Supports up to two simultaneous calls:
 *   - [info]       : foreground (active / ringing) call — drives the primary UI.
 *   - [secondCall] : background / held call — shown as a compact banner.
 *
 * Thread-safe: MutableStateFlow updates are always safe from any thread.
 */
data class ActiveCallInfo(
    val call: Call,
    val state: Int,
    val displayName: String,
    val number: String,
    val photoUri: String? = null
)

object CallStateHolder {

    private val _info       = MutableStateFlow<ActiveCallInfo?>(null)
    private val _secondCall = MutableStateFlow<ActiveCallInfo?>(null)

    /** Foreground / primary call. */
    val info: StateFlow<ActiveCallInfo?> = _info.asStateFlow()

    /** Background / held call (non-null when there are two simultaneous calls). */
    val secondCall: StateFlow<ActiveCallInfo?> = _secondCall.asStateFlow()

    // ── Update / remove ──────────────────────────────────────────────────────

    /**
     * Called by [MyInCallService] whenever call state or details change.
     * Pass a non-null [context] on the first call so a photo lookup can be performed;
     * subsequent updates reuse the already-resolved photo.
     */
    fun update(call: Call, context: Context? = null) {
        val number = call.details.handle?.schemeSpecificPart.orEmpty()
        val name = listOfNotNull(
            call.details.callerDisplayName?.takeIf { it.isNotBlank() },
            call.details.contactDisplayName?.takeIf { it.isNotBlank() },
            number.takeIf { it.isNotBlank() }
        ).firstOrNull() ?: "Unknown"

        val primary   = _info.value
        val secondary = _secondCall.value

        when {
            // Update existing primary call in-place, reusing photo.
            primary?.call === call -> {
                val refreshedPhoto = when {
                    primary.photoUri != null -> primary.photoUri
                    context != null          -> lookupPhoto(context, number)
                    else                     -> null
                }
                _info.value = primary.copy(
                    state       = call.state,
                    displayName = name,
                    photoUri    = refreshedPhoto
                )
            }
            // Update existing secondary call in-place.
            secondary?.call === call -> {
                val refreshedPhoto = when {
                    secondary.photoUri != null -> secondary.photoUri
                    context != null            -> lookupPhoto(context, number)
                    else                       -> null
                }
                _secondCall.value = secondary.copy(
                    state       = call.state,
                    displayName = name,
                    photoUri    = refreshedPhoto
                )
            }
            // New call — no primary yet.
            primary == null -> {
                val photo = if (context != null) lookupPhoto(context, number) else null
                _info.value = ActiveCallInfo(call, call.state, name, number, photo)
            }
            // Second new call — slot it as secondary.
            secondary == null -> {
                val photo = if (context != null) lookupPhoto(context, number) else null
                _secondCall.value = ActiveCallInfo(call, call.state, name, number, photo)
            }
            // Third call — replace primary (edge case; most carriers don't support 3-way add).
            else -> {
                val photo = if (context != null) lookupPhoto(context, number) else null
                _info.value = ActiveCallInfo(call, call.state, name, number, photo)
            }
        }
    }

    /** Remove a specific call (called from [MyInCallService.onCallRemoved]). */
    fun remove(call: Call) {
        when {
            _info.value?.call === call -> {
                // Promote secondary to primary when foreground call ends.
                _info.value       = _secondCall.value
                _secondCall.value = null
            }
            _secondCall.value?.call === call -> {
                _secondCall.value = null
            }
        }
    }

    fun clear() {
        _info.value       = null
        _secondCall.value = null
    }

    // ── Multi-call actions ───────────────────────────────────────────────────

    /**
     * Swap foreground and background calls.
     * Puts the current primary on hold and resumes the secondary.
     */
    fun swap() {
        val p = _info.value ?: return
        val s = _secondCall.value ?: return
        p.call.hold()
        s.call.unhold()
        _info.value       = s
        _secondCall.value = p
    }

    // ── Primary call actions ─────────────────────────────────────────────────

    fun answer()  { _info.value?.call?.answer(0 /* VideoProfile.STATE_AUDIO_ONLY */) }
    fun reject()  { _info.value?.call?.reject(false, null) }
    fun hangup()  { _info.value?.call?.disconnect() }
    fun hold()    { _info.value?.call?.hold() }
    fun unhold()  { _info.value?.call?.unhold() }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun lookupPhoto(context: Context, number: String): String? {
        if (number.isBlank()) return null
        return try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number)
            )
            context.contentResolver.query(
                uri,
                arrayOf(
                    ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI,
                    ContactsContract.PhoneLookup.PHOTO_URI
                ),
                null, null, null
            )?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) ?: cursor.getString(1) else null
            }
        } catch (_: Exception) { null }
    }
}
