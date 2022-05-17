<template>
    <div class = "container">
        <div class = "row justify-content-center">
            <div class =  "col-12">
                <div class="card">
                    <div class = "container">
                        <div class = "row justify-content-center">
                            <img
                                :src = "receta.url"
                                class = "card-img-top img-fluid w-50"
                                alt = ""
                            />
                        </div>
                    </div>
                    <div class = "card-body">
                        <h5 class="card-title">{{receta.nombre}}</h5>
                        <p class="card-text">Dificultad: {{getStars(receta.dificultad)}}</p>
                        <p class="card-text">Tiempo de preparación: {{receta.tiempoPreparacion}}</p>
                        <div class="card-text"> 
                            Ingredientes:
                            <div class="list-group">
                                <button
                                    type="button"
                                    v-for="(racion, index) in receta.raciones"
                                    class="list-group-item list-group-item-action"
                                    :key="racion.id"
                                >
                                    {{ index + 1 }} - {{ racion.ingrediente.nombre}}: {{racion.cantidad}}
                                </button>
                            </div>
                        </div>
                        <p class="card-text mt-2">Descripción: {{receta.descripcion}}</p>
                        <div class="m-2">
                            <button class="btn btn-primary m-2" @click="$emit('editar', receta)">Editar</button>
                            <button class="btn btn-danger m-2" @click="$emit('eliminar',receta)">Eliminar</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
const starImage = String.fromCodePoint(0X2850);

export default {
    name: 'RecetasPage',
    props: {
        receta: {
            type: Object,
            required: true,
        }
    },
    methods: {
        getStars(total){
            console.log("Estrellas")
            let printStars = "";
            for(let i = 0; i<total; i++){
                printStars = printStars + starImage + " ";
            }
            return printStars;
        }
    }
};
</script>

<style>

</style>