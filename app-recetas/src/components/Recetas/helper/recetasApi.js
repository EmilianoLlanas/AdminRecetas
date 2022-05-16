import axios from 'axios'

const recetasApi = axios.create({
    baseURL: 'http://localhost:8080/receta-controller/recetas',
    timeout: 5000,
    headers: {
        "Content-type": "application/json"
    }
});

export {
    recetasApi
} 