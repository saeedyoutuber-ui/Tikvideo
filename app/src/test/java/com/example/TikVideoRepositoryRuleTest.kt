package com.example

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.FirestoreVideo
import com.example.data.TikVideoRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class TikVideoRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createVideo_authenticatedUser_succeedsAndReturnsId() {
        runBlocking {
            signInTestUser(ALICE_EMAIL)
            val repository = TikVideoRepository(firestore)

            val video = FirestoreVideo(
                caption = "Test TikVideo Reel 🚀",
                soundTitle = "Audio 1",
                creatorName = "Alice",
                creatorHandle = "@alice",
                category = "FOR_YOU"
            )

            val result = withTimeout(DEFAULT_TIMEOUT_MS) {
                repository.createVideo(video)
            }
            assertTrue(result.isSuccess)
            val videoId = result.getOrNull()
            assertNotNull(videoId)
            assertTrue(videoId!!.isNotEmpty())
        }
    }

    @Test
    fun observeVideos_unauthenticatedUser_failsWithPermissionDenied() {
        runBlocking {
            auth.signOut()
            val repository = TikVideoRepository(firestore)

            try {
                withTimeout(FLOW_TIMEOUT_MS) {
                    repository.observeVideos().first()
                }
                fail("Expected snapshot listener to throw on unauthenticated access")
            } catch (e: Exception) {
                val firestoreException = e.unwrapFirestoreException()
                assertNotNull("Expected nested FirebaseFirestoreException, got $e", firestoreException)
                assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException?.code)
            }
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice@tikvideo.test"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
