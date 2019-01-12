# User documentation

This page describes how to use the application. Since the application is targeting the french community,
the documentation is only available in french.

## Pré-requis: importer les données

La première étape consiste à importer les données dans l'application. Les données seront téléchargées
de [pathfinderfr-data](https://github.com/SvenWerlen/pathfinderfr-data/tree/master/data), elles-mêmes
extraites du site web [pathfinder-fr.org](http://www.pathfinder-fr.org).

* Ouvrir le panneau de navigation latéral
* Choisir _Mettre à jour la bibliothèque_
* Cliquer sur le bouton _Démarrer_
* Attendre la fin du processus 

_Note: l'application va télécharger environ 3 Mb de données_ 

![Capture import de données](../images/02-loaddata_small.jpg)


## Visionner les listes

Lorsque l'application dispose de données, la page d'accueil présentera les statistiques et la
navigation latérale permettra d'accéder aux différentes rubriques (compétences, dons, sorts). 

* Cliquer sur une rubrique pour obtenir la liste des éléments correspondants
* Cliquer sur un élément pour obtenir sa description
  * Le bouton _plus/moins_ (en bas à droite) permet d'afficher les détails (attributs/métadonnées)
  * Le bouton _étoile_ (en haut à droite) permet de mettre l'élément dans la liste des favoris
  * Le bouton _lien_ (en bas à gauche), visible uniquement en mode "détails" ouvre un navigateur vers la fiche [pathfinder-fr.org](https://www.pathfinder-fr.org) correspondante.

![Écran d'accueil](../images/01-welcome_small.jpg)
![Navigation latérale](../images/03-navigation_small.jpg)
![Liste de sorts](../images/04-spell-list_small.jpg)
![Détails d'un sort](../images/07-spell-detail_small.jpg)
 
 
 ## Filtrer / rechercher les listes
 
 L'application permet de filtrer les listes grâce à sa fonction de recherche intégrée dans la 
 barre supérieure de l'application.
 
 * La recherche est uniquement disponible dans la vue _liste_
 * Le filtre prend effet uniquement lorsqu'au moins 3 charactères sont saisis
 * Cliquer sur le bouton _x_ (croix) pour fermer la recherche et réinitialiser la liste complète
 * Changer de liste ré-initialisera la recherche
 
 L'application permet également de filtrer les listes grâce à sa fonction de filtre (bouton dans le coin inférieur droit)
 
 * Le bouton de filtre est uniquement disponible dans la vue _liste_ et pour les _sorts_
 * Décocher l'option _Tous_ et cocher les catégories souhaitées individuellement
 * Cliquer sur le bouton _Appliquer_ pour appliquer les filtres
 * Noter que le bouton change de couleur pour indiquer que des filtres sont actifs. 
 
 Les options de filtre sont conservées dans les préférences de l'application. Vous pouvez changer
 d'écran ou redémarrer l'application, les filtres continueront de s'appliquer.  
 
 ![Fonction de recherche](../images/05-search-list_small.jpg)
 ![Fonction de filtre](../images/06-filter-list_small.jpg)
 
  ## Paramètres et configuration
  
  L'application offre des configurations et paramètres selon les besoins de l'utilisateur.
  
  * **Général | Nom long dans les listes**: affiche un nom long dans les listes. Exemple: affiche le niveau du sort directement dans la liste. 
  * **Général | Détail des compétences, dons, sorts**: affiche le mode détaillé par défaut (voir ci-dessus). Évite de devoir cliquer systématiquement sur le bouton _plus_ pour voir les métadonnées.