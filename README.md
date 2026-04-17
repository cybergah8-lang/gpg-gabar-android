<div align="center">
  <img src="images (4).png" alt="GPG Gabar Tanıtım" width="700" />
</div>

# GPG Gabar

Sepaneke Androidê ya li ser standarda PGP/OpenPGP şîfrekirina nivîs û pelan dike.

Sepaneke Androidê ye ku li ser standarda cîhanî ya PGP/OpenPGP hatiye avakirin. Ev sepan rê dide te ku nivîs û pelên xwe bi ewlehiya bilind bişîfre bikî û dema pêwîst be jî wan veki.

## Taybetmendiyên Sereke

- Şîfrekirin û vekirina nivîsan (text)
- Şîfrekirina pelan (bi paşgira `.gpg`) û vekirina wan (`.gpg` / `.pgp`)
- Çêkirina kilîtên PGP
- Anîna kilîtan ji pelên `.asc` (kilîta giştî û kilîta veşartî)
- Derxistina kilîtan (tenê kilîta giştî an jî kilîta giştî + veşartî)
- Rêvebirina kilîtan di hundirê sepanê de (lîstekirin, jêbirin)
- Piştgiriya zimanên cuda (Kurmancî, Tirkî, Îngilîzî û yên din)
- Bi menuya "Share" re rasterast şîfrekirin û vekirin

## Bingeha Teknîkî

- **Ziman û Navrû:** Kotlin, Jetpack Compose, Material 3
- **Kriptografî:** Bouncy Castle (`bcprov`, `bcpg`)
- **Ewlehiya Daneyan:** SQLCipher + AndroidX Security (EncryptedSharedPreferences)
- **Min SDK:** 24
- **Target/Compile SDK:** 36

## Notên Ewlehiyê

- Sepan ji bo şîfreya kilîtê dirêjahiyeke pir bilind (minimum 100 karakter) ferz dike.
- Dosyayên îmzekirina release (`signing/`, `keystore.properties`, `.jks`) **qet** nayên barkirin nav repositoriyê.
- Pelên herêmî yên wekî `local.properties`, `.idea/`, `build/` û hwd. nayên barkirin.

## Projeyê Çawa Xebitînin?

### Pêdiviyên

- Android Studio (guhertoya herî nû)
- JDK (ya ku bi Android Studio re tê, bes e)
- Android SDK (minSdk 24 û jor)

### Sazkirin

```bash
git clone <REPO_URL>
cd gpg-gabar-android
