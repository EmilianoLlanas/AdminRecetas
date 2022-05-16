<template>
  <h2>Listar recetas</h2>
    <div class="list-group">
        <button
         type="button" 
         :class="itemClass"
         v-for="(receta, index) in recetas"
         :key="receta.id"
         @click="enviarReceta(receta)">{{ index }} - {{ receta.nombre }}
        </button>
    </div>

</template>

<script>
import {listarRecetas} from './helper/recetasService'

export default {
    name: 'ListarRecetas',
    data(){
        return{
            recetas: [], 
            selectedId: null
        }
    },
    computed: {
        itemClass(){
            console.log('itemClass::selectedId::%O', this.selectedId)
            let style = "list-group-item list-group-item-action";
            for(let i = 0; i<this.recetas.size; i++){

                let receta =  this.recetas[i];
                console.log('itemClass::receta::%O', receta)

                if(this.selectedId === this.receta.id){
                    style = style + "active";
                }
            }

            return style;
        } 
    },
    methods: {
        async fetchRecetas() {
            this.recetas = await listarRecetas();
            console.log('Listar recetas: %O', this.recetas)
        },
        enviarReceta(recipe) {
            this.selectedId = recipe.id;
            this.$emit('change', this.receta);
        }
    },
    mounted() {
        this.fetchRecetas();
    }
}
</script>

<style>

</style>