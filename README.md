# TP Android — Liste de contacts

Application Android en Kotlin conforme à l'énoncé `TP_Android_Contacts_V2_Enonce_Etudiant.pdf`.

## Fonctionnalités

- `MainActivity` : liste de contacts dans une `ListView` personnalisée (photo, nom complet, téléphone, e-mail).
- Clic sur un contact : ouverture de `DetailActivity` par **Intent explicite**, avec transmission du contact et de sa position.
- `DetailActivity` : consultation et modification du nom, du prénom, du téléphone et de l'e-mail.
- Bouton **Appeler** : Intent implicite `ACTION_DIAL` avec le numéro du contact.
- Bouton **Envoyer un e-mail** : Intent implicite `ACTION_SENDTO` (`mailto:`) avec l'adresse préremplie.
- Bouton **Galerie** : sélection d'une photo avec `ActivityResultContracts.PickVisualMedia` (photo picker moderne, repli automatique sur les anciens appareils).
- Bouton **Appareil photo** : capture via `ActivityResultContracts.TakePicture` et un URI `FileProvider`.
- Bouton **Enregistrer** : validation, renvoi du contact modifié avec `setResult(RESULT_OK, ...)`, puis mise à jour immédiate de la liste et de la photo dans `MainActivity`.
- Bouton **Annuler** (et bouton Retour système) : retour sans enregistrer, l'ancienne photo est conservée.
- Gestion des cas d'erreur : champs obligatoires vides, e-mail invalide, annulation de la galerie ou de la caméra, absence d'application compatible, caméra indisponible.
- Conservation de l'état lors d'une rotation d'écran (`onSaveInstanceState`).

## Champs obligatoires

- Nom, prénom et téléphone sont obligatoires.
- L'e-mail est facultatif, mais doit être valide s'il est renseigné.
- Les boutons d'appel et d'e-mail vérifient que la donnée correspondante est présente et valide.

## Structure utile

```
app/src/main/java/com/example/tpandroidlistedecontacts/
├── MainActivity.kt        # liste + lancement de DetailActivity + mise à jour du résultat
├── DetailActivity.kt      # édition, photo, appel, e-mail, enregistrement/annulation
├── Contact.kt             # modèle Serializable
├── ContactAdapter.kt      # adapter personnalisé de la ListView
├── PhotoStorage.kt        # copie des photos dans le stockage interne
└── ImageUtils.kt          # chargement des photos avec sous-échantillonnage

app/src/main/res/
├── layout/activity_main.xml
├── layout/item_contact.xml
├── layout/activity_detail.xml
├── drawable/              # icônes et avatar par défaut
└── xml/file_paths.xml     # chemins exposés par le FileProvider
```

## Lancer le projet

1. Ouvrir le dossier dans Android Studio.
2. Laisser Gradle synchroniser le projet.
3. Lancer la configuration `app` sur un émulateur ou un appareil Android (API 24 minimum).

En ligne de commande :

```powershell
.\gradlew.bat :app:assembleDebug
```

L'APK est généré dans `app/build/outputs/apk/debug/app-debug.apk`.

> Le chemin du projet contient un tiret cadratin (`—`). Pour que le build fonctionne sous Windows, la propriété `android.overridePathCheck=true` a été ajoutée dans `gradle.properties`.

## Tests à réaliser sur appareil / émulateur

- [ ] Ouvrir un contact, modifier ses informations, enregistrer et vérifier la mise à jour dans la liste.
- [ ] Vérifier que l'annulation (bouton ou retour système) conserve les anciennes données.
- [ ] Vider le nom, le prénom ou le téléphone puis enregistrer : les erreurs doivent s'afficher.
- [ ] Saisir un e-mail invalide puis enregistrer : l'erreur doit s'afficher.
- [ ] Lancer le composeur avec **Appeler**.
- [ ] Lancer l'application de messagerie avec **Envoyer un e-mail**.
- [ ] Choisir une photo dans la galerie : elle doit apparaître dans `DetailActivity` après enregistrement, puis dans la liste.
- [ ] Annuler la sélection de photo : l'ancienne photo doit rester affichée.
- [ ] Prendre une photo avec la caméra : elle doit apparaître après validation.
- [ ] Annuler la prise de photo : l'ancienne photo doit rester affichée.
- [ ] Tester sur un appareil sans application téléphone, messagerie ou caméra : un message explicite doit s'afficher.
- [ ] Faire une rotation d'écran dans `DetailActivity` : les saisies et la photo doivent être conservées.

## Notes techniques

- Les photos choisies sont copiées dans `filesDir/contacts/` afin de ne pas dépendre d'une autorisation temporaire sur l'URI de la galerie.
- Les photos prises avec la caméra passent par `FileProvider` (`cacheDir/camera/`) puis sont importées dans le stockage interne.
- Aucune permission de stockage ou de caméra n'est demandée : la photo picker et `ACTION_IMAGE_CAPTURE` ne nécessitent pas de permission runtime dans ce cas d'usage.
- Les `<queries>` du manifeste rendent les applications d'appel, de messagerie et de capture photo visibles pour `resolveActivity` sur Android 11+.
- Le résultat de `DetailActivity` est renvoyé via `setResult` / `ActivityResultContracts.StartActivityForResult`, conformément à l'énoncé.

## Remise

Le dépôt contient le projet complet. Les captures d'écran réelles et le compte rendu PDF demandés dans l'énoncé doivent être ajoutés après exécution sur un appareil ou un émulateur, car ils dépendent de l'environnement d'exécution.
