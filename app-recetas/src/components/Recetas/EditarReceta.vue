<template>
    <div>
        <h2>Editar receta</h2>
        <form class = "form-floating" @submit.prevent="enviar()">
            <div class="form-floating mt-3 mb-3">
                <input id="name" 
                 type = "text" class="form-control" 
                 v-model="record.nombre" placeholder="Nombre">
                <label for="name">Nombre</label>  
            </div>

            <div class="form-floating mb-3 mt-3">
                <input id="dificultad" 
                 type = "text" class="form-control" 
                 v-model="record.dificultad" placeholder="Dificultad">
                <label for="dificultad">Dificultad</label> 
            </div>

            <div class="form-floating mb-3 mt-3">
                <input id="tiempoPreparacion" 
                 type = "text" class="form-control" 
                 v-model="record.tiempoPreparacion" placeholder="Tiempo de preparación">
                <label for="tiempoPreparacion">Tiempo de preparación</label> 
            </div>

            <div class="form-floating mb-3 mt-3">
                <input id="descripcion" 
                 type = "text" class="form-control" 
                 v-model="record.descripcion" placeholder="Descripción">
                <label for="descripcion">Descripción</label> 
            </div>
            <button type="submit" class="btn btn-primary">Aceptar</button>
            <button type="submit" class="btn btn-danger">Cancelar</button>
        </form>
    </div>
</template>

<script>
export default {
    name: 'EditarReceta',
    props: {
        receta: {
            type: Object,
            required: true,
        },
    },
    data() {
        return {
            record: {...this.receta}
        }
    },
    methods: {
        enviar() {
            console.log('Enviar::receta::%O', this.receta);
            console.log('Enviar::record::%O', this.record);
            this.$emit('update', this.record);
        }
    }
}
</script>

<style>

</style>