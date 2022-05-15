<template>
    <div>
        <h1>Receta Page</h1>
        <MostrarReceta/>
        <EditarReceta/>
        <ListarRecetas/>
    </div>
</template>

<script>
import MostrarReceta from './MostrarReceta.vue'
import EditarReceta from './EditarReceta.vue'
import ListarRecetas from './ListarRecetas.vue'

export default {
    name: 'RecetasPage',
    components: {
    MostrarReceta,
    EditarReceta,
    ListarRecetas
  }
}
</script>

<style>

</style>