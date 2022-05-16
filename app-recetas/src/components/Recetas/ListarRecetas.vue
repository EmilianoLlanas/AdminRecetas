<template>
  <h2>Listar recetas</h2>
    <div class="list-group">
        <button
         type="button" 
         :class="getItemClass(receta)"
         v-for="(receta, index) in recetas"
         :key="receta.id"
         @click="enviarReceta(receta)">{{ index }} - {{ receta.nombre }}
        </button>
    </div>

</template>

<script>
//import {listarRecetas} from './helper/recetasService'

export default {
    name: 'ListarRecetas',
    props: {
        recetas: {
            type: Array,
            required: true,
        }
    },
    data(){
        return{
            //recetas: [], 
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
        enviarReceta(recipe) {
            this.selectedId = recipe.id;
            console.log(recipe);
            this.$emit('change', recipe);
        }, 
        getItemClass(recipe) {
            let style = "list-group-item list-group-item-action";

            if(this.selectedId === recipe.id){
                style = style + " active";
            }
            
            return style;
        }
    }
}
</script>

<style>

</style>