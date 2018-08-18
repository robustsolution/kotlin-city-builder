# Game Design

## Intéractions entre les types de bâtiments

```dot {engine="circo"}
digraph {
  Habitant -> Bonheur[color=green]
  Habitant -> Construction
  Habitant -> Nourriture[color=blue]
  Habitant -> Recherche[color=red]
  Bonheur -> Habitant[color=red]
  Bonheur -> Construction[color=blue]
  Bonheur -> Recherche
  Bonheur -> Nourriture[color=green]
  Construction -> Habitant[color=blue]
  Construction -> Recherche[color=green]
  Construction -> Bonheur
  Construction -> Nourriture[color=red]
  Nourriture -> Habitant
  Nourriture -> Construction[color=green]
  Nourriture -> Bonheur[color=red]
  Nourriture -> Recherche[color=blue]
  Recherche -> Habitant[color=green]
  Recherche -> Bonheur[color=blue]
  Recherche -> Nourriture
  Recherche -> Construction[color=red]
}
```

Couleur | Modificateur (indicatif)
:------:|:------------------------:
Rouge|+ +
Vert|+
Bleu|-
Noir|- -

Chaque bâtiment produit sa ressource, mais cette production est modifiée par differents facteurs dont la proximité des autres bâtiments.