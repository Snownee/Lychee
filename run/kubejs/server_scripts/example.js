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