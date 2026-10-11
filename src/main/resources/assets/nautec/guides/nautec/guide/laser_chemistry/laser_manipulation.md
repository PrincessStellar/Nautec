---
navigation:
  title: Moving Laser Power
  icon: nautec:laser_junction
  position: 0
  parent: laser_chemistry/laser_chemistry-index.md
item_ids:
  - nautec:prismarine_laser_relay
  - nautec:laser_junction
---

# <Color id="light_purple">Moving Laser Power</Color>

<Column alignItems="center" fullWidth={true}>
  <ItemImage id="laser_junction" scale="2"/>
  Carry Aquatic Power (AP) from a source to the machines that run on it.
</Column>

## <Color id="gold">How a Beam Works</Color>

Every NauTec machine runs on Aquatic Power, carried by a laser beam. A beam is a rate, measured in AP per tick. Nothing along the way stores it, and machines do not use it up: a machine runs as long as enough AP per tick is arriving.

* A beam travels in a straight line, up to 16 blocks from most laser blocks and 64 from a relay (both configurable).
* The first block with a hitbox in its path stops it. Water does not. A low laser block such as the Charger catches a beam coming from the side, even though the beam runs over its top.
* It only connects when the block at the far end accepts a beam on the face it hits. Connections are rechecked every half second.
* A powered beam burns any mob or player standing in it.
* An item dropped into a powered beam can be changed by [Item Transformation](nautec:getting_started/item_transformation.md).

Beams also carry purity, which some recipes need. See [Beam Optics](nautec:deep_engineering/beam_optics.md) for how routing changes it.

<Color id="gold">Tip</Color>: the <ItemLink id="prism_monocle"/> shows the power and purity arriving at any laser block you look at.

***

## <Color id="gold">What Machines Need</Color>

| Machine | AP per tick it needs |
|---|---|
| [Mixer](mixer.md) | 10 or more |
| [Deep Sea Drain](drain.md) | more than 20 |
| [Charger](charger.md) | any amount (the item takes what the beam delivers, up to its input limit) |
| [Fishing Station](fishing_station.md) | 1 or more |
| [Confined Spawner](confined_spawner.md) | 50 to run nonstop (less runs it in bursts) |

An <ItemLink id="aquatic_catalyst"/> burning Prismarine Shards or Prismarine Crystal Shards sends 12 AP per tick. Burning Prismarine Crystals it sends 6. The machine requirements above are configurable.

***

<Row>
  <ItemImage id="prismarine_laser_relay"/>
  ### <Color id="aqua">Prismarine Laser Relay</Color>
</Row>

Takes a beam in at the back and sends it out the front, up to 64 blocks (`relayLaserDistance`). Power and purity pass through unchanged, so chain relays to cover any distance. Relays can sit underwater.

A relay lines itself up when you place it against another laser block. Place it on a source's emitting face (a catalyst's lens, another relay's front) and it takes that beam in and passes it on away from the source. Place it on a machine that takes a beam and it points into that machine. Against anything else, its front points the way you are looking. Right-click it with an <ItemLink id="aquarine_steel_wrench"/> in Rotate mode to turn it to the next direction.

<RecipeFor id="prismarine_laser_relay"/>

***

<Row>
  <ItemImage id="laser_junction"/>
  ### <Color id="aqua">Laser Junction</Color>
</Row>

Merges beams and sends them where you choose. Each of its six faces can be an input, an output or closed, and all of them start closed.

* Right-click a face with an <ItemLink id="aquarine_steel_wrench"/> to make it an input. Do it again to close it.
* Shift-right-click a face to make it an output. Do it again to close it.

The power of every input is added together and split evenly between the connected outputs, rounded down. Two outputs from a 25 AP input get 12 AP each. Purity stays close to the purest input, losing a quarter of the gap to the average of every connected input. A source that has gone idle counts as zero, so an empty input still pulls the purity down a little.

Use one to add two weak sources together, for example two catalysts into a [Deep Sea Drain](drain.md). It also turns a beam round a corner (one input, one output on another face) and splits one beam evenly between up to five machines, with no loss of purity either way.

<RecipeFor id="laser_junction"/>

***

## <Color id="gold">Longer and Bent Routes</Color>

Chain relays for distance, and use a Laser Junction wherever the beam has to turn or split. [Beam Optics](nautec:deep_engineering/beam_optics.md) covers the Focusing Lens, which raises purity. For power that has to cross the map or reach another dimension, see the [Prism Satellite](nautec:deep_engineering/prism_satellite.md).
