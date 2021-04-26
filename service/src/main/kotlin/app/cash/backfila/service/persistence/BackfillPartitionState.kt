package app.cash.backfila.service.persistence

enum class BackfillPartitionState {
  PAUSED,
  RUNNING,
  STALE,
  CANCELLED,
  COMPLETE
}
