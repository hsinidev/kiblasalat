import os
import sqlite3
import urllib.request
import json
import traceback

DB_DIR = os.path.join("app", "src", "main", "assets", "databases")
DB_PATH = os.path.join(DB_DIR, "quran.db")

# Fallback dataset (Al-Fatihah, Al-Ikhlas, Al-Falaq, An-Nas) to ensure the app is fully functional even if offline during build
FALLBACK_SURAH_LIST = [
    {
        "id": 1,
        "name": "سُورَةُ ٱلْفَاتِحَةِ",
        "english_name": "Al-Fatihah",
        "english_name_translation": "The Opening",
        "revelation_type": "Meccan",
        "total_ayahs": 7,
        "ayahs": [
            {"num": 1, "arabic": "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ", "english": "In the name of Allah, the Entirely Merciful, the Especially Merciful.", "juz": 1},
            {"num": 2, "arabic": "ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَٰلَمِينَ", "english": "[All] praise is [due] to Allah, Lord of the worlds -", "juz": 1},
            {"num": 3, "arabic": "ٱلرَّحْمَٰنِ ٱلرَّحِيمِ", "english": "The Entirely Merciful, the Especially Merciful,", "juz": 1},
            {"num": 4, "arabic": "مَٰلِكِ يَوْمِ ٱلدِّينِ", "english": "Sovereign of the Day of Recompense.", "juz": 1},
            {"num": 5, "arabic": "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "english": "It is You we worship and You we ask for help.", "juz": 1},
            {"num": 6, "arabic": "ٱهْدِنَا ٱلصِّرَٰطَ ٱلْمُسْتَقِيمَ", "english": "Guide us to the straight path -", "juz": 1},
            {"num": 7, "arabic": "صِرَٰطَ ٱلَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ ٱلْمَغْضُوبِ عَلَيْهِمْ وَلَا ٱلضَّآلِّينَ", "english": "The path of those upon whom You have bestowed favor, not of those who have evoked [Your] anger or of those who are astray.", "juz": 1}
        ]
    },
    {
        "id": 112,
        "name": "سُورَةُ ٱلْإِخْلَاصِ",
        "english_name": "Al-Ikhlas",
        "english_name_translation": "The Sincerity",
        "revelation_type": "Meccan",
        "total_ayahs": 4,
        "ayahs": [
            {"num": 1, "arabic": "قُلْ هُوَ ٱللَّهُ أَحَدٌ", "english": "Say, \"He is Allah, [who is] One,", "juz": 30},
            {"num": 2, "arabic": "ٱللَّهُ ٱلصَّمَدُ", "english": "Allah, the Eternal Refuge.", "juz": 30},
            {"num": 3, "arabic": "لَمْ يَلِدْ وَلَمْ يُولَدْ", "english": "He neither begets nor is born,", "juz": 30},
            {"num": 4, "arabic": "وَلَمْ يَكُن لَّهُۥ كُفُوًا أَحَدٌ", "english": "Nor is there to Him any equivalent.\"", "juz": 30}
        ]
    },
    {
        "id": 113,
        "name": "سُورَةُ ٱلْفَلَقِ",
        "english_name": "Al-Falaq",
        "english_name_translation": "The Daybreak",
        "revelation_type": "Meccan",
        "total_ayahs": 5,
        "ayahs": [
            {"num": 1, "arabic": "قُلْ أَعُوذُ بِرَبِّ ٱلْفَلَقِ", "english": "Say, \"I seek refuge in the Lord of daybreak", "juz": 30},
            {"num": 2, "arabic": "مِن شَرِّ مَا خَلَقَ", "english": "From the evil of whatever He created", "juz": 30},
            {"num": 3, "arabic": "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ", "english": "And from the evil of darkness when it settles", "juz": 30},
            {"num": 4, "arabic": "وَمِن شَرِّ ٱلنَّفَّٰثَٰتِ فِي ٱلْعُقَدِ", "english": "And from the evil of the blowers in knots", "juz": 30},
            {"num": 5, "arabic": "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ", "english": "And from the evil of an envier when he envies.\"", "juz": 30}
        ]
    },
    {
        "id": 114,
        "name": "سُورَةُ ٱلنَّاسِ",
        "english_name": "An-Nas",
        "english_name_translation": "Mankind",
        "revelation_type": "Meccan",
        "total_ayahs": 6,
        "ayahs": [
            {"num": 1, "arabic": "قُلْ أَعُوذُ بِرَبِّ ٱلنَّاسِ", "english": "Say, \"I seek refuge in the Lord of mankind,", "juz": 30},
            {"num": 2, "arabic": "مَلِكِ ٱلنَّاسِ", "english": "The Sovereign of mankind.", "juz": 30},
            {"num": 3, "arabic": "إِلَٰهِ ٱلنَّاسِ", "english": "The God of mankind,", "juz": 30},
            {"num": 4, "arabic": "مِن شَرِّ ٱلْوَسْوَاسِ ٱلْخَنَّاسِ", "english": "From the evil of the retreating whisperer -", "juz": 30},
            {"num": 5, "arabic": "ٱلَّذِي يُوَسْوِسُ فِي صُدُورِ ٱلنَّاسِ", "english": "Who whispers [evil] into the breasts of mankind -", "juz": 30},
            {"num": 6, "arabic": "مِنَ ٱلْجِنَّةِ وَٱلنَّاسِ", "english": "From among the jinn and mankind.\"", "juz": 30}
        ]
    }
]

def fetch_json(url):
    print(f"Fetching {url}...")
    req = urllib.request.Request(
        url, 
        headers={'User-Agent': 'Mozilla/5.0'}
    )
    with urllib.request.urlopen(req, timeout=15) as response:
        return json.loads(response.read().decode('utf-8'))

def create_database():
    os.makedirs(DB_DIR, exist_ok=True)
    if os.path.exists(DB_PATH):
        os.remove(DB_PATH)

    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()

    # Create tables matching Room Entity structure exactly
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS `surah` (
        `id` INTEGER NOT NULL, 
        `name` TEXT NOT NULL, 
        `english_name` TEXT NOT NULL, 
        `english_name_translation` TEXT NOT NULL, 
        `revelation_type` TEXT NOT NULL, 
        `total_ayahs` INTEGER NOT NULL, 
        PRIMARY KEY(`id`)
    );
    """)

    cursor.execute("""
    CREATE TABLE IF NOT EXISTS `ayah` (
        `id` INTEGER NOT NULL, 
        `surah_id` INTEGER NOT NULL, 
        `number_in_surah` INTEGER NOT NULL, 
        `text_arabic` TEXT NOT NULL, 
        `text_english` TEXT NOT NULL, 
        `juz` INTEGER NOT NULL, 
        PRIMARY KEY(`id`), 
        FOREIGN KEY(`surah_id`) REFERENCES `surah`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
    );
    """)
    cursor.execute("CREATE INDEX IF NOT EXISTS `index_ayah_surah_id` ON `ayah` (`surah_id`);")

    cursor.execute("""
    CREATE TABLE IF NOT EXISTS `bookmarks` (
        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
        `surah_id` INTEGER NOT NULL, 
        `ayah_number` INTEGER NOT NULL, 
        `created_at` INTEGER NOT NULL, 
        FOREIGN KEY(`surah_id`) REFERENCES `surah`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
    );
    """)
    cursor.execute("CREATE INDEX IF NOT EXISTS `index_bookmarks_surah_id` ON `bookmarks` (`surah_id`);")

    cursor.execute("""
    CREATE TABLE IF NOT EXISTS `settings` (
        `key` TEXT NOT NULL, 
        `value` TEXT NOT NULL, 
        PRIMARY KEY(`key`)
    );
    """)

    # Attempt to fetch full online dataset
    try:
        ar_data = fetch_json("http://api.alquran.cloud/v1/quran/quran-uthmanic")
        en_data = fetch_json("http://api.alquran.cloud/v1/quran/en.sahih")
        
        ar_surahs = ar_data["data"]["surahs"]
        en_surahs = en_data["data"]["surahs"]

        print("Populating Quran database from API...")
        global_ayah_id = 1
        for i in range(114):
            ar_surah = ar_surahs[i]
            en_surah = en_surahs[i]
            
            surah_id = ar_surah["number"]
            name = ar_surah["name"]
            english_name = ar_surah["englishName"]
            english_name_translation = ar_surah["englishNameTranslation"]
            revelation_type = ar_surah["revelationType"]
            total_ayahs = len(ar_surah["ayahs"])

            cursor.execute(
                "INSERT INTO surah (id, name, english_name, english_name_translation, revelation_type, total_ayahs) VALUES (?, ?, ?, ?, ?, ?)",
                (surah_id, name, english_name, english_name_translation, revelation_type, total_ayahs)
            )

            for j in range(total_ayahs):
                ar_ayah = ar_surah["ayahs"][j]
                en_ayah = en_surah["ayahs"][j]
                
                # Strip the Bismillah prefix for all Surahs except Surah 1 and 9 from Uthmanic text if it contains it
                # to render it nicely in the headers. Al-Quran.cloud returns Bismillah prefix in some editions.
                # In quran-uthmanic, first Ayah of each surah (except 1 and 9) starts with "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ "
                text_arabic = ar_ayah["text"]
                bismillah = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"
                if surah_id != 1 and surah_id != 9 and j == 0 and text_arabic.startswith(bismillah):
                    text_arabic = text_arabic[len(bismillah):].strip()
                    # If empty (which sometimes happens if it was just the Bismillah), restore it or leave it
                    if not text_arabic:
                        text_arabic = ar_ayah["text"]

                cursor.execute(
                    "INSERT INTO ayah (id, surah_id, number_in_surah, text_arabic, text_english, juz) VALUES (?, ?, ?, ?, ?, ?)",
                    (global_ayah_id, surah_id, ar_ayah["numberInSurah"], text_arabic, en_ayah["text"], ar_ayah["juz"])
                )
                global_ayah_id += 1

        print(f"Successfully populated database with 114 Surahs and {global_ayah_id - 1} Ayahs.")

    except Exception as e:
        print("Failed to download Quran data from API. Initializing with local fallback dataset...")
        traceback.print_exc()
        
        # Populate using fallback dataset
        global_ayah_id = 1
        for surah in FALLBACK_SURAH_LIST:
            cursor.execute(
                "INSERT INTO surah (id, name, english_name, english_name_translation, revelation_type, total_ayahs) VALUES (?, ?, ?, ?, ?, ?)",
                (surah["id"], surah["name"], surah["english_name"], surah["english_name_translation"], surah["revelation_type"], surah["total_ayahs"])
            )
            for ayah in surah["ayahs"]:
                cursor.execute(
                    "INSERT INTO ayah (id, surah_id, number_in_surah, text_arabic, text_english, juz) VALUES (?, ?, ?, ?, ?, ?)",
                    (global_ayah_id, surah["id"], ayah["num"], ayah["arabic"], ayah["english"], ayah["juz"])
                )
                global_ayah_id += 1
        print(f"Successfully populated database with fallback {len(FALLBACK_SURAH_LIST)} Surahs and {global_ayah_id - 1} Ayahs.")

    # Commit and close
    conn.commit()
    conn.close()
    print("Quran pre-populated database created successfully.")

if __name__ == "__main__":
    create_database()
