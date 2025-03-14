// priority: 0

// Visit the wiki for more info - https://kubejs.com/

ServerEvents.recipes(event => {
	let yamlRecipe = yaml => event.custom(Lychee.toJSON(yaml))
	yamlRecipe(`
    type: 'lychee:item_burning'
    item_in:
      item: grass_block
    post:
      - type: delay
        s: 0.2
      - type: random
        entries:
          - weight: 1999
            type: drop_item
            id: coal
          - weight: 1
            type: drop_item
            id: diamond
    `).id('test:yaml_recipe')
})

ServerEvents.recipes(event => {
	let lb = LycheeBuilder.create(event.jsonOps)
	let javaRecipe = obj => event.custom(Lychee.toJSON(obj))
	let $Chance = Java.loadClass('snownee.lychee.contextual.Chance')
	javaRecipe(
		lb.itemBurningRecipe(lb.sized('3x glass'))
			.post(lb.dropItem('3x sand'))
			.condition($Chance(0.5))
	).id('test:java_recipe')
	lb.teardown()
})