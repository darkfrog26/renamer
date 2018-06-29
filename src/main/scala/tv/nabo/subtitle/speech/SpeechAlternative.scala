package tv.nabo.subtitle.speech

case class SpeechAlternative(confidence: Double, transcript: String, words: List[SpeechWord])
