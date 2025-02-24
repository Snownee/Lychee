// priority: 0

// Visit the wiki for more info - https://kubejs.com/

console.info('Hello, World! (Loaded startup scripts)')

function copyComponents(input, output) {
    let components = input.getComponentsPatch();
    if (typeof components === "function") {
        components = components();
    }

    for (let entry of components.entrySet()) {
        let type = entry.getKey();
        let value = entry.getValue();
        if (type == null) continue;
        if (!value.isPresent()) continue;
        output.set(type, value.get());
    }
}

LycheeEvents.customAction('repair_item', event => {
    let durability = event.data.durability
    event.applyFunc = (recipe, ctx, times) => {
        let input = ctx.getItem(0)
        let material = ctx.getItem(1)
        let output = ctx.getItem(2)
        copyComponents(input, output)
        let cost = 0
        for (; cost < material.count && output.damaged; cost++) {
            output.setDamageValue(output.damageValue - durability)
        }
        ctx.get(LycheeContextKey.ANVIL).materialCost = cost
    }
})

LycheeEvents.customCondition('is_item_damaged', event => {
    let target = LycheeReference.fromJson(event.data, "target")
    event.setTestFunc((recipe, ctx, times) => {
        let indexes = recipe.getItemIndexes(target)
        return ctx.getItem(indexes.get(0)).damaged ? times : 0
    })
})

let $DataComponents = Java.loadClass("net.minecraft.core.component.DataComponents");
let $ArmorTrim = Java.loadClass("net.minecraft.world.item.armortrim.ArmorTrim");
let $Registries = Java.loadClass("net.minecraft.core.registries.Registries");
let $TrimMaterials = Java.loadClass("net.minecraft.world.item.armortrim.TrimMaterials");
let $TrimPatterns = Java.loadClass("net.minecraft.world.item.armortrim.TrimPatterns");

let trimPool = [$TrimPatterns.COAST, $TrimPatterns.SPIRE, $TrimPatterns.RIB, $TrimPatterns.SNOUT, $TrimPatterns.DUNE]

LycheeEvents.customAction('apply_random_trim', event => {
    event.applyFunc = (recipe, ctx, times) => {
        let input = ctx.getItem(0)
        let output = ctx.getItem(2)
        let random = ctx.get(LycheeContextKey.RANDOM)
        let level = ctx.get(LycheeContextKey.LEVEL)
        copyComponents(input, output)
        let registryAccess = level.registryAccess()
        let material = output.get($DataComponents.TRIM).material() ?? $TrimMaterials.COPPER
        let pattern = registryAccess.lookup($Registries.TRIM_PATTERN).orElseThrow().get(trimPool[random.nextInt(trimPool.length)]).orElseThrow()
        output.set($DataComponents.TRIM, new $ArmorTrim(material, pattern))
    }
})

LycheeEvents.customCondition('is_item_trimmed', event => {
    let target = LycheeReference.fromJson(event.data, 'target')
    event.testFunc = (recipe, ctx, times) => {
        let indexes = recipe.getItemIndexes(target)
        let stack = ctx.getItem(indexes.getInt(0))
        return stack == null ? 0 : (stack.get($DataComponents.TRIM) != null ? times : 0)
    }
})


let $DirectionPlane = Java.loadClass('net.minecraft.core.Direction$Plane')
let $LootContextParams = Java.loadClass('net.minecraft.world.level.storage.loot.parameters.LootContextParams')

LycheeEvents.customCondition('neighbor_block_boost', event => {
	let booster_block = event.data.booster_block

	event.testFunc = (recipe, ctx, times) => {
		let params = ctx.get(LycheeContextKey.LOOT_PARAMS)
		let item = params.get($LootContextParams.THIS_ENTITY)
		let count = item.lychee$getCount()
		if (count != 0) {
			return times
		}
		let pos = params.get(LycheeLootContextParams.BLOCK_POS)
		for (const direction of $DirectionPlane.HORIZONTAL) {
			let neighbor = ctx.get(LycheeContextKey.LEVEL).getBlock(pos.relative(direction))
			if (neighbor == booster_block) {
				count += 1
			}
		}
		item.lychee$setCount(count)
		console.info('Neighbor block boost: ' + count)
		return times
	}
})