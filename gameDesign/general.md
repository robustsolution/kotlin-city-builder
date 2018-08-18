# Game Design

## Interection entre les types de bâtiments

```dot {engine="circo"}
digraph {
  Habitant -> Bonheur[color=green]
  Habitant -> Construction
  Habitant -> Nouriture[color=blue]
  Habitant -> Recherche[color=red]
  Bonheur -> Habitant[color=red]
  Bonheur -> Construction[color=blue]
  Bonheur -> Recherche
  Bonheur -> Nouriture[color=green]
  Construction -> Habitant[color=blue]
  Construction -> Recherche[color=green]
  Construction -> Bonheur
  Construction -> Nouriture[color=red]
  Nouriture -> Habitant
  Nouriture -> Construction[color=green]
  Nouriture -> Bonheur[color=red]
  Nouriture -> Recherche[color=blue]
  Recherche -> Habitant[color=green]
  Recherche -> Bonheur[color=blue]
  Recherche -> Nouriture
  Recherche -> Construction[color=red]
}
```

Couleur | Modificateur (indicatif)
:------:|:------------------------:
Rouge|+ +
Vert|+
Bleu|-
Noir|- -

Chaque bâtiment produit sa ressource mais cette production est modifié par different facteurs dont la proximite des autres bâtiments comme cite precedement.