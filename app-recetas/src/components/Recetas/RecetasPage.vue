<template>
    <div class="container">
        <h1>Recetas Page</h1>
        <div class="row">
            <div class="col-sm-10 col-8">
                <MostrarReceta/>
                <EditarReceta/>
            </div>
            <div class="col-sm-2 col-4">
                <ListarRecetas @change="mostrarReceta"/>
            </div>
        </div>
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
  },
  methods: {
      mostrarReceta(recipe) {
          console.log("mostrarReceta:recipe:%O", recipe)
      }
  }
}
</script>

<style>

</style>