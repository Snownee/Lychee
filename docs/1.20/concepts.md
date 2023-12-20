# Concepts

## Repeatability

Repeatability means this interaction can be bulk processed. The ["place block" action](post-action.md#place-block-place) cancels repeatability because it is nonsense to place single block at a position for multiple times.

For example, in the case of logs burning into charcoals, if you burn a stack of logs, it will repeat the post actions for 64 times and drop 64 charcoals. But if you add a "place block" action to the recipe, it becomes unrepeatable and will only drop one charcoal.
