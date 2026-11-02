package com.smusic.app.domain.engine

import com.smusic.app.domain.model.LanguageContext
import com.smusic.app.domain.model.Song
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanguageResolver @Inject constructor() {

    private val languagePatterns = mapOf(
        "Tamil" to Pattern.compile("[\\u0B80-\\u0BFF]+"),
        "Hindi" to Pattern.compile("[\\u0900-\\u097F]+"),
        "Telugu" to Pattern.compile("[\\u0C00-\\u0C7F]+"),
        "Malayalam" to Pattern.compile("[\\u0D00-\\u0D7F]+"),
        "Kannada" to Pattern.compile("[\\u0C80-\\u0CFF]+"),
        "Bengali" to Pattern.compile("[\\u0980-\\u09FF]+"),
        "Punjabi" to Pattern.compile("[\\u0A00-\\u0A7F]+"),
        "Marathi" to Pattern.compile("[\\u0900-\\u097F]+")
    )

    private val romanizedLanguageKeywords = mapOf(
        "Tamil" to listOf(
            "kaadhal", "kanne", "vizhi", "kannazhaga", "aaruyire", "thalaiva", "vaathi", "kuthu",
            "gaana", "theri", "anbe", "poove", "uyire", "machan", "ponniyin", "neruppu",
            "maruvarthai", "venmegam", "oru", "naan", "ennodu", "vaadi", "kadavul", "thendral",
            "malare", "singappenney", "katchi", "sera", "kavalthayya", "arabic", "halamithi",
            "habibo", "rowdy", "baby", "kollywood", "tamil", "padam", "paatu", "nayagan",
            "jailer", "leo", "varisu", "vikram", "beast", "master"
        ),
        "Hindi" to listOf(
            "pyaar", "ishq", "dil", "mohabbat", "tum", "hum", "sanam", "zindagi", "aashiqui",
            "tere", "meri", "deewane", "chashni", "rabba", "duniya", "ankhiyaan", "jaan",
            "saathiya", "shayari", "dhadkan", "muskurane", "bekhayali", "tujhe", "raatan",
            "kesariya", "chaleya", "kahani", "bollywood", "hindi", "tauba", "shiddat", "lut",
            "gaye", "o maahi", "satranga", "ve kamleya", "apna bana le"
        ),
        "Telugu" to listOf(
            "prema", "gundello", "gundallona", "choodu", "manasu", "cheliya", "priyathama",
            "kanulu", "oohalu", "chinni", "pranam", "neeli", "kallu", "samajavaragamana",
            "buttabomma", "saranga", "dariya", "kurchi", "madhura", "ramuloo", "tollywood",
            "telugu", "pilla", "raaye", "kalavathi", "inupa", "oomeri"
        ),
        "Malayalam" to listOf(
            "premam", "swapnam", "kannethire", "hridayam", "kanmani", "ente", "ninte",
            "sneham", "kaathirunnu", "kudukku", "darshana", "aaraadhike", "cherathukal",
            "pavizha", "mazha", "illuminati", "mollywood", "malayalam", "jaada", "arm",
            "manjummel", "avesham", "vazha"
        ),
        "Punjabi" to listOf(
            "jatt", "gabru", "soch", "sohneya", "koka", "surma", "mitti", "yaari", "billo",
            "akhiyan", "parche", "wakhra", "punjab", "taare", "suit", "gaddi", "kurti",
            "jatti", "punjabi", "softly", "winning", "speech", "chobbar", "admirin", "you"
        ),
        "English" to listOf(
            "pop", "rock", "hip hop", "rap", "remix", "feat", "billboard", "official video",
            "soundtrack", "love", "night", "summer", "dance", "baby"
        )
    )

    private val artistLanguageMap = mapOf(
        // Tamil
        "anirudh" to "Tamil",
        "anirudh ravichander" to "Tamil",
        "a.r. rahman" to "Tamil",
        "a. r. rahman" to "Tamil",
        "ar rahman" to "Tamil",
        "yuvan" to "Tamil",
        "yuvan shankar raja" to "Tamil",
        "harris jayaraj" to "Tamil",
        "ilaiyaraaja" to "Tamil",
        "ilayaraja" to "Tamil",
        "santhosh narayanan" to "Tamil",
        "d. imman" to "Tamil",
        "g.v. prakash" to "Tamil",
        "g. v. prakash kumar" to "Tamil",
        "gv prakash" to "Tamil",
        "sid sriram" to "Tamil",
        "sean roldan" to "Tamil",
        "hiphop tamizha" to "Tamil",
        "vijay antony" to "Tamil",
        "pradeep kumar" to "Tamil",
        "p. susheela" to "Tamil",
        "s. p. balasubrahmanyam" to "Tamil",
        "spb" to "Tamil",
        "vidyasagar" to "Tamil",
        "deva" to "Tamil",

        // Hindi
        "arijit singh" to "Hindi",
        "arijit" to "Hindi",
        "shreya ghoshal" to "Hindi",
        "neha kakkar" to "Hindi",
        "atif aslam" to "Hindi",
        "pritam" to "Hindi",
        "vishal-shekhar" to "Hindi",
        "sachin-jigar" to "Hindi",
        "badshah" to "Hindi",
        "yo yo honey singh" to "Hindi",
        "honey singh" to "Hindi",
        "jubin nautiyal" to "Hindi",
        "armaan malik" to "Hindi",
        "sonu nigam" to "Hindi",
        "alka yagnik" to "Hindi",
        "kumar sanu" to "Hindi",
        "udit narayan" to "Hindi",
        "sunidhi chauhan" to "Hindi",
        "mohit chauhan" to "Hindi",
        "kk" to "Hindi",
        "darshan raval" to "Hindi",
        "b praak" to "Hindi",
        "mika singh" to "Hindi",

        // Telugu
        "devi sri prasad" to "Telugu",
        "dsp" to "Telugu",
        "s. thaman" to "Telugu",
        "thaman" to "Telugu",
        "thaman s" to "Telugu",
        "mickey j. meyer" to "Telugu",
        "m.m. keeravaani" to "Telugu",
        "keeravaani" to "Telugu",
        "anurag kulkarni" to "Telugu",
        "ram miriyala" to "Telugu",
        "mangli" to "Telugu",

        // Malayalam
        "sushin shyam" to "Malayalam",
        "jakes bejoy" to "Malayalam",
        "hesham abdul wahab" to "Malayalam",
        "shaan rahman" to "Malayalam",
        "deepak dev" to "Malayalam",
        "vineeth sreenivasan" to "Malayalam",
        "k.s. harisankar" to "Malayalam",

        // Punjabi
        "karan aujla" to "Punjabi",
        "diljit dosanjh" to "Punjabi",
        "sidhu moosewala" to "Punjabi",
        "ap dhillon" to "Punjabi",
        "shubh" to "Punjabi",
        "guru randhawa" to "Punjabi",
        "ammy virk" to "Punjabi",
        "jasmine sandlas" to "Punjabi",
        "sunanda sharma" to "Punjabi",

        // English / Global
        "the weeknd" to "English",
        "taylor swift" to "English",
        "ed sheeran" to "English",
        "drake" to "English",
        "billie eilish" to "English",
        "bruno mars" to "English",
        "dua lipa" to "English",
        "justin bieber" to "English",
        "post malone" to "English",
        "eminem" to "English",
        "coldplay" to "English",
        "imagine dragons" to "English",
        "ariana grande" to "English",
        "rihanna" to "English",
        "kendrick lamar" to "English",
        "travis scott" to "English"
    )

    private val genreKeywords = mapOf(
        "Melody" to listOf("melody", "romantic", "love", "duet", "slow", "feel", "soul", "unplugged", "acoustic", "lofi"),
        "Mass" to listOf("mass", "kuthu", "beat", "dance", "party", "fast", "folk", "dappu", "bhangra", "remix", "club", "trap"),
        "Sad" to listOf("sad", "broken", "pain", "tears", "alone", "heartbreak", "emotional", "crying"),
        "Devotional" to listOf("devotional", "bhajan", "god", "spiritual", "mantra", "aarti", "chalisa"),
        "Classical" to listOf("classical", "carnatic", "hindustani", "raga"),
        "Folk" to listOf("folk", "gaana", "village", "desi")
    )

    fun resolveFromQuery(query: String): LanguageContext {
        val q = query.lowercase()
        var resolvedLang = "unknown"
        var resolvedGenre = ""
        var confidence = 0.0f

        // 1. Explicit language keyword match
        val languages = listOf("tamil", "hindi", "telugu", "malayalam", "kannada", "punjabi", "bengali", "marathi", "english")
        for (lang in languages) {
            if (q.contains(lang)) {
                resolvedLang = lang.replaceFirstChar { it.uppercase() }
                confidence = 0.95f
                break
            }
        }

        // 2. Artist match check
        if (resolvedLang == "unknown") {
            for ((artist, lang) in artistLanguageMap) {
                if (q.contains(artist)) {
                    resolvedLang = lang
                    confidence = 0.9f
                    break
                }
            }
        }

        // 3. Romanized keywords check
        if (resolvedLang == "unknown") {
            val tokens = q.split(Regex("[^\\p{Alnum}]+")).filter { it.isNotBlank() }
            for ((lang, keywords) in romanizedLanguageKeywords) {
                if (tokens.any { token -> keywords.contains(token) }) {
                    resolvedLang = lang
                    confidence = 0.8f
                    break
                }
            }
        }

        // 4. Genre keyword match
        for ((genre, keywords) in genreKeywords) {
            if (keywords.any { q.contains(it) }) {
                resolvedGenre = genre
                if (confidence < 0.5f) confidence = 0.7f
                break
            }
        }

        return if (resolvedLang != "unknown") {
            LanguageContext(resolvedLang, resolvedGenre, confidence = confidence, source = "query")
        } else {
            LanguageContext.UNKNOWN
        }
    }

    fun resolveFromSong(song: Song): LanguageContext {
        val resolvedGenre = resolveGenre(song)

        if (song.language.isNotBlank() && song.language.lowercase() != "unknown") {
            return LanguageContext(
                language = song.language.replaceFirstChar { it.uppercase() },
                genre = resolvedGenre.ifBlank { song.genre },
                confidence = 1.0f,
                source = "metadata"
            )
        }

        val combinedText = "${song.title} ${song.artist}".lowercase()

        // 1. Script detection (native character sets)
        for ((lang, pattern) in languagePatterns) {
            if (pattern.matcher(combinedText).find()) {
                return LanguageContext(lang, resolvedGenre, confidence = 0.98f, source = "script")
            }
        }

        // 2. Artist-to-language knowledge match
        val artistLower = song.artist.lowercase()
        for ((artistKey, lang) in artistLanguageMap) {
            if (artistLower.contains(artistKey)) {
                return LanguageContext(lang, resolvedGenre, confidence = 0.95f, source = "artist_knowledge")
            }
        }

        // 3. Romanized song title keyword match
        val tokens = combinedText.split(Regex("[^\\p{Alnum}]+")).filter { it.length >= 3 }
        for ((lang, keywords) in romanizedLanguageKeywords) {
            if (tokens.any { token -> keywords.contains(token) }) {
                return LanguageContext(lang, resolvedGenre, confidence = 0.85f, source = "romanized_keywords")
            }
        }

        // 4. Fallback: Check if English query terms exist
        if (combinedText.contains("english") || combinedText.contains("pop")) {
            return LanguageContext("English", resolvedGenre, confidence = 0.7f, source = "english_terms")
        }

        return if (resolvedGenre.isNotBlank()) {
            LanguageContext("unknown", resolvedGenre, confidence = 0.5f, source = "genre_only")
        } else {
            LanguageContext.UNKNOWN
        }
    }

    fun resolveGenre(song: Song): String {
        if (song.genre.isNotBlank() && song.genre.lowercase() != "unknown") {
            return song.genre
        }
        val text = "${song.title} ${song.artist}".lowercase()
        for ((genre, keywords) in genreKeywords) {
            if (keywords.any { text.contains(it) }) {
                return genre
            }
        }
        return ""
    }
}
