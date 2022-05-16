<template>
    <div class="container">
        <h1>Recetas Page </h1>
        <div class="row">
            <div class="col-sm-10 col-8">
                <MostrarReceta v-if="!edit && receta" :receta="receta" @editar="editarReceta"/>
                <EditarReceta v-if="edit" :receta="receta" @update="actualizarReceta"/>
            </div>
            <div class="col-sm-2 col-4">
                <ListarRecetas @change="mostrarReceta" :recetas="recetas"/>
            </div>
        </div>
    </div>
</template>

<script>
import MostrarReceta from './MostrarReceta.vue'
import EditarReceta from './EditarReceta.vue'
import ListarRecetas from './ListarRecetas.vue'
import { actualizarReceta, listarRecetas } from './helper/recetasService';

export default {
    name: 'RecetasPage',
    data() {
        return{
            recetas: [],
            receta: null,
            edit: false
        } 
    },
    components: {
        MostrarReceta,
        EditarReceta,
        ListarRecetas
    },
    methods: {
    async fetchRecetas(){
        this.recetas = await listarRecetas();
    },
      mostrarReceta(recipe) {
        //console.log("mostrarReceta:recipe:%O", recipe)
        this.receta = recipe;
        //this.edit = true;
    },
    editarReceta(recipe) { 
        //console.log("mostrarReceta:recipe:%O", recipe)
        this.edit = true; 
        this.receta = recipe;
    },
    async actualizarReceta(recipe) {
        const response = await actualizarReceta(recipe.id, recipe);
        console.log(response);
        this.edit = false;
        this.receta = recipe;
        this.recetas=this.recetas.map(receta=> {
            if(receta.id === recipe.id){
                return recipe;
            }else{
                return receta;
            }
        });
    }
  },
  mounted() {
        this.fetchRecetas();
  }
}
</script>

<style>

</style>