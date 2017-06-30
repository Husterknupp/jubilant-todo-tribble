package de.husterknupp.todoapp

import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class ScannedBranchRepositoryTest {
    val testRepoFile = File("./test-scanned-branches")

    @Before
    fun removeBefore() {
        if (testRepoFile.exists()) {
            testRepoFile.delete()
        }
    }

    @After
    fun removeAfterLast() {
        if (testRepoFile.exists()) {
            testRepoFile.delete()
        }
    }

    @Test
    fun testSave() {
        val scannedBranch = ScannedBranch("123", "master", 987L)
        ScannedBranchRepository("./test-scanned-branches").saveScannedBranch(scannedBranch)

        assertTrue { testRepoFile.readText().contains("123") }
        assertTrue { testRepoFile.readText().contains("master") }
        assertTrue { testRepoFile.readText().contains("987") }
        assertTrue { !testRepoFile.readText().contains("987L") }
    }

    infix fun String.noOfOccurrences(subStr: String): Int {
        return split(subStr).size - 1
    }

    @Test
    fun testSaveNotTwiceTheSame() {
        var scannedBranch = ScannedBranch("123", "master", 987L)
        ScannedBranchRepository("./test-scanned-branches").saveScannedBranch(scannedBranch)
        assertEquals( testRepoFile.readText() noOfOccurrences "123",  1)

        scannedBranch = ScannedBranch("123", "master", 987L)
        ScannedBranchRepository("./test-scanned-branches").saveScannedBranch(scannedBranch)
        assertEquals( testRepoFile.readText() noOfOccurrences "123",  1)
    }

    @Test
    fun testSaveSavesDifferentObjectsAsSeparateEntities() {
        testRepoFile
                .writeText("[{\"repoId\":\"123\", \"branchName\": \"master\", \"timestampUtcScannedLast\": 987}]")
        val scannedBranch = ScannedBranch("456", "master", 999L)
        ScannedBranchRepository("./test-scanned-branches").saveScannedBranch(scannedBranch)

        assertTrue { testRepoFile.readText().contains("123") }
        assertTrue { testRepoFile.readText().contains("456") }
    }

    @Test
    fun testFindAllScannedBranches() {
        testRepoFile
                .writeText("[{\"repoId\":\"123\", \"branchName\": \"master\", \"timestampUtcScannedLast\": 987}" +
                        ",{\"repoId\":\"456\", \"branchName\": \"develop\", \"timestampUtcScannedLast\": 999}]")
        val savedBranches = ScannedBranchRepository("./test-scanned-branches").findAllScannedBranches()

        assertEquals(savedBranches.size, 2)
        assertEquals( savedBranches.count { (repoId) -> repoId == "123" }, 1)
        assertEquals( savedBranches.count { (repoId) -> repoId == "456" }, 1)
    }
}
