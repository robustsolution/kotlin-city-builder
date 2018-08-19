# Game Design
## The different resources
Resource | Explanation
:-:|:-:
Citizens|The citizens are the main resource of the game. They are placed in the buildings to activate the production or to improve it. Citizens have a life time.
Happiness|Happiness is the resource that allows the creation of new citizens.
Food|Food is the resource consumed by every citizen. It is necessary to manage this resource to arrive at a balance. If the food is at zero, citizens will die.
Research|The research is the resource that allows you to obtain improvements, bonuses, new buildings or enlarge your city.
Stone|Stone is the resource that is used to build new buildings or improve them.
Building|The buildings allow the production of the different resources. Buildings are disintegrating over time, so they must be repaired with stone otherwise they will collapse and kill the citizens inside.
## The different types of buildings
Type | Explanation
:-:|:-:
Citizens|These buildings increase the maximum number of Citizens of the city and allow the production of new Citizens.
Happiness|These buildings produce the Happiness resource.
Food|These buildings increase the maximum number of Food storable and produce it. 
Research|These buildings produce the Research resource and allow you to use it to improv your city. 
Stone|These buildings increase the maximum number of Stone storable and produce it.
## Interactions between different types of buildings
Each building produces its resource, but this production is modified by different factors including the proximity of other buildings.
![
  Citizens  -> Happiness[color=green]
  Research  -> Happiness[color=blue]
    Stone   -> Happiness[color=yellow]
    Food    -> Happiness[color=red]
  Citizens  ->   Stone  [color=yellow]
  Happiness ->   Stone  [color=blue]
  Research  ->   Stone  [color=red]
    Food    ->   Stone  [color=green]
  Citizens  ->   Food   [color=blue]
  Happiness ->   Food   [color=green]
    Stone   ->   Food   [color=red]
  Research  ->   Food   [color=yellsow]
  Happiness -> Citizens [color=red]
    Stone   -> Citizens [color=blue]
    Food    -> Citizens [color=yellow]
  Research  -> Citizens [color=green]
  Citizens  -> Research [color=red]
  Happiness -> Research [color=yellow]
    Stone   -> Research [color=green]
    Food    -> Research [color=blue]
](assets/penta.png)

Color | Modifier (indicative)
:------:|:------------------------:
Red|+ +
Green|+
Blue|-
Yellow|- -

