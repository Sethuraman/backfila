package app.cash.backfila.client.s3

import app.cash.backfila.client.BackfillConfig
import app.cash.backfila.client.PrepareBackfillConfig
import app.cash.backfila.client.s3.record.RecordStrategy
import app.cash.backfila.client.s3.record.Utf8StringNewlineStrategy
import app.cash.backfila.client.s3.shim.FakeS3Service
import app.cash.backfila.embedded.Backfila
import app.cash.backfila.embedded.createWetRun
import com.squareup.wire.internal.newMutableList
import javax.inject.Inject
import misk.testing.MiskTest
import misk.testing.MiskTestModule
import okio.ByteString.Companion.encodeUtf8
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@MiskTest(startService = true)
class S3Utf8StringNewlineBackfillTest {
  @Suppress("unused")
  @MiskTestModule
  val module = TestingModule()

  @Inject lateinit var backfila: Backfila

  @Inject lateinit var fakeS3: FakeS3Service

  @BeforeEach
  fun `loadFiles`() {
    fakeS3.loadResourceDirectory("file-structure")
  }

  @Test
  fun `backfilling breakfast`() {
    val run = backfila.createWetRun<BreakfastBackfill>(parameters = RecipeAttributes())
    run.execute()

    assertThat(run.backfill.backfilledIngredients.size).isEqualTo(7)
  }

  @Test
  fun `backfilling eggs for breakfast`() {
    val run = backfila.createWetRun<BreakfastBackfill>(parameters = RecipeAttributes(ingredientRegex = "egg"))
    run.execute()

    assertThat(run.backfill.backfilledIngredients.map { it.first })
      .containsExactlyInAnyOrder("main-egg-and-bacon", "main-egg-on-toast")
  }

  @Test
  fun `validation failures`() {
    // Load too many files
    for (i in 1..101) {
      fakeS3.add("1001-recipies", "breakfast/file-$i", "content-$i".encodeUtf8())
    }

    // Load a long key
    val longFileName = "lo${"n".repeat(293)}g-file"
    fakeS3.add("war-and-recipies", "breakfast/$longFileName", "short content".encodeUtf8())

    with(SoftAssertions()) {
      this.assertThatCode {
        backfila.createWetRun<BreakfastBackfill>(
          parameterData = mapOf("validate" to "false".encodeUtf8()),
        )
        fail("validate must fail")
      }.hasMessageContaining("Validate failed")

      this.assertThatCode {
        backfila.createWetRun<BreakfastBackfill>(rangeStart = "a start")
        fail("invalid range must fail")
      }.hasMessageContaining("Range is currently unsupported")

      this.assertThatCode {
        backfila.createWetRun<BreakfastBackfill>(rangeEnd = "a end")
        fail("invalid range must fail")
      }.hasMessageContaining("Range is currently unsupported")

      this.assertThatCode {
        backfila.createWetRun<BrunchBackfill>()
        fail("no files must fail")
      }.hasMessageContaining("No files found for bucket")

      this.assertThatCode {
        backfila.createWetRun<BreakfastBackfill>(RecipeAttributes(cookbook = "1001-recipies"))
        fail("too many files must fail")
      }.hasMessageContaining("which is more than 100 files")
        .hasMessageContaining("breakfast/file-1, breakfast/file-2, breakfast/file-3")

      this.assertThatCode {
        backfila.createWetRun<BreakfastBackfill>(RecipeAttributes(cookbook = "war-and-recipies"))
        fail("long postfix must fail")
      }.hasMessageContaining("Found invalid postfixes")
        .hasMessageContaining(longFileName)
      this.assertThat(longFileName.encodeUtf8().size).isEqualTo(301)

      this.assertAll()
    }
  }

  abstract class RecipiesBackfill : S3DatasourceBackfill<String, RecipeAttributes>() {
    val backfilledIngredients = newMutableList<Pair<String, String>>()

    override fun runOne(item: String, config: BackfillConfig<RecipeAttributes>) {
      if (item.contains(config.parameters.ingredientRegex)) {
        backfilledIngredients.add(config.partitionName to item)
      }
    }

    override fun validate(config: PrepareBackfillConfig<RecipeAttributes>) {
      check(config.parameters.validate) { "Validate failed" }
    }

    override fun getBucket(config: PrepareBackfillConfig<RecipeAttributes>) =
      config.parameters.cookbook

    override fun getPrefix(config: PrepareBackfillConfig<RecipeAttributes>) =
      staticPrefix + "/" + config.parameters.course

    override val recordStrategy: RecordStrategy<String> = Utf8StringNewlineStrategy()
  }

  class BreakfastBackfill @Inject constructor() : RecipiesBackfill() {
    override val staticPrefix = "breakfast"
  }

  class BrunchBackfill @Inject constructor() : RecipiesBackfill() {
    override val staticPrefix = "brunch"
  }

  class LunchBackfill @Inject constructor() : RecipiesBackfill() {
    override val staticPrefix = "lunch"
  }

  class DinnerBackfill @Inject constructor() : RecipiesBackfill() {
    override val staticPrefix = "dinner"
  }

  data class RecipeAttributes(
    val cookbook: String = "in-the-kitchen-with-mikepaw",
    val course: String = "",
    val ingredientRegex: String = "",
    val validate: Boolean = true,
  )
}
