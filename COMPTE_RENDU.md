# Compte rendu — TP Android « Liste de contacts »

**Auteur :** _[à compléter]_
**Classe / groupe :** _[à compléter]_
**Date :** _[à compléter]_

## 1. Objectif

Réaliser une application Android en Kotlin avec deux activités permettant d'afficher une liste de contacts, de consulter et modifier un contact, de gérer sa photo, de lancer le composeur téléphonique et une application de messagerie, puis de renvoyer les modifications à la liste.

## 2. Travail réalisé

- Création des activités `MainActivity` et `DetailActivity`.
- Modèle `Contact` (`Serializable`) contenant l'identifiant, le nom, le prénom, le téléphone, l'e-mail et le chemin de la photo.
- `ListView` personnalisée via `ContactAdapter` : photo, nom complet, téléphone et e-mail.
- Ouverture de `DetailActivity` avec un **Intent explicite** transportant le contact et sa position.
- Formulaire d'édition avec `TextInputLayout` et validation des champs.
- **Appeler** : Intent implicite `ACTION_DIAL` avec un URI `tel:`.
- **Envoyer un e-mail** : Intent implicite `ACTION_SENDTO` avec un URI `mailto:` et un sujet prérempli.
- **Galerie** : `ActivityResultContracts.PickVisualMedia`.
- **Appareil photo** : `ActivityResultContracts.TakePicture` avec un URI `FileProvider`.
- **Enregistrer** : `setResult(RESULT_OK, ...)` puis mise à jour immédiate de la liste et de la photo dans `MainActivity`.
- **Annuler** et bouton Retour système : retour sans enregistrement, conservation de l'ancienne photo.
- Gestion des cas d'erreur : champs obligatoires, e-mail invalide, annulation de photo, absence d'application compatible, caméra indisponible.
- Conservation de l'état lors d'une rotation d'écran.

## 3. Principales étapes

1. Analyse de l'énoncé et des maquettes.
2. Création du projet Android Studio (Kotlin, `minSdk 24`).
3. Mise en place du modèle `Contact` et de la liste personnalisée.
4. Création de `DetailActivity` et transmission des données par Intent explicite.
5. Ajout des Intents implicites pour l'appel et l'e-mail.
6. Gestion de la galerie et de la caméra avec les Activity Result APIs.
7. Retour du contact modifié à `MainActivity` et actualisation de la liste.
8. Validation, gestion des erreurs et tests.

## 4. Tests effectués

| Test | Résultat attendu | Résultat obtenu |
|---|---|---|
| Ouvrir un contact et modifier ses informations | La liste est mise à jour après « Enregistrer » | _[à compléter]_ |
| Annuler une modification | Les anciennes données restent affichées | _[à compléter]_ |
| Enregistrer avec un champ obligatoire vide | Les champs sont signalés en rouge, rien n'est enregistré | _[à compléter]_ |
| Saisir un e-mail invalide | Un message d'erreur s'affiche | _[à compléter]_ |
| Cliquer sur « Appeler » | Le composeur téléphonique s'ouvre avec le numéro | _[à compléter]_ |
| Cliquer sur « Envoyer un e-mail » | L'application de messagerie s'ouvre avec l'adresse | _[à compléter]_ |
| Choisir une photo dans la galerie | La photo apparaît dans le détail puis dans la liste | _[à compléter]_ |
| Annuler la sélection de photo | L'ancienne photo reste affichée | _[à compléter]_ |
| Prendre une photo avec la caméra | La photo apparaît après validation | _[à compléter]_ |
| Annuler la prise de photo | L'ancienne photo reste affichée | _[à compléter]_ |
| Rotation de l'écran dans le détail | Les saisies et la photo sont conservées | _[à compléter]_ |

## 5. Difficultés rencontrées

- **Chemin du projet sous Windows** : le tiret cadratin (`—`) du dossier déclenche un contrôle d'AGP et perturbe les tests unitaires lancés depuis ce chemin. La propriété `android.overridePathCheck=true` a été ajoutée ; les tests unitaires ont été vérifiés dans un chemin ASCII.
- **Photos** : les URI de la galerie sont temporaires. Les images sont donc copiées dans le stockage interne de l'application.
- **Caméra** : utilisation de `FileProvider` pour partager un fichier temporaire avec l'application appareil photo, sans demander la permission `CAMERA`.
- **Android 11+** : ajout de balises `<queries>` pour que `resolveActivity` puisse détecter les applications d'appel, de messagerie et de capture photo.
- **Rotation** : sauvegarde du chemin de la photo et du fichier temporaire dans `onSaveInstanceState`.

## 6. Captures d'écran

Les captures réelles sont à placer dans le dossier [`captures/`](captures/) :

- [ ] `01-main-activity.png`
- [ ] `02-detail-activity.png`
- [ ] `03-modification.png`
- [ ] `04-galerie.png`
- [ ] `05-camera.png`
- [ ] `06-appel.png`
- [ ] `07-email.png`

## 7. Conclusion

L'application met en œuvre les deux activités demandées, la `ListView` personnalisée, les Intents explicites et implicites, la gestion de la galerie et de la caméra, ainsi que le retour de résultat entre activités. Les cas d'erreur principaux sont gérés et l'interface s'adapte à la rotation de l'écran.

## Annexe — Commandes utiles

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:lintDebug
```

APK : `app/build/outputs/apk/debug/app-debug.apk`.
